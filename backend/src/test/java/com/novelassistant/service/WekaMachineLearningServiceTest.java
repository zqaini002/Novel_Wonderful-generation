package com.novelassistant.service;

import com.novelassistant.model.ClusterResult;
import com.novelassistant.model.SentimentResult;
import com.novelassistant.service.impl.WekaMachineLearningServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WekaMachineLearningServiceTest {

    private WekaMachineLearningServiceImpl machineLearningService;

    @BeforeEach
    public void setUp() {
        machineLearningService = new WekaMachineLearningServiceImpl();
    }

    @Test
    public void testCalculateTextSimilarity() {
        String text1 = "这是一个关于魔法世界的故事";
        String text2 = "这个故事讲述了魔法世界的冒险";
        
        double similarity = machineLearningService.calculateTextSimilarity(text1, text2);
        
        assertTrue(similarity >= 0.0 && similarity <= 1.0, 
                "Similarity should be between 0 and 1, got: " + similarity);
    }

    @Test
    public void testClusterTexts() {
        List<String> texts = Arrays.asList(
                "魔法师的冒险故事",
                "勇者斗恶龙的传说",
                "魔法师的学习之路",
                "勇者的成长历程",
                "魔法世界的奇遇"
        );
        
        List<ClusterResult> clusterResults = machineLearningService.clusterTexts(texts, 2);
        
        assertNotNull(clusterResults, "Cluster results should not be null");
        assertTrue(clusterResults.size() > 0, "Should have at least one cluster result");
        assertEquals(texts.size(), clusterResults.size(), "All texts should be assigned to clusters");
    }

    @Test
    public void testClassifyText() {
        String text = "年轻的魔法师踏上了寻找龙的旅程";
        List<String> categories = Arrays.asList("fantasy", "sci-fi", "mystery");
        
        Map<String, Double> result = machineLearningService.classifyText(text, categories);
        
        assertNotNull(result, "Classification result should not be null");
        assertTrue(result.size() > 0, "Should have at least one category score");
        
        // 检查分数总和接近1.0（概率分布）
        double sum = result.values().stream().mapToDouble(Double::doubleValue).sum();
        assertTrue(Math.abs(sum - 1.0) < 0.1, "Sum of probabilities should be close to 1.0");
    }

    @Test
    public void testDeepSentimentAnalysis() {
        String positiveText = "这本小说非常精彩，我很喜欢";
        String negativeText = "这个故事很差，我非常失望";
        
        SentimentResult positiveResult = machineLearningService.deepSentimentAnalysis(positiveText);
        SentimentResult negativeResult = machineLearningService.deepSentimentAnalysis(negativeText);
        
        assertNotNull(positiveResult, "Positive sentiment result should not be null");
        assertNotNull(negativeResult, "Negative sentiment result should not be null");
        
        assertTrue(positiveResult.getScore() > 0.5, "Positive text should have score > 0.5");
        assertTrue(negativeResult.getScore() < 0.5, "Negative text should have score < 0.5");
    }
    
    @Test
    public void testTrainWordVectorModel() {
        List<String> corpus = Arrays.asList(
                "魔法师的冒险故事",
                "勇者斗恶龙的传说",
                "魔法师的学习之路",
                "勇者的成长历程",
                "魔法世界的奇遇"
        );
        
        boolean result = machineLearningService.trainWordVectorModel(corpus, 100);
        
        assertTrue(result, "Word vector model training should succeed");
    }
    
    @Test
    public void testPredictPlotDevelopment() {
        List<String> previousChapters = Arrays.asList(
                "魔法师发现了一个神秘的洞穴",
                "洞穴中藏着一把传说中的剑"
        );
        
        Map<String, Double> predictions = machineLearningService.predictPlotDevelopment(previousChapters);
        
        assertNotNull(predictions, "Plot predictions should not be null");
    }
    
    @Test
    public void testExtractTopicsWithLDA() {
        String text = "魔法师的冒险故事讲述了一个年轻的魔法师如何在魔法世界中学习魔法，战胜恶龙，保护魔法世界的和平。";
        
        Map<Integer, List<String>> topics = machineLearningService.extractTopicsWithLDA(text, 2);
        
        assertNotNull(topics, "Topics should not be null");
        assertTrue(topics.size() > 0, "Should extract at least one topic");
    }
} 