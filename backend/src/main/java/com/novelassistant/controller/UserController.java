package com.novelassistant.controller;

import com.novelassistant.entity.User;
import com.novelassistant.payload.request.UpdateProfileRequest;
import com.novelassistant.payload.request.ChangePasswordRequest;
import com.novelassistant.payload.response.MessageResponse;
import com.novelassistant.payload.response.UserProfileResponse;
import com.novelassistant.payload.response.UserStatsResponse;
import com.novelassistant.security.services.UserDetailsImpl;
import com.novelassistant.service.UserService;
import com.novelassistant.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 * 处理用户个人资料、密码修改等相关请求
 */
@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private NovelService novelService;

    /**
     * 获取当前用户信息
     *
     * @param userDetails 当前认证用户
     * @return 用户信息
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserProfileResponse userProfile = userService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(userProfile);
    }

    /**
     * 更新用户个人资料
     *
     * @param userDetails 当前认证用户
     * @param profileRequest 更新请求
     * @return 更新结果
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateProfileRequest profileRequest) {
        
        // 确保用户只能修改自己的资料
        if (!userDetails.getId().equals(profileRequest.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 不能修改其他用户的资料!"));
        }
        
        UserProfileResponse updatedProfile = userService.updateUserProfile(profileRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * 修改用户密码
     *
     * @param userDetails 当前认证用户
     * @param passwordRequest 密码修改请求
     * @return 修改结果
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChangePasswordRequest passwordRequest) {
        
        boolean result = userService.changePassword(
                userDetails.getId(),
                passwordRequest.getOldPassword(),
                passwordRequest.getNewPassword()
        );
        
        if (result) {
            return ResponseEntity.ok(new MessageResponse("密码修改成功!"));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 原密码不正确!"));
        }
    }

    /**
     * 获取用户统计信息
     *
     * @param userDetails 当前认证用户
     * @return 用户统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserStats(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserStatsResponse stats = userService.getUserStats(userDetails.getId());
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取用户上传的小说列表
     *
     * @param userDetails 当前认证用户
     * @return 用户上传的小说列表
     */
    @GetMapping("/novels")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserNovels(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("novels", novelService.getNovelsByUserId(userDetails.getId()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
} 