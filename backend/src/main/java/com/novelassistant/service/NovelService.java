package com.novelassistant.service;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface NovelService {
    
    /**
     * 获取所有小说列表
     * @return 小说列表
     */
    List<Novel> getAllNovels();
    
    /**
     * 根据ID获取小说详情
     * @param id 小说ID
     * @return 小说详情
     */
    Novel getNovelById(Long id);
    
    /**
     * 获取小说处理状态
     * @param id 小说ID
     * @return 处理状态信息
     */
    Map<String, Object> getNovelStatus(Long id);
    
    /**
     * 处理上传的小说文件
     * @param file 小说文件
     * @param title 标题
     * @param author 作者
     * @return 处理结果
     */
    Map<String, Object> processNovel(MultipartFile file, String title, String author);
    
    /**
     * 获取小说的章节列表
     * @param novelId 小说ID
     * @return 章节列表
     */
    List<Chapter> getChaptersByNovelId(Long novelId);
    
    /**
     * 获取小说的标签列表
     * @param novelId 小说ID
     * @return 标签列表
     */
    List<Tag> getTagsByNovelId(Long novelId);
} 