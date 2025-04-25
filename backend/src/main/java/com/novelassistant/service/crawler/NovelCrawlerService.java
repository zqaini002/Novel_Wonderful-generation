package com.novelassistant.service.crawler;

import java.util.Map;

/**
 * 小说爬虫服务接口
 * 用于从网络获取小说信息
 */
public interface NovelCrawlerService {
    
    /**
     * 从URL获取小说信息
     * @param url 小说URL
     * @param maxChapters 最大抓取章节数，0表示不限制
     * @return 包含小说信息的Map对象
     */
    Map<String, Object> crawlNovelFromUrl(String url, int maxChapters);
    
    /**
     * 检查URL是否受支持
     * @param url 待检查的URL
     * @return 是否支持爬取该URL
     */
    boolean isSupportedUrl(String url);
} 