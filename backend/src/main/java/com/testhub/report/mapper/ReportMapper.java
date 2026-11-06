package com.testhub.report.mapper;

import com.testhub.report.dto.MemberWorkload;
import com.testhub.report.dto.ModuleCoverage;
import com.testhub.report.dto.NameValue;
import com.testhub.report.dto.TrendItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/** 报表统计查询（SQL 见 resources/mapper/ReportMapper.xml）。 */
@Mapper
public interface ReportMapper {

    List<TrendItem> selectExecutionTrend(@Param("projectId") Long projectId,
                                         @Param("startTime") LocalDateTime startTime);

    List<NameValue> selectCasePriority(@Param("projectId") Long projectId);

    List<NameValue> selectCaseType(@Param("projectId") Long projectId);

    List<NameValue> selectDefectSeverity(@Param("projectId") Long projectId);

    List<NameValue> selectDefectStatus(@Param("projectId") Long projectId);

    List<ModuleCoverage> selectModuleCoverage(@Param("projectId") Long projectId);

    List<MemberWorkload> selectWorkload(@Param("projectId") Long projectId);
}
