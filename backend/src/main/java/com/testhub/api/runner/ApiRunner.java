package com.testhub.api.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.testhub.api.dto.ApiRunResult;
import com.testhub.api.dto.AssertionConfig;
import com.testhub.api.dto.AssertionResult;
import com.testhub.api.dto.ExtractorConfig;
import com.testhub.api.entity.ApiRunLog;
import com.testhub.api.entity.ApiScript;
import com.testhub.api.entity.EnvInfo;
import com.testhub.api.mapper.ApiRunLogMapper;
import com.testhub.api.mapper.ApiScriptMapper;
import com.testhub.api.mapper.EnvInfoMapper;
import com.testhub.common.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 轻量接口脚本执行器。
 *
 * <p>脚本由 {@code requestConfig}、{@code assertions}、{@code extractors} 三段 JSON 描述，
 * 执行时按「变量替换 -&gt; 发送请求 -&gt; 断言校验 -&gt; 变量提取 -&gt; 落库日志」的顺序处理，
 * 从而实现登录后取 token、下单后取单号这类接口依赖场景。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiRunner {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");

    private final ObjectMapper objectMapper;
    private final ApiScriptMapper scriptMapper;
    private final EnvInfoMapper envMapper;
    private final ApiRunLogMapper runLogMapper;

    @Value("${testhub.api-runner.connect-timeout:10}")
    private long connectTimeout;

    @Value("${testhub.api-runner.read-timeout:30}")
    private long readTimeout;

    @Value("${testhub.api-runner.max-response-length:20000}")
    private int maxResponseLength;

    /** 跨脚本共享的全局变量（内存实现，重启失效）。 */
    private final Map<String, String> globalVariables = new ConcurrentHashMap<>();

    public Map<String, String> getGlobalVariables() {
        return Map.copyOf(globalVariables);
    }

    public void clearGlobalVariables() {
        globalVariables.clear();
    }

    @SuppressWarnings("unchecked")
    public ApiRunResult run(Long scriptId, Long envId) {
        ApiScript script = scriptMapper.selectById(scriptId);
        if (script == null) {
            throw new BizException(404, "接口脚本不存在");
        }
        ApiRunResult result = new ApiRunResult();
        result.setScriptId(script.getId());
        result.setScriptName(script.getName());
        result.setExecutedAt(LocalDateTime.now());

        Long finalEnvId = envId != null ? envId : script.getEnvId();
        EnvInfo env = finalEnvId == null ? null : envMapper.selectById(finalEnvId);

        Map<String, String> variables = new LinkedHashMap<>(globalVariables);
        variables.putAll(parseFlatJson(script.getVariables()));
        if (env != null) {
            variables.putAll(parseFlatJson(env.getVariables()));
        }

        Map<String, Object> requestConfig;
        try {
            requestConfig = StringUtils.hasText(script.getRequestConfig())
                    ? objectMapper.readValue(script.getRequestConfig(), new TypeReference<Map<String, Object>>() {
                    })
                    : new LinkedHashMap<>();
        } catch (Exception e) {
            throw new BizException("请求配置不是合法 JSON：" + e.getMessage());
        }

        String method = String.valueOf(requestConfig.getOrDefault("method", "GET")).toUpperCase();
        String rawPath = String.valueOf(requestConfig.getOrDefault("path", "/"));
        String path = replaceVariables(rawPath, variables);
        String baseUrl = env == null ? "" : env.getBaseUrl();
        String url = path.startsWith("http") ? path : trimEnd(baseUrl) + ensureStart(path);

        Map<String, String> headers = new LinkedHashMap<>();
        if (env != null) {
            parseFlatJson(env.getHeaders())
                    .forEach((k, v) -> headers.put(k, replaceVariables(v, variables)));
        }
        Object rawHeaders = requestConfig.get("headers");
        if (rawHeaders instanceof Map<?, ?> map) {
            map.forEach((k, v) -> headers.put(String.valueOf(k),
                    replaceVariables(String.valueOf(v), variables)));
        }

        String body = null;
        Object rawBody = requestConfig.get("body");
        if (rawBody != null && !"NONE".equalsIgnoreCase(String.valueOf(rawBody))) {
            body = replaceVariables(rawBody instanceof String s ? s : toJson(rawBody), variables);
            headers.putIfAbsent("Content-Type", "application/json");
        }

        result.setMethod(method);
        result.setRequestUrl(url);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(readTimeout));
        headers.forEach(builder::header);
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        builder.method(method, publisher);

        long start = System.currentTimeMillis();
        HttpResponse<String> response;
        try {
            response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            result.setDurationMs(cost);
            result.setStatus("ERROR");
            result.setFailMessage("请求发送失败：" + e.getMessage());
            saveLog(script, finalEnvId, result);
            return result;
        }
        long cost = System.currentTimeMillis() - start;

        String responseBody = response.body() == null ? "" : response.body();
        if (responseBody.length() > maxResponseLength) {
            responseBody = responseBody.substring(0, maxResponseLength) + "...(已截断)";
        }
        result.setStatusCode(response.statusCode());
        result.setDurationMs(cost);
        result.setResponseBody(responseBody);

        // 断言
        List<AssertionConfig> assertions = parseList(script.getAssertions(), AssertionConfig.class);
        List<AssertionResult> assertionResults = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (AssertionConfig assertion : assertions) {
            AssertionResult ar = evaluate(assertion, response, responseBody, cost);
            assertionResults.add(ar);
            if (!ar.isPassed()) {
                failures.add(ar.getMessage());
            }
        }
        result.setAssertionResults(assertionResults);

        // 变量提取
        List<ExtractorConfig> extractors = parseList(script.getExtractors(), ExtractorConfig.class);
        if (!extractors.isEmpty()) {
            try {
                for (ExtractorConfig extractor : extractors) {
                    Object value = readJsonPath(responseBody, extractor.getExpression());
                    if (value != null) {
                        String text = String.valueOf(value);
                        result.getExtractedVars().put(extractor.getName(), text);
                        globalVariables.put(extractor.getName(), text);
                        log.info("提取变量 {} = {}", extractor.getName(), text);
                    }
                }
            } catch (Exception e) {
                failures.add("变量提取失败：" + e.getMessage());
            }
        }

        if (failures.isEmpty()) {
            result.setStatus("PASS");
        } else {
            result.setStatus("FAIL");
            result.setFailMessage(String.join("；", failures));
        }
        saveLog(script, finalEnvId, result);
        return result;
    }

    private AssertionResult evaluate(AssertionConfig config, HttpResponse<String> response,
                                     String body, long cost) {
        AssertionResult result = new AssertionResult();
        String type = config.getType() == null ? "JSON_PATH" : config.getType();
        String operator = config.getOperator() == null ? "EQ" : config.getOperator();
        String expected = config.getExpected() != null ? config.getExpected() : config.getExpression();
        String message = StringUtils.hasText(config.getMessage()) ? config.getMessage() : type + " 断言";
        result.setMessage(message);
        result.setExpected("NOT_EMPTY".equals(operator) ? "非空" : expected);

        try {
            switch (type) {
                case "STATUS_CODE" -> {
                    String actual = String.valueOf(response.statusCode());
                    result.setActual(actual);
                    result.setPassed("EQ".equals(operator) ? actual.equals(expected) : !actual.equals(expected));
                }
                case "RESPONSE_TIME" -> {
                    String actual = String.valueOf(cost);
                    result.setActual(actual);
                    result.setPassed(compareNumber(cost, Long.parseLong(expected), operator));
                }
                case "BODY_CONTAINS" -> {
                    boolean contains = body.contains(expected == null ? "" : expected);
                    result.setActual(contains ? "包含" : "不包含");
                    result.setPassed(contains);
                }
                default -> {
                    Object value = readJsonPath(body, config.getExpression());
                    String actual = value == null ? "null" : String.valueOf(value);
                    result.setActual(actual);
                    result.setPassed(compareValue(actual, expected, operator));
                }
            }
        } catch (Exception e) {
            result.setPassed(false);
            result.setActual("断言执行异常：" + e.getMessage());
        }
        return result;
    }

    private boolean compareValue(String actual, String expected, String operator) {
        if ("NOT_EMPTY".equals(operator)) {
            return actual != null && !actual.isBlank() && !"null".equals(actual);
        }
        return switch (operator) {
            case "NEQ" -> !actual.equals(expected);
            case "CONTAINS" -> actual.contains(expected == null ? "" : expected);
            case "GT" -> compareNumber(parseLongSafe(actual), parseLongSafe(expected), "GT");
            case "LT" -> compareNumber(parseLongSafe(actual), parseLongSafe(expected), "LT");
            default -> actual.equals(expected);
        };
    }

    private boolean compareNumber(long actual, long expected, String operator) {
        return switch (operator) {
            case "GT" -> actual > expected;
            case "LT" -> actual < expected;
            case "NEQ" -> actual != expected;
            default -> actual == expected;
        };
    }

    private long parseLongSafe(String value) {
        try {
            return (long) Double.parseDouble(value);
        } catch (Exception e) {
            return 0L;
        }
    }

    private Object readJsonPath(String body, String expression) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        try {
            return JsonPath.read(body, expression);
        } catch (Exception e) {
            return null;
        }
    }

    private void saveLog(ApiScript script, Long envId, ApiRunResult result) {
        ApiRunLog runLog = new ApiRunLog();
        runLog.setScriptId(script.getId());
        runLog.setProjectId(script.getProjectId());
        runLog.setEnvId(envId);
        runLog.setScriptName(script.getName());
        runLog.setStatus(result.getStatus());
        runLog.setStatusCode(result.getStatusCode());
        runLog.setDurationMs(result.getDurationMs());
        runLog.setRequestUrl(result.getRequestUrl());
        runLog.setResponseBody(result.getResponseBody());
        runLog.setFailMessage(result.getFailMessage());
        runLog.setExecutedAt(result.getExecutedAt());
        runLogMapper.insert(runLog);

        script.setLastRunStatus(result.getStatus());
        script.setLastRunAt(result.getExecutedAt());
        scriptMapper.updateById(script);
    }

    private <T> List<T> parseList(String json, Class<T> clazz) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new BizException("脚本配置不是合法 JSON 数组：" + e.getMessage());
        }
    }

    private Map<String, String> parseFlatJson(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (!StringUtils.hasText(json)) {
            return result;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            map.forEach((k, v) -> result.put(k, v == null ? "" : String.valueOf(v)));
        } catch (Exception e) {
            log.warn("变量配置解析失败: {}", e.getMessage());
        }
        return result;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    /** 把 {{var}} 替换为变量值，未定义的变量原样保留以便定位问题。 */
    public String replaceVariables(String text, Map<String, String> variables) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        Matcher matcher = VAR_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.get(key);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value == null ? matcher.group() : value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String trimEnd(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String ensureStart(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }
}
