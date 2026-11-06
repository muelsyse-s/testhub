package com.testhub.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 通用「名称-数值」统计项，供饼图/柱状图使用。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameValue {

    private String name;
    private Long value;
}
