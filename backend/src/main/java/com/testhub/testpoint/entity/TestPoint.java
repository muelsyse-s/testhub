package com.testhub.testpoint.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 测试点：需求与用例之间的设计单元。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_point")
public class TestPoint extends BaseEntity {

    private Long projectId;
    private String module;
    private String pointNo;
    private String title;
    private String description;
    private String priority;
    private String riskLevel;
    private String status;
    private String sourceType;
    private Long creatorId;
    private String creatorName;

    @TableField(exist = false)
    private Long caseCount;
}
