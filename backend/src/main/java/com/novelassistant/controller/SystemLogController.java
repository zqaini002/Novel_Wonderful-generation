package com.novelassistant.controller;

import com.novelassistant.service.SystemLogService;
import com.novelassistant.util.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Calendar;

/**
 * 系统日志控制器
 */
@RestController
@RequestMapping("/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class SystemLogController {

    private static final Logger logger = LogUtil.getLogger(SystemLogController.class);
    
    @Autowired
    private SystemLogService systemLogService;

    /**
     * 获取系统日志
     * 
     * @param page 页码
     * @param size 每页大小
     * @param level 日志级别
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param query 搜索关键词
     * @param userId 用户ID
     * @return 系统日志列表和分页信息
     */
    @GetMapping
    public ResponseEntity<?> getSystemLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String userId) {
        
        // 调整开始日期为当天的开始时间
        if (startDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            startDate = calendar.getTime();
        }
        
        // 调整结束日期，确保包含当天的所有时间
        if (endDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            endDate = calendar.getTime();
        }
        
        logger.info("接收到获取系统日志请求，页码: {}, 每页大小: {}, 日志级别: {}, 开始日期: {}, 结束日期: {}, 查询关键词: {}, 用户ID: {}", 
                page, size, level, startDate, endDate, query, userId);
        
        try {
            Map<String, Object> logs = systemLogService.getSystemLogs(page, size, level, startDate, endDate, query, userId);
            logger.info("获取系统日志成功, 共 {} 条记录", logs.get("total"));
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("获取系统日志时发生错误", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 添加系统日志
     * 该接口主要用于调试和测试，正常日志记录应该通过SystemLogService直接调用
     */
    @PostMapping
    public ResponseEntity<?> addSystemLog(@RequestBody Map<String, Object> logData) {
        try {
            String level = (String) logData.getOrDefault("level", "INFO");
            String loggerName = (String) logData.getOrDefault("logger", "ManualLogEntry");
            String message = (String) logData.getOrDefault("message", "");
            
            // 尝试获取用户ID，如果是字符串则保留原样
            String userId = null;
            Object userIdObj = logData.get("userId");
            if (userIdObj != null) {
                if (userIdObj instanceof Number) {
                    userId = String.valueOf(userIdObj);
                } else if (userIdObj instanceof String) {
                    userId = (String) userIdObj;
                }
            }
            
            String ip = (String) logData.getOrDefault("ip", null);
            String thread = (String) logData.getOrDefault("thread", Thread.currentThread().getName());
            String stackTrace = (String) logData.getOrDefault("stackTrace", null);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) logData.get("params");
            
            systemLogService.addSystemLog(level, loggerName, message, userId, ip, thread, stackTrace, params);
            
            logger.info("手动添加系统日志成功, 级别: {}, 消息: {}", level, message);
            return ResponseEntity.ok(Collections.singletonMap("message", "日志添加成功"));
        } catch (Exception e) {
            logger.error("添加系统日志时发生错误", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 清理系统日志
     * 
     * @param days 天数，默认为30天
     * @return 清理结果
     */
    @DeleteMapping("/clean")
    public ResponseEntity<?> cleanLogs(@RequestParam(defaultValue = "30") int days) {
        logger.info("接收到清理系统日志请求，清理 {} 天前的日志", days);
        
        try {
            int deletedCount = systemLogService.cleanLogs(days);
            logger.info("清理系统日志成功，共删除 {} 条记录", deletedCount);
            return ResponseEntity.ok(Collections.singletonMap("deletedCount", deletedCount));
        } catch (Exception e) {
            logger.error("清理系统日志时发生错误", e);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
} 