package com.testhub.report.dto;

import lombok.Data;

import java.time.LocalDate;

/** 每日执行趋势。 */
@Data
public class TrendItem {

    private LocalDate statDate;
    private Long total;
    private Long passCount;
    private Long failCount;
}
