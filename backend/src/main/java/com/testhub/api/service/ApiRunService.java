package com.testhub.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.api.dto.ApiRunResult;
import com.testhub.api.entity.ApiRunLog;
import com.testhub.api.entity.ApiScript;
import com.testhub.api.mapper.ApiRunLogMapper;
import com.testhub.api.runner.ApiRunner;
import com.testhub.common.PageQuery;
import com.testhub.common.PageResult;
import com.testhub.common.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 接口脚本批量执行与日志查询。 */
@Service
@RequiredArgsConstructor
public class ApiRunService {

    private final ApiRunner apiRunner;
    private final ApiRunLogMapper runLogMapper;
    private final ApiScriptService apiScriptService;

    public ApiRunResult run(Long scriptId, Long envId) {
        return apiRunner.run(scriptId, envId);
    }

    /** 批量执行，用于接口回归。 */
    public Map<String, Object> runBatch(List<Long> scriptIds, Long envId) {
        List<ApiRunResult> results = new ArrayList<>();
        int pass = 0;
        long totalCost = 0;
        for (Long id : scriptIds) {
            ApiRunResult result = apiRunner.run(id, envId);
            results.add(result);
            if (result.isPassed()) {
                pass++;
            }
            totalCost += result.getDurationMs() == null ? 0 : result.getDurationMs();
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", results.size());
        summary.put("pass", pass);
        summary.put("fail", results.size() - pass);
        summary.put("passRate", results.isEmpty() ? 0d
                : Math.round(pass * 10000.0 / results.size()) / 100.0);
        summary.put("totalCostMs", totalCost);
        summary.put("results", results);
        return summary;
    }

    public PageResult<ApiRunLog> pageLogs(PageQuery query, Long scriptId, Long projectId, String status) {
        Page<ApiRunLog> page = PageUtils.of(query);
        LambdaQueryWrapper<ApiRunLog> wrapper = new LambdaQueryWrapper<ApiRunLog>()
                .eq(scriptId != null, ApiRunLog::getScriptId, scriptId)
                .eq(projectId != null, ApiRunLog::getProjectId, projectId)
                .eq(status != null && !status.isBlank(), ApiRunLog::getStatus, status)
                .orderByDesc(ApiRunLog::getId);
        return PageResult.of(runLogMapper.selectPage(page, wrapper));
    }

    public List<ApiScript> listRunnable(Long projectId) {
        return apiScriptService.listByProject(projectId);
    }

    public Map<String, String> globalVariables() {
        return apiRunner.getGlobalVariables();
    }

    public void clearGlobalVariables() {
        apiRunner.clearGlobalVariables();
    }

    /** 今日脚本执行概览。 */
    public Map<String, Object> todaySummary(Long projectId) {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        List<ApiRunLog> logs = runLogMapper.selectList(new LambdaQueryWrapper<ApiRunLog>()
                .eq(projectId != null, ApiRunLog::getProjectId, projectId)
                .ge(ApiRunLog::getExecutedAt, start));
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", logs.size());
        summary.put("pass", logs.stream().filter(l -> "PASS".equals(l.getStatus())).count());
        summary.put("fail", logs.stream().filter(l -> !"PASS".equals(l.getStatus())).count());
        return summary;
    }
}
