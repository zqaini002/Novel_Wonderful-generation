package com.novelassistant.repository;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * 根据小说查找所有标签
     * @param novel 小说实体
     * @return 标签列表
     */
    List<Tag> findByNovel(Novel novel);
} 