package com.testhub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger/OpenAPI 文档配置。 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI testHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TestHub 测试资产管理系统 API")
                        .version("1.0.0")
                        .description("测试点、测试用例、测试计划、执行记录、缺陷、接口脚本统一管理平台的开放接口"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
