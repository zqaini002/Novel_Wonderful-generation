package com.novelassistant.service;

import com.novelassistant.payload.request.UpdateProfileRequest;
import com.novelassistant.payload.response.UserProfileResponse;
import com.novelassistant.payload.response.UserStatsResponse;

/**
 * 用户服务接口
 * 处理用户资料相关的业务逻辑
 */
public interface UserService {
    
    /**
     * 获取用户个人资料
     *
     * @param userId 用户ID
     * @return 用户个人资料响应
     */
    UserProfileResponse getUserProfile(Long userId);
    
    /**
     * 更新用户个人资料
     *
     * @param profileRequest 更新请求
     * @return 更新后的用户个人资料
     */
    UserProfileResponse updateUserProfile(UpdateProfileRequest profileRequest);
    
    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 密码修改是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 获取用户统计信息
     *
     * @param userId 用户ID
     * @return 用户统计信息
     */
    UserStatsResponse getUserStats(Long userId);
} 