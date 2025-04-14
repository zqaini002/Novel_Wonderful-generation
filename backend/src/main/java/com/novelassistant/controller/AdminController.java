package com.novelassistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.User;
import com.novelassistant.service.AdminService;
import com.novelassistant.service.NovelService;

import java.util.*;

/**
 * 后台管理控制器，提供管理员操作的API接口
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8081")
@PreAuthorize("hasRole('ADMIN')")  // 只有ADMIN角色的用户才能访问这些接口
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private NovelService novelService;
    
    /**
     * 获取系统统计信息
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(Collections.singletonMap("users", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetail(@PathVariable("id") Long id) {
        try {
            User user = adminService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 修改用户状态（启用/禁用）
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable("id") Long id,
            @RequestParam("enabled") boolean enabled) {
        try {
            User user = adminService.updateUserStatus(id, enabled);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "用户已删除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 获取所有小说列表（管理员视图）
     */
    @GetMapping("/novels")
    public ResponseEntity<?> getAllNovels() {
        try {
            List<Novel> novels = novelService.getAllNovels();
            Map<String, Object> response = new HashMap<>();
            response.put("novels", novels);
            response.put("total", novels.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 删除小说
     */
    @DeleteMapping("/novels/{id}")
    public ResponseEntity<?> deleteNovel(@PathVariable("id") Long id) {
        try {
            adminService.deleteNovel(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "小说已删除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 获取系统日志
     */
    @GetMapping("/logs")
    public ResponseEntity<?> getSystemLogs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            Map<String, Object> logs = adminService.getSystemLogs(page, size);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 清理系统缓存
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<?> clearSystemCache() {
        try {
            adminService.clearSystemCache();
            return ResponseEntity.ok(Collections.singletonMap("message", "系统缓存已清理"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
} 
 