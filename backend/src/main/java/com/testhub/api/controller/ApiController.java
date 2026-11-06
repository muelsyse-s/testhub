package com.testhub.api.controller;

import com.testhub.api.dto.ApiQuery;
import com.testhub.api.dto.ApiRunResult;
import com.testhub.api.entity.ApiInfo;
import com.testhub.api.service.ApiInfoService;
import com.testhub.common.PageResult;
import com.testhub.common.R;
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

/** 接口定义接口。 */
@Tag(name = "09. 接口定义")
@RestController
@RequestMapping("/api/apis")
@RequiredArgsConstructor
public class ApiController {

    private final ApiInfoService apiInfoService;

    @Operation(summary = "分页查询接口")
    @GetMapping
    public R<PageResult<ApiInfo>> page(ApiQuery query) {
        return R.ok(apiInfoService.page(query));
    }

    @Operation(summary = "查询全部接口（下拉框）")
    @GetMapping("/all")
    public R<List<ApiInfo>> all(@RequestParam(required = false) Long projectId) {
        return R.ok(apiInfoService.listAll(projectId));
    }

    @Operation(summary = "接口详情")
    @GetMapping("/{id}")
    public R<ApiInfo> detail(@PathVariable Long id) {
        return R.ok(apiInfoService.get(id));
    }

    @Operation(summary = "新增接口")
    @PostMapping
    public R<ApiInfo> create(@Valid @RequestBody ApiInfo api) {
        return R.ok(apiInfoService.create(api));
    }

    @Operation(summary = "修改接口")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ApiInfo api) {
        apiInfoService.update(id, api);
        return R.ok();
    }

    @Operation(summary = "删除接口")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        apiInfoService.delete(id);
        return R.ok();
    }
}
