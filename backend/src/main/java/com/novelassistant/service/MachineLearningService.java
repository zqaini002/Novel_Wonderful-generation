package com.novelassistant.service;

import java.util.List;
import java.util.Map;

/**
 * 提供机器学习相关功能的服务接口
 * 用于增强NLP服务的能力，提供更深层次的文本分析
 */
public interface MachineLearningService {
    
    /**
     * 使用词向量模型计算文本相似度
     * 
     * @param text1 第一段文本
     * @param text2 第二段文本
     * @return 相似度得分 (0-1之间)
     */
    double calculateTextSimilarity(String text1, String text2);
    
    /**
     * 对文本进行聚类分析
     * 
     * @param textList 文本片段列表
     * @param clusterCount 希望分的簇数
     * @return 聚类结果，键为聚类ID，值为该类中的文本索引列表
     */
    Map<Integer, List<Integer>> clusterTexts(List<String> textList, int clusterCount);
    
    /**
     * 基于深度学习的情感分析
     * 提供比传统NLP更准确的情感评分
     * 
     * @param text 需要分析的文本
     * @return 情感得分（0-1之间，0为极度负面，1为极度正面）
     */
    double deepSentimentAnalysis(String text);
    
    /**
     * 预测下一个情节发展方向
     * 基于前文内容预测可能的后续发展
     * 
     * @param previousChapters 之前章节的内容
     * @return 可能的情节发展方向及其可能性
     */
    Map<String, Double> predictPlotDevelopment(List<String> previousChapters);
    
    /**
     * 使用主题模型(LDA)提取文档主题
     * 
     * @param text 文本内容
     * @param topicCount 希望提取的主题数量
     * @return 主题ID到关键词列表的映射
     */
    Map<Integer, List<String>> extractTopicsWithLDA(String text, int topicCount);
    
    /**
     * 使用深度学习模型检测文本风格特征
     * 
     * @param text 需要分析的文本
     * @return 各种风格特征及其强度的映射
     */
    Map<String, Double> detectWritingStyle(String text);
    
    /**
     * 使用命名实体识别+关系抽取进行角色关系网络构建
     * 
     * @param text 文本内容
     * @return 角色关系网络，包含实体及其关系
     */
    Map<String, List<Map<String, Object>>> buildCharacterNetwork(String text);
    
    /**
     * 对文本进行多类别分类
     * 
     * @param text 需要分类的文本
     * @param categories 类别列表
     * @return 各类别及其概率的映射
     */
    Map<String, Double> classifyText(String text, List<String> categories);
    
    /**
     * 文本复杂度分析
     * 
     * @param text 需要分析的文本
     * @return 文本的各种复杂度指标及其值
     */
    Map<String, Double> analyzeTextComplexity(String text);
    
    /**
     * 词向量模型训练
     * 根据小说内容训练自定义的词向量模型
     * 
     * @param corpus 语料库
     * @param vectorSize 向量维度
     * @return 是否训练成功
     */
    boolean trainWordVectorModel(List<String> corpus, int vectorSize);
    
    /**
     * 计算文本之间的语义关联度
     * 
     * @param mainText 主文本
     * @param referenceTexts 参考文本列表
     * @return 每个参考文本与主文本的语义关联度
     */
    List<Double> calculateSemanticRelatedness(String mainText, List<String> referenceTexts);
    
    /**
     * 分析小说结构并识别不同的结构部分
     * 
     * @param chapters 小说章节内容列表
     * @return 结构分析结果，包含各结构部分(如开端、发展、高潮、结局等)的名称、起始章节、结束章节和相应百分比
     */
    Map<String, List<Map<String, Object>>> analyzeNovelStructure(List<String> chapters);
} 