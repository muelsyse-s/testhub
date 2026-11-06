package com.testhub.report.dto;

import lombok.Data;

/** 成员执行工作量。 */
@Data
public class MemberWorkload {

    private String name;
    private Long executed;
    private Long passCount;
    private Long failCount;
}
