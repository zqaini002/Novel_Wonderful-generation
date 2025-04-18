package com.novelassistant.service.impl;

import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Tag;
import com.novelassistant.entity.NovelCharacter;
import com.novelassistant.entity.CharacterRelationship;
import com.novelassistant.repository.ChapterRepository;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.TagRepository;
import com.novelassistant.repository.NovelCharacterRepository;
import com.novelassistant.repository.CharacterRelationshipRepository;
import com.novelassistant.service.VisualizationService;
import com.novelassistant.util.LogUtil;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 小说可视化服务实现类
 * 提供各类数据可视化所需数据的服务实现
 */
@Service
public class VisualizationServiceImpl implements VisualizationService {

    private static final Logger logger = LogUtil.getLogger(VisualizationServiceImpl.class);
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private NovelCharacterRepository characterRepository;
    
    @Autowired
    private CharacterRelationshipRepository relationshipRepository;

    /**
     * 获取小说关键词云数据
     */
    @Override
    public List<Map<String, Object>> getKeywordCloudData(Long novelId) {
        logger.info("获取小说关键词云数据, 小说ID: {}", novelId);
        
        // 验证小说是否存在
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在，ID: " + novelId));

        // 获取小说的所有章节
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        
        // 统计关键词频率
        Map<String, Integer> keywordFrequency = new HashMap<>();
        
        // 从章节中提取关键词
        for (Chapter chapter : chapters) {
            List<String> keywords = chapter.getKeywords();
            if (keywords != null) {
                for (String keyword : keywords) {
                    keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + 1);
                }
            }
        }
        
        // 从Tags中提取关键词
        List<Tag> tags = tagRepository.findByNovelId(novelId);
        for (Tag tag : tags) {
            keywordFrequency.put(tag.getName(), keywordFrequency.getOrDefault(tag.getName(), 0) + 3);
        }
        
