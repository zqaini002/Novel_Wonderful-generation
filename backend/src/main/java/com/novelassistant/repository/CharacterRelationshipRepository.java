package com.novelassistant.repository;

import com.novelassistant.entity.CharacterRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色关系仓库接口
 */
@Repository
public interface CharacterRelationshipRepository extends JpaRepository<CharacterRelationship, Long> {
    
    /**
     * 根据小说ID查找所有角色关系
     * @param novelId 小说ID
     * @return 角色关系列表
     */
    List<CharacterRelationship> findByNovelId(Long novelId);
    
    /**
     * 根据源角色ID查找相关的角色关系
     * @param sourceCharacterId 源角色ID
     * @return 角色关系列表
     */
    List<CharacterRelationship> findBySourceCharacterId(Long sourceCharacterId);
    
    /**
     * 根据目标角色ID查找相关的角色关系
     * @param targetCharacterId 目标角色ID
     * @return 角色关系列表
     */
    List<CharacterRelationship> findByTargetCharacterId(Long targetCharacterId);
    
    /**
     * 根据源角色ID和目标角色ID查找角色关系
     * @param sourceCharacterId 源角色ID
     * @param targetCharacterId 目标角色ID
     * @return 角色关系列表
     */
    List<CharacterRelationship> findBySourceCharacterIdAndTargetCharacterId(Long sourceCharacterId, Long targetCharacterId);
} 