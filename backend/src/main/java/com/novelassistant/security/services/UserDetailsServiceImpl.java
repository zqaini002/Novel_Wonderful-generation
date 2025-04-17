package com.novelassistant.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novelassistant.entity.User;
import com.novelassistant.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("正在查找用户: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("用户名不存在: {}", username);
                    return new UsernameNotFoundException("用户名或密码错误");
                });
                
        // 检查用户是否已禁用
        if (!user.isEnabled()) {
            logger.warn("用户 {} 已被禁用", username);
            throw new UsernameNotFoundException("账号已被禁用，请联系管理员");
        }

        return UserDetailsImpl.build(user);
    }
} 