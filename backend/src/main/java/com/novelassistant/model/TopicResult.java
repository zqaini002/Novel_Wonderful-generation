package com.novelassistant.model;

import java.util.List;

/**
 * 主题分析结果类
 * 用于存储文本主题分析的结果
 */
public class TopicResult {
    private int topicId;
    private List<String> keywords;
    private double weight;
    
    public TopicResult(int topicId, List<String> keywords, double weight) {
        this.topicId = topicId;
        this.keywords = keywords;
        this.weight = weight;
    }
    
    /**
     * 获取主题ID
     * 
     * @return 主题ID
     */
    public int getTopicId() {
        return topicId;
    }
    
    /**
     * 设置主题ID
     * 
     * @param topicId 主题ID
     */
    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }
    
    /**
     * 获取关键词列表
     * 
     * @return 与该主题相关的关键词列表
     */
    public List<String> getKeywords() {
        return keywords;
    }
    
    /**
     * 设置关键词列表
     * 
     * @param keywords 关键词列表
     */
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    
    /**
     * 获取主题权重
     * 
     * @return 主题权重
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     * 设置主题权重
     * 
     * @param weight 主题权重
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    @Override
    public String toString() {
        return "TopicResult{" +
                "topicId=" + topicId +
                ", keywords=" + keywords +
                ", weight=" + weight +
                '}';
    }
}