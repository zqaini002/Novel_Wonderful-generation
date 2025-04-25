package com.novelassistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Tag;
import com.novelassistant.service.NovelService;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.security.services.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@RestController
@RequestMapping({"/", "/api"})
public class NovelController {
    
    private static final Logger logger = LoggerFactory.getLogger(NovelController.class);
    
    @Autowired
    private NovelService novelService;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @GetMapping("/novels")
    public ResponseEntity<?> getNovelList(@RequestParam(value = "userId", required = false) Long userId) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (userId != null) {
                // 如果提供了用户ID，过滤该用户的小说
                response.put("novels", novelService.getNovelsByUserId(userId));
            } else {
                // 否则返回所有小说
                response.put("novels", novelService.getAllNovels());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    @GetMapping("/novels/{id}")
    public ResponseEntity<?> getNovelDetail(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(novelService.getNovelById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    @GetMapping("/novels/{id}/status")
    public ResponseEntity<?> getNovelStatus(@PathVariable("id") Long id) {
        try {
            // 检查小说是否存在
            boolean exists = novelService.existsNovelById(id);
            if (!exists) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "小说不存在");
                errorResponse.put("id", id);
                errorResponse.put("status", "NOT_FOUND");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            return ResponseEntity.ok(novelService.getNovelStatus(id));
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/novels/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadNovel(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "author", required = false) String author) {
        try {
            Long userId = userDetails.getId();
            return ResponseEntity.ok(novelService.processNovel(file, title, author, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    @GetMapping("/novels/{id}/chapters")
    public ResponseEntity<?> getNovelChapters(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(novelService.getChaptersByNovelId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    @GetMapping("/novels/{id}/tags")
    public ResponseEntity<?> getNovelTags(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(novelService.getTagsByNovelId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/novels/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteNovel(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            // 获取当前登录用户ID
            Long userId = userDetails.getId();
            
            // 先检查小说是否存在
            if (!novelService.existsNovelById(id)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "小说不存在");
                errorResponse.put("id", id);
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            // 获取小说并验证所有者（只能删除自己的小说，管理员除外）
            Novel novel = novelService.getNovelById(id);
            boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                
            if (!isAdmin && !userId.equals(novel.getUserId())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "无权限删除此小说");
                return ResponseEntity.status(403).body(errorResponse);
            }
            
            // 执行删除操作
            novelService.deleteNovel(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "小说删除成功");
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 刷新小说标签
     * 基于深度内容分析更新小说标签
     */
    @PostMapping("/novels/{id}/refresh-tags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> refreshNovelTags(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            // 获取当前登录用户ID
            Long userId = userDetails.getId();
            
            // 先检查小说是否存在
            if (!novelService.existsNovelById(id)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "小说不存在");
                errorResponse.put("id", id);
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            // 获取小说并验证所有者（只能刷新自己的小说标签，管理员除外）
            Novel novel = novelService.getNovelById(id);
            boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                
            if (!isAdmin && !userId.equals(novel.getUserId())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "无权限刷新此小说标签");
                return ResponseEntity.status(403).body(errorResponse);
            }
            
            // 执行标签刷新操作
            int tagCount = novelService.refreshNovelTags(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "小说标签刷新成功");
            response.put("id", id);
            response.put("tagCount", tagCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 恢复已删除的小说
     */
    @PostMapping("/novels/{id}/restore")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> restoreNovel(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("接收到恢复小说请求, ID: {}, 用户: {}", id, userDetails.getUsername());
        
        // 检查用户对小说的所有权
        Novel novel = novelRepository.findById(id).orElse(null);
        if (novel == null) {
            logger.error("恢复小说失败: 小说不存在, ID: {}", id);
            Map<String, String> error = new HashMap<>();
            error.put("message", "小说不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        
        if (!novel.getUserId().equals(userDetails.getId()) && !userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            logger.error("恢复小说失败: 权限不足, 用户ID: {}, 小说所有者ID: {}", userDetails.getId(), novel.getUserId());
            Map<String, String> error = new HashMap<>();
            error.put("message", "没有权限恢复此小说");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        
        try {
            boolean result = novelService.restoreNovel(id);
            if (result) {
                logger.info("小说恢复成功, ID: {}", id);
                return ResponseEntity.ok().build();
            } else {
                logger.error("恢复小说失败: 小说不存在或已经是正常状态, ID: {}", id);
                Map<String, String> error = new HashMap<>();
                error.put("message", "小说不存在或已经是正常状态");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            logger.error("恢复小说时发生错误: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "恢复小说失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 获取用户已删除的小说列表
     */
    @GetMapping("/user/novels/deleted")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDeletedNovels(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("接收到获取已删除小说列表请求, 用户: {}", userDetails.getUsername());
        
        try {
            List<Novel> deletedNovels = novelService.getDeletedNovelsByUserId(userDetails.getId());
            logger.info("获取已删除小说列表成功, 用户: {}, 数量: {}", userDetails.getUsername(), deletedNovels.size());
            return ResponseEntity.ok(deletedNovels);
        } catch (Exception e) {
            logger.error("获取已删除小说列表失败: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取已删除小说列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 从URL导入小说
     * 仅支持笔趣阁网站
     */
    @PostMapping("/novels/import-from-url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> importNovelFromUrl(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("url") String url,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "maxChapters", required = false, defaultValue = "10") int maxChapters) {
        try {
            Long userId = userDetails.getId();
            logger.info("接收到从URL导入小说请求, URL: {}, 标题: {}, 作者: {}, 用户ID: {}, 最大章节数: {}", 
                        url, title, author, userId, maxChapters);
            
            // 验证URL格式
            if (!url.startsWith("http")) {
                logger.error("无效的URL格式: {}", url);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "无效的URL格式，URL必须以http或https开头");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 调用导入服务
            Map<String, Object> result = novelService.importNovelFromUrl(url, userId, maxChapters);
            
            if ((Boolean) result.getOrDefault("success", false)) {
                logger.info("从URL导入小说成功, 小说ID: {}, 标题: {}", result.get("novelId"), result.get("title"));
                return ResponseEntity.ok(result);
            } else {
                logger.error("从URL导入小说失败: {}", result.get("error"));
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("从URL导入小说时发生错误: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "导入小说失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 从URL导入小说 - 别名方法，与import-from-url功能相同
     * 仅支持笔趣阁网站
     */
    @PostMapping("/novels/upload-from-url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadNovelFromUrl(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("url") String url,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "maxChapters", required = false, defaultValue = "10") int maxChapters) {
        return importNovelFromUrl(userDetails, url, title, author, maxChapters);
    }
} 