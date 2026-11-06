package com.testhub.report.controller;

import com.testhub.common.R;
import com.testhub.report.dto.MemberWorkload;
import com.testhub.report.dto.ModuleCoverage;
import com.testhub.report.dto.NameValue;
import com.testhub.report.dto.TrendItem;
import com.testhub.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 质量报表接口。 */
@Tag(name = "12. 质量报表")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "总览指标")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview(@RequestParam(required = false) Long projectId) {
        return R.ok(reportService.overview(projectId));
    }

    @Operation(summary = "每日执行趋势")
    @GetMapping("/execution-trend")
    public R<List<TrendItem>> trend(@RequestParam(required = false) Long projectId,
                                    @RequestParam(defaultValue = "14") int days) {
        return R.ok(reportService.executionTrend(projectId, days));
    }

    @Operation(summary = "用例优先级分布")
    @GetMapping("/case-priority")
    public R<List<NameValue>> casePriority(@RequestParam(required = false) Long projectId) {
        return R.ok(reportService.casePriority(projectId));
    }

    @Operation(summary = "用例类型分布")
    @GetMapping("/case-type")
    public R<List<NameValue>> caseType(@RequestParam(required = false) Long projectId) {
        return R.ok(reportService.caseType(projectId));
    }

    @Operation(summary = "缺陷严重程度分布")
    @GetMapping("/defect-severity")
    public R<List<NameValue>> defectSeverity(@RequestParam(required = false) Long projectId) {
        return R.ok(reportService.defectSeverity(projectId));
    }

    @Operation(summary = "缺陷状态分布")
    @GetMapping("/defect-status")
    public R<List<NameValue>> defectStatus(@RequestParam(required = false) Long projectId) {
        return R.ok(reportService.defectStatus(projectId));
    }

    @Operation(summary = "模块覆盖情况")
    @GetMapping("/module-coverage")
    public R<List<ModuleCoverage>> moduleCoverage(@RequestParam(required = false) Long projectId) {
        return R.ok(reportService.moduleCoverage(projectId));
    }

    @Operation(summary = "成员执行工作量")
    @GetMapping("/workload")
    public R<List<MemberWorkload>> workload(@RequestParam(required = false) Long projectId) {
        return R.ok(reportService.workload(projectId));
    }
}
