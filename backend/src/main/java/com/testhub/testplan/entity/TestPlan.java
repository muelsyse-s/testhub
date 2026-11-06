package com.testhub.testplan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 测试计划。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_plan")
public class TestPlan extends BaseEntity {

    private Long projectId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long ownerId;
    private String ownerName;
}
