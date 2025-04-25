package com.novelassistant.entity;

import jakarta.persistence.*;
import java.util.Date;

/**
 * 小说角色实体类
 */
@Entity
@Table(name = "novel_characters")
public class NovelCharacter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "importance")
    private Integer importance = 50;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(columnDefinition = "LONGTEXT")
    @Lob
    private String description;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    // 关联到小说
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", insertable = false, updatable = false)
    private Novel novel;
    
    // 构造函数
    public NovelCharacter() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getNovelId() {
        return novelId;
    }
    
    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getImportance() {
        return importance;
    }
    
    public void setImportance(Integer importance) {
        this.importance = importance;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public Novel getNovel() {
        return novel;
    }
    
    public void setNovel(Novel novel) {
        this.novel = novel;
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
} 