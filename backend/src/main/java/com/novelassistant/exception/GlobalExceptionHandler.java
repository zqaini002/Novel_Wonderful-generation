package com.novelassistant.exception;

import com.novelassistant.payload.response.ErrorResponse;
import com.novelassistant.util.LogUtil;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

/**
 * 全局异常处理器
 * 统一处理所有未捕获的异常，并记录详细日志
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LogUtil.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        logger.error("未捕获异常: {}", ex.getMessage());
        logger.error("异常详情: {}", LogUtil.formatException(ex));
        
        ErrorResponse errorResponse = new ErrorResponse(
                new Date(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "服务器内部错误",
                ex.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理认证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(BadCredentialsException ex, WebRequest request) {
        logger.warn("认证失败: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                new Date(),
                HttpStatus.UNAUTHORIZED.value(),
                "认证失败",
                "用户名或密码错误",
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.warn("访问被拒绝: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                new Date(),
                HttpStatus.FORBIDDEN.value(),
                "权限不足",
                "您没有权限访问此资源",
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("参数校验失败: {}", ex.getMessage());
        
        StringBuilder errorDetails = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            errorDetails.append(error.getDefaultMessage()).append("; ");
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
                new Date(),
                HttpStatus.BAD_REQUEST.value(),
                "参数校验失败",
                errorDetails.toString(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
} 