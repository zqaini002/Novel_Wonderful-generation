package com.novelassistant.service.impl;

import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Novel;
import com.novelassistant.repository.ChapterRepository;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.service.NlpService;
import com.novelassistant.service.NovelService;
import com.novelassistant.service.crawler.NovelCrawlerService;
import com.novelassistant.util.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小说服务扩展实现类
 * 增加URL导入小说功能
 */
@Service
@Primary
public class NovelServiceExtImpl extends NovelServiceImpl {

    private static final Logger logger = LogUtil.getLogger(NovelServiceExtImpl.class);

    @Autowired
    private NovelCrawlerService novelCrawlerService;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private NlpService nlpService;
    
    /**
     * 从URL导入小说
     * @param url 小说URL
     * @param userId 用户ID
     * @param title 可选的小说标题，如果提供则覆盖从网页提取的标题
     * @param author 可选的小说作者，如果提供则覆盖从网页提取的作者
     * @param maxChapters 最大章节数限制，0表示不限制
     * @return 处理结果
     */
    @Override
    @Transactional
    public Map<String, Object> importNovelFromUrl(String url, Long userId, String title, String author, int maxChapters) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查URL是否受支持
        if (!novelCrawlerService.isSupportedUrl(url)) {
            result.put("success", false);
            result.put("error", "不支持的URL格式，目前仅支持笔趣阁网站");
            return result;
        }
        
        // 爬取小说信息
        Map<String, Object> crawlResult = novelCrawlerService.crawlNovelFromUrl(url, maxChapters);
        
        if (!(Boolean) crawlResult.getOrDefault("success", false)) {
            result.put("success", false);
            result.put("error", crawlResult.get("error"));
            return result;
        }
        
