package com.xuegongbu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 使用固定线程池执行定时任务，避免任务之间相互影响
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(5));
    }
}