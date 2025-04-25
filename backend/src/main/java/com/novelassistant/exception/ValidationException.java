package com.novelassistant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证异常
 * 用于表示输入数据验证失败
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码
     */
    private final String errorCode;
    
    /**
     * 字段错误信息映射
     */
    private final Map<String, String> fieldErrors;
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     */
    public ValidationException(String message) {
        super(message);
        this.errorCode = "VALIDATION_ERROR";
        this.fieldErrors = new HashMap<>();
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param fieldErrors 字段错误信息映射
     */
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.errorCode = "VALIDATION_ERROR";
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param errorCode 错误代码
     */
    public ValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = new HashMap<>();
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param errorCode 错误代码
     * @param fieldErrors 字段错误信息映射
     */
    public ValidationException(String message, String errorCode, Map<String, String> fieldErrors) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取字段错误信息映射
     * 
     * @return 字段错误信息映射
     */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    /**
     * 添加字段错误
     * 
     * @param field 字段名
     * @param message 错误信息
     */
    public void addFieldError(String field, String message) {
        fieldErrors.put(field, message);
    }
} 
 