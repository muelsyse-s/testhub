package com.testhub.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 接口脚本执行结果。 */
@Data
public class ApiRunResult {

    private Long scriptId;
    private String scriptName;
    private String status;
    private Integer statusCode;
    private Long durationMs;
    private String requestUrl;
    private String method;
    private String responseBody;
    private String failMessage;
    private LocalDateTime executedAt;
    private List<AssertionResult> assertionResults = new ArrayList<>();
    private java.util.Map<String, String> extractedVars = new java.util.LinkedHashMap<>();

    public boolean isPassed() {
        return "PASS".equals(status);
    }
}
