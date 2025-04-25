package com.novelassistant.service;

import com.novelassistant.entity.Chapter;
import java.util.List;

/**
 * 文本处理服务
 * 用于处理文本内容并生成章节
 */
public interface ProcessingService {
    
    /**
     * 将文本内容处理并生成章节列表
     * 
     * @param content 原始文本内容
     * @param novelId 小说ID
     * @return 生成的章节列表
     */
    List<Chapter> processTextToChapters(String content, Long novelId);
    
    /**
     * 检查内容是否只是章节标题列表，没有实际内容
     *
     * @param content 要检查的内容
     * @return 如果内容只包含章节标题列表返回true，否则返回false
     */
    boolean isChapterTitleList(String content);
} 