package com.novelassistant.repository;

import com.novelassistant.entity.NovelCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 小说角色仓库接口
 */
@Repository
public interface NovelCharacterRepository extends JpaRepository<NovelCharacter, Long> {
    
    /**
     * 根据小说ID查找所有角色
     * @param novelId 小说ID
     * @return 角色列表
     */
    List<NovelCharacter> findByNovelId(Long novelId);
    
    /**
     * 根据小说ID和角色名称查找角色
     * @param novelId 小说ID
     * @param name 角色名称
     * @return 角色列表
     */
    List<NovelCharacter> findByNovelIdAndNameContaining(Long novelId, String name);
    
    /**
     * 根据小说ID和角色类别查找角色
     * @param novelId 小说ID
     * @param category 角色类别
     * @return 角色列表
     */
    List<NovelCharacter> findByNovelIdAndCategory(Long novelId, String category);
} 