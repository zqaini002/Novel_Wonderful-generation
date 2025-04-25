package com.novelassistant.repository.visualization;

import com.novelassistant.entity.visualization.EmotionalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionalDataRepository extends JpaRepository<EmotionalData, Long> {
    
    /**
     * 根据小说ID查找情节波动数据
     * @param novelId 小说ID
     * @return 情节波动数据列表
     */
    List<EmotionalData> findByNovelId(Long novelId);
    
    /**
     * 根据小说ID查找情节波动数据，按章节号排序
     * @param novelId 小说ID
     * @return 按章节号排序的情节波动数据列表
     */
    List<EmotionalData> findByNovelIdOrderByChapterNumberAsc(Long novelId);
    
    /**
     * 根据小说ID和章节号查找情节波动数据
     * @param novelId 小说ID
     * @param chapterNumber 章节号
     * @return 匹配的情节波动数据
     */
    EmotionalData findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);
    
    /**
     * 根据小说ID查找重要章节的情节波动数据
     * @param novelId 小说ID
     * @return 重要章节的情节波动数据列表
     */
    List<EmotionalData> findByNovelIdAndIsImportantTrue(Long novelId);
    
    /**
     * 根据小说ID查找高潮开始章节
     * @param novelId 小说ID
     * @return 高潮开始章节的情节波动数据列表
     */
    List<EmotionalData> findByNovelIdAndIsClimaxStartTrue(Long novelId);
    
    /**
     * 删除小说的所有情节波动数据
     * @param novelId 小说ID
     */
    void deleteByNovelId(Long novelId);
} 