package com.novelassistant.repository.visualization;

import com.novelassistant.entity.visualization.WordCountData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordCountDataRepository extends JpaRepository<WordCountData, Long> {
    
    /**
     * 根据小说ID查找字数分布数据
     * @param novelId 小说ID
     * @return 字数分布数据列表
     */
    List<WordCountData> findByNovelId(Long novelId);
    
    /**
     * 根据小说ID查找字数分布数据，按范围开始值排序
     * @param novelId 小说ID
     * @return 按范围开始值排序的字数分布数据列表
     */
    List<WordCountData> findByNovelIdOrderByRangeStartAsc(Long novelId);
    
    /**
     * 根据小说ID和范围区间查找字数分布数据
     * @param novelId 小说ID
     * @param rangeStart 范围开始
     * @param rangeEnd 范围结束
     * @return 匹配的字数分布数据
     */
    WordCountData findByNovelIdAndRangeStartAndRangeEnd(Long novelId, Integer rangeStart, Integer rangeEnd);
    
    /**
     * 删除小说的所有字数分布数据
     * @param novelId 小说ID
     */
    void deleteByNovelId(Long novelId);
} 