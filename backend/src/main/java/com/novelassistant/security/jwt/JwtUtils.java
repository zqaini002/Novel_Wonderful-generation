package com.novelassistant.security.jwt;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import com.novelassistant.security.services.UserDetailsImpl;
import com.novelassistant.util.LogUtil;

import jakarta.annotation.PostConstruct;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT令牌
 */
@Component
public class JwtUtils {
    private static final Logger logger = LogUtil.getLogger(JwtUtils.class);

    @Value("${novelassistant.app.jwtExpirationMs}")
    private int jwtExpirationMs;
    
    // 使用静态密钥，确保应用程序重启后令牌仍然有效
    private static Key jwtKey;
    
    @PostConstruct
    public void init() {
        // 初始化时生成安全的HS512密钥
        jwtKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("dmlrMFCymPj1xtpOsVXuGo4LPSRTzWZmT0SgH3Fy8yD1Aa8d4gJkE2QbUcXn7I06"));
        logger.info("JWT密钥已初始化，算法: HS512，密钥长度: 512位");
    }

    /**
     * 根据认证信息生成JWT令牌
     * @param authentication 认证信息
     * @return JWT令牌字符串
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        logger.debug("生成JWT令牌 - 用户: {}, 颁发时间: {}, 过期时间: {}", 
                userPrincipal.getUsername(), now, expiryDate);

        String token = Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtKey)
                .compact();
        
        // 打印令牌长度而不是实际内容，避免安全风险
        logger.debug("JWT令牌生成成功，令牌长度: {}", token.length());
        return token;
    }

    /**
     * 从JWT令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUserNameFromJwtToken(String token) {
        try {
            String username = Jwts.parser()
                    .setSigningKey(jwtKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            
            logger.debug("从JWT令牌解析用户名: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("解析JWT令牌用户名失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 验证JWT令牌是否有效
     * @param authToken JWT令牌
     * @return 是否有效
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(authToken);
            
            logger.debug("JWT令牌验证成功");
            return true;
        } catch (SignatureException e) {
            logger.error("无效的JWT签名: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("无效的JWT令牌: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT声明字符串为空: {}", e.getMessage());
        }

        return false;
    }
} 