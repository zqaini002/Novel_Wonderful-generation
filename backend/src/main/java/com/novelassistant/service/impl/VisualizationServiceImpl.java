package com.novelassistant.service.impl;

import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Tag;
import com.novelassistant.entity.NovelCharacter;
import com.novelassistant.entity.CharacterRelationship;
import com.novelassistant.entity.CharacterDialogue;
import com.novelassistant.entity.visualization.EmotionalData;
import com.novelassistant.repository.ChapterRepository;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.TagRepository;
import com.novelassistant.repository.NovelCharacterRepository;
import com.novelassistant.repository.CharacterRelationshipRepository;
import com.novelassistant.repository.CharacterDialogueRepository;
import com.novelassistant.repository.visualization.EmotionalDataRepository;
import com.novelassistant.service.VisualizationService;
import com.novelassistant.service.NlpService;
import com.novelassistant.util.LogUtil;
import com.novelassistant.service.MachineLearningService;
import com.novelassistant.util.ApplicationContextProvider;
import com.novelassistant.util.RequestContextHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * 小说可视化服务实现类
 * 提供各类数据可视化所需数据的服务实现
 */
@Service
public class VisualizationServiceImpl implements VisualizationService {

    private static final Logger logger = LoggerFactory.getLogger(VisualizationServiceImpl.class);
    
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
    
    @Autowired
    private CharacterDialogueRepository characterDialogueRepository;
    
    @Autowired
    private EmotionalDataRepository emotionalDataRepository;
    
    @Autowired
    private NlpService nlpService;

    @Autowired
    private MachineLearningService machineLearningService;

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
        
        try {
            // 准备章节内容用于机器学习分析
            List<String> chapterContents = chapters.stream()
                    .filter(chapter -> chapter.getContent() != null && !chapter.getContent().isEmpty())
                    .map(Chapter::getContent)
                    .collect(Collectors.toList());
            
            if (!chapterContents.isEmpty()) {
                // 合并所有章节内容
                String fullContent = String.join("\n\n", chapterContents);
                
                // 使用LDA主题模型提取主题关键词（更高质量的主题词）
                Map<Integer, List<String>> topicKeywords = machineLearningService.extractTopicsWithLDA(fullContent, 5);
                
                // 将主题关键词添加到结果中（给予较高权重）
                for (List<String> keywords : topicKeywords.values()) {
                    for (int i = 0; i < keywords.size(); i++) {
                        String keyword = keywords.get(i);
                        // 根据在主题中的位置给予不同权重
                        int weight = 15 - Math.min(i, 10);
                        keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + weight);
                    }
                }
                
                // 添加写作风格相关关键词
                Map<String, Double> styleFeatures = machineLearningService.detectWritingStyle(fullContent);
                for (Map.Entry<String, Double> entry : styleFeatures.entrySet()) {
                    if (entry.getValue() > 0.6) { // 只添加显著的风格特征
                        String keyword = entry.getKey();
                        int weight = (int)(entry.getValue() * 10);
                        keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + weight);
                    }
                }
                
                // 对小说进行文类分类，添加类别作为关键词
                List<String> categories = Arrays.asList("奇幻", "科幻", "武侠", "言情", "悬疑", "历史", "都市");
                Map<String, Double> genreScores = machineLearningService.classifyText(fullContent, categories);
                for (Map.Entry<String, Double> entry : genreScores.entrySet()) {
                    if (entry.getValue() > 0.3) { // 只添加较为明显的类别
                        keywordFrequency.put(entry.getKey(), keywordFrequency.getOrDefault(entry.getKey(), 0) + (int)(entry.getValue() * 10));
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("使用机器学习提取关键词失败，回退到传统方法: {}", e.getMessage());
            // 出错时回退到传统方法
        }
        
        // 从章节中提取已有关键词
        for (Chapter chapter : chapters) {
            List<String> keywords = chapter.getKeywords();
            if (keywords != null) {
                for (String keyword : keywords) {
                    keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + 1);
                }
            }
        }
        
        // 从Tags中提取关键词，按不同标签类型区分权重
        List<Tag> tags = tagRepository.findByNovelId(novelId);
        for (Tag tag : tags) {
            // 根据标签类型设置不同权重
            int weight = getWeightByTagType(tag.getType());
            keywordFrequency.put(tag.getName(), keywordFrequency.getOrDefault(tag.getName(), 0) + weight);
        }
        
