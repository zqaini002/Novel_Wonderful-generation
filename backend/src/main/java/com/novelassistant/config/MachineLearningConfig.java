package com.novelassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.novelassistant.service.MachineLearningService;
import com.novelassistant.service.impl.Dl4jMachineLearningServiceImpl;

/**
 * 机器学习服务配置类
 */
@Configuration
public class MachineLearningConfig {
    
    /**
     * 注册机器学习服务实现
     * 
     * @return Dl4J实现的机器学习服务
     */
    @Bean
    public MachineLearningService machineLearningService() {
        return new Dl4jMachineLearningServiceImpl();
    }
} 