package com.testhub.testplan.controller;

import com.testhub.common.PageQuery;
import com.testhub.common.PageResult;
import com.testhub.common.R;
import com.testhub.testplan.entity.TestExecution;
import com.testhub.testplan.service.ExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 执行记录接口。 */
@Tag(name = "07. 测试执行")
@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    @Operation(summary = "分页查询执行记录")
    @GetMapping
    public R<PageResult<TestExecution>> page(PageQuery query,
                                             @RequestParam(required = false) Long planId,
                                             @RequestParam(required = false) Long projectId,
                                             @RequestParam(required = false) String result) {
        return R.ok(executionService.page(query, planId, projectId, result));
    }

    @Operation(summary = "提交执行结果")
    @PostMapping
    public R<TestExecution> record(@RequestBody ExecutionService.ExecutionRequest request) {
        return R.ok(executionService.record(request));
    }

    @Operation(summary = "查询某用例的历史执行记录")
    @GetMapping("/case/{caseId}")
    public R<List<TestExecution>> byCase(@PathVariable Long caseId) {
        return R.ok(executionService.listByCase(caseId));
    }
}
