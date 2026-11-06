package com.testhub.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 测试项目。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project")
public class Project extends BaseEntity {

    private String code;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerName;
    private String status;
}
