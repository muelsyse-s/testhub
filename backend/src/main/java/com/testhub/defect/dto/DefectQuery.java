package com.testhub.defect.dto;

import com.testhub.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 缺陷查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DefectQuery extends PageQuery {

    private Long projectId;
    private String status;
    private String severity;
    private String priority;
    private Long assigneeId;
}
