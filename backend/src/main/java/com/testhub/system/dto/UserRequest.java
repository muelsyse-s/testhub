package com.testhub.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/** 用户新增/修改请求。 */
@Data
public class UserRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名须为 3-20 位字母、数字或下划线")
    private String username;

    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String email;
    private String phone;
    private Integer status = 1;
    private List<String> roles;
}
