package com.novelassistant.repository;

import com.novelassistant.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * 系统日志的JPA Repository
 */
@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    
    /**
     * 根据日志级别和时间范围查询日志
     */
    Page<SystemLog> findByLevelAndTimestampBetween(String level, Date startDate, Date endDate, Pageable pageable);
    
    /**
     * 根据时间范围查询日志
     */
    Page<SystemLog> findByTimestampBetween(Date startDate, Date endDate, Pageable pageable);
    
    /**
     * 根据日志级别查询日志
     */
    Page<SystemLog> findByLevel(String level, Pageable pageable);
    
    /**
     * 高级查询：根据多个条件组合查询
     */
    @Query("SELECT s FROM SystemLog s WHERE " +
           "(:level IS NULL OR s.level = :level) AND " +
           "(:startDate IS NULL OR s.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR s.timestamp <= :endDate) AND " +
           "(:query IS NULL OR :query = '' OR s.message LIKE CONCAT('%', :query, '%') OR s.logger LIKE CONCAT('%', :query, '%')) AND " +
           "(:userId IS NULL OR s.userId = :userId)")
    Page<SystemLog> findLogs(
            @Param("level") String level,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("query") String query,
            @Param("userId") String userId,
            Pageable pageable);
    
    /**
     * 删除指定日期之前的日志
     */
    int deleteByTimestampBefore(Date date);
} 