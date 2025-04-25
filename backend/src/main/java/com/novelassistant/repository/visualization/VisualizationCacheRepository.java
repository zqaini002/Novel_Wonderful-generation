package com.novelassistant.repository.visualization;

import com.novelassistant.entity.visualization.VisualizationCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface VisualizationCacheRepository extends JpaRepository<VisualizationCache, Long> {
    
    /**
     * 根据小说ID查找缓存
     * @param novelId 小说ID
     * @return 缓存列表
     */
    List<VisualizationCache> findByNovelId(Long novelId);
    
    /**
     * 根据小说ID和可视化类型查找缓存
     * @param novelId 小说ID
     * @param visualizationType 可视化类型
     * @return 匹配的缓存
     */
    VisualizationCache findByNovelIdAndVisualizationType(Long novelId, String visualizationType);
    
    /**
     * 根据小说ID、可视化类型和未过期查找缓存
     * @param novelId 小说ID
     * @param visualizationType 可视化类型
     * @param now 当前时间
     * @return 未过期的缓存
     */
    @Query("SELECT vc FROM VisualizationCache vc WHERE vc.novel.id = ?1 AND vc.visualizationType = ?2 AND (vc.expiresAt IS NULL OR vc.expiresAt > ?3)")
    VisualizationCache findValidCache(Long novelId, String visualizationType, Date now);
    
    /**
     * 删除小说的所有缓存
     * @param novelId 小说ID
     */
    void deleteByNovelId(Long novelId);
    
    /**
     * 删除所有过期缓存
     * @param now 当前时间
     * @return 删除的记录数
     */
    @Query("DELETE FROM VisualizationCache vc WHERE vc.expiresAt < ?1")
    int deleteExpiredCache(Date now);
} 