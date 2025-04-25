package com.novelassistant.entity;

import jakarta.persistence.*;
import java.util.Date;

/**
 * 角色对话实体类
 */
@Entity
@Table(name = "character_dialogues")
public class CharacterDialogue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "character_id", nullable = false)
    private Long characterId;
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(name = "content", columnDefinition = "LONGTEXT", nullable = false)
    @Lob
    private String content;
    
    @Column(name = "emotion", length = 50)
    private String emotion;
    
    @Column(name = "importance")
    private Integer importance = 1;
    
    @Column(name = "context", columnDefinition = "LONGTEXT")
    @Lob
    private String context;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    // 关联到小说
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", insertable = false, updatable = false)
    private Novel novel;
    
    // 关联到角色
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", insertable = false, updatable = false)
    private NovelCharacter character;
    
    // 关联到章节
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", insertable = false, updatable = false)
    private Chapter chapter;
    
    // 构造函数
    public CharacterDialogue() {
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
    
    public Long getCharacterId() {
        return characterId;
    }
    
    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }
    
    public Long getChapterId() {
        return chapterId;
    }
    
    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getEmotion() {
        return emotion;
    }
    
    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }
    
    public Integer getImportance() {
        return importance;
    }
    
    public void setImportance(Integer importance) {
        this.importance = importance;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
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
    
    public NovelCharacter getCharacter() {
        return character;
    }
    
    public void setCharacter(NovelCharacter character) {
        this.character = character;
    }
    
    public Chapter getChapter() {
        return chapter;
    }
    
    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
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