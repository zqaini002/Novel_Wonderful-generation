package com.novelassistant.repository.visualization;

import com.novelassistant.entity.visualization.StructureData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StructureDataRepository extends JpaRepository<StructureData, Long> {
    
    /**
     * 根据小说ID查找结构数据
     * @param novelId 小说ID
     * @return 结构数据列表
     */
    List<StructureData> findByNovelId(Long novelId);
    
    /**
     * 根据小说ID查找结构数据，按百分比排序
     * @param novelId 小说ID
     * @return 按百分比排序的结构数据列表
     */
    List<StructureData> findByNovelIdOrderByPercentageDesc(Long novelId);
    
    /**
     * 根据小说ID和部分名称查找结构数据
     * @param novelId 小说ID
     * @param sectionName 部分名称
     * @return 匹配的结构数据
     */
    StructureData findByNovelIdAndSectionName(Long novelId, String sectionName);
    
    /**
     * 删除小说的所有结构数据
     * @param novelId 小说ID
     */
    void deleteByNovelId(Long novelId);
} 