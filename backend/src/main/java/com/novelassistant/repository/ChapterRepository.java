package com.novelassistant.repository;

import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    
    /**
     * 根据小说按章节顺序查找所有章节
     * @param novel 小说实体
     * @return 章节列表
     */
    List<Chapter> findByNovelOrderByChapterNumberAsc(Novel novel);
} 