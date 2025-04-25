package com.novelassistant.repository.visualization;

import com.novelassistant.entity.visualization.VisualizationKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisualizationKeywordRepository extends JpaRepository<VisualizationKeyword, Long> {
    
    /**
     * 根据小说ID查找关键词
     * @param novelId 小说ID
     * @return 关键词列表
     */
    List<VisualizationKeyword> findByNovelId(Long novelId);
    
    /**
     * 根据小说ID查找按权重排序的关键词（降序）
     * @param novelId 小说ID
     * @return 按权重排序的关键词列表
     */
    List<VisualizationKeyword> findByNovelIdOrderByWeightDesc(Long novelId);
    
    /**
     * 根据小说ID查找前N个关键词（按权重排序）
     * @param novelId 小说ID
     * @param limit 限制数量
     * @return 前N个关键词
     */
    @Query(value = "SELECT * FROM visualization_keywords WHERE novel_id = ?1 ORDER BY weight DESC LIMIT ?2", nativeQuery = true)
    List<VisualizationKeyword> findTopKeywordsByNovelId(Long novelId, int limit);
    
    /**
     * 根据小说ID和关键词查找
     * @param novelId 小说ID
     * @param keyword, 关键词
     * @return 匹配的关键词实体
     */
    VisualizationKeyword findByNovelIdAndKeyword(Long novelId, String keyword);
    
    /**
     * 删除小说的所有关键词
     * @param novelId 小说ID
     */
    void deleteByNovelId(Long novelId);
} 