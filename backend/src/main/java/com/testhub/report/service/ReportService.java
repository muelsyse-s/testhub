package com.testhub.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testhub.api.entity.ApiInfo;
import com.testhub.api.entity.ApiScript;
import com.testhub.api.mapper.ApiInfoMapper;
import com.testhub.api.mapper.ApiScriptMapper;
import com.testhub.defect.entity.Defect;
import com.testhub.defect.mapper.DefectMapper;
import com.testhub.project.entity.Project;
import com.testhub.project.mapper.ProjectMapper;
import com.testhub.report.dto.MemberWorkload;
import com.testhub.report.dto.ModuleCoverage;
import com.testhub.report.dto.NameValue;
import com.testhub.report.dto.TrendItem;
import com.testhub.report.mapper.ReportMapper;
import com.testhub.testcase.entity.TestCase;
import com.testhub.testcase.mapper.TestCaseMapper;
import com.testhub.testplan.entity.TestExecution;
import com.testhub.testplan.entity.TestPlan;
import com.testhub.testplan.mapper.TestExecutionMapper;
import com.testhub.testplan.mapper.TestPlanMapper;
import com.testhub.testpoint.entity.TestPoint;
import com.testhub.testpoint.mapper.TestPointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 质量报表统计。 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProjectMapper projectMapper;
    private final TestPointMapper testPointMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestPlanMapper testPlanMapper;
    private final TestExecutionMapper executionMapper;
    private final DefectMapper defectMapper;
    private final ApiInfoMapper apiInfoMapper;
    private final ApiScriptMapper apiScriptMapper;
    private final ReportMapper reportMapper;

    /** 首页总览指标。 */
    public Map<String, Object> overview(Long projectId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("projectCount", projectMapper.selectCount(new LambdaQueryWrapper<Project>()));
        long pointCount = testPointMapper.selectCount(projectWrapper(projectId));
        long caseCount = testCaseMapper.selectCount(caseWrapper(projectId));
        data.put("pointCount", pointCount);
        data.put("caseCount", caseCount);
        data.put("planCount", testPlanMapper.selectCount(planWrapper(projectId)));
        data.put("executionCount", executionMapper.selectCount(executionWrapper(projectId)));

        long pass = executionMapper.selectCount(executionWrapper(projectId).eq(TestExecution::getResult, "PASS"));
        long fail = executionMapper.selectCount(executionWrapper(projectId).eq(TestExecution::getResult, "FAIL"));
        long effective = pass + fail;
        data.put("passCount", pass);
        data.put("failCount", fail);
        data.put("passRate", effective == 0 ? 0d : Math.round(pass * 10000.0 / effective) / 100.0);

        long openDefects = defectMapper.selectCount(defectWrapper(projectId)
                .in(Defect::getStatus, List.of("OPEN", "FIXING")));
        data.put("defectOpenCount", openDefects);
        data.put("defectTotal", defectMapper.selectCount(defectWrapper(projectId)));

        long apiCount = apiInfoMapper.selectCount(new LambdaQueryWrapper<ApiInfo>()
                .eq(projectId != null, ApiInfo::getProjectId, projectId));
        long scriptCount = apiScriptMapper.selectCount(new LambdaQueryWrapper<ApiScript>()
                .eq(projectId != null, ApiScript::getProjectId, projectId));
        data.put("apiCount", apiCount);
        data.put("scriptCount", scriptCount);

        long autoCase = testCaseMapper.selectCount(caseWrapper(projectId).eq(TestCase::getAutoFlag, 1));
        data.put("automationRate", caseCount == 0 ? 0d
                : Math.round(autoCase * 10000.0 / caseCount) / 100.0);

        // 测试点覆盖率：至少有一条用例的测试点占比
        List<TestCase> cases = testCaseMapper.selectList(caseWrapper(projectId).select(TestCase::getTestPointId));
        long covered = cases.stream().map(TestCase::getTestPointId).filter(Objects::nonNull).distinct().count();
        data.put("coverageRate", pointCount == 0 ? 0d
                : Math.round(covered * 10000.0 / pointCount) / 100.0);
        data.put("coveredPointCount", covered);
        return data;
    }

    public List<TrendItem> executionTrend(Long projectId, int days) {
        int span = days <= 0 ? 14 : Math.min(days, 90);
        LocalDateTime start = LocalDate.now().minusDays(span - 1L).atStartOfDay();
        List<TrendItem> items = reportMapper.selectExecutionTrend(projectId, start);
        if (items.isEmpty()) {
            // 演示数据不在最近窗口内时回退为全量趋势，避免图表空白
            items = reportMapper.selectExecutionTrend(projectId, null);
        }
        return items;
    }

    public List<NameValue> casePriority(Long projectId) {
        return reportMapper.selectCasePriority(projectId);
    }

    public List<NameValue> caseType(Long projectId) {
        return reportMapper.selectCaseType(projectId);
    }

    public List<NameValue> defectSeverity(Long projectId) {
        return reportMapper.selectDefectSeverity(projectId);
    }

    public List<NameValue> defectStatus(Long projectId) {
        return reportMapper.selectDefectStatus(projectId);
    }

    public List<ModuleCoverage> moduleCoverage(Long projectId) {
        return reportMapper.selectModuleCoverage(projectId);
    }

    public List<MemberWorkload> workload(Long projectId) {
        return reportMapper.selectWorkload(projectId);
    }

    private LambdaQueryWrapper<TestPoint> projectWrapper(Long projectId) {
        return new LambdaQueryWrapper<TestPoint>().eq(projectId != null, TestPoint::getProjectId, projectId);
    }

    private LambdaQueryWrapper<TestCase> caseWrapper(Long projectId) {
        return new LambdaQueryWrapper<TestCase>().eq(projectId != null, TestCase::getProjectId, projectId);
    }

    private LambdaQueryWrapper<TestPlan> planWrapper(Long projectId) {
        return new LambdaQueryWrapper<TestPlan>().eq(projectId != null, TestPlan::getProjectId, projectId);
    }

    private LambdaQueryWrapper<TestExecution> executionWrapper(Long projectId) {
        return new LambdaQueryWrapper<TestExecution>().eq(projectId != null, TestExecution::getProjectId, projectId);
    }

    private LambdaQueryWrapper<Defect> defectWrapper(Long projectId) {
        return new LambdaQueryWrapper<Defect>().eq(projectId != null, Defect::getProjectId, projectId);
    }
}
