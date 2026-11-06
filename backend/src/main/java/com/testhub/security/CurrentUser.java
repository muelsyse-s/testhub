package com.testhub.security;

import com.testhub.common.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 获取当前登录用户的静态工具。 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static LoginUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        throw new BizException(401, "未登录");
    }

    public static Long id() {
        return get().getId();
    }

    public static String name() {
        LoginUser user = get();
        return user.getRealName() == null ? user.getUsername() : user.getRealName();
    }
}
