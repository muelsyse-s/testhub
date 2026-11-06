package com.testhub.api.controller;

import com.testhub.api.dto.ApiRunResult;
import com.testhub.api.dto.ScriptQuery;
import com.testhub.api.entity.ApiRunLog;
import com.testhub.api.entity.ApiScript;
import com.testhub.api.service.ApiRunService;
import com.testhub.api.service.ApiScriptService;
import com.testhub.common.PageQuery;
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
import java.util.Map;

/** 接口脚本与执行接口。 */
@Tag(name = "10. 接口脚本")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiScriptController {

    private final ApiScriptService apiScriptService;
    private final ApiRunService apiRunService;

    @Operation(summary = "分页查询接口脚本")
    @GetMapping("/api-scripts")
    public R<PageResult<ApiScript>> page(ScriptQuery query) {
        return R.ok(apiScriptService.page(query));
    }

    @Operation(summary = "脚本详情")
    @GetMapping("/api-scripts/{id}")
    public R<ApiScript> detail(@PathVariable Long id) {
        return R.ok(apiScriptService.get(id));
    }

    @Operation(summary = "新增脚本")
    @PostMapping("/api-scripts")
    public R<ApiScript> create(@Valid @RequestBody ApiScript script) {
        return R.ok(apiScriptService.create(script));
    }

    @Operation(summary = "修改脚本")
    @PutMapping("/api-scripts/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ApiScript script) {
        apiScriptService.update(id, script);
        return R.ok();
    }

    @Operation(summary = "删除脚本")
    @DeleteMapping("/api-scripts/{id}")
    public R<Void> delete(@PathVariable Long id) {
        apiScriptService.delete(id);
        return R.ok();
    }

    @Operation(summary = "执行单个脚本", description = "按环境变量发送 HTTP 请求并逐条校验断言，结果落库")
    @PostMapping("/api-scripts/{id}/run")
    public R<ApiRunResult> run(@PathVariable Long id, @RequestParam(required = false) Long envId) {
        return R.ok(apiRunService.run(id, envId));
    }

    @Operation(summary = "批量执行脚本")
    @PostMapping("/api-scripts/run-batch")
    public R<Map<String, Object>> runBatch(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("scriptIds");
        List<Long> ids = rawIds == null ? List.of() : rawIds.stream().map(Integer::longValue).toList();
        Long envId = body.get("envId") == null ? null : Long.valueOf(body.get("envId").toString());
        return R.ok(apiRunService.runBatch(ids, envId));
    }

    @Operation(summary = "脚本执行日志")
    @GetMapping("/api-run-logs")
    public R<PageResult<ApiRunLog>> logs(PageQuery query,
                                         @RequestParam(required = false) Long scriptId,
                                         @RequestParam(required = false) Long projectId,
                                         @RequestParam(required = false) String status) {
        return R.ok(apiRunService.pageLogs(query, scriptId, projectId, status));
    }

    @Operation(summary = "全局变量（跨脚本提取结果）")
    @GetMapping("/api-scripts/variables")
    public R<Map<String, String>> variables() {
        return R.ok(apiRunService.globalVariables());
    }

    @Operation(summary = "清空全局变量")
    @DeleteMapping("/api-scripts/variables")
    public R<Void> clearVariables() {
        apiRunService.clearGlobalVariables();
        return R.ok();
    }
}
