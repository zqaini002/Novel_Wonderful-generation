package com.novelassistant.repository;

import com.novelassistant.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {
    // 可以添加自定义查询方法
} 