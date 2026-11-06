package com.testhub.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testhub.api.entity.EnvInfo;
import com.testhub.api.mapper.EnvInfoMapper;
import com.testhub.common.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 测试环境管理。 */
@Service
@RequiredArgsConstructor
public class EnvService {

    private final EnvInfoMapper envInfoMapper;

    public List<EnvInfo> list(Long projectId) {
        return envInfoMapper.selectList(new LambdaQueryWrapper<EnvInfo>()
                .eq(projectId != null, EnvInfo::getProjectId, projectId)
                .orderByAsc(EnvInfo::getId));
    }

    public EnvInfo get(Long id) {
        EnvInfo env = envInfoMapper.selectById(id);
        if (env == null) {
            throw new BizException(404, "环境不存在");
        }
        return env;
    }

    public EnvInfo create(EnvInfo env) {
        envInfoMapper.insert(env);
        return env;
    }

    public void update(Long id, EnvInfo env) {
        get(id);
        env.setId(id);
        envInfoMapper.updateById(env);
    }

    public void delete(Long id) {
        get(id);
        envInfoMapper.deleteById(id);
    }
}
