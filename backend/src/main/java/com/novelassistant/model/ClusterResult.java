package com.novelassistant.model;

/**
 * 聚类结果类
 * 用于存储文本聚类的结果
 */
public class ClusterResult {
    private String text;
    private int clusterId;
    
    public ClusterResult(String text, int clusterId) {
        this.text = text;
        this.clusterId = clusterId;
    }
    
    /**
     * 获取文本内容
     * 
     * @return 原始文本内容
     */
    public String getText() {
        return text;
    }
    
    /**
     * 设置文本内容
     * 
     * @param text 文本内容
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * 获取聚类ID
     * 
     * @return 聚类ID
     */
    public int getClusterId() {
        return clusterId;
    }
    
    /**
     * 设置聚类ID
     * 
     * @param clusterId 聚类ID
     */
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }
    
    @Override
    public String toString() {
        return "ClusterResult{" +
                "text='" + text + '\'' +
                ", clusterId=" + clusterId +
                '}';
    }
} 