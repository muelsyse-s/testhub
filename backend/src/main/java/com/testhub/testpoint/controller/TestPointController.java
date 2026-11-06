package com.testhub.testpoint.controller;

import com.testhub.common.PageResult;
import com.testhub.common.R;
import com.testhub.testpoint.dto.TestPointQuery;
import com.testhub.testpoint.entity.TestPoint;
import com.testhub.testpoint.service.TestPointService;
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

/** 测试点接口。 */
@Tag(name = "04. 测试点管理")
@RestController
@RequestMapping("/api/test-points")
@RequiredArgsConstructor
public class TestPointController {

    private final TestPointService testPointService;

    @Operation(summary = "分页查询测试点")
    @GetMapping
    public R<PageResult<TestPoint>> page(TestPointQuery query) {
        return R.ok(testPointService.page(query));
    }

    @Operation(summary = "查询模块列表")
    @GetMapping("/modules")
    public R<List<String>> modules(@RequestParam(required = false) Long projectId) {
        return R.ok(testPointService.listModules(projectId));
    }

    @Operation(summary = "测试点详情")
    @GetMapping("/{id}")
    public R<TestPoint> detail(@PathVariable Long id) {
        return R.ok(testPointService.get(id));
    }

    @Operation(summary = "新增测试点")
    @PostMapping
    public R<TestPoint> create(@Valid @RequestBody TestPoint point) {
        return R.ok(testPointService.create(point));
    }

    @Operation(summary = "修改测试点")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TestPoint point) {
        testPointService.update(id, point);
        return R.ok();
    }

    @Operation(summary = "删除测试点")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        testPointService.delete(id);
        return R.ok();
    }
}
