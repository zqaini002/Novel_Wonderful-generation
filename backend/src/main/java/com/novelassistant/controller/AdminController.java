package com.novelassistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.User;
import com.novelassistant.service.AdminService;
import com.novelassistant.service.NovelService;
import com.novelassistant.repository.UserRepository;
import com.novelassistant.repository.NovelRepository;

import java.util.*;

/**
 * 后台管理控制器，提供管理员操作的API接口
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")  // 只有ADMIN角色的用户才能访问这些接口
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private NovelService novelService;
    
    @Autowired
    private PasswordEncoder encoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
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
     * 获取详细的系统统计信息
     */
    @GetMapping("/dashboard/detailed")
    public ResponseEntity<?> getDetailedSystemStats() {
        logger.info("接收到获取详细系统统计信息请求");
        try {
            Map<String, Object> stats = adminService.getDetailedSystemStats();
            logger.info("详细系统统计信息获取成功");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("获取详细系统统计信息失败", e);
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> statusMap) {
        logger.info("接收到修改用户状态请求, 用户ID: {}, 状态数据: {}", id, statusMap);
        try {
            if (statusMap == null || !statusMap.containsKey("enabled")) {
                logger.error("请求参数错误，缺少enabled字段");
                return ResponseEntity.badRequest().body(Map.of("message", "请求参数错误，缺少enabled字段"));
            }
            
            boolean enabled = statusMap.get("enabled");
            logger.info("用户ID: {}, 目标状态: {}", id, enabled);
            
            User user = adminService.getUserById(id);
            
            if (user == null) {
                logger.error("用户不存在, ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // 检查是否试图禁用唯一的管理员
            if (!enabled && isLastAdmin(user)) {
                logger.error("无法禁用唯一的管理员账户, 用户: {}", user.getUsername());
                return ResponseEntity.badRequest().body(Map.of("message", "无法禁用唯一的管理员账户"));
            }
            
            User updatedUser = adminService.updateUserStatus(id, enabled);
            logger.info("用户状态更新成功, 用户: {}, 新状态: {}", updatedUser.getUsername(), enabled);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("更新用户状态失败, 用户ID: " + id, e);
            return ResponseEntity.badRequest().body(Map.of("message", "更新用户状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查用户是否是最后一个管理员
     */
    private boolean isLastAdmin(User user) {
        // 检查这个用户是否有管理员角色
        boolean userIsAdmin = user.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        
        if (!userIsAdmin) {
            return false; // 如果用户不是管理员，就不是最后一个管理员
        }
        
        // 计算启用状态的管理员数量
        int adminCount = 0;
        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if (u.isEnabled() && u.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()))) {
                adminCount++;
            }
        }
        
        // 如果只有一个启用状态的管理员，且就是这个用户，那么这是最后一个管理员
        return adminCount <= 1;
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
     * 获取所有小说列表
     */
    @GetMapping("/novels")
    public ResponseEntity<?> getAllNovels() {
        try {
            List<Novel> novels = novelService.getAllNovels();
            Map<String, Object> response = new HashMap<>();
            response.put("novels", novels);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 获取所有小说列表（包括已删除的）
     */
    @GetMapping("/novels/all")
    public ResponseEntity<?> getAllNovelsIncludeDeleted() {
        try {
            List<Novel> novels = novelRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("novels", novels);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 获取已删除的小说列表
     */
    @GetMapping("/novels/deleted")
    public ResponseEntity<?> getDeletedNovels() {
        try {
            List<Novel> novels = novelRepository.findByIsDeletedTrue();
            Map<String, Object> response = new HashMap<>();
            response.put("novels", novels);
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
     * 恢复已删除的小说
     */
    @PostMapping("/novels/{id}/restore")
    public ResponseEntity<?> restoreNovel(@PathVariable("id") Long id) {
        try {
            boolean result = novelService.restoreNovel(id);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "小说恢复成功");
                response.put("id", id);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "小说不存在或已经是正常状态"));
            }
        } catch (Exception e) {
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
            // 捕获可能返回的异常
            long startTime = System.currentTimeMillis();
            adminService.clearSystemCache();
            long endTime = System.currentTimeMillis();
            
            // 构建详细响应
            Map<String, Object> response = new HashMap<>();
            response.put("message", "系统缓存已清理成功");
            response.put("status", "success");
            response.put("timeSpent", (endTime - startTime) + "ms");
            
            // 获取最新系统状态
            Map<String, Object> stats = adminService.getDashboardStats();
            response.put("systemStats", stats);
            
            logger.info("系统缓存清理成功，耗时: {}ms", (endTime - startTime));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("清理系统缓存失败", e);
            
            // 构建错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            errorResponse.put("details", e.getClass().getName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserInfo(@PathVariable Long id, @RequestBody Map<String, String> userData) {
        try {
            User user = adminService.getUserById(id);
            
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 更新密码
            if (userData.containsKey("password")) {
                String newPassword = userData.get("password");
                if (newPassword != null && !newPassword.isEmpty()) {
                    user.setPassword(encoder.encode(newPassword));
                }
            }
            
            // 可以添加更多用户信息更新逻辑，例如邮箱等
            if (userData.containsKey("email")) {
                String newEmail = userData.get("email");
                if (newEmail != null && !newEmail.isEmpty()) {
                    user.setEmail(newEmail);
                }
            }
            
            // 直接应用变更到已获取的用户对象
            User updatedUser = adminService.updateUserStatus(id, user.isEnabled());
            
            return ResponseEntity.ok(Map.of("message", "用户信息已更新", "userId", id));
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "更新用户信息失败: " + e.getMessage()));
        }
    }
} 
 