package com.novelassistant.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.novelassistant.payload.response.MessageResponse;
import com.novelassistant.util.LogUtil;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LogUtil.getLogger(GlobalExceptionHandler.class);
    
    // 处理验证错误
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        
        logger.warn("请求参数验证失败: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }
    
    // 处理认证失败异常
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {
        logger.warn("认证失败: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("用户名或密码错误"));
    }
    
    // 处理用户禁用异常
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledException(DisabledException ex) {
        logger.warn("禁用账户尝试登录: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("账号已被禁用，请联系管理员"));
    }
    
    // 处理用户锁定异常
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLockedException(LockedException ex) {
        logger.warn("锁定账户尝试登录: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("账号已被锁定，请稍后再试或联系管理员"));
    }
    
    // 处理用户名不存在异常
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        logger.warn("用户不存在: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("用户名或密码错误"));
    }
    
    // 通用认证异常处理
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("认证异常: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("认证失败，请检查您的凭据"));
    }
    
    // 权限不足异常
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("权限不足: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse("您没有权限执行此操作"));
    }
    
    // 通用异常处理
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("全局异常处理: {}", ex.getMessage());
        logger.error("异常详情: ", ex);
        
        String message = "处理请求时发生错误";
        if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            message = ex.getMessage();
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse(message));
    }
} 