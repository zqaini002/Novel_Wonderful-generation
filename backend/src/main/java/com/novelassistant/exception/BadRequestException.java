package com.novelassistant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 错误请求异常
 * 用于表示客户端请求参数错误
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 默认构造函数
     */
    public BadRequestException() {
        super("Bad request");
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     */
    public BadRequestException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param cause 异常原因
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
 