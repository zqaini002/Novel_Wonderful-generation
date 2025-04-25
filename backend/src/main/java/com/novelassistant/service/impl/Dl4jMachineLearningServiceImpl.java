package com.novelassistant.service.impl;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Chapter;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.service.MachineLearningService;
import com.novelassistant.service.NlpService;
import com.novelassistant.util.RequestContextHolder;
import lombok.RequiredArgsConstructor;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于DL4J机器学习算法的文本分析服务实现类
 */
@Service
@Primary
public class Dl4jMachineLearningServiceImpl implements MachineLearningService {

    private static final Logger logger = LoggerFactory.getLogger(Dl4jMachineLearningServiceImpl.class);
    
    private static final String MODEL_PATH = "models/sgns.zhihu.word";
    private static final int DEFAULT_VECTOR_SIZE = 100;
    
    @Autowired
    @Lazy
    private NlpService nlpService;
    
    @Autowired
    private NovelRepository novelRepository;
    
    private Word2Vec word2Vec;
    
    /**
     * 初始化词向量模型
     * 如果存在预训练模型则加载，否则使用默认词向量
     */
    private void initWordVectors() {
        try {
            // 使用绝对路径加载模型文件
            File modelFile = new File("E:/Novel_Wonderful generation/novel-assistant/backend/src/main/resources/models/45000-small.txt");
            
            if (modelFile.exists()) {
                logger.info("加载预训练词向量模型: {}", modelFile.getAbsolutePath());
                logger.info("模型加载中，请耐心等待...");
                
                // 显示加载进度条
                Thread progressThread = new Thread(() -> {
                    String[] progressChars = {"|", "/", "-", "\\"};
                    int counter = 0;
                    while (!Thread.currentThread().isInterrupted()) {
                        System.out.print("\r加载词向量模型中 " + progressChars[counter % 4] + " ");
                        counter++;
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    System.out.println("\r加载词向量模型完成!                ");
                });
                
                progressThread.setDaemon(true);
                progressThread.start();
                
                try {
                    long startTime = System.currentTimeMillis();
                    
                    // 尝试多种方式加载模型文件
                    try {
                        // 方法1: 使用标准Text格式加载
                        logger.info("尝试使用Text格式加载词向量...");
                        word2Vec = WordVectorSerializer.readWord2Vec(modelFile);
                    } catch (Exception e1) {
                        logger.warn("使用Text格式加载失败: {}", e1.getMessage());
                        
                try {
                            // 方法2: 使用Word2Vec专用格式加载
                            logger.info("尝试使用Word2Vec专用格式加载...");
                            word2Vec = WordVectorSerializer.readWord2VecModel(modelFile, false);
                        } catch (Exception e2) {
                            logger.warn("使用Word2Vec专用格式加载失败: {}", e2.getMessage());
                            
                            try {
                                // 方法3: 使用二进制格式加载
                                logger.info("尝试使用二进制格式加载...");
                                word2Vec = WordVectorSerializer.readBinaryModel(modelFile, true, true);
                            } catch (Exception e3) {
                                // 所有加载方法都失败，抛出异常
                                logger.error("所有尝试的加载方法都失败");
                                throw new RuntimeException("无法加载词向量模型，所有尝试方法均失败", e3);
                            }
                        }
                    }
                    
                    long endTime = System.currentTimeMillis();
                    
                    // 停止进度条线程
                    progressThread.interrupt();
                    
                    logger.info("词向量模型加载成功，词汇表大小: {}", word2Vec.getVocab().numWords());
                    logger.info("模型加载耗时: {} 秒", (endTime - startTime) / 1000.0);
                } catch (Exception e) {
                    // 停止进度条线程
                    progressThread.interrupt();
                    logger.error("加载词向量模型失败", e);
                    logger.info("未找到预训练模型，将使用基础分析方法");
                }
            } else {
                logger.info("未找到预训练模型文件: {}，将使用基础分析方法", modelFile.getAbsolutePath());
                
                // 如果需要，可以创建models目录
                try {
                // 创建一个临时目录确保路径存在
                    File tempDir = new File("models");
                    if (!tempDir.exists()) {
                        tempDir.mkdirs();
                        logger.info("创建了models目录，用于将来保存模型");
                    }
                } catch (Exception ex) {
                    logger.warn("无法创建models目录: {}", ex.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("检查词向量模型时出错", e);
            logger.info("未找到预训练模型，将使用基础分析方法");
        }
    }
    
    /**
     * 构造函数
     */
    public Dl4jMachineLearningServiceImpl() {
        // 初始化词向量模型
        initWordVectors();
    }
    
    /**
     * 添加一个默认节点，确保即使没有提取到角色也能返回有效结果
     */
    private void addDefaultNode(List<Map<String, Object>> nodes) {
        Map<String, Object> defaultNode = new HashMap<>();
        defaultNode.put("id", 1);
        defaultNode.put("name", "未知角色");
        defaultNode.put("category", "未分类");
        defaultNode.put("value", 1);
        defaultNode.put("symbolSize", 50);
        defaultNode.put("importance", 1.0);
        nodes.add(defaultNode);
    }
    
    /**
     * Triple类用于存储两个角色及其共现次数
     */
    private static class Triple {
        String character1;
        String character2;
        Integer count;
        
        public Triple(String character1, String character2, Integer count) {
            // 确保character1和character2按字典序排列，防止重复
            if (character1.compareTo(character2) <= 0) {
                this.character1 = character1;
                this.character2 = character2;
            } else {
                this.character1 = character2;
                this.character2 = character1;
            }
            this.count = count;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Triple triple = (Triple) o;
            return Objects.equals(character1, triple.character1) &&
                   Objects.equals(character2, triple.character2);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(character1, character2);
        }
    }
    
    @Override
    public double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }
        
        try {
            if (word2Vec != null) {
                // 使用词向量计算两个文本的相似度
                return word2Vec.similarity(text1, text2);
            } else {
                // 回退到简单相似度计算
                return calculateSimpleSimilarity(text1, text2);
            }
        } catch (Exception e) {
            logger.error("计算文本相似度失败", e);
            // 回退到简单相似度计算
            return calculateSimpleSimilarity(text1, text2);
        }
    }
    
    private double calculateSimpleSimilarity(String text1, String text2) {
        // 使用TF-IDF特征的余弦相似度
        
        // 计算关键词
        Map<String, Integer> keywords1 = nlpService.extractKeywords(text1, 30);
        Map<String, Integer> keywords2 = nlpService.extractKeywords(text2, 30);
        
        // 合并关键词集
        Set<String> allKeywords = new HashSet<>(keywords1.keySet());
        allKeywords.addAll(keywords2.keySet());
        
        // 计算TF-IDF向量
        double[] vector1 = new double[allKeywords.size()];
        double[] vector2 = new double[allKeywords.size()];
        
        int i = 0;
        for (String keyword : allKeywords) {
            vector1[i] = keywords1.getOrDefault(keyword, 0) / 100.0;
            vector2[i] = keywords2.getOrDefault(keyword, 0) / 100.0;
            i++;
        }
        
        // 计算余弦相似度
        return cosineSimilarity(vector1, vector2);
    }
    
    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    @Override
    public Map<Integer, List<Integer>> clusterTexts(List<String> textList, int clusterCount) {
        if (textList == null || textList.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            // 创建特征向量
            List<double[]> vectors = new ArrayList<>();
            
            for (String text : textList) {
                // 将文本转换为特征向量
                double[] vector = createFeatureVector(text);
                vectors.add(vector);
            }
            
            // 转换为二维数组
            double[][] data = vectors.toArray(new double[0][]);
            
            // 使用简单K-means实现替代SMILE库
            return performKMeansClustering(data, clusterCount);
        } catch (Exception e) {
            logger.error("聚类分析失败", e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * 执行K-means聚类
     */
    private Map<Integer, List<Integer>> performKMeansClustering(double[][] data, int k) {
        int n = data.length;
        if (n < k) {
            k = n;
        }
        
        // 随机选择初始中心点
        Random rand = new Random(42);
        int[] centers = new int[k];
        boolean[] chosen = new boolean[n];
        
        for (int i = 0; i < k; i++) {
            int idx;
            do {
                idx = rand.nextInt(n);
            } while (chosen[idx]);
            
            centers[i] = idx;
            chosen[idx] = true;
        }
        
        // 分配点到最近的中心点
        int[] labels = new int[n];
        boolean changed;
        
        // 迭代直到收敛或达到最大迭代次数
        int maxIterations = 100;
        for (int iter = 0; iter < maxIterations; iter++) {
            changed = false;
            
            // 分配阶段
            for (int i = 0; i < n; i++) {
                double minDist = Double.MAX_VALUE;
                int bestCluster = 0;
                
                for (int j = 0; j < k; j++) {
                    double dist = distance(data[i], data[centers[j]]);
                    if (dist < minDist) {
                        minDist = dist;
                        bestCluster = j;
                    }
                }
                
                if (labels[i] != bestCluster) {
                    changed = true;
                    labels[i] = bestCluster;
                }
            }
            
            // 更新阶段
            int[] clusterSize = new int[k];
            for (int i = 0; i < n; i++) {
                clusterSize[labels[i]]++;
            }
            
            int[] newCenters = new int[k];
            for (int j = 0; j < k; j++) {
                if (clusterSize[j] == 0) {
                    // 如果有空的簇，随机选择新的中心
                    newCenters[j] = rand.nextInt(n);
                    continue;
                }
                
                double minSumDist = Double.MAX_VALUE;
                
                for (int i = 0; i < n; i++) {
                    if (labels[i] != j) continue;
                    
                    double sumDist = 0;
                    for (int l = 0; l < n; l++) {
                        if (labels[l] != j) continue;
                        sumDist += distance(data[i], data[l]);
                    }
                    
                    if (sumDist < minSumDist) {
                        minSumDist = sumDist;
                        newCenters[j] = i;
                    }
                }
            }
            
            for (int j = 0; j < k; j++) {
                if (centers[j] != newCenters[j]) {
                    changed = true;
                    centers[j] = newCenters[j];
                }
            }
            
            if (!changed) {
                break;
            }
        }
        
        // 整理结果
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int clusterId = labels[i];
            if (!result.containsKey(clusterId)) {
                result.put(clusterId, new ArrayList<>());
            }
            result.get(clusterId).add(i);
        }
        
        return result;
    }
    
    /**
     * 计算欧几里得距离
     */
    private double distance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    /**
     * 计算标点符号因素
     */
    private double calculatePunctuationFactor(String text) {
        // 计算标点符号频率
        long punctuationCount = 0;
        String punctuations = "，。！？；：''【】《》、,.!?;:\"'[]<>";
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (punctuations.indexOf(c) >= 0) {
                punctuationCount++;
            }
        }
        
        double freq = (double) punctuationCount / text.length();
        
        // 感叹号和问号对情感的影响更大
        long emotionalPunctCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ("！!？?".indexOf(c) >= 0) {
                emotionalPunctCount++;
            }
        }
        
        // 标点因子权重 = 标点频率 * 0.5 + 情感标点频率 * 0.5
        double emotionalFreq = (double) emotionalPunctCount / text.length();
        return freq * 0.5 + emotionalFreq * 0.5;
    }
    
    /**
     * 创建文本的特征向量
     */
    private double[] createFeatureVector(String text) {
        if (word2Vec != null) {
            try {
                // 使用Word2Vec创建文本向量
                INDArray textVector = word2Vec.getWordVectorsMean(tokenizeText(text));
                if (textVector != null) {
                    return textVector.toDoubleVector();
                }
            } catch (Exception e) {
                logger.debug("使用Word2Vec创建特征向量失败，回退到TF-IDF: {}", e.getMessage());
            }
        }
        
        // 回退到TF-IDF特征
        Map<String, Integer> keywords = nlpService.extractKeywords(text, 50);
        
        // 简化版特征向量，使用关键词权重
        double[] features = new double[50];
        int i = 0;
        for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
            if (i < 50) {
                features[i] = entry.getValue() / 100.0; // 归一化权重
                i++;
            } else {
                break;
            }
        }
        
        return features;
    }
    
    /**
     * 对文本进行分词
     */
    private Collection<String> tokenizeText(String text) {
        List<Term> terms = HanLP.segment(text);
        return terms.stream()
                .map(term -> term.word)
                .collect(Collectors.toList());
    }
    
    @Override
    public double deepSentimentAnalysis(String text) {
        // 目前使用传统NLP服务的情感分析，后续可替换为深度学习模型
        double basicSentiment = nlpService.analyzeSentiment(text);
        
        // 基于更多特征的情感分析增强
        double enhancedSentiment = enhanceSentimentAnalysis(text, basicSentiment);
        
        return enhancedSentiment;
    }
    
    /**
     * 增强情感分析
     */
    private double enhanceSentimentAnalysis(String text, double basicSentiment) {
        // 考虑更多文本特征进行情感分析
        return basicSentiment;
    }
    
    @Override
    public Map<String, Double> predictPlotDevelopment(List<String> previousChapters) {
        if (previousChapters == null || previousChapters.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String, Double> predictions = new HashMap<>();
        
        try {
            // 提取每个章节的关键词和主题
            List<Map<String, Integer>> chapterKeywords = new ArrayList<>();
            for (String chapter : previousChapters) {
                chapterKeywords.add(nlpService.extractKeywords(chapter, 20));
            }
            
            // 分析关键词趋势
            Map<String, List<Integer>> keywordTrends = analyzeKeywordTrends(chapterKeywords);
            
            // 基于关键词趋势预测可能的发展
            predictions = predictBasedOnTrends(keywordTrends);
            
            // 加入基于人物的预测
            Map<String, Double> characterPredictions = predictBasedOnCharacters(previousChapters);
            predictions.putAll(characterPredictions);
            
        } catch (Exception e) {
            logger.error("预测情节发展时出错", e);
        }
        
        return predictions;
    }
    
    /**
     * 分析关键词随章节变化的趋势
     */
    private Map<String, List<Integer>> analyzeKeywordTrends(List<Map<String, Integer>> chapterKeywords) {
        Map<String, List<Integer>> trends = new HashMap<>();
        
        // 获取所有关键词
        Set<String> allKeywords = new HashSet<>();
        for (Map<String, Integer> chapter : chapterKeywords) {
            allKeywords.addAll(chapter.keySet());
        }
        
        // 对每个关键词，记录其在各章节中的权重
        for (String keyword : allKeywords) {
            List<Integer> weights = new ArrayList<>();
            for (Map<String, Integer> chapter : chapterKeywords) {
                weights.add(chapter.getOrDefault(keyword, 0));
            }
            trends.put(keyword, weights);
        }
        
        return trends;
    }
    
    /**
     * 基于关键词趋势预测
     */
    private Map<String, Double> predictBasedOnTrends(Map<String, List<Integer>> keywordTrends) {
        Map<String, Double> predictions = new HashMap<>();
        
        // 寻找权重呈上升趋势的关键词
        for (Map.Entry<String, List<Integer>> entry : keywordTrends.entrySet()) {
            String keyword = entry.getKey();
            List<Integer> weights = entry.getValue();
            
            if (weights.size() < 2) {
                continue;
            }
            
            // 计算趋势斜率
            double slope = calculateTrendSlope(weights);
            
            // 上升趋势强的关键词可能在未来情节中更重要
            if (slope > 0.5) {
                predictions.put("关键情节:" + keyword, 0.7 + slope * 0.2);
            }
        }
        
        return predictions;
    }
    
    /**
     * 计算趋势斜率
     */
    private double calculateTrendSlope(List<Integer> values) {
        if (values.size() < 2) {
            return 0;
        }
        
        // 简单线性回归
        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += i * i;
        }
        
        // 计算斜率
        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) {
            return 0;
        }
        
        double slope = (n * sumXY - sumX * sumY) / denominator;
        
        // 归一化
        return Math.min(1, Math.max(-1, slope / 100));
    }
    
