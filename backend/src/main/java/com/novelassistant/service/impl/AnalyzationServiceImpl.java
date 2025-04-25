package com.novelassistant.service.impl;

import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Novel;
import com.novelassistant.entity.NovelCharacter;
import com.novelassistant.repository.ChapterRepository;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.service.AnalyzationService;
import com.novelassistant.service.MachineLearningService;
import com.novelassistant.service.NlpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 小说分析服务实现类
 * 用于分析小说内容，生成摘要、提取关键词等
 * 已整合机器学习功能增强分析能力
 */
@Service
public class AnalyzationServiceImpl implements AnalyzationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyzationServiceImpl.class);
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private NlpService nlpService;
    
    @Autowired
    private MachineLearningService mlService;
    
    @Override
    public Map<String, Object> analyzeNovel(Long novelId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));
        
            // 获取章节
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        if (chapters.isEmpty()) {
                result.put("error", "小说没有章节内容");
                return result;
        }
        
        // 合并所有章节内容
            String fullContent = chapters.stream()
                    .map(Chapter::getContent)
                    .collect(Collectors.joining("\n\n"));
            
            // 基本信息
            result.put("title", novel.getTitle());
            result.put("author", novel.getAuthor());
            result.put("wordCount", fullContent.length());
            result.put("chapterCount", chapters.size());
            
            // NLP分析
            performNlpAnalysis(result, fullContent, chapters);
            
            // 机器学习增强分析
            enhanceWithMachineLearning(result, fullContent, chapters);
            
        } catch (Exception e) {
            logger.error("分析小说时出错", e);
            result.put("error", "分析过程中出错: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 执行基础NLP分析
     */
    private void performNlpAnalysis(Map<String, Object> result, String fullContent, List<Chapter> chapters) {
        // 生成摘要
        String summary = nlpService.generateSummary(fullContent, 500);
        result.put("summary", summary);
        
        // 提取关键词
        Map<String, Integer> keywords = nlpService.extractKeywords(fullContent, 20);
        result.put("keywords", keywords);
        
        // 提取人物
        List<String> characters = nlpService.extractCharacters(fullContent);
        result.put("characters", characters);
        
        // 分析情感
        double sentiment = nlpService.analyzeSentiment(fullContent);
        result.put("sentiment", sentiment);
        
        // 提取对话
        List<Map<String, String>> dialogues = nlpService.extractDialogues(fullContent);
        result.put("dialogueCount", dialogues.size());
        
        // 分析人物关系
        List<Map<String, Object>> relationships = nlpService.analyzeCharacterRelationships(dialogues);
        result.put("characterRelationships", relationships);
    }
    
    /**
     * 使用机器学习增强分析
     */
    private void enhanceWithMachineLearning(Map<String, Object> result, String fullContent, List<Chapter> chapters) {
        // 提取章节内容列表
        List<String> chapterContents = chapters.stream()
                .map(Chapter::getContent)
                .collect(Collectors.toList());
        
        // 深度情感分析
        double deepSentiment = mlService.deepSentimentAnalysis(fullContent);
        result.put("deepSentiment", deepSentiment);
        
        // 预测情节发展
        Map<String, Double> plotPredictions = mlService.predictPlotDevelopment(chapterContents);
        result.put("plotPredictions", plotPredictions);
        
        // 主题提取
        Map<Integer, List<String>> topics = mlService.extractTopicsWithLDA(fullContent, 5);
        result.put("topics", topics);
        
        // 写作风格分析
        Map<String, Double> writingStyle = mlService.detectWritingStyle(fullContent);
        result.put("writingStyle", writingStyle);
        
        // 构建角色网络
        Map<String, List<Map<String, Object>>> characterNetwork = mlService.buildCharacterNetwork(fullContent);
        result.put("enhancedCharacterNetwork", characterNetwork);
        
        // 文本复杂度分析
        Map<String, Double> textComplexity = mlService.analyzeTextComplexity(fullContent);
        result.put("textComplexity", textComplexity);
        
        // 类型分类
        List<String> categories = Arrays.asList("奇幻", "科幻", "武侠", "言情", "悬疑", "历史", "都市");
        Map<String, Double> genreClassification = mlService.classifyText(fullContent, categories);
        result.put("genreClassification", genreClassification);
        
        // 章节聚类分析
        if (chapterContents.size() > 5) {
            Map<Integer, List<Integer>> chapterClusters = mlService.clusterTexts(chapterContents, 3);
            result.put("chapterClusters", chapterClusters);
        }
    }

    @Override
    public Map<String, Object> analyzeChapter(Long chapterId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new RuntimeException("章节不存在: " + chapterId));
            
            String content = chapter.getContent();
            
            // 基本信息
            result.put("title", chapter.getTitle());
            result.put("wordCount", content.length());
            
            // NLP分析
            // 生成摘要
            String summary = nlpService.generateSummary(content, 200);
            result.put("summary", summary);
            
            // 提取关键词
            Map<String, Integer> keywords = nlpService.extractKeywords(content, 10);
            result.put("keywords", keywords);
            
            // 提取人物
            List<String> characters = nlpService.extractCharacters(content);
            result.put("characters", characters);
            
            // 分析情感
            double sentiment = nlpService.analyzeSentiment(content);
            result.put("sentiment", sentiment);
            
            // 机器学习增强分析
            // 深度情感分析
            double deepSentiment = mlService.deepSentimentAnalysis(content);
            result.put("deepSentiment", deepSentiment);
        
            // 写作风格分析
            Map<String, Double> writingStyle = mlService.detectWritingStyle(content);
            result.put("writingStyle", writingStyle);
            
            // 文本复杂度分析
            Map<String, Double> textComplexity = mlService.analyzeTextComplexity(content);
            result.put("textComplexity", textComplexity);
            
        } catch (Exception e) {
            logger.error("分析章节时出错", e);
            result.put("error", "分析过程中出错: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> compareChapters(List<Long> chapterIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Chapter> chapters = chapterRepository.findAllById(chapterIds);
            if (chapters.size() < 2) {
                result.put("error", "请至少提供两个章节进行比较");
                return result;
            }
            
            // 提取章节内容
            List<String> contents = chapters.stream()
                    .map(Chapter::getContent)
                    .collect(Collectors.toList());
            
            // 计算章节间相似度矩阵
            List<List<Double>> similarityMatrix = new ArrayList<>();
            for (String content1 : contents) {
                List<Double> similarities = new ArrayList<>();
                for (String content2 : contents) {
                    double similarity = mlService.calculateTextSimilarity(content1, content2);
                    similarities.add(similarity);
                }
                similarityMatrix.add(similarities);
            }
            result.put("similarityMatrix", similarityMatrix);
            
            // 提取各章节关键词
            List<Map<String, Integer>> chapterKeywords = new ArrayList<>();
            for (String content : contents) {
                chapterKeywords.add(nlpService.extractKeywords(content, 10));
            }
            result.put("chapterKeywords", chapterKeywords);
            
            // 提取各章节情感
            List<Double> sentiments = new ArrayList<>();
            List<Double> deepSentiments = new ArrayList<>();
            for (String content : contents) {
                sentiments.add(nlpService.analyzeSentiment(content));
                deepSentiments.add(mlService.deepSentimentAnalysis(content));
            }
            result.put("sentiments", sentiments);
            result.put("deepSentiments", deepSentiments);
            
            // 章节聚类
            Map<Integer, List<Integer>> clusters = mlService.clusterTexts(contents, Math.min(3, contents.size()));
            result.put("chapterClusters", clusters);
            
            // 主要人物在各章节中的出现情况
            Set<String> allCharacters = new HashSet<>();
            for (String content : contents) {
                allCharacters.addAll(nlpService.extractCharacters(content));
            }
            
            Map<String, List<Integer>> characterAppearances = new HashMap<>();
            for (String character : allCharacters) {
                List<Integer> appearances = new ArrayList<>();
                for (String content : contents) {
                    int count = countOccurrences(content, character);
                    appearances.add(count);
                }
                characterAppearances.put(character, appearances);
            }
            result.put("characterAppearances", characterAppearances);
            
            // 写作风格对比
            List<Map<String, Double>> writingStyles = new ArrayList<>();
            for (String content : contents) {
                writingStyles.add(mlService.detectWritingStyle(content));
            }
            result.put("writingStyles", writingStyles);
            
        } catch (Exception e) {
            logger.error("比较章节时出错", e);
            result.put("error", "比较过程中出错: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 计算字符串在文本中出现的次数
     */
    private int countOccurrences(String text, String target) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) >= 0) {
            count++;
            index += target.length();
        }
        return count;
    }
    
    @Override
    public Map<String, Object> analyzeCharacter(Long novelId, String characterName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Novel novel = novelRepository.findById(novelId)
                    .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));
        
            // 获取章节
            List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
            if (chapters.isEmpty()) {
                result.put("error", "小说没有章节内容");
                return result;
            }
            
            // 合并所有章节内容
            String fullContent = chapters.stream()
                    .map(Chapter::getContent)
                    .collect(Collectors.joining("\n\n"));
            
            // 提取角色出场信息
            List<Map<String, String>> dialogues = nlpService.extractDialogues(fullContent);
            
            // 筛选与角色相关的对话
            List<Map<String, String>> characterDialogues = dialogues.stream()
                    .filter(d -> characterName.equals(d.get("speaker")))
                    .collect(Collectors.toList());
            
            // 基本信息
            int appearanceCount = countOccurrences(fullContent, characterName);
            result.put("name", characterName);
            result.put("appearanceCount", appearanceCount);
            result.put("dialogueCount", characterDialogues.size());
            
            // 分析角色情感
            double sentiment = 0.5;
            if (!characterDialogues.isEmpty()) {
                List<Double> sentiments = new ArrayList<>();
                for (Map<String, String> dialogue : characterDialogues) {
                    String content = dialogue.get("content");
                    if (content != null && !content.isEmpty()) {
                        sentiments.add(mlService.deepSentimentAnalysis(content));
                    }
                }
                sentiment = sentiments.stream().mapToDouble(d -> d).average().orElse(0.5);
            }
            result.put("sentiment", sentiment);
            
            // 角色关系网络
            Map<String, List<Map<String, Object>>> characterNetwork = mlService.buildCharacterNetwork(fullContent);
            List<Map<String, Object>> relationships = characterNetwork.get("relationships").stream()
                    .filter(r -> characterName.equals(r.get("character1")) || characterName.equals(r.get("character2")))
                    .collect(Collectors.toList());
            result.put("relationships", relationships);
            
            // 角色对话关键词
            String allDialogues = characterDialogues.stream()
                    .map(d -> d.get("content"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));
            
            if (!allDialogues.isEmpty()) {
                Map<String, Integer> keywords = nlpService.extractKeywords(allDialogues, 15);
                result.put("dialogueKeywords", keywords);
                
                // 角色语言风格分析
                Map<String, Double> languageStyle = mlService.detectWritingStyle(allDialogues);
                result.put("languageStyle", languageStyle);
    }
    
            // 角色在各章节中的出现情况
            List<Integer> chapterAppearances = new ArrayList<>();
        for (Chapter chapter : chapters) {
                int count = countOccurrences(chapter.getContent(), characterName);
                chapterAppearances.add(count);
            }
            result.put("chapterAppearances", chapterAppearances);
            
        } catch (Exception e) {
            logger.error("分析角色时出错", e);
            result.put("error", "分析过程中出错: " + e.getMessage());
        }
        
        return result;
    }
} 