package com.novelassistant.repository;

import com.novelassistant.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {
    // 可以添加自定义查询方法

    /**
     * 根据处理状态统计小说数量
     * @param status 处理状态
     * @return 小说数量
     */
    long countByProcessingStatus(Novel.ProcessingStatus status);
    
    /**
     * 统计指定用户上传的小说数量
     * @param userId 用户ID
     * @return 小说数量
     */
    int countByUserId(Long userId);
    
    /**
     * 根据用户ID查找小说列表
     * @param userId 用户ID
     * @return 该用户上传的小说列表
     */
    List<Novel> findByUserId(Long userId);
    
    /**
     * 统计指定日期之后创建的小说数量
     * @param date 日期
     * @return 小说数量
     */
    long countByCreatedAtAfter(Date date);
} 