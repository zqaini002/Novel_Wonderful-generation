package com.novelassistant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 资源未找到异常
 * 用于表示请求的资源不存在
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 资源类型
     */
    private final String resourceType;
    
    /**
     * 资源标识
     */
    private final String resourceId;
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "UNKNOWN";
        this.resourceId = "UNKNOWN";
    }
    
    /**
     * 构造函数
     * 
     * @param resourceType 资源类型
     * @param resourceId 资源标识
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with id %s not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * 构造函数
     * 
     * @param resourceType 资源类型
     * @param resourceId 资源标识
     * @param message 错误信息
     */
    public ResourceNotFoundException(String resourceType, String resourceId, String message) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * 获取资源类型
     * 
     * @return 资源类型
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * 获取资源标识
     * 
     * @return 资源标识
     */
    public String getResourceId() {
        return resourceId;
    }
} 