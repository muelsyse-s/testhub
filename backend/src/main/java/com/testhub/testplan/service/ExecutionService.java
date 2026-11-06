package com.testhub.testplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.BizException;
import com.testhub.common.PageQuery;
import com.testhub.common.PageResult;
import com.testhub.common.PageUtils;
import com.testhub.security.CurrentUser;
import com.testhub.testcase.entity.TestCase;
import com.testhub.testcase.mapper.TestCaseMapper;
import com.testhub.testplan.entity.TestExecution;
import com.testhub.testplan.entity.TestPlan;
import com.testhub.testplan.mapper.TestExecutionMapper;
import com.testhub.testplan.mapper.TestPlanMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/** 用例执行记录。 */
@Service
@RequiredArgsConstructor
public class ExecutionService {

    private final TestExecutionMapper executionMapper;
    private final TestPlanMapper planMapper;
    private final TestCaseMapper testCaseMapper;

    public PageResult<TestExecution> page(PageQuery query, Long planId, Long projectId, String result) {
        Page<TestExecution> page = PageUtils.of(query);
        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<TestExecution>()
                .eq(planId != null, TestExecution::getPlanId, planId)
                .eq(projectId != null, TestExecution::getProjectId, projectId)
                .eq(StringUtils.hasText(result), TestExecution::getResult, result)
                .orderByDesc(TestExecution::getId);
        return PageResult.of(executionMapper.selectPage(page, wrapper));
    }

    /** 提交一次执行结果。 */
    public TestExecution record(ExecutionRequest request) {
        if (request.getCaseId() == null || request.getPlanId() == null) {
            throw new BizException("计划与用例不能为空");
        }
        TestPlan plan = planMapper.selectById(request.getPlanId());
        if (plan == null) {
            throw new BizException(404, "测试计划不存在");
        }
        TestCase testCase = testCaseMapper.selectById(request.getCaseId());
        if (testCase == null) {
            throw new BizException(404, "用例不存在");
        }
        List<String> allowed = List.of("PASS", "FAIL", "BLOCK", "SKIP");
        if (!allowed.contains(request.getResult())) {
            throw new BizException("执行结果只能是 PASS/FAIL/BLOCK/SKIP");
        }
        TestExecution execution = new TestExecution();
        execution.setProjectId(plan.getProjectId());
        execution.setPlanId(plan.getId());
        execution.setCaseId(testCase.getId());
        execution.setExecutorId(CurrentUser.id());
        execution.setExecutorName(CurrentUser.name());
        execution.setResult(request.getResult());
        execution.setActualResult(request.getActualResult());
        execution.setDurationMs(request.getDurationMs());
        execution.setExecutedAt(LocalDateTime.now());
        execution.setRemark(request.getRemark());
        executionMapper.insert(execution);
        return execution;
    }

    public List<TestExecution> listByCase(Long caseId) {
        return executionMapper.selectList(new LambdaQueryWrapper<TestExecution>()
                .eq(TestExecution::getCaseId, caseId)
                .orderByDesc(TestExecution::getId));
    }

    @Data
    public static class ExecutionRequest {
        private Long planId;
        private Long caseId;
        private String result;
        private String actualResult;
        private Long durationMs;
        private String remark;
    }
}
