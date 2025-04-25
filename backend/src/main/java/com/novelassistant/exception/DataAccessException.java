package com.novelassistant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 数据访问异常
 * 当数据库操作失败时抛出
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DataAccessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码，用于标识不同类型的数据访问错误
     */
    private final String errorCode;
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     */
    public DataAccessException(String message) {
        super(message);
        this.errorCode = "DB_ERROR";
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param errorCode 错误代码
     */
    public DataAccessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param cause 原始异常
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DB_ERROR";
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param cause 原始异常
     * @param errorCode 错误代码
     */
    public DataAccessException(String message, Throwable cause, String errorCode) {
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
 