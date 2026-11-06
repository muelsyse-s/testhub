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
import com.testhub.testplan.dto.PlanCaseVO;
import com.testhub.testplan.dto.PlanProgress;
import com.testhub.testplan.entity.PlanCase;
import com.testhub.testplan.entity.TestExecution;
import com.testhub.testplan.entity.TestPlan;
import com.testhub.testplan.mapper.PlanCaseMapper;
import com.testhub.testplan.mapper.TestExecutionMapper;
import com.testhub.testplan.mapper.TestPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 测试计划与计划用例编排。 */
@Service
@RequiredArgsConstructor
public class TestPlanService {

    private final TestPlanMapper testPlanMapper;
    private final PlanCaseMapper planCaseMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestExecutionMapper executionMapper;

    public PageResult<TestPlan> page(PageQuery query, Long projectId, String status) {
        Page<TestPlan> page = PageUtils.of(query);
        LambdaQueryWrapper<TestPlan> wrapper = new LambdaQueryWrapper<TestPlan>()
                .eq(projectId != null, TestPlan::getProjectId, projectId)
                .eq(StringUtils.hasText(status), TestPlan::getStatus, status)
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(TestPlan::getName, query.getKeyword())
                        .or().like(TestPlan::getDescription, query.getKeyword()))
                .orderByDesc(TestPlan::getId);
        return PageResult.of(testPlanMapper.selectPage(page, wrapper));
    }

    public TestPlan get(Long id) {
        TestPlan plan = testPlanMapper.selectById(id);
        if (plan == null) {
            throw new BizException(404, "测试计划不存在");
        }
        return plan;
    }

    public TestPlan create(TestPlan plan) {
        plan.setOwnerId(CurrentUser.id());
        plan.setOwnerName(CurrentUser.name());
        if (!StringUtils.hasText(plan.getStatus())) {
            plan.setStatus("PLANNING");
        }
        testPlanMapper.insert(plan);
        return plan;
    }

    public void update(Long id, TestPlan plan) {
        get(id);
        plan.setId(id);
        testPlanMapper.updateById(plan);
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        testPlanMapper.deleteById(id);
        planCaseMapper.delete(new LambdaQueryWrapper<PlanCase>().eq(PlanCase::getPlanId, id));
    }

    public List<PlanCaseVO> listPlanCases(Long planId) {
        get(planId);
        return testPlanMapper.selectPlanCases(planId);
    }

    /** 向计划中批量加入用例，已存在的自动跳过。 */
    @Transactional
    public int addCases(Long planId, List<Long> caseIds, Long assigneeId, String assigneeName) {
        get(planId);
        if (caseIds == null || caseIds.isEmpty()) {
            throw new BizException("请选择要加入的用例");
        }
        List<PlanCase> existing = planCaseMapper.selectList(new LambdaQueryWrapper<PlanCase>()
                .eq(PlanCase::getPlanId, planId));
        List<Long> existsIds = existing.stream().map(PlanCase::getCaseId).toList();
        int added = 0;
        int sort = existing.size();
        for (Long caseId : caseIds) {
            if (existsIds.contains(caseId)) {
                continue;
            }
            TestCase testCase = testCaseMapper.selectById(caseId);
            if (testCase == null) {
                continue;
            }
            PlanCase planCase = new PlanCase();
            planCase.setPlanId(planId);
            planCase.setCaseId(caseId);
            planCase.setAssigneeId(assigneeId);
            planCase.setAssigneeName(assigneeName);
            planCase.setSortNo(sort++);
            planCaseMapper.insert(planCase);
            added++;
        }
        return added;
    }

    public void removeCase(Long planId, Long caseId) {
        planCaseMapper.delete(new LambdaQueryWrapper<PlanCase>()
                .eq(PlanCase::getPlanId, planId)
                .eq(PlanCase::getCaseId, caseId));
    }

    /** 计划执行进度。 */
    public PlanProgress progress(Long planId) {
        List<PlanCase> planCases = planCaseMapper.selectList(new LambdaQueryWrapper<PlanCase>()
                .eq(PlanCase::getPlanId, planId));
        List<TestExecution> executions = executionMapper.selectList(new LambdaQueryWrapper<TestExecution>()
                .eq(TestExecution::getPlanId, planId)
                .orderByAsc(TestExecution::getId));

        PlanProgress progress = new PlanProgress();
        progress.setTotal(planCases.size());
        // 同一用例取最近一次执行结果参与统计
        java.util.Map<Long, String> latest = new java.util.HashMap<>();
        executions.forEach(e -> latest.put(e.getCaseId(), e.getResult()));
        progress.setExecuted(latest.size());
        for (String result : latest.values()) {
            switch (result) {
                case "PASS" -> progress.setPass(progress.getPass() + 1);
                case "FAIL" -> progress.setFail(progress.getFail() + 1);
                case "BLOCK" -> progress.setBlock(progress.getBlock() + 1);
                case "SKIP" -> progress.setSkip(progress.getSkip() + 1);
                default -> {
                }
            }
        }
        progress.setNotRun(progress.getTotal() - progress.getExecuted());
        return progress;
    }
}
