package com.testhub.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testhub.common.BizException;
import com.testhub.security.JwtTokenProvider;
import com.testhub.security.LoginUser;
import com.testhub.system.dto.LoginRequest;
import com.testhub.system.dto.LoginResponse;
import com.testhub.system.entity.SysUser;
import com.testhub.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/** 认证服务。 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(403, "账号已被禁用，请联系管理员");
        }
        List<String> roles = userService.listRoleCodes(user.getId());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRealName(), roles);
        String token = tokenProvider.generate(loginUser);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), roles);
    }
}