        // 如果机器学习分析不成功或关键词太少，使用传统NLP方法补充
        if (keywordFrequency.size() < 20 && !chapters.isEmpty()) {
            // 从章节内容中提取关键内容，对未被提取的内容进行补充分析
            extractContentKeywords(chapters, keywordFrequency);
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
     * 根据标签类型获取权重
     */
    private int getWeightByTagType(Tag.TagType type) {
        switch (type) {
            case POSITIVE:
                return 5;  // 积极标签较高权重
            case WARNING:
                return 4;  // 警告标签中等权重
            case INFO:
            default:
                return 3;  // 信息标签基本权重
        }
    }
    
    /**
     * 从章节内容中提取更多的关键词
     */
    private void extractContentKeywords(List<Chapter> chapters, Map<String, Integer> keywordFrequency) {
        // 样本分析：选择一部分章节进行详细分析
        int maxChaptersToAnalyze = Math.min(chapters.size(), 10);  // 最多分析10章
        int step = chapters.size() / maxChaptersToAnalyze;
        step = Math.max(1, step);  // 确保步长至少为1
        
        for (int i = 0; i < chapters.size(); i += step) {
            Chapter chapter = chapters.get(i);
            if (chapter.getContent() == null || chapter.getContent().isEmpty()) {
                continue;
            }
            
            // 使用NLP服务进行深度关键词提取
            try {
                Map<String, Integer> contentKeywords = nlpService.extractKeywords(chapter.getContent(), 20);
                
                // 合并结果
                for (Map.Entry<String, Integer> entry : contentKeywords.entrySet()) {
                    String keyword = entry.getKey();
                    int weight = Math.max(1, entry.getValue() / 20);  // 缩放权重
                    keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + weight);
                }
            } catch (Exception e) {
                // 忽略单个章节的分析错误，确保整体流程不中断
                logger.warn("分析章节内容时出错，章节ID: " + chapter.getId(), e);
            }
        }
        
        // 分析小说整体主题（如果有总结）
        Novel novel = novelRepository.findById(chapters.get(0).getNovel().getId())
                .orElseThrow(() -> new RuntimeException("获取小说失败，ID: " + chapters.get(0).getNovel().getId()));
                
        if (novel.getOverallSummary() != null && !novel.getOverallSummary().isEmpty()) {
            try {
                Map<String, Integer> summaryKeywords = nlpService.extractKeywords(novel.getOverallSummary(), 10);
                for (Map.Entry<String, Integer> entry : summaryKeywords.entrySet()) {
                    String keyword = entry.getKey();
                    int weight = Math.max(1, entry.getValue() / 15);  // 略微降低权重
                    keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + weight);
                }
            } catch (Exception e) {
                logger.warn("分析小说总结时出错，小说ID: " + novel.getId(), e);
            }
        }
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

        // 准备数据
        List<Map<String, Object>> emotionalData = new ArrayList<>();
        
        // 从数据库获取情感数据，按章节号排序
        List<com.novelassistant.entity.visualization.EmotionalData> dataList = 
                emotionalDataRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
        
