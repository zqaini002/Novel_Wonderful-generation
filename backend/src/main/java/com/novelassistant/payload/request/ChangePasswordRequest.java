package com.novelassistant.payload.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求
 */
@Data
public class ChangePasswordRequest {
    
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度必须在6到40个字符之间")
    private String newPassword;
} 