package com.testhub.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.BizException;
import com.testhub.common.PageQuery;
import com.testhub.common.PageResult;
import com.testhub.common.PageUtils;
import com.testhub.project.entity.Project;
import com.testhub.project.mapper.ProjectMapper;
import com.testhub.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/** 项目管理。 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;

    public PageResult<Project> page(PageQuery query) {
        Page<Project> page = PageUtils.of(query);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Project::getName, query.getKeyword())
                        .or().like(Project::getCode, query.getKeyword()))
                .orderByDesc(Project::getId);
        return PageResult.of(projectMapper.selectPage(page, wrapper));
    }

    public List<Project> listAll() {
        return projectMapper.selectList(new LambdaQueryWrapper<Project>()
                .eq(Project::getStatus, "ACTIVE")
                .orderByDesc(Project::getId));
    }

    public Project get(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(404, "项目不存在");
        }
        return project;
    }

    public Project create(Project project) {
        Long exists = projectMapper.selectCount(new LambdaQueryWrapper<Project>()
                .eq(Project::getCode, project.getCode()));
        if (exists != null && exists > 0) {
            throw new BizException("项目编码已存在");
        }
        project.setOwnerId(CurrentUser.id());
        project.setOwnerName(CurrentUser.name());
        if (!StringUtils.hasText(project.getStatus())) {
            project.setStatus("ACTIVE");
        }
        projectMapper.insert(project);
        return project;
    }

    public void update(Long id, Project project) {
        get(id);
        project.setId(id);
        projectMapper.updateById(project);
    }

    public void delete(Long id) {
        get(id);
        projectMapper.deleteById(id);
    }
}
