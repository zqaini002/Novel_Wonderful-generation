package com.novelassistant.entity.visualization;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.novelassistant.entity.Novel;
import jakarta.persistence.*;
import java.util.Date;
import java.util.Calendar;

/**
 * 可视化缓存实体类
 * 用于存储生成的可视化数据的缓存
 */
@Entity
@Table(name = "visualization_cache")
public class VisualizationCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonBackReference
    private Novel novel;
    
    @Column(name = "visualization_type", nullable = false)
    private String visualizationType;
    
    @Column(name = "data_json", nullable = false, columnDefinition = "JSON")
    private String dataJson;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
    
    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;
    
    // 构造函数
    public VisualizationCache() {
    }
    
    public VisualizationCache(Novel novel, String visualizationType, String dataJson) {
        this.novel = novel;
        this.visualizationType = visualizationType;
        this.dataJson = dataJson;
        
        // 默认缓存一周后过期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 7);
        this.expiresAt = calendar.getTime();
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
    
    public String getVisualizationType() {
        return visualizationType;
    }
    
    public void setVisualizationType(String visualizationType) {
        this.visualizationType = visualizationType;
    }
    
    public String getDataJson() {
        return dataJson;
    }
    
    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
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
    
    public Date getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
} 