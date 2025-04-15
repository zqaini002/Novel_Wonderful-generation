package com.novelassistant.service.impl;

import com.novelassistant.entity.Role;
import com.novelassistant.entity.User;
import com.novelassistant.exception.ResourceNotFoundException;
import com.novelassistant.payload.request.UpdateProfileRequest;
import com.novelassistant.payload.response.UserProfileResponse;
import com.novelassistant.payload.response.UserStatsResponse;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.UserRepository;
import com.novelassistant.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取用户个人资料
     *
     * @param userId 用户ID
     * @return 用户个人资料响应
     */
    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        return convertToUserProfileResponse(user);
    }

    /**
     * 更新用户个人资料
     *
     * @param profileRequest 更新请求
     * @return 更新后的用户个人资料
     */
    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(UpdateProfileRequest profileRequest) {
        User user = userRepository.findById(profileRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + profileRequest.getId()));
        
        // 更新基本信息
        user.setEmail(profileRequest.getEmail());
        user.setNickname(profileRequest.getNickname());
        user.setUpdatedAt(new Date());
        
        // 如果需要修改密码
        if (profileRequest.getNewPassword() != null && !profileRequest.getNewPassword().isEmpty()) {
            // 验证旧密码
            if (profileRequest.getOldPassword() == null || !passwordEncoder.matches(profileRequest.getOldPassword(), user.getPassword())) {
                throw new IllegalArgumentException("原密码不正确");
            }
            
            // 设置新密码
            user.setPassword(passwordEncoder.encode(profileRequest.getNewPassword()));
        }
        
        // 保存更新
        userRepository.save(user);
        
        return convertToUserProfileResponse(user);
    }

    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 密码修改是否成功
     */
    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 设置新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(new Date());
        
        // 保存更新
        userRepository.save(user);
        
        return true;
    }

    /**
     * 获取用户统计信息
     *
     * @param userId 用户ID
     * @return 用户统计信息
     */
    @Override
    public UserStatsResponse getUserStats(Long userId) {
        // 获取用户上传的小说数量
        int novelCount = 0;
        try {
            logger.debug("开始统计用户ID为: {} 的小说数量", userId);
            novelCount = novelRepository.countByUserId(userId);
            logger.debug("用户ID: {} 的小说数量统计结果为: {}", userId, novelCount);
        } catch (Exception e) {
            // 如果查询出错，则默认为0
            logger.error("统计用户ID为: {} 的小说数量失败: {}", userId, e.getMessage());
            logger.debug("异常详情: ", e);
        }
        
        // 目前暂不支持阅读统计和收藏统计，后续可扩展
        int totalViews = 0;
        int favoriteCount = 0;
        
        UserStatsResponse response = new UserStatsResponse(novelCount, totalViews, favoriteCount);
        logger.debug("返回用户ID: {} 的统计结果: {}", userId, response);
        return response;
    }
    
    /**
     * 将用户实体转换为用户资料响应
     *
     * @param user 用户实体
     * @return 用户资料响应
     */
    private UserProfileResponse convertToUserProfileResponse(User user) {
        // 将角色枚举转换为字符串集合
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                roles
        );
    }
} 