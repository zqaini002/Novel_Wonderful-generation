package com.novelassistant.service;

import java.util.Date;
import java.util.Map;

/**
 * 系统日志服务接口
 */
public interface SystemLogService {
    
    /**
     * 获取系统日志
     * 
     * @param page 页码
     * @param size 每页大小
     * @param level 日志级别 (INFO, WARN, ERROR, DEBUG)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param query 搜索关键词
     * @param userId 用户ID
     * @return 日志数据，包含日志列表和分页信息
     */
    Map<String, Object> getSystemLogs(int page, int size, String level, 
                                      Date startDate, Date endDate, 
                                      String query, String userId);
    
    /**
     * 添加系统日志
     * 
     * @param level 日志级别
     * @param logger Logger名称
     * @param message 日志消息
     * @param userId 用户ID (可选)
     * @param ip IP地址 (可选)
     * @param thread 线程名 (可选)
     * @param stackTrace 堆栈跟踪 (可选)
     * @param params 其他参数 (可选)
     */
    void addSystemLog(String level, String logger, String message, 
                      String userId, String ip, String thread, 
                      String stackTrace, Map<String, Object> params);
    
    /**
     * 清理指定天数前的日志
     * 
     * @param days 天数
     * @return 清理的日志条数
     */
    int cleanLogs(int days);
} 