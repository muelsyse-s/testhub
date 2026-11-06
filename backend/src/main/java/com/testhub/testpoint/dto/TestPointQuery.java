package com.testhub.testpoint.dto;

import com.testhub.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 测试点查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TestPointQuery extends PageQuery {

    private Long projectId;
    private String module;
    private String priority;
    private String status;
    private String riskLevel;
    private String sourceType;
}
