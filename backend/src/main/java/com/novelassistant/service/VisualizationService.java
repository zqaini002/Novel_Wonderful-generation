package com.novelassistant.service;

import java.util.List;
import java.util.Map;

/**
 * 小说可视化服务接口
 * 提供各类数据可视化所需数据的服务
 */
public interface VisualizationService {
    
    /**
     * 获取小说关键词云数据
     * 
     * @param novelId 小说ID
     * @return 关键词云数据列表
     */
    List<Map<String, Object>> getKeywordCloudData(Long novelId);
    
    /**
     * 获取小说情节波动图数据
     * 
     * @param novelId 小说ID
     * @return 情节波动图数据
     */
    Map<String, Object> getEmotionalFluctuationData(Long novelId);
    
    /**
     * 获取小说结构分析数据
     * 
     * @param novelId 小说ID
     * @return 结构分析数据
     */
    Map<String, Object> getStructureAnalysisData(Long novelId);
    
    /**
     * 获取小说人物关系网络数据
     * 
     * @param novelId 小说ID
     * @return 人物关系网络数据
     */
    Map<String, Object> getCharacterRelationshipData(Long novelId);
    
    /**
     * 获取小说场景分布数据
     * 
     * @param novelId 小说ID
     * @return 场景分布数据
     */
    Map<String, Object> getSceneDistributionData(Long novelId);
    
    /**
     * 获取小说综合统计数据
     * 
     * @param novelId 小说ID
     * @return 综合统计数据
     */
    Map<String, Object> getNovelStatisticsData(Long novelId);
} 