        // 如果存在实际分析的情感数据，使用它
        if (!dataList.isEmpty()) {
            for (com.novelassistant.entity.visualization.EmotionalData data : dataList) {
                Map<String, Object> chapterData = new HashMap<>();
                
                // 基本信息
                chapterData.put("chapter", "第" + data.getChapterNumber() + "章");
                chapterData.put("chapterTitle", data.getChapterTitle());
                chapterData.put("emotion", data.getEmotionValue());
                chapterData.put("event", data.getEventDescription());
                
                // 重要事件标记
                if (data.getIsImportant() != null && data.getIsImportant()) {
                    chapterData.put("isImportant", true);
                }
                
                // 情节高潮段落标记
                if (data.getIsClimaxStart() != null && data.getIsClimaxStart()) {
                    chapterData.put("isClimaxStart", true);
                }
                
                if (data.getIsClimaxEnd() != null && data.getIsClimaxEnd()) {
                    chapterData.put("isClimaxEnd", true);
                }
                
                emotionalData.add(chapterData);
            }
        } else {
            // 如果没有实际数据，获取小说的所有章节并使用机器学习进行分析
            List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
            logger.info("小说 ID: {} 没有情感分析数据，使用机器学习分析", novelId);
            
            try {
                // 准备章节内容
                List<String> chapterContents = chapters.stream()
                        .filter(chapter -> chapter.getContent() != null && !chapter.getContent().isEmpty())
                        .map(Chapter::getContent)
                        .collect(Collectors.toList());
                
                if (!chapterContents.isEmpty()) {
                    // 使用深度情感分析处理每个章节
                    double[] emotionValues = new double[chapterContents.size()];
                    for (int i = 0; i < chapterContents.size(); i++) {
                        // 使用深度情感分析，比传统NLP更准确
                        emotionValues[i] = machineLearningService.deepSentimentAnalysis(chapterContents.get(i));
                        // 转换到0-100的范围
                        emotionValues[i] = emotionValues[i] * 100;
                    }
                    
                    // 预测情节发展，识别关键章节
                    Map<String, Double> plotPredictions = machineLearningService.predictPlotDevelopment(chapterContents);
                    
                    // 识别潜在的重要情节转折点
                    List<Integer> keyChapters = detectKeyChapters(emotionValues);
                    
                    // 识别情节高潮段起始
                    int[] climaxRange = detectClimaxRange(emotionValues);
                    
                    // 构建每个章节的情感数据
                    for (int i = 0; i < chapters.size() && i < emotionValues.length; i++) {
                        Chapter chapter = chapters.get(i);
                        Map<String, Object> chapterData = new HashMap<>();
                        
                        // 基础数据
                        chapterData.put("chapter", "第" + chapter.getChapterNumber() + "章");
                        chapterData.put("chapterTitle", chapter.getTitle());
                        chapterData.put("emotion", emotionValues[i]);
                        
                        // 添加章节摘要作为事件描述
                        String eventDesc = (chapter.getSummary() != null && !chapter.getSummary().isEmpty())
                                ? chapter.getSummary()
                                : "章节" + chapter.getChapterNumber();
                        
                        // 截断过长的摘要
                        if (eventDesc.length() > 100) {
                            eventDesc = eventDesc.substring(0, 97) + "...";
                        }
                        
                        chapterData.put("event", eventDesc);
                        
                        // 标记关键章节
                        if (keyChapters.contains(i)) {
                            chapterData.put("isImportant", true);
                        }
                        
                        // 标记情节高潮段落的开始和结束
                        if (i == climaxRange[0]) {
                            chapterData.put("isClimaxStart", true);
                        }
                        if (i == climaxRange[1]) {
                            chapterData.put("isClimaxEnd", true);
                        }
                        
                        emotionalData.add(chapterData);
                    }
                    
                    // 保存分析结果到数据库，方便下次使用
                    saveEmotionalDataToDB(novel, chapters, emotionValues, keyChapters, climaxRange);
                    
                    logger.info("成功使用机器学习分析小说情感曲线，小说ID: {}", novelId);
                } else {
                    // 如果没有章节内容，使用模拟数据
                    generateMockEmotionalData(chapters, emotionalData);
                    logger.warn("小说没有章节内容，使用模拟数据. 小说ID: {}", novelId);
                }
            } catch (Exception e) {
                logger.error("机器学习分析情感数据失败，回退到模拟数据. 小说ID: {}, 错误: {}", novelId, e.getMessage());
                // 出错时使用模拟数据
                generateMockEmotionalData(chapters, emotionalData);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("emotional", emotionalData);
        
        return result;
    }
    
    /**
     * 检测关键章节（情感变化显著的章节）
     */
    private List<Integer> detectKeyChapters(double[] emotionValues) {
        List<Integer> keyChapters = new ArrayList<>();
        if (emotionValues.length < 3) {
            return keyChapters;
        }
        
        // 计算情感变化率
        double[] changes = new double[emotionValues.length - 1];
        for (int i = 0; i < changes.length; i++) {
            changes[i] = emotionValues[i + 1] - emotionValues[i];
        }
        
        // 计算变化率的平均值和标准差
        double avgChange = 0;
        for (double change : changes) {
            avgChange += Math.abs(change);
        }
        avgChange /= changes.length;
        
        double stdDev = 0;
        for (double change : changes) {
            stdDev += Math.pow(Math.abs(change) - avgChange, 2);
        }
        stdDev = Math.sqrt(stdDev / changes.length);
        
        // 检测变化显著的章节
        double threshold = avgChange + stdDev * 1.5;
        for (int i = 0; i < changes.length; i++) {
            if (Math.abs(changes[i]) > threshold) {
                keyChapters.add(i + 1); // 变化发生在i和i+1之间，标记i+1
            }
        }
        
        // 检测情感峰值和谷值的章节
        for (int i = 1; i < emotionValues.length - 1; i++) {
            if ((emotionValues[i] > emotionValues[i - 1] && emotionValues[i] > emotionValues[i + 1]) ||
                (emotionValues[i] < emotionValues[i - 1] && emotionValues[i] < emotionValues[i + 1])) {
                // 是峰值或谷值
                if (!keyChapters.contains(i)) {
                    keyChapters.add(i);
                }
            }
        }
        
        // 如果关键章节太少，添加情感值最高和最低的章节
        if (keyChapters.size() < 3) {
            int highestIndex = 0;
            int lowestIndex = 0;
            for (int i = 0; i < emotionValues.length; i++) {
                if (emotionValues[i] > emotionValues[highestIndex]) {
                    highestIndex = i;
                }
                if (emotionValues[i] < emotionValues[lowestIndex]) {
                    lowestIndex = i;
                }
            }
            
            if (!keyChapters.contains(highestIndex)) {
                keyChapters.add(highestIndex);
            }
            if (!keyChapters.contains(lowestIndex)) {
                keyChapters.add(lowestIndex);
            }
        }
        
        // 按章节序号排序
        Collections.sort(keyChapters);
        
        return keyChapters;
    }
    
    /**
     * 检测情节高潮范围
     * @return 返回[开始章节索引, 结束章节索引]
     */
    private int[] detectClimaxRange(double[] emotionValues) {
        if (emotionValues.length <= 5) {
            return new int[]{0, emotionValues.length - 1};
        }
        
        // 计算移动平均
        int windowSize = Math.max(3, emotionValues.length / 10);
        double[] smoothed = new double[emotionValues.length];
        
        for (int i = 0; i < emotionValues.length; i++) {
            double sum = 0;
            int count = 0;
            
            for (int j = Math.max(0, i - windowSize/2); j <= Math.min(emotionValues.length - 1, i + windowSize/2); j++) {
                sum += emotionValues[j];
                count++;
            }
            
            smoothed[i] = sum / count;
        }
        
        // 找出情感值最高的段落
        int maxStartIndex = 0;
        double maxAvgEmotion = 0;
        int climaxLength = Math.max(2, emotionValues.length / 5); // 高潮长度约为总长的1/5
        
        for (int i = 0; i < smoothed.length - climaxLength; i++) {
            double avgEmotion = 0;
            for (int j = i; j < i + climaxLength; j++) {
                avgEmotion += smoothed[j];
            }
            avgEmotion /= climaxLength;
            
            if (avgEmotion > maxAvgEmotion) {
                maxAvgEmotion = avgEmotion;
                maxStartIndex = i;
            }
        }
        
        return new int[]{maxStartIndex, maxStartIndex + climaxLength - 1};
    }
    
    /**
     * 保存情感分析数据到数据库
     */
    private void saveEmotionalDataToDB(Novel novel, List<Chapter> chapters, double[] emotionValues, 
                                      List<Integer> keyChapters, int[] climaxRange) {
        try {
            for (int i = 0; i < chapters.size() && i < emotionValues.length; i++) {
                Chapter chapter = chapters.get(i);
                
                EmotionalData emotionalData = new EmotionalData();
                emotionalData.setNovel(novel);
                emotionalData.setChapter(chapter);
                emotionalData.setChapterNumber(chapter.getChapterNumber());
                emotionalData.setChapterTitle(chapter.getTitle());
                emotionalData.setEmotionValue(emotionValues[i]);
                
                // 设置章节描述
                String eventDesc = (chapter.getSummary() != null && !chapter.getSummary().isEmpty())
                        ? chapter.getSummary() : "章节" + chapter.getChapterNumber();
                if (eventDesc.length() > 100) {
                    eventDesc = eventDesc.substring(0, 97) + "...";
                }
                emotionalData.setEventDescription(eventDesc);
                
                // 设置关键章节标记
                emotionalData.setIsImportant(keyChapters.contains(i));
                
                // 设置高潮开始和结束标记
                emotionalData.setIsClimaxStart(i == climaxRange[0]);
                emotionalData.setIsClimaxEnd(i == climaxRange[1]);
                
                // 保存到数据库
                emotionalDataRepository.save(emotionalData);
            }
            logger.info("成功保存情感分析数据到数据库，小说ID: {}", novel.getId());
        } catch (Exception e) {
            logger.error("保存情感分析数据失败, 小说ID: {}, 错误: {}", novel.getId(), e.getMessage());
        }
    }
    
    /**
     * 生成模拟情感数据（在机器学习分析失败时使用）
     */
    private void generateMockEmotionalData(List<Chapter> chapters, List<Map<String, Object>> emotionalData) {
            // 处理每个章节的情节数据
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                Map<String, Object> chapterData = new HashMap<>();
                
                // 基础数据
                chapterData.put("chapter", "第" + chapter.getChapterNumber() + "章");
                chapterData.put("chapterTitle", chapter.getTitle());
                
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
        
        // 统计章节字数范围分布
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
        
        try {
            // 获取章节内容列表用于机器学习分析
            List<String> chapterContents = chapters.stream()
                    .map(Chapter::getContent)
                    .collect(Collectors.toList());
            
            // 使用机器学习服务分析小说结构，替代原来的模拟数据
            Map<String, List<Map<String, Object>>> mlStructureData = 
                    machineLearningService.analyzeNovelStructure(chapterContents);
            
            // 小说段落结构数据（从机器学习分析结果获取）
        Map<String, Object> structureSections = new LinkedHashMap<>();
            structureSections.put("type", "小说结构分析");
            
            // 从机器学习结果中获取主结构和详细结构
            List<Map<String, Object>> mainStructure = mlStructureData.get("mainStructure");
            List<Map<String, Object>> detailStructure = mlStructureData.get("detailStructure");
            
            // 格式化为API所需的结构
            List<Map<String, Object>> sections = new ArrayList<>();
            for (Map<String, Object> section : mainStructure) {
                sections.add(section);
            }
            
            structureSections.put("sections", sections);
            
            // 添加详细结构数据
            structureData.put("mainStructure", mainStructure);
            structureData.put("detailStructure", detailStructure);
            
            logger.info("使用机器学习成功分析小说结构，小说ID: {}", novelId);
        } catch (Exception e) {
            logger.error("机器学习分析小说结构失败，回退到默认结构，小说ID: {}, 错误: {}", novelId, e.getMessage());
            
            // 回退到默认的结构分配
            Map<String, Object> structureSections = createDefaultStructure(chapters.size());
            structureData.put("structure", structureSections);
        }
        
        // 整合所有数据
        structureData.put("novelInfo", novelInfo);
        structureData.put("wordCount", wordCountData);
        
        return structureData;
    }
    
    /**
     * 创建默认的结构数据（当机器学习分析失败时使用）
     */
    private Map<String, Object> createDefaultStructure(int totalChapters) {
        Map<String, Object> structureSections = new LinkedHashMap<>();
        structureSections.put("type", "小说结构分析");
        
        // 模拟标准小说结构
        String[] sectionNames = {"序章", "引子", "发展", "高潮", "结局"};
        double[] percentages = {0.1, 0.2, 0.4, 0.2, 0.1};
        
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
        
        structureSections.put("sections", sections);
        return structureSections;
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
            
            // 从数据库获取现有角色和关系数据
            List<NovelCharacter> characters = characterRepository.findByNovelId(novelId);
            List<CharacterRelationship> relationships = relationshipRepository.findByNovelId(novelId);
            
            // 如果数据库有足够的数据，直接使用
            if (!characters.isEmpty() && !relationships.isEmpty()) {
                logger.info("使用数据库中的角色关系数据，小说ID: {}", novelId);
                return buildSimplifiedNetworkFromDB(characters, relationships);
            }
            
            // 如果数据库数据不足，使用简化版的分析
            List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
            if (chapters.isEmpty()) {
                logger.warn("小说没有章节数据，无法分析角色关系. 小说ID: {}", novelId);
                return simplifiedCharacterNetworkData(novel.getTitle());
            }
            
            try {
                // 使用简化版的ML分析
                return analyzeSimplifiedCharacterNetwork(novel, chapters);
            } catch (Exception e) {
                logger.error("分析人物关系网络失败，使用简化演示数据. 小说ID: {}, 错误: {}", novelId, e.getMessage());
                return simplifiedCharacterNetworkData(novel.getTitle());
            }
        } catch (Exception e) {
            logger.error("获取人物关系网络数据异常: ", e);
            return simplifiedCharacterNetworkData(novelOpt.map(Novel::getTitle).orElse("未知小说"));
            }
    }
    
