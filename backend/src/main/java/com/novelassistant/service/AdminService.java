package com.novelassistant.service;

import com.novelassistant.entity.User;
import java.util.List;
import java.util.Map;

/**
 * 后台管理服务接口
 */
public interface AdminService {
    
    /**
     * 获取系统统计信息
     * @return 统计信息，包括用户数、小说数、处理中小说数等
     */
    Map<String, Object> getDashboardStats();
    
    /**
     * 获取详细的系统统计信息
     * @return 详细统计信息，包括系统状态、存储信息等
     */
    Map<String, Object> getDetailedSystemStats();
    
    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    List<User> getAllUsers();
    
    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    User getUserById(Long id);
    
    /**
     * 更新用户状态（启用/禁用）
     * @param id 用户ID
     * @param enabled 是否启用
     * @return 更新后的用户信息
     */
    User updateUserStatus(Long id, boolean enabled);
    
    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteUser(Long id);
    
    /**
     * 删除小说
     * @param id 小说ID
     */
    void deleteNovel(Long id);
    
    /**
     * 清理系统缓存
     */
    void clearSystemCache();
} 
 