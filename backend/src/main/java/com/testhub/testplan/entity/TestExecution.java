package com.testhub.testplan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 测试执行记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_execution")
public class TestExecution extends BaseEntity {

    private Long projectId;
    private Long planId;
    private Long caseId;
    private Long executorId;
    private String executorName;
    private String result;
    private String actualResult;
    private Long durationMs;
    private LocalDateTime executedAt;
    private String remark;
}
