package com.novelassistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")  // 只有ADMIN角色的用户才能访问这些接口
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private NovelService novelService;
    
    /**
     * 获取系统统计信息
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        logger.info("接收到获取仪表盘数据请求");
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            logger.info("仪表盘数据获取成功: {}", stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取仪表盘数据失败", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        logger.info("接收到获取所有用户列表请求");
        try {
            List<User> users = adminService.getAllUsers();
            logger.info("获取到 {} 个用户", users.size());
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("total", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetail(@PathVariable("id") Long id) {
        logger.info("接收到获取用户详情请求, 用户ID: {}", id);
        try {
            User user = adminService.getUserById(id);
            logger.info("获取用户详情成功: {}", user.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("获取用户详情失败, 用户ID: " + id, e);
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
        logger.info("接收到修改用户状态请求, 用户ID: {}, 启用状态: {}", id, enabled);
        try {
            User user = adminService.updateUserStatus(id, enabled);
            logger.info("用户状态修改成功, 用户: {}, 新状态: {}", user.getUsername(), enabled);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("修改用户状态失败, 用户ID: " + id, e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        logger.info("接收到删除用户请求, 用户ID: {}", id);
        try {
            adminService.deleteUser(id);
            logger.info("用户删除成功, ID: {}", id);
            return ResponseEntity.ok(Collections.singletonMap("message", "用户已删除"));
        } catch (Exception e) {
            logger.error("删除用户失败, 用户ID: " + id, e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 获取所有小说列表（管理员视图）
     */
    @GetMapping("/novels")
    public ResponseEntity<?> getAllNovels() {
        logger.info("接收到获取所有小说列表请求");
        try {
            List<Novel> novels = novelService.getAllNovels();
            logger.info("获取到 {} 个小说", novels.size());
            Map<String, Object> response = new HashMap<>();
            response.put("novels", novels);
            response.put("total", novels.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取小说列表失败", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 删除小说
     */
    @DeleteMapping("/novels/{id}")
    public ResponseEntity<?> deleteNovel(@PathVariable("id") Long id) {
        logger.info("接收到删除小说请求, 小说ID: {}", id);
        try {
            adminService.deleteNovel(id);
            logger.info("小说删除成功, ID: {}", id);
            return ResponseEntity.ok(Collections.singletonMap("message", "小说已删除"));
        } catch (Exception e) {
            logger.error("删除小说失败, 小说ID: " + id, e);
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
        logger.info("接收到获取系统日志请求, 页码: {}, 每页大小: {}", page, size);
        try {
            Map<String, Object> logs = adminService.getSystemLogs(page, size);
            logger.info("获取系统日志成功, 共 {} 条日志", logs.get("total"));
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("获取系统日志失败", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 清理系统缓存
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<?> clearSystemCache() {
        logger.info("接收到清理系统缓存请求");
        try {
            adminService.clearSystemCache();
            logger.info("系统缓存清理成功");
            return ResponseEntity.ok(Collections.singletonMap("message", "系统缓存已清理"));
        } catch (Exception e) {
            logger.error("清理系统缓存失败", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
} 
 