    /**
     * 使用简化版的机器学习分析人物关系网络
     */
    private Map<String, Object> analyzeSimplifiedCharacterNetwork(Novel novel, List<Chapter> chapters) {
        logger.info("使用简化版机器学习分析人物关系网络，小说ID: {}", novel.getId());
        
        try {
            // 设置当前小说ID
            Long previousNovelId = RequestContextHolder.getCurrentNovelId();
            RequestContextHolder.setCurrentNovelId(novel.getId());
            
            // 只选择前10章和最后5章进行分析，避免处理整个小说内容
            List<Chapter> sampleChapters = new ArrayList<>();
            
            // 添加前10章
            int frontChapters = Math.min(10, chapters.size());
            for (int i = 0; i < frontChapters; i++) {
                sampleChapters.add(chapters.get(i));
            }
            
            // 添加后5章（如果总章节数大于15）
            if (chapters.size() > 15) {
                for (int i = chapters.size() - 5; i < chapters.size(); i++) {
                    sampleChapters.add(chapters.get(i));
                }
            }
            
            // 合并章节内容
            String sampleContent = sampleChapters.stream()
                    .filter(chapter -> chapter.getContent() != null && !chapter.getContent().isEmpty())
                    .map(Chapter::getContent)
                    .collect(Collectors.joining("\n\n"));
            
            if (sampleContent.isEmpty()) {
                throw new RuntimeException("样本章节内容为空，无法分析角色关系");
            }
            
            // 使用机器学习服务构建简化版人物关系网络
            Map<String, List<Map<String, Object>>> mlResult = null;
            try {
                logger.info("开始调用机器学习服务进行简化人物网络分析，小说ID: {}", novel.getId());
                // 限制最多返回10个主要角色和15个关系
                mlResult = machineLearningService.buildCharacterNetwork(sampleContent);
                logger.info("简化人物网络分析成功，小说ID: {}", novel.getId());
            } catch (Exception e) {
                logger.error("调用机器学习服务分析人物网络失败: {}", e.getMessage(), e);
                throw e;
            }
            
            List<Map<String, Object>> characterNodes = mlResult.get("nodes");
            List<Map<String, Object>> relationLinks = mlResult.get("links");
            
            if (characterNodes == null || characterNodes.isEmpty()) {
                throw new RuntimeException("未能识别出人物角色");
            }
            
            // 仅保留最重要的角色和关系
            List<Map<String, Object>> limitedNodes = limitImportantNodes(characterNodes, 8);
            List<Map<String, Object>> limitedLinks = limitImportantLinks(relationLinks, limitedNodes, 12);
            
            // 存储简化后的结果到数据库（只存储重要角色）
            saveSimplifiedNetworkToDB(novel, limitedNodes, limitedLinks);
            
            // 构建返回数据
            Map<String, Object> networkData = new HashMap<>();
            networkData.put("nodes", limitedNodes);
            networkData.put("links", limitedLinks);
            
            return networkData;
        } finally {
            // 恢复原始上下文
            RequestContextHolder.clearCurrentNovelId();
        }
    }
    
