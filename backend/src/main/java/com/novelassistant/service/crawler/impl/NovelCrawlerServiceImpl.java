package com.novelassistant.service.crawler.impl;

import com.novelassistant.service.crawler.NovelCrawlerService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

/**
 * 小说爬虫服务实现类
 * 用于从笔趣阁网站获取小说信息
 */
@Service
public class NovelCrawlerServiceImpl implements NovelCrawlerService {
    
    private static final Logger logger = LoggerFactory.getLogger(NovelCrawlerServiceImpl.class);
    
    // 支持的域名列表
    private static final List<String> SUPPORTED_DOMAINS = Arrays.asList(
            "biq03.cc", "www.biq03.cc"
    );
    
    @Override
    public Map<String, Object> crawlNovelFromUrl(String url, int maxChapters) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取网页内容
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
                    .timeout(30000)
                    .get();
            
            // 解析小说标题和作者
            String title = document.select("h1").text().trim();
            String author = "";
            Element authorElement = document.selectFirst("div.bookinfo > div > p:contains(作者)");
            if (authorElement != null) {
                author = authorElement.text().replace("作者：", "").trim();
            } else {
                // 尝试从内容简介中提取作者
                Element infoElement = document.selectFirst("meta[property=og:novel:author]");
                if (infoElement != null) {
                    author = infoElement.attr("content").trim();
                }
            }
            
            result.put("title", title);
            result.put("author", author);
            result.put("sourceUrl", url);
            
            // 提取内容简介
            Element descElement = document.selectFirst("div#intro");
            String description = "";
            if (descElement != null) {
                description = descElement.text().trim();
            } else {
                // 尝试其他选择器
                descElement = document.selectFirst("meta[property=og:description]");
                if (descElement != null) {
                    description = descElement.attr("content").trim();
                }
            }
            result.put("description", description);
            
            // 提取章节目录
            List<Map<String, String>> chapters = new ArrayList<>();
            
            // 查找章节列表容器
            Element chapterListContainer = document.selectFirst("div.listmain");
            if (chapterListContainer == null) {
                chapterListContainer = document.selectFirst("div#list");
            }
            
