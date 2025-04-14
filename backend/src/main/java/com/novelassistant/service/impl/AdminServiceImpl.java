package com.novelassistant.service.impl;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.User;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.UserRepository;
import com.novelassistant.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

/**
 * 后台管理服务实现类
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计用户总数
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // 统计小说总数
        long totalNovels = novelRepository.count();
        stats.put("totalNovels", totalNovels);
        
        // 统计处理中的小说数量
        long processingNovels = novelRepository.countByProcessingStatus(Novel.ProcessingStatus.PROCESSING);
        stats.put("processingNovels", processingNovels);
        
        // 统计已完成小说数量
        long completedNovels = novelRepository.countByProcessingStatus(Novel.ProcessingStatus.COMPLETED);
        stats.put("completedNovels", completedNovels);
        
        // 最近一周注册用户数
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date lastWeek = calendar.getTime();
        long newUsers = userRepository.countByCreatedAtAfter(lastWeek);
        stats.put("newUsers", newUsers);
        
        return stats;
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by("id").ascending());
    }
    
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }
    
    @Override
    @Transactional
    public User updateUserStatus(Long id, boolean enabled) {
        User user = getUserById(id);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
    
    @Override
    @Transactional
    public void deleteNovel(Long id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("小说不存在: " + id));
        
        // 删除相关章节和标签
        novelRepository.delete(novel);
    }
    
    @Override
    public Map<String, Object> getSystemLogs(int page, int size) {
        // 这里是模拟实现，实际项目中应该从日志文件或数据库中读取日志
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> logs = new ArrayList<>();
        
        // 模拟添加10条日志数据
        for (int i = page * size; i < (page * size) + size && i < 100; i++) {
            Map<String, Object> log = new HashMap<>();
            log.put("id", i + 1);
            log.put("timestamp", new Date(System.currentTimeMillis() - (i * 3600000)));
            log.put("level", i % 5 == 0 ? "ERROR" : (i % 3 == 0 ? "WARN" : "INFO"));
            log.put("message", "系统日志消息样例 #" + (i + 1));
            log.put("source", "AdminService");
            logs.add(log);
        }
        
        result.put("logs", logs);
        result.put("total", 100); // 假设总共有100条日志
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }
    
    @Override
    public void clearSystemCache() {
        // 实际项目中应该清理应用缓存
        // 这里只是模拟实现
        System.gc(); // 请求JVM执行垃圾回收，仅作为示例
    }
} 
 