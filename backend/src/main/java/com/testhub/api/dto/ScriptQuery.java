package com.testhub.api.dto;

import com.testhub.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 脚本查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScriptQuery extends PageQuery {

    private Long projectId;
    private Long apiId;
    private String scriptType;
    private Integer enabled;
}
