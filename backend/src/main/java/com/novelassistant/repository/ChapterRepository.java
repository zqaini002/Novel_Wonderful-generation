package com.novelassistant.repository;

import com.novelassistant.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    
    /**
     * 根据小说ID按章节号升序查找所有章节
     * @param novelId 小说ID
     * @return 按章节号排序的章节列表
     */
    List<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId);
    
    /**
     * 根据小说ID按章节号查找所有章节（别名方法）
     * @param novelId 小说ID
     * @return 按章节号排序的章节列表
     */
    default List<Chapter> findByNovelIdOrderByChapterNumber(Long novelId) {
        return findByNovelIdOrderByChapterNumberAsc(novelId);
    }
    
    /**
     * 统计小说的章节数量
     * @param novelId 小说ID
     * @return 章节数量
     */
    long countByNovelId(Long novelId);
} 