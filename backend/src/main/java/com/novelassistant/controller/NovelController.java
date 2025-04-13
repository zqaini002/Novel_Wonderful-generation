package com.novelassistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Tag;
import com.novelassistant.service.NovelService;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8081")
public class NovelController {
    
    @Autowired
    private NovelService novelService;
    
    @GetMapping("/novels")
    public ResponseEntity<?> getNovelList() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("novels", novelService.getAllNovels());
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
            return ResponseEntity.ok(novelService.getNovelStatus(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
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