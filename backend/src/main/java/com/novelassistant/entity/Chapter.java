package com.novelassistant.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

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
    
    @Column(name = "chapter_number")
    private Integer chapterNumber;
    
    @Column(nullable = false)
    private String title;
    
    @Column
    @Lob
    private String content;
    
    @Column
    @Lob
    private String summary;
    
    @ElementCollection
    @CollectionTable(name = "chapter_keywords", 
                   joinColumns = @JoinColumn(name = "chapter_id"))
    @Column(name = "keyword")
    private List<String> keywords;
    
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