package com.novelassistant.service.impl;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Tag;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.ChapterRepository;
import com.novelassistant.repository.TagRepository;
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
    public Map<String, Object> getNovelStatus(Long id) {
        Novel novel = getNovelById(id);
        
        Map<String, Object> status = new HashMap<>();
        status.put("status", novel.getProcessingStatus().name());
        status.put("processedChapters", novel.getProcessedChapters());
        status.put("totalChapters", novel.getTotalChapters());
        
        return status;
    }
    
    @Override
    @Transactional
    public Map<String, Object> processNovel(MultipartFile file, String title, String author) {
        try {
            // 创建新小说记录
            Novel novel = new Novel(title, author);
            novel.setProcessingStatus(Novel.ProcessingStatus.PENDING);
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
        return chapterRepository.findByNovelOrderByChapterNumberAsc(novel);
    }
    
    @Override
    public List<Tag> getTagsByNovelId(Long novelId) {
        Novel novel = getNovelById(novelId);
        return tagRepository.findByNovel(novel);
    }
    
    @Transactional
    protected void processNovelFile(Long novelId, String filePath) throws IOException {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));
        
        // 更新状态为处理中
        novel.setProcessingStatus(Novel.ProcessingStatus.PROCESSING);
        novelRepository.save(novel);
        
        // 读取并处理小说文件
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            // 这里应该有实际的小说内容提取和分析逻辑
            // 简化版：按行读取，每100行作为一个章节
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
                        saveChapter(novel, chapterCount, chapterTitle, currentChapter.toString());
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
                saveChapter(novel, chapterCount, chapterTitle, currentChapter.toString());
            }
            
            // 更新小说元数据
            novel.setTotalChapters(chapterCount);
            novel.setProcessedChapters(chapterCount);
            novel.setDescription("通过文件上传导入的小说，共" + chapterCount + "章");
            
            // 生成示例摘要
            novel.setOverallSummary("这是一部" + novel.getTitle() + "的小说，由" + novel.getAuthor() + "创作。");
            novel.setWorldBuildingSummary("小说中的世界设定...");
            novel.setCharacterDevelopmentSummary("小说中的主要人物...");
            
            // 添加一些示例标签
            addTag(novel, "小说", Tag.TagType.INFO);
            
            if (novel.getTitle().contains("修真") || novel.getTitle().contains("仙")) {
                addTag(novel, "修真", Tag.TagType.INFO);
                addTag(novel, "仙侠世界", Tag.TagType.POSITIVE);
            } else if (novel.getTitle().contains("科技") || novel.getTitle().contains("未来")) {
                addTag(novel, "科幻", Tag.TagType.INFO);
                addTag(novel, "未来世界", Tag.TagType.POSITIVE);
            }
            
            // 更新状态为已完成
            novel.setProcessingStatus(Novel.ProcessingStatus.COMPLETED);
            novelRepository.save(novel);
            
        } catch (Exception e) {
            novel.setProcessingStatus(Novel.ProcessingStatus.FAILED);
            novelRepository.save(novel);
            throw e;
        }
    }
    
    private void saveChapter(Novel novel, int chapterNumber, String title, String content) {
        Chapter chapter = new Chapter(novel, chapterNumber, title);
        chapter.setContent(content);
        
        // 简单摘要：取内容前100个字符
        String summaryText = content.length() > 100 ? 
                content.substring(0, 100) + "..." : content;
        chapter.setSummary(summaryText);
        
        // 简单关键词提取：取标题中的词
        List<String> keywords = new ArrayList<>();
        for (String word : title.split("\\s+")) {
            if (word.length() > 1) {
                keywords.add(word);
            }
        }
        if (keywords.isEmpty()) {
            keywords.add("章节" + chapterNumber);
        }
        chapter.setKeywords(keywords);
        
        chapterRepository.save(chapter);
    }
    
    private void addTag(Novel novel, String name, Tag.TagType type) {
        Tag tag = new Tag(novel, name, type);
        tagRepository.save(tag);
    }
} 