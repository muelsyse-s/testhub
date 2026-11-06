package com.testhub.testplan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 测试计划与用例的关联。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_case")
public class PlanCase extends BaseEntity {

    private Long planId;
    private Long caseId;
    private Long assigneeId;
    private String assigneeName;
    private Integer sortNo;
}
