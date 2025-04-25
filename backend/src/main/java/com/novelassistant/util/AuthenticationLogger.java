package com.novelassistant.util;

import org.slf4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 认证日志工具类
 * 用于详细记录认证过程中的信息，便于调试
 */
@Component
public class AuthenticationLogger {
    
    private static final Logger logger = LogUtil.getLogger(AuthenticationLogger.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 记录密码验证的详细日志
     * @param username 用户名
     * @param rawPassword 原始密码
     * @param encodedPassword 编码后的密码
     */
    public void logPasswordVerification(String username, String rawPassword, String encodedPassword) {
        logger.debug("正在验证用户 {} 的密码...", username);
        
        // 打印部分信息用于调试，避免暴露完整密码
        logger.debug("原始密码长度: {}", rawPassword != null ? rawPassword.length() : 0);
        logger.debug("加密密码: {}", encodedPassword);
        
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        logger.debug("密码匹配结果: {}", matches);
        
        // 如果不匹配，打印额外调试信息
        if (!matches) {
            logger.debug("密码不匹配，更多信息:");
            logger.debug("用户名: {}", username);
            logger.debug("原始密码前2个字符: {}", rawPassword != null && rawPassword.length() > 2 ? 
                    rawPassword.substring(0, 2) + "***" : "无效密码");
            logger.debug("密码编码算法: {}", encodedPassword != null && encodedPassword.startsWith("$2a$") ? 
                    "BCrypt" : "未知");
        }
    }
    
    /**
     * 生成新的加密密码并打印日志
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encodeAndLogPassword(String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        logger.debug("密码加密 - 原始密码长度: {}, 加密结果: {}", 
                rawPassword != null ? rawPassword.length() : 0, encoded);
        return encoded;
    }
    
    /**
     * 打印认证尝试信息
     * @param username 用户名
     * @param result 认证结果
     * @param details 额外详情
     */
    public void logAuthenticationAttempt(String username, boolean result, String details) {
        if (result) {
            logger.info("用户 {} 认证成功", username);
        } else {
            logger.warn("用户 {} 认证失败: {}", username, details);
        }
    }
} 