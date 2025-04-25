package com.novelassistant.entity.visualization;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Chapter;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 情节波动数据实体类
 * 用于存储小说情节波动图的数据
 */
@Entity
@Table(name = "visualization_emotional_data")
public class EmotionalData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonBackReference
    private Novel novel;
    
    @ManyToOne
    @JoinColumn(name = "chapter_id")
    @JsonBackReference
    private Chapter chapter;
    
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;
    
    @Column(name = "chapter_title")
    private String chapterTitle;
    
    @Column(name = "emotion_value", nullable = false)
    private Double emotionValue;
    
    @Column(name = "is_important")
    private Boolean isImportant = false;
    
    @Column(name = "is_climax_start")
    private Boolean isClimaxStart = false;
    
    @Column(name = "is_climax_end")
    private Boolean isClimaxEnd = false;
    
  
    @Column(columnDefinition = "LONGTEXT")
    @Lob
    private String eventDescription;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
    
    // 构造函数
    public EmotionalData() {
    }
    
    public EmotionalData(Novel novel, Chapter chapter, Integer chapterNumber, String chapterTitle, Double emotionValue) {
        this.novel = novel;
        this.chapter = chapter;
        this.chapterNumber = chapterNumber;
        this.chapterTitle = chapterTitle;
        this.emotionValue = emotionValue;
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
    
    public Chapter getChapter() {
        return chapter;
    }
    
    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }
    
    public Integer getChapterNumber() {
        return chapterNumber;
    }
    
    public void setChapterNumber(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
    }
    
    public String getChapterTitle() {
        return chapterTitle;
    }
    
    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }
    
    public Double getEmotionValue() {
        return emotionValue;
    }
    
    public void setEmotionValue(Double emotionValue) {
        this.emotionValue = emotionValue;
    }
    
    public Boolean getIsImportant() {
        return isImportant;
    }
    
    public void setIsImportant(Boolean isImportant) {
        this.isImportant = isImportant;
    }
    
    public Boolean getIsClimaxStart() {
        return isClimaxStart;
    }
    
    public void setIsClimaxStart(Boolean isClimaxStart) {
        this.isClimaxStart = isClimaxStart;
    }
    
    public Boolean getIsClimaxEnd() {
        return isClimaxEnd;
    }
    
    public void setIsClimaxEnd(Boolean isClimaxEnd) {
        this.isClimaxEnd = isClimaxEnd;
    }
    
    public String getEventDescription() {
        return eventDescription;
    }
    
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
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