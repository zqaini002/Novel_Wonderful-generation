package com.novelassistant.util;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

/**
 * 词向量工具测试类
 * 用于测试词向量文件的清理和加载
 */
public class WordVectorTest {

    private static final Logger logger = LoggerFactory.getLogger(WordVectorTest.class);

    public static void main(String[] args) {
        // 指定原始词向量文件和清理后的词向量文件路径
        String originalFilePath = "E:/Novel_Wonderful generation/novel-assistant/backend/src/main/resources/models/5000-small.txt";
        String cleanedFilePath = "E:/Novel_Wonderful generation/novel-assistant/backend/src/main/resources/models/5000-small-cleaned.txt";
        
        try {
            // 1. 清理词向量文件
            logger.info("开始清理词向量文件...");
            int vectorCount = WordVectorCleaner.cleanWordVectorFile(originalFilePath, cleanedFilePath);
            logger.info("清理完成，共 {} 个有效词向量", vectorCount);
            
            // 2. 尝试加载清理后的词向量文件
            logger.info("尝试加载清理后的词向量文件...");
            File cleanedFile = new File(cleanedFilePath);
            
            try {
                // 测试各种加载方法
                testLoadMethod("readWord2Vec", () -> WordVectorSerializer.readWord2Vec(cleanedFile));
                testLoadMethod("readWord2VecModel(text)", () -> WordVectorSerializer.readWord2VecModel(cleanedFile, true));
                testLoadMethod("readWord2VecModel(binary)", () -> WordVectorSerializer.readWord2VecModel(cleanedFile, false));
                testLoadMethod("readBinaryModel", () -> WordVectorSerializer.readBinaryModel(cleanedFile, true, true));
            } catch (Exception e) {
                logger.error("加载测试失败", e);
            }
            
        } catch (Exception e) {
            logger.error("测试过程中出错", e);
        }
    }
    
    /**
     * 测试特定的加载方法
     */
    private static void testLoadMethod(String methodName, WordVecLoader loader) {
        try {
            logger.info("测试加载方法: {}", methodName);
            long startTime = System.currentTimeMillis();
            
            Word2Vec model = loader.load();
            
            long endTime = System.currentTimeMillis();
            logger.info("加载成功！词汇表大小: {}，耗时: {} 秒", 
                    model.getVocab().numWords(), (endTime - startTime) / 1000.0);
            
            // 测试一些简单的词向量操作
            testWordVectorModel(model);
            
        } catch (Exception e) {
            logger.error("使用 {} 加载失败: {}", methodName, e.getMessage());
        }
    }
    
    /**
     * 测试词向量模型的基本功能
     */
    private static void testWordVectorModel(Word2Vec model) {
        // 获取前10个词汇
        int count = 0;
        logger.info("词汇表示例:");
        for (String word : model.getVocab().words()) {
            if (count++ < 10) {
                logger.info("  - {}", word);
            } else {
                break;
            }
        }
        
        // 尝试获取一些中文词的向量
        String[] testWords = {"的", "是", "在", "了", "和"};
        for (String word : testWords) {
            if (model.hasWord(word)) {
                double[] vector = model.getWordVector(word);
                logger.info("词 '{}' 的向量维度: {}", word, vector.length);
            } else {
                logger.info("词 '{}' 不在词汇表中", word);
            }
        }
        
        // 尝试计算词相似度
        if (model.hasWord("的") && model.hasWord("和")) {
            double similarity = model.similarity("的", "和");
            logger.info("'的'和'和'的相似度: {}", similarity);
        }
        
        // 尝试查找最相似的词
        if (model.hasWord("中国")) {
            logger.info("与'中国'最相似的词:");
            Collection<String> nearestWords = model.wordsNearest("中国", 5);
            for (String word : nearestWords) {
                logger.info("  - {}", word);
            }
        }
    }
    
    /**
     * 词向量加载器接口
     */
    @FunctionalInterface
    private interface WordVecLoader {
        Word2Vec load() throws Exception;
    }
} 