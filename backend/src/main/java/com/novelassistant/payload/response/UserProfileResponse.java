package com.novelassistant.payload.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

/**
 * 用户个人资料响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private Date createdAt; // 注册时间
    private Date updatedAt; // 更新时间
    private Date lastLoginAt; // 上次登录时间
    private Set<String> roles;
    
    // 不包含密码字段，确保安全
} 