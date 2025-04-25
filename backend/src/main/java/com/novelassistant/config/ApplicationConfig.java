package com.novelassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 应用程序配置类
 */
@Configuration
public class ApplicationConfig {

    /**
     * 创建线程池ExecutorService
     * 用于异步处理小说相关任务
     */
    @Bean
    public ExecutorService executorService() {
        // 创建一个固定大小的线程池，用于异步处理任务
        return Executors.newFixedThreadPool(5);
    }
} 