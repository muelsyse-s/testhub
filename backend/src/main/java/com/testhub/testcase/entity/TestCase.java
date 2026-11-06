package com.testhub.testcase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 测试用例。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_case")
public class TestCase extends BaseEntity {

    private Long projectId;
    private Long testPointId;
    private String caseNo;
    private String title;
    private String precondition;
    /** JSON 数组字符串，元素为步骤描述。 */
    private String steps;
    private String expectedResult;
    private String priority;
    private String caseType;
    private String designMethod;
    private String status;
    private Integer autoFlag;
    private Long creatorId;
    private String creatorName;
}
