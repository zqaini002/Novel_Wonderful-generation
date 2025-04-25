package com.novelassistant.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "chapters")
public class Chapter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonBackReference
    private Novel novel;
    
    @Column(name = "novel_id", insertable = false, updatable = false)
    private Long novelId;
    
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "LONGTEXT")
    @Lob
    private String content;
    
    @Column(name = "word_count")
    private Integer wordCount;
    
    @Column(columnDefinition = "MEDIUMTEXT")
    private String summary;
    
    private List<String> keywords = new ArrayList<>();
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
    
    // Constructors
    public Chapter() {
    }
    
    public Chapter(Novel novel, Integer chapterNumber, String title) {
        this.novel = novel;
        this.novelId = novel.getId();
        this.chapterNumber = chapterNumber;
        this.title = title;
    }
    
    // Getters and Setters
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
    
    public Integer getChapterNumber() {
        return chapterNumber;
    }
    
    public void setChapterNumber(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
        // 自动计算字数
        if (content != null) {
            // 中文每个字符算一个字，英文每个单词算一个词
            this.wordCount = content.length();
        }
    }
    
    public Integer getWordCount() {
        return wordCount;
    }
    
    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public List<String> getKeywords() {
        return keywords;
    }
    
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
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
