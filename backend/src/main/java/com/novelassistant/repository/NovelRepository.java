package com.novelassistant.repository;

import com.novelassistant.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {
    // 可以添加自定义查询方法

    /**
     * 根据处理状态统计小说数量
     * @param status 处理状态
     * @return 小说数量
     */
    long countByProcessingStatus(Novel.ProcessingStatus status);
} 