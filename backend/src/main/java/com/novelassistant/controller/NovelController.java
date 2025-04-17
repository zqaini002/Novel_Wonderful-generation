package com.novelassistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Tag;
import com.novelassistant.service.NovelService;
import com.novelassistant.security.services.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.*;

@RestController
@RequestMapping({"/", "/api"})
public class NovelController {
    
    @Autowired
    private NovelService novelService;
    
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
    public ResponseEntity<?> uploadNovel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "author", required = false) String author) {
        try {
            return ResponseEntity.ok(novelService.processNovel(file, title, author));
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
} 