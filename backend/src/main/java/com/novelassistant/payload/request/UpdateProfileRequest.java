package com.novelassistant.payload.request;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 用户资料更新请求
 */
@Data
public class UpdateProfileRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long id;
    
    @Email(message = "请提供有效的邮箱地址")
    @NotBlank(message = "邮箱不能为空")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    // 以下字段用于密码修改，允许为空
    
    private String oldPassword;
    
    @Size(min = 6, max = 40, message = "密码长度必须在6到40个字符之间")
    private String newPassword;
} 