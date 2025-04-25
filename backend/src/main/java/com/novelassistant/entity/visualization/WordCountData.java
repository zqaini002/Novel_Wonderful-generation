package com.novelassistant.entity.visualization;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.novelassistant.entity.Novel;
import jakarta.persistence.*;
import java.util.Date;

/**
 * 章节字数分布数据实体类
 * 用于存储小说章节字数分布数据
 */
@Entity
@Table(name = "visualization_word_count_data")
public class WordCountData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonBackReference
    private Novel novel;
    
    @Column(name = "range_start", nullable = false)
    private Integer rangeStart;
    
    @Column(name = "range_end", nullable = false)
    private Integer rangeEnd;
    
    @Column(name = "chapter_count", nullable = false)
    private Integer chapterCount;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
    
    // 构造函数
    public WordCountData() {
    }
    
    public WordCountData(Novel novel, Integer rangeStart, Integer rangeEnd, Integer chapterCount) {
        this.novel = novel;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
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
    
    public Integer getRangeStart() {
        return rangeStart;
    }
    
    public void setRangeStart(Integer rangeStart) {
        this.rangeStart = rangeStart;
    }
    
    public Integer getRangeEnd() {
        return rangeEnd;
    }
    
    public void setRangeEnd(Integer rangeEnd) {
        this.rangeEnd = rangeEnd;
    }
    
    public Integer getChapterCount() {
        return chapterCount;
    }
    
    public void setChapterCount(Integer chapterCount) {
        this.chapterCount = chapterCount;
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