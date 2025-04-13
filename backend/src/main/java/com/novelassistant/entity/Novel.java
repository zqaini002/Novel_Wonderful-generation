package com.novelassistant.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "novels")
public class Novel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column
    private String author;
    
    @Column
    private String description;
    
    @Column(name = "processing_status")
    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus = ProcessingStatus.PROCESSING;
    
    @Column(name = "processed_chapters")
    private Integer processedChapters = 0;
    
    @Column(name = "total_chapters")
    private Integer totalChapters = 0;
    
    @Column(name = "overall_summary")
    @Lob
    private String overallSummary;
    
    @Column(name = "world_building_summary")
    @Lob
    private String worldBuildingSummary;
    
    @Column(name = "character_development_summary")
    @Lob
    private String characterDevelopmentSummary;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
    
    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Chapter> chapters;
    
    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Tag> tags;
    
    // 处理状态枚举
    public enum ProcessingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
    
    // Constructors
    public Novel() {
    }
    
    public Novel(String title, String author) {
        this.title = title;
        this.author = author;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }
    
    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }
    
    public Integer getProcessedChapters() {
        return processedChapters;
    }
    
    public void setProcessedChapters(Integer processedChapters) {
        this.processedChapters = processedChapters;
    }
    
    public Integer getTotalChapters() {
        return totalChapters;
    }
    
    public void setTotalChapters(Integer totalChapters) {
        this.totalChapters = totalChapters;
    }
    
    public String getOverallSummary() {
        return overallSummary;
    }
    
    public void setOverallSummary(String overallSummary) {
        this.overallSummary = overallSummary;
    }
    
    public String getWorldBuildingSummary() {
        return worldBuildingSummary;
    }
    
    public void setWorldBuildingSummary(String worldBuildingSummary) {
        this.worldBuildingSummary = worldBuildingSummary;
    }
    
    public String getCharacterDevelopmentSummary() {
        return characterDevelopmentSummary;
    }
    
    public void setCharacterDevelopmentSummary(String characterDevelopmentSummary) {
        this.characterDevelopmentSummary = characterDevelopmentSummary;
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
    
    public List<Chapter> getChapters() {
        return chapters;
    }
    
    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
    
    public List<Tag> getTags() {
        return tags;
    }
    
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
} 