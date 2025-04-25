package com.novelassistant.service.impl;

import com.novelassistant.entity.*;
import com.novelassistant.entity.visualization.EmotionalData;
import com.novelassistant.repository.*;
import com.novelassistant.repository.visualization.*;
import com.novelassistant.service.NlpService;
import com.novelassistant.service.NovelService;
import com.novelassistant.service.ProcessingService;
import com.novelassistant.service.AnalyzationService;
import com.novelassistant.service.TaggingService;
import com.novelassistant.exception.DataAccessException;
import com.novelassistant.exception.ServiceException;
import com.novelassistant.exception.CharacterRelationshipException;
import com.novelassistant.util.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.net.URL;

// 添加import java.util.Objects
import java.util.Objects;

// Add these imports at the top of the file
import java.time.format.DateTimeFormatter;
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
    
    @Autowired
    private ProcessingService processingService;
    
    @Autowired
    private AnalyzationService analyzationService;
    
    @Autowired
    private TaggingService taggingService;
    
    @Autowired
    private ExecutorService executorService;
    
    private static final Logger logger = LoggerFactory.getLogger(NovelServiceImpl.class);
    
    @Override
    public List<Novel> getAllNovels() {
        return novelRepository.findByIsDeletedFalse();
    }
    
    @Override
    public Novel getNovelById(Long id) {
        return novelRepository.findByIdAndIsDeletedFalse(id)
                .orElse(null);
    }
    
    @Override
    public boolean existsNovelById(Long id) {
        return novelRepository.findByIdAndIsDeletedFalse(id).isPresent();
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
    
    /**
     * 异步处理小说文件
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processNovelFileAsync(Long novelId, String filePath) {
        try {
            processNovelFile(novelId, filePath);
            
            // 处理完成后，进行额外的分析步骤
            Novel novel = novelRepository.findById(novelId).orElse(null);
            if (novel != null) {
                // 设置状态为已完成
                novel.setProcessingStatus(Novel.ProcessingStatus.COMPLETED);
                novelRepository.save(novel);
                
                // 调用分析服务进行更深入的分析
                try {
                    Map<String, Object> analysisResult = analyzationService.analyzeNovel(novelId);
                    logger.info("Novel analysis completed for novel ID: {}, found {} data points", 
                            novelId, analysisResult.size());
                } catch (Exception e) {
                    logger.error("Novel analysis failed for novel ID: {}, error: {}", novelId, e.getMessage(), e);
                }
                
                // 生成标签
                try {
                    taggingService.generateTagsForNovel(novelId);
                } catch (Exception e) {
                    logger.error("Tag generation failed for novel ID: {}, error: {}", novelId, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process novel file: {}, error: {}", filePath, e.getMessage(), e);
            try {
                // 更新状态为失败
                updateNovelStatus(novelId, Novel.ProcessingStatus.FAILED);
            } catch (Exception ex) {
                logger.error("Failed to update novel status: {}", ex.getMessage(), ex);
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
        return novelRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    /**
     * 从URL导入小说
     * 该方法在扩展实现类中实现，此处为空实现
     *
     * @param url         小说URL
     * @param userId      用户ID
     * @param maxChapters 最大章节数限制，0表示不限制
     * @return 处理结果
     */
    @Override
    public Map<String, Object> importNovelFromUrl(String url, Long userId, int maxChapters) {
        // 调用新方法，保持兼容性
        return importNovelFromUrl(url, userId, null, null, maxChapters);
    }

    /**
     * 从URL导入小说（带标题和作者参数）
     * 该方法在扩展实现类中实现，此处为空实现
     *
     * @param url         小说URL
     * @param userId      用户ID
     * @param title       可选的小说标题
     * @param author      可选的小说作者
     * @param maxChapters 最大章节数限制，0表示不限制
     * @return 处理结果
     */
    @Override
    public Map<String, Object> importNovelFromUrl(String url, Long userId, String title, String author, int maxChapters) {
        // 默认实现返回错误信息，实际功能由NovelServiceExtImpl实现
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "未实现此功能");
        return result;
    }
    
    @Override
    @Transactional
    public boolean deleteNovel(Long id) {
        try {
            // 检查小说是否存在
            Optional<Novel> novelOpt = novelRepository.findByIdAndIsDeletedFalse(id);
            if (!novelOpt.isPresent()) {
                return false;
            }
            
            // 软删除小说（更新标志位）
            Novel novel = novelOpt.get();
            novel.setIsDeleted(true);
            novelRepository.save(novel);
            
            logger.info("小说已软删除: ID={}, 标题={}", id, novel.getTitle());
            return true;
        } catch (Exception e) {
            logger.error("删除小说失败: ", e);
            throw new RuntimeException("删除小说失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public boolean restoreNovel(Long id) {
        try {
            // 检查小说是否存在且已被标记为删除
            Optional<Novel> novelOpt = novelRepository.findById(id);
            if (!novelOpt.isPresent() || !novelOpt.get().getIsDeleted()) {
                return false;
            }
            
            // 恢复小说（更新标志位）
            Novel novel = novelOpt.get();
            novel.setIsDeleted(false);
            novelRepository.save(novel);
            
            logger.info("小说已恢复: ID={}, 标题={}", id, novel.getTitle());
            return true;
        } catch (Exception e) {
            logger.error("恢复小说失败: ", e);
            throw new RuntimeException("恢复小说失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Novel> getDeletedNovelsByUserId(Long userId) {
        return novelRepository.findAll().stream()
                .filter(novel -> novel.getUserId().equals(userId) && novel.getIsDeleted())
                .collect(Collectors.toList());
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
        logger.info("生成世界观摘要");
        if (fullContent != null && fullContent.length() > 0) {
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
        } else {
            novel.setWorldBuildingSummary("无法生成世界观摘要，小说内容为空。");
        }
        
        // 生成剧情进展摘要
        logger.info("生成情节发展摘要");
        if (fullContent != null && fullContent.length() > 0) {
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
        } else {
            novel.setPlotProgressionSummary("无法生成情节发展摘要，小说内容为空。");
        }
        
        // 为小说添加标签 - 增强版标签生成
        generateSmartTags(novel, keywords, fullContent, characters, relationships);
        
        // 更新状态为完成
        novel.setProcessingStatus(Novel.ProcessingStatus.COMPLETED);
        novelRepository.save(novel);
        
        // 处理情感数据，标识情节高潮点
        identifyEmotionalPatterns(novel);
    }
    
    /**
     * 智能标签生成
     * 基于内容分析结果智能生成多种类型的标签
     */
    private void generateSmartTags(Novel novel, Map<String, Integer> keywords, String fullContent, 
                                  List<String> characters, List<Map<String, Object>> relationships) {
        // 1. 基于内容关键词生成标签（取前10个，根据内容特征自动判断类型）
        keywords.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String keyword = entry.getKey();
                    Tag.TagType type = determineTagType(keyword, fullContent);
                    addTag(novel, keyword, type);
                });
        
        // 2. 基于情感分析生成POSITIVE和WARNING标签
        generateEmotionBasedTags(novel, fullContent);
        
        // 3. 基于角色分析生成标签
        generateCharacterBasedTags(novel, characters, relationships);
        
        // 4. 基于内容长度和结构生成标签
        generateStructureBasedTags(novel, fullContent);
        
        // 5. 生成内容引导标签（新增）
        generateContentGuideTags(novel, fullContent);
    }
    
    /**
     * 根据关键词和上下文自动判断标签类型
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
        
        // 然后尝试分析关键词在全文中的上下文
        // 提取关键词所在的短句（简单实现）
        List<String> sentences = new ArrayList<>();
        int lastIndex = 0;
        int index;
        while ((index = context.indexOf(keyword, lastIndex)) != -1) {
            int sentenceStart = Math.max(0, index - 50);
            int sentenceEnd = Math.min(context.length(), index + keyword.length() + 50);
            String sentence = context.substring(sentenceStart, sentenceEnd);
            sentences.add(sentence);
            lastIndex = index + keyword.length();
            
            // 最多分析5个句子
            if (sentences.size() >= 5) {
                break;
            }
        }
        
        // 对每个句子进行情感分析
        double totalSentiment = 0;
        int sentenceCount = 0;
        for (String sentence : sentences) {
            double sentiment = nlpService.analyzeSentiment(sentence);
            totalSentiment += sentiment;
            sentenceCount++;
        }
        
        // 根据平均情感值判断标签类型
        if (sentenceCount > 0) {
            double avgSentiment = totalSentiment / sentenceCount;
            if (avgSentiment > 0.6) {
                return Tag.TagType.POSITIVE;
            } else if (avgSentiment < 0.4) {
                return Tag.TagType.WARNING;
            }
        }
        
        // 默认返回INFO类型
        return Tag.TagType.INFO;
    }
    
    /**
     * 生成内容引导类标签
     */
    private void generateContentGuideTags(Novel novel, String content) {
        // 难度标签
        String[] complexWords = {"哲学", "形而上", "抽象", "理论", "概念", "复杂", "技术", "专业", "深奥"};
        int complexCount = countOccurrences(content, complexWords);
        if (complexCount > 20) {
            addTag(novel, "阅读难度高", Tag.TagType.WARNING);
        } else if (complexCount < 5) {
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
     * 基于情感分析生成标签
     */
    private void generateEmotionBasedTags(Novel novel, String content) {
        // 分析整体情感
        double overallSentiment = nlpService.analyzeSentiment(content);
        
        // 获取所有章节
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novel.getId());
        
        // 分析情感波动
        if (chapters.size() >= 3) {
            List<Double> sentiments = new ArrayList<>();
            for (Chapter chapter : chapters) {
                if (chapter.getContent() != null && !chapter.getContent().isEmpty()) {
                    double sentiment = nlpService.analyzeSentiment(chapter.getContent());
                    sentiments.add(sentiment);
                }
            }
            
            // 计算情感波动度（方差）
            double avgSentiment = sentiments.stream().mapToDouble(d -> d).average().orElse(0.5);
            double variance = sentiments.stream()
                    .mapToDouble(d -> Math.pow(d - avgSentiment, 2))
                    .average().orElse(0);
            
            // 情感波动大
            if (variance > 0.04) { // 设定阈值
                addTag(novel, "情节起伏大", Tag.TagType.INFO);
                addTag(novel, "情感丰富", Tag.TagType.POSITIVE);
            }
            
            // 情感持续上升或下降
            boolean ascending = true;
            boolean descending = true;
            
            for (int i = 1; i < sentiments.size(); i++) {
                if (sentiments.get(i) < sentiments.get(i - 1)) {
                    ascending = false;
                }
                if (sentiments.get(i) > sentiments.get(i - 1)) {
                    descending = false;
                }
            }
            
            if (ascending && sentiments.size() > 3) {
                addTag(novel, "情感逐渐高涨", Tag.TagType.POSITIVE);
            }
            
            if (descending && sentiments.size() > 3) {
                addTag(novel, "情感逐渐低沉", Tag.TagType.WARNING);
            }
        }
        
        // 根据整体情感基调生成标签
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
        }
        
        if (relationships.size() > characters.size() * 1.5) {
            addTag(novel, "人物关系复杂", Tag.TagType.INFO);
            
            // 关系复杂可能是优点也可能是缺点，这里基于复杂度判断
            if (relationships.size() > characters.size() * 3) {
                addTag(novel, "人际关系混乱", Tag.TagType.WARNING);
            } else {
                addTag(novel, "人物互动丰富", Tag.TagType.POSITIVE);
            }
        }
        
        // 检查是否有重要角色（Top 3）
        if (characters.size() >= 3) {
            addTag(novel, "角色形象鲜明", Tag.TagType.POSITIVE);
        }
    }
    
    /**
     * 基于内容长度和结构生成标签
     */
    private void generateStructureBasedTags(Novel novel, String content) {
        // 内容长度相关标签
        int wordCount = content.length();
        
        if (wordCount > 300000) { // 约30万字以上
            addTag(novel, "长篇巨著", Tag.TagType.INFO);
            addTag(novel, "篇幅较长", Tag.TagType.WARNING);
        } else if (wordCount > 100000) { // 约10万字以上
            addTag(novel, "中长篇", Tag.TagType.INFO);
        } else if (wordCount < 30000) { // 约3万字以下
            addTag(novel, "短篇", Tag.TagType.INFO);
            addTag(novel, "篇幅较短", Tag.TagType.INFO);
        }
        
        // 章节长度分析
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novel.getId());
        if (chapters.size() > 0) {
            int totalChapters = chapters.size();
            double avgChapterLength = chapters.stream()
                    .filter(c -> c.getContent() != null)
                    .mapToInt(c -> c.getContent().length())
                    .average()
                    .orElse(0);
            
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
        if (characters == null || characters.isEmpty()) {
            return "小说中未能识别明确的角色。";
        }
        
        try {
            // 验证内容有效性
            if (fullContent == null || fullContent.isEmpty()) {
                logger.warn("生成角色摘要时，内容为空");
                return "无法生成角色摘要，内容为空。";
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
                    .filter(d -> d.get("speaker") != null && d.get("speaker").equals(character))
                    .map(d -> d.get("content"))
                    .filter(Objects::nonNull)
                    .limit(5)
                    .collect(Collectors.toList());
                
                // 计算角色的互动关系
                List<String> characterRelations = relationships.stream()
                    .filter(r -> r.get("character1") != null && r.get("character2") != null && 
                            (r.get("character1").equals(character) || r.get("character2").equals(character)))
                    .map(r -> {
                        String other = r.get("character1").equals(character) ? 
                                    (String) r.get("character2") : (String) r.get("character1");
                        return other + "(" + r.get("relationship") + ")";
                    })
                    .limit(3)
                    .collect(Collectors.toList());
                
                if (!characterSentences.isEmpty()) {
                    try {
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
                    } catch (Exception e) {
                        logger.warn("为角色 {} 生成摘要时出错: {}", character, e.getMessage());
                        summary.append(character).append("：无法生成摘要\n\n");
                    }
                }
            }
            
            String result = summary.toString();
            
            // 验证摘要长度，避免异常情况
            if (result.length() > 10000) {
                logger.warn("角色摘要过长 ({} 字符)，截断处理", result.length());
                result = result.substring(0, 10000) + "...(摘要过长，已截断)";
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("生成角色发展摘要时出错: {}", e.getMessage(), e);
            return "生成角色发展摘要时出错，请稍后重试。";
        }
    }
    
    /**
     * 保存角色关系到数据库
     *
     * @param novel         小说对象
     * @param characters    识别出的角色列表
     * @param relationships 分析出的关系列表
     */
    private void saveCharacterRelationships(Novel novel, List<String> characters, List<Map<String, Object>> relationships) {
        // Only save if novel is not null
        if (novel == null) {
            logger.warn("保存角色关系失败: 小说对象为空");
            throw new ServiceException("保存角色关系失败: 小说对象为空", ErrorCodes.NOVEL_MISSING);
        }
        
        // 检查角色数据
        if (characters == null || characters.isEmpty()) {
            logger.warn("保存角色关系失败: 未识别到角色数据，小说ID: {}", novel.getId());
            
            // 尝试从数据库中查找现有角色
            List<NovelCharacter> existingCharacters = characterRepository.findByNovelId(novel.getId());
            if (!existingCharacters.isEmpty()) {
                logger.info("发现该小说已有 {} 个角色数据，跳过保存", existingCharacters.size());
            return;
        }
        
            throw new CharacterRelationshipException("未识别到角色数据", novel.getId());
        }
        
        // 检查关系数据
        if (relationships == null) {
            logger.warn("保存角色关系失败: 关系数据为空，小说ID: {}", novel.getId());
            throw new CharacterRelationshipException("关系数据为空", novel.getId());
        }
        
        // 提高处理的角色数量上限
        int limit = Math.min(50, characters.size());
        List<String> mainCharacters = characters.subList(0, limit);
        
        // 记录初始数据
        logger.info("开始保存角色关系数据，小说ID: {}，角色数量: {}，关系数量: {}", 
                   novel.getId(), mainCharacters.size(), relationships.size());
        
        // 创建角色实体
        Map<String, NovelCharacter> characterEntities = new HashMap<>();
        
        try {
        for (String characterName : mainCharacters) {
                // 跳过空名称或过短的名称
                if (characterName == null || characterName.length() < 2) {
                    logger.debug("跳过无效角色名: {}", characterName);
                    continue;
                }
                
                // 检查是否已存在该角色
                try {
                    List<NovelCharacter> existingCharacters = characterRepository.findByNovelIdAndName(novel.getId(), characterName);
                    if (!existingCharacters.isEmpty()) {
                        // 如果已存在该角色，使用已有角色
                        characterEntities.put(characterName, existingCharacters.get(0));
                        logger.debug("使用已存在角色: {}, ID: {}", characterName, existingCharacters.get(0).getId());
                        continue;
                    }
                } catch (Exception e) {
                    logger.warn("查询角色时出错: {}, 错误: {}", characterName, e.getMessage());
                    // 继续创建新角色
                }
                
            NovelCharacter characterEntity = new NovelCharacter();
            characterEntity.setName(characterName);
            characterEntity.setNovel(novel);
                characterEntity.setNovelId(novel.getId());
                
                // 根据角色在列表中的位置设置重要性 - 保证前10个角色有较高重要性
                int importance = 100;
                int idx = characters.indexOf(characterName);
                if (idx >= 0) {
                    importance = idx < 10 ? 100 - idx * 5 : 50 - (idx - 10) / 2;
                }
                characterEntity.setImportance(Math.max(10, importance)); // 重要性最低为10
                
            characterEntity.setDescription(generateCharacterDescription(characterName));
                // 前10个为主要角色，其余为次要角色
                characterEntity.setCategory(idx < 10 ? "主要角色" : "次要角色");
            
                try {
            // 保存到数据库
                    NovelCharacter saved = characterRepository.save(characterEntity);
                    characterEntities.put(characterName, saved);
                    logger.debug("保存角色成功: {}, ID: {}", characterName, saved.getId());
                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                    // 处理数据完整性冲突异常（如唯一约束违反）
                    logger.warn("保存角色时发生数据完整性冲突: {}, 错误: {}", characterName, e.getMessage());
                    
                    // 尝试再次查询已存在角色
                    List<NovelCharacter> retryExistingCharacters = characterRepository.findByNovelIdAndName(novel.getId(), characterName);
                    if (!retryExistingCharacters.isEmpty()) {
                        characterEntities.put(characterName, retryExistingCharacters.get(0));
                        logger.debug("冲突后使用已存在角色: {}, ID: {}", characterName, retryExistingCharacters.get(0).getId());
                    }
                } catch (Exception e) {
                    logger.error("保存角色失败: {}, 错误: {}", characterName, e.getMessage(), e);
                    // 继续处理其他角色，不抛出异常中断整个过程
                }
            }
        } catch (Exception e) {
            logger.error("保存角色过程中发生错误: {}", e.getMessage(), e);
            throw new DataAccessException("保存角色数据失败", e, ErrorCodes.CHARACTER_SAVE_ERROR);
        }
        
        // 检查角色实体是否为空
        if (characterEntities.isEmpty()) {
            logger.warn("没有成功保存任何角色实体，无法继续保存关系，小说ID: {}", novel.getId());
            throw new CharacterRelationshipException("保存角色失败，无法继续处理关系数据", novel.getId());
        }
        
        // 保存角色关系
        int savedRelationships = 0;
        
        // 如果关系数据为空或太少，尝试自动生成一些基本关系
        if (relationships.isEmpty() && characterEntities.size() > 1) {
            logger.info("关系数据为空，尝试自动生成基本关系，小说ID: {}", novel.getId());
            relationships = generateBasicRelationships(new ArrayList<>(characterEntities.keySet()));
        }
        
        try {
        for (Map<String, Object> relationship : relationships) {
            String char1 = (String) relationship.get("character1");
            String char2 = (String) relationship.get("character2");
                
                // 跳过空的角色名
                if (char1 == null || char2 == null || char1.equals(char2)) {
                    logger.debug("跳过无效关系: {} → {}", char1, char2);
                    continue;
                }
                
            String relationshipType = (String) relationship.get("relationship");
                if (relationshipType == null || relationshipType.isEmpty()) {
                    relationshipType = "角色关系"; // 提供默认关系类型
                }
                
                Double confidence = 50.0;
                if (relationship.get("confidence") instanceof Double) {
                    confidence = (Double) relationship.get("confidence");
                } else if (relationship.get("confidence") instanceof Integer) {
                    confidence = ((Integer) relationship.get("confidence")).doubleValue();
                }
            
                // 如果两个角色都在角色实体列表中
            if (characterEntities.containsKey(char1) && characterEntities.containsKey(char2)) {
                CharacterRelationship relationEntity = new CharacterRelationship();
                relationEntity.setNovelId(novel.getId());
                relationEntity.setSourceCharacterId(characterEntities.get(char1).getId());
                relationEntity.setTargetCharacterId(characterEntities.get(char2).getId());
                relationEntity.setRelationshipType(relationshipType);
                    relationEntity.setImportance(confidence != null ? confidence.intValue() : 30);
                relationEntity.setDescription(relationshipType);
                    
                    try {
                        // 检查是否已存在相同关系
                        List<CharacterRelationship> existingRelations = relationshipRepository.findBySourceCharacterIdAndTargetCharacterId(
                            relationEntity.getSourceCharacterId(), relationEntity.getTargetCharacterId());
                        
                        if (!existingRelations.isEmpty()) {
                            logger.debug("关系已存在: {} → {}, 跳过保存", char1, char2);
                            savedRelationships++;
                            continue;
                        }
                
                // 保存到数据库
                        CharacterRelationship saved = relationshipRepository.save(relationEntity);
                        savedRelationships++;
                        logger.debug("保存角色关系成功: {} → {}, ID: {}", char1, char2, saved.getId());
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // 处理数据完整性冲突异常（如外键约束违反）
                        logger.warn("保存角色关系时发生数据完整性冲突: {} → {}, 错误: {}", 
                                  char1, char2, e.getMessage());
                        // 继续处理其他关系
                    } catch (Exception e) {
                        logger.error("保存角色关系失败: {} → {}, 错误: {}", char1, char2, e.getMessage(), e);
                        // 继续处理其他关系，不抛出异常中断整个过程
                    }
                } else {
                    logger.debug("角色不在实体列表中，跳过关系: {} → {}", char1, char2);
                }
            }
        } catch (Exception e) {
            logger.error("保存角色关系过程中发生错误: {}", e.getMessage(), e);
            throw new DataAccessException("保存角色关系数据失败", e, ErrorCodes.RELATIONSHIP_SAVE_ERROR);
        }
        
        logger.info("完成保存角色关系数据，小说ID: {}，成功保存角色: {}，成功保存关系: {}", 
                   novel.getId(), characterEntities.size(), savedRelationships);
        
        // 检查是否成功保存了关系
        if (savedRelationships == 0 && !relationships.isEmpty()) {
            logger.warn("未能成功保存任何角色关系，尽管有 {} 条关系数据", relationships.size());
        }
    }
    
    /**
     * 当没有足够的关系数据时，生成基本的角色关系
     *
     * @param characters 角色列表
     * @return 生成的基本关系
     */
    private List<Map<String, Object>> generateBasicRelationships(List<String> characters) {
        List<Map<String, Object>> basicRelationships = new ArrayList<>();
        
        // 仅处理前10个主要角色
        int limit = Math.min(10, characters.size());
        List<String> mainChars = characters.subList(0, limit);
        
        // 常见关系类型
        String[] relationTypes = {"朋友", "熟人", "同伴", "合作", "认识"};
        Random random = new Random();
        
        // 为主要角色两两创建关系
        for (int i = 0; i < mainChars.size(); i++) {
            for (int j = i + 1; j < mainChars.size(); j++) {
                String char1 = mainChars.get(i);
                String char2 = mainChars.get(j);
                
                // 随机选择关系类型
                String relationType = relationTypes[random.nextInt(relationTypes.length)];
                
                // 创建关系
                Map<String, Object> relation = new HashMap<>();
                relation.put("character1", char1);
                relation.put("character2", char2);
                relation.put("relationship", relationType);
                relation.put("confidence", 50.0); // 中等置信度
                
                basicRelationships.add(relation);
                
                // 创建反向关系
                Map<String, Object> reverseRelation = new HashMap<>();
                reverseRelation.put("character1", char2);
                reverseRelation.put("character2", char1);
                reverseRelation.put("relationship", relationType);
                reverseRelation.put("confidence", 50.0);
                
                basicRelationships.add(reverseRelation);
            }
        }
        
        logger.info("自动生成了 {} 个基本角色关系", basicRelationships.size());
        return basicRelationships;
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
     *
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
        int startIndex = (int) (emotionalDataList.size() * 0.55);
        int endIndex = (int) (emotionalDataList.size() * 0.85);
        
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
    
    /**
     * 更新小说标签
     * 可以定期调用此方法更新小说的标签
     *
     * @param novelId 小说ID
     * @return 更新的标签数量
     */
    @Override
    @Transactional
    public int refreshNovelTags(Long novelId) {
        // 获取小说
        Novel novel = getNovelById(novelId);
        
        // 只处理已完成处理的小说
        if (novel.getProcessingStatus() != Novel.ProcessingStatus.COMPLETED) {
            return 0;
        }
        
        // 获取现有标签
        List<Tag> existingTags = tagRepository.findByNovelId(novelId);
        
        // 获取章节内容
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        if (chapters.isEmpty()) {
            return 0;
        }
        
        // 合并章节内容获取全文
        StringBuilder fullTextBuilder = new StringBuilder();
        for (Chapter chapter : chapters) {
            if (chapter.getContent() != null) {
                fullTextBuilder.append(chapter.getContent()).append("\n");
            }
        }
        String fullContent = fullTextBuilder.toString();
        
        // 提取关键词
        Map<String, Integer> keywords = nlpService.extractKeywords(fullContent, 20);
        
        // 提取角色
        List<String> characters = nlpService.extractCharacters(fullContent);
        
        // 提取对话和关系
        List<Map<String, String>> dialogues = nlpService.extractDialogues(fullContent);
        List<Map<String, Object>> relationships = nlpService.analyzeCharacterRelationships(dialogues);
        
        // 删除旧标签
        tagRepository.deleteAll(existingTags);
        
        // 重新生成标签
        generateSmartTags(novel, keywords, fullContent, characters, relationships);
        
        // 获取更新后的标签数
        return (int) tagRepository.countByNovelId(novelId);
    }

    /**
     * 批量更新所有小说标签
     * 可以作为定时任务运行
     *
     * @return 更新的小说数量
     */
    @Override
    @Transactional
    @Async
    public int refreshAllNovelTags() {
        // 获取所有已处理完成的小说
        List<Novel> completedNovels = novelRepository.findAll().stream()
                .filter(n -> n.getProcessingStatus() == Novel.ProcessingStatus.COMPLETED)
                .collect(Collectors.toList());
        int updatedCount = 0;
        
        for (Novel novel : completedNovels) {
            try {
                int tagCount = refreshNovelTags(novel.getId());
                if (tagCount > 0) {
                    updatedCount++;
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他小说
                logger.error("更新小说标签失败，小说ID: " + novel.getId(), e);
            }
        }
        
        return updatedCount;
    }
    
    /**
     * 更新小说状态为错误状态并设置错误信息
     */
    private void updateNovelWithError(Long novelId, String errorMessage) {
        try {
            Optional<Novel> optionalNovel = novelRepository.findById(novelId);
            if (optionalNovel.isPresent()) {
                Novel novel = optionalNovel.get();
                novel.setProcessingStatus(Novel.ProcessingStatus.FAILED);
                novel.setDescription(errorMessage);
                novel.setUpdatedAt(new Date());
                novelRepository.save(novel);
                logger.info("已更新小说 {} 状态为错误: {}", novelId, errorMessage);
            } else {
                logger.error("无法找到小说进行错误状态更新, ID: {}", novelId);
            }
        } catch (Exception e) {
            logger.error("更新小说错误状态时出错, ID: {}, 错误: {}", novelId, e.getMessage(), e);
        }
    }
    
    /**
     * 截断标题，确保不超过数据库字段长度限制
     * 数据库中title字段为varchar(255)
     */
    private String truncateTitle(String title) {
        if (title == null) {
            return "未命名章节";
        }
        
        // 限制标题最大长度为255字符
        final int MAX_TITLE_LENGTH = 255;
        if (title.length() > MAX_TITLE_LENGTH) {
            logger.warn("章节标题过长，已截断：{}", title);
            // 简单截断，保留前面部分和省略号
            return title.substring(0, MAX_TITLE_LENGTH - 3) + "...";
        }
        
        return title;
    }
    
    // 辅助方法 - 更新小说处理状态
    private void updateNovelStatus(Long novelId, Novel.ProcessingStatus status) {
        try {
            Optional<Novel> optionalNovel = novelRepository.findById(novelId);
            if (optionalNovel.isPresent()) {
                Novel novel = optionalNovel.get();
                novel.setProcessingStatus(status);
                novel.setUpdatedAt(new Date());
                novelRepository.save(novel);
            }
        } catch (Exception e) {
            logger.error("Error updating novel status for novel {}: {}", novelId, e.getMessage(), e);
        }
    }

    /**
     * 检测小说类型/流派
     * 基于内容分析返回可能的小说类型
     */
    private String detectNovelGenre(String content) {
        // 简单实现，根据关键词判断小说类型
        if (content.contains("魔法") || content.contains("仙侠") || content.contains("修真") || 
            content.contains("修仙") || content.contains("道法") || content.contains("法术")) {
            return "仙侠/修真";
        } else if (content.contains("科技") || content.contains("未来") || content.contains("太空") || 
                  content.contains("星球") || content.contains("机器人")) {
            return "科幻";
        } else if (content.contains("爱情") || content.contains("恋爱") || content.contains("浪漫")) {
            return "言情/爱情";
        } else if (content.contains("推理") || content.contains("侦探") || content.contains("案件") || 
                  content.contains("谜题")) {
            return "推理/侦探";
        } else if (content.contains("战争") || content.contains("军事") || content.contains("战场")) {
            return "军事/战争";
        } else if (content.contains("玄幻") || content.contains("异世界") || content.contains("魔兽")) {
            return "玄幻/奇幻";
        } else if (content.contains("历史") || content.contains("朝代") || content.contains("古代")) {
            return "历史";
        } else {
            return "小说";  // 默认类型
        }
    }

    /**
     * 生成小说整体摘要
     *
     * @param novel 小说实体
     * @return 生成的摘要
     */
    @Override
    public String generateOverallSummary(Novel novel) {
        logger.info("开始生成小说《{}》整体摘要", novel.getTitle());
        
        // 获取所有章节
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novel.getId());
        if (chapters.isEmpty()) {
            logger.warn("小说《{}》没有章节，无法生成摘要", novel.getTitle());
            return "该小说暂无章节内容，无法生成摘要。";
        }
        
        // 构建完整内容
        StringBuilder contentBuilder = new StringBuilder();
        for (Chapter chapter : chapters) {
            if (chapter.getTitle() != null) {
                contentBuilder.append(chapter.getTitle()).append("\n");
            }
            if (chapter.getContent() != null && !chapter.getContent().isEmpty()) {
                contentBuilder.append(chapter.getContent()).append("\n\n");
            }
        }
        
        String fullContent = contentBuilder.toString().trim();
        
        // 记录内容长度
        logger.info("小说《{}》内容总长度: {} 字符", novel.getTitle(), fullContent.length());
        
        // 检查内容是否为空
        if (fullContent.isEmpty()) {
            logger.warn("小说《{}》内容为空，无法生成摘要", novel.getTitle());
            return "该小说暂无内容，无法生成摘要。";
        }
        
        // 检查是否只包含章节标题
        boolean onlyChapterTitles = true;
        for (Chapter chapter : chapters) {
            if (chapter.getContent() != null && !chapter.getContent().trim().isEmpty()) {
                onlyChapterTitles = false;
                break;
            }
        }
        
        // 如果只有章节标题，则基于标题生成摘要
        if (onlyChapterTitles) {
            logger.info("小说《{}》只包含章节标题，将基于标题生成摘要", novel.getTitle());
            StringBuilder titleBasedSummary = new StringBuilder();
            titleBasedSummary.append("《").append(novel.getTitle()).append("》");
            
            if (novel.getAuthor() != null && !novel.getAuthor().isEmpty()) {
                titleBasedSummary.append("是").append(novel.getAuthor()).append("创作的小说，");
            }
            
            titleBasedSummary.append("包含").append(chapters.size()).append("个章节。");
            titleBasedSummary.append("根据章节标题分析，这部小说讲述了");
            
            // 添加前5个章节的标题（如果有）
            int titleCount = Math.min(5, chapters.size());
            for (int i = 0; i < titleCount; i++) {
                String title = chapters.get(i).getTitle();
                if (title != null && !title.trim().isEmpty()) {
                    if (i > 0) {
                        titleBasedSummary.append("，");
                        if (i == titleCount - 1) {
                            titleBasedSummary.append("以及");
                        }
                    }
                    titleBasedSummary.append("\"").append(title).append("\"");
                }
            }
            
            titleBasedSummary.append("等情节。");
            if (novel.getCreateTime() != null) {
                titleBasedSummary.append("该小说创建于" + novel.getCreateTime() + "。");
            }
            
            return titleBasedSummary.toString();
        }
        
        // 如果内容过长，截取前100,000个字符
        String processingContent = fullContent;
        if (fullContent.length() > 100000) {
            logger.info("小说《{}》内容过长，截取前100,000字符进行处理", novel.getTitle());
            processingContent = fullContent.substring(0, 100000);
        }
        
        // 调用已有的方法完成摘要生成
        return generateOverallSummary(novel.getId(), processingContent);
    }

    /**
     * 根据小说ID和内容生成整体摘要
     *
     * @param novelId     小说ID
     * @param fullContent 完整内容
     * @return 生成的摘要
     */
    private String generateOverallSummary(Long novelId, String fullContent) {
        logger.info("根据ID和内容生成小说摘要，ID: {}", novelId);
        
        // 获取小说实体
        Optional<Novel> optionalNovel = novelRepository.findById(novelId);
        if (!optionalNovel.isPresent()) {
            logger.error("找不到ID为{}的小说，无法生成摘要", novelId);
            return "无法找到小说记录，摘要生成失败。";
        }
        
        Novel novel = optionalNovel.get();
        
        // 获取章节数量
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        
        // 检查内容是否为空
        if (fullContent == null || fullContent.trim().isEmpty()) {
            logger.warn("小说《{}》内容为空，无法生成摘要", novel.getTitle());
            return "该小说暂无内容，无法生成摘要。";
        }
        
        // 记录内容长度
        logger.info("小说《{}》内容总长度: {} 字符", novel.getTitle(), fullContent.length());
        
        // 如果内容过长，截取前100,000个字符
        String processingContent = fullContent;
        if (fullContent.length() > 100000) {
            logger.info("小说《{}》内容过长，截取前100,000字符进行处理", novel.getTitle());
            processingContent = fullContent.substring(0, 100000);
        }
        
        try {
            // 使用NLP服务生成摘要
            logger.info("调用NLP服务为小说《{}》生成摘要", novel.getTitle());
            String summary = nlpService.generateSummary(processingContent, 500);
            
            // 验证生成的摘要
            if (summary != null && !summary.trim().isEmpty()) {
                // 检查摘要是否包含章节标题
                boolean containsChapterMarkers = summary.contains("第1章") || 
                                                summary.contains("章节") || 
                                                summary.contains("第一章");
                
                if (containsChapterMarkers) {
                    logger.warn("生成的摘要包含章节标记，尝试重新生成");
                    // 尝试重新生成一次
                    summary = nlpService.generateSummary(processingContent, 500);
                    
                    // 再次检查
                    if (summary.contains("第1章") || summary.contains("章节") || summary.contains("第一章")) {
                        logger.warn("重新生成的摘要仍包含章节标记，使用基本信息作为摘要");
                        summary = "《" + novel.getTitle() + "》是一部包含" + chapters.size() + 
                                 "章的小说" + (novel.getAuthor() != null ? "，作者是" + novel.getAuthor() : "") + 
                                 "。创建于" + novel.getCreateTime() + "。";
                    }
                }
                
                logger.info("成功生成小说《{}》摘要，长度：{} 字符", novel.getTitle(), summary.length());
                return summary;
            } else {
                logger.warn("NLP服务未能生成有效摘要");
                return "无法生成有效摘要，请稍后重试。";
            }
        } catch (Exception e) {
            logger.error("生成小说《{}》摘要时发生错误: {}", novel.getTitle(), e.getMessage(), e);
            return "摘要生成过程中发生错误：" + e.getMessage();
        }
    }
} 