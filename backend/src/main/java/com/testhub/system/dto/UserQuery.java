package com.testhub.system.dto;

import com.testhub.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    private Integer status;
    private String role;
}
