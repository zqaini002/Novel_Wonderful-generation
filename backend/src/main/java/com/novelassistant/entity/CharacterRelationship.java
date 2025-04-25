package com.novelassistant.entity;

import jakarta.persistence.*;
import java.util.Date;

/**
 * 角色关系实体类
 */
@Entity
@Table(name = "character_relationships")
public class CharacterRelationship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false)
    private Long novelId;
    
    @Column(name = "source_character_id", nullable = false)
    private Long sourceCharacterId;
    
    @Column(name = "target_character_id", nullable = false)
    private Long targetCharacterId;
    
    @Column(name = "relationship_type", length = 50)
    private String relationshipType;
    
    @Column(name = "importance")
    private Integer importance = 1;
    
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
    
    // 关联到源角色
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_character_id", insertable = false, updatable = false)
    private NovelCharacter sourceCharacter;
    
    // 关联到目标角色
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_character_id", insertable = false, updatable = false)
    private NovelCharacter targetCharacter;
    
    // 构造函数
    public CharacterRelationship() {
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
    
    public Long getSourceCharacterId() {
        return sourceCharacterId;
    }
    
    public void setSourceCharacterId(Long sourceCharacterId) {
        this.sourceCharacterId = sourceCharacterId;
    }
    
    public Long getTargetCharacterId() {
        return targetCharacterId;
    }
    
    public void setTargetCharacterId(Long targetCharacterId) {
        this.targetCharacterId = targetCharacterId;
    }
    
    public String getRelationshipType() {
        return relationshipType;
    }
    
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
    
    public Integer getImportance() {
        return importance;
    }
    
    public void setImportance(Integer importance) {
        this.importance = importance;
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
    
    public NovelCharacter getSourceCharacter() {
        return sourceCharacter;
    }
    
    public void setSourceCharacter(NovelCharacter sourceCharacter) {
        this.sourceCharacter = sourceCharacter;
    }
    
    public NovelCharacter getTargetCharacter() {
        return targetCharacter;
    }
    
    public void setTargetCharacter(NovelCharacter targetCharacter) {
        this.targetCharacter = targetCharacter;
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