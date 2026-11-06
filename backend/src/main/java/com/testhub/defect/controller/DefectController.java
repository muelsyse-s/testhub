package com.testhub.defect.controller;

import com.testhub.common.PageResult;
import com.testhub.common.R;
import com.testhub.defect.dto.DefectQuery;
import com.testhub.defect.entity.Defect;
import com.testhub.defect.service.DefectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 缺陷接口。 */
@Tag(name = "08. 缺陷管理")
@RestController
@RequestMapping("/api/defects")
@RequiredArgsConstructor
public class DefectController {

    private final DefectService defectService;

    @Operation(summary = "分页查询缺陷")
    @GetMapping
    public R<PageResult<Defect>> page(DefectQuery query) {
        return R.ok(defectService.page(query));
    }

    @Operation(summary = "缺陷状态枚举")
    @GetMapping("/statuses")
    public R<List<String>> statuses() {
        return R.ok(defectService.listStatuses());
    }

    @Operation(summary = "缺陷详情")
    @GetMapping("/{id}")
    public R<Defect> detail(@PathVariable Long id) {
        return R.ok(defectService.get(id));
    }

    @Operation(summary = "提交缺陷")
    @PostMapping
    public R<Defect> create(@Valid @RequestBody Defect defect) {
        return R.ok(defectService.create(defect));
    }

    @Operation(summary = "修改缺陷")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody Defect defect) {
        defectService.update(id, defect);
        return R.ok();
    }

    @Operation(summary = "缺陷状态流转")
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        defectService.changeStatus(id, body.get("status"));
        return R.ok();
    }

    @Operation(summary = "删除缺陷")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        defectService.delete(id);
        return R.ok();
    }
}
