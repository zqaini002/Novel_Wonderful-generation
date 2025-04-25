package com.novelassistant.service.impl;

import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Novel;
import com.novelassistant.entity.NovelCharacter;
import com.novelassistant.entity.Tag;
import com.novelassistant.repository.ChapterRepository;
import com.novelassistant.repository.NovelCharacterRepository;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.TagRepository;
import com.novelassistant.service.NlpService;
import com.novelassistant.service.TaggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签生成服务实现类
 * 用于为小说生成标签
 */
@Service
public class TaggingServiceImpl implements TaggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaggingServiceImpl.class);
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private NovelCharacterRepository characterRepository;
    
    @Autowired
    private NlpService nlpService;
    
    @Override
    @Transactional
    public int generateTagsForNovel(Long novelId) {
        logger.info("开始为小说生成标签，小说ID: {}", novelId);
        
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));
        
        // 清除旧标签
        List<Tag> existingTags = tagRepository.findByNovelId(novelId);
        if (!existingTags.isEmpty()) {
            logger.info("删除小说现有标签: {} 个", existingTags.size());
            tagRepository.deleteAll(existingTags);
        }
        
        // 获取章节内容
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        if (chapters.isEmpty()) {
            logger.warn("小说没有章节内容，无法生成标签，小说ID: {}", novelId);
            return 0;
        }
        
        // 合并章节内容获取全文
        StringBuilder fullContentBuilder = new StringBuilder();
        for (Chapter chapter : chapters) {
            if (chapter.getContent() != null) {
                fullContentBuilder.append(chapter.getContent()).append("\n");
            }
        }
        String fullContent = fullContentBuilder.toString();
        
        // 提取关键词
        Map<String, Integer> keywords = nlpService.extractKeywords(fullContent, 20);
        
        // 提取角色
        List<String> characters = nlpService.extractCharacters(fullContent);
        
        // 获取对话
        List<Map<String, String>> dialogues = nlpService.extractDialogues(fullContent);
        
        // 分析关系
        List<Map<String, Object>> relationships = nlpService.analyzeCharacterRelationships(dialogues);
        
        // 基于内容分析生成多种标签
        logger.info("生成基于内容关键词的标签");
        generateKeywordBasedTags(novel, keywords, fullContent);
        
        logger.info("生成基于情感分析的标签");
        generateEmotionBasedTags(novel, fullContent, chapters);
        
        logger.info("生成基于角色分析的标签");
        generateCharacterBasedTags(novel, characters, relationships);
        
        logger.info("生成基于内容结构的标签");
        generateStructureBasedTags(novel, fullContent, chapters);
        
        logger.info("生成内容引导标签");
        generateContentGuideTags(novel, fullContent);
        
        // 获取生成的标签数量
        int tagCount = (int) tagRepository.countByNovelId(novelId);
        logger.info("标签生成完成，小说ID: {}, 标签数量: {}", novelId, tagCount);
        
        return tagCount;
    }
    
    /**
     * 基于关键词生成标签
     */
    private void generateKeywordBasedTags(Novel novel, Map<String, Integer> keywords, String fullContent) {
        // 取前10个关键词作为标签
        keywords.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String keyword = entry.getKey();
                    
                    // 根据上下文确定标签类型
                    Tag.TagType type = determineTagType(keyword, fullContent);
                    
                    // 添加标签
                    addTag(novel, keyword, type);
                });
    }
    
    /**
     * 根据关键词和上下文判断标签类型
     */
    private Tag.TagType determineTagType(String keyword, String context) {
        // 积极特点相关的词汇
        Set<String> positiveWords = new HashSet<>(Arrays.asList(
            "精彩", "优美", "生动", "感人", "深刻", "震撼", "精细", "完美", "优秀", "精良",
            "动人", "流畅", "美丽", "温馨", "感动", "励志", "幽默", "成长", "友情", "爱情",
            "情感", "热血", "青春", "美好", "欢乐", "快乐", "治愈", "创新", "成功", "胜利"
        ));
        
        // 潜在问题相关的词汇
        Set<String> warningWords = new HashSet<>(Arrays.asList(
            "晦涩", "混乱", "拖沓", "冗长", "枯燥", "乏味", "单调", "重复", "平淡", "松散",
            "矛盾", "不合理", "不连贯", "难懂", "生硬", "粗糙", "模糊", "敏感", "暴力", "血腥",
            "恐怖", "惊悚", "悲剧", "压抑", "阴暗", "低俗", "批判", "争议", "消极", "失败"
        ));
        
        // 首先直接匹配关键词本身
        if (positiveWords.contains(keyword)) {
            return Tag.TagType.POSITIVE;
        }
        if (warningWords.contains(keyword)) {
            return Tag.TagType.WARNING;
        }
        
        // 分析关键词在内容中的上下文
        double sentiment = nlpService.analyzeSentiment(keyword);
        if (sentiment > 0.6) {
            return Tag.TagType.POSITIVE;
        } else if (sentiment < 0.4) {
            return Tag.TagType.WARNING;
        }
        
        // 默认返回INFO类型
        return Tag.TagType.INFO;
    }
    
    /**
     * 基于情感分析生成标签
     */
    private void generateEmotionBasedTags(Novel novel, String fullContent, List<Chapter> chapters) {
        // 分析整体情感
        double overallSentiment = nlpService.analyzeSentiment(fullContent);
        
        // 分析章节情感变化
        if (chapters.size() >= 3) {
            List<Double> chapterSentiments = new ArrayList<>();
            for (Chapter chapter : chapters) {
                if (chapter.getContent() != null && !chapter.getContent().isEmpty()) {
                    double sentiment = nlpService.analyzeSentiment(chapter.getContent());
                    chapterSentiments.add(sentiment);
                }
            }
            
            // 计算情感波动度
            if (!chapterSentiments.isEmpty()) {
                double avgSentiment = chapterSentiments.stream().mapToDouble(d -> d).average().orElse(0.5);
                double variance = chapterSentiments.stream()
                        .mapToDouble(d -> Math.pow(d - avgSentiment, 2))
                        .average().orElse(0);
                
                // 情感波动大
                if (variance > 0.04) {
                    addTag(novel, "情节起伏大", Tag.TagType.INFO);
                    addTag(novel, "情感丰富", Tag.TagType.POSITIVE);
                } else {
                    addTag(novel, "情节平稳", Tag.TagType.INFO);
                }
                
                // 检测情感趋势
                if (chapterSentiments.size() > 3) {
                    boolean ascending = true;
                    boolean descending = true;
                    
                    for (int i = 1; i < chapterSentiments.size(); i++) {
                        if (chapterSentiments.get(i) < chapterSentiments.get(i-1)) {
                            ascending = false;
                        }
                        if (chapterSentiments.get(i) > chapterSentiments.get(i-1)) {
                            descending = false;
                        }
                    }
                    
                    if (ascending) {
                        addTag(novel, "情感逐渐高涨", Tag.TagType.POSITIVE);
                    }
                    
                    if (descending) {
                        addTag(novel, "情感逐渐低沉", Tag.TagType.WARNING);
                    }
                }
            }
        }
        
        // 基于整体情感基调生成标签
        if (overallSentiment > 0.7) {
            addTag(novel, "积极向上", Tag.TagType.POSITIVE);
        } else if (overallSentiment < 0.3) {
            addTag(novel, "基调消极", Tag.TagType.WARNING);
        }
    }
    
    /**
     * 基于角色分析生成标签
     */
    private void generateCharacterBasedTags(Novel novel, List<String> characters, List<Map<String, Object>> relationships) {
        // 角色数量相关标签
        if (characters.size() > 10) {
            addTag(novel, "角色众多", Tag.TagType.INFO);
        } else if (characters.size() < 3) {
            addTag(novel, "角色精简", Tag.TagType.INFO);
        }
        
        // 角色关系相关标签
        if (relationships.size() > characters.size() * 1.5) {
            addTag(novel, "人物关系复杂", Tag.TagType.INFO);
            
            // 关系复杂可能是优点也可能是缺点
            if (relationships.size() > characters.size() * 3) {
                addTag(novel, "人际关系混乱", Tag.TagType.WARNING);
            } else {
                addTag(novel, "人物互动丰富", Tag.TagType.POSITIVE);
            }
        }
        
        // 检查数据库中的角色
        List<NovelCharacter> dbCharacters = characterRepository.findByNovelId(novel.getId());
        if (!dbCharacters.isEmpty()) {
            // 找出主要角色
            List<NovelCharacter> mainCharacters = dbCharacters.stream()
                    .filter(c -> c.getImportance() > 50)
                    .collect(Collectors.toList());
            
            if (mainCharacters.size() >= 3) {
                addTag(novel, "人物形象鲜明", Tag.TagType.POSITIVE);
            }
        }
    }
    
    /**
     * 基于内容结构生成标签
     */
    private void generateStructureBasedTags(Novel novel, String fullContent, List<Chapter> chapters) {
        // 内容长度相关标签
        int wordCount = fullContent.length();
        
        if (wordCount > 300000) { // 约30万字以上
            addTag(novel, "长篇巨著", Tag.TagType.INFO);
        } else if (wordCount > 100000) { // 约10万字以上
            addTag(novel, "中长篇", Tag.TagType.INFO);
        } else if (wordCount < 30000) { // 约3万字以下
            addTag(novel, "短篇", Tag.TagType.INFO);
        }
        
        // 章节长度分析
        if (chapters.size() > 0) {
            int totalChapters = chapters.size();
            OptionalDouble avgChapterLengthOpt = chapters.stream()
                    .filter(c -> c.getContent() != null)
                    .mapToInt(c -> c.getContent().length())
                    .average();
            
            if (avgChapterLengthOpt.isPresent()) {
                double avgChapterLength = avgChapterLengthOpt.getAsDouble();
                
                if (totalChapters > 100) {
                    addTag(novel, "章节较多", Tag.TagType.INFO);
                }
                
                if (avgChapterLength > 5000) {
                    addTag(novel, "章节内容丰富", Tag.TagType.POSITIVE);
                } else if (avgChapterLength < 1000) {
                    addTag(novel, "章节简短", Tag.TagType.INFO);
                }
            }
        }
    }
    
    /**
     * 生成内容引导标签
     */
    private void generateContentGuideTags(Novel novel, String content) {
        // 难度标签
        String[] complexWords = {"哲学", "形而上", "抽象", "理论", "概念", "复杂", "技术", "专业", "深奥"};
        int complexCount = countOccurrences(content, complexWords);
        if (complexCount > 20) {
            addTag(novel, "阅读难度高", Tag.TagType.WARNING);
        } else if (complexCount < 5 && content.length() > 50000) {
            addTag(novel, "轻松易读", Tag.TagType.POSITIVE);
        }
        
        // 受众标签
        if (content.contains("少年") || content.contains("孩子") || content.contains("小学") || content.contains("童话")) {
            addTag(novel, "儿童读物", Tag.TagType.INFO);
        }
        
        if (content.contains("青春") || content.contains("高中") || content.contains("大学") || content.contains("校园")) {
            addTag(novel, "青少年读物", Tag.TagType.INFO);
        }
        
        // 文学流派判断
        String[] streamOfConsciousnessWords = {"意识流", "内心独白", "心理描写", "思维", "意识", "心流"};
        int streamCount = countOccurrences(content, streamOfConsciousnessWords);
        if (streamCount > 10) {
            addTag(novel, "意识流", Tag.TagType.INFO);
        }
        
        String[] realisticWords = {"现实主义", "社会", "现实", "问题", "批判", "反映"};
        int realisticCount = countOccurrences(content, realisticWords);
        if (realisticCount > 15) {
            addTag(novel, "现实主义", Tag.TagType.INFO);
        }
        
        String[] romanticWords = {"浪漫", "理想", "幻想", "情感", "抒情", "唯美"};
        int romanticCount = countOccurrences(content, romanticWords);
        if (romanticCount > 15) {
            addTag(novel, "浪漫主义", Tag.TagType.INFO);
        }
    }
    
    /**
     * 计算单词在文本中出现的次数
     */
    private int countOccurrences(String text, String[] words) {
        int count = 0;
        for (String word : words) {
            int lastIndex = 0;
            while (lastIndex != -1) {
                lastIndex = text.indexOf(word, lastIndex);
                if (lastIndex != -1) {
                    count++;
                    lastIndex += word.length();
                }
            }
        }
        return count;
    }
    
    /**
     * 添加标签
     */
    private void addTag(Novel novel, String name, Tag.TagType type) {
        try {
            Tag tag = new Tag(novel, name, type);
            tagRepository.save(tag);
            logger.debug("标签已添加: {}, 类型: {}", name, type);
        } catch (Exception e) {
            logger.warn("添加标签失败: {}, 错误: {}", name, e.getMessage());
        }
    }
} 