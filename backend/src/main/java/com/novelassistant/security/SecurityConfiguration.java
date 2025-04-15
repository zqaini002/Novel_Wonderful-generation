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
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/test/public").permitAll()
                .requestMatchers("/api/novels/demo/**").permitAll() // 演示数据无需认证
                .requestMatchers("/api/novels/*/status").permitAll() // 小说处理状态查询无需认证
                // 暂时开放更多API权限，方便开发测试
                .requestMatchers("/api/novels/**").permitAll() // 开放所有小说相关接口
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