            if (chapterListContainer != null) {
                Elements chapterElements = chapterListContainer.select("dl > dd");
                
                // 限制章节数量
                int limit = (maxChapters > 0 && maxChapters < chapterElements.size()) ? maxChapters : chapterElements.size();
                
                Random random = new Random();
                for (int i = 0; i < limit; i++) {
                    Element chapterElement = chapterElements.get(i);
                    Element linkElement = chapterElement.selectFirst("a");
                    String chapterTitle = chapterElement.text().trim();
                    
                    // 提取章节编号
                    int chapterNumber = i + 1;
                    Pattern pattern = Pattern.compile("第([\\d零一二三四五六七八九十百千万亿]+)章");
                    Matcher matcher = pattern.matcher(chapterTitle);
                    if (matcher.find()) {
                        try {
                            String numStr = matcher.group(1);
                            // 这里仅处理数字形式，中文数字转换需要额外逻辑
                            if (numStr.matches("\\d+")) {
                                chapterNumber = Integer.parseInt(numStr);
                            }
                        } catch (Exception e) {
                            // 解析失败，使用索引作为章节号
                            logger.warn("解析章节号失败: {}", e.getMessage());
                        }
                    }
                    
                    String chapterUrl = "";
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        if (href.startsWith("http")) {
                            chapterUrl = href;
                        } else {
                            // 相对路径转绝对路径
                            chapterUrl = getBaseUrl(url) + href;
                        }
                    }
                    
                    Map<String, String> chapter = new HashMap<>();
                    chapter.put("title", chapterTitle);
                    chapter.put("url", chapterUrl);
                    chapter.put("number", String.valueOf(chapterNumber));
                    
                    // 获取章节内容
                    try {
                        String chapterContent = fetchChapterContent(chapterUrl);
                        chapter.put("content", chapterContent);
                        logger.info("成功获取章节内容: {}", chapterTitle);
                    } catch (Exception e) {
                        logger.error("获取章节 {} 内容失败: {}", chapterTitle, e.getMessage());
                        chapter.put("content", ""); // 设置空内容
                    }
                    
                    chapters.add(chapter);
                    
                    // 加入随机延迟，避免频繁请求
                    try {
                        // 延迟时间在1000-3000毫秒之间随机
                        int delayTime = 1000 + random.nextInt(2000);
                        logger.debug("章节爬取延迟: {}ms", delayTime);
                        Thread.sleep(delayTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            result.put("chapters", chapters);
            result.put("success", true);
            
        } catch (IOException e) {
            logger.error("爬取小说失败: {}", e.getMessage());
            result.put("success", false);
            result.put("error", "爬取小说失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取章节内容
     * @param chapterUrl 章节URL
     * @return 章节内容
     * @throws IOException 如果获取失败
     */
    private String fetchChapterContent(String chapterUrl) throws IOException {
        int maxRetries = 3; // 最大重试次数
        int retryCount = 0;
        int retryDelayMs = 1500; // 重试延迟，单位毫秒
        
        while (retryCount < maxRetries) {
            try {
                Document doc = Jsoup.connect(chapterUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
                        .timeout(30000)
                        .get();
                
                // 获取章节内容
                Element contentElement = doc.selectFirst("div#chaptercontent");
                if (contentElement == null) {
                    // 尝试其他可能的选择器
                    contentElement = doc.selectFirst("div.content");
                    if (contentElement == null) {
                        contentElement = doc.selectFirst("div.Readarea");
                        if (contentElement == null) {
                            // 尝试使用class为ReadAjax_content的div
                            contentElement = doc.selectFirst("div.ReadAjax_content");
                            if (contentElement == null) {
                                // 再尝试组合查询
                                contentElement = doc.selectFirst("div#chaptercontent.Readarea.ReadAjax_content");
                            }
                        }
                    }
                }
                
                if (contentElement != null) {
                    // 获取内容，保留段落格式
                    String content = contentElement.html()
                            .replaceAll("<br\\s*/*>", "\n") // 将<br>替换为换行符
                            .replaceAll("<script[\\s\\S]*?</script>", "") // 移除脚本标签
                            .replaceAll("<[^>]*>", ""); // 移除其他HTML标签
                    
                    // 清理内容
                    content = content.trim()
                            .replaceAll("\\s*\n\\s*", "\n\n") // 标准化换行
                            .replaceAll("(\\n\\s*){3,}", "\n\n") // 删除过多的空行
                            .replaceAll("请百度搜索.*", "") // 移除常见的推广文字
                            .replaceAll("www\\..*\\.com", "") // 移除网站链接
                            .replaceAll("笔趣.*", ""); // 移除笔趣阁相关文字
                    
                    logger.info("成功提取章节内容，长度: {}", content.length());
                    return content;
                }
                
                // 记录HTML内容以便调试
                logger.error("未找到章节内容，HTML: {}", doc.html().substring(0, Math.min(500, doc.html().length())));
                throw new IOException("未找到章节内容");
                
            } catch (IOException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    logger.error("获取章节内容失败，已重试{}次：{}", maxRetries, e.getMessage());
                    throw e; // 重试次数用尽，抛出异常
                }
                
                // 重试前增加延迟时间，并记录重试日志
                logger.warn("获取章节内容失败，将进行第{}次重试，URL: {}, 错误: {}", 
                        retryCount, chapterUrl, e.getMessage());
                
                try {
                    // 每次重试增加更长的延迟时间
                    Thread.sleep(retryDelayMs * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("获取章节内容被中断", e);
                }
                
                // 更换User-Agent，减少被识别为爬虫的可能
                String[] userAgents = {
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15",
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1"
                };
                // 随机选择一个User-Agent
                int randomIndex = new Random().nextInt(userAgents.length);
                logger.info("重试使用新的User-Agent: {}", userAgents[randomIndex]);
            }
        }
        
        throw new IOException("获取章节内容失败，超过最大重试次数");
    }
    
    @Override
    public boolean isSupportedUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String host = parsedUrl.getHost().toLowerCase();
            
            for (String domain : SUPPORTED_DOMAINS) {
                if (host.equals(domain) || host.endsWith("." + domain)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            logger.error("URL检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取URL的基础部分
     */
    private String getBaseUrl(String url) {
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String protocol = parsedUrl.getProtocol();
            String host = parsedUrl.getHost();
            int port = parsedUrl.getPort();
            
            StringBuilder baseUrl = new StringBuilder();
            baseUrl.append(protocol).append("://").append(host);
            
            if (port != -1) {
                baseUrl.append(":").append(port);
            }
            
            return baseUrl.toString();
        } catch (Exception e) {
            logger.error("获取基础URL失败: {}", e.getMessage());
            return url.substring(0, url.lastIndexOf("/") + 1);
        }
    }
} 