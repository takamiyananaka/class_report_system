package com.xuegongbu.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.xuegongbu.mapper")
public class MyBatisConfig {
}
