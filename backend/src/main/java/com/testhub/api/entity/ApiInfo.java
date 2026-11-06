package com.testhub.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 接口定义。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_info")
public class ApiInfo extends BaseEntity {

    private Long projectId;
    private String module;
    private String name;
    private String method;
    private String path;
    private String headers;
    private String params;
    private String bodyType;
    private String body;
    private String description;
    private String status;
}
