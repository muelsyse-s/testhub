package com.testhub.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.BizException;
import com.testhub.common.PageResult;
import com.testhub.common.PageUtils;
import com.testhub.system.dto.UserQuery;
import com.testhub.system.dto.UserRequest;
import com.testhub.system.entity.SysRole;
import com.testhub.system.entity.SysUser;
import com.testhub.system.entity.SysUserRole;
import com.testhub.system.mapper.SysRoleMapper;
import com.testhub.system.mapper.SysUserMapper;
import com.testhub.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** 用户与角色管理。 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<SysUser> page(UserQuery query) {
        Page<SysUser> page = PageUtils.of(query);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(SysUser::getUsername, query.getKeyword())
                        .or().like(SysUser::getRealName, query.getKeyword())
                        .or().like(SysUser::getEmail, query.getKeyword()))
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByAsc(SysUser::getId);
        PageResult<SysUser> result = PageResult.of(userMapper.selectPage(page, wrapper));
        result.getRecords().forEach(this::fillRoles);
        return result;
    }

    public List<SysRole> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }

    public List<String> listRoleCodes(Long userId) {
        List<SysUserRole> relations = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).filter(Objects::nonNull).toList();
        Map<Long, String> roleMap = roleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getCode));
        return relations.stream().map(r -> roleMap.get(r.getRoleId())).filter(Objects::nonNull).toList();
    }

    @Transactional
    public SysUser create(UserRequest request) {
        long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (exists > 0) {
            throw new BizException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        String rawPassword = StringUtils.hasText(request.getPassword()) ? request.getPassword() : "123456";
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        userMapper.insert(user);
        saveRoles(user.getId(), request.getRoles());
        return user;
    }

    @Transactional
    public void update(Long id, UserRequest request) {
        SysUser user = requireUser(id);
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userMapper.updateById(user);
        if (request.getRoles() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
            saveRoles(id, request.getRoles());
        }
    }

    @Transactional
    public void delete(Long id) {
        SysUser user = requireUser(id);
        if ("admin".equals(user.getUsername())) {
            throw new BizException("内置管理员账号不允许删除");
        }
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

    public void resetPassword(Long id, String password) {
        SysUser user = requireUser(id);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }

    public SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private void fillRoles(SysUser user) {
        user.setRoles(listRoleCodes(user.getId()));
    }

    private void saveRoles(Long userId, List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            roleCodes = List.of("TESTER");
        }
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getCode, roleCodes));
        for (SysRole role : roles) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(role.getId());
            userRoleMapper.insert(relation);
        }
    }
}