    /**
     * 仅保留最重要的角色节点
     */
    private List<Map<String, Object>> limitImportantNodes(List<Map<String, Object>> nodes, int maxNodes) {
        // 按重要性排序并限制数量
        return nodes.stream()
                .sorted((a, b) -> {
                    Object valueA = a.get("value");
                    Object valueB = b.get("value");
                    int importanceA = (valueA instanceof Number) ? ((Number) valueA).intValue() : 0;
                    int importanceB = (valueB instanceof Number) ? ((Number) valueB).intValue() : 0;
                    return Integer.compare(importanceB, importanceA); // 降序
                })
                .limit(maxNodes)
                .collect(Collectors.toList());
    }
    
    /**
     * 仅保留与重要角色相关的重要关系
     */
    private List<Map<String, Object>> limitImportantLinks(List<Map<String, Object>> links, 
                                                          List<Map<String, Object>> limitedNodes, 
                                                          int maxLinks) {
        // 创建重要节点ID集合
        Set<String> importantNodeIds = limitedNodes.stream()
                .map(node -> node.get("id").toString())
                .collect(Collectors.toSet());
        
        // 仅保留与重要节点相关的关系，并按重要性排序
        return links.stream()
                .filter(link -> {
                    String source = link.get("source").toString();
                    String target = link.get("target").toString();
                    return importantNodeIds.contains(source) && importantNodeIds.contains(target);
                })
                .sorted((a, b) -> {
                    Object valueA = a.get("value");
                    Object valueB = b.get("value");
                    int importanceA = (valueA instanceof Number) ? ((Number) valueA).intValue() : 0;
                    int importanceB = (valueB instanceof Number) ? ((Number) valueB).intValue() : 0;
                    return Integer.compare(importanceB, importanceA); // 降序
                })
                .limit(maxLinks)
                .collect(Collectors.toList());
    }
    
