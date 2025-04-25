package com.novelassistant.repository;

import com.novelassistant.entity.CharacterDialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色对话仓库接口
 */
@Repository
public interface CharacterDialogueRepository extends JpaRepository<CharacterDialogue, Long> {
    
    /**
     * 根据小说ID查找所有角色对话
     * @param novelId 小说ID
     * @return 角色对话列表
     */
    List<CharacterDialogue> findByNovelId(Long novelId);
    
    /**
     * 根据角色ID查找相关的角色对话
     * @param characterId 角色ID
     * @return 角色对话列表
     */
    List<CharacterDialogue> findByCharacterId(Long characterId);
    
    /**
     * 根据章节ID查找相关的角色对话
     * @param chapterId 章节ID
     * @return 角色对话列表
     */
    List<CharacterDialogue> findByChapterId(Long chapterId);
    
    /**
     * 根据角色ID和章节ID查找角色对话
     * @param characterId 角色ID
     * @param chapterId 章节ID
     * @return 角色对话列表
     */
    List<CharacterDialogue> findByCharacterIdAndChapterId(Long characterId, Long chapterId);
    
    /**
     * 根据小说ID和角色ID查找角色对话
     * @param novelId 小说ID
     * @param characterId 角色ID
     * @return 角色对话列表
     */
    List<CharacterDialogue> findByNovelIdAndCharacterId(Long novelId, Long characterId);
} 