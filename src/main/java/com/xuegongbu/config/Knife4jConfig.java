package com.xuegongbu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger. v3.oas.models. info.Contact;
import io. swagger.v3.oas. models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger. v3.oas.models. security.SecurityScheme;
import org.springdoc.core. models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置
 * 访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 基本信息，并添加 JWT 认证
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("学工部课程考勤系统 API")
                        .version("1.0.0")
                        .description("学工部课程考勤系统接口文档，提供教师管理、课程管理、考勤管理等功能")
                        .contact(new Contact()
                                .name("虚动智能")
                                . email("support@example.com")
                                .url("https://github.com/takamiyananaka/class_report_system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // 添加JWT认证配置
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        . description("请输入JWT Token，格式：直接输入token值即可，无需添加'Bearer '前缀")))
                // 全局应用JWT认证
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
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
     * 前台模块
     */
    @Bean
    public GroupedOpenApi frontApi() {
        return GroupedOpenApi.builder()
                .group("01-前台模块")
                .pathsToMatch("/front/**")
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
     * 课表模块
     */
    @Bean
    public GroupedOpenApi courseScheduleApi() {
        return GroupedOpenApi.builder()
                .group("04-课表模块")
                .pathsToMatch("/courseSchedule/**")
                .build();
    }
}