package com.novelassistant.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 日志工具类
 * 提供统一的日志记录方法，支持请求跟踪和结构化日志
 */
public class LogUtil {
    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID = "userId";
    
    /**
     * 获取指定类的Logger
     * @param clazz 类
     * @return Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * 开始请求日志跟踪
     * @param requestId 请求ID，如果为null则自动生成
     * @return 请求ID
     */
    public static String startRequest(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(REQUEST_ID, requestId);
        return requestId;
    }
    
    /**
     * 设置当前用户ID
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.isEmpty()) {
            MDC.put(USER_ID, userId);
        }
    }
    
    /**
     * 结束请求日志跟踪
     */
    public static void endRequest() {
        MDC.remove(REQUEST_ID);
        MDC.remove(USER_ID);
    }
    
    /**
     * 格式化异常信息
     * @param e 异常
     * @return 格式化后的异常信息
     */
    public static String formatException(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().startsWith("com.novelassistant")) {
                sb.append("\tat ").append(element).append("\n");
            }
        }
        return sb.toString();
    }
} 