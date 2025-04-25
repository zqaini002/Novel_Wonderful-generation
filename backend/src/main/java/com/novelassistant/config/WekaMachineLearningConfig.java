package com.novelassistant.config;

import org.springframework.context.annotation.Configuration;

/**
 * Weka机器学习配置类
 */
@Configuration
public class WekaMachineLearningConfig {

    /**
     * 注册Weka机器学习服务实现
     * 注意: 该Bean定义已被注释掉，因为WekaMachineLearningServiceImpl类已经通过@Service注解注册了同名的Bean
     */
    /*
    @Bean
    @Primary
    public MachineLearningService wekaMachineLearningService() {
        return new WekaMachineLearningServiceImpl();
    }
    */
} 