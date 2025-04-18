package com.novelassistant.service.impl;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Tag;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.ChapterRepository;
import com.novelassistant.repository.TagRepository;
import com.novelassistant.service.NlpService;
import com.novelassistant.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileInputStream;

@Service
public class NovelServiceImpl implements NovelService {

    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private NlpService nlpService;
    
    @Override
    public List<Novel> getAllNovels() {
        return novelRepository.findAll();
    }
    
    @Override
    public Novel getNovelById(Long id) {
        return novelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("小说不存在: " + id));
    }
    
    @Override
    public boolean existsNovelById(Long id) {
        return novelRepository.existsById(id);
    }
    
    @Override
    public Map<String, Object> getNovelStatus(Long id) {
        if (!existsNovelById(id)) {
            Map<String, Object> status = new HashMap<>();
            status.put("status", "NOT_FOUND");
            status.put("error", "小说不存在: " + id);
            return status;
        }
        
        Novel novel = novelRepository.findById(id).get(); // 这里可以安全地使用get()，因为已经检查了存在性
        
        Map<String, Object> status = new HashMap<>();
        status.put("status", novel.getProcessingStatus().name());
        status.put("processedChapters", novel.getProcessedChapters());
        status.put("totalChapters", novel.getTotalChapters());
        
        return status;
    }
    
    @Override
    @Transactional
    public Map<String, Object> processNovel(MultipartFile file, String title, String author, Long userId) {
        try {
            // 创建新小说记录
            Novel novel = new Novel(title, author);
            novel.setProcessingStatus(Novel.ProcessingStatus.PENDING);
            novel.setUserId(userId); // 设置用户ID
            novel = novelRepository.save(novel);
            
            // 将文件保存到临时目录，防止在异步处理前被删除
            File tempFile = File.createTempFile("novel_", ".tmp");
            file.transferTo(tempFile);
            final String tempFilePath = tempFile.getAbsolutePath();
            
            // 开始上传过程
            final Long novelId = novel.getId();
            
            // 使用新方法处理文件，避免使用原始Thread
            processNovelFileAsync(novelId, tempFilePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", novel.getId());
            response.put("status", novel.getProcessingStatus().name());
            response.put("message", "文件已上传，开始处理");
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("处理小说文件失败: " + e.getMessage(), e);
        }
    }
    
    // 添加异步方法处理小说文件
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processNovelFileAsync(Long novelId, String filePath) {
        try {
            processNovelFile(novelId, filePath);
            // 处理完成后删除临时文件
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException e) {
                // 无需处理
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Novel failedNovel = novelRepository.findById(novelId).orElse(null);
                if (failedNovel != null) {
                    failedNovel.setProcessingStatus(Novel.ProcessingStatus.FAILED);
                    novelRepository.save(failedNovel);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // 确保临时文件被删除
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException ex) {
                // 无需处理
            }
        }
    }
    
    @Override
    public List<Chapter> getChaptersByNovelId(Long novelId) {
        Novel novel = getNovelById(novelId);
        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novel.getId());
    }
    
    @Override
    public List<Tag> getTagsByNovelId(Long novelId) {
        Novel novel = getNovelById(novelId);
        return tagRepository.findByNovelId(novel.getId());
    }
    
    @Override
    public List<Novel> getNovelsByUserId(Long userId) {
        return novelRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional
    public boolean deleteNovel(Long id) {
        try {
            // 检查小说是否存在
            if (!existsNovelById(id)) {
                return false;
            }
            
            // 删除小说
            novelRepository.deleteById(id);
            // 章节和标签会通过级联删除自动删除，无需手动处理
            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除小说失败: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    protected void processNovelFile(Long novelId, String filePath) throws IOException {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));
        
        // 更新状态为处理中
        novel.setProcessingStatus(Novel.ProcessingStatus.PROCESSING);
        novelRepository.save(novel);
        
        // 读取整个文件内容，用于整体分析
        String fullContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        
        // 使用NLP服务检测章节分隔
        List<Integer> chapterPositions = nlpService.detectChapterBreaks(fullContent);
        
        // 如果没有找到章节分隔，使用默认方法
        if (chapterPositions.isEmpty()) {
            processFileByLines(novel, filePath);
        } else {
            processFileByChapters(novel, fullContent, chapterPositions);
        }
        
        // 提取全文关键词
        Map<String, Integer> keywords = nlpService.extractKeywords(fullContent, 20);
        
        // 生成小说摘要
        String overallSummary = nlpService.generateSummary(fullContent, 500);
        novel.setOverallSummary(overallSummary);
        
        // 识别人物
        List<String> characters = nlpService.extractCharacters(fullContent);
        
        // 生成世界观和角色发展摘要
        String worldBuildingSummary = generateWorldBuildingSummary(fullContent);
        novel.setWorldBuildingSummary(worldBuildingSummary);
        
        String characterSummary = generateCharacterSummary(fullContent, characters);
        novel.setCharacterDevelopmentSummary(characterSummary);
        
        // 提取主题作为标签
        Map<String, Double> topics = nlpService.extractTopics(fullContent, 5);
        for (Map.Entry<String, Double> topic : topics.entrySet()) {
            if (topic.getValue() > 0.3) { // 只添加权重较高的主题
                addTag(novel, topic.getKey(), Tag.TagType.INFO);
            }
        }
        
        // 基于关键词添加标签
        for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
            if (entry.getValue() > 70) { // 只添加权重较高的关键词
                addTag(novel, entry.getKey(), Tag.TagType.POSITIVE);
            }
        }
        
        // 更新状态为已完成
        novel.setProcessingStatus(Novel.ProcessingStatus.COMPLETED);
        novelRepository.save(novel);
    }
    
    /**
     * 按行处理文件（旧方法，当无法检测章节时使用）
     */
    private void processFileByLines(Novel novel, String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            String line;
            StringBuilder currentChapter = new StringBuilder();
            int lineCount = 0;
            int chapterCount = 0;
            String chapterTitle = "第1章";
            
            while ((line = reader.readLine()) != null) {
                // 检测章节标题
                if (line.trim().startsWith("第") && line.trim().contains("章")) {
                    // 保存上一章节（如果有内容）
                    if (currentChapter.length() > 0) {
                        saveChapterWithNlp(novel, chapterCount, chapterTitle, currentChapter.toString());
                        currentChapter = new StringBuilder();
                    }
                    
                    chapterCount++;
                    chapterTitle = line.trim();
                }
                
                currentChapter.append(line).append("\n");
                lineCount++;
                
                // 每处理100行更新一次进度
                if (lineCount % 100 == 0) {
                    novel.setProcessedChapters(chapterCount);
                    novelRepository.save(novel);
                }
            }
            
            // 保存最后一章
            if (currentChapter.length() > 0) {
                saveChapterWithNlp(novel, chapterCount, chapterTitle, currentChapter.toString());
            }
            
            // 更新小说元数据
            novel.setTotalChapters(chapterCount);
            novel.setProcessedChapters(chapterCount);
            novel.setDescription(generateDescription(novel));
        }
    }
    
    /**
     * 基于检测到的章节位置处理文件
     */
    private void processFileByChapters(Novel novel, String fullContent, List<Integer> chapterPositions) {
        int chapterCount = chapterPositions.size();
        
        // 处理每个章节
        for (int i = 0; i < chapterCount; i++) {
            int startPos = chapterPositions.get(i);
            int endPos = (i < chapterCount - 1) ? chapterPositions.get(i + 1) : fullContent.length();
            
            String chapterContent = fullContent.substring(startPos, endPos);
            String chapterTitle = extractChapterTitle(chapterContent);
            
            saveChapterWithNlp(novel, i + 1, chapterTitle, chapterContent);
            
            // 更新进度
            novel.setProcessedChapters(i + 1);
            novelRepository.save(novel);
        }
        
        // 更新小说元数据
        novel.setTotalChapters(chapterCount);
        novel.setProcessedChapters(chapterCount);
        novel.setDescription(generateDescription(novel));
    }
    
    /**
     * 从章节内容中提取标题
     */
    private String extractChapterTitle(String chapterContent) {
        String[] lines = chapterContent.split("\n", 2);
        if (lines.length > 0 && lines[0].trim().startsWith("第") && lines[0].trim().contains("章")) {
            return lines[0].trim();
        }
        return "未命名章节";
    }
    
    /**
     * 生成小说描述
     */
    private String generateDescription(Novel novel) {
        StringBuilder description = new StringBuilder();
        description.append("《").append(novel.getTitle()).append("》");
        
        if (novel.getAuthor() != null && !novel.getAuthor().isEmpty()) {
            description.append("，作者: ").append(novel.getAuthor());
        }
        
        description.append("，共").append(novel.getTotalChapters()).append("章");
        
        // 如果有摘要，添加摘要的一部分
        if (novel.getOverallSummary() != null && !novel.getOverallSummary().isEmpty()) {
            String summary = novel.getOverallSummary();
            if (summary.length() > 100) {
                summary = summary.substring(0, 100) + "...";
            }
            description.append("。").append(summary);
        }
        
        return description.toString();
    }
    
    /**
     * 使用NLP服务生成世界观摘要
     */
    private String generateWorldBuildingSummary(String fullContent) {
        // 提取与世界观相关的句子
        List<String> sentences = Arrays.asList(fullContent.split("[。！？.!?]"));
        List<String> worldBuildingSentences = new ArrayList<>();
        
        // 世界观相关的关键词
        String[] worldKeywords = {"世界", "宇宙", "国家", "王国", "帝国", "城市", "山脉", "大陆", "星球", 
                                  "时代", "年代", "历史", "文明", "种族", "魔法", "科技", "规则", "法则"};
        
        for (String sentence : sentences) {
            for (String keyword : worldKeywords) {
                if (sentence.contains(keyword)) {
                    worldBuildingSentences.add(sentence);
                    break;
                }
            }
            
            if (worldBuildingSentences.size() >= 30) {
                break;
            }
        }
        
        if (worldBuildingSentences.isEmpty()) {
            return "该小说未包含明确的世界观设定描述。";
        }
        
        // 连接句子后使用NLP生成摘要
        String worldContent = String.join("。", worldBuildingSentences) + "。";
        return nlpService.generateSummary(worldContent, 300);
    }
    
    /**
     * 使用NLP服务生成角色发展摘要
     */
    private String generateCharacterSummary(String fullContent, List<String> characters) {
        if (characters.isEmpty()) {
            return "未能识别出明确的角色。";
        }
        
        StringBuilder characterSummary = new StringBuilder();
        characterSummary.append("主要角色包括：");
        
        // 只取前5个角色
        List<String> mainCharacters = characters.size() > 5 ? 
                characters.subList(0, 5) : characters;
        
        characterSummary.append(String.join("、", mainCharacters)).append("。");
        
        // 提取每个主要角色相关的句子
        for (String character : mainCharacters) {
            List<String> sentences = Arrays.asList(fullContent.split("[。！？.!?]"));
            List<String> characterSentences = new ArrayList<>();
            
            for (String sentence : sentences) {
                if (sentence.contains(character)) {
                    characterSentences.add(sentence);
                }
                
                if (characterSentences.size() >= 10) {
                    break;
                }
            }
            
            if (!characterSentences.isEmpty()) {
                String characterContent = String.join("。", characterSentences) + "。";
                String charSummary = nlpService.generateSummary(characterContent, 100);
                characterSummary.append(character).append("：").append(charSummary).append("\n");
            }
        }
        
        return characterSummary.toString();
    }
    
    /**
     * 使用NLP增强的章节保存方法
     */
    private void saveChapterWithNlp(Novel novel, int chapterNumber, String title, String content) {
        Chapter chapter = new Chapter(novel, chapterNumber, title);
        chapter.setContent(content);
        
        // 使用NLP生成摘要
        String summaryText = nlpService.generateSummary(content, 200);
        chapter.setSummary(summaryText);
        
        // 使用NLP提取关键词
        Map<String, Integer> keywordMap = nlpService.extractKeywords(content, 10);
        List<String> keywords = new ArrayList<>(keywordMap.keySet());
        chapter.setKeywords(keywords);
        
        chapterRepository.save(chapter);
    }
    
    private void addTag(Novel novel, String name, Tag.TagType type) {
        Tag tag = new Tag(novel, name, type);
        tagRepository.save(tag);
    }
} 