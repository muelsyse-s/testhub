package com.testhub.project.controller;

import com.testhub.common.PageQuery;
import com.testhub.common.PageResult;
import com.testhub.common.R;
import com.testhub.project.entity.Project;
import com.testhub.project.service.ProjectService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 项目接口。 */
@Tag(name = "03. 项目管理")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "分页查询项目")
    @GetMapping
    public R<PageResult<Project>> page(PageQuery query) {
        return R.ok(projectService.page(query));
    }

    @Operation(summary = "查询全部启用项目（下拉框）")
    @GetMapping("/options")
    public R<List<Project>> options() {
        return R.ok(projectService.listAll());
    }

    @Operation(summary = "项目详情")
    @GetMapping("/{id}")
    public R<Project> detail(@PathVariable Long id) {
        return R.ok(projectService.get(id));
    }

    @Operation(summary = "新增项目")
    @PostMapping
    public R<Project> create(@Valid @RequestBody Project project) {
        return R.ok(projectService.create(project));
    }

    @Operation(summary = "修改项目")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody Project project) {
        projectService.update(id, project);
        return R.ok();
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return R.ok();
    }
}
