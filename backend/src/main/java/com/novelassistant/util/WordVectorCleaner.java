package com.novelassistant.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 词向量文件清理和标准化工具
 * 用于处理和修复词向量文件中的编码问题和格式问题
 */
public class WordVectorCleaner {
    
    private static final Logger logger = LoggerFactory.getLogger(WordVectorCleaner.class);
    
    // 向量值的正则表达式模式
    private static final Pattern VECTOR_PATTERN = Pattern.compile("^[\\s]*([\\-]?[0-9]+(\\.[0-9]+)?[\\s]*)+$");
    
    /**
     * 清理和标准化词向量文件
     * 
     * @param inputFilePath 输入文件路径
     * @param outputFilePath 输出文件路径
     * @return 成功处理的词向量数量
     */
    public static int cleanWordVectorFile(String inputFilePath, String outputFilePath) {
        logger.info("开始清理词向量文件: {}", inputFilePath);
        
        List<String> validLines = new ArrayList<>();
        int vectorDimension = 0;
        int totalVectorsProcessed = 0;
        int validVectorsCount = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFilePath), StandardCharsets.UTF_8))) {
            
            // 读取第一行，获取词向量数量和维度信息
            String firstLine = reader.readLine();
            if (firstLine != null && !firstLine.trim().isEmpty()) {
                String[] headerParts = firstLine.trim().split("\\s+");
                if (headerParts.length == 2) {
                    try {
                        int declaredVectorCount = Integer.parseInt(headerParts[0]);
                        vectorDimension = Integer.parseInt(headerParts[1]);
                        
                        logger.info("词向量文件声明包含 {} 个向量，每个维度为 {}", declaredVectorCount, vectorDimension);
                        validLines.add(headerParts[0] + " " + headerParts[1]); // 保留头信息
                    } catch (NumberFormatException e) {
                        logger.warn("头信息解析错误，将重新计算词向量数量和维度");
                    }
                }
            }
            
            // 处理每一行
            String line;
            while ((line = reader.readLine()) != null) {
                totalVectorsProcessed++;
                
                // 跳过空行
                if (line.trim().isEmpty()) {
                    logger.debug("跳过空行: 行 {}", totalVectorsProcessed);
                    continue;
                }
                
                // 分割词和向量值
                int firstSpaceIndex = line.indexOf(' ');
                if (firstSpaceIndex <= 0) {
                    logger.debug("跳过格式错误的行: 行 {}, 内容: {}", totalVectorsProcessed, line);
                    continue;
                }
                
                String word = line.substring(0, firstSpaceIndex).trim();
                String vectorValues = line.substring(firstSpaceIndex).trim();
                
                // 验证词不为空
                if (word.isEmpty()) {
                    logger.debug("跳过词为空的行: 行 {}", totalVectorsProcessed);
                    continue;
                }
                
                // 验证向量值格式
                if (!VECTOR_PATTERN.matcher(vectorValues).matches()) {
                    logger.debug("跳过向量值格式错误的行: 行 {}, 词: {}", totalVectorsProcessed, word);
                    continue;
                }
                
                // 验证向量维度
                String[] values = vectorValues.trim().split("\\s+");
                if (vectorDimension > 0 && values.length != vectorDimension) {
                    logger.debug("跳过维度不匹配的行: 行 {}, 词: {}, 维度: {}", totalVectorsProcessed, word, values.length);
                    continue;
                }
                
                // 添加有效行
                validLines.add(word + " " + vectorValues);
                validVectorsCount++;
            }
            
            // 如果没有读取到有效的向量维度，使用第一个有效向量的维度
            if (vectorDimension == 0 && validVectorsCount > 0) {
                String[] firstVector = validLines.get(1).substring(validLines.get(1).indexOf(' ')).trim().split("\\s+");
                vectorDimension = firstVector.length;
                
                // 更新头信息
                validLines.set(0, validVectorsCount + " " + vectorDimension);
            }
            
            // 写入清理后的文件
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8))) {
                
                // 写入更新后的头信息
                writer.write(validVectorsCount + " " + vectorDimension);
                writer.newLine();
                
                // 写入有效的向量行
                for (int i = 1; i < validLines.size(); i++) {
                    writer.write(validLines.get(i));
                    writer.newLine();
                }
            }
            
            logger.info("词向量文件清理完成。处理了 {} 行，有效向量: {} 个，维度: {}", 
                    totalVectorsProcessed, validVectorsCount, vectorDimension);
            logger.info("清理后的文件已保存至: {}", outputFilePath);
            
        } catch (IOException e) {
            logger.error("清理词向量文件时出错", e);
        }
        
        return validVectorsCount;
    }
    
    /**
     * 主方法，可以直接运行此工具类进行文件处理
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("用法: java com.novelassistant.util.WordVectorCleaner <输入文件路径> <输出文件路径>");
            return;
        }
        
        String inputFile = args[0];
        String outputFile = args[1];
        
        int processedCount = cleanWordVectorFile(inputFile, outputFile);
        System.out.println("处理完成，共清理 " + processedCount + " 个有效词向量。");
    }
} 