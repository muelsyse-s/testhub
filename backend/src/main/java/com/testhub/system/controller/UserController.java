package com.testhub.system.controller;

import com.testhub.common.PageResult;
import com.testhub.common.R;
import com.testhub.system.dto.UserQuery;
import com.testhub.system.dto.UserRequest;
import com.testhub.system.entity.SysRole;
import com.testhub.system.entity.SysUser;
import com.testhub.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

/** 用户管理接口。 */
@Tag(name = "02. 用户与角色")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    public R<PageResult<SysUser>> page(UserQuery query) {
        return R.ok(userService.page(query));
    }

    @Operation(summary = "查询全部角色")
    @GetMapping("/roles")
    public R<List<SysRole>> roles() {
        return R.ok(userService.listRoles());
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEST_MANAGER')")
    public R<SysUser> create(@Valid @RequestBody UserRequest request) {
        return R.ok(userService.create(request));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEST_MANAGER')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        userService.update(id, request);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public R<Void> resetPassword(@PathVariable Long id, @RequestParam String password) {
        userService.resetPassword(id, password);
        return R.ok();
    }
}
