package com.novelassistant.service.impl;

import com.novelassistant.entity.*;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.ChapterRepository;
import com.novelassistant.repository.TagRepository;
import com.novelassistant.repository.NovelCharacterRepository;
import com.novelassistant.repository.CharacterRelationshipRepository;
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
import java.util.stream.Collectors;

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
    
    @Autowired
    private NovelCharacterRepository characterRepository;
    
    @Autowired
    private CharacterRelationshipRepository relationshipRepository;
    
    @Autowired
    private com.novelassistant.repository.visualization.EmotionalDataRepository emotionalDataRepository;
    
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
        
        // 获取对话并分析人物关系
        List<Map<String, String>> dialogues = nlpService.extractDialogues(fullContent);
        List<Map<String, Object>> relationships = nlpService.analyzeCharacterRelationships(dialogues);
        
        // 保存角色和关系
        saveCharacterRelationships(novel, characters, relationships);
        
        // 生成角色发展摘要
        String characterSummary = generateCharacterSummary(fullContent, characters);
        novel.setCharacterDevelopmentSummary(characterSummary);
        
        // 生成世界观摘要
        novel.setWorldBuildingSummary("【世界观分析】\n小说中的世界背景描述。");
        
        // 生成剧情进展摘要
        novel.setPlotProgressionSummary("【剧情分析】\n小说中的主要情节发展和转折点。");
        
        // 为小说添加标签
        // 按关键词权重排序，取前5个作为标签
        keywords.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> addTag(novel, entry.getKey(), Tag.TagType.INFO));
        
        // 更新状态为完成
        novel.setProcessingStatus(Novel.ProcessingStatus.COMPLETED);
        novelRepository.save(novel);
        
        // 处理情感数据，标识情节高潮点
        identifyEmotionalPatterns(novel);
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
     * 生成角色发展摘要
     */
    private String generateCharacterSummary(String fullContent, List<String> characters) {
        if (characters.isEmpty()) {
            return "小说中未能识别明确的角色。";
        }
        
        // 获取所有对话
        List<Map<String, String>> dialogues = nlpService.extractDialogues(fullContent);
        
        // 提取角色关系 - 仅用于摘要生成，不再保存到数据库（已在processNovelFile中处理）
        List<Map<String, Object>> relationships = nlpService.analyzeCharacterRelationships(dialogues);
        
        StringBuilder summary = new StringBuilder();
        summary.append("主要角色：").append(String.join("、", characters.subList(0, Math.min(5, characters.size()))));
        summary.append("\n\n");
        
        // 为每个主要角色生成描述
        int limit = Math.min(10, characters.size());
        for (int i = 0; i < limit; i++) {
            String character = characters.get(i);
            
            // 提取与该角色相关的内容
            List<String> sentences = Arrays.asList(fullContent.split("[。！？.!?]"));
            List<String> characterSentences = new ArrayList<>();
            
            for (String sentence : sentences) {
                if (sentence.contains(character)) {
                    characterSentences.add(sentence);
                }
                
                if (characterSentences.size() >= 15) {
                    break;
                }
            }
            
            // 提取该角色的对话
            List<String> characterDialogues = dialogues.stream()
                .filter(d -> d.get("speaker").equals(character))
                .map(d -> d.get("content"))
                .limit(5)
                .collect(Collectors.toList());
            
            // 计算角色的互动关系
            List<String> characterRelations = relationships.stream()
                .filter(r -> r.get("character1").equals(character) || r.get("character2").equals(character))
                .map(r -> {
                    String other = r.get("character1").equals(character) ? 
                                  (String)r.get("character2") : (String)r.get("character1");
                    return other + "(" + r.get("relationship") + ")";
                })
                .limit(3)
                .collect(Collectors.toList());
            
            if (!characterSentences.isEmpty()) {
                // 为该角色生成摘要
                String characterContent = String.join("。", characterSentences) + "。";
                String charSummary = nlpService.generateSummary(characterContent, 100);
                
                summary.append(character).append("：").append(charSummary);
                
                // 添加角色对话示例
                if (!characterDialogues.isEmpty()) {
                    summary.append("\n典型对话：\"").append(characterDialogues.get(0)).append("\"");
                }
                
                // 添加角色关系
                if (!characterRelations.isEmpty()) {
                    summary.append("\n相关角色：").append(String.join("、", characterRelations));
                }
                
                summary.append("\n\n");
            }
        }
        
        return summary.toString();
    }
    
    /**
     * 保存角色关系到数据库
     * @param novel 小说对象
     * @param characters 识别出的角色列表
     * @param relationships 分析出的关系列表
     */
    private void saveCharacterRelationships(Novel novel, List<String> characters, List<Map<String, Object>> relationships) {
        // Only save if novel is not null
        if (novel == null) {
            return;
        }
        
        // 只处理前20个角色
        int limit = Math.min(20, characters.size());
        List<String> mainCharacters = characters.subList(0, limit);
        
        // 创建角色实体
        Map<String, NovelCharacter> characterEntities = new HashMap<>();
        for (String characterName : mainCharacters) {
            NovelCharacter characterEntity = new NovelCharacter();
            characterEntity.setName(characterName);
            characterEntity.setNovel(novel);
            characterEntity.setNovelId(novel.getId()); // 显式设置novelId字段
            characterEntity.setImportance(100 - characters.indexOf(characterName) * 5); // 根据角色排名设置重要性
            characterEntity.setDescription(generateCharacterDescription(characterName));
            
            // 保存到数据库
            characterRepository.save(characterEntity);
            characterEntities.put(characterName, characterEntity);
        }
        
        // 保存角色关系
        for (Map<String, Object> relationship : relationships) {
            String char1 = (String) relationship.get("character1");
            String char2 = (String) relationship.get("character2");
            String relationshipType = (String) relationship.get("relationship");
            Double confidence = (Double) relationship.get("confidence");
            
            // 如果两个角色都在主要角色列表中
            if (characterEntities.containsKey(char1) && characterEntities.containsKey(char2)) {
                CharacterRelationship relationEntity = new CharacterRelationship();
                relationEntity.setNovelId(novel.getId());
                relationEntity.setSourceCharacterId(characterEntities.get(char1).getId());
                relationEntity.setTargetCharacterId(characterEntities.get(char2).getId());
                relationEntity.setRelationshipType(relationshipType);
                relationEntity.setImportance(confidence != null ? confidence.intValue() : 1);
                relationEntity.setDescription(relationshipType);
                
                // 保存到数据库
                relationshipRepository.save(relationEntity);
            }
        }
    }
    
    /**
     * 为角色生成简短描述
     */
    private String generateCharacterDescription(String characterName) {
        // 这里可以根据角色在小说中的表现生成描述
        // 简单实现，返回默认描述
        return "小说中的角色：" + characterName;
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
        
        // 保存章节
        chapterRepository.save(chapter);
        
        // 使用NLP分析章节情感
        double emotionValue = nlpService.analyzeSentiment(content);
        
        // 创建情感数据对象
        com.novelassistant.entity.visualization.EmotionalData emotionalData = new com.novelassistant.entity.visualization.EmotionalData();
        emotionalData.setNovel(novel);
        emotionalData.setChapter(chapter);
        emotionalData.setChapterNumber(chapterNumber);
        emotionalData.setChapterTitle(title);
        
        // 将0-1范围的情感值转换为0-100范围以便展示
        emotionalData.setEmotionValue(emotionValue * 100);
        
        // 将章节摘要设置为事件描述
        emotionalData.setEventDescription(summaryText);
        
        // 根据内容特征判断是否为重要章节或情节高潮
        boolean isImportant = isImportantChapter(content, emotionValue);
        emotionalData.setIsImportant(isImportant);
        
        // 保存情感数据
        emotionalDataRepository.save(emotionalData);
    }
    
    /**
     * 判断章节是否为重要章节
     * 基于内容特征和情感值
     */
    private boolean isImportantChapter(String content, double emotionValue) {
        // 高情感值或低情感值通常对应重要情节点
        if (emotionValue > 0.8 || emotionValue < 0.2) {
            return true;
        }
        
        // 检查是否包含重要情节关键词
        String[] importantKeywords = {"战斗", "死亡", "相遇", "发现", "离别", "相爱", "失去", 
                                     "冲突", "危机", "转折", "告白", "决定", "选择", "背叛"};
        
        for (String keyword : importantKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void addTag(Novel novel, String name, Tag.TagType type) {
        Tag tag = new Tag(novel, name, type);
        tagRepository.save(tag);
    }
    
    /**
     * 分析小说情感数据，识别情节高潮点
     * @param novel 小说对象
     */
    private void identifyEmotionalPatterns(Novel novel) {
        // 获取按章节号排序的所有情感数据
        List<com.novelassistant.entity.visualization.EmotionalData> emotionalDataList = 
                emotionalDataRepository.findByNovelIdOrderByChapterNumberAsc(novel.getId());
        
        if (emotionalDataList.size() < 5) {
            return; // 章节太少，无法进行有效分析
        }
        
        // 分析情感曲线变化趋势，找出波峰和波谷
        List<com.novelassistant.entity.visualization.EmotionalData> peaks = new ArrayList<>();
        List<com.novelassistant.entity.visualization.EmotionalData> valleys = new ArrayList<>();
        
        // 简单的波峰波谷检测逻辑
        for (int i = 1; i < emotionalDataList.size() - 1; i++) {
            com.novelassistant.entity.visualization.EmotionalData prev = emotionalDataList.get(i - 1);
            com.novelassistant.entity.visualization.EmotionalData current = emotionalDataList.get(i);
            com.novelassistant.entity.visualization.EmotionalData next = emotionalDataList.get(i + 1);
            
            double prevVal = prev.getEmotionValue();
            double currentVal = current.getEmotionValue();
            double nextVal = next.getEmotionValue();
            
            // 如果当前值大于前后两个，则为波峰
            if (currentVal > prevVal && currentVal > nextVal) {
                peaks.add(current);
            }
            
            // 如果当前值小于前后两个，则为波谷
            if (currentVal < prevVal && currentVal < nextVal) {
                valleys.add(current);
            }
        }
        
        // 识别主要情节高潮区域（通常在小说的2/3处）
        int startIndex = (int)(emotionalDataList.size() * 0.55);
        int endIndex = (int)(emotionalDataList.size() * 0.85);
        
        // 在主要区域内找最高的波峰作为高潮点
        com.novelassistant.entity.visualization.EmotionalData mainClimaxPeak = null;
        double maxPeakValue = 0;
        
        for (com.novelassistant.entity.visualization.EmotionalData peak : peaks) {
            int chapterNum = peak.getChapterNumber();
            int chapterIndex = chapterNum - 1; // 假设章节号从1开始
            
            if (chapterIndex >= startIndex && chapterIndex <= endIndex && peak.getEmotionValue() > maxPeakValue) {
                maxPeakValue = peak.getEmotionValue();
                mainClimaxPeak = peak;
            }
        }
        
        // 如果找到了主要高潮点，则标记高潮开始和结束点
        if (mainClimaxPeak != null) {
            // 找主要高潮前的波谷作为高潮开始点
            com.novelassistant.entity.visualization.EmotionalData climaxStart = null;
            for (com.novelassistant.entity.visualization.EmotionalData valley : valleys) {
                if (valley.getChapterNumber() < mainClimaxPeak.getChapterNumber() && 
                    valley.getChapterNumber() > mainClimaxPeak.getChapterNumber() - 10) {
                    climaxStart = valley;
                    break;
                }
            }
            
            // 找主要高潮后的波谷作为高潮结束点
            com.novelassistant.entity.visualization.EmotionalData climaxEnd = null;
            for (com.novelassistant.entity.visualization.EmotionalData valley : valleys) {
                if (valley.getChapterNumber() > mainClimaxPeak.getChapterNumber() && 
                    valley.getChapterNumber() < mainClimaxPeak.getChapterNumber() + 10) {
                    climaxEnd = valley;
                    break;
                }
            }
            
            // 标记高潮开始和结束
            if (climaxStart != null) {
                climaxStart.setIsClimaxStart(true);
                emotionalDataRepository.save(climaxStart);
            }
            
            // 主要高潮点标记为重要章节
            mainClimaxPeak.setIsImportant(true);
            emotionalDataRepository.save(mainClimaxPeak);
            
            if (climaxEnd != null) {
                climaxEnd.setIsClimaxEnd(true);
                emotionalDataRepository.save(climaxEnd);
            }
        }
    }
} 