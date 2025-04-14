package com.novelassistant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS配置类 (备份)
 * 已被WebConfig替代，保留此文件仅作参考
 */
@Configuration
public class CorsConfigurationBak implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8081") // 前端端口
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
} 