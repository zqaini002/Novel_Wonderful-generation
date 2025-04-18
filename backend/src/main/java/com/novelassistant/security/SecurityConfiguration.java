package com.novelassistant.security;

import com.novelassistant.config.CustomAuthenticationProvider;
import com.novelassistant.security.jwt.AuthEntryPointJwt;
import com.novelassistant.security.jwt.AuthTokenFilter;
import com.novelassistant.security.services.UserDetailsServiceImpl;
import com.novelassistant.util.LogUtil;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    
    private static final Logger logger = LogUtil.getLogger(SecurityConfiguration.class);

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    
    // 使用@Lazy注解避免循环依赖
    @Autowired
    @Lazy
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }
    
    /**
     * 密码编码器，定义为Bean以便全局使用
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("创建BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 这个Bean已被CustomAuthenticationProvider替代
     * 保留作为备用
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        logger.debug("创建标准DaoAuthenticationProvider");

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        logger.debug("创建AuthenticationManager");
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.debug("配置安全过滤链...");
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/novels/demo/**").permitAll() // 演示数据无需认证
                .requestMatchers("/novels/*/status").permitAll() // 小说处理状态查询无需认证
                // 对小说上传接口进行特殊处理，需要用户认证
                .requestMatchers("/novels/upload").authenticated()
                .requestMatchers("/novels/**").permitAll() // 其他小说相关接口可以公开访问
                .requestMatchers("/admin/logs/**").hasRole("ADMIN") // 系统日志专门的权限控制
                .requestMatchers("/admin/**").hasRole("ADMIN") // 管理员API权限控制
                .requestMatchers("/error").permitAll() // 允许错误页面访问
                .anyRequest().authenticated()
            );

        // 使用自定义认证提供者
        logger.debug("配置认证提供者: {}", customAuthenticationProvider.getClass().getSimpleName());
        http.authenticationProvider(customAuthenticationProvider);

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        logger.info("安全配置完成");
        
        return http.build();
    }

    // CORS配置源，注意：此配置与WebConfig中的CORS配置功能重复
    // WebConfig配置会在Web层生效，这个配置在Security层生效
    // 为保证一致性，这两个配置应当保持一致或者考虑移除其中一个
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        logger.debug("CORS配置完成");
        return source;
    }
}