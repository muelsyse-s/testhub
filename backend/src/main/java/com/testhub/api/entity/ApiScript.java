package com.testhub.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 接口自动化脚本。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_script")
public class ApiScript extends BaseEntity {

    private Long projectId;
    private Long apiId;
    private String name;
    private String scriptType;
    private Long envId;
    /** 请求配置 JSON：method/path/headers/body。 */
    private String requestConfig;
    /** 断言配置 JSON 数组。 */
    private String assertions;
    /** 变量提取配置 JSON 数组。 */
    private String extractors;
    private String variables;
    private Integer enabled;
    private String lastRunStatus;
    private LocalDateTime lastRunAt;
}