    /**
     * 基于人物角色预测
     */
    private Map<String, Double> predictBasedOnCharacters(List<String> chapters) {
        Map<String, Double> predictions = new HashMap<>();
        
        // 提取所有章节的人物角色
        List<String> allCharacters = new ArrayList<>();
        for (String chapter : chapters) {
            allCharacters.addAll(nlpService.extractCharacters(chapter));
        }
        
        // 统计每个人物出现的频率
        Map<String, Integer> characterCounts = new HashMap<>();
        for (String character : allCharacters) {
            characterCounts.put(character, characterCounts.getOrDefault(character, 0) + 1);
        }
        
        // 找出最主要的人物
        List<Map.Entry<String, Integer>> sortedCharacters = characterCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());
        
        // 对主要人物进行预测
        for (Map.Entry<String, Integer> entry : sortedCharacters) {
            String character = entry.getKey();
            int count = entry.getValue();
            
            double probability = Math.min(0.9, 0.5 + count * 0.05);
            predictions.put("人物发展:" + character, probability);
        }
        
        return predictions;
    }
    
    @Override
    public Map<Integer, List<String>> extractTopicsWithLDA(String text, int topicCount) {
        Map<Integer, List<String>> result = new HashMap<>();
        
        try {
            // 分词
            List<Term> terms = HanLP.segment(text);
            List<String> tokens = terms.stream()
                    .map(term -> term.word)
                    .collect(Collectors.toList());
            
            // 构建简单的文档-词矩阵，不依赖SMILE库
            // 为简化实现，仅提取频繁词作为主题
            Map<String, Integer> wordFreq = new HashMap<>();
            for (String token : tokens) {
                if (token.length() > 1) { // 过滤掉单字词
                    wordFreq.put(token, wordFreq.getOrDefault(token, 0) + 1);
                }
            }
            
            // 按频率排序
            List<Map.Entry<String, Integer>> sortedWords = wordFreq.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList());
            
            // 分割为主题，每个主题包含部分高频词
            int wordsPerTopic = Math.min(10, Math.max(1, sortedWords.size() / topicCount));
            
            for (int i = 0; i < topicCount && i * wordsPerTopic < sortedWords.size(); i++) {
                List<String> topicWords = new ArrayList<>();
                
                for (int j = 0; j < wordsPerTopic && i * wordsPerTopic + j < sortedWords.size(); j++) {
                    topicWords.add(sortedWords.get(i * wordsPerTopic + j).getKey());
                }
                
                result.put(i, topicWords);
            }
            
        } catch (Exception e) {
            logger.error("主题提取失败", e);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Double> detectWritingStyle(String text) {
        Map<String, Double> styleFeatures = new HashMap<>();
        
        try {
            // 计算基本文本统计特征
            calculateBasicTextFeatures(text, styleFeatures);
            
            // 分析句子复杂度
            analyzeSentenceComplexity(text, styleFeatures);
            
            // 分析词汇丰富度
            analyzeVocabularyRichness(text, styleFeatures);
            
            // 分析修辞手法
            analyzeRhetoricalDevices(text, styleFeatures);
            
            // 识别特定风格标记
            identifyStyleMarkers(text, styleFeatures);
            
        } catch (Exception e) {
            logger.error("文本风格分析失败", e);
        }
        
        return styleFeatures;
    }
    
    /**
     * 计算基本文本统计特征
     */
    private void calculateBasicTextFeatures(String text, Map<String, Double> features) {
        // 段落数量
        String[] paragraphs = text.split("\\n+");
        features.put("段落数量", (double) paragraphs.length);
        
        // 句子数量
        String[] sentences = text.split("[。！？.!?]+");
        features.put("句子数量", (double) sentences.length);
        
        // 平均句子长度
        double totalLength = 0;
        for (String sentence : sentences) {
            totalLength += sentence.length();
        }
        double avgSentenceLength = sentences.length > 0 ? totalLength / sentences.length : 0;
        features.put("平均句长", avgSentenceLength);
        
        // 标点符号频率
        long punctuationCount = text.chars().filter(c -> "，。！？；：''【】《》、,.!?;:\"\'[]<>".indexOf(c) >= 0).count();
        features.put("标点符号频率", (double) punctuationCount / text.length());
    }
    
    /**
     * 分析句子复杂度
     */
    private void analyzeSentenceComplexity(String text, Map<String, Double> features) {
        String[] sentences = text.split("[。！？.!?]+");
        
        // 计算复合句比例
        int complexSentenceCount = 0;
        for (String sentence : sentences) {
            if (sentence.contains("，") || sentence.contains("；") || 
                sentence.contains(",") || sentence.contains(";")) {
                complexSentenceCount++;
            }
        }
        
        double complexRatio = sentences.length > 0 ? (double) complexSentenceCount / sentences.length : 0;
        features.put("复合句比例", complexRatio);
        
        // 分析句式变化
        double sentenceLengthVariance = calculateVariance(Arrays.stream(sentences)
                .mapToDouble(String::length)
                .toArray());
        features.put("句长变化", sentenceLengthVariance);
    }
    
    /**
     * 计算方差
     */
    private double calculateVariance(double[] values) {
        if (values.length == 0) {
            return 0;
        }
        
        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values)
                .map(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        
        return variance;
    }
    
    /**
     * 分析词汇丰富度
     */
    private void analyzeVocabularyRichness(String text, Map<String, Double> features) {
        List<Term> terms = HanLP.segment(text);
        List<String> tokens = terms.stream()
                .map(term -> term.word)
                .collect(Collectors.toList());
        
        // 词汇量
        Set<String> uniqueTokens = new HashSet<>(tokens);
        features.put("词汇量", (double) uniqueTokens.size());
        
        // 词汇多样性 (TTR: Type-Token Ratio)
        double ttr = tokens.isEmpty() ? 0 : (double) uniqueTokens.size() / tokens.size();
        features.put("词汇多样性", ttr);
        
        // 罕见词比例
        // 用简单方法估计：长度大于3的词视为可能的罕见词
        long rareWordCount = tokens.stream().filter(t -> t.length() > 3).count();
        double rareWordRatio = tokens.isEmpty() ? 0 : (double) rareWordCount / tokens.size();
        features.put("罕见词比例", rareWordRatio);
    }
    
    /**
     * 分析修辞手法
     */
    private void analyzeRhetoricalDevices(String text, Map<String, Double> features) {
        // 重复修辞
        double repetitionScore = detectRepetition(text);
        features.put("重复修辞", repetitionScore);
        
        // 对比修辞
        double contrastScore = detectContrast(text);
        features.put("对比修辞", contrastScore);
        
        // 比喻修辞
        double metaphorScore = detectMetaphor(text);
        features.put("比喻修辞", metaphorScore);
    }
    
    /**
     * 检测重复修辞
     */
    private double detectRepetition(String text) {
        // 简单检测：查找连续的相同词或相似结构
        String[] sentences = text.split("[。！？.!?]+");
        
        int repetitionCount = 0;
        for (String sentence : sentences) {
            List<String> tokens = HanLP.segment(sentence).stream()
                    .map(term -> term.word)
                    .collect(Collectors.toList());
            
            // 检查连续相同词
            for (int i = 0; i < tokens.size() - 1; i++) {
                if (tokens.get(i).equals(tokens.get(i + 1))) {
                    repetitionCount++;
                }
            }
        }
        
        return Math.min(1.0, repetitionCount / 10.0);
    }
    
    /**
     * 检测对比修辞
     */
    private double detectContrast(String text) {
        // 检测对比词组
        String[] contrastPatterns = {"虽然", "但是", "一方面", "另一方面", "不是", "而是"};
        
        int contrastCount = 0;
        for (String pattern : contrastPatterns) {
            int index = -1;
            while ((index = text.indexOf(pattern, index + 1)) >= 0) {
                contrastCount++;
            }
        }
        
        return Math.min(1.0, contrastCount / 10.0);
    }
    
    /**
     * 检测比喻修辞
     */
    private double detectMetaphor(String text) {
        // 检测比喻标记词
        String[] metaphorPatterns = {"像", "如", "好比", "仿佛", "宛如", "恰似"};
        
        int metaphorCount = 0;
        for (String pattern : metaphorPatterns) {
            int index = -1;
            while ((index = text.indexOf(pattern, index + 1)) >= 0) {
                metaphorCount++;
            }
        }
        
        return Math.min(1.0, metaphorCount / 5.0);
    }
    
    /**
     * 识别特定风格标记
     */
    private void identifyStyleMarkers(String text, Map<String, Double> features) {
        // 识别不同文学风格的特征
        // 散文化特征
        double proseStyleScore = detectProseStyle(text);
        features.put("散文化风格", proseStyleScore);
        
        // 叙事风格
        double narrativeStyleScore = detectNarrativeStyle(text);
        features.put("叙事风格", narrativeStyleScore);
        
        // 对话密度
        double dialogueDensity = detectDialogueDensity(text);
        features.put("对话密度", dialogueDensity);
        
        // 抒情风格
        double lyricalStyleScore = detectLyricalStyle(text);
        features.put("抒情风格", lyricalStyleScore);
    }
    
    /**
     * 检测散文化风格
     */
    private double detectProseStyle(String text) {
        // 关键词数组
        String[] proseWords = {"宛如", "仿佛", "恍若", "似乎", "好像", "犹如", "如同", "一般", 
                             "简直", "就像", "有如", "好比", "彷佛", "恰似", "就如"};
        
        // 描述性词汇（形容词）比例
        List<Term> terms = HanLP.segment(text);
        
        long adjCount = 0;
        for (Term term : terms) {
            if (term.nature != null && term.nature.toString().startsWith("a")) {
                adjCount++;
            }
        }
        
        double adjRatio = terms.isEmpty() ? 0 : (double) adjCount / terms.size();
        
        // 计算关键词得分
        double keywordScore = calculateKeywordScore(text, proseWords);
        
        // 加权平均
        return 0.5 * adjRatio + 0.5 * keywordScore;
    }
    
    /**
     * 检测叙事风格
     */
    private double detectNarrativeStyle(String text) {
        // 叙事特征：使用过去时态，第三人称，情节发展词汇
        
        // 检测过去时态标记
        String[] pastTenseMarkers = {"了", "过", "已经", "曾经"};
        int pastTenseCount = 0;
        for (String marker : pastTenseMarkers) {
            int index = -1;
            while ((index = text.indexOf(marker, index + 1)) >= 0) {
                pastTenseCount++;
            }
        }
        
        // 检测第三人称
        String[] thirdPersonMarkers = {"他", "她", "它", "他们", "她们", "它们"};
        int thirdPersonCount = 0;
        for (String marker : thirdPersonMarkers) {
            int index = -1;
            while ((index = text.indexOf(marker, index + 1)) >= 0) {
                thirdPersonCount++;
            }
        }
        
        // 情节发展词汇
        String[] plotMarkers = {"接着", "然后", "之后", "随即", "最终", "终于"};
        int plotMarkerCount = 0;
        for (String marker : plotMarkers) {
            int index = -1;
            while ((index = text.indexOf(marker, index + 1)) >= 0) {
                plotMarkerCount++;
            }
        }
        
        // 综合评分
        double textLength = text.length();
        double score = (pastTenseCount / textLength * 300) + 
                      (thirdPersonCount / textLength * 500) + 
                      (plotMarkerCount / textLength * 300);
        
        return Math.min(1.0, score);
    }
    
    /**
     * 检测对话密度
     */
    private double detectDialogueDensity(String text) {
        // 对话通常在引号内
        int quoteCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '"' || c == '"' || c == '"') {
                quoteCount++;
            }
        }
        
        // 计算对话占比
        double dialogueRatio = quoteCount / 2.0 / text.length();
        
        return Math.min(1.0, dialogueRatio * 20);
    }
    
    /**
     * 检测抒情风格
     */
    private double detectLyricalStyle(String text) {
        // 抒情特征：情感词汇多，第一人称，感叹句多
        
        // 情感词汇
        String[] emotionWords = {"喜悦", "悲伤", "高兴", "痛苦", "快乐", "忧郁", "兴奋", "失落", 
                               "愉快", "沮丧", "欣喜", "悲痛", "欢乐", "悲哀", "愤怒", "恐惧"};
        int emotionCount = 0;
        for (String word : emotionWords) {
            int index = -1;
            while ((index = text.indexOf(word, index + 1)) >= 0) {
                emotionCount++;
            }
        }
        
        // 第一人称
        String[] firstPersonMarkers = {"我", "我们", "咱们", "自己", "吾"};
        int firstPersonCount = 0;
        for (String marker : firstPersonMarkers) {
            int index = -1;
            while ((index = text.indexOf(marker, index + 1)) >= 0) {
                firstPersonCount++;
            }
        }
        
        // 感叹句
        int exclamationCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '!' || c == '！') {
                exclamationCount++;
            }
        }
        
        // 综合评分
        double textLength = text.length();
        double score = (emotionCount / textLength * 500) + 
                      (firstPersonCount / textLength * 300) + 
                      (exclamationCount / textLength * 200);
        
        return Math.min(1.0, score);
    }
    
    @Override
    public Map<String, List<Map<String, Object>>> buildCharacterNetwork(String text) {
        logger.info("开始构建人物关系网络，基于文本内容");
        
        try {
            // 获取当前处理的小说ID并记录日志
            Long novelId = RequestContextHolder.getCurrentNovelId();
            logger.info("当前处理的小说ID: {}", novelId);
            
            Map<String, List<Map<String, Object>>> result;
            
            if (novelId != null) {
                // 使用小说ID构建角色网络
                result = buildCharacterNetworkByNovelId(novelId);
            } else {
                // 如果没有小说ID，则使用文本内容进行分析
                result = buildCharacterNetworkByTextContent(text);
            }
            
            // 检查结果，确保有可用数据
            if (result.get("nodes").isEmpty()) {
                addDefaultResult(result);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("构建人物关系网络时出错: {}", e.getMessage(), e);
            // 发生错误时返回默认结果
            return createDefaultResult();
        }
    }
    
    /**
     * 创建默认结果，确保即使分析失败也有返回内容
     */
    private Map<String, List<Map<String, Object>>> createDefaultResult() {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        result.put("nodes", nodes);
        result.put("links", links);
        
        addDefaultNode(nodes);
        
        return result;
    }
    
    /**
     * 添加默认结果以确保有返回内容
     */
    private void addDefaultResult(Map<String, List<Map<String, Object>>> result) {
        List<Map<String, Object>> nodes = result.get("nodes");
        if (nodes == null) {
            nodes = new ArrayList<>();
            result.put("nodes", nodes);
        }
        
        if (nodes.isEmpty()) {
            addDefaultNode(nodes);
        }
        
        if (!result.containsKey("links")) {
            result.put("links", new ArrayList<>());
        }
    }
    
    /**
     * 根据文本内容构建人物关系网络
     */
    private Map<String, List<Map<String, Object>>> buildCharacterNetworkByTextContent(String text) {
        logger.info("开始基于文本内容构建人物关系网络");
        
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        result.put("nodes", nodes);
        result.put("links", links);
        
        try {
            // 从文本中提取角色
            List<String> characters = nlpService.extractCharacters(text);
            logger.info("从文本中提取到{}个角色", characters.size());
            
            if (characters.isEmpty()) {
                logger.warn("未从文本中提取到角色，将返回默认结果");
                addDefaultNode(nodes);
                return result;
            }
            
            // 过滤掉非人物名称
            List<String> meaningfulCharacters = filterMeaningfulCharacters(characters);
            logger.info("过滤后有{}个有效角色", meaningfulCharacters.size());
            
            if (meaningfulCharacters.isEmpty()) {
                logger.warn("过滤后没有有效角色，将返回默认结果");
                addDefaultNode(nodes);
                return result;
            }
            
            // 计算角色出现频率
            Map<String, Integer> characterOccurrences = countCharacterOccurrences(meaningfulCharacters);
            
            // 分析角色共现关系
            Map<String, Map<String, Integer>> coOccurrenceMap = new HashMap<>();
            
            // 分段进行分析，每段大约1000字
            int segmentLength = 1000;
            int contentLength = text.length();
            
            for (int i = 0; i < contentLength; i += segmentLength) {
                int end = Math.min(i + segmentLength, contentLength);
                String segment = text.substring(i, end);
                
                // 找出该段落中出现的角色
                List<String> charactersInSegment = new ArrayList<>();
                for (String character : meaningfulCharacters) {
                    if (segment.contains(character)) {
                        charactersInSegment.add(character);
                    }
                }
                
                // 分析该段落中角色的共现关系
                for (int j = 0; j < charactersInSegment.size(); j++) {
                    String char1 = charactersInSegment.get(j);
                    
                    for (int k = j + 1; k < charactersInSegment.size(); k++) {
                        String char2 = charactersInSegment.get(k);
                        
                        // 确保角色对的顺序一致性
                        if (char1.compareTo(char2) > 0) {
                            String temp = char1;
                            char1 = char2;
                            char2 = temp;
                        }
                        
                        // 更新共现次数
                        coOccurrenceMap
                            .computeIfAbsent(char1, k1 -> new HashMap<>())
                            .merge(char2, 1, Integer::sum);
                    }
                }
            }
            
            // 将共现关系转换为Triple列表
            List<Triple> coOccurrences = new ArrayList<>();
            for (Map.Entry<String, Map<String, Integer>> entry : coOccurrenceMap.entrySet()) {
                String char1 = entry.getKey();
                
                for (Map.Entry<String, Integer> innerEntry : entry.getValue().entrySet()) {
                    String char2 = innerEntry.getKey();
                    Integer count = innerEntry.getValue();
                    
                    if (count >= 2) { // 只考虑共现至少2次的关系
                        coOccurrences.add(new Triple(char1, char2, count));
                    }
                }
            }
            
            // 按共现次数排序
            coOccurrences.sort((t1, t2) -> t2.count.compareTo(t1.count));
            
            // 构建节点和连接
            buildNodesAndLinks(characterOccurrences, coOccurrences, nodes, links);
            
            // 如果没有连接，创建基本连接确保图表能正常渲染
            if (links.isEmpty() && nodes.size() > 1) {
                logger.info("未找到角色间连接，创建基本连接以确保图表渲染");
                createBasicLinks(nodes, links);
            }
            
        } catch (Exception e) {
            logger.error("基于文本构建人物关系网络时发生错误: {}", e.getMessage(), e);
            
            // 确保即使出错也返回可用的结果
            if (nodes.isEmpty()) {
                addDefaultNode(nodes);
            }
        }
        
        return result;
    }
    
    /**
     * 根据小说ID构建人物关系网络
     * @param novelId 小说ID
     * @return 包含节点和连接的关系网络数据
     */
    private Map<String, List<Map<String, Object>>> buildCharacterNetworkByNovelId(Long novelId) {
        logger.info("开始构建小说ID为{}的人物关系网络", novelId);
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        result.put("nodes", nodes);
        result.put("links", links);
        
        try {
            // 先获取小说内容
            String content = extractNovelContent(novelId);
            if (content == null || content.isEmpty()) {
                logger.warn("未能获取小说内容，将返回默认结果");
                addDefaultNode(nodes);
                return result;
            }
            
            // 获取角色列表
            List<String> characters = nlpService.extractCharacters(content);
            logger.info("从小说中提取到{}个角色", characters.size());
            if (characters.isEmpty()) {
                logger.warn("未提取到角色，将返回默认结果");
                addDefaultNode(nodes);
                return result;
            }
            
            // 过滤掉非人物名称
            List<String> meaningfulCharacters = filterMeaningfulCharacters(characters);
            logger.info("过滤后有{}个有效角色", meaningfulCharacters.size());
            
            if (meaningfulCharacters.isEmpty()) {
                logger.warn("过滤后没有有效角色，将返回默认结果");
                addDefaultNode(nodes);
                return result;
            }
            
            // 计算角色出现频率
            Map<String, Integer> characterOccurrences = countCharacterOccurrences(meaningfulCharacters);
            logger.info("计算了{}个角色的出现频率", characterOccurrences.size());
            
            // 分析角色共现关系
            List<Triple> coOccurrences = analyzeCoOccurrences(novelId, new ArrayList<>(characterOccurrences.keySet()));
            logger.info("分析得到{}个角色共现关系", coOccurrences.size());
            
            // 构建节点和连接
            buildNodesAndLinks(characterOccurrences, coOccurrences, nodes, links);
            
            // 如果没有连接，创建基本连接确保图表能正常渲染
            if (links.isEmpty() && nodes.size() > 1) {
                logger.info("未找到角色间连接，创建基本连接以确保图表渲染");
                createBasicLinks(nodes, links);
            }
            
        } catch (Exception e) {
            logger.error("构建人物关系网络时发生错误: {}", e.getMessage(), e);
            // 确保即使出错也返回可用的结果
            if (nodes.isEmpty()) {
                addDefaultNode(nodes);
            }
        }
        
        logger.info("人物关系网络构建完成，包含{}个节点和{}个连接", nodes.size(), links.size());
        return result;
    }
    
    /**
     * 构建节点和链接数据
     */
    private void buildNodesAndLinks(
            Map<String, Integer> characterOccurrences, 
            List<Triple> coOccurrences, 
            List<Map<String, Object>> nodes, 
            List<Map<String, Object>> links) {
            
        // 根据出现频率排序角色
        List<Map.Entry<String, Integer>> sortedCharacters = new ArrayList<>(characterOccurrences.entrySet());
        sortedCharacters.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        // 确定主要角色（出现频率前30%或至少前5个）
        int mainCharCount = Math.max(5, (int)(sortedCharacters.size() * 0.3));
        mainCharCount = Math.min(mainCharCount, sortedCharacters.size());
            
        // 创建节点
        Map<String, Integer> characterIdMap = new HashMap<>();
        int nodeId = 1;
        
        // 添加主要角色节点
        for (int i = 0; i < mainCharCount; i++) {
            Map.Entry<String, Integer> entry = sortedCharacters.get(i);
            Map<String, Object> node = new HashMap<>();
            node.put("id", nodeId);
            node.put("name", entry.getKey());
            
            // 确定角色类别（主角、配角等）
            String category;
            double importance;
            if (i == 0) {
                category = "主角";
                importance = 1.0;
            } else if (i < 3) {
                category = "主要角色";
                importance = 0.8;
            } else if (i < mainCharCount / 2) {
                category = "重要配角";
                importance = 0.6;
            } else {
                category = "次要角色";
                importance = 0.4;
            }
            
            node.put("category", category);
            node.put("value", entry.getValue());
            node.put("symbolSize", 40 + Math.min(entry.getValue() * 2, 60)); // 节点大小基于出现频率
            node.put("importance", importance);
            nodes.add(node);
            characterIdMap.put(entry.getKey(), nodeId);
            nodeId++;
        }
        
        // 添加次要角色节点（最多再添加同等数量的次要角色）
        int secondaryCharLimit = Math.min(sortedCharacters.size() - mainCharCount, mainCharCount);
        for (int i = mainCharCount; i < mainCharCount + secondaryCharLimit; i++) {
            Map.Entry<String, Integer> entry = sortedCharacters.get(i);
            Map<String, Object> node = new HashMap<>();
            node.put("id", nodeId);
            node.put("name", entry.getKey());
            node.put("category", "配角");
            node.put("value", entry.getValue());
            node.put("symbolSize", 30 + Math.min(entry.getValue(), 20)); // 次要角色节点稍小
            node.put("importance", 0.2);
            nodes.add(node);
            characterIdMap.put(entry.getKey(), nodeId);
            nodeId++;
        }
        
        logger.info("创建了{}个人物节点", nodes.size());
        
        // 创建连接
        int linkId = 1;
        for (Triple relation : coOccurrences) {
            // 只创建主要角色之间的连接
            if (characterIdMap.containsKey(relation.character1) && characterIdMap.containsKey(relation.character2)) {
                Map<String, Object> link = new HashMap<>();
                link.put("id", linkId++);
                link.put("source", characterIdMap.get(relation.character1));
                link.put("target", characterIdMap.get(relation.character2));
                link.put("relation", "关联");
                link.put("value", relation.count);
                link.put("desc", relation.character1 + "与" + relation.character2 + "共同出现" + relation.count + "次");
                links.add(link);
            }
        }
        
        logger.info("基于共现关系创建了{}个连接", links.size());
    }
    
    /**
     * 提取小说内容
     */
    private String extractNovelContent(Long novelId) {
        try {
            // 使用小说ID查询小说内容
            Optional<Novel> novelOpt = novelRepository.findById(novelId);
            if (!novelOpt.isPresent()) {
                logger.warn("未找到ID为{}的小说", novelId);
                return "";
            }
            
            Novel novel = novelOpt.get();
            // 从章节中获取内容
            List<Chapter> chapters = novel.getChapters();
            if (chapters == null || chapters.isEmpty()) {
                logger.warn("小说ID为{}的小说没有章节", novelId);
                return "";
            }
            
            // 按章节顺序排序
            chapters.sort((c1, c2) -> c1.getChapterNumber().compareTo(c2.getChapterNumber()));
            
            // 合并章节内容
            StringBuilder contentBuilder = new StringBuilder();
            for (Chapter chapter : chapters) {
                contentBuilder.append(chapter.getTitle()).append("\n");
                if (chapter.getContent() != null) {
                    contentBuilder.append(chapter.getContent()).append("\n\n");
                }
            }
            
            return contentBuilder.toString();
        } catch (Exception e) {
            logger.error("提取小说内容时发生错误: {}", e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * 统计角色出现次数
     */
    private Map<String, Integer> countCharacterOccurrences(List<String> characters) {
        Map<String, Integer> occurrences = new HashMap<>();
            for (String character : characters) {
            occurrences.put(character, occurrences.getOrDefault(character, 0) + 1);
        }
        return occurrences;
    }
    
    /**
     * 分析角色共现关系
     * @param novelId 小说ID
     * @param characters 角色列表
     * @return 角色共现关系列表
     */
    private List<Triple> analyzeCoOccurrences(Long novelId, List<String> characters) {
        logger.info("开始分析小说ID为{}的角色共现关系", novelId);
        
        List<Triple> coOccurrences = new ArrayList<>();
        Map<String, Map<String, Integer>> coOccurrenceMap = new HashMap<>();
        
        try {
            String content = extractNovelContent(novelId);
            if (content == null || content.isEmpty()) {
                logger.warn("未能获取小说内容");
                return coOccurrences;
            }
            
            // 分段进行分析，每段大约1000字
            int segmentLength = 1000;
            int contentLength = content.length();
            
            for (int i = 0; i < contentLength; i += segmentLength) {
                int end = Math.min(i + segmentLength, contentLength);
                String segment = content.substring(i, end);
                
                // 找出该段落中出现的角色
                List<String> charactersInSegment = new ArrayList<>();
                for (String character : characters) {
                    if (segment.contains(character)) {
                        charactersInSegment.add(character);
                    }
                }
                
                // 分析该段落中角色的共现关系
                for (int j = 0; j < charactersInSegment.size(); j++) {
                    String char1 = charactersInSegment.get(j);
                    
                    for (int k = j + 1; k < charactersInSegment.size(); k++) {
                        String char2 = charactersInSegment.get(k);
                        
                        // 确保角色对的顺序一致性
                        if (char1.compareTo(char2) > 0) {
                            String temp = char1;
                            char1 = char2;
                            char2 = temp;
                        }
                        
                        // 更新共现次数
                        coOccurrenceMap
                            .computeIfAbsent(char1, k1 -> new HashMap<>())
                            .merge(char2, 1, Integer::sum);
                    }
                }
            }
            
            // 将共现关系转换为Triple列表
            for (Map.Entry<String, Map<String, Integer>> entry : coOccurrenceMap.entrySet()) {
                String char1 = entry.getKey();
                
                for (Map.Entry<String, Integer> innerEntry : entry.getValue().entrySet()) {
                    String char2 = innerEntry.getKey();
                    Integer count = innerEntry.getValue();
                    
                    if (count >= 2) { // 只考虑共现至少2次的关系
                        coOccurrences.add(new Triple(char1, char2, count));
                    }
                }
            }
            
            // 按共现次数排序
            coOccurrences.sort((t1, t2) -> t2.count.compareTo(t1.count));
            
        } catch (Exception e) {
            logger.error("分析角色共现关系时发生错误: {}", e.getMessage(), e);
        }
        
        return coOccurrences;
    }
    
    /**
     * 过滤出有意义的人物角色名称
     * @param characters 原始人物列表
     * @return 过滤后的人物列表
     */
    private List<String> filterMeaningfulCharacters(List<String> characters) {
        if (characters == null || characters.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 常见的非人物词语或虚词
        Set<String> commonWords = new HashSet<>(Arrays.asList(
            "一个", "这个", "那个", "我们", "你们", "他们", "她们", "那里", "这里", 
            "什么", "为什么", "怎么", "如何", "自己", "一下", "一点", "一些", "大家",
            "任何", "某个", "有人", "如此", "这样", "那样", "其他", "别人", "许多",
            "可能", "情况", "事情", "时候", "当时", "突然", "很多", "一般", "一直",
            "已经", "所有", "几乎", "太多", "还有", "知道", "现在", "时间", "地方"
        ));
        
        return characters.stream()
                .filter(name -> !commonWords.contains(name))
                .filter(name -> name.length() >= 1)  // 确保名称至少有1个字符
                .collect(Collectors.toList());
    }
    
    /**
     * 在节点之间创建基本连接，确保图表可渲染
     * @param nodes 节点列表
     * @param links 要添加连接的链接列表
     */
    private void createBasicLinks(List<Map<String, Object>> nodes, List<Map<String, Object>> links) {
        if (nodes.size() < 2) {
            return;  // 至少需要两个节点才能创建连接
        }
        
        logger.info("创建基本链接，节点数量: {}", nodes.size());
        
        int linkId = 1;
        // 确保所有节点都连接到第一个节点，形成星型结构
        Map<String, Object> firstNode = nodes.get(0);
        Integer firstNodeId = (Integer) firstNode.get("id");
        
        for (int i = 1; i < nodes.size(); i++) {
            Map<String, Object> node = nodes.get(i);
            Integer nodeId = (Integer) node.get("id");
            
            Map<String, Object> link = new HashMap<>();
            link.put("id", linkId++);
            link.put("source", firstNodeId);
            link.put("target", nodeId);
            link.put("relation", "关联");
            link.put("value", 1);
            link.put("desc", firstNode.get("name") + "与" + node.get("name") + "的关联");
            
            links.add(link);
        }
        
        // 如果有足够多的节点，创建环形结构增加图表稳定性
        if (nodes.size() >= 3) {
            for (int i = 1; i < nodes.size() - 1; i++) {
                Map<String, Object> currentNode = nodes.get(i);
                Map<String, Object> nextNode = nodes.get(i + 1);
                
                Integer currentId = (Integer) currentNode.get("id");
                Integer nextId = (Integer) nextNode.get("id");
                
                Map<String, Object> link = new HashMap<>();
                link.put("id", linkId++);
                link.put("source", currentId);
                link.put("target", nextId);
                link.put("relation", "关联");
                link.put("value", 1);
                link.put("desc", currentNode.get("name") + "与" + nextNode.get("name") + "的关联");
                
                links.add(link);
            }
            
            // 收尾相连，形成环
            Map<String, Object> lastNode = nodes.get(nodes.size() - 1);
            Map<String, Object> secondNode = nodes.get(1);
            
            Integer lastId = (Integer) lastNode.get("id");
            Integer secondId = (Integer) secondNode.get("id");
            
            Map<String, Object> link = new HashMap<>();
            link.put("id", linkId++);
            link.put("source", lastId);
            link.put("target", secondId);
            link.put("relation", "关联");
            link.put("value", 1);
            link.put("desc", lastNode.get("name") + "与" + secondNode.get("name") + "的关联");
            
            links.add(link);
        }
        
        logger.info("创建了{}个基本连接", links.size());
    }
    
    /**
     * 深度学习增强关系分析
     */
    private void enhanceRelationshipAnalysis(List<Map<String, Object>> relationships, String text) {
        // 增强现有的关系分析
        for (Map<String, Object> relationship : relationships) {
            // 可以在这里添加基于深度学习的关系强化
            // 例如：从文本中提取更多关系线索等
        }
    }
    
    /**
     * 计算角色在文本中的出现次数
     */
    private int countCharacterAppearances(String character, String text) {
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(character, index)) != -1) {
            count++;
            index += character.length();
        }
        
        return count;
    }
    
    /**
     * 计算两个角色在文本中的共现次数
     */
    private int countCooccurrences(String char1, String char2, String text) {
        // 分段计算共现次数，以段落为单位
        String[] paragraphs = text.split("\n+");
        int cooccurrences = 0;
        
        for (String paragraph : paragraphs) {
            if (paragraph.contains(char1) && paragraph.contains(char2)) {
                cooccurrences++;
            }
        }
        
        return cooccurrences;
    }
    
    /**
     * 计算角色的平均情感值
     */
    private double averageCharacterSentiment(String character, List<Map<String, String>> dialogues) {
        List<Double> sentiments = new ArrayList<>();
        
        for (Map<String, String> dialogue : dialogues) {
            if (character.equals(dialogue.get("speaker"))) {
                String content = dialogue.get("content");
                if (content != null && !content.isEmpty()) {
                    sentiments.add(deepSentimentAnalysis(content));
                }
            }
        }
        
        return sentiments.isEmpty() ? 0.5 : sentiments.stream().mapToDouble(d -> d).average().orElse(0.5);
    }
    
    @Override
    public Map<String, Double> classifyText(String text, List<String> categories) {
        Map<String, Double> result = new HashMap<>();
        
        try {
            // 提取文本特征
            Map<String, Double> features = extractTextFeatures(text);
            
            // 基于规则的分类器
            for (String category : categories) {
                double score = calculateCategoryScore(category, features, text);
                result.put(category, score);
            }
            
            // 归一化概率分布
            double sum = result.values().stream().mapToDouble(d -> d).sum();
            if (sum > 0) {
                for (String category : result.keySet()) {
                    result.put(category, result.get(category) / sum);
                }
            }
            
        } catch (Exception e) {
            logger.error("文本分类失败", e);
        }
        
        return result;
    }
    
    /**
     * 提取文本特征
     */
    private Map<String, Double> extractTextFeatures(String text) {
        Map<String, Double> features = new HashMap<>();
        
        // 使用文本风格分析获取特征
        Map<String, Double> styleFeatures = detectWritingStyle(text);
        features.putAll(styleFeatures);
        
        // 添加情感特征
        features.put("情感倾向", deepSentimentAnalysis(text));
        
        // 计算关键词覆盖率
        Map<String, Integer> keywords = nlpService.extractKeywords(text, 20);
        double keywordCoverage = keywords.values().stream().mapToDouble(i -> i / 100.0).average().orElse(0);
        features.put("关键词覆盖率", keywordCoverage);
        
        return features;
    }
    
    /**
     * 计算文本在某个类别下的得分
     */
    private double calculateCategoryScore(String category, Map<String, Double> features, String text) {
        switch (category.toLowerCase()) {
            case "奇幻":
                return calculateFantasyScore(features, text);
            case "科幻":
                return calculateSciFiScore(features, text);
            case "武侠":
                return calculateWuxiaScore(features, text);
            case "言情":
                return calculateRomanceScore(features, text);
            case "悬疑":
                return calculateMysteryScore(features, text);
            case "历史":
                return calculateHistoricalScore(features, text);
            case "都市":
                return calculateUrbanScore(features, text);
            default:
                return 0.1; // 默认低概率
        }
    }
    
    /**
     * 计算奇幻类型得分
     */
    private double calculateFantasyScore(Map<String, Double> features, String text) {
        // 奇幻关键词
        String[] fantasyWords = {"魔法", "精灵", "巫师", "龙", "魔兽", "法术", "咒语", "神器", 
                               "魔域", "神殿", "勇者", "神灵", "圣剑", "预言", "神秘"};
        
        double keywordScore = calculateKeywordScore(text, fantasyWords);
        double styleScore = features.getOrDefault("对比修辞", 0.0) + features.getOrDefault("比喻修辞", 0.0);
        
        return (keywordScore * 0.7 + styleScore * 0.3);
    }
    
    /**
     * 计算科幻类型得分
     */
    private double calculateSciFiScore(Map<String, Double> features, String text) {
        // 科幻关键词
        String[] sciFiWords = {"科技", "飞船", "机器人", "太空", "星球", "未来", "基因", "人工智能", 
                             "克隆", "虚拟", "光速", "时空", "量子", "文明", "实验"};
        
        double keywordScore = calculateKeywordScore(text, sciFiWords);
        double dialogueScore = features.getOrDefault("对话密度", 0.0);
        
        return (keywordScore * 0.8 + dialogueScore * 0.2);
    }
    
    /**
     * 计算武侠类型得分
     */
    private double calculateWuxiaScore(Map<String, Double> features, String text) {
        // 武侠关键词
        String[] wuxiaWords = {"武功", "剑法", "内力", "掌门", "师父", "门派", "江湖", "轻功", 
                             "侠客", "武林", "绝学", "刀法", "镖局", "棋局", "大侠"};
        
        double keywordScore = calculateKeywordScore(text, wuxiaWords);
        double narrativeScore = features.getOrDefault("叙事风格", 0.0);
        
        return (keywordScore * 0.7 + narrativeScore * 0.3);
    }
    
    /**
     * 计算言情类型得分
     */
    private double calculateRomanceScore(Map<String, Double> features, String text) {
        // 言情关键词
        String[] romanceWords = {"爱情", "心跳", "告白", "暗恋", "相思", "恋爱", "温柔", "拥抱", 
                               "亲吻", "浪漫", "心动", "缘分", "约会", "甜蜜", "幸福"};
        
        double keywordScore = calculateKeywordScore(text, romanceWords);
        double lyricalScore = features.getOrDefault("抒情风格", 0.0);
        double sentimentScore = features.getOrDefault("情感倾向", 0.5);
        
        return (keywordScore * 0.5 + lyricalScore * 0.3 + sentimentScore * 0.2);
    }
    
    /**
     * 计算悬疑类型得分
     */
    private double calculateMysteryScore(Map<String, Double> features, String text) {
        // 悬疑关键词
        String[] mysteryWords = {"谜团", "线索", "侦探", "案件", "真相", "凶手", "嫌疑", "调查", 
                               "证据", "秘密", "谋杀", "诡异", "绑架", "推理", "悬疑"};
        
        double keywordScore = calculateKeywordScore(text, mysteryWords);
        double complexSentenceRatio = features.getOrDefault("复合句比例", 0.0);
        double dialogueDensity = features.getOrDefault("对话密度", 0.0);
        
        return (keywordScore * 0.6 + complexSentenceRatio * 0.2 + dialogueDensity * 0.2);
    }
    
    /**
     * 计算历史类型得分
     */
    private double calculateHistoricalScore(Map<String, Double> features, String text) {
        // 历史关键词
        String[] historicalWords = {"朝代", "王朝", "皇帝", "将军", "宰相", "战争", "兵变", "朝廷", 
                                  "官府", "封建", "亡国", "史书", "典籍", "诏书", "年号"};
        
        double keywordScore = calculateKeywordScore(text, historicalWords);
        double proseStyle = features.getOrDefault("散文化风格", 0.0);
        
        return (keywordScore * 0.8 + proseStyle * 0.2);
    }
    
    /**
     * 计算都市类型得分
     */
    private double calculateUrbanScore(Map<String, Double> features, String text) {
        // 都市关键词
        String[] urbanWords = {"公司", "职场", "办公室", "城市", "上班", "老板", "会议", "项目", 
                             "生活", "现代", "地铁", "咖啡", "饭局", "时尚", "白领"};
        
        double keywordScore = calculateKeywordScore(text, urbanWords);
        double dialogueDensity = features.getOrDefault("对话密度", 0.0);
        
        return (keywordScore * 0.7 + dialogueDensity * 0.3);
    }
    
    /**
     * 计算关键词得分
     */
    private double calculateKeywordScore(String text, String[] keywords) {
        int matchCount = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                matchCount++;
            }
        }
        
        return Math.min(1.0, (double) matchCount / (keywords.length / 2.0));
    }
    
    @Override
    public Map<String, Double> analyzeTextComplexity(String text) {
        Map<String, Double> complexityMetrics = new HashMap<>();
        
        try {
            // 分词
            List<String> tokens = HanLP.segment(text).stream()
                    .map(term -> term.word)
                    .collect(Collectors.toList());
            
            // 句子
            String[] sentences = text.split("[。！？.!?]+");
            
            // 词汇复杂度：平均词长
            double avgWordLength = tokens.stream()
                    .mapToInt(String::length)
                    .average()
                    .orElse(0);
            complexityMetrics.put("平均词长", avgWordLength);
            
            // 句法复杂度：平均句长
            double avgSentenceLength = sentences.length > 0
                    ? (double) tokens.size() / sentences.length
                    : 0;
            complexityMetrics.put("平均句长", avgSentenceLength);
            
            // 词汇多样性：TTR (Type-Token Ratio)
            double typeTokenRatio = tokens.isEmpty()
                    ? 0
                    : (double) new HashSet<>(tokens).size() / tokens.size();
            complexityMetrics.put("词汇多样性", typeTokenRatio);
            
            // 可读性指标（使用简化的算法）
            double readabilityScore = calculateReadabilityScore(avgWordLength, avgSentenceLength);
            complexityMetrics.put("可读性指标", readabilityScore);
            
            // 词性多样性
            double posVariety = calculatePOSVariety(text);
            complexityMetrics.put("词性多样性", posVariety);
            
            // 短句比例
            double shortSentenceRatio = Arrays.stream(sentences)
                    .filter(s -> s.length() < 15)
                    .count() / (double) sentences.length;
            complexityMetrics.put("短句比例", shortSentenceRatio);
            
            // 长句比例
            double longSentenceRatio = Arrays.stream(sentences)
                    .filter(s -> s.length() > 50)
                    .count() / (double) sentences.length;
            complexityMetrics.put("长句比例", longSentenceRatio);
            
            // 整体复杂度
            double overallComplexity = calculateOverallComplexity(complexityMetrics);
            complexityMetrics.put("整体复杂度", overallComplexity);
            
        } catch (Exception e) {
            logger.error("文本复杂度分析失败", e);
        }
        
        return complexityMetrics;
    }
    
    /**
     * 计算可读性得分
     */
    private double calculateReadabilityScore(double avgWordLength, double avgSentenceLength) {
        // 简化的可读性公式
        return 206.835 - (1.015 * avgSentenceLength) - (84.6 * avgWordLength / 100);
    }
    
    /**
     * 计算词性多样性
     */
    private double calculatePOSVariety(String text) {
        // 获取词性标注
        List<String> posTags = HanLP.segment(text).stream()
                .map(term -> term.nature.toString())
                .collect(Collectors.toList());
        
        // 计算词性种类占总词数的比例
        return posTags.isEmpty()
                ? 0
                : (double) new HashSet<>(posTags).size() / Math.min(20, posTags.size());
    }
    
    /**
     * 计算整体复杂度
     */
    private double calculateOverallComplexity(Map<String, Double> metrics) {
        double avgWordLength = metrics.getOrDefault("平均词长", 0.0);
        double avgSentenceLength = metrics.getOrDefault("平均句长", 0.0);
        double typeTokenRatio = metrics.getOrDefault("词汇多样性", 0.0);
        double longSentenceRatio = metrics.getOrDefault("长句比例", 0.0);
        
        // 加权平均
        return (avgWordLength / 5.0) * 0.25 +
               (avgSentenceLength / 30.0) * 0.35 +
               typeTokenRatio * 0.25 +
               longSentenceRatio * 0.15;
    }
    
    @Override
    public boolean trainWordVectorModel(List<String> corpus, int vectorSize) {
        try {
            logger.info("开始训练词向量模型, 语料大小: {}, 向量维度: {}", corpus.size(), vectorSize);
            
            // 准备语料
            SentenceIterator sentenceIterator = new CollectionSentenceIterator(corpus);
            
            // 设置分词器
            TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
            tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
            
            // 配置Word2Vec
            Word2Vec.Builder builder = new Word2Vec.Builder()
                    .seed(42)
                    .minWordFrequency(3)
                    .iterations(5)
                    .layerSize(vectorSize > 0 ? vectorSize : DEFAULT_VECTOR_SIZE)
                    .windowSize(5)
                    .iterate(sentenceIterator)
                    .tokenizerFactory(tokenizerFactory);
            
            // 训练模型
            Word2Vec word2VecModel = builder.build();
            word2VecModel.fit();
            
            // 保存模型
            Path modelDir = Paths.get(MODEL_PATH).getParent();
            if (!Files.exists(modelDir)) {
                Files.createDirectories(modelDir);
            }
            
            WordVectorSerializer.writeWord2VecModel(word2VecModel, MODEL_PATH);
            
            // 更新当前使用的模型
            this.word2Vec = word2VecModel;
            
            logger.info("词向量模型训练完成，词汇表大小: {}", word2VecModel.getVocab().numWords());
            logger.info("模型已保存到: {}", MODEL_PATH);
            
            return true;
        } catch (Exception e) {
            logger.error("训练词向量模型失败", e);
            return false;
        }
    }
    
    @Override
    public List<Double> calculateSemanticRelatedness(String mainText, List<String> referenceTexts) {
        List<Double> relatedness = new ArrayList<>();
        
        try {
            // 如果有Word2Vec模型，使用它来计算语义相关性
            if (word2Vec != null) {
                Collection<String> mainTokens = tokenizeText(mainText);
                INDArray mainVector = word2Vec.getWordVectorsMean(mainTokens);
                
                for (String refText : referenceTexts) {
                    Collection<String> refTokens = tokenizeText(refText);
                    INDArray refVector = word2Vec.getWordVectorsMean(refTokens);
                    
                    if (mainVector != null && refVector != null) {
                        double similarity = Transforms.cosineSim(mainVector, refVector);
                        relatedness.add(similarity);
                    } else {
                        // 回退到简单相似度
                        double similarity = calculateTextSimilarity(mainText, refText);
                        relatedness.add(similarity);
                    }
                }
            } else {
                // 回退到简单相似度计算
                for (String refText : referenceTexts) {
                    double similarity = calculateTextSimilarity(mainText, refText);
                    relatedness.add(similarity);
                }
            }
        } catch (Exception e) {
            logger.error("计算语义关联度时出错", e);
            
            // 出错时也返回结果，使用简单相似度
            for (String refText : referenceTexts) {
                try {
                    double similarity = calculateSimpleSimilarity(mainText, refText);
                    relatedness.add(similarity);
                } catch (Exception ex) {
                    relatedness.add(0.0);
                }
            }
        }
        
        return relatedness;
    }
    
    @Override
    public Map<String, List<Map<String, Object>>> analyzeNovelStructure(List<String> chapters) {
        logger.info("使用深度学习分析小说结构，共{}章", chapters.size());
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        
        try {
            // 主结构部分定义
            String[] mainSectionNames = {"开端", "铺垫", "发展", "高潮", "结局"};
            // 详细结构部分定义
            String[] detailSectionNames = {
                "角色介绍", "世界设定", "初始冲突", "情节递进", "次要冲突", 
                "关系发展", "主要冲突", "危机", "转折点", "高潮", "解决", "结局"
            };
            
            // 计算每章的特征向量
            List<double[]> chapterFeatures = calculateChapterFeatures(chapters);
            
            // 计算每章情感曲线
            double[] sentimentCurve = calculateSentimentCurve(chapters);
            
            // 分析主题变化
            List<Map<String, Double>> topicTransitions = analyzeTopicTransitions(chapters);
            
            // 1. 分析主要结构
            List<Map<String, Object>> mainStructure = analyzeMainStructure(
                chapters, chapterFeatures, sentimentCurve, mainSectionNames);
            result.put("mainStructure", mainStructure);
            
            // 2. 分析详细结构
            List<Map<String, Object>> detailStructure = analyzeDetailStructure(
                chapters, chapterFeatures, sentimentCurve, topicTransitions, detailSectionNames);
            result.put("detailStructure", detailStructure);
            
            // 总章节数
            result.put("totalChapters", Collections.singletonList(
                Map.of("value", chapters.size())
            ));
            
            logger.info("小说结构分析完成");
        } catch (Exception e) {
            logger.error("分析小说结构时发生错误", e);
            // 出错时返回基础结构
            result.put("mainStructure", generateDefaultMainStructure(chapters.size()));
            result.put("detailStructure", generateDefaultDetailStructure(chapters.size()));
            result.put("totalChapters", Collections.singletonList(
                Map.of("value", chapters.size())
            ));
        }
        
        return result;
    }
    
    /**
     * 计算每章的特征向量，包括文本特征、情感特征等
     */
    private List<double[]> calculateChapterFeatures(List<String> chapters) {
        List<double[]> features = new ArrayList<>();
        
        for (String chapter : chapters) {
            // 1. 词频特征
            Map<String, Integer> keywords = nlpService.extractKeywords(chapter, 20);
            
            // 2. 句子复杂度特征
            double avgSentenceLength = calculateAverageSentenceLength(chapter);
            
            // 3. 对话比例特征
            double dialogueRatio = calculateDialogueRatio(chapter);
            
            // 4. 情感特征
            double sentiment = nlpService.analyzeSentiment(chapter);
            
            // 合并特征
            double[] featureVector = new double[] {
                avgSentenceLength / 50.0,  // 归一化句子长度
                dialogueRatio,             // 对话比例
                sentiment,                 // 情感得分
                keywords.size() / 20.0     // 关键词丰富度
            };
            
            features.add(featureVector);
        }
        
        return features;
    }
    
    /**
     * 计算情感曲线
     */
    private double[] calculateSentimentCurve(List<String> chapters) {
        double[] sentimentCurve = new double[chapters.size()];
        
        for (int i = 0; i < chapters.size(); i++) {
            sentimentCurve[i] = deepSentimentAnalysis(chapters.get(i));
        }
        
        // 平滑情感曲线
        return smoothCurve(sentimentCurve);
    }
    
    /**
     * 对曲线数据进行平滑处理
     */
    private double[] smoothCurve(double[] curve) {
        double[] smoothed = new double[curve.length];
        
        // 简单的移动平均平滑
        int windowSize = Math.min(5, curve.length);
        for (int i = 0; i < curve.length; i++) {
            double sum = 0;
            int count = 0;
            for (int j = Math.max(0, i - windowSize/2); j <= Math.min(curve.length - 1, i + windowSize/2); j++) {
                sum += curve[j];
                count++;
            }
            smoothed[i] = sum / count;
        }
        
        return smoothed;
    }
    
    /**
     * 分析主题变化
     */
    private List<Map<String, Double>> analyzeTopicTransitions(List<String> chapters) {
        List<Map<String, Double>> topicTransitions = new ArrayList<>();
        
        // 每个章节提取主题
        for (String chapter : chapters) {
            Map<String, Double> topics = new HashMap<>();
            // 从章节中提取主题及其分数
            Map<String, Double> extractedTopics = nlpService.extractTopics(chapter, 3);
            topicTransitions.add(extractedTopics);
        }
        
        return topicTransitions;
    }
    
    /**
     * 分析主要结构
     */
    private List<Map<String, Object>> analyzeMainStructure(
            List<String> chapters, 
            List<double[]> features, 
            double[] sentimentCurve, 
            String[] sectionNames) {
        
        List<Map<String, Object>> mainStructure = new ArrayList<>();
        int totalChapters = chapters.size();
        
        // 根据小说的总体长度动态调整结构比例
        double[] percentages;
        if (totalChapters <= 10) {
            percentages = new double[] {0.2, 0.15, 0.3, 0.25, 0.1}; // 短篇
        } else if (totalChapters <= 30) {
            percentages = new double[] {0.15, 0.2, 0.35, 0.2, 0.1}; // 中篇
        } else {
            percentages = new double[] {0.1, 0.15, 0.4, 0.25, 0.1}; // 长篇
        }
        
        // 调整结构边界：基于情感曲线和主题变化的峰值
        int[] adjustedBoundaries = detectStructureBoundaries(sentimentCurve, totalChapters, percentages);
        
        // 默认色彩
        String[] colors = {"#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de"};
        
        // 创建结构部分
        int startChapter = 0;
        for (int i = 0; i < sectionNames.length; i++) {
            int endChapter = (i < sectionNames.length - 1) ? adjustedBoundaries[i] : totalChapters;
            
            Map<String, Object> section = new HashMap<>();
            section.put("name", sectionNames[i]);
            section.put("value", endChapter - startChapter);
            section.put("percentage", (double)(endChapter - startChapter) / totalChapters);
            section.put("startChapter", startChapter + 1);
            section.put("endChapter", endChapter);
            section.put("color", colors[i % colors.length]);
            
            mainStructure.add(section);
            startChapter = endChapter;
        }
        
        return mainStructure;
    }
    
    /**
     * 检测结构边界
     */
    private int[] detectStructureBoundaries(double[] sentimentCurve, int totalChapters, double[] percentages) {
        int[] boundaries = new int[percentages.length - 1];
        
        // 理想境界点
        for (int i = 0; i < boundaries.length; i++) {
            double cumPercentage = 0;
            for (int j = 0; j <= i; j++) {
                cumPercentage += percentages[j];
            }
            boundaries[i] = (int) Math.round(cumPercentage * totalChapters);
        }
        
        // 调整边界：寻找情感变化最显著的附近点
        if (sentimentCurve.length > 5) {
            for (int i = 0; i < boundaries.length; i++) {
                int idealPoint = boundaries[i];
                int searchStart = Math.max(0, idealPoint - 3);
                int searchEnd = Math.min(sentimentCurve.length - 1, idealPoint + 3);
                
                // 在理想点附近寻找情感变化最显著的点
                double maxChange = 0;
                int bestPoint = idealPoint;
                
                for (int j = searchStart + 1; j <= searchEnd; j++) {
                    double change = Math.abs(sentimentCurve[j] - sentimentCurve[j-1]);
                    if (change > maxChange) {
                        maxChange = change;
                        bestPoint = j;
                    }
                }
                
                boundaries[i] = bestPoint;
            }
        }
        
        return boundaries;
    }
    
    /**
     * 分析详细结构
     */
    private List<Map<String, Object>> analyzeDetailStructure(
            List<String> chapters, 
            List<double[]> features, 
            double[] sentimentCurve,
            List<Map<String, Double>> topicTransitions,
            String[] sectionNames) {
        
        List<Map<String, Object>> detailStructure = new ArrayList<>();
        int totalChapters = chapters.size();
        
        // 为短篇小说简化结构
        if (totalChapters < 12) {
            String[] simplifiedSections = {"起因", "经过", "转折", "结果"};
            double[] percentages = {0.25, 0.35, 0.25, 0.15};
            
            int startChapter = 0;
            String[] colors = {"#5470c6", "#91cc75", "#ee6666", "#73c0de"};
            
            for (int i = 0; i < simplifiedSections.length; i++) {
                int sectionChapters = (int) Math.round(totalChapters * percentages[i]);
                int endChapter = Math.min(startChapter + sectionChapters, totalChapters);
                
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
            // 为长篇小说创建详细结构
            // 检测重要转折点
            List<Integer> keyPoints = detectKeyPoints(sentimentCurve, topicTransitions);
            
            // 根据检测到的关键点分配结构部分
            organizeDetailStructure(detailStructure, keyPoints, totalChapters, sectionNames);
        }
        
        return detailStructure;
    }
    
    /**
     * 检测关键转折点
     */
    private List<Integer> detectKeyPoints(double[] sentimentCurve, List<Map<String, Double>> topicTransitions) {
        List<Integer> keyPoints = new ArrayList<>();
        
        // 计算情感变化率
        double[] sentimentChange = new double[sentimentCurve.length - 1];
        for (int i = 0; i < sentimentCurve.length - 1; i++) {
            sentimentChange[i] = sentimentCurve[i+1] - sentimentCurve[i];
        }
        
        // 寻找情感变化的峰值
        for (int i = 1; i < sentimentChange.length - 1; i++) {
            if ((sentimentChange[i] > 0 && sentimentChange[i-1] <= 0) || 
                (sentimentChange[i] < 0 && sentimentChange[i-1] >= 0)) {
                keyPoints.add(i);
            }
        }
        
        // 检测主题变化
        if (topicTransitions.size() > 3) {
            for (int i = 1; i < topicTransitions.size() - 1; i++) {
                double topicSimilarity = calculateTopicSimilarity(
                    topicTransitions.get(i-1), topicTransitions.get(i));
                if (topicSimilarity < 0.5) { // 主题有显著变化
                    if (!keyPoints.contains(i)) {
                        keyPoints.add(i);
                    }
                }
            }
        }
        
        // 排序并限制关键点数量
        Collections.sort(keyPoints);
        if (keyPoints.size() > 10) {
            // 保留最显著的10个关键点
            keyPoints = keyPoints.subList(0, 10);
        }
        
        return keyPoints;
    }
    
    /**
     * 计算主题相似度
     */
    private double calculateTopicSimilarity(Map<String, Double> topics1, Map<String, Double> topics2) {
        Set<String> allTopics = new HashSet<>(topics1.keySet());
        allTopics.addAll(topics2.keySet());
        
        double similarity = 0;
        for (String topic : allTopics) {
            double score1 = topics1.getOrDefault(topic, 0.0);
            double score2 = topics2.getOrDefault(topic, 0.0);
            similarity += Math.min(score1, score2);
        }
        
        return similarity;
    }
    
    /**
     * 根据关键点组织详细结构
     */
    private void organizeDetailStructure(
            List<Map<String, Object>> detailStructure,
            List<Integer> keyPoints, 
            int totalChapters,
            String[] sectionNames) {
        
        String[] colors = {
            "#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de",
            "#3ba272", "#fc8452", "#9a60b4", "#ea7ccc", "#eacf5b", 
            "#d16b6b", "#8cd0c3"
        };
        
        // 确保有足够的关键点
        if (keyPoints.size() < 3) {
            // 使用简单划分
            int segmentSize = totalChapters / sectionNames.length;
            for (int i = 0; i < sectionNames.length; i++) {
                int startChapter = i * segmentSize;
                int endChapter = (i == sectionNames.length - 1) ? totalChapters : (i + 1) * segmentSize;
                
                Map<String, Object> section = new HashMap<>();
                section.put("name", sectionNames[i]);
                section.put("value", endChapter - startChapter);
                section.put("startChapter", startChapter + 1);
                section.put("endChapter", endChapter);
                section.put("color", colors[i % colors.length]);
                
                detailStructure.add(section);
            }
            return;
        }
        
        // 添加开始点
        if (keyPoints.get(0) > 0) {
            keyPoints.add(0, 0);
        }
        
        // 添加结束点
        if (keyPoints.get(keyPoints.size() - 1) < totalChapters - 1) {
            keyPoints.add(totalChapters);
        }
        
        // 根据关键点划分结构
        int sectionsToCreate = Math.min(sectionNames.length, keyPoints.size() - 1);
        for (int i = 0; i < sectionsToCreate; i++) {
            int startChapter = keyPoints.get(i);
            int endChapter = keyPoints.get(i + 1);
            
            Map<String, Object> section = new HashMap<>();
            section.put("name", sectionNames[i]);
            section.put("value", endChapter - startChapter);
            section.put("startChapter", startChapter + 1);
            section.put("endChapter", endChapter);
            section.put("color", colors[i % colors.length]);
            
            detailStructure.add(section);
        }
    }
    
    /**
     * 生成默认主结构
     */
    private List<Map<String, Object>> generateDefaultMainStructure(int totalChapters) {
        List<Map<String, Object>> structure = new ArrayList<>();
        String[] sections = {"开端", "铺垫", "发展", "高潮", "结局"};
        double[] percentages = {0.1, 0.2, 0.4, 0.2, 0.1};
        String[] colors = {"#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de"};
        
        int startChapter = 0;
        for (int i = 0; i < sections.length; i++) {
            int sectionChapters = (int) Math.round(totalChapters * percentages[i]);
            int endChapter = Math.min(startChapter + sectionChapters, totalChapters);
            
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
        List<Map<String, Object>> structure = new ArrayList<>();
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
        
        int startChapter = 0;
        for (int i = 0; i < sections.length; i++) {
            int sectionChapters = (int) Math.round(totalChapters * percentages[i]);
            if (sectionChapters == 0) sectionChapters = 1; // 确保每部分至少有一章
            int endChapter = Math.min(startChapter + sectionChapters, totalChapters);
            
            if (i == sections.length - 1) {
                endChapter = totalChapters; // 确保最后一部分覆盖到最后一章
            }
            
            Map<String, Object> section = new HashMap<>();
            section.put("name", sections[i]);
            section.put("value", endChapter - startChapter);
            section.put("startChapter", startChapter + 1);
            section.put("endChapter", endChapter);
            section.put("color", colors[i]);
            
            structure.add(section);
            startChapter = endChapter;
            
            // 如果已经达到了最后一章，中断循环
            if (endChapter == totalChapters) {
                break;
            }
        }
        
        return structure;
    }
    
    /**
     * 计算平均句长
     */
    private double calculateAverageSentenceLength(String text) {
        String[] sentences = text.split("[。！？.!?]+");
        if (sentences.length == 0) return 0;
        
        int totalChars = 0;
        for (String sentence : sentences) {
            totalChars += sentence.trim().length();
        }
        
        return (double) totalChars / sentences.length;
    }
    
    /**
     * 计算对话比例
     */
    private double calculateDialogueRatio(String text) {
        int dialogueChars = 0;
        int totalChars = text.length();
        
        // 简单对话检测：中文或英文引号内的内容
        String dialoguePattern = "[\"\\u201C\\u201D].*?[\"\\u201C\\u201D]|[\\u2018\\u2019].*?[\\u2018\\u2019]";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(dialoguePattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            dialogueChars += matcher.group().length();
        }
        
        return totalChars > 0 ? (double) dialogueChars / totalChars : 0;
    }
} 