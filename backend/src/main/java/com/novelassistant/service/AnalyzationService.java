package com.novelassistant.service;

import java.util.List;
import java.util.Map;

/**
 * 小说分析服务
 * 用于分析小说内容并生成摘要、标签等
 */
public interface AnalyzationService {
    
    /**
     * 分析小说内容
     * 包括生成摘要、提取关键词、分析情感等
     * 
     * @param novelId 小说ID
     * @return 分析结果的映射
     */
    Map<String, Object> analyzeNovel(Long novelId);
    
    /**
     * 分析章节内容
     * 
     * @param chapterId 章节ID
     * @return 分析结果的映射
     */
    Map<String, Object> analyzeChapter(Long chapterId);
    
    /**
     * 比较多个章节
     * 
     * @param chapterIds 章节ID列表
     * @return 比较结果的映射
     */
    Map<String, Object> compareChapters(List<Long> chapterIds);
    
    /**
     * 分析小说中的角色
     * 
     * @param novelId 小说ID
     * @param characterName 角色名称
     * @return 分析结果的映射
     */
    Map<String, Object> analyzeCharacter(Long novelId, String characterName);
} 