        // 转换为前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : keywordFrequency.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            result.add(item);
        }
        
        // 按频率排序，最多返回50个关键词
        return result.stream()
                .sorted((a, b) -> ((Integer) b.get("value")).compareTo((Integer) a.get("value")))
                .limit(50)
                .collect(Collectors.toList());
    }

    /**
     * 获取小说情节波动图数据
     */
    @Override
    public Map<String, Object> getEmotionalFluctuationData(Long novelId) {
        logger.info("获取小说情节波动图数据, 小说ID: {}", novelId);
        
        // 验证小说是否存在
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在，ID: " + novelId));

        // 获取小说的所有章节（按章节号排序）
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        
        // 准备数据
        List<Map<String, Object>> emotionalData = new ArrayList<>();
        
        // 处理每个章节的情节数据
        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            Map<String, Object> chapterData = new HashMap<>();
            
            // 基础数据
            chapterData.put("chapter", "第" + chapter.getChapterNumber() + "章");
            chapterData.put("chapterTitle", chapter.getTitle());
            
            // 通过章节摘要分析计算情感值
            // 实际项目中应该通过NLP分析计算，这里模拟数据
            
            // 模拟情感值：随机波动，但总体呈现起伏状态
            double tensionBase = Math.sin(i * 0.3) * 30 + 50;  // 基础值在20-80之间波动
            double randomVariation = Math.random() * 20 - 10;  // 添加-10到10的随机变化
            double emotionValue = Math.max(0, Math.min(100, tensionBase + randomVariation));
            
            // 每5章左右设置一个关键事件
            boolean isKeyEvent = (i % 5 == 0 || i % 7 == 0);
            
            chapterData.put("emotion", emotionValue);
            
            // 添加章节摘要作为事件描述
            String eventDesc = (chapter.getSummary() != null && !chapter.getSummary().isEmpty())
                    ? chapter.getSummary()
                    : "章节关键事件描述";
            
            // 截断过长的摘要
            if (eventDesc.length() > 100) {
                eventDesc = eventDesc.substring(0, 97) + "...";
            }
            
            chapterData.put("event", eventDesc);
            
            // 标记关键章节（高潮）
            if (isKeyEvent) {
                chapterData.put("isImportant", true);
            }
            
            // 标记情节高潮段落的开始和结束
            if (i > 0 && i < chapters.size() - 10 && i % 15 == 0) {
                chapterData.put("isClimaxStart", true);
            }
            if (i > 5 && i < chapters.size() - 5 && i % 15 == 5) {
                chapterData.put("isClimaxEnd", true);
            }
            
            emotionalData.add(chapterData);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("emotional", emotionalData);
        
        return result;
    }

    /**
     * 获取小说结构分析数据
     */
    @Override
    public Map<String, Object> getStructureAnalysisData(Long novelId) {
        logger.info("获取小说结构分析数据, 小说ID: {}", novelId);
        
        // 验证小说是否存在
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在，ID: " + novelId));

        // 获取小说的所有章节（按章节号排序）
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        
        // 小说基本信息
        Map<String, Object> novelInfo = new HashMap<>();
        novelInfo.put("title", novel.getTitle());
        novelInfo.put("author", novel.getAuthor());
        novelInfo.put("totalChapters", chapters.size());
        
        // 构建小说结构数据
        Map<String, Object> structureData = new HashMap<>();
        
        // 章节字数分布
        Map<String, Object> wordCountData = new LinkedHashMap<>();
        wordCountData.put("type", "章节字数分布");
        
        List<Map<String, Object>> wordCountSeries = new ArrayList<>();
        
        // 模拟章节字数范围分布
        int[] wordCountRanges = {0, 1000, 2000, 3000, 4000, 5000, 10000};
        int[] wordCountDistribution = new int[wordCountRanges.length - 1];
        
        // 统计每个章节的字数分布
        for (Chapter chapter : chapters) {
            int wordCount = chapter.getWordCount() != null ? chapter.getWordCount() : 
                            (chapter.getContent() != null ? chapter.getContent().length() / 2 : 0);
            
            for (int i = 0; i < wordCountRanges.length - 1; i++) {
                if (wordCount >= wordCountRanges[i] && wordCount < wordCountRanges[i + 1]) {
                    wordCountDistribution[i]++;
                    break;
                }
            }
        }
        
        // 构建系列数据
        List<String> wordCountCategories = new ArrayList<>();
        List<Integer> wordCountValues = new ArrayList<>();
        
        for (int i = 0; i < wordCountRanges.length - 1; i++) {
            String range = wordCountRanges[i] + "-" + wordCountRanges[i + 1];
            wordCountCategories.add(range);
            wordCountValues.add(wordCountDistribution[i]);
        }
        
        Map<String, Object> wordCountItem = new HashMap<>();
        wordCountItem.put("name", "章节数");
        wordCountItem.put("data", wordCountValues);
        wordCountSeries.add(wordCountItem);
        
        wordCountData.put("categories", wordCountCategories);
        wordCountData.put("series", wordCountSeries);
        
        // 小说段落结构
        Map<String, Object> structureSections = new LinkedHashMap<>();
        
        // 模拟标准小说结构
        String[] sectionNames = {"序章", "引子", "发展", "高潮", "结局"};
        double[] percentages = {0.1, 0.2, 0.4, 0.2, 0.1};
        
        int totalChapters = chapters.size();
        int startChapter = 0;
        
        List<Map<String, Object>> sections = new ArrayList<>();
        
        for (int i = 0; i < sectionNames.length; i++) {
            int sectionChapters = (int) Math.round(totalChapters * percentages[i]);
            int endChapter = Math.min(startChapter + sectionChapters, totalChapters);
            
            Map<String, Object> section = new HashMap<>();
            section.put("name", sectionNames[i]);
            section.put("percentage", percentages[i]);
            section.put("startChapter", startChapter + 1);
            section.put("endChapter", endChapter);
            section.put("chapterCount", endChapter - startChapter);
            
            sections.add(section);
            startChapter = endChapter;
        }
        
        structureSections.put("type", "小说结构分析");
        structureSections.put("sections", sections);
        
        // 整合所有数据
        structureData.put("novelInfo", novelInfo);
        structureData.put("wordCount", wordCountData);
        structureData.put("structure", structureSections);
        
        return structureData;
    }

    /**
     * 获取小说人物关系网络数据
     */
    @Override
    public Map<String, Object> getCharacterRelationshipData(Long novelId) {
        Map<String, Object> networkData = new HashMap<>();
        Optional<Novel> novelOpt = Optional.empty();
        
        try {
            // 检查小说是否存在
            novelOpt = novelRepository.findById(novelId);
            if (!novelOpt.isPresent()) {
                logger.warn("获取人物关系网络数据失败: 小说不存在, ID: {}", novelId);
                return networkData;
            }
            
            Novel novel = novelOpt.get();
            // 检查小说处理状态，只有完成处理的小说才有可视化数据
            if (!"COMPLETED".equals(novel.getProcessingStatus())) {
                logger.warn("获取人物关系网络数据失败: 小说未完成处理, ID: {}", novelId);
                return demoCharacterNetworkData(novel.getTitle());
            }
            
            // 获取小说的所有角色
            List<NovelCharacter> characters = characterRepository.findByNovelId(novelId);
            if (characters.isEmpty()) {
                logger.warn("小说没有角色数据, ID: {}", novelId);
                return demoCharacterNetworkData(novel.getTitle());
            }
            
            // 获取小说的所有角色关系
            List<CharacterRelationship> relationships = relationshipRepository.findByNovelId(novelId);
            
            // 构建节点数据
            List<Map<String, Object>> nodes = new ArrayList<>();
            for (NovelCharacter character : characters) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", character.getId().toString());
                node.put("name", character.getName());
                node.put("value", character.getImportance());
                node.put("category", character.getCategory());
                node.put("desc", character.getDescription());
                nodes.add(node);
            }
            
            // 构建关系数据
            List<Map<String, Object>> links = new ArrayList<>();
            for (CharacterRelationship relationship : relationships) {
                Map<String, Object> link = new HashMap<>();
                link.put("source", relationship.getSourceCharacterId().toString());
                link.put("target", relationship.getTargetCharacterId().toString());
                link.put("relation", relationship.getRelationshipType());
                link.put("value", relationship.getImportance());
                link.put("desc", relationship.getDescription());
                links.add(link);
            }
            
            // 设置返回数据
            networkData.put("nodes", nodes);
            networkData.put("links", links);
            
            return networkData;
        } catch (Exception e) {
            logger.error("获取人物关系网络数据异常: ", e);
            return demoCharacterNetworkData(novelOpt.map(Novel::getTitle).orElse("未知小说"));
        }
    }

    /**
     * 生成演示用的人物关系网络数据
     */
    private Map<String, Object> demoCharacterNetworkData(String title) {
        Map<String, Object> networkData = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        
        // 生成一些演示用的节点（角色）
        String[] names = {"主角", "女主角", "反派", "挚友", "导师", "家人1", "家人2", "朋友1", "朋友2", "对手"};
        String[] categories = {"主要角色", "主要角色", "主要角色", "主要角色", "主要角色", "次要角色", "次要角色", "次要角色", "次要角色", "主要角色"};
        int[] importances = {80, 70, 65, 50, 55, 40, 35, 30, 30, 45};
        
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", String.valueOf(i + 1));
            node.put("name", names[i]);
            node.put("value", importances[i]);
            node.put("category", categories[i]);
            node.put("desc", title + "中的" + names[i]);
            nodes.add(node);
        }
        
        // 生成一些演示用的关系
        String[][] relations = {
            {"1", "2", "恋人", "5"},
            {"1", "3", "宿敌", "5"},
            {"1", "4", "好友", "4"},
            {"1", "5", "师徒", "4"},
            {"1", "6", "亲子", "3"},
            {"1", "7", "亲子", "3"},
            {"2", "3", "敌对", "2"},
            {"2", "8", "朋友", "2"},
            {"4", "8", "熟识", "1"},
            {"4", "9", "熟识", "1"},
            {"5", "3", "旧识", "2"},
            {"5", "10", "师徒", "2"},
            {"6", "7", "伴侣", "3"}
        };
        
        for (String[] relation : relations) {
            Map<String, Object> link = new HashMap<>();
            link.put("source", relation[0]);
            link.put("target", relation[1]);
            link.put("relation", relation[2]);
            link.put("value", Integer.parseInt(relation[3]));
            links.add(link);
        }
        
        networkData.put("nodes", nodes);
        networkData.put("links", links);
        
        return networkData;
    }

    /**
     * 获取小说场景分布数据
     */
    @Override
    public Map<String, Object> getSceneDistributionData(Long novelId) {
        // 此功能暂未实现，返回空数据
        return new HashMap<>();
    }

    /**
     * 获取小说综合统计数据
     */
    @Override
    public Map<String, Object> getNovelStatisticsData(Long novelId) {
        // 此功能暂未实现，返回空数据
        return new HashMap<>();
    }
} 