    /**
     * 将简化的人物关系网络保存到数据库（减少数据量）
     */
    private void saveSimplifiedNetworkToDB(Novel novel, 
                                        List<Map<String, Object>> nodes, 
                                        List<Map<String, Object>> links) {
        try {
            if (novel == null || novel.getId() == null) {
                logger.error("保存人物关系网络失败: novel对象为null或ID为null");
                return;
            }
            
            logger.info("开始保存简化人物关系网络，小说ID: {}", novel.getId());
            
            // 保存角色信息
            Map<String, Long> characterIdMap = new HashMap<>(); // 名称到ID的映射
            
            for (Map<String, Object> node : nodes) {
                String name = (String) node.get("name");
                if (name == null || name.trim().isEmpty()) {
                    continue;
                }
                
                NovelCharacter character = new NovelCharacter();
                character.setNovelId(novel.getId());
                character.setName(name);
                character.setDescription((String) node.getOrDefault("desc", ""));
                character.setCategory((String) node.getOrDefault("category", "其他"));
                
                // 设置重要性
                Object importance = node.get("value");
                if (importance instanceof Number) {
                    character.setImportance(((Number) importance).intValue());
                } else {
                    character.setImportance(50);
                }
                
                character.setNovel(novel);
                
                // 保存角色并记录ID
                NovelCharacter savedCharacter = characterRepository.save(character);
                characterIdMap.put(name, savedCharacter.getId());
                
                // 更新节点ID为数据库ID
                node.put("id", savedCharacter.getId().toString());
            }
            
            // 仅保存重要关系（减少数据量）
            for (Map<String, Object> link : links) {
                Object sourceObj = link.get("source");
                Object targetObj = link.get("target");
                String relation = (String) link.get("relation");
                
                if (sourceObj == null || targetObj == null || relation == null) {
                    continue;
                }
                
                // 处理source和target
                String sourceId = sourceObj.toString();
                String targetId = targetObj.toString();
                
                // 解析角色ID
                Long sourceCharacterId = characterIdMap.get(sourceId);
                Long targetCharacterId = characterIdMap.get(targetId);
                
                if (sourceCharacterId == null || targetCharacterId == null) {
                    // 尝试直接查找角色名称
                    for (Map<String, Object> node : nodes) {
                        if (node.get("id").toString().equals(sourceId)) {
                            String characterName = (String) node.get("name");
                            if (characterName != null) {
                                sourceCharacterId = characterIdMap.get(characterName);
                            }
                        }
                        if (node.get("id").toString().equals(targetId)) {
                            String characterName = (String) node.get("name");
                            if (characterName != null) {
                                targetCharacterId = characterIdMap.get(characterName);
                            }
                        }
                    }
                }
                
                if (sourceCharacterId == null || targetCharacterId == null) {
                    continue;
                }
                
                CharacterRelationship relationship = new CharacterRelationship();
                relationship.setNovelId(novel.getId());
                relationship.setSourceCharacterId(sourceCharacterId);
                relationship.setTargetCharacterId(targetCharacterId);
                relationship.setRelationshipType(relation);
                relationship.setDescription((String) link.getOrDefault("desc", ""));
                
                // 设置重要性
                Object importance = link.get("value");
                if (importance instanceof Number) {
                    relationship.setImportance(((Number) importance).intValue());
                } else {
                    relationship.setImportance(3);
                }
                
                relationship.setNovel(novel);
                
                // 保存关系
                relationshipRepository.save(relationship);
                
                // 更新连接的source和target为数据库ID
                link.put("source", sourceCharacterId.toString());
                link.put("target", targetCharacterId.toString());
            }
            
            logger.info("成功保存简化人物关系网络，小说ID: {}", novel.getId());
        } catch (Exception e) {
            logger.error("保存简化人物关系网络失败, 小说ID: {}, 错误: {}", 
                         novel != null ? novel.getId() : "unknown", e.getMessage());
        }
    }
    
