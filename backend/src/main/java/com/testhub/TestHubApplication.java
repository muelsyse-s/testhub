package com.testhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TestHub 测试资产管理系统启动类。
 *
 * <p>系统面向测试团队，统一管理测试点、测试用例、测试计划、执行记录、
 * 缺陷、接口定义与接口自动化脚本等测试资产。</p>
 */
@SpringBootApplication
@MapperScan("com.testhub.**.mapper")
public class TestHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestHubApplication.class, args);
    }
}
