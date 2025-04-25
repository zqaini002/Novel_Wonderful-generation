package com.novelassistant.entity.visualization;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.novelassistant.entity.Novel;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 小说结构数据实体类
 * 用于存储小说结构分析的数据
 */
@Entity
@Table(name = "visualization_structure_data")
public class StructureData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonBackReference
    private Novel novel;
    
    @Column(name = "section_name", nullable = false)
    private String sectionName;
    
    @Column(nullable = false)
    private Double percentage;
    
    @Column(name = "start_chapter", nullable = false)
    private Integer startChapter;
    
    @Column(name = "end_chapter", nullable = false)
    private Integer endChapter;
    
    @Column(name = "chapter_count", nullable = false)
    private Integer chapterCount;
    
    @Column(columnDefinition = "LONGTEXT")
    @Lob
    private String description;
    
    @Column
    private String color;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
    
    // 构造函数
    public StructureData() {
    }
    
    public StructureData(Novel novel, String sectionName, Double percentage, Integer startChapter, Integer endChapter, Integer chapterCount) {
        this.novel = novel;
        this.sectionName = sectionName;
        this.percentage = percentage;
        this.startChapter = startChapter;
        this.endChapter = endChapter;
        this.chapterCount = chapterCount;
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
    
    public String getSectionName() {
        return sectionName;
    }
    
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
    
    public Double getPercentage() {
        return percentage;
    }
    
    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
    
    public Integer getStartChapter() {
        return startChapter;
    }
    
    public void setStartChapter(Integer startChapter) {
        this.startChapter = startChapter;
    }
    
    public Integer getEndChapter() {
        return endChapter;
    }
    
    public void setEndChapter(Integer endChapter) {
        this.endChapter = endChapter;
    }
    
    public Integer getChapterCount() {
        return chapterCount;
    }
    
    public void setChapterCount(Integer chapterCount) {
        this.chapterCount = chapterCount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
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