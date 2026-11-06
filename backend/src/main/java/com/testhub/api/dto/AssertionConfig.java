package com.testhub.api.dto;

import lombok.Data;

/** 单条断言配置。 */
@Data
public class AssertionConfig {

    /** STATUS_CODE / JSON_PATH / RESPONSE_TIME / BODY_CONTAINS。 */
    private String type;
    /** JSONPath 表达式，或状态码/响应耗时的期望值。 */
    private String expression;
    /** EQ / NEQ / CONTAINS / NOT_EMPTY / GT / LT。 */
    private String operator;
    private String expected;
    private String message;
}
