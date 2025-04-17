package com.novelassistant.security.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.novelassistant.security.services.UserDetailsServiceImpl;
import com.novelassistant.util.LogUtil;

/**
 * JWT认证过滤器
 * 每次HTTP请求都会经过此过滤器，负责解析JWT令牌并设置认证信息
 */
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LogUtil.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 解析JWT令牌
            String jwt = parseJwt(request);
            if (jwt != null) {
                logger.debug("开始处理JWT令牌 - 请求路径: {}", request.getRequestURI());
                
                // 验证令牌
                if (jwtUtils.validateJwtToken(jwt)) {
                    // 从令牌中获取用户名
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("JWT令牌有效，用户名: {}", username);
                    
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // 检查用户是否被禁用
                    if (!userDetails.isEnabled()) {
                        logger.warn("用户 {} 已被禁用，拒绝访问 API: {}", username, request.getRequestURI());
                        // 不设置认证，这样请求会被视为未认证
                        return;
                    }
                    
                    // 创建认证令牌
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置认证信息
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 记录用户授权信息
                    LogUtil.setUserId(username);
                    logger.debug("用户 {} 认证成功，角色: {}", username, userDetails.getAuthorities());
                } else {
                    logger.warn("JWT令牌验证失败，请求路径: {}", request.getRequestURI());
                }
            } else {
                logger.debug("请求中没有JWT令牌，请求路径: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            logger.error("无法设置用户认证: {}", e.getMessage());
            logger.error("异常详情: {}", LogUtil.formatException(e));
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中解析JWT令牌
     * @param request HTTP请求
     * @return JWT令牌字符串，如果不存在则返回null
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            // 只打印令牌长度，不打印令牌内容
            logger.debug("解析到JWT令牌，长度: {}", token.length());
            return token;
        }

        return null;
    }
} 