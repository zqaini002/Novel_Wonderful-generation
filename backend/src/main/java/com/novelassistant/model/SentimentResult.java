package com.novelassistant.model;

/**
 * 情感分析结果类
 * 用于存储文本情感分析的结果
 */
public class SentimentResult {
    private double score;
    private String sentiment;
    
    public SentimentResult(double score, String sentiment) {
        this.score = score;
        this.sentiment = sentiment;
    }
    
    /**
     * 获取情感分数
     * 
     * @return 情感分数（0-1之间，0为极度负面，1为极度正面）
     */
    public double getScore() {
        return score;
    }
    
    /**
     * 设置情感分数
     * 
     * @param score 情感分数
     */
    public void setScore(double score) {
        this.score = score;
    }
    
    /**
     * 获取情感类别
     * 
     * @return 情感类别（积极、消极、中性等）
     */
    public String getSentiment() {
        return sentiment;
    }
    
    /**
     * 设置情感类别
     * 
     * @param sentiment 情感类别
     */
    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
    
    @Override
    public String toString() {
        return "SentimentResult{" +
                "score=" + score +
                ", sentiment='" + sentiment + '\'' +
                '}';
    }
} 