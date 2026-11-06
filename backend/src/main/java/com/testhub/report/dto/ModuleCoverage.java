package com.testhub.report.dto;

import lombok.Data;

/** 模块级测试点/用例覆盖。 */
@Data
public class ModuleCoverage {

    private String module;
    private Long pointCount;
    private Long caseCount;
}
