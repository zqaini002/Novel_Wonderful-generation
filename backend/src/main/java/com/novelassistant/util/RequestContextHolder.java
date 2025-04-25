package com.novelassistant.util;

import org.springframework.stereotype.Component;

/**
 * 请求上下文持有者，用于在请求上下文中存储和获取数据
 */
@Component
public class RequestContextHolder {

    private static final ThreadLocal<Long> CURRENT_NOVEL_ID = new ThreadLocal<>();

    /**
     * 设置当前正在处理的小说ID
     * @param novelId 小说ID
     */
    public static void setCurrentNovelId(Long novelId) {
        CURRENT_NOVEL_ID.set(novelId);
    }

    /**
     * 获取当前正在处理的小说ID
     * @return 小说ID，如果没有设置则返回null
     */
    public static Long getCurrentNovelId() {
        return CURRENT_NOVEL_ID.get();
    }

    /**
     * 清除当前小说ID
     */
    public static void clearCurrentNovelId() {
        CURRENT_NOVEL_ID.remove();
    }

    /**
     * 清除所有线程本地变量，防止内存泄漏
     * 应在请求结束时调用
     */
    public static void clear() {
        clearCurrentNovelId();
    }
} 