package com.testhub.testplan.controller;

import com.testhub.common.PageQuery;
import com.testhub.common.PageResult;
import com.testhub.common.R;
import com.testhub.testplan.dto.PlanCaseVO;
import com.testhub.testplan.dto.PlanProgress;
import com.testhub.testplan.entity.TestPlan;
import com.testhub.testplan.service.TestPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 测试计划接口。 */
@Tag(name = "06. 测试计划")
@RestController
@RequestMapping("/api/test-plans")
@RequiredArgsConstructor
public class TestPlanController {

    private final TestPlanService testPlanService;

    @Operation(summary = "分页查询测试计划")
    @GetMapping
    public R<PageResult<TestPlan>> page(PageQuery query,
                                        @RequestParam(required = false) Long projectId,
                                        @RequestParam(required = false) String status) {
        return R.ok(testPlanService.page(query, projectId, status));
    }

    @Operation(summary = "计划详情")
    @GetMapping("/{id}")
    public R<TestPlan> detail(@PathVariable Long id) {
        return R.ok(testPlanService.get(id));
    }

    @Operation(summary = "新增计划")
    @PostMapping
    public R<TestPlan> create(@Valid @RequestBody TestPlan plan) {
        return R.ok(testPlanService.create(plan));
    }

    @Operation(summary = "修改计划")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TestPlan plan) {
        testPlanService.update(id, plan);
        return R.ok();
    }

    @Operation(summary = "删除计划")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        testPlanService.delete(id);
        return R.ok();
    }

    @Operation(summary = "查询计划内用例及最近执行结果")
    @GetMapping("/{id}/cases")
    public R<List<PlanCaseVO>> cases(@PathVariable Long id) {
        return R.ok(testPlanService.listPlanCases(id));
    }

    @Operation(summary = "向计划批量加入用例")
    @PostMapping("/{id}/cases")
    public R<Map<String, Integer>> addCases(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("caseIds");
        List<Long> caseIds = rawIds == null ? List.of() : rawIds.stream().map(Integer::longValue).toList();
        Long assigneeId = body.get("assigneeId") == null ? null : Long.valueOf(body.get("assigneeId").toString());
        String assigneeName = body.get("assigneeName") == null ? null : body.get("assigneeName").toString();
        int added = testPlanService.addCases(id, caseIds, assigneeId, assigneeName);
        return R.ok(Map.of("added", added));
    }

    @Operation(summary = "从计划中移除用例")
    @DeleteMapping("/{id}/cases/{caseId}")
    public R<Void> removeCase(@PathVariable Long id, @PathVariable Long caseId) {
        testPlanService.removeCase(id, caseId);
        return R.ok();
    }

    @Operation(summary = "计划执行进度")
    @GetMapping("/{id}/progress")
    public R<PlanProgress> progress(@PathVariable Long id) {
        return R.ok(testPlanService.progress(id));
    }
}
