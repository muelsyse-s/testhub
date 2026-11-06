package com.testhub.api.controller;

import com.testhub.api.entity.EnvInfo;
import com.testhub.api.service.EnvService;
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

/** 环境管理接口。 */
@Tag(name = "11. 环境管理")
@RestController
@RequestMapping("/api/environments")
@RequiredArgsConstructor
public class EnvController {

    private final EnvService envService;

    @Operation(summary = "环境列表")
    @GetMapping
    public R<List<EnvInfo>> list(@RequestParam(required = false) Long projectId) {
        return R.ok(envService.list(projectId));
    }

    @Operation(summary = "新增环境")
    @PostMapping
    public R<EnvInfo> create(@Valid @RequestBody EnvInfo env) {
        return R.ok(envService.create(env));
    }

    @Operation(summary = "修改环境")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody EnvInfo env) {
        envService.update(id, env);
        return R.ok();
    }

    @Operation(summary = "删除环境")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        envService.delete(id);
        return R.ok();
    }
}
