package com.testhub.defect.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 缺陷。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("defect")
public class Defect extends BaseEntity {

    private Long projectId;
    private Long caseId;
    private Long executionId;
    private String defectNo;
    private String title;
    private String description;
    private String severity;
    private String priority;
    private String status;
    private Long assigneeId;
    private String assigneeName;
    private Long reporterId;
    private String reporterName;
    private LocalDateTime resolvedAt;
}