        try {
            // 创建小说实体
            Novel novel = new Novel();
            
            // 设置小说标题，优先使用传入的标题
            if (title != null && !title.trim().isEmpty()) {
                novel.setTitle(title.trim());
                logger.info("使用提供的标题: {}", title);
            } else {
                novel.setTitle((String) crawlResult.get("title"));
                logger.info("使用爬取的标题: {}", crawlResult.get("title"));
            }
            
            // 设置小说作者，优先使用传入的作者
            if (author != null && !author.trim().isEmpty()) {
                novel.setAuthor(author.trim());
                logger.info("使用提供的作者: {}", author);
            } else {
                novel.setAuthor((String) crawlResult.get("author"));
                logger.info("使用爬取的作者: {}", crawlResult.get("author"));
            }
            
            novel.setDescription((String) crawlResult.get("description"));
            novel.setSourceUrl(url);
            novel.setUserId(userId);
            novel.setProcessingStatus(Novel.ProcessingStatus.PROCESSING);
            
            // 获取章节信息
            @SuppressWarnings("unchecked")
            List<Map<String, String>> chaptersInfo = (List<Map<String, String>>) crawlResult.get("chapters");
            
            // 记录章节总数和已处理数量
            novel.setTotalChapters(chaptersInfo.size());
            novel.setProcessedChapters(0);
            
            // 保存小说
            novel = novelRepository.save(novel);
            logger.info("成功保存小说: {}, ID: {}", novel.getTitle(), novel.getId());
            
            final Long novelId = novel.getId();
            
            // 保存章节信息
            List<Chapter> chapters = new ArrayList<>();
            
            // 构建完整内容，用于生成摘要
            StringBuilder fullContentBuilder = new StringBuilder();
            
            for (Map<String, String> chapterInfo : chaptersInfo) {
                Chapter chapter = new Chapter();
                chapter.setNovel(novel);
                chapter.setTitle(chapterInfo.get("title"));
                
                // 设置章节编号
                try {
                    chapter.setChapterNumber(Integer.parseInt(chapterInfo.get("number")));
                } catch (NumberFormatException e) {
                    chapter.setChapterNumber(chapters.size() + 1);
                }
                
                // 保存章节内容
                String content = chapterInfo.get("content");
                if (content != null && !content.isEmpty()) {
                    chapter.setContent(content);
                    
                    // 添加到全文中
                    fullContentBuilder.append(chapter.getTitle()).append("\n");
                    fullContentBuilder.append(content).append("\n\n");
                    
                    // 计算字数
                    chapter.setWordCount(content.length());
                    
                    // 使用NLP生成摘要（如果可用）
                    try {
                        String summary = nlpService.generateSummary(content, 200);
                        chapter.setSummary(summary);
                        
                        // 提取关键词
                        Map<String, Integer> keywordMap = nlpService.extractKeywords(content, 10);
                        List<String> keywords = new ArrayList<>(keywordMap.keySet());
                        chapter.setKeywords(keywords);
                        
                        logger.info("成功处理章节内容: {}", chapter.getTitle());
                    } catch (Exception e) {
                        logger.error("处理章节内容时出错: {}", e.getMessage());
                        chapter.setSummary("摘要生成失败");
                        // 确保异常不会影响章节保存
                    }
                }
                
                chapters.add(chapter);
            }
            
            // 批量保存章节
            List<Chapter> savedChapters = chapterRepository.saveAll(chapters);
            logger.info("成功保存章节数量: {}", savedChapters.size());
            
            // 生成小说整体摘要和分析结果
            String fullContent = fullContentBuilder.toString();
            if (!fullContent.isEmpty()) {
                try {
                    // 设置小说整体摘要
                    String overallSummary = nlpService.generateSummary(fullContent, 500);
                    novel.setOverallSummary(overallSummary);
                    logger.info("成功生成小说整体摘要，长度: {}", overallSummary.length());
                    
                    // 提取全文关键词
                    Map<String, Integer> keywords = nlpService.extractKeywords(fullContent, 20);
                    
                    // 识别人物
                    List<String> characters = nlpService.extractCharacters(fullContent);
                    
                    // 获取对话并分析人物关系
                    List<Map<String, String>> dialogues = nlpService.extractDialogues(fullContent);
                    List<Map<String, Object>> relationships = nlpService.analyzeCharacterRelationships(dialogues);
                    
                    // 生成角色发展摘要
                    if (!characters.isEmpty()) {
                        // 直接使用NLP服务生成角色摘要
                        String characterContent = characters.stream()
                                .map(character -> {
                                    StringBuilder context = new StringBuilder();
                                    context.append(character).append(": ");
                                    
                                    // 提取与该角色相关的内容片段
                                    String[] sentences = fullContent.split("[。！？.!?]");
                                    int count = 0;
                                    for (String sentence : sentences) {
                                        if (sentence.contains(character) && count < 10) {
                                            context.append(sentence).append("。");
                                            count++;
                                        }
                                    }
                                    return context.toString();
                                })
                                .limit(10) // 只处理前10个主要角色
                                .collect(Collectors.joining("\n\n"));
                        
                        String characterSummary = nlpService.generateSummary(characterContent, 1000);
                        novel.setCharacterDevelopmentSummary(characterSummary);
                        logger.info("成功生成角色发展摘要，长度: {}", characterSummary.length());
                    }
                    
                    // 生成世界观摘要
                    if (fullContent.length() > 0) {
                        // 限制处理内容长度
                        String processingContent = fullContent.length() > 50000 ?
                                fullContent.substring(0, 50000) : fullContent;
                        
                        String worldBuildingSummary = nlpService.generateWorldBuildingSummary(processingContent);
                        
                        // 验证生成的摘要是否合理
                        if (worldBuildingSummary != null && worldBuildingSummary.length() < 5000 &&
                                !worldBuildingSummary.contains("第1章") && !worldBuildingSummary.contains("章节")) {
                            novel.setWorldBuildingSummary(worldBuildingSummary);
                            logger.info("成功生成世界观摘要，长度: {}", worldBuildingSummary.length());
                        } else {
                            logger.warn("生成的世界观摘要异常，可能包含章节内容，设置默认摘要");
                            novel.setWorldBuildingSummary("世界观摘要生成失败，请稍后重试。");
                        }
                    }
                    
                    // 生成剧情进展摘要
                    if (fullContent.length() > 0) {
                        // 限制处理内容长度
                        String processingContent = fullContent.length() > 50000 ?
                                fullContent.substring(0, 50000) : fullContent;
                        
                        String plotProgressionSummary = nlpService.generatePlotProgressionSummary(processingContent);
                        
                        // 验证生成的摘要是否合理
                        if (plotProgressionSummary != null && plotProgressionSummary.length() < 5000 &&
                                !plotProgressionSummary.contains("第1章") && !plotProgressionSummary.contains("章节")) {
                            novel.setPlotProgressionSummary(plotProgressionSummary);
                            logger.info("成功生成情节发展摘要，长度: {}", plotProgressionSummary.length());
                        } else {
                            logger.warn("生成的情节发展摘要异常，可能包含章节内容，设置默认摘要");
                            novel.setPlotProgressionSummary("情节发展摘要生成失败，请稍后重试。");
                        }
                    }
                    
                } catch (Exception e) {
                    logger.error("生成小说摘要时出错: {}", e.getMessage(), e);
                }
            }
            
            // 更新小说处理状态
            novel.setProcessingStatus(Novel.ProcessingStatus.COMPLETED);
            novel.setProcessedChapters(chapters.size());
            novelRepository.save(novel);
            logger.info("更新小说处理状态为完成，已处理章节: {}", novel.getProcessedChapters());
            
            // 返回结果
            result.put("success", true);
            result.put("novelId", novel.getId());
            result.put("title", novel.getTitle());
            result.put("chapterCount", chapters.size());
            
        } catch (Exception e) {
            logger.error("导入小说失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "导入小说失败: " + e.getMessage());
            // 显式抛出异常，确保事务回滚
            throw new RuntimeException("导入小说失败", e);
        }
        
        return result;
    }
} 