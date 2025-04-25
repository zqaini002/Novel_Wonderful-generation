package com.novelassistant.service.impl;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Role;
import com.novelassistant.entity.User;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.UserRepository;
import com.novelassistant.repository.SystemLogRepository;
import com.novelassistant.service.AdminService;
import com.novelassistant.service.NovelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.*;

import com.novelassistant.exception.NotFoundException;

/**
 * 后台管理服务实现类
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired(required = false)
    private SystemLogRepository systemLogRepository;
    
    @Autowired
    private NovelService novelService;
    
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
    public Map<String, Object> getDetailedSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 包含基础统计数据
        stats.putAll(getDashboardStats());
        
        try {
            // 统计活跃用户数(30天内登录过的用户)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date lastMonth = calendar.getTime();
            long activeUsers = userRepository.countByLastLoginAtAfter(lastMonth);
            stats.put("activeUsers", activeUsers);
            
            // 统计管理员用户数 - 使用枚举类型
            // 注意：修改这里使用Role.ERole枚举类型而不是字符串
            long adminUsers = userRepository.countByRoles_Name(Role.ERole.ROLE_ADMIN);
            stats.put("adminUsers", adminUsers);
            
            // 统计待处理小说数
            long pendingNovels = novelRepository.countByProcessingStatus(Novel.ProcessingStatus.PENDING);
            stats.put("pendingNovels", pendingNovels);
            
            // 统计处理失败的小说数
            long failedNovels = novelRepository.countByProcessingStatus(Novel.ProcessingStatus.FAILED);
            stats.put("failedNovels", failedNovels);
            
            // 统计今日上传的小说数
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            Date todayStart = today.getTime();
            long todayNovels = novelRepository.countByCreatedAtAfter(todayStart);
            stats.put("todayNovels", todayNovels);
            
            // 获取数据库大小
            String dbSize = getDatabaseSize();
            stats.put("dbSize", dbSize);
            
            // 获取文件存储大小
            String storageSize = getStorageSize();
            stats.put("storageSize", storageSize);
            
            // 获取系统运行时间
            long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
            long uptimeDays = uptimeMillis / (1000 * 60 * 60 * 24);
            long uptimeHours = (uptimeMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            long uptimeMinutes = (uptimeMillis % (1000 * 60 * 60)) / (1000 * 60);
            String uptime = String.format("%d 天 %d 小时 %d 分钟", uptimeDays, uptimeHours, uptimeMinutes);
            stats.put("uptime", uptime);
            
            // 获取最近错误数
            long recentErrors = 0;
            try {
                if (systemLogRepository != null) {
                    // 计算一个月内的错误日志
                    Calendar errorCalendar = Calendar.getInstance();
                    errorCalendar.add(Calendar.DAY_OF_MONTH, -30);
                    Date errorDate = errorCalendar.getTime();
                    recentErrors = systemLogRepository.countByLevelAndTimestampAfter("ERROR", errorDate);
                }
            } catch (Exception e) {
                System.err.println("获取错误日志统计失败: " + e.getMessage());
            }
            stats.put("recentErrors", recentErrors);
            
        } catch (Exception e) {
            // 发生异常时记录日志，但仍返回已获取的统计数据
            System.err.println("获取详细系统统计信息时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * 获取数据库大小(实现方法)
     */
    private String getDatabaseSize() {
        try {
            // 使用JDBC直接查询MySQL的information_schema获取数据库大小
            javax.sql.DataSource dataSource = null;
            try {
                // 尝试通过Spring上下文获取DataSource
                org.springframework.web.context.WebApplicationContext ctx = 
                    org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext();
                if (ctx != null) {
                    dataSource = ctx.getBean(javax.sql.DataSource.class);
                }
            } catch (Exception e) {
                System.err.println("无法获取DataSource: " + e.getMessage());
            }
            
            if (dataSource != null) {
                try (java.sql.Connection conn = dataSource.getConnection();
                    java.sql.Statement stmt = conn.createStatement();
                    java.sql.ResultSet rs = stmt.executeQuery(
                        "SELECT SUM(data_length + index_length) / 1024 as size_kb " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE()")) {
                    
                    if (rs.next()) {
                        double sizeKB = rs.getDouble("size_kb");
                        if (sizeKB > 1024 * 1024) {
                            return String.format("%.2f GB", sizeKB / (1024 * 1024));
                        } else if (sizeKB > 1024) {
                            return String.format("%.2f MB", sizeKB / 1024);
                        } else {
                            return String.format("%.2f KB", sizeKB);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("查询数据库大小失败: " + e.getMessage());
                }
            }
            
            // 如果无法获取实际大小，返回备用估算值
            long userCount = userRepository.count();
            long novelCount = novelRepository.count();
            long estimatedSize = userCount * 2 + novelCount * 5;
            estimatedSize = Math.max(estimatedSize, 100);
            
            if (estimatedSize > 1024) {
                return String.format("%.2f MB", estimatedSize / 1024.0);
            } else {
                return String.format("%d KB", estimatedSize);
            }
        } catch (Exception e) {
            return "100 KB"; // 默认值
        }
    }
    
    /**
     * 获取存储大小(实现方法)
     */
    private String getStorageSize() {
        try {
            // 定位项目根目录
            File rootDir = new File(".");
            
            // 计算目录大小
            long sizeInBytes = getFolderSize(rootDir);
            
            // 转换为可读格式
            if (sizeInBytes > 1024 * 1024 * 1024) {
                return String.format("%.2f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
            } else if (sizeInBytes > 1024 * 1024) {
                return String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
            } else {
                return String.format("%.2f KB", sizeInBytes / 1024.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "未知";
        }
    }
    
    /**
     * 递归计算文件夹大小
     */
    private long getFolderSize(File folder) {
        long size = 0;
        
        // 检查文件夹是否存在且是目录
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            
            // 确保列表不为空
            if (files != null) {
                for (File file : files) {
                    // 跳过隐藏文件、日志文件夹和target文件夹
                    if (file.isHidden() || file.getName().equals("logs") || 
                        file.getName().equals("target") || file.getName().equals("node_modules")) {
                        continue;
                    }
                    
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getFolderSize(file);
                    }
                }
            }
        } else if (folder.exists() && folder.isFile()) {
            size = folder.length();
        }
        
        return size;
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
    
    /**
     * 删除小说（管理员权限）
     * 管理员可以删除任何小说，不受用户权限限制
     */
    @Override
    @Transactional
    public void deleteNovel(Long id) {
        try {
            // 检查小说是否存在
            Optional<Novel> novelOpt = novelRepository.findById(id);
            if (!novelOpt.isPresent()) {
                throw new NotFoundException("小说不存在: " + id);
            }
            
            // 调用小说服务的软删除方法
            boolean result = novelService.deleteNovel(id);
            if (!result) {
                throw new RuntimeException("删除小说失败: 可能已被删除或发生其他错误");
            }
            
            logger.info("管理员成功删除小说: {}", id);
        } catch (Exception e) {
            logger.error("管理员删除小说失败: ", e);
            throw e;
        }
    }
    
    /**
     * 清理系统缓存
     * 实现多种缓存的清理，包括临时文件、Hibernate缓存等
     */
    @Override
    public void clearSystemCache() {
        // 创建一个结果列表记录清理的项目
        List<String> cleanedItems = new ArrayList<>();
        
        try {
            // 1. 清理JVM内存
            System.gc();
            cleanedItems.add("JVM内存");
            
            // 2. 清理临时文件目录
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            int tempFilesDeleted = cleanDirectory(tempDir, "novel_", true);
            if (tempFilesDeleted > 0) {
                cleanedItems.add("临时文件(" + tempFilesDeleted + "个)");
            }
            
            // 3. 清理上传目录中的临时文件
            File uploadsDir = new File("./uploads");
            if (uploadsDir.exists()) {
                int uploadFilesDeleted = cleanDirectory(uploadsDir, ".tmp", false);
                if (uploadFilesDeleted > 0) {
                    cleanedItems.add("上传临时文件(" + uploadFilesDeleted + "个)");
                }
            }
            
            // 4. 尝试清理Hibernate二级缓存
            try {
                // 获取EntityManagerFactory
                org.springframework.web.context.WebApplicationContext ctx = 
                    org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext();
                if (ctx != null) {
                    jakarta.persistence.EntityManagerFactory emf = ctx.getBean(jakarta.persistence.EntityManagerFactory.class);
                    if (emf != null && emf.getCache() != null) {
                        emf.getCache().evictAll();
                        cleanedItems.add("Hibernate缓存");
                    }
                }
            } catch (Exception e) {
                System.err.println("清理Hibernate缓存时发生异常: " + e.getMessage());
            }
            
            // 记录清理操作到日志
            if (systemLogRepository != null) {
                com.novelassistant.entity.SystemLog log = new com.novelassistant.entity.SystemLog();
                log.setLevel("INFO");
                log.setLogger("AdminService");
                log.setMessage("管理员手动清理系统缓存: " + String.join(", ", cleanedItems));
                log.setTimestamp(new Date());
                systemLogRepository.save(log);
            }
            
            System.out.println("系统缓存清理完成, 清理项目: " + String.join(", ", cleanedItems));
        } catch (Exception e) {
            System.err.println("清理系统缓存时发生异常: " + e.getMessage());
            e.printStackTrace();
            
            // 记录错误到日志
            if (systemLogRepository != null) {
                com.novelassistant.entity.SystemLog log = new com.novelassistant.entity.SystemLog();
                log.setLevel("ERROR");
                log.setLogger("AdminService");
                log.setMessage("清理系统缓存失败: " + e.getMessage());
                log.setTimestamp(new Date());
                systemLogRepository.save(log);
            }
            
            throw e; // 重新抛出异常以便控制器处理
        }
    }
    
    /**
     * 清理指定目录中满足条件的文件
     * @param directory 需要清理的目录
     * @param filePrefix 文件前缀/后缀，用于筛选要删除的文件
     * @param isPrefixMatch true表示前缀匹配，false表示后缀匹配
     * @return 删除的文件数量
     */
    private int cleanDirectory(File directory, String filePrefix, boolean isPrefixMatch) {
        int deletedCount = 0;
        
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        boolean shouldDelete = isPrefixMatch ? 
                                fileName.startsWith(filePrefix) : 
                                fileName.endsWith(filePrefix);
                        
                        // 添加时间条件：只删除超过1小时的文件
                        if (shouldDelete && (System.currentTimeMillis() - file.lastModified() > 60 * 60 * 1000)) {
                            if (file.delete()) {
                                deletedCount++;
                            }
                        }
                    }
                }
            }
        }
        
        return deletedCount;
    }
} 
 