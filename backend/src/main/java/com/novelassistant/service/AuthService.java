package com.novelassistant.service;

import com.novelassistant.payload.request.LoginRequest;
import com.novelassistant.payload.request.SignupRequest;
import com.novelassistant.payload.response.JwtResponse;
import com.novelassistant.payload.response.MessageResponse;

public interface AuthService {
    /**
     * 用户登录认证
     * 
     * @param loginRequest 登录请求参数
     * @return JWT响应，包含用户信息和令牌
     */
    JwtResponse authenticateUser(LoginRequest loginRequest);
    
    /**
     * 用户注册
     * 
     * @param signUpRequest 注册请求参数
     * @return 注册结果消息
     */
    MessageResponse registerUser(SignupRequest signUpRequest);
    
    /**
     * 初始化角色和管理员账号
     */
    void initializeRolesAndAdmin();
} 