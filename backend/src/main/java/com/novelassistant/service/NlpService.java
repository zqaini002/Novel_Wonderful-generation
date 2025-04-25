package com.novelassistant.service;

import java.util.List;
import java.util.Map;

/**
 * 提供自然语言处理功能的服务接口
 */
public interface NlpService {
    
    /**
     * 生成文本摘要
     * 
     * @param text 需要生成摘要的文本
     * @param maxLength 摘要的最大长度
     * @return 生成的摘要
     */
    String generateSummary(String text, int maxLength);
    
    /**
     * 从文本中提取关键词
     * 
     * @param text 需要提取关键词的文本
     * @param maxKeywords 最大关键词数量
     * @return 关键词及其权重的映射
     */
    Map<String, Integer> extractKeywords(String text, int maxKeywords);
    
    /**
     * 检测文本中的章节分隔位置
     * 
     * @param text 完整文本
     * @return 章节开始位置的索引列表
     */
    List<Integer> detectChapterBreaks(String text);
    
    /**
     * 从文本中提取主题
     * 
     * @param text 需要分析的文本
     * @param topicCount 主题数量
     * @return 主题及其权重的映射
     */
    Map<String, Double> extractTopics(String text, int topicCount);
    
    /**
     * 从文本中提取人物角色
     * 
     * @param text 需要分析的文本
     * @return 识别出的人物角色列表
     */
    List<String> extractCharacters(String text);
    
    /**
     * 分析文本情感
     * 
     * @param text 需要分析的文本
     * @return 正面情感的概率（0-1之间的值）
     */
    double analyzeSentiment(String text);
    
    /**
     * 从文本中提取对话，用于人物关系识别
     * 
     * @param text 需要分析的文本
     * @return 对话列表，每项包含说话者和内容
     */
    List<Map<String, String>> extractDialogues(String text);
    
    /**
     * 分析角色之间的关系
     * 
     * @param dialogues 从文本中提取的对话列表
     * @return 关系列表，每项包含character1, character2, relationship, confidence等字段
     */
    List<Map<String, Object>> analyzeCharacterRelationships(List<Map<String, String>> dialogues);
    
    /**
     * 从文本中提取命名实体
     * 
     * @param text 需要分析的文本
     * @param entityType 实体类型，如"PERSON", "LOCATION", "ORGANIZATION"等
     * @return 提取的命名实体列表
     */
    List<String> extractNamedEntities(String text, String entityType);
    
    /**
     * 生成世界观摘要
     * 
     * @param text 需要分析的文本
     * @return 生成的世界观摘要
     */
    String generateWorldBuildingSummary(String text);
    
    /**
     * 生成情节发展摘要
     * 
     * @param text 需要分析的文本
     * @return 生成的情节发展摘要
     */
    String generatePlotProgressionSummary(String text);
} 