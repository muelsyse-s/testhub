package com.testhub.testplan.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 计划内用例视图。 */
@Data
public class PlanCaseVO {

    private Long planCaseId;
    private Long planId;
    private Long caseId;
    private String caseNo;
    private String title;
    private String priority;
    private String caseType;
    private String assigneeName;
    private String lastResult;
    private LocalDateTime lastExecutedAt;
    private Integer execCount;
}
