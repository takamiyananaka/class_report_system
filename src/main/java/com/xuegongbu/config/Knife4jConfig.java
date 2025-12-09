package com.xuegongbu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io. swagger.v3.oas. models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info. License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置
 * 访问地址：http://localhost:8080/api/doc.html
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        . title("学工部课程考勤系统 API")
                        .version("1.0.0")
                        .description("学工部课程考勤系统接口文档，提供教师管理、课程管理、考勤管理等功能")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("support@example.com")
                                .url("https://github.com/takamiyananaka/class_report_system"))
                        .license(new License()
                                .name("MIT License")
                                . url("https://opensource.org/licenses/MIT")));
    }

    /**
     * 全部接口分组
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00-全部接口")
                .pathsToMatch("/**")
                .build();
    }

    /**
     * 认证模块
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-认证模块")
                .pathsToMatch("/auth/**", "/login/**")
                .build();
    }

    /**
     * 教师模块
     */
    @Bean
    public GroupedOpenApi teacherApi() {
        return GroupedOpenApi.builder()
                .group("02-教师模块")
                .pathsToMatch("/teacher/**")
                .build();
    }

    /**
     * 课程模块
     */
    @Bean
    public GroupedOpenApi courseApi() {
        return GroupedOpenApi.builder()
                .group("03-课程模块")
                .pathsToMatch("/course/**")
                .build();
    }

    /**
     * 班级模块
     */
    @Bean
    public GroupedOpenApi classApi() {
        return GroupedOpenApi.builder()
                .group("04-班级模块")
                .pathsToMatch("/class/**")
                .build();
    }

    /**
     * 考勤模块
     */
    @Bean
    public GroupedOpenApi attendanceApi() {
        return GroupedOpenApi.builder()
                .group("05-考勤模块")
                .pathsToMatch("/attendance/**")
                .build();
    }
}