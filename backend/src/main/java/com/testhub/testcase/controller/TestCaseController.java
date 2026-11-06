package com.testhub.testcase.controller;

import com.testhub.common.PageResult;
import com.testhub.common.R;
import com.testhub.testcase.dto.TestCaseQuery;
import com.testhub.testcase.entity.TestCase;
import com.testhub.testcase.service.TestCaseService;
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

/** 测试用例接口。 */
@Tag(name = "05. 测试用例管理")
@RestController
@RequestMapping("/api/test-cases")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    @Operation(summary = "分页查询用例")
    @GetMapping
    public R<PageResult<TestCase>> page(TestCaseQuery query) {
        return R.ok(testCaseService.page(query));
    }

    @Operation(summary = "查询可加入计划的用例")
    @GetMapping("/candidates")
    public R<List<TestCase>> candidates(@RequestParam(required = false) Long projectId) {
        return R.ok(testCaseService.listByPlanCandidates(projectId));
    }

    @Operation(summary = "用例详情")
    @GetMapping("/{id}")
    public R<TestCase> detail(@PathVariable Long id) {
        return R.ok(testCaseService.get(id));
    }

    @Operation(summary = "新增用例")
    @PostMapping
    public R<TestCase> create(@Valid @RequestBody TestCase testCase) {
        return R.ok(testCaseService.create(testCase));
    }

    @Operation(summary = "修改用例")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TestCase testCase) {
        testCaseService.update(id, testCase);
        return R.ok();
    }

    @Operation(summary = "复制用例")
    @PostMapping("/{id}/copy")
    public R<TestCase> copy(@PathVariable Long id) {
        return R.ok(testCaseService.copy(id));
    }

    @Operation(summary = "删除用例")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        testCaseService.delete(id);
        return R.ok();
    }

    @Operation(summary = "批量删除用例")
    @DeleteMapping("/batch")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        testCaseService.batchDelete(ids);
        return R.ok();
    }
}
