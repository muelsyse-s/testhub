package com.testhub.testcase.dto;

import com.testhub.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用例查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TestCaseQuery extends PageQuery {

    private Long projectId;
    private Long testPointId;
    private String priority;
    private String caseType;
    private String status;
    private Integer autoFlag;
}
