package com.testhub.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 接口脚本执行日志。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_run_log")
public class ApiRunLog extends BaseEntity {

    private Long scriptId;
    private Long projectId;
    private Long envId;
    private String scriptName;
    private String status;
    private Integer statusCode;
    private Long durationMs;
    private String requestUrl;
    private String responseBody;
    private String failMessage;
    private LocalDateTime executedAt;
}
