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
import com.novelassistant.exception.*;

import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

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
    
    // 处理自定义验证异常
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex) {
        logger.warn("数据验证失败: {}, 错误代码: {}", ex.getMessage(), ex.getErrorCode());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("errorCode", ex.getErrorCode());
        response.put("fieldErrors", ex.getFieldErrors());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // 处理服务层异常
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<?> handleServiceException(ServiceException ex) {
        logger.warn("业务逻辑错误: {}, 错误代码: {}", ex.getMessage(), ex.getErrorCode());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("errorCode", ex.getErrorCode());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // 处理自定义数据访问异常
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDataAccessException(DataAccessException ex) {
        logger.error("数据访问错误: {}, 错误代码: {}", ex.getMessage(), ex.getErrorCode(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "数据库操作失败");
        response.put("errorCode", ex.getErrorCode());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    // 处理资源未找到异常
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("资源未找到: {}, 资源类型: {}, 资源ID: {}", 
                ex.getMessage(), ex.getResourceType(), ex.getResourceId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("resourceType", ex.getResourceType());
        response.put("resourceId", ex.getResourceId());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    // 处理错误请求异常
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        logger.warn("错误请求: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }
    
    // 处理数据完整性冲突异常（如外键约束、唯一键冲突等）
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "数据操作违反完整性约束";
        String details = ex.getMessage();
        
        // 尝试提取更友好的错误消息
        if (details != null) {
            if (details.contains("foreign key constraint")) {
                message = "操作失败: 数据被其他记录引用，无法删除";
            } else if (details.contains("unique constraint") || details.contains("Duplicate entry")) {
                message = "操作失败: 数据已存在，不能重复添加";
            }
        }
        
        logger.error("数据完整性错误: {}", details, ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("errorCode", "DATA_INTEGRITY_ERROR");
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    // 处理SQL异常
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> handleSQLException(SQLException ex) {
        String errorCode = String.valueOf(ex.getErrorCode());
        String sqlState = ex.getSQLState();
        
        logger.error("SQL异常: 错误码={}, SQL状态={}, 消息={}", errorCode, sqlState, ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "数据库操作失败");
        response.put("errorCode", "SQL_ERROR_" + errorCode);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    // 处理不正确的数据访问API使用异常
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<?> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {
        logger.error("不正确的数据访问API使用: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "数据访问方法使用错误");
        response.put("errorCode", "INVALID_DATA_ACCESS_USAGE");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("errorCode", "INTERNAL_SERVER_ERROR");
        response.put("path", request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}