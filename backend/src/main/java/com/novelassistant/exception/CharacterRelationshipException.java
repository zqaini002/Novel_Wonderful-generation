package com.novelassistant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 角色关系异常
 * 用于处理角色关系操作相关的错误
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CharacterRelationshipException extends ServiceException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 源角色ID
     */
    private final Long sourceCharacterId;
    
    /**
     * 目标角色ID
     */
    private final Long targetCharacterId;
    
    /**
     * 小说ID
     */
    private final Long novelId;
    
    /**
     * 默认构造函数
     * 
     * @param message 错误信息
     */
    public CharacterRelationshipException(String message) {
        super(message, "CHARACTER_RELATIONSHIP_ERROR");
        this.sourceCharacterId = null;
        this.targetCharacterId = null;
        this.novelId = null;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param novelId 小说ID
     */
    public CharacterRelationshipException(String message, Long novelId) {
        super(message, "CHARACTER_RELATIONSHIP_ERROR");
        this.sourceCharacterId = null;
        this.targetCharacterId = null;
        this.novelId = novelId;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param sourceCharacterId 源角色ID
     * @param targetCharacterId 目标角色ID
     */
    public CharacterRelationshipException(String message, Long sourceCharacterId, Long targetCharacterId) {
        super(message, "CHARACTER_RELATIONSHIP_ERROR");
        this.sourceCharacterId = sourceCharacterId;
        this.targetCharacterId = targetCharacterId;
        this.novelId = null;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param novelId 小说ID
     * @param sourceCharacterId 源角色ID
     * @param targetCharacterId 目标角色ID
     */
    public CharacterRelationshipException(String message, Long novelId, Long sourceCharacterId, Long targetCharacterId) {
        super(message, "CHARACTER_RELATIONSHIP_ERROR");
        this.sourceCharacterId = sourceCharacterId;
        this.targetCharacterId = targetCharacterId;
        this.novelId = novelId;
    }
    
    /**
     * 获取源角色ID
     * 
     * @return 源角色ID
     */
    public Long getSourceCharacterId() {
        return sourceCharacterId;
    }
    
    /**
     * 获取目标角色ID
     * 
     * @return 目标角色ID
     */
    public Long getTargetCharacterId() {
        return targetCharacterId;
    }
    
    /**
     * 获取小说ID
     * 
     * @return 小说ID
     */
    public Long getNovelId() {
        return novelId;
    }
}
 