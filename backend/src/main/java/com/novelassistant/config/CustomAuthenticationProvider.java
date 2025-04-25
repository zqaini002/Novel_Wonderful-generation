package com.novelassistant.config;

import com.novelassistant.security.services.UserDetailsImpl;
import com.novelassistant.security.services.UserDetailsServiceImpl;
import com.novelassistant.util.AuthenticationLogger;
import com.novelassistant.util.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 自定义认证提供者
 * 用于对认证过程进行详细记录，便于调试
 */
@Component
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {
    
    private static final Logger logger = LogUtil.getLogger(CustomAuthenticationProvider.class);
    
    @Autowired
    private AuthenticationLogger authLogger;
    
    /**
     * 使用构造函数注入依赖，避免循环依赖
     * 添加@Lazy注解延迟加载
     */
    @Autowired
    public CustomAuthenticationProvider(@Lazy PasswordEncoder passwordEncoder, 
                                       UserDetailsServiceImpl userDetailsService) {
        super.setPasswordEncoder(passwordEncoder);
        super.setUserDetailsService(userDetailsService);
        logger.debug("CustomAuthenticationProvider已初始化");
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        
        logger.debug("开始认证过程 - 用户名: {}", username);
        logger.debug("提供的密码长度: {}", password != null ? password.length() : 0);
        
        try {
            // 加载用户详情
            UserDetails loadedUser = getUserDetailsService().loadUserByUsername(username);
            logger.debug("已加载用户详情: {}", loadedUser.getUsername());
            
            if (loadedUser instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) loadedUser;
                logger.debug("用户ID: {}, 邮箱: {}, 角色数量: {}", 
                        userDetails.getId(), 
                        userDetails.getEmail(),
                        userDetails.getAuthorities().size());
                logger.debug("存储的密码: {}", userDetails.getPassword());
                
                // 使用日志工具验证密码
                authLogger.logPasswordVerification(username, password, userDetails.getPassword());
            }
            
            // 调用父类方法进行认证
            Authentication result = super.authenticate(authentication);
            logger.info("用户 {} 认证成功", username);
            return result;
        } catch (BadCredentialsException e) {
            logger.error("用户 {} 认证失败: 密码不匹配", username);
            throw e;
        } catch (AuthenticationException e) {
            logger.error("用户 {} 认证异常: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("认证过程发生未知异常: {}", e.getMessage());
            logger.error("异常详情: {}", LogUtil.formatException(e));
            throw new BadCredentialsException("认证过程发生未知异常", e);
        }
    }
} 