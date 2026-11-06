package com.testhub.testpoint.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.BizException;
import com.testhub.common.PageResult;
import com.testhub.common.PageUtils;
import com.testhub.security.CurrentUser;
import com.testhub.testcase.entity.TestCase;
import com.testhub.testcase.mapper.TestCaseMapper;
import com.testhub.testpoint.dto.TestPointQuery;
import com.testhub.testpoint.entity.TestPoint;
import com.testhub.testpoint.mapper.TestPointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 测试点管理。 */
@Service
@RequiredArgsConstructor
public class TestPointService {

    private final TestPointMapper testPointMapper;
    private final TestCaseMapper testCaseMapper;

    public PageResult<TestPoint> page(TestPointQuery query) {
        Page<TestPoint> page = PageUtils.of(query);
        LambdaQueryWrapper<TestPoint> wrapper = new LambdaQueryWrapper<TestPoint>()
                .eq(query.getProjectId() != null, TestPoint::getProjectId, query.getProjectId())
                .eq(StringUtils.hasText(query.getModule()), TestPoint::getModule, query.getModule())
                .eq(StringUtils.hasText(query.getPriority()), TestPoint::getPriority, query.getPriority())
                .eq(StringUtils.hasText(query.getStatus()), TestPoint::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getRiskLevel()), TestPoint::getRiskLevel, query.getRiskLevel())
                .eq(StringUtils.hasText(query.getSourceType()), TestPoint::getSourceType, query.getSourceType())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(TestPoint::getTitle, query.getKeyword())
                        .or().like(TestPoint::getPointNo, query.getKeyword())
                        .or().like(TestPoint::getDescription, query.getKeyword()))
                .orderByDesc(TestPoint::getId);
        PageResult<TestPoint> result = PageResult.of(testPointMapper.selectPage(page, wrapper));
        fillCaseCount(result.getRecords());
        return result;
    }

    public List<String> listModules(Long projectId) {
        List<TestPoint> points = testPointMapper.selectList(new LambdaQueryWrapper<TestPoint>()
                .select(TestPoint::getModule)
                .eq(projectId != null, TestPoint::getProjectId, projectId));
        return points.stream().map(TestPoint::getModule).filter(StringUtils::hasText)
                .distinct().sorted().toList();
    }

    public TestPoint get(Long id) {
        TestPoint point = testPointMapper.selectById(id);
        if (point == null) {
            throw new BizException(404, "测试点不存在");
        }
        fillCaseCount(List.of(point));
        return point;
    }

    public TestPoint create(TestPoint point) {
        if (!StringUtils.hasText(point.getPointNo())) {
            point.setPointNo(generatePointNo(point.getProjectId()));
        }
        point.setCreatorId(CurrentUser.id());
        point.setCreatorName(CurrentUser.name());
        testPointMapper.insert(point);
        return point;
    }

    public void update(Long id, TestPoint point) {
        get(id);
        point.setId(id);
        testPointMapper.updateById(point);
    }

    public void delete(Long id) {
        TestPoint point = get(id);
        Long caseCount = testCaseMapper.selectCount(new LambdaQueryWrapper<TestCase>()
                .eq(TestCase::getTestPointId, id));
        if (caseCount != null && caseCount > 0) {
            throw new BizException("该测试点下存在 " + caseCount + " 条用例，请先处理用例后再删除");
        }
        testPointMapper.deleteById(id);
    }

    /** 按项目内序号自动生成测试点编号。 */
    public String generatePointNo(Long projectId) {
        Long count = testPointMapper.selectCount(new LambdaQueryWrapper<TestPoint>()
                .eq(projectId != null, TestPoint::getProjectId, projectId));
        return String.format("TP-%d-%03d", projectId == null ? 0 : projectId, (count == null ? 0 : count) + 1);
    }

    private void fillCaseCount(List<TestPoint> points) {
        if (points.isEmpty()) {
            return;
        }
        List<Long> ids = points.stream().map(TestPoint::getId).toList();
        List<TestCase> cases = testCaseMapper.selectList(new LambdaQueryWrapper<TestCase>()
                .select(TestCase::getId, TestCase::getTestPointId)
                .in(TestCase::getTestPointId, ids));
        Map<Long, Long> counts = cases.stream()
                .filter(c -> c.getTestPointId() != null)
                .collect(Collectors.groupingBy(TestCase::getTestPointId, Collectors.counting()));
        points.forEach(p -> p.setCaseCount(counts.getOrDefault(p.getId(), 0L)));
    }
}
