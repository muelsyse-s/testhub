package com.testhub.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证与鉴权集成测试。
 *
 * <p>验证三条核心安全规则：正确账号可登录并拿到 token；错误密码被拒绝；
 * 未携带 token 访问受保护接口返回 401。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("正确账号密码登录成功并返回 token")
    void loginSuccess() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", "wtb", "password", "123456"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("错误密码登录失败，业务码非 0")
    void loginFailWithWrongPassword() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", "wtb", "password", "wrong"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("未携带 token 访问受保护接口返回 401")
    void accessWithoutTokenShouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/test-points"))
                .andExpect(status().isUnauthorized());
    }
}
