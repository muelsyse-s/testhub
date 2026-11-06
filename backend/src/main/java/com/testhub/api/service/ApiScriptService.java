package com.testhub.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.api.dto.ScriptQuery;
import com.testhub.api.entity.ApiScript;
import com.testhub.api.mapper.ApiScriptMapper;
import com.testhub.common.BizException;
import com.testhub.common.PageResult;
import com.testhub.common.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/** 接口脚本管理。 */
@Service
@RequiredArgsConstructor
public class ApiScriptService {

    private final ApiScriptMapper apiScriptMapper;

    public PageResult<ApiScript> page(ScriptQuery query) {
        Page<ApiScript> page = PageUtils.of(query);
        LambdaQueryWrapper<ApiScript> wrapper = new LambdaQueryWrapper<ApiScript>()
                .eq(query.getProjectId() != null, ApiScript::getProjectId, query.getProjectId())
                .eq(query.getApiId() != null, ApiScript::getApiId, query.getApiId())
                .eq(StringUtils.hasText(query.getScriptType()), ApiScript::getScriptType, query.getScriptType())
                .eq(query.getEnabled() != null, ApiScript::getEnabled, query.getEnabled())
                .like(StringUtils.hasText(query.getKeyword()), ApiScript::getName, query.getKeyword())
                .orderByDesc(ApiScript::getId);
        return PageResult.of(apiScriptMapper.selectPage(page, wrapper));
    }

    public List<ApiScript> listByProject(Long projectId) {
        return apiScriptMapper.selectList(new LambdaQueryWrapper<ApiScript>()
                .eq(projectId != null, ApiScript::getProjectId, projectId)
                .eq(ApiScript::getEnabled, 1)
                .orderByAsc(ApiScript::getId));
    }

    public ApiScript get(Long id) {
        ApiScript script = apiScriptMapper.selectById(id);
        if (script == null) {
            throw new BizException(404, "接口脚本不存在");
        }
        return script;
    }

    public ApiScript create(ApiScript script) {
        if (script.getEnabled() == null) {
            script.setEnabled(1);
        }
        apiScriptMapper.insert(script);
        return script;
    }

    public void update(Long id, ApiScript script) {
        get(id);
        script.setId(id);
        apiScriptMapper.updateById(script);
    }

    public void delete(Long id) {
        get(id);
        apiScriptMapper.deleteById(id);
    }
}
