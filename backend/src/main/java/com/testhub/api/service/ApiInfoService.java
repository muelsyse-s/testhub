package com.testhub.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.api.dto.ApiQuery;
import com.testhub.api.entity.ApiInfo;
import com.testhub.api.entity.ApiScript;
import com.testhub.api.mapper.ApiInfoMapper;
import com.testhub.api.mapper.ApiScriptMapper;
import com.testhub.common.BizException;
import com.testhub.common.PageResult;
import com.testhub.common.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/** 接口定义管理。 */
@Service
@RequiredArgsConstructor
public class ApiInfoService {

    private final ApiInfoMapper apiInfoMapper;
    private final ApiScriptMapper apiScriptMapper;

    public PageResult<ApiInfo> page(ApiQuery query) {
        Page<ApiInfo> page = PageUtils.of(query);
        LambdaQueryWrapper<ApiInfo> wrapper = new LambdaQueryWrapper<ApiInfo>()
                .eq(query.getProjectId() != null, ApiInfo::getProjectId, query.getProjectId())
                .eq(StringUtils.hasText(query.getModule()), ApiInfo::getModule, query.getModule())
                .eq(StringUtils.hasText(query.getMethod()), ApiInfo::getMethod, query.getMethod())
                .eq(StringUtils.hasText(query.getStatus()), ApiInfo::getStatus, query.getStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(ApiInfo::getName, query.getKeyword())
                        .or().like(ApiInfo::getPath, query.getKeyword()))
                .orderByDesc(ApiInfo::getId);
        return PageResult.of(apiInfoMapper.selectPage(page, wrapper));
    }

    public List<ApiInfo> listAll(Long projectId) {
        return apiInfoMapper.selectList(new LambdaQueryWrapper<ApiInfo>()
                .eq(projectId != null, ApiInfo::getProjectId, projectId)
                .orderByAsc(ApiInfo::getId));
    }

    public ApiInfo get(Long id) {
        ApiInfo api = apiInfoMapper.selectById(id);
        if (api == null) {
            throw new BizException(404, "接口不存在");
        }
        return api;
    }

    public ApiInfo create(ApiInfo api) {
        apiInfoMapper.insert(api);
        return api;
    }

    public void update(Long id, ApiInfo api) {
        get(id);
        api.setId(id);
        apiInfoMapper.updateById(api);
    }

    public void delete(Long id) {
        get(id);
        Long scripts = apiScriptMapper.selectCount(new LambdaQueryWrapper<ApiScript>()
                .eq(ApiScript::getApiId, id));
        if (scripts != null && scripts > 0) {
            throw new BizException("该接口下存在 " + scripts + " 个脚本，请先删除脚本");
        }
        apiInfoMapper.deleteById(id);
    }
}
