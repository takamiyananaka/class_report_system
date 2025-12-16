package com.xuegongbu.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com. baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou. mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.xuegongbu.mapper")
public class MyBatisConfig {

    /**
     * 配置 MyBatis-Plus 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType. MYSQL);

        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInterceptor.setMaxLimit(500L);

        // 溢出总页数后是否进行处理（默认不处理）
        paginationInterceptor.setOverflow(false);

        interceptor. addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}