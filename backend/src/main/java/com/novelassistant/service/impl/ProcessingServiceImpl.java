package com.novelassistant.service.impl;

import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.Novel;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.service.NlpService;
import com.novelassistant.service.ProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本处理服务实现类
 * 用于处理文本内容并生成章节
 */
@Service
public class ProcessingServiceImpl implements ProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessingServiceImpl.class);
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private NlpService nlpService;
    
    @Override
    public List<Chapter> processTextToChapters(String content, Long novelId) {
        logger.info("开始处理文本并生成章节，小说ID: {}, 内容长度: {} 字符", novelId, content.length());
        
        // 获取小说实体
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("小说不存在: " + novelId));
        
        List<Chapter> chapters = new ArrayList<>();
        
        // 输出前100个字符，辅助调试
        String previewContent = content.length() > 100 ? content.substring(0, 100) : content;
        logger.info("内容前100字符预览: {}", previewContent.replaceAll("\n", "\\\\n"));
        
        // 检查内容是否为空
        if (content == null || content.trim().isEmpty()) {
            logger.error("输入内容为空，无法处理");
            // 创建一个空章节，防止返回null
            Chapter emptyChapter = new Chapter(novel, 1, "空章节");
            emptyChapter.setContent("内容为空");
            emptyChapter.setSummary("内容为空");
            chapters.add(emptyChapter);
            return chapters;
        }
        
        // 预处理内容，清理HTML残留和不可见字符
        logger.info("开始清理内容...");
        String cleanedContent = cleanContent(content);
        logger.info("内容清理完成，清理后长度: {} 字符", cleanedContent.length());
        
        // 如果清理后内容为空，使用原始内容
        if (cleanedContent.trim().isEmpty()) {
            logger.warn("清理后内容为空，将使用原始内容");
            cleanedContent = content;
        }
        
        // 检测是否只有章节列表没有实际内容
        boolean isOnlyChapterList = isChapterTitleList(cleanedContent);
        if (isOnlyChapterList) {
            logger.warn("检测到内容可能只包含章节标题列表，没有实际章节内容");
            
            // 从章节标题列表中提取章节
            List<String> chapterTitles = extractChapterTitles(cleanedContent);
            logger.info("从章节列表提取出 {} 个章节标题", chapterTitles.size());
            
            if (!chapterTitles.isEmpty()) {
                // 创建包含警告信息的章节
                for (int i = 0; i < chapterTitles.size(); i++) {
                    Chapter chapter = new Chapter(novel, i + 1, chapterTitles.get(i));
                    // 将标题作为内容，并添加警告信息
                    String warningContent = "【系统提示：此章节内容未能成功获取】\n\n" +
                            "可能原因：\n" +
                            "1. 目标网站需要登录或采用了反爬虫技术\n" +
                            "2. 网站结构已变更或内容受到保护\n" +
                            "3. 网络连接问题导致获取不完整\n\n" +
                            "您可以尝试：\n" +
                            "- 上传本地小说文件\n" +
                            "- 使用其他URL源\n" +
                            "- 手动添加内容";
                    
                    chapter.setContent(warningContent);
                    chapter.setSummary("章节内容未能获取");
                    chapters.add(chapter);
                }
                logger.info("从章节列表创建了 {} 个带警告信息的章节", chapters.size());
                return chapters;
            }
        }
        
        // 强制分割方法：直接按章节标题模式分割
        logger.info("尝试直接按章节标题模式强制分割...");
        chapters = forceChapterSplit(cleanedContent, novel);
        
        if (!chapters.isEmpty()) {
            logger.info("强制分割成功，得到 {} 个章节", chapters.size());
            
            // 检查章节内容是否有效
            boolean validContentFound = false;
            for (Chapter chapter : chapters) {
                if (chapter.getContent() != null && 
                    chapter.getContent().length() > 100 && 
                    !chapter.getContent().contains("【系统提示：此章节内容未能成功获取】")) {
                    validContentFound = true;
                    break;
                }
            }
            
            if (!validContentFound) {
                logger.warn("章节分割成功但内容可能不完整，所有章节内容都比较短");
                // 将第一个章节设置为提示信息
                if (!chapters.isEmpty()) {
                    Chapter firstChapter = chapters.get(0);
                    String warningContent = "【系统提示：爬取的内容可能不完整】\n\n" +
                        "系统仅抓取到章节标题列表，但没有获取到完整的章节内容。\n\n" +
                        "可能原因：\n" +
                        "1. 目标网站需要登录或采用了反爬虫技术\n" +
                        "2. 网站结构已变更或内容受到保护\n\n" +
                        "您可以尝试：\n" +
                        "- 上传本地小说文件\n" +
                        "- 使用其他URL源";
                    
                    firstChapter.setContent(firstChapter.getContent() + "\n\n" + warningContent);
                }
            }
            
            return chapters;
        }
        
        // 如果强制分割失败，检查是否包含章节标题
        boolean isPreChaptered = detectPreChapteredContent(cleanedContent);
        if (isPreChaptered) {
            logger.info("检测到内容包含章节标题标记，尝试提取章节");
            chapters = processPreChapteredContent(cleanedContent, novel);
            logger.info("预分章节处理完成，得到 {} 个章节", chapters.size());
            
            // 如果通过预分章节方式处理成功，直接返回结果
            if (!chapters.isEmpty()) {
                logger.info("成功处理预分章节内容，共 {} 个章节", chapters.size());
                return chapters;
            }
        }
        
        // 如果预分章节处理未成功，使用正则表达式查找章节分隔
        List<Integer> chapterPositions = findChapterPositions(cleanedContent);
        logger.info("通过正则表达式找到 {} 个章节分隔点", chapterPositions.size());
        
        // 如果找到章节分隔，处理常规章节分隔
        if (!chapterPositions.isEmpty()) {
            chapters = processChaptersByPositions(cleanedContent, chapterPositions, novel);
            logger.info("基于章节分隔点处理完成，得到 {} 个章节", chapters.size());
            return chapters;
        }
        
        // 如果没有找到章节分隔，尝试使用NLP服务检测
        chapterPositions = nlpService.detectChapterBreaks(cleanedContent);
        logger.info("通过NLP服务找到 {} 个章节分隔点", chapterPositions.size());
        
        // 如果NLP服务找到章节分隔，处理这些章节
        if (!chapterPositions.isEmpty()) {
            chapters = processChaptersByPositions(cleanedContent, chapterPositions, novel);
            logger.info("基于NLP章节分隔点处理完成，得到 {} 个章节", chapters.size());
            return chapters;
        }
        
        // 所有方法都未能找到章节分隔，尝试按空行分割
        logger.info("尝试按空行分割内容...");
        chapters = splitByEmptyLines(cleanedContent, novel);
        if (!chapters.isEmpty()) {
            logger.info("按空行分割成功，得到 {} 个章节", chapters.size());
            return chapters;
        }
        
        // 最后尝试按固定字数分割
        logger.info("尝试按固定字数分割...");
        chapters = splitByFixedLength(cleanedContent, novel);
        if (!chapters.isEmpty()) {
            logger.info("按固定字数分割成功，得到 {} 个章节", chapters.size());
            return chapters;
        }
        
        // 所有分割方法都失败，将整个文本作为一个章节
        logger.info("未找到任何章节分隔，将整个文本作为一个章节");
        Chapter chapter = new Chapter(novel, 1, "第1章");
        chapter.setContent(cleanedContent);
        
        // 使用NLP生成摘要
        String summary = nlpService.generateSummary(cleanedContent, 200);
        chapter.setSummary(summary);
        
        // 使用NLP提取关键词
        Map<String, Integer> keywordMap = nlpService.extractKeywords(cleanedContent, 10);
        List<String> keywords = new ArrayList<>(keywordMap.keySet());
        chapter.setKeywords(keywords);
        
        chapters.add(chapter);
        
        logger.info("处理完成，共生成 {} 个章节", chapters.size());
        return chapters;
    }
    
    /**
     * 清理内容，移除HTML残留和不可见字符，但保留核心内容
     */
    private String cleanContent(String content) {
        // 保存原始内容，以防清理过程中意外删除所有内容
        String originalContent = content;
        
        logger.info("原始内容长度: {} 字符", content.length());
        
        // 首先检测内容是否包含WebScraper生成的错误信息
        if (content.contains("【系统提示：抓取内容失败】") || 
            content.contains("【系统提示：抓取失败】") ||
            content.contains("【系统提示：处理章节列表时出错】")) {
            logger.warn("内容包含抓取失败提示，使用保守清理");
            return conservativeCleaning(content);
        }
        
        try {
            // 内容预览，查看前300字符来判断内容类型
            String preview = content.length() > 300 ? content.substring(0, 300) : content;
            boolean hasChapterFormat = preview.matches("(?s).*第[0-9一二三四五六七八九十百千万]+[章节].*") || 
                                    preview.contains("Chapter") || 
                                    preview.matches("(?s).*\\d+\\..*");
            
            // 如果前300字符包含章节标记和导航元素，可能是标准网络小说页面
            if (hasChapterFormat && (preview.contains("目录") || preview.contains("上一章") || preview.contains("下一章"))) {
                logger.info("检测到可能是标准网络小说章节格式，使用保守清理");
                String cleaned = conservativeCleaning(content);
                
                // 移除常见的网站导航和广告
                cleaned = cleaned.replaceAll("请收藏本站：.*", "")
                               .replaceAll("笔趣阁.*https?://[^\\s]+", "")
                               .replaceAll("『.*?加入书签.*?』", "")
                               .replaceAll("『.*?点此报错.*?』", "")
                               .replaceAll("上一章.*?目录.*?下一章", "")
                               .replaceAll(".*?手机版：.*?https?://[^\\s]+", "")
                               .replaceAll("https?://[^\\s]+", "");
                
                return cleaned;
            }
            
            // 如果有多个章节标题，几乎肯定是章节列表或内容，应该使用保守清理
            int chapterCount = 0;
            String lowerContent = content.toLowerCase();
            
            // 计算可能的章节数量
            int idx = 0;
            while ((idx = lowerContent.indexOf("第", idx)) != -1) {
                if (idx + 2 < lowerContent.length() && 
                    (lowerContent.substring(idx + 1).contains("章") || 
                     lowerContent.substring(idx + 1).contains("节"))) {
                    chapterCount++;
                }
                idx++;
            }
            
            if (chapterCount > 3) {
                logger.info("检测到可能包含{}个章节，使用保守清理", chapterCount);
                String cleaned = conservativeCleaning(content);
                
                // 进一步清理可能的广告和导航元素
                if (cleaned.contains("请收藏本站") || cleaned.contains("笔趣阁") || 
                    cleaned.contains("加入书签") || cleaned.contains("点此报错")) {
                    logger.info("检测到网站特定元素，进行深度清理");
                    cleaned = cleaned.replaceAll("请收藏本站：.*", "")
                                   .replaceAll("笔趣阁.*https?://[^\\s]+", "")
                                   .replaceAll("『.*?加入书签.*?』", "")
                                   .replaceAll("『.*?点此报错.*?』", "")
                                   .replaceAll("上一章.*?目录.*?下一章", "")
                                   .replaceAll(".*?手机版：.*?https?://[^\\s]+", "")
                                   .replaceAll("https?://[^\\s]+", "");
                }
                
                return cleaned;
            }
            
            // 常规清理
            // 移除HTML标签
            content = content.replaceAll("<[^>]+>", " ");
            
            // 移除连续的空格、换行和制表符
            content = content.replaceAll("\\s+", " ");
            
            // 移除常见不可见字符
            content = content.replaceAll("[\\x00-\\x09\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
            
            // 移除UTF-8 BOM
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }
            
            // 恢复段落换行（句号后的空格替换为换行）
            content = content.replaceAll("。 ", "。\n");
            
            // 恢复章节标题格式
            content = content.replaceAll("(第[0-9一二三四五六七八九十百千万]+[章节][^，。！？；,\\.!\\?;\\n]*)", "\n$1\n");
            
            // 替换连续多个换行为一个
            content = content.replaceAll("\\n+", "\n");
            
            logger.info("常规清理完成，清理后内容长度: {} 字符", content.length());
            
        } catch (Exception e) {
            logger.error("内容清理过程中发生错误: {}", e.getMessage());
            content = originalContent; // 恢复原始内容
        }
        
        // 如果清理后内容为空或太短，使用原始内容
        if (content == null || content.trim().isEmpty() || content.length() < originalContent.length() * 0.1) {
            logger.warn("清理后内容太短或为空，使用原始内容");
            content = originalContent;
        }
        
        return content;
    }
    
    /**
     * 保守的内容清理方法，仅移除明确的HTML标签和少量特定内容
     */
    private String conservativeCleaning(String content) {
        logger.info("执行保守清理...");
        
        try {
            // 移除HTML标签
            content = content.replaceAll("<[^>]*>", "");
            
            // 处理常见的HTML实体
            content = content.replaceAll("&nbsp;", " ")
                            .replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .replaceAll("&amp;", "&")
                            .replaceAll("&quot;", "\"")
                            .replaceAll("&#39;", "'");
            
            String[] lines = content.split("\n");
            StringBuilder cleaned = new StringBuilder();
            int lineCount = 0;
            int ignoredLineCount = 0;
            
            for (String line : lines) {
                lineCount++;
                String trimmed = line.trim();
                
                // 跳过完全空行
                if (trimmed.isEmpty()) {
                    cleaned.append("\n");
                    continue;
                }
                
                // 只跳过绝对肯定是导航栏和广告的行
                if (trimmed.equals("上一章 目录 下一章") || 
                    trimmed.equals("笔趣阁") ||
                    trimmed.equals("笔趣阁手机版") ||
                    trimmed.equals("访问：") ||
                    trimmed.startsWith("http://") ||
                    trimmed.startsWith("https://")) {
                    ignoredLineCount++;
                    continue;
                }
                
                // 保留所有其他内容，包括章节标题和正文
                cleaned.append(line).append("\n");
            }
            
            String result = cleaned.toString();
            logger.info("保守清理完成，总共{}行，跳过{}行，清理后内容长度: {} 字符", 
                       lineCount, ignoredLineCount, result.length());
            return result;
            
        } catch (Exception e) {
            logger.error("保守清理过程中发生异常: {}", e.getMessage(), e);
            // 如果保守清理也失败，直接返回原始内容
            return content;
        }
    }
    
    /**
     * 检查内容是否包含章节标题
     */
    private boolean containsChapterTitles(String content) {
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.matches("第[0-9一二三四五六七八九十百千万]+[章回节].*") || 
                line.matches("Chapter\\s*[0-9]+.*") ||
                line.matches("[0-9]+\\..*")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检测内容是否已经包含章节格式（WebScraperService可能已经处理过）
     */
    private boolean detectPreChapteredContent(String content) {
        // 分割成行
        String[] lines = content.split("\n");
        logger.info("内容总行数: {}", lines.length);
        
        int chapterTitleCount = 0;
        List<String> foundTitles = new ArrayList<>();
        
        // 计算可能是章节标题的行数
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 检查多种章节标题格式
            boolean isChapterTitle = line.matches("第[0-9一二三四五六七八九十百千万]+[章回节].*") || 
                                   line.matches("Chapter\\s*[0-9]+.*") ||
                                   line.matches("[0-9]+\\..*") ||  // 数字+点格式
                                   line.matches("\\d+\\s+.*") ||   // 数字+空格格式
                                   line.matches("第.{1,10}章.*");   // 第X章格式，更宽松匹配
            
            if (isChapterTitle) {
                chapterTitleCount++;
                foundTitles.add(line);
                // 最多记录10个标题用于记录
                if (foundTitles.size() >= 10) break;
            }
        }
        
        logger.info("找到 {} 个可能的章节标题", chapterTitleCount);
        if (!foundTitles.isEmpty()) {
            logger.info("章节标题示例: {}", foundTitles);
        }
        
        // 只要有2个或以上的可能章节标题即认为是预分章节的
        return chapterTitleCount >= 2;
    }
    
    /**
     * 强制按章节标题分割内容
     * 这是最直接的方法，尝试识别章节标题并以此为分界点分割内容
     */
    private List<Chapter> forceChapterSplit(String content, Novel novel) {
        List<Chapter> chapters = new ArrayList<>();
        
        // 查找所有可能的章节标题行及其位置
        Pattern titlePattern = Pattern.compile("(?m)^[ \\t]*(第[0-9一二三四五六七八九十百千万]+[章回节].*|Chapter\\s*[0-9]+.*|[0-9]+\\..*|第.{1,10}章.*)$");
        Matcher titleMatcher = titlePattern.matcher(content);
        
        List<Integer> titlePositions = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        
        while (titleMatcher.find()) {
            titlePositions.add(titleMatcher.start());
            titles.add(titleMatcher.group().trim());
        }
        
        logger.info("找到 {} 个章节标题", titles.size());
        
        // 如果找到至少两个章节标题，按标题位置分割内容
        if (titles.size() >= 2) {
            for (int i = 0; i < titles.size(); i++) {
                int startPos = titlePositions.get(i);
                int endPos = (i < titles.size() - 1) ? titlePositions.get(i + 1) : content.length();
                
                // 提取章节内容
                String chapterContent = content.substring(startPos, endPos).trim();
                String title = titles.get(i);
                
                // 从内容中移除标题行
                String contentWithoutTitle = chapterContent.substring(title.length()).trim();
                
                // 保存章节
                if (!contentWithoutTitle.isEmpty()) {
                    saveChapter(chapters, novel, i + 1, title, contentWithoutTitle);
                }
            }
            
            return chapters;
        }
        
        // 如果上面方法失败，尝试使用更松散的模式匹配章节
        Pattern loosePattern = Pattern.compile("(?s)(第[0-9一二三四五六七八九十百千万]+[章回节].*?)(?=第[0-9一二三四五六七八九十百千万]+[章回节]|$)");
        Matcher looseMatcher = loosePattern.matcher(content);
        
        List<String> looseChapters = new ArrayList<>();
        while (looseMatcher.find()) {
            looseChapters.add(looseMatcher.group(1).trim());
        }
        
        logger.info("使用松散模式找到 {} 个章节", looseChapters.size());
        
        if (looseChapters.size() >= 2) {
            for (int i = 0; i < looseChapters.size(); i++) {
                String chapterText = looseChapters.get(i);
                
                // 提取章节标题
                String[] lines = chapterText.split("\n", 2);
                String title = lines[0].trim();
                String content2 = lines.length > 1 ? lines[1].trim() : "";
                
                // 保存章节
                if (!content2.isEmpty()) {
                    saveChapter(chapters, novel, i + 1, title, content2);
                }
            }
            
            return chapters;
        }
        
        // 尝试以某些章节格式标记为基础分割
        return findAndSplitByCommonPatterns(content, novel);
    }
    
    /**
     * 基于常见章节格式查找和分割内容
     */
    private List<Chapter> findAndSplitByCommonPatterns(String content, Novel novel) {
        List<Chapter> chapters = new ArrayList<>();
        
        // 尝试寻找常见的章节格式
        String[] patterns = {
            "第.*?章",  // 第X章
            "Chapter\\s*\\d+", // Chapter X
            "\\d+\\.", // 数字+点
            "第\\s*\\d+\\s*[章节]" // 第X章/节 带空格
        };
        
        for (String patternStr : patterns) {
            logger.info("尝试使用模式 '{}' 查找章节", patternStr);
            
            Pattern pattern = Pattern.compile("(?s)(" + patternStr + ".*?)(?=" + patternStr + "|$)");
            Matcher matcher = pattern.matcher(content);
            
            List<String> found = new ArrayList<>();
            while (matcher.find()) {
                found.add(matcher.group(1).trim());
            }
            
            logger.info("使用模式 '{}' 找到 {} 个章节", patternStr, found.size());
            
            if (found.size() >= 2) {
                logger.info("成功使用模式 '{}' 分割出章节", patternStr);
                
                for (int i = 0; i < found.size(); i++) {
                    String chapterText = found.get(i);
                    
                    // 尝试提取章节标题
                    String[] lines = chapterText.split("\n", 2);
                    String title = lines[0].trim();
                    String chapterContent = lines.length > 1 ? lines[1].trim() : "";
                    
                    // 保存章节
                    if (!chapterContent.isEmpty()) {
                        saveChapter(chapters, novel, i + 1, title, chapterContent);
                    }
                }
                
                return chapters;
            }
        }
        
        // 所有尝试都失败，返回空列表
        return chapters;
    }
    
    /**
     * 处理已经包含章节标题的内容
     */
    private List<Chapter> processPreChapteredContent(String content, Novel novel) {
        List<Chapter> chapters = new ArrayList<>();
        
        // 尝试多种分割方式
        logger.info("尝试多种章节分割方式...");
        
        // 1. 按行分析，寻找章节标题行的位置
        String[] lines = content.split("\n");
        List<Integer> titleLineIndexes = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            boolean isTitle = line.matches("第[0-9一二三四五六七八九十百千万]+[章回节].*") || 
                             line.matches("Chapter\\s*[0-9]+.*") ||
                             line.matches("[0-9]+\\..*") ||
                             line.matches("第.{1,10}章.*");
                             
            if (isTitle) {
                titleLineIndexes.add(i);
                titles.add(line);
            }
        }
        
        logger.info("按行分析找到 {} 个章节标题行", titleLineIndexes.size());
        
        // 如果找到了足够的章节标题行，按行号分割内容
        if (titleLineIndexes.size() >= 2) {
            for (int i = 0; i < titleLineIndexes.size(); i++) {
                int startLine = titleLineIndexes.get(i) + 1; // 标题的下一行
                int endLine = (i < titleLineIndexes.size() - 1) ? 
                              titleLineIndexes.get(i + 1) : lines.length;
                
                StringBuilder contentBuilder = new StringBuilder();
                for (int j = startLine; j < endLine; j++) {
                    if (j < lines.length) {
                        contentBuilder.append(lines[j]).append("\n");
                    }
                }
                
                String chapterContent = contentBuilder.toString().trim();
                if (!chapterContent.isEmpty()) {
                    saveChapter(chapters, novel, i + 1, titles.get(i), chapterContent);
                }
            }
            
            return chapters;
        }
        
        // 2. 使用正则表达式查找章节块
        // 这个模式尝试匹配从一个章节标题到下一个章节标题（或文件结尾）的所有内容
        Pattern chapterPattern = Pattern.compile(
            "(?sm)^[ \\t]*(第[0-9一二三四五六七八九十百千万]+[章回节].*|Chapter\\s*[0-9]+.*|[0-9]+\\..*|第.{1,10}章.*)$.*?(?=^[ \\t]*(?:第[0-9一二三四五六七八九十百千万]+[章回节]|Chapter\\s*[0-9]+|[0-9]+\\.|第.{1,10}章)|\\z)",
            Pattern.MULTILINE
        );
        
        Matcher matcher = chapterPattern.matcher(content);
        List<String> chapterBlocks = new ArrayList<>();
        
        while (matcher.find()) {
            chapterBlocks.add(matcher.group().trim());
        }
        
        logger.info("使用正则表达式找到 {} 个章节块", chapterBlocks.size());
        
        if (chapterBlocks.size() >= 2) {
            for (int i = 0; i < chapterBlocks.size(); i++) {
                String block = chapterBlocks.get(i);
                String[] blockLines = block.split("\n", 2);
                
                String title = blockLines[0].trim();
                String chapterContent = blockLines.length > 1 ? blockLines[1].trim() : "";
                
                if (!chapterContent.isEmpty()) {
                    saveChapter(chapters, novel, i + 1, title, chapterContent);
                }
            }
            
            return chapters;
        }
        
        // 3. 尝试更激进的分割方法
        logger.info("尝试更激进的分割方法");
        return processAggressiveSplit(content, novel);
    }
    
    /**
     * 使用更激进的方式分割章节
     * 直接查找章节标题行，然后以此为界限分割内容
     */
    private List<Chapter> processAggressiveSplit(String content, Novel novel) {
        List<Chapter> chapters = new ArrayList<>();
        String[] lines = content.split("\n");
        
        List<Integer> titleLineIndexes = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        
        // 查找所有可能的章节标题行
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.matches("第[0-9一二三四五六七八九十百千万]+[章回节].*") || 
                line.matches("Chapter\\s*[0-9]+.*") ||
                line.matches("[0-9]+\\..*")) {
                titleLineIndexes.add(i);
                titles.add(line);
            }
        }
        
        logger.info("找到 {} 个可能的章节标题行", titleLineIndexes.size());
        
        // 如果找到了章节标题，以此为分界点分割内容
        if (!titleLineIndexes.isEmpty()) {
            for (int i = 0; i < titleLineIndexes.size(); i++) {
                int startLine = titleLineIndexes.get(i) + 1; // 标题行的下一行
                int endLine = (i < titleLineIndexes.size() - 1) ? titleLineIndexes.get(i + 1) : lines.length;
                
                StringBuilder chapterContent = new StringBuilder();
                for (int j = startLine; j < endLine; j++) {
                    chapterContent.append(lines[j]).append("\n");
                }
                
                // 保存章节
                String title = titles.get(i);
                saveChapter(chapters, novel, i + 1, title, chapterContent.toString());
            }
        }
        
        return chapters;
    }
    
    /**
     * 基于章节位置处理文本为章节
     */
    private List<Chapter> processChaptersByPositions(String content, List<Integer> chapterPositions, Novel novel) {
        List<Chapter> chapters = new ArrayList<>();
        
        for (int i = 0; i < chapterPositions.size(); i++) {
            int startPos = chapterPositions.get(i);
            int endPos = (i < chapterPositions.size() - 1) ? chapterPositions.get(i + 1) : content.length();
            
            String chapterContent = content.substring(startPos, endPos);
            String chapterTitle = extractChapterTitle(chapterContent);
            
            saveChapter(chapters, novel, i + 1, chapterTitle, chapterContent);
        }
        
        return chapters;
    }
    
    /**
     * 创建并保存章节
     */
    private void saveChapter(List<Chapter> chapters, Novel novel, int chapterNumber, String title, String content) {
        // 确保标题不超出数据库字段长度限制
        title = truncateTitle(title);
        
        Chapter chapter = new Chapter(novel, chapterNumber, title);
        chapter.setContent(content);
        
        // 使用NLP生成摘要
        String summary = nlpService.generateSummary(content, 200);
        chapter.setSummary(summary);
        
        // 使用NLP提取关键词
        Map<String, Integer> keywordMap = nlpService.extractKeywords(content, 10);
        List<String> keywords = new ArrayList<>(keywordMap.keySet());
        chapter.setKeywords(keywords);
        
        chapters.add(chapter);
    }
    
    /**
     * 使用正则表达式查找章节分隔位置
     */
    private List<Integer> findChapterPositions(String content) {
        List<Integer> positions = new ArrayList<>();
        
        // 匹配常见的章节标题格式
        // 1. "第X章 标题"
        // 2. "第X回 标题"
        // 3. "Chapter X 标题"
        Pattern pattern = Pattern.compile("(?m)^(\\s*第\\s*[0-9一二三四五六七八九十百千万零]+\\s*[章回节]|\\s*Chapter\\s*[0-9]+)[^\\n]*$");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            positions.add(matcher.start());
        }
        
        return positions;
    }
    
    /**
     * 从章节内容中提取标题
     */
    private String extractChapterTitle(String chapterContent) {
        // 提取章节的第一行作为标题
        String[] lines = chapterContent.split("\\n", 2);
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            // 如果第一行是章节标记，直接返回
            if (firstLine.matches(".*第[0-9一二三四五六七八九十百千万零]+[章回节].*") || 
                firstLine.toLowerCase().matches(".*chapter\\s*[0-9]+.*")) {
                
                // 限制标题长度，防止超出数据库限制
                return truncateTitle(firstLine);
            }
        }
        
        // 如果无法确定标题，返回默认标题
        return "未命名章节";
    }
    
    /**
     * 截断标题，确保不超过数据库字段长度限制
     * 数据库中title字段为varchar(255)
     */
    private String truncateTitle(String title) {
        if (title == null) {
            return "未命名章节";
        }
        
        // 限制标题最大长度为255字符（数据库字段限制）
        final int MAX_TITLE_LENGTH = 255;
        if (title.length() > MAX_TITLE_LENGTH) {
            logger.warn("章节标题过长，已截断：{}", title);
            // 截断标题，保留重要部分（如"第X章"）
            if (title.matches(".*第[0-9一二三四五六七八九十百千万零]+[章回节].*")) {
                // 提取章节序号部分
                int chapterPartEnd = title.indexOf("章");
                if (chapterPartEnd == -1) {
                    chapterPartEnd = title.indexOf("回");
                }
                if (chapterPartEnd == -1) {
                    chapterPartEnd = title.indexOf("节");
                }
                
                if (chapterPartEnd != -1 && chapterPartEnd < MAX_TITLE_LENGTH - 10) {
                    // 保留章节序号和部分标题
                    chapterPartEnd += 1; // 包含"章/回/节"字符
                    int remainingLength = MAX_TITLE_LENGTH - chapterPartEnd - 3; // 为"..."预留3个字符
                    if (remainingLength > 0 && chapterPartEnd + remainingLength < title.length()) {
                        return title.substring(0, chapterPartEnd) + 
                               title.substring(chapterPartEnd, chapterPartEnd + remainingLength) + 
                               "...";
                    }
                }
            }
            
            // 简单截断，保留前面部分和省略号
            return title.substring(0, MAX_TITLE_LENGTH - 3) + "...";
        }
        
        return title;
    }
    
    /**
     * 按空行分割内容
     */
    private List<Chapter> splitByEmptyLines(String content, Novel novel) {
        List<Chapter> chapters = new ArrayList<>();
        
        // 按多个连续空行分割
        String[] blocks = content.split("\\n\\s*\\n\\s*\\n+");
        logger.info("按空行分割得到 {} 个文本块", blocks.length);
        
        // 如果只有一个块，尝试按单个空行分割
        if (blocks.length < 2) {
            blocks = content.split("\\n\\s*\\n+");
            logger.info("按单个空行分割得到 {} 个文本块", blocks.length);
        }
        
        // 如果得到的块少于2个，则认为没有足够的分割点
        if (blocks.length < 2) {
            logger.info("分割后块数不足，无法按空行分割");
            return chapters;
        }
        
        // 处理每个块为一个章节
        for (int i = 0; i < blocks.length; i++) {
            String block = blocks[i].trim();
            
            // 跳过空块
            if (block.isEmpty()) continue;
            
            // 提取章节标题（第一行）
            String[] lines = block.split("\\n", 2);
            String title = lines[0].trim();
            
            // 如果标题不像章节标题，使用默认标题
            if (!title.matches(".*第.*[章节].*") && 
                !title.matches("Chapter\\s*\\d+.*") && 
                !title.matches("\\d+\\..*")) {
                title = "第" + (i + 1) + "章";
            }
            
            // 章节内容
            String chapterContent = block;
            
            saveChapter(chapters, novel, i + 1, title, chapterContent);
        }
        
        return chapters;
    }
    
    /**
     * 按固定字数分割内容（当其他所有方法都失败时的最后手段）
     */
    private List<Chapter> splitByFixedLength(String content, Novel novel) {
        List<Chapter> chapters = new ArrayList<>();
        
        // 定义每章节的最大字符数
        final int CHAPTER_LENGTH = 3000;
        
        // 计算需要分割的章节数
        int totalLength = content.length();
        int chapterCount = Math.max(1, (int) Math.ceil((double) totalLength / CHAPTER_LENGTH));
        
        logger.info("按固定字数分割，内容总长度: {} 字符，预计分割为 {} 章", totalLength, chapterCount);
        
        for (int i = 0; i < chapterCount; i++) {
            int startIndex = i * CHAPTER_LENGTH;
            int endIndex = Math.min(startIndex + CHAPTER_LENGTH, totalLength);
            
            // 获取当前章节内容
            String chapterContent = content.substring(startIndex, endIndex);
            
            // 尝试在句尾处截断，避免句子中间断开
            if (endIndex < totalLength) {
                int sentenceEndPos = chapterContent.lastIndexOf('。');
                if (sentenceEndPos > chapterContent.length() * 0.8) { // 确保不会截得太短
                    chapterContent = chapterContent.substring(0, sentenceEndPos + 1);
                }
            }
            
            // 创建章节标题
            String title = "第" + (i + 1) + "章";
            
            saveChapter(chapters, novel, i + 1, title, chapterContent);
        }
        
        return chapters;
    }
    
    /**
     * 检查内容是否只是章节标题列表，没有实际内容
     */
    public boolean isChapterTitleList(String content) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        }

        // 检测特定的错误提示信息
        if (content.contains("抓取失败") || content.contains("无法访问") || content.contains("Error:")) {
            return true;
        }

        List<String> lines = Arrays.stream(content.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        if (lines.isEmpty()) {
            return true;
        }

        // 提取章节标题
        List<String> chapterTitles = extractChapterTitles(content);
        
        // 计算内容行数与章节标题数的比例
        int nonEmptyLines = lines.size();
        int titleCount = chapterTitles.size();
        
        // 改进的判断逻辑
        // 1. 增加判断实际内容长度，如果内容非常长，不可能只是标题列表
        if (content.length() > 1000 && titleCount < 20) {
            logger.info("内容长度超过1000字符且章节数小于20，判定为包含正常内容");
            return false;
        }
        
        // 2. 计算每行平均长度，标题列表的平均行长通常较短
        int totalLength = lines.stream().mapToInt(String::length).sum();
        double avgLineLength = (double) totalLength / nonEmptyLines;
        
        if (avgLineLength > 50) {
            logger.info("平均行长度超过50字符，判定为包含正常内容，平均长度：" + avgLineLength);
            return false;
        }
        
        // 3. 检查是否有明显的段落结构（多行连续文本）
        int consecutiveContentLines = 0;
        int maxConsecutiveLines = 0;
        
        for (String line : lines) {
            if (line.length() > 30 && !chapterTitles.contains(line)) {
                consecutiveContentLines++;
                maxConsecutiveLines = Math.max(maxConsecutiveLines, consecutiveContentLines);
            } else {
                consecutiveContentLines = 0;
            }
        }
        
        if (maxConsecutiveLines > 3) {
            logger.info("检测到连续3行以上的长文本内容，判定为包含正常内容");
            return false;
        }
        
        // 原有判断逻辑
        logger.info("内容分析：总行数=" + nonEmptyLines + ", 章节标题数=" + titleCount + 
                ", 内容行数=" + (nonEmptyLines - titleCount) + 
                ", 长内容行数=" + lines.stream().filter(line -> line.length() > 100).count());
        
        // 如果标题数与行数比例过高，判定为章节标题列表
        boolean isChapterTitleList = titleCount > 0 && (double) titleCount / nonEmptyLines > 0.7;
        
        if (isChapterTitleList) {
            logger.warn("检测到内容可能只是章节标题列表，没有实际章节内容");
        }
        
        return isChapterTitleList;
    }
    
    /**
     * 从章节标题列表中提取章节标题
     */
    private List<String> extractChapterTitles(String content) {
        List<String> titles = new ArrayList<>();
        if (content == null || content.trim().isEmpty()) {
            return titles;
        }
        
        // 分割成行
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 检查是否包含数字+章节标记
            if (line.matches("第[0-9一二三四五六七八九十百千万]+[章节卷回集].*") ||
                line.matches("[0-9]+\\..*") ||
                line.matches(".*第[0-9一二三四五六七八九十百千万]+[章节].*") ||
                line.matches("^\\d+\\s+.*")) {
                
                // 如果行包含✓或✗符号，说明是WebScraper生成的章节状态行，需要清理
                if (line.contains("✓") || line.contains("✗")) {
                    // 去除状态标记
                    line = line.replaceAll("\\s*[✓✗]\\s*$", "");
                    
                    // 去除行首的数字和点
                    if (line.matches("^\\d+\\.\\s+.*")) {
                        line = line.replaceAll("^\\d+\\.\\s+", "");
                    } else if (line.matches("^\\d+\\s+.*")) {
                        line = line.replaceAll("^\\d+\\s+", "");
                    }
                }
                
                titles.add(truncateTitle(line));
            }
        }
        
        // 如果没有找到标准格式的章节标题，尝试使用更宽松的模式
        if (titles.isEmpty() && content.contains("章")) {
            logger.info("未找到标准格式章节标题，尝试使用宽松模式");
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.length() > 50) continue;
                
                // 任何包含"章"且不太长的行
                if (line.contains("章") && !line.contains("系统提示")) {
                    titles.add(truncateTitle(line));
                }
            }
        }
        
        logger.info("从内容中提取到 {} 个章节标题", titles.size());
        return titles;
    }
} 