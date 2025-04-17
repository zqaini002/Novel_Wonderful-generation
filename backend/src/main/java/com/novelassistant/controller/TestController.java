package com.novelassistant.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true", maxAge = 3600)
@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/public")
    public Map<String, Object> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "公开API访问成功，无需认证");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Map<String, Object> userEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "用户API访问成功，需要USER或ADMIN角色");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "管理员API访问成功，需要ADMIN角色");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
} 