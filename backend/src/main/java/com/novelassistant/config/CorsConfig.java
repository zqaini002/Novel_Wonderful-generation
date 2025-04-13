package com.novelassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域请求配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的域，这里设置为前端开发服务器的地址
        config.addAllowedOrigin("http://localhost:8081");
        
        // 允许的请求方法
        config.addAllowedMethod("*");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 是否允许携带cookie
        config.setAllowCredentials(true);
        
        // 暴露的响应头
        config.addExposedHeader("Content-Disposition");
        
        // 对所有请求路径应用这些配置
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 