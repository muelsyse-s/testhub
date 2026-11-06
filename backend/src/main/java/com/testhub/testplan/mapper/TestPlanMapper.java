package com.testhub.testplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testhub.testplan.dto.PlanCaseVO;
import com.testhub.testplan.entity.TestPlan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPlanMapper extends BaseMapper<TestPlan> {

    /** 查询计划下的用例及其最近一次执行结果。 */
    List<PlanCaseVO> selectPlanCases(@Param("planId") Long planId);
}