    /**
     * 从数据库构建简化的人物关系网络
     */
    private Map<String, Object> buildSimplifiedNetworkFromDB(List<NovelCharacter> characters, List<CharacterRelationship> relationships) {
        Map<String, Object> networkData = new HashMap<>();
        
        try {
            // 仅选择重要角色（最多8个）
            List<NovelCharacter> importantCharacters = characters.stream()
                .sorted((a, b) -> Integer.compare(b.getImportance(), a.getImportance()))
                .limit(8)
                .collect(Collectors.toList());
            
            // 构建节点数据
            List<Map<String, Object>> nodes = new ArrayList<>();
            Map<Long, Integer> idToIndexMap = new HashMap<>();
            
            for (int i = 0; i < importantCharacters.size(); i++) {
                NovelCharacter character = importantCharacters.get(i);
                Map<String, Object> node = new HashMap<>();
                
                node.put("id", i);
                node.put("name", character.getName());
                node.put("value", character.getImportance());
                node.put("category", character.getCategory());
                node.put("desc", character.getDescription());
                nodes.add(node);
                
                idToIndexMap.put(character.getId(), i);
            }
            
            // 构建关系数据（仅包含重要角色间的关系，最多12个）
            List<Map<String, Object>> links = new ArrayList<>();
            
            // 过滤出重要角色之间的关系
            List<CharacterRelationship> filteredRelationships = relationships.stream()
                .filter(rel -> idToIndexMap.containsKey(rel.getSourceCharacterId()) && 
                              idToIndexMap.containsKey(rel.getTargetCharacterId()))
                .sorted((a, b) -> Integer.compare(b.getImportance(), a.getImportance()))
                .limit(12)
                .collect(Collectors.toList());
            
            for (CharacterRelationship relationship : filteredRelationships) {
                Integer sourceIndex = idToIndexMap.get(relationship.getSourceCharacterId());
                Integer targetIndex = idToIndexMap.get(relationship.getTargetCharacterId());
                
                if (sourceIndex != null && targetIndex != null) {
                Map<String, Object> link = new HashMap<>();
                    link.put("source", sourceIndex);
                    link.put("target", targetIndex);
                link.put("relation", relationship.getRelationshipType());
                link.put("value", relationship.getImportance());
                link.put("desc", relationship.getDescription());
                links.add(link);
                }
            }
            
            logger.info("从数据库构建了简化人物网络：节点数={}, 关系数={}", nodes.size(), links.size());
            
            networkData.put("nodes", nodes);
            networkData.put("links", links);
        } catch (Exception e) {
            logger.error("构建简化网络数据出错: {}", e.getMessage());
        }
            
            return networkData;
    }

