package com.testhub.api.dto;

import lombok.Data;

/** 断言执行结果。 */
@Data
public class AssertionResult {

    private String message;
    private boolean passed;
    private String actual;
    private String expected;
}
