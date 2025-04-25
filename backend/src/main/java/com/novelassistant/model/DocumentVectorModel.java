package com.novelassistant.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档向量模型类
 * 用于表示和处理文本的向量表示
 */
public class DocumentVectorModel {
    
    private Map<String, Double> vocabulary;
    private int vectorSize;
    
    public DocumentVectorModel() {
        this.vocabulary = new HashMap<>();
        this.vectorSize = 100; // 默认向量大小
    }
    
    /**
     * 计算两个文档的余弦相似度
     * 
     * @param doc1 第一个文档
     * @param doc2 第二个文档
     * @return 相似度分数，范围在 0-1 之间
     */
    public double calculateCosineSimilarity(String doc1, String doc2) {
        Map<String, Double> vector1 = createDocumentVector(doc1);
        Map<String, Double> vector2 = createDocumentVector(doc2);
        
        // 计算点积
        double dotProduct = 0.0;
        for (String term : vector1.keySet()) {
            if (vector2.containsKey(term)) {
                dotProduct += vector1.get(term) * vector2.get(term);
            }
        }
        
        // 计算向量范数
        double norm1 = 0.0;
        for (double val : vector1.values()) {
            norm1 += val * val;
        }
        norm1 = Math.sqrt(norm1);
        
        double norm2 = 0.0;
        for (double val : vector2.values()) {
            norm2 += val * val;
        }
        norm2 = Math.sqrt(norm2);
        
        // 计算余弦相似度
        if (norm1 > 0 && norm2 > 0) {
            return dotProduct / (norm1 * norm2);
        } else {
            return 0.0;
        }
    }
    
    /**
     * 为文档创建向量表示
     * 
     * @param document 输入文档
     * @return 文档的向量表示
     */
    public Map<String, Double> createDocumentVector(String document) {
        Map<String, Double> vector = new HashMap<>();
        String[] words = document.split("\\s+");
        
        // 统计词频
        Map<String, Double> termFrequency = new HashMap<>();
        for (String word : words) {
            if (vocabulary.containsKey(word)) {
                termFrequency.put(word, termFrequency.getOrDefault(word, 0.0) + 1.0);
            }
        }
        
        // 计算 TF-IDF
        for (String term : termFrequency.keySet()) {
            double tf = termFrequency.get(term) / words.length;
            double idf = Math.log(vocabulary.size() / vocabulary.get(term));
            vector.put(term, tf * idf);
        }
        
        return vector;
    }
    
    public Map<String, Double> getVocabulary() {
        return vocabulary;
    }
    
    public void setVocabulary(Map<String, Double> vocabulary) {
        this.vocabulary = vocabulary;
    }
    
    public int getVectorSize() {
        return vectorSize;
    }
    
    public void setVectorSize(int vectorSize) {
        this.vectorSize = vectorSize;
    }
} 