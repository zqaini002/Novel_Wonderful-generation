package com.novelassistant.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novelassistant.entity.SystemLog;
import com.novelassistant.repository.SystemLogRepository;
import com.novelassistant.service.SystemLogService;
import com.novelassistant.util.LogUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 系统日志服务实现
 */
@Service
public class SystemLogServiceImpl implements SystemLogService {

    private static final Logger logger = LogUtil.getLogger(SystemLogServiceImpl.class);
    
    @Autowired
    private SystemLogRepository systemLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getSystemLogs(int page, int size, String level,
                                             Date startDate, Date endDate,
                                             String query, String userId) {
        try {
            // 添加调试日志
            logger.debug("获取系统日志参数: page={}, size={}, level={}, startDate={}, endDate={}, query={}, userId={}",
                    page, size, level, startDate, endDate, query, userId);
                    
            // 创建排序和分页对象
            Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 打印SQL查询参数
            logger.debug("执行查询: level={}, startDate={}, endDate={}, query={}, userId={}",
                    level, startDate, endDate, query, userId);
            
            // 使用JPA条件查询
            Page<SystemLog> logsPage = systemLogRepository.findLogs(
                    level, startDate, endDate, query, userId, pageable);
            
            // 打印结果
            logger.debug("查询结果: 总条数={}, 当前页记录数={}", 
                    logsPage.getTotalElements(), logsPage.getNumberOfElements());
            
            // 如果有记录，打印记录ID和level
            if (logsPage.getNumberOfElements() > 0) {
                StringBuilder sb = new StringBuilder("查询到的日志记录: ");
                for (SystemLog log : logsPage.getContent()) {
                    sb.append("[id=").append(log.getId())
                      .append(", level=").append(log.getLevel())
                      .append(", timestamp=").append(log.getTimestamp())
                      .append("], ");
                }
                logger.debug(sb.toString());
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("logs", logsPage.getContent());
            result.put("total", logsPage.getTotalElements());
            result.put("page", page);
            result.put("size", size);
            
            return result;
        } catch (Exception e) {
            logger.error("获取系统日志时发生错误", e);
            
            // 如果发生错误，返回一个空结果，避免前端崩溃
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("logs", new ArrayList<>());
            emptyResult.put("total", 0);
            emptyResult.put("page", page);
            emptyResult.put("size", size);
            emptyResult.put("error", e.getMessage());
            
            return emptyResult;
        }
    }

    @Override
    public void addSystemLog(String level, String logger, String message,
                            String userId, String ip, String thread,
                            String stackTrace, Map<String, Object> params) {
        try {
            // 创建日志实体
            SystemLog log = new SystemLog(level, logger, message);
            log.setUserId(userId);
            log.setIp(ip);
            log.setThread(thread);
            log.setStackTrace(stackTrace);
            
            // 将参数转换为JSON字符串
            if (params != null && !params.isEmpty()) {
                try {
                    log.setParamsJson(objectMapper.writeValueAsString(params));
                } catch (Exception e) {
                    // 如果JSON转换失败，记录错误但不阻断主要功能
                    this.logger.warn("转换日志参数为JSON时出错: " + e.getMessage());
                }
            }
            
            // 保存到数据库
            systemLogRepository.save(log);
        } catch (Exception e) {
            // 记录日志保存失败，但不抛出异常，避免影响主要业务流程
            this.logger.error("保存系统日志时发生错误", e);
        }
    }

    @Override
    @Transactional
    public int cleanLogs(int days) {
        try {
            // 计算days天前的日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            Date thresholdDate = calendar.getTime();
            
            // 删除早于thresholdDate的日志
            int deletedCount = systemLogRepository.deleteByTimestampBefore(thresholdDate);
            
            this.logger.info("成功清理 {} 天前的日志, 共删除 {} 条记录", days, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            this.logger.error("清理系统日志时发生错误", e);
            return 0;
        }
    }
} 