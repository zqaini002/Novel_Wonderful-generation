package com.novelassistant.controller;

import com.novelassistant.payload.request.LoginRequest;
import com.novelassistant.payload.request.SignupRequest;
import com.novelassistant.service.AuthService;
import com.novelassistant.util.LogUtil;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private static final Logger logger = LogUtil.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("用户登录请求 - 用户名: {}", loginRequest.getUsername());
        try {
            ResponseEntity<?> response = ResponseEntity.ok(authService.authenticateUser(loginRequest));
            logger.info("用户 {} 登录成功", loginRequest.getUsername());
            return response;
        } catch (Exception e) {
            logger.error("用户 {} 登录失败: {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("用户注册请求 - 用户名: {}, 邮箱: {}", signUpRequest.getUsername(), signUpRequest.getEmail());
        try {
            ResponseEntity<?> response = ResponseEntity.ok(authService.registerUser(signUpRequest));
            logger.info("用户 {} 注册成功", signUpRequest.getUsername());
            return response;
        } catch (Exception e) {
            logger.error("用户 {} 注册失败: {}", signUpRequest.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/check")
    public ResponseEntity<?> checkAuthStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        
        Map<String, Object> response = new HashMap<>();
        response.put("isAuthenticated", isAuthenticated);
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities());
        
        if (isAuthenticated) {
            logger.debug("认证状态检查 - 用户: {}, 已认证", auth.getName());
        } else {
            logger.debug("认证状态检查 - 未认证用户");
        }
        
        return ResponseEntity.ok(response);
    }
}