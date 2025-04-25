package com.novelassistant.util;

/**
 * 错误代码常量类
 * 用于统一管理应用中的错误代码
 */
public final class ErrorCodes {
    
    /**
     * 私有构造函数，防止实例化
     */
    private ErrorCodes() {
    }
    
    // ==================== 通用错误 ====================
    
    /**
     * 未知错误
     */
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    
    /**
     * 服务器内部错误
     */
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    
    /**
     * 服务暂时不可用
     */
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    
    // ==================== 认证与授权错误 ====================
    
    /**
     * 未认证错误
     */
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    
    /**
     * 权限不足错误
     */
    public static final String FORBIDDEN = "FORBIDDEN";
    
    /**
     * 用户名或密码错误
     */
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    
    /**
     * 令牌无效
     */
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    
    /**
     * 令牌过期
     */
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    
    // ==================== 数据库错误 ====================
    
    /**
     * 数据库访问错误
     */
    public static final String DB_ERROR = "DB_ERROR";
    
    /**
     * 数据不存在
     */
    public static final String DATA_NOT_FOUND = "DATA_NOT_FOUND";
    
    /**
     * 数据已存在
     */
    public static final String DATA_ALREADY_EXISTS = "DATA_ALREADY_EXISTS";
    
    /**
     * 数据完整性错误
     */
    public static final String DATA_INTEGRITY_ERROR = "DATA_INTEGRITY_ERROR";
    
    /**
     * 外键约束错误
     */
    public static final String FOREIGN_KEY_CONSTRAINT_ERROR = "FOREIGN_KEY_CONSTRAINT_ERROR";
    
    /**
     * 唯一约束错误
     */
    public static final String UNIQUE_CONSTRAINT_ERROR = "UNIQUE_CONSTRAINT_ERROR";
    
    // ==================== 业务逻辑错误 ====================
    
    /**
     * 服务逻辑错误
     */
    public static final String SERVICE_ERROR = "SERVICE_ERROR";
    
    /**
     * 操作失败
     */
    public static final String OPERATION_FAILED = "OPERATION_FAILED";
    
    /**
     * 操作被拒绝
     */
    public static final String OPERATION_REJECTED = "OPERATION_REJECTED";
    
    // ==================== 资源相关错误 ====================
    
    /**
     * 资源未找到
     */
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    
    /**
     * 资源已存在
     */
    public static final String RESOURCE_ALREADY_EXISTS = "RESOURCE_ALREADY_EXISTS";
    
    /**
     * 资源不可访问
     */
    public static final String RESOURCE_INACCESSIBLE = "RESOURCE_INACCESSIBLE";
    
    // ==================== 输入验证错误 ====================
    
    /**
     * 输入验证错误
     */
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    
    /**
     * 缺少必填字段
     */
    public static final String MISSING_REQUIRED_FIELD = "MISSING_REQUIRED_FIELD";
    
    /**
     * 无效参数
     */
    public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
    
    /**
     * 请求体错误
     */
    public static final String INVALID_REQUEST_BODY = "INVALID_REQUEST_BODY";
    
    // ==================== 文件相关错误 ====================
    
    /**
     * 文件上传错误
     */
    public static final String FILE_UPLOAD_ERROR = "FILE_UPLOAD_ERROR";
    
    /**
     * 文件不支持
     */
    public static final String UNSUPPORTED_FILE_TYPE = "UNSUPPORTED_FILE_TYPE";
    
    /**
     * 文件大小超限
     */
    public static final String FILE_SIZE_EXCEEDED = "FILE_SIZE_EXCEEDED";
    
    // ==================== 小说处理错误 ====================
    
    /**
     * 小说处理错误
     */
    public static final String NOVEL_PROCESSING_ERROR = "NOVEL_PROCESSING_ERROR";
    
    /**
     * 小说缺失
     */
    public static final String NOVEL_MISSING = "NOVEL_MISSING";
    
    /**
     * 小说章节错误
     */
    public static final String NOVEL_CHAPTER_ERROR = "NOVEL_CHAPTER_ERROR";
    
    // ==================== 角色处理错误 ====================
    
    /**
     * 角色处理错误
     */
    public static final String CHARACTER_ERROR = "CHARACTER_ERROR";
    
    /**
     * 保存角色错误
     */
    public static final String CHARACTER_SAVE_ERROR = "CHARACTER_SAVE_ERROR";
    
    /**
     * 角色关系错误
     */
    public static final String CHARACTER_RELATIONSHIP_ERROR = "CHARACTER_RELATIONSHIP_ERROR";
    
    /**
     * 保存角色关系错误
     */
    public static final String RELATIONSHIP_SAVE_ERROR = "RELATIONSHIP_SAVE_ERROR";
} 
 