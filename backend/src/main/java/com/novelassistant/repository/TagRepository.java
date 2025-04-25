package com.novelassistant.repository;

import com.novelassistant.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * 根据小说ID查找所有标签
     * @param novelId 小说ID
     * @return 标签列表
     */
    List<Tag> findByNovelId(Long novelId);
    
    /**
     * 根据小说ID和标签类型查找标签
     * @param novelId 小说ID
     * @param type 标签类型
     * @return 符合条件的标签列表
     */
    List<Tag> findByNovelIdAndType(Long novelId, Tag.TagType type);
    
    /**
     * 统计小说的标签数量
     * @param novelId 小说ID
     * @return 标签数量
     */
    long countByNovelId(Long novelId);
} 