package com.novelassistant.entity.visualization;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.novelassistant.entity.Novel;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 可视化关键词实体类
 * 用于存储小说关键词及其权重数据
 */
@Entity
@Table(name = "visualization_keywords")
public class VisualizationKeyword {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonBackReference
    private Novel novel;
    
    @Column(nullable = false)
    private String keyword;
    
    @Column(nullable = false)
    private Integer weight = 1;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
    
    // 构造函数
    public VisualizationKeyword() {
    }
    
    public VisualizationKeyword(Novel novel, String keyword, Integer weight) {
        this.novel = novel;
        this.keyword = keyword;
        this.weight = weight;
    }
    
    // Getters 和 Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Novel getNovel() {
        return novel;
    }
    
    public void setNovel(Novel novel) {
        this.novel = novel;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public Integer getWeight() {
        return weight;
    }
    
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
} 