package com.xuegongbu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置
 * 访问地址：http://localhost:8080/doc.html
 * 
 * Sa-Token认证使用说明：
 * 1. 先调用 /front/login 接口登录获取 Token
 * 2. 点击右上角 "Authorize" 按钮
 * 3. 在弹出的对话框中输入 Token（不需要加 "Bearer " 前缀）
 * 4. 点击 "Authorize" 确认
 * 5. 之后所有需要认证的接口都会自动带上 Token
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 基本信息和认证
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("学工部课程考勤系统 API")
                        .version("1.0.0")
                        .description("学工部课程考勤系统接口文档，提供教师管理、课程管理、考勤管理等功能\n\n" +
                                "**Sa-Token认证使用说明：**\n" +
                                "1. 先调用 认证 接口登录获取 Token\n" +
                                "2. 点击右上角 **Authorize** 🔓 按钮\n" +
                                "3. 在弹出的对话框中输入 Token（需要加 \"Bearer \" 前缀）\n" +
                                "4. 点击 **Authorize** 确认\n" +
                                "5. 之后所有需要认证的接口都会自动带上 Authorization Header\n\n" +
                                "**提示：** 登录成功后，所有接口的 🔒 图标表示需要认证")
                        .contact(new Contact()
                                .name("学工部课程考勤系统")
                                .email("support@example.com")
                                .url("https://github.com/takamiyananaka/class_report_system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入Token（不需要加 'Bearer ' 前缀）\n\n" +
                                                "获取方式：调用登录接口后复制返回的 token 字段")));
        // 注意：不再在这里添加全局 SecurityItem，改用 GlobalOpenApiCustomizer 精确控制
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
     * 教师模块
     */
    @Bean
    public GroupedOpenApi teacherApi() {
        return GroupedOpenApi.builder()
                .group("01-教师模块")
                .pathsToMatch("/teacher/**")
                .build();
    }


    /**
     * 课表模块
     */
    @Bean
    public GroupedOpenApi courseScheduleApi() {
        return GroupedOpenApi.builder()
                .group("02-课表模块")
                .pathsToMatch("/courseSchedule/**")
                .build();
    }

    /**
     * 学院模块
     */
    @Bean
    public GroupedOpenApi collegeApi() {
        return GroupedOpenApi.builder()
                .group("03-学院模块")
                .pathsToMatch("/college/**")
                .build();
    }

    /**
     * 认证模块
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("04-认证模块")
                .pathsToMatch("/auth/**")
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

    /**
     * 预警模块
     */

    @Bean
    public GroupedOpenApi warningApi() {
        return GroupedOpenApi.builder()
                .group("06-预警模块")
                .pathsToMatch("/alert/**")
                .build();
    }
    /**
     * 全局接口认证配置
     * 自动为所有接口添加 Authorization 认证要求，但排除登录接口
     */
    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            // 设置全局安全要求
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    // 排除不需要认证的接口
                    boolean isPublicEndpoint = path.equals("/front/login") 
                        || path.equals("/auth/login")
                        || path.equals("/auth/logout")
                        || path.equals("/auth/forgot-password/send-code")
                        || path.equals("/auth/forgot-password/reset")
                        || path.equals("/courseSchedule/downloadTemplate")
                        || path.startsWith("/doc.html")
                        || path.startsWith("/v3/api-docs")
                        || path.startsWith("/swagger-ui");
                    
                    if (!isPublicEndpoint) {
                        // 为所有操作添加安全要求
                        pathItem.readOperations().forEach(operation -> {
                            // 确保每个操作都有安全要求
                            if (operation.getSecurity() == null || operation.getSecurity().isEmpty()) {
                                operation.addSecurityItem(
                                    new SecurityRequirement().addList("Authorization")
                                );
                            }
                        });
                    }
                });
            }
        };
    }
}