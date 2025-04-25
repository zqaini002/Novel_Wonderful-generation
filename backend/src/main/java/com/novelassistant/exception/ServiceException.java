package com.novelassistant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 服务层异常
 * 用于表示业务逻辑错误或业务规则违反
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ServiceException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码，用于标识不同类型的业务错误
     */
    private final String errorCode;
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     */
    public ServiceException(String message) {
        super(message);
        this.errorCode = "SERVICE_ERROR";
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param errorCode 错误代码
     */
    public ServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param cause 原始异常
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SERVICE_ERROR";
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param cause 原始异常
     * @param errorCode 错误代码
     */
    public ServiceException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
} 
 