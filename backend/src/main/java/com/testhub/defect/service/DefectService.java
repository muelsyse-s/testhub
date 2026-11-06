package com.testhub.defect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.BizException;
import com.testhub.common.PageResult;
import com.testhub.common.PageUtils;
import com.testhub.defect.dto.DefectQuery;
import com.testhub.defect.entity.Defect;
import com.testhub.defect.mapper.DefectMapper;
import com.testhub.project.entity.Project;
import com.testhub.project.mapper.ProjectMapper;
import com.testhub.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/** 缺陷管理。 */
@Service
@RequiredArgsConstructor
public class DefectService {

    private final DefectMapper defectMapper;
    private final ProjectMapper projectMapper;

    public PageResult<Defect> page(DefectQuery query) {
        Page<Defect> page = PageUtils.of(query);
        LambdaQueryWrapper<Defect> wrapper = new LambdaQueryWrapper<Defect>()
                .eq(query.getProjectId() != null, Defect::getProjectId, query.getProjectId())
                .eq(StringUtils.hasText(query.getStatus()), Defect::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getSeverity()), Defect::getSeverity, query.getSeverity())
                .eq(StringUtils.hasText(query.getPriority()), Defect::getPriority, query.getPriority())
                .eq(query.getAssigneeId() != null, Defect::getAssigneeId, query.getAssigneeId())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Defect::getTitle, query.getKeyword())
                        .or().like(Defect::getDefectNo, query.getKeyword()))
                .orderByDesc(Defect::getId);
        return PageResult.of(defectMapper.selectPage(page, wrapper));
    }

    public Defect get(Long id) {
        Defect defect = defectMapper.selectById(id);
        if (defect == null) {
            throw new BizException(404, "缺陷不存在");
        }
        return defect;
    }

    public List<String> listStatuses() {
        return List.of("OPEN", "FIXING", "RESOLVED", "CLOSED", "REJECTED");
    }

    public Defect create(Defect defect) {
        if (!StringUtils.hasText(defect.getDefectNo())) {
            defect.setDefectNo(generateDefectNo(defect.getProjectId()));
        }
        defect.setReporterId(CurrentUser.id());
        defect.setReporterName(CurrentUser.name());
        if (!StringUtils.hasText(defect.getStatus())) {
            defect.setStatus("OPEN");
        }
        defectMapper.insert(defect);
        return defect;
    }

    public void update(Long id, Defect defect) {
        get(id);
        defect.setId(id);
        if ("RESOLVED".equals(defect.getStatus()) || "CLOSED".equals(defect.getStatus())) {
            defect.setResolvedAt(defect.getResolvedAt() == null ? LocalDateTime.now() : defect.getResolvedAt());
        }
        defectMapper.updateById(defect);
    }

    /** 缺陷状态流转：OPEN -> FIXING -> RESOLVED -> CLOSED，或 REJECTED 关闭。 */
    public void changeStatus(Long id, String status) {
        Defect defect = get(id);
        List<String> allowed = List.of("OPEN", "FIXING", "RESOLVED", "CLOSED", "REJECTED");
        if (!allowed.contains(status)) {
            throw new BizException("非法缺陷状态：" + status);
        }
        defect.setStatus(status);
        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
            defect.setResolvedAt(LocalDateTime.now());
        }
        if ("OPEN".equals(status)) {
            defect.setResolvedAt(null);
        }
        defectMapper.updateById(defect);
    }

    public void delete(Long id) {
        get(id);
        defectMapper.deleteById(id);
    }

    public String generateDefectNo(Long projectId) {
        Project project = projectId == null ? null : projectMapper.selectById(projectId);
        String code = project == null ? "GEN" : project.getCode();
        Long count = defectMapper.selectCount(new LambdaQueryWrapper<Defect>()
                .eq(projectId != null, Defect::getProjectId, projectId));
        return String.format("BUG-%s-%03d", code, (count == null ? 0 : count) + 1);
    }
}
