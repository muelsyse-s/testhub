package com.testhub.api.dto;

import com.testhub.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 接口查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiQuery extends PageQuery {

    private Long projectId;
    private String module;
    private String method;
    private String status;
}
