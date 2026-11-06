package com.testhub.api.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 变量替换逻辑单元测试。
 *
 * <p>覆盖正常替换、变量缺失保留原文、特殊字符（$ 与 \）不被正则替换破坏三种情况。</p>
 */
class ApiRunnerVariableTest {

    private final ApiRunner runner = new ApiRunner(new ObjectMapper(), null, null, null);

    @Test
    @DisplayName("已定义变量应被替换")
    void shouldReplaceDefinedVariable() {
        Map<String, String> vars = new HashMap<>();
        vars.put("token", "abc123");
        assertThat(runner.replaceVariables("Bearer {{token}}", vars)).isEqualTo("Bearer abc123");
    }

    @Test
    @DisplayName("未定义变量保留原样，便于定位配置问题")
    void shouldKeepUnknownVariable() {
        assertThat(runner.replaceVariables("Bearer {{missing}}", Map.of())).isEqualTo("Bearer {{missing}}");
    }

    @Test
    @DisplayName("变量值中的 $ 与反斜杠不应被当作正则分组")
    void shouldEscapeSpecialCharacters() {
        Map<String, String> vars = new HashMap<>();
        vars.put("pwd", "a$1\\b");
        assertThat(runner.replaceVariables("{{pwd}}", vars)).isEqualTo("a$1\\b");
    }

    @Test
    @DisplayName("同一变量出现多次应全部替换")
    void shouldReplaceAllOccurrences() {
        Map<String, String> vars = new HashMap<>();
        vars.put("id", "1001");
        assertThat(runner.replaceVariables("/api/item/{{id}}/detail?no={{id}}", vars))
                .isEqualTo("/api/item/1001/detail?no=1001");
    }
}
