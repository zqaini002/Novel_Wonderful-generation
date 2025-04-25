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
     * 检查小说是否存在
     * @param id 小说ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsNovelById(Long id);
    
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
     * @param userId 上传用户ID
     * @return 处理结果
     */
    Map<String, Object> processNovel(MultipartFile file, String title, String author, Long userId);
    
    /**
     * 从URL导入小说
     * @param url 小说URL
     * @param userId 用户ID
     * @param title 可选的小说标题，如果提供则覆盖从网页提取的标题
     * @param author 可选的小说作者，如果提供则覆盖从网页提取的作者
     * @param maxChapters 最大章节数限制，0表示不限制
     * @return 处理结果
     */
    Map<String, Object> importNovelFromUrl(String url, Long userId, String title, String author, int maxChapters);
    
    /**
     * 从URL导入小说（兼容旧接口）
     * @param url 小说URL
     * @param userId 用户ID
     * @param maxChapters 最大章节数限制，0表示不限制
     * @return 处理结果
     */
    default Map<String, Object> importNovelFromUrl(String url, Long userId, int maxChapters) {
        return importNovelFromUrl(url, userId, null, null, maxChapters);
    }
    
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
    
    /**
     * 根据用户ID获取小说列表
     * @param userId 用户ID
     * @return 该用户上传的小说列表
     */
    List<Novel> getNovelsByUserId(Long userId);
    
    /**
     * 删除小说（软删除）
     * @param id 小说ID
     * @return 操作是否成功
     */
    boolean deleteNovel(Long id);
    
    /**
     * 恢复已删除的小说
     * @param id 小说ID
     * @return 操作是否成功
     */
    boolean restoreNovel(Long id);
    
    /**
     * 获取用户已删除的小说列表
     * @param userId 用户ID
     * @return 已删除的小说列表
     */
    List<Novel> getDeletedNovelsByUserId(Long userId);
    
    /**
     * 更新小说标签
     * @param novelId 小说ID
     * @return 更新的标签数量
     */
    int refreshNovelTags(Long novelId);
    
    /**
     * 批量更新所有小说标签
     * @return 更新的小说数量
     */
    int refreshAllNovelTags();
    
    /**
     * 生成小说整体摘要
     * @param novel 小说实体
     * @return 生成的摘要
     */
    String generateOverallSummary(Novel novel);
} 