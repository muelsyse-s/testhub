package com.testhub.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 测试环境。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("env_info")
public class EnvInfo extends BaseEntity {

    private Long projectId;
    private String name;
    private String baseUrl;
    private String headers;
    private String variables;
    private Integer enabled;
}
