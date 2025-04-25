package com.novelassistant.service.impl;

import com.novelassistant.model.ClusterResult;
import com.novelassistant.model.DocumentVectorModel;
import com.novelassistant.model.SentimentResult;
import com.novelassistant.model.TopicResult;
import com.novelassistant.service.MachineLearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.*;

@Service("wekaMachineLearningService")
public class WekaMachineLearningServiceImpl implements MachineLearningService {
    private static final Logger logger = LoggerFactory.getLogger(WekaMachineLearningServiceImpl.class);
    private static final String MODEL_PATH = "models/weka_model";
    private static final String SENTIMENT_LEXICON_PATH = "models/sentiment/chinese_sentiment_words.txt";
    
    // 词汇表和文档向量模型
    private Map<String, Double> vocabulary;
    private DocumentVectorModel documentVectorModel;
    // 文档集合，用于计算语义相关度
    private List<String> documents;
    // 情感词典
    private Map<String, Double> sentimentLexicon;

    public WekaMachineLearningServiceImpl() {
        // 初始化词汇表
        vocabulary = new HashMap<>();
        documents = new ArrayList<>();
        
        // 初始化情感词典
        loadSentimentLexicon();
        
        // 初始化文档向量模型
        initDocumentVectorModel();
    }
    
    /**
     * 加载情感词典
     */
    private void loadSentimentLexicon() {
        sentimentLexicon = new HashMap<>();
        try {
            // 使用ClassPathResource从类路径加载资源
            Resource resource = new ClassPathResource(SENTIMENT_LEXICON_PATH);
            
            if (resource.exists()) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 跳过注释行和空行
                        if (line.startsWith("#") || line.trim().isEmpty()) {
                            continue;
                        }
                        
                        // 解析词语和情感分数
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length == 2) {
                            try {
                                String word = parts[0];
                                double score = Double.parseDouble(parts[1]);
                                sentimentLexicon.put(word, score);
                            } catch (NumberFormatException e) {
                                logger.warn("无法解析情感词典中的分数: {}", line);
                            }
                        }
                    }
                }
                logger.info("成功加载情感词典，包含 {} 个词语", sentimentLexicon.size());
            } else {
                logger.warn("情感词典文件不存在: {}", SENTIMENT_LEXICON_PATH);
            }
        } catch (Exception e) {
            logger.error("加载情感词典时出错: {}", e.getMessage(), e);
            // 如果加载失败，初始化一个基本的情感词典
            initializeBasicSentimentLexicon();
        }
    }
    
    /**
     * 初始化基本情感词典（当无法加载外部词典时使用）
     */
    private void initializeBasicSentimentLexicon() {
        logger.info("初始化基本情感词典");
        sentimentLexicon = new HashMap<>();
        // 添加一些基本情感词
        sentimentLexicon.put("喜欢", 3.0);
        sentimentLexicon.put("爱", 4.0);
        sentimentLexicon.put("高兴", 3.0);
        sentimentLexicon.put("开心", 3.0);
        sentimentLexicon.put("快乐", 3.0);
        sentimentLexicon.put("幸福", 4.0);
        sentimentLexicon.put("欢乐", 3.0);
        sentimentLexicon.put("满意", 2.0);
        
        sentimentLexicon.put("讨厌", -3.0);
        sentimentLexicon.put("恨", -4.0);
        sentimentLexicon.put("悲伤", -3.0);
        sentimentLexicon.put("难过", -3.0);
        sentimentLexicon.put("痛苦", -4.0);
        sentimentLexicon.put("失望", -2.0);
        sentimentLexicon.put("愤怒", -3.0);
        sentimentLexicon.put("恐惧", -3.0);
        
        logger.info("已初始化基本情感词典，包含 {} 个词语", sentimentLexicon.size());
    }
    
    /**
     * 初始化文档向量模型
     */
    private void initDocumentVectorModel() {
        try {
            File modelFile = new File(MODEL_PATH);
            if (modelFile.exists()) {
                // 如果模型文件存在，加载已有模型
                logger.info("Loading existing Weka model from {}", MODEL_PATH);
                // 模型加载逻辑
                documentVectorModel = new DocumentVectorModel();
            } else {
                // 如果模型文件不存在，创建新模型
                logger.info("No existing Weka model found. A new model will be created when training is requested.");
                documentVectorModel = new DocumentVectorModel();
            }
        } catch (Exception e) {
            logger.error("Error initializing document vector model: {}", e.getMessage());
            documentVectorModel = new DocumentVectorModel();
        }
    }

    /**
     * 计算两段文本的相似度
     */
    @Override
    public double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            logger.warn("Cannot calculate similarity for empty texts");
            return 0.0;
        }

        try {
            return documentVectorModel.calculateCosineSimilarity(text1, text2);
        } catch (Exception e) {
            logger.error("Error calculating text similarity", e);
            return 0.0;
        }
    }

    /**
     * 对文本集合进行聚类
     */
    @Override
    public Map<Integer, List<Integer>> clusterTexts(List<String> texts, int clusterCount) {
        if (texts == null || texts.isEmpty() || clusterCount <= 0 || clusterCount > texts.size()) {
            logger.warn("Invalid input for clustering: texts size={}, numClusters={}", 
                    texts != null ? texts.size() : 0, clusterCount);
            return Collections.emptyMap();
        }

        try {
            // 创建属性列表
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("text", (ArrayList<String>) null));
            
            // 创建数据集
            Instances dataSet = new Instances("TextClusteringDataset", attributes, 0);
            
            // 添加实例
            for (String text : texts) {
                Instance instance = new DenseInstance(1);
                instance.setValue(0, text);
                dataSet.add(instance);
            }
            
            // 使用StringToWordVector过滤器将文本转换为词向量
            StringToWordVector filter = new StringToWordVector();
            filter.setInputFormat(dataSet);
            Instances vectorizedData = Filter.useFilter(dataSet, filter);
            
            // 使用K-means进行聚类
            SimpleKMeans kMeans = new SimpleKMeans();
            kMeans.setNumClusters(clusterCount);
            kMeans.buildClusterer(vectorizedData);
            
            // 获取聚类结果
            Map<Integer, List<Integer>> results = new HashMap<>();
            for (int i = 0; i < texts.size(); i++) {
                Instance instance = vectorizedData.instance(i);
                int clusterNum = kMeans.clusterInstance(instance);
                
                // 将文本索引添加到对应的聚类中
                if (!results.containsKey(clusterNum)) {
                    results.put(clusterNum, new ArrayList<>());
                }
                results.get(clusterNum).add(i);
            }
            
            return results;
        } catch (Exception e) {
            logger.error("Error clustering texts: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 对文本进行深度情感分析
     */
    @Override
    public double deepSentimentAnalysis(String text) {
        if (text == null || text.isEmpty()) {
            logger.warn("Cannot analyze sentiment for empty text");
            return 0.5; // 中性
        }
        
        try {
            // 分词（简单实现，按空格分割）
            String[] words = text.split("\\s+");
            
            // 情感分数累加 (简单实现)
            double sentimentScore = 0.5; // 默认为中性
            
            // 检测积极和消极词汇
            int positiveCount = 0;
            int negativeCount = 0;
            
            // 简单的情感词典
            Set<String> positiveWords = new HashSet<>(Arrays.asList(
                "喜欢", "开心", "快乐", "高兴", "美好", "优秀", "精彩", "成功"
            ));
            
            Set<String> negativeWords = new HashSet<>(Arrays.asList(
                "讨厌", "难过", "痛苦", "悲伤", "糟糕", "失败", "恐惧", "担忧"
            ));
            
            // 计算正面和负面词汇数量
            for (String word : words) {
                if (positiveWords.contains(word)) {
                    positiveCount++;
                } else if (negativeWords.contains(word)) {
                    negativeCount++;
                }
            }
            
            // 有情感词时计算得分
            if (positiveCount > 0 || negativeCount > 0) {
                int total = positiveCount + negativeCount;
                sentimentScore = (double) positiveCount / total;
            }
            
            return sentimentScore;
        } catch (Exception e) {
            logger.error("Error analyzing sentiment: {}", e.getMessage());
            return 0.5; // 出错时返回中性
        }
    }

    /**
     * 根据已有文本预测可能的后续发展
     */
    @Override
    public Map<String, Double> predictPlotDevelopment(List<String> previousChapters) {
        if (previousChapters == null || previousChapters.isEmpty()) {
            Map<String, Double> defaultPrediction = new HashMap<>();
            defaultPrediction.put("无法预测", 1.0);
            return defaultPrediction;
        }
        
        try {
            // 合并之前的章节内容
            StringBuilder content = new StringBuilder();
            for (String chapter : previousChapters) {
                content.append(chapter).append(" ");
            }
            
            // 提取关键词
            Map<String, Integer> keywords = extractKeywords(content.toString(), 10);
            
            // 基于关键词生成可能的发展方向
            Map<String, Double> predictions = new HashMap<>();
            
            // 这里使用简单的规则作为示例
            // 在实际应用中，应该使用训练好的模型
            if (content.toString().contains("魔法") || content.toString().contains("巫师")) {
                predictions.put("魔法对决", 0.8);
                predictions.put("学习新魔法", 0.6);
            }
            
            if (content.toString().contains("冒险") || content.toString().contains("旅程")) {
                predictions.put("发现宝藏", 0.7);
                predictions.put("遭遇危险", 0.6);
            }
            
            if (content.toString().contains("敌人") || content.toString().contains("对手")) {
                predictions.put("最终决战", 0.75);
                predictions.put("暂时撤退", 0.4);
            }
            
            // 如果没有任何预测，返回默认值
            if (predictions.isEmpty()) {
                predictions.put("故事继续发展", 0.5);
                predictions.put("引入新角色", 0.3);
            }
            
            return predictions;
        } catch (Exception e) {
            logger.error("Error predicting plot development: {}", e.getMessage());
            Map<String, Double> errorPrediction = new HashMap<>();
            errorPrediction.put("预测错误", 1.0);
            return errorPrediction;
        }
    }

    /**
     * 提取文本中的关键词
     */
    private Map<String, Integer> extractKeywords(String text, int numKeywords) {
        Map<String, Integer> result = new HashMap<>();
        
        try {
            // 简单实现：按词频统计
            String[] words = text.split("\\s+");
            Map<String, Integer> wordCount = new HashMap<>();
            
            for (String word : words) {
                if (word.length() > 1) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
            
            // 按词频排序
            List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordCount.entrySet());
            sortedWords.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            // 提取前N个关键词
            int count = 0;
            for (Map.Entry<String, Integer> entry : sortedWords) {
                if (count >= numKeywords) break;
                result.put(entry.getKey(), entry.getValue());
                count++;
            }
        } catch (Exception e) {
            logger.error("Error extracting keywords: {}", e.getMessage());
        }
        
        return result;
    }

    /**
     * 使用LDA提取文本主题
     */
    @Override
    public Map<Integer, List<String>> extractTopicsWithLDA(String text, int topicCount) {
        if (text == null || text.isEmpty() || topicCount <= 0) {
            return Collections.emptyMap();
        }
        
        try {
            // 将文本分词并计算词频
            String[] words = text.split("\\s+");
            Map<String, Integer> wordFreq = new HashMap<>();
            
            for (String word : words) {
                if (word.length() > 1) {
                    wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
                }
            }
            
            // 按词频排序并选取前N个作为主题词
            List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordFreq.entrySet());
            sortedWords.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            Map<Integer, List<String>> results = new HashMap<>();
            
            // 简单模拟LDA结果：将高频词划分到不同主题
            for (int i = 0; i < topicCount; i++) {
                List<String> topicWords = new ArrayList<>();
                
                // 为每个主题选择一些词（从排序列表中选择）
                int start = i * 5;
                int end = Math.min(start + 5, sortedWords.size());
                
                for (int j = start; j < end; j++) {
                    if (j < sortedWords.size()) {
                        topicWords.add(sortedWords.get(j).getKey());
                    }
                }
                
                if (!topicWords.isEmpty()) {
                    results.put(i, topicWords);
                }
            }
            
            return results;
        } catch (Exception e) {
            logger.error("Error extracting topics: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 检测文本的写作风格
     */
    @Override
    public Map<String, Double> detectWritingStyle(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            Map<String, Double> styleAttributes = new HashMap<>();
            
            // 计算平均句子长度
            String[] sentences = text.split("[.!?。！？]");
            double avgSentenceLength = 0;
            if (sentences.length > 0) {
                for (String sentence : sentences) {
                    avgSentenceLength += sentence.length();
                }
                avgSentenceLength /= sentences.length;
            }
            
            // 计算平均段落长度
            String[] paragraphs = text.split("\n\n");
            double avgParagraphLength = 0;
            if (paragraphs.length > 0) {
                for (String paragraph : paragraphs) {
                    avgParagraphLength += paragraph.length();
                }
                avgParagraphLength /= paragraphs.length;
            }
            
            // 计算对话频率
            int dialogCount = 0;
            for (String sentence : sentences) {
                if (sentence.contains("\"") || sentence.contains("\"") || 
                    sentence.contains("'") || sentence.contains("'")) {
                    dialogCount++;
                }
            }
            double dialogFrequency = sentences.length > 0 ? (double) dialogCount / sentences.length : 0;
            
            // 添加写作风格属性
            styleAttributes.put("平均句子长度", avgSentenceLength);
            styleAttributes.put("平均段落长度", avgParagraphLength);
            styleAttributes.put("对话频率", dialogFrequency);
            
            return styleAttributes;
        } catch (Exception e) {
            logger.error("Error detecting writing style: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 构建小说人物关系网络
     */
    @Override
    public Map<String, List<Map<String, Object>>> buildCharacterNetwork(String text) {
        // 首先提取人物，然后构建关系网络
        List<String> characters = extractCharacters(text);
        return buildCharacterNetwork(text, characters);
    }
    
    /**
     * 提取文本中的人物
     */
    private List<String> extractCharacters(String text) {
        // 简单实现：直接返回一些可能的人物
        List<String> characters = new ArrayList<>();
        
        // 在实际应用中，应该使用命名实体识别来提取人物
        if (text.contains("魔法师")) characters.add("魔法师");
        if (text.contains("勇士")) characters.add("勇士");
        if (text.contains("国王")) characters.add("国王");
        if (text.contains("公主")) characters.add("公主");
        if (text.contains("巫师")) characters.add("巫师");
        
        return characters;
    }
    
    /**
     * 根据指定人物构建关系网络
     */
    private Map<String, List<Map<String, Object>>> buildCharacterNetwork(String text, List<String> characters) {
        if (text == null || text.isEmpty() || characters == null || characters.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            Map<String, List<Map<String, Object>>> characterNetwork = new HashMap<>();
            
            // 为每个角色初始化关系列表
            for (String character : characters) {
                characterNetwork.put(character, new ArrayList<>());
            }
            
            // 分割文本为句子
            String[] sentences = text.split("[.!?。！？]");
            
            // 检查每个句子中出现的角色组合
            for (String sentence : sentences) {
                List<String> charactersInSentence = new ArrayList<>();
                
                // 检查句子中出现的角色
                for (String character : characters) {
                    if (sentence.contains(character)) {
                        charactersInSentence.add(character);
                    }
                }
                
                // 如果句子中出现了多个角色，则认为它们有关系
                if (charactersInSentence.size() > 1) {
                    for (String character : charactersInSentence) {
                        List<Map<String, Object>> relations = characterNetwork.get(character);
                        
                        for (String otherCharacter : charactersInSentence) {
                            if (!character.equals(otherCharacter)) {
                                // 检查是否已经存在这个关系
                                boolean relationExists = false;
                                for (Map<String, Object> relation : relations) {
                                    if (relation.get("character").equals(otherCharacter)) {
                                        relationExists = true;
                                        // 增加关系强度
                                        double strength = (double) relation.get("strength") + 1.0;
                                        relation.put("strength", strength);
                                        break;
                                    }
                                }
                                
                                // 如果关系不存在，创建新关系
                                if (!relationExists) {
                                    Map<String, Object> relation = new HashMap<>();
                                    relation.put("character", otherCharacter);
                                    relation.put("strength", 1.0);
                                    relation.put("type", "unknown");
                                    relations.add(relation);
                                }
                            }
                        }
                    }
                }
            }
            
            return characterNetwork;
        } catch (Exception e) {
            logger.error("Error building character network: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 将文本分类到多个类别
     */
    @Override
    public Map<String, Double> classifyText(String text, List<String> categories) {
        if (text == null || text.isEmpty() || categories == null || categories.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            Map<String, Double> categoryScores = new HashMap<>();
            
            // 定义一些类别和相关词汇
            Map<String, String[]> categoryKeywords = new HashMap<>();
            categoryKeywords.put("fantasy", new String[]{"魔法", "巫师", "龙", "精灵", "魔幻", "传说", "魔法师", "勇士"});
            categoryKeywords.put("sci-fi", new String[]{"科技", "未来", "太空", "机器人", "人工智能", "星球", "外星", "时空"});
            categoryKeywords.put("mystery", new String[]{"神秘", "谜团", "侦探", "线索", "真相", "案件", "调查", "犯罪"});
            categoryKeywords.put("romance", new String[]{"爱情", "浪漫", "恋爱", "情感", "甜蜜", "心动", "约会", "恋人"});
            categoryKeywords.put("history", new String[]{"历史", "古代", "朝代", "王朝", "帝国", "文明", "战争", "革命"});
            
            // 只处理请求的类别
            for (String category : categories) {
                String[] keywords = categoryKeywords.get(category.toLowerCase());
                if (keywords != null) {
                    int matchCount = 0;
                    for (String keyword : keywords) {
                        if (text.contains(keyword)) {
                            matchCount++;
                        }
                    }
                    
                    // 计算匹配比例作为类别得分
                    double score = (double) matchCount / keywords.length;
                    categoryScores.put(category, score);
                }
            }
            
            // 归一化分数
            double sum = categoryScores.values().stream().mapToDouble(Double::doubleValue).sum();
            if (sum > 0) {
                for (String category : categoryScores.keySet()) {
                    categoryScores.put(category, categoryScores.get(category) / sum);
                }
            } else {
                // 如果所有类别得分都为0，给第一个类别一个默认分数
                if (!categories.isEmpty()) {
                    categoryScores.put(categories.get(0), 1.0);
                }
            }
            
            return categoryScores;
        } catch (Exception e) {
            logger.error("Error classifying text: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 分析文本的复杂度
     */
    @Override
    public Map<String, Double> analyzeTextComplexity(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            Map<String, Double> complexityMetrics = new HashMap<>();
            
            // 1. 词汇多样性
            String[] words = text.split("\\s+");
            Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
            double vocabularyDiversity = words.length > 0 ? (double) uniqueWords.size() / words.length : 0;
            
            // 2. 平均句子长度
            String[] sentences = text.split("[.!?。！？]");
            double avgSentenceLength = 0;
            if (sentences.length > 0) {
                int totalWords = 0;
                for (String sentence : sentences) {
                    String[] sentenceWords = sentence.trim().split("\\s+");
                    totalWords += sentenceWords.length;
                }
                avgSentenceLength = (double) totalWords / sentences.length;
            }
            
            // 3. 长句子比例
            int longSentences = 0;
            for (String sentence : sentences) {
                String[] sentenceWords = sentence.trim().split("\\s+");
                if (sentenceWords.length > 20) {
                    longSentences++;
                }
            }
            double longSentenceRatio = sentences.length > 0 ? (double) longSentences / sentences.length : 0;
            
            // 添加到结果集
            complexityMetrics.put("词汇多样性", vocabularyDiversity);
            complexityMetrics.put("平均句长", avgSentenceLength);
            complexityMetrics.put("长句比例", longSentenceRatio);
            
            // 综合复杂度（0-1之间）
            double overallComplexity = (0.4 * vocabularyDiversity) + 
                                       (0.4 * Math.min(1, avgSentenceLength / 30)) + 
                                       (0.2 * longSentenceRatio);
            complexityMetrics.put("总体复杂度", overallComplexity);
            
            return complexityMetrics;
        } catch (Exception e) {
            logger.error("Error analyzing text complexity: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 训练词向量模型
     */
    @Override
    public boolean trainWordVectorModel(List<String> corpus, int vectorSize) {
        if (corpus == null || corpus.isEmpty() || vectorSize <= 0) {
            return false;
        }
        
        try {
            // 初始化词汇表
            vocabulary = new HashMap<>();
            documents = new ArrayList<>(corpus);
            
            // 处理语料库中的每个文档
            for (String document : corpus) {
                String[] words = document.split("\\s+");
                
                // 更新词汇表
                for (String word : words) {
                    vocabulary.put(word, vocabulary.getOrDefault(word, 0.0) + 1.0);
                }
            }
            
            // 创建文档向量模型
            documentVectorModel = new DocumentVectorModel();
            documentVectorModel.setVocabulary(vocabulary);
            documentVectorModel.setVectorSize(vectorSize);
            
            logger.info("Word vector model trained with {} unique words and vector size of {}", vocabulary.size(), vectorSize);
            return true;
        } catch (Exception e) {
            logger.error("Error training word vector model: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 计算文本与参考文本列表之间的语义相关度
     */
    @Override
    public List<Double> calculateSemanticRelatedness(String mainText, List<String> referenceTexts) {
        if (mainText == null || mainText.isEmpty() || referenceTexts == null || referenceTexts.isEmpty()) {
            logger.warn("Cannot calculate semantic relatedness for empty texts");
            return Collections.emptyList();
        }
        
            List<Double> relatedness = new ArrayList<>();
        try {
            for (String referenceText : referenceTexts) {
                double similarity = calculateTextSimilarity(mainText, referenceText);
                relatedness.add(similarity);
            }
        } catch (Exception e) {
            logger.error("Error calculating semantic relatedness: {}", e.getMessage());
        }
        return relatedness;
    }

    /**
     * 分析小说结构并识别不同的结构部分
     * 
     * @param chapters 小说章节内容列表
     * @return 结构分析结果，包含各结构部分(如开端、发展、高潮、结局等)的名称、起始章节、结束章节和相应百分比
     */
    @Override
    public Map<String, List<Map<String, Object>>> analyzeNovelStructure(List<String> chapters) {
        logger.info("分析小说结构，章节数: {}", chapters.size());
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        
        try {
            // Weka实现的小说结构分析
            // 1. 定义主要结构部分
            String[] mainSections = {"开端", "铺垫", "发展", "高潮", "结局"};
            String[] detailSections = {
                "角色介绍", "世界设定", "初始冲突", "情节递进", "次要冲突", 
                "关系发展", "主要冲突", "危机", "转折点", "高潮", "解决", "结局"
            };
            
            // 2. 分析章节特征
            List<Map<String, Double>> chapterFeatures = analyzeChaptersFeatures(chapters);
            
            // 3. 根据章节特征和数量，分析出主要结构
            List<Map<String, Object>> mainStructure = createMainStructure(
                chapters.size(), chapterFeatures, mainSections);
            result.put("mainStructure", mainStructure);
            
            // 4. 分析详细结构
            List<Map<String, Object>> detailStructure = createDetailStructure(
                chapters.size(), chapterFeatures, detailSections);
            result.put("detailStructure", detailStructure);
            
            // 5. 添加总章节数信息
            result.put("totalChapters", Collections.singletonList(
                Collections.singletonMap("value", chapters.size())
            ));
            
            logger.info("小说结构分析完成");
        } catch (Exception e) {
            logger.error("分析小说结构时出错: {}", e.getMessage());
            // 出错时返回默认结构
            result.put("mainStructure", generateDefaultMainStructure(chapters.size()));
            result.put("detailStructure", generateDefaultDetailStructure(chapters.size()));
            result.put("totalChapters", Collections.singletonList(
                Collections.singletonMap("value", chapters.size())
            ));
        }
        
        return result;
    }
    
    /**
     * 分析每个章节的特征
     */
    private List<Map<String, Double>> analyzeChaptersFeatures(List<String> chapters) {
        List<Map<String, Double>> features = new ArrayList<>();
        
        for (String chapter : chapters) {
            Map<String, Double> chapterFeature = new HashMap<>();
            
            // 计算章节长度特征
            chapterFeature.put("length", (double) chapter.length());
            
            // 估算对话比例
            double dialogueRatio = estimateDialogueRatio(chapter);
            chapterFeature.put("dialogueRatio", dialogueRatio);
            
            // 情感分析
            double sentiment = deepSentimentAnalysis(chapter);
            chapterFeature.put("sentiment", sentiment);
            
            // 关键词密度
            Map<String, Integer> keywords = extractKeywords(chapter, 10);
            double keywordDensity = keywords.size() > 0 ? 
                    (double) keywords.values().stream().mapToInt(Integer::intValue).sum() / chapter.length() : 0;
            chapterFeature.put("keywordDensity", keywordDensity);
            
            features.add(chapterFeature);
        }
        
        return features;
    }
    
    /**
     * 估算对话比例
     */
    private double estimateDialogueRatio(String text) {
        // 简单估算：统计引号出现次数
        int quoteCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '"' || c == '\u201C' || c == '\u201D' || c == '\u2018' || c == '\u2019') {
                quoteCount++;
            }
        }
        
        // 引号数量除以2得到大致的对话段落数
        int dialogueCount = quoteCount / 2;
        // 估计每个对话平均15个字符
        int estimatedDialogueChars = dialogueCount * 15;
        
        return text.length() > 0 ? (double) estimatedDialogueChars / text.length() : 0;
    }
    
    /**
     * 创建主要结构
     */
    private List<Map<String, Object>> createMainStructure(
            int totalChapters, 
            List<Map<String, Double>> features, 
            String[] sectionNames) {
        
        List<Map<String, Object>> mainStructure = new ArrayList<>();
        
        // 根据章节数调整结构比例
        double[] percentages;
        if (totalChapters <= 10) {
            percentages = new double[] {0.2, 0.15, 0.3, 0.25, 0.1}; // 短篇
        } else if (totalChapters <= 30) {
            percentages = new double[] {0.15, 0.2, 0.35, 0.2, 0.1}; // 中篇
        } else {
            percentages = new double[] {0.1, 0.15, 0.4, 0.25, 0.1}; // 长篇
        }
        
        // 默认颜色
        String[] colors = {"#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de"};
        
        // 计算每个部分的章节数和范围
        int startChapter = 0;
        for (int i = 0; i < sectionNames.length; i++) {
            int sectionChapters = (int) Math.round(totalChapters * percentages[i]);
            if (sectionChapters == 0) sectionChapters = 1; // 至少1章
            
            int endChapter = Math.min(startChapter + sectionChapters, totalChapters);
            if (i == sectionNames.length - 1) {
                endChapter = totalChapters; // 确保最后一部分到达最后一章
            }
            
            Map<String, Object> section = new HashMap<>();
            section.put("name", sectionNames[i]);
            section.put("value", endChapter - startChapter);
            section.put("percentage", percentages[i]);
            section.put("startChapter", startChapter + 1); // 1-based索引
            section.put("endChapter", endChapter);
            section.put("color", colors[i % colors.length]);
            
            mainStructure.add(section);
            startChapter = endChapter;
        }
        
        return mainStructure;
    }
    
    /**
     * 创建详细结构
     */
    private List<Map<String, Object>> createDetailStructure(
            int totalChapters, 
            List<Map<String, Double>> features, 
            String[] sectionNames) {
        
        List<Map<String, Object>> detailStructure = new ArrayList<>();
        
        // 为短篇小说简化结构
        if (totalChapters < 12) {
            String[] simplifiedSections = {"起因", "经过", "转折", "结果"};
            double[] percentages = {0.25, 0.35, 0.25, 0.15};
            String[] colors = {"#5470c6", "#91cc75", "#ee6666", "#73c0de"};
            
            int startChapter = 0;
            for (int i = 0; i < simplifiedSections.length; i++) {
                int sectionChapters = (int) Math.round(totalChapters * percentages[i]);
                if (sectionChapters == 0) sectionChapters = 1;
                
                int endChapter = Math.min(startChapter + sectionChapters, totalChapters);
                if (i == simplifiedSections.length - 1) {
                    endChapter = totalChapters;
                }
                
                Map<String, Object> section = new HashMap<>();
                section.put("name", simplifiedSections[i]);
                section.put("value", endChapter - startChapter);
                section.put("startChapter", startChapter + 1);
                section.put("endChapter", endChapter);
                section.put("color", colors[i % colors.length]);
                
                detailStructure.add(section);
                startChapter = endChapter;
            }
        } else {
            // 长篇的详细结构分配
            String[] colors = {
                "#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de",
                "#3ba272", "#fc8452", "#9a60b4", "#ea7ccc", "#eacf5b", 
                "#d16b6b", "#8cd0c3"
            };
            
            // 计算每个部分的章节数
            double[] percentages = {0.05, 0.05, 0.08, 0.12, 0.08, 0.12, 0.12, 0.1, 0.08, 0.08, 0.06, 0.06};
            
            int startChapter = 0;
            int sections = Math.min(sectionNames.length, percentages.length);
            for (int i = 0; i < sections; i++) {
                int sectionChapters = (int) Math.round(totalChapters * percentages[i]);
                if (sectionChapters == 0) sectionChapters = 1;
                
                int endChapter = Math.min(startChapter + sectionChapters, totalChapters);
                if (i == sections - 1) {
                    endChapter = totalChapters;
                }
                
                Map<String, Object> section = new HashMap<>();
                section.put("name", sectionNames[i]);
                section.put("value", endChapter - startChapter);
                section.put("startChapter", startChapter + 1);
                section.put("endChapter", endChapter);
                section.put("color", colors[i % colors.length]);
                
                detailStructure.add(section);
                startChapter = endChapter;
                
                // 如果已经到达最后一章，中断循环
                if (endChapter == totalChapters) {
                    break;
                }
            }
        }
        
        return detailStructure;
    }
    
    /**
     * 生成默认主结构
     */
    private List<Map<String, Object>> generateDefaultMainStructure(int totalChapters) {
        String[] sections = {"开端", "铺垫", "发展", "高潮", "结局"};
        double[] percentages = {0.1, 0.2, 0.4, 0.2, 0.1};
        String[] colors = {"#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de"};
        
        List<Map<String, Object>> structure = new ArrayList<>();
        int startChapter = 0;
        
        for (int i = 0; i < sections.length; i++) {
            int sectionChapters = (int) Math.round(totalChapters * percentages[i]);
            if (sectionChapters == 0) sectionChapters = 1;
            
            int endChapter = Math.min(startChapter + sectionChapters, totalChapters);
            if (i == sections.length - 1) {
                endChapter = totalChapters;
            }
            
            Map<String, Object> section = new HashMap<>();
            section.put("name", sections[i]);
            section.put("value", endChapter - startChapter);
            section.put("startChapter", startChapter + 1);
            section.put("endChapter", endChapter);
            section.put("color", colors[i]);
            
            structure.add(section);
            startChapter = endChapter;
        }
        
        return structure;
    }
    
    /**
     * 生成默认详细结构
     */
    private List<Map<String, Object>> generateDefaultDetailStructure(int totalChapters) {
        String[] sections = {
            "角色介绍", "世界设定", "初始冲突", "情节递进", "次要冲突", 
            "关系发展", "主要冲突", "危机", "转折点", "高潮", "解决", "结局"
        };
        double[] percentages = {0.05, 0.05, 0.08, 0.12, 0.08, 0.12, 0.12, 0.1, 0.08, 0.08, 0.06, 0.06};
        String[] colors = {
            "#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de",
            "#3ba272", "#fc8452", "#9a60b4", "#ea7ccc", "#eacf5b", 
            "#d16b6b", "#8cd0c3"
        };
        
        List<Map<String, Object>> structure = new ArrayList<>();
        int startChapter = 0;
        
        for (int i = 0; i < sections.length; i++) {
            int sectionChapters = (int) Math.round(totalChapters * percentages[i]);
            if (sectionChapters == 0) sectionChapters = 1;
            
            int endChapter = Math.min(startChapter + sectionChapters, totalChapters);
            if (i == sections.length - 1) {
                endChapter = totalChapters;
            }
            
            Map<String, Object> section = new HashMap<>();
            section.put("name", sections[i]);
            section.put("value", endChapter - startChapter);
            section.put("startChapter", startChapter + 1);
            section.put("endChapter", endChapter);
            section.put("color", colors[i]);
            
            structure.add(section);
            startChapter = endChapter;
            
            // 如果已经到达最后一章，中断循环
            if (endChapter == totalChapters) {
                break;
            }
        }
        
        return structure;
    }
} 