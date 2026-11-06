package com.testhub.testcase.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.BizException;
import com.testhub.common.PageResult;
import com.testhub.common.PageUtils;
import com.testhub.security.CurrentUser;
import com.testhub.testcase.dto.TestCaseQuery;
import com.testhub.testcase.entity.TestCase;
import com.testhub.testcase.mapper.TestCaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/** 测试用例管理。 */
@Service
@RequiredArgsConstructor
public class TestCaseService {

    private final TestCaseMapper testCaseMapper;

    public PageResult<TestCase> page(TestCaseQuery query) {
        Page<TestCase> page = PageUtils.of(query);
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<TestCase>()
                .eq(query.getProjectId() != null, TestCase::getProjectId, query.getProjectId())
                .eq(query.getTestPointId() != null, TestCase::getTestPointId, query.getTestPointId())
                .eq(StringUtils.hasText(query.getPriority()), TestCase::getPriority, query.getPriority())
                .eq(StringUtils.hasText(query.getCaseType()), TestCase::getCaseType, query.getCaseType())
                .eq(StringUtils.hasText(query.getStatus()), TestCase::getStatus, query.getStatus())
                .eq(query.getAutoFlag() != null, TestCase::getAutoFlag, query.getAutoFlag())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(TestCase::getTitle, query.getKeyword())
                        .or().like(TestCase::getCaseNo, query.getKeyword())
                        .or().like(TestCase::getExpectedResult, query.getKeyword()))
                .orderByDesc(TestCase::getId);
        return PageResult.of(testCaseMapper.selectPage(page, wrapper));
    }

    public List<TestCase> listByPlanCandidates(Long projectId) {
        return testCaseMapper.selectList(new LambdaQueryWrapper<TestCase>()
                .eq(projectId != null, TestCase::getProjectId, projectId)
                .in(TestCase::getStatus, List.of("READY", "DRAFT"))
                .orderByDesc(TestCase::getId));
    }

    public TestCase get(Long id) {
        TestCase testCase = testCaseMapper.selectById(id);
        if (testCase == null) {
            throw new BizException(404, "用例不存在");
        }
        return testCase;
    }

    public TestCase create(TestCase testCase) {
        if (!StringUtils.hasText(testCase.getCaseNo())) {
            testCase.setCaseNo(generateCaseNo(testCase.getProjectId()));
        }
        testCase.setCreatorId(CurrentUser.id());
        testCase.setCreatorName(CurrentUser.name());
        if (!StringUtils.hasText(testCase.getStatus())) {
            testCase.setStatus("DRAFT");
        }
        testCaseMapper.insert(testCase);
        return testCase;
    }

    public void update(Long id, TestCase testCase) {
        get(id);
        testCase.setId(id);
        testCaseMapper.updateById(testCase);
    }

    public void delete(Long id) {
        get(id);
        testCaseMapper.deleteById(id);
    }

    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择要删除的用例");
        }
        testCaseMapper.deleteBatchIds(ids);
    }

    /** 复制用例，生成新编号，状态置为草稿。 */
    public TestCase copy(Long id) {
        TestCase source = get(id);
        TestCase copy = new TestCase();
        copy.setProjectId(source.getProjectId());
        copy.setTestPointId(source.getTestPointId());
        copy.setCaseNo(generateCaseNo(source.getProjectId()));
        copy.setTitle(source.getTitle() + "（副本）");
        copy.setPrecondition(source.getPrecondition());
        copy.setSteps(source.getSteps());
        copy.setExpectedResult(source.getExpectedResult());
        copy.setPriority(source.getPriority());
        copy.setCaseType(source.getCaseType());
        copy.setDesignMethod(source.getDesignMethod());
        copy.setStatus("DRAFT");
        copy.setAutoFlag(source.getAutoFlag());
        copy.setCreatorId(CurrentUser.id());
        copy.setCreatorName(CurrentUser.name());
        testCaseMapper.insert(copy);
        return copy;
    }

    public String generateCaseNo(Long projectId) {
        Long count = testCaseMapper.selectCount(new LambdaQueryWrapper<TestCase>()
                .eq(projectId != null, TestCase::getProjectId, projectId));
        return String.format("TC-%d-%03d", projectId == null ? 0 : projectId, (count == null ? 0 : count) + 1);
    }
}
