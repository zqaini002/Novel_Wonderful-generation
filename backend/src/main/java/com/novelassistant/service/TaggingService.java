package com.novelassistant.service;

/**
 * 标签生成服务
 * 用于生成小说标签
 */
public interface TaggingService {
    
    /**
     * 为小说生成标签
     * 基于小说内容、关键词、情感分析等生成各类标签
     * 
     * @param novelId 小说ID
     * @return 生成的标签数量
     */
    int generateTagsForNovel(Long novelId);
} 