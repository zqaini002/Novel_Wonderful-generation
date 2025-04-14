package com.novelassistant.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web配置类
 * 用于配置Web相关的设置，如CORS等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;

    /**
     * 配置CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8081")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加请求日志拦截器
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns("/static/**", "/error"); // 排除静态资源和错误页面
    }
} 