    /**
     * 生成简化的演示人物关系网络数据
     */
    private Map<String, Object> simplifiedCharacterNetworkData(String title) {
        Map<String, Object> networkData = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        
        // 生成更少的演示角色 (只生成6个)
        String[] names = {"主角", "女主角", "反派", "挚友", "导师", "家人"};
        String[] categories = {"主要角色", "主要角色", "主要角色", "主要角色", "主要角色", "次要角色"};
        int[] importances = {80, 70, 65, 50, 55, 40};
        
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", i);
            node.put("name", names[i]);
            node.put("value", importances[i]);
            node.put("category", categories[i]);
            node.put("desc", title + "中的" + names[i]);
            nodes.add(node);
        }
        
        // 生成更少的演示关系 (只生成8个)
        int[][] relations = {
            {0, 1, 5}, // 主角-女主角，强度5
            {0, 2, 5}, // 主角-反派，强度5
            {0, 3, 4}, // 主角-挚友，强度4
            {0, 4, 4}, // 主角-导师，强度4
            {0, 5, 3}, // 主角-家人，强度3
            {1, 2, 2}, // 女主角-反派，强度2
            {3, 4, 2}, // 挚友-导师，强度2
            {4, 2, 2}  // 导师-反派，强度2
        };
        
        String[] relationTypes = {"恋人", "宿敌", "好友", "师徒", "亲子", "敌对", "熟识", "旧识"};
        
        for (int i = 0; i < relations.length; i++) {
            Map<String, Object> link = new HashMap<>();
            int sourceIndex = relations[i][0];
            int targetIndex = relations[i][1];
            int value = relations[i][2];
            
            link.put("source", sourceIndex);
            link.put("target", targetIndex);
            link.put("relation", relationTypes[i]);
            link.put("value", value);
            link.put("desc", names[sourceIndex] + "与" + names[targetIndex] + "的" + relationTypes[i] + "关系");
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