package com.novelassistant.service.impl;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.mining.word2vec.DocVectorModel;
import com.hankcs.hanlp.mining.word2vec.WordVectorModel;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.summary.TextRankKeyword;
import com.hankcs.hanlp.summary.TextRankSentence;
import com.novelassistant.service.NlpService;
import com.novelassistant.service.MachineLearningService;
import com.novelassistant.util.ApplicationContextProvider;
import com.novelassistant.util.ChineseNameDictionary;
import com.novelassistant.util.RequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 基于HanLP的自然语言处理服务实现
 */
@Service
@Primary
public class HanlpNlpServiceImpl implements NlpService {

    private static final Logger logger = LoggerFactory.getLogger(HanlpNlpServiceImpl.class);
    
    private final MachineLearningService mlService;
    
    @Autowired
    public HanlpNlpServiceImpl(@Lazy MachineLearningService mlService) {
        this.mlService = mlService;
    }

    // 章节标题识别的正则表达式 - 优化版本，支持更多格式
    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
        // 第X章/节/卷 格式，允许更长的标题，支持更多数字表示法
        "第[0-9零一二三四五六七八九十百千万亿]+[章节集卷篇].*?(?:\\n|$)|" +
        // 第X章 标题 格式
        "第[0-9零一二三四五六七八九十百千万亿]+[章节集卷篇]\\s*[^\\n]{0,50}|" +
        // 数字编号格式：1. 标题 或 1、标题
        "^\\s*\\d+[、.．\\s]+[^\\n]{2,50}(?:\\n|$)|" +
        // 特殊标记格式，如【第一章】
        "[【\\[（(][^】\\]）)]*?第[^】\\]）)]*?章[^】\\]）)]*?[】\\]）)].*?(?:\\n|$)|" +
        // 特殊标记词作为章节开始，常见于网文
        "^\\s*(?:序章|引子|楔子|尾声|终章|番外|后记)\\s*.*?(?:\\n|$)"
    );
    
    // 人名词性集合
    private static final Set<String> PERSON_NATURES = new HashSet<>(Arrays.asList("nr", "nrj", "nrf"));
    
    // 情感词典权重
    private static final Map<String, Integer> EMOTION_DICT = new HashMap<>();
    
    static {
        // 初始化情感词典（扩展版）
        // 正面情感词
        EMOTION_DICT.put("喜悦", 2);
        EMOTION_DICT.put("高兴", 2);
        EMOTION_DICT.put("愉快", 2);
        EMOTION_DICT.put("激动", 1);
        EMOTION_DICT.put("兴奋", 2);
        EMOTION_DICT.put("幸福", 3);
        EMOTION_DICT.put("开心", 2);
        EMOTION_DICT.put("欢喜", 2);
        EMOTION_DICT.put("快乐", 2);
        EMOTION_DICT.put("欣慰", 1);
        EMOTION_DICT.put("满意", 1);
        EMOTION_DICT.put("欢乐", 2);
        EMOTION_DICT.put("愉悦", 2);
        EMOTION_DICT.put("欣喜", 2);
        EMOTION_DICT.put("欢欣", 2);
        EMOTION_DICT.put("快活", 2);
        EMOTION_DICT.put("欢畅", 2);
        EMOTION_DICT.put("甜蜜", 2);
        EMOTION_DICT.put("惊喜", 1);
        
        // 负面情感词
        EMOTION_DICT.put("悲伤", -2);
        EMOTION_DICT.put("难过", -2);
        EMOTION_DICT.put("痛苦", -3);
        EMOTION_DICT.put("伤心", -2);
        EMOTION_DICT.put("失望", -1);
        EMOTION_DICT.put("沮丧", -2);
        EMOTION_DICT.put("绝望", -3);
        EMOTION_DICT.put("愤怒", -2);
        EMOTION_DICT.put("生气", -1);
        EMOTION_DICT.put("恼火", -1);
        EMOTION_DICT.put("焦虑", -1);
        EMOTION_DICT.put("恐惧", -2);
        EMOTION_DICT.put("害怕", -2);
        EMOTION_DICT.put("担心", -1);
        EMOTION_DICT.put("苦恼", -1);
        EMOTION_DICT.put("忧愁", -1);
        EMOTION_DICT.put("悲痛", -3);
        EMOTION_DICT.put("哀伤", -2);
        EMOTION_DICT.put("痛心", -2);
        EMOTION_DICT.put("悲哀", -2);
        EMOTION_DICT.put("忧伤", -1);
        EMOTION_DICT.put("烦躁", -1);
        EMOTION_DICT.put("愁苦", -2);
        EMOTION_DICT.put("怨恨", -2);
        EMOTION_DICT.put("仇恨", -3);
        EMOTION_DICT.put("厌恶", -2);
        EMOTION_DICT.put("讨厌", -1);
        EMOTION_DICT.put("憎恨", -3);
        EMOTION_DICT.put("烦恼", -1);
        EMOTION_DICT.put("不快", -1);
        EMOTION_DICT.put("不满", -1);
        EMOTION_DICT.put("怒火", -2);
        EMOTION_DICT.put("不爽", -1);
        EMOTION_DICT.put("愤恨", -2);
        EMOTION_DICT.put("嫉妒", -1);
        EMOTION_DICT.put("羞愧", -1);
        EMOTION_DICT.put("内疚", -1);
        EMOTION_DICT.put("惭愧", -1);
        EMOTION_DICT.put("委屈", -1);
        EMOTION_DICT.put("困惑", -1);
        EMOTION_DICT.put("迷茫", -1);
        
        // 复杂情感词
        EMOTION_DICT.put("感动", 2);
        EMOTION_DICT.put("惊讶", 0);
        EMOTION_DICT.put("惊奇", 0);
        EMOTION_DICT.put("惊骇", -1);
        EMOTION_DICT.put("震惊", -1);
        EMOTION_DICT.put("思念", 1);
        EMOTION_DICT.put("怀念", 1);
        EMOTION_DICT.put("渴望", 1);
        EMOTION_DICT.put("期待", 1);
        EMOTION_DICT.put("希望", 1);
        EMOTION_DICT.put("同情", 0);
        EMOTION_DICT.put("怜悯", 0);
        EMOTION_DICT.put("好奇", 1);
        EMOTION_DICT.put("满足", 1);
        EMOTION_DICT.put("安心", 1);
        EMOTION_DICT.put("放心", 1);
        EMOTION_DICT.put("释然", 1);
        EMOTION_DICT.put("宽慰", 1);
    }

    @Override
    public Map<String, Integer> extractKeywords(String text, int maxKeywords) {
        // 结果关键词和权重映射
        Map<String, Integer> result = new LinkedHashMap<>();
        
        // 1. 基于TextRank获取基本关键词
        List<String> textRankKeywords = new TextRankKeyword().getKeywordList(text, maxKeywords);
        
        // 2. 获取文本中的人名、地名、组织名等命名实体
        List<Term> terms = HanLP.segment(text);
        
        // 直接在这里提取命名实体，而不是调用extractNamedEntities方法
        Map<String, Integer> entityScores = new HashMap<>();
        
        // 命名实体类型及其基础权重
        Map<String, Integer> entityTypeWeights = new HashMap<>();
        entityTypeWeights.put("nr", 80);  // 人名
        entityTypeWeights.put("ns", 70);  // 地名
        entityTypeWeights.put("nt", 60);  // 组织名
        entityTypeWeights.put("nw", 60);  // 作品名
        
        // 统计各实体出现频率
        Map<String, Integer> entityCounts = new HashMap<>();
        
        for (Term term : terms) {
            String nature = term.nature.toString();
            if (entityTypeWeights.containsKey(nature)) {
                String entity = term.word;
                entityCounts.put(entity, entityCounts.getOrDefault(entity, 0) + 1);
            }
        }
        
        // 根据频率和类型计算最终权重
        for (Term term : terms) {
            String nature = term.nature.toString();
            if (entityTypeWeights.containsKey(nature)) {
                String entity = term.word;
                if (entity.length() > 1) {  // 过滤单字实体，减少噪音
                    int typeWeight = entityTypeWeights.get(nature);
                    int count = entityCounts.getOrDefault(entity, 0);
                    
                    // 权重计算：基础类型权重 + 出现频率加成
                    int score = typeWeight + Math.min(20, count * 2);
                    
                    // 更新最高分
                    if (!entityScores.containsKey(entity) || entityScores.get(entity) < score) {
                        entityScores.put(entity, score);
                    }
                }
            }
        }
        
        // 3. 提取标题中的关键词（如果文本第一行是标题）
        Map<String, Integer> titleKeywords = extractTitleKeywords(text);
        
        // 4. 检测并提取重复高频词组
        Map<String, Integer> repeatedPhrases = extractRepeatedPhrases(text);
        
        // 5. 识别情节转折点相关词
        Map<String, Integer> plotKeywords = extractPlotKeywords(text);
        
        // 6. 识别主题相关词汇 (新增)
        Map<String, Integer> themeKeywords = extractThemeKeywords(text);
        
        // 7. 识别情感相关词汇 (新增)
        Map<String, Integer> emotionKeywords = extractEmotionKeywords(text);
        
        // 将TextRank关键词添加到结果中
        for (int i = 0; i < textRankKeywords.size(); i++) {
            String keyword = textRankKeywords.get(i);
            // 根据排名计算权重，排名越靠前权重越高
            int weight = 100 - i * (100 / (maxKeywords + 1));
            result.put(keyword, weight);
        }
        
        // 合并命名实体，增加权重
        mergeKeywordMaps(result, entityScores, maxKeywords);
        
        // 合并标题关键词，但降低权重比例 (从优先级最高调整为适中)
        for (Map.Entry<String, Integer> entry : titleKeywords.entrySet()) {
            String titleKeyword = entry.getKey();
            // 修改标题关键词的权重从90降至75
            int titleWeight = 75;
            
            if (result.containsKey(titleKeyword)) {
                // 如果已存在，增加权重但不要过度倾向标题词
                result.put(titleKeyword, Math.min(100, result.get(titleKeyword) + titleWeight/2));
            } else if (result.size() < maxKeywords) {
                // 如果未满，直接添加
                result.put(titleKeyword, titleWeight);
            } else {
                // 如果已满，替换权重最小的项（如果新项权重更大）
                String minWeightKey = getMinWeightKey(result);
                if (minWeightKey != null && result.get(minWeightKey) < titleWeight) {
                    result.remove(minWeightKey);
                    result.put(titleKeyword, titleWeight);
                }
            }
        }
        
        // 合并重复词组和情节关键词
        mergeKeywordMaps(result, repeatedPhrases, maxKeywords);
        mergeKeywordMaps(result, plotKeywords, maxKeywords);
        
        // 合并主题关键词和情感关键词 (新增)
        mergeKeywordMaps(result, themeKeywords, maxKeywords);
        mergeKeywordMaps(result, emotionKeywords, maxKeywords);
        
        // 对结果按权重排序
        Map<String, Integer> sortedResult = result.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(maxKeywords)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        
        return sortedResult;
    }

    /**
     * 从文本中提取命名实体
     * 
     * @param text 需要分析的文本
     * @param entityType 实体类型，如"PERSON", "LOCATION", "ORGANIZATION"等
     * @return 提取的命名实体列表
     */
    @Override
    public List<String> extractNamedEntities(String text, String entityType) {
        logger.info("提取命名实体, 类型: {}", entityType);
        
        List<Term> terms = HanLP.segment(text);
        Map<String, Integer> entityMap = new HashMap<>();
        
        // 根据实体类型选择对应的词性
        Set<String> targetNatures = new HashSet<>();
        
        switch (entityType.toUpperCase()) {
            case "PERSON":
                targetNatures.addAll(PERSON_NATURES); // nr, nrj, nrf 等
                break;
            case "LOCATION":
                targetNatures.add("ns"); // 地名
                break;
            case "ORGANIZATION":
                targetNatures.add("nt"); // 机构名
                break;
            case "TIME":
                targetNatures.add("t"); // 时间词
                break;
            default:
                targetNatures.addAll(PERSON_NATURES); // 默认提取人名
        }
        
        // 提取匹配的实体并统计出现次数
        for (Term term : terms) {
            String nature = term.nature.toString();
            if (targetNatures.contains(nature)) {
                String entity = term.word;
                // 过滤无效实体（长度过短或数字）
                if (entity.length() < 2 || entity.matches("\\d+")) {
                    continue;
                }
                entityMap.put(entity, entityMap.getOrDefault(entity, 0) + 1);
            }
        }
        
        // 排序并返回频率最高的实体
        return entityMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 从标题中提取关键词
     */
    private Map<String, Integer> extractTitleKeywords(String text) {
        Map<String, Integer> titleKeywords = new HashMap<>();
        
        // 获取第一行作为可能的标题
        String firstLine = text.trim().split("\n")[0];
        
        // 如果第一行很短，可能是标题
        if (firstLine.length() < 50) {
            // 分词
            List<Term> titleTerms = HanLP.segment(firstLine);
            
            // 提取名词、动词等实质性词语
            for (Term term : titleTerms) {
                String nature = term.nature.toString();
                String word = term.word;
                
                // 只保留名词、动词、形容词等有意义的词
                if ((nature.startsWith("n") || nature.startsWith("v") || nature.startsWith("a"))
                        && word.length() > 1) {
                    titleKeywords.put(word, 90);  // 标题词高权重
                }
            }
        }
        
        return titleKeywords;
    }

    /**
     * 提取文本中频繁重复的词组
     */
    private Map<String, Integer> extractRepeatedPhrases(String text) {
        Map<String, Integer> repeatedPhrases = new HashMap<>();
        Map<String, Integer> phraseCounts = new HashMap<>();
        
        // 获取2-3个词的组合
        List<Term> terms = HanLP.segment(text);
        List<String> words = terms.stream().map(term -> term.word).collect(Collectors.toList());
        
        // 统计2-3词组合的出现频率
        for (int i = 0; i < words.size() - 1; i++) {
            // 二元组
            String bigram = words.get(i) + words.get(i + 1);
            phraseCounts.put(bigram, phraseCounts.getOrDefault(bigram, 0) + 1);
            
            // 三元组
            if (i < words.size() - 2) {
                String trigram = words.get(i) + words.get(i + 1) + words.get(i + 2);
                phraseCounts.put(trigram, phraseCounts.getOrDefault(trigram, 0) + 1);
            }
        }
        
        // 筛选高频词组
        for (Map.Entry<String, Integer> entry : phraseCounts.entrySet()) {
            String phrase = entry.getKey();
            int count = entry.getValue();
            
            // 频率阈值根据文本长度动态调整
            int threshold = Math.max(3, text.length() / 5000);
            
            if (count >= threshold && phrase.length() > 2) {
                int score = Math.min(85, 50 + count * 5);  // 根据频率计算权重
                repeatedPhrases.put(phrase, score);
            }
        }
        
        return repeatedPhrases;
    }

    /**
     * 提取与情节转折相关的关键词
     */
    private Map<String, Integer> extractPlotKeywords(String text) {
        Map<String, Integer> plotKeywords = new HashMap<>();
        
        // 情节转折相关词汇
        String[] plotMarkers = {
            "突然", "忽然", "但是", "然而", "不过", "却", "竟然", "居然", "原来", 
            "终于", "最终", "结果", "没想到", "意外", "惊讶", "震惊", "转折", "变化"
        };
        
        // 提取与转折词相关的上下文
        for (String marker : plotMarkers) {
            int pos = 0;
            while ((pos = text.indexOf(marker, pos + 1)) != -1) {
                // 获取转折词周围的上下文（前10个字和后20个字）
                int start = Math.max(0, pos - 10);
                int end = Math.min(text.length(), pos + marker.length() + 20);
                
                String context = text.substring(start, end);
                
                // 从上下文中提取名词
                List<Term> contextTerms = HanLP.segment(context);
                for (Term term : contextTerms) {
                    if (term.nature.toString().startsWith("n") && term.word.length() > 1) {
                        String keyword = term.word;
                        int currentScore = plotKeywords.getOrDefault(keyword, 0);
                        plotKeywords.put(keyword, Math.min(80, currentScore + 15));
                    }
                }
            }
        }
        
        return plotKeywords;
    }

    /**
     * 提取文本中的主题相关词汇
     */
    private Map<String, Integer> extractThemeKeywords(String text) {
        Map<String, Integer> themeKeywords = new HashMap<>();
        
        // 主题分类字典
        Map<String, List<String>> themeDict = new HashMap<>();
        themeDict.put("爱情", Arrays.asList("爱情", "恋爱", "情侣", "相爱", "爱慕", "恋人", "告白", "表白", "婚姻", "求婚"));
        themeDict.put("友情", Arrays.asList("友情", "朋友", "友谊", "伙伴", "交情", "友人", "死党", "同伴"));
        themeDict.put("冒险", Arrays.asList("冒险", "探险", "探索", "旅行", "历险", "征程", "奇遇", "探险家"));
        themeDict.put("成长", Arrays.asList("成长", "成熟", "进步", "蜕变", "成人", "青春", "懂事", "领悟"));
        themeDict.put("奇幻", Arrays.asList("奇幻", "魔法", "魔术", "神奇", "幻想", "超能力", "异能", "法术"));
        themeDict.put("科幻", Arrays.asList("科幻", "未来", "太空", "宇宙", "科技", "外星", "人工智能", "机器人"));
        themeDict.put("悬疑", Arrays.asList("悬疑", "推理", "谜题", "侦探", "案件", "谋杀", "犯罪", "线索"));
        themeDict.put("历史", Arrays.asList("历史", "古代", "朝代", "皇帝", "王朝", "古典", "古风", "传统"));
        themeDict.put("战争", Arrays.asList("战争", "战斗", "征战", "军事", "军队", "作战", "兵法", "战略"));
        themeDict.put("哲学", Arrays.asList("哲学", "思考", "意义", "存在", "真理", "道德", "伦理", "价值"));
        
        // 检测主题词出现频率
        for (Map.Entry<String, List<String>> themeEntry : themeDict.entrySet()) {
            String themeName = themeEntry.getKey();
            List<String> keywords = themeEntry.getValue();
            
            int count = 0;
            for (String keyword : keywords) {
                // 简单计数
                int startIndex = 0;
                while (startIndex != -1) {
                    startIndex = text.indexOf(keyword, startIndex);
                    if (startIndex != -1) {
                        count++;
                        startIndex += keyword.length();
                    }
                }
            }
            
            // 如果主题词频繁出现，添加到结果中
            if (count > 5) {  // 设置阈值
                themeKeywords.put(themeName, 70 + Math.min(20, count / 5));  // 基础分70，每5次出现加1分，最多加20分
            }
        }
        
        return themeKeywords;
    }
    
    /**
     * 提取文本中的情感相关词汇
     */
    private Map<String, Integer> extractEmotionKeywords(String text) {
        Map<String, Integer> emotionKeywords = new HashMap<>();
        
        // 情感分类字典
        Map<String, List<String>> emotionDict = new HashMap<>();
        emotionDict.put("欢乐", Arrays.asList("欢乐", "快乐", "喜悦", "高兴", "开心", "欣喜", "兴奋", "愉快"));
        emotionDict.put("悲伤", Arrays.asList("悲伤", "伤心", "难过", "痛苦", "哀伤", "忧伤", "哭泣", "流泪"));
        emotionDict.put("愤怒", Arrays.asList("愤怒", "愤恨", "气愤", "暴怒", "发火", "恼怒", "生气", "怒火"));
        emotionDict.put("恐惧", Arrays.asList("恐惧", "害怕", "惊恐", "恐慌", "畏惧", "惊吓", "惧怕", "胆怯"));
        emotionDict.put("惊喜", Arrays.asList("惊喜", "意外", "惊讶", "吃惊", "震惊", "诧异", "意想不到", "惊奇"));
        emotionDict.put("感动", Arrays.asList("感动", "感慨", "感恩", "感激", "温情", "暖心", "催泪", "触动"));
        
        // 检测情感词出现频率
        for (Map.Entry<String, List<String>> emotionEntry : emotionDict.entrySet()) {
            String emotionName = emotionEntry.getKey();
            List<String> keywords = emotionEntry.getValue();
            
            int count = 0;
            for (String keyword : keywords) {
                // 简单计数
                int startIndex = 0;
                while (startIndex != -1) {
                    startIndex = text.indexOf(keyword, startIndex);
                    if (startIndex != -1) {
                        count++;
                        startIndex += keyword.length();
                    }
                }
            }
            
            // 如果情感词频繁出现，添加到结果中
            if (count > 10) {  // 设置阈值
                emotionKeywords.put(emotionName, 65 + Math.min(25, count / 5));  // 基础分65，每5次出现加1分，最多加25分
            }
        }
        
        return emotionKeywords;
    }

    /**
     * 将源关键词映射合并到目标映射
     */
    private void mergeKeywordMaps(Map<String, Integer> target, Map<String, Integer> source, int maxKeywords) {
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            String keyword = entry.getKey();
            int weight = entry.getValue();
            
            if (target.containsKey(keyword)) {
                // 如果已存在，增加权重
                target.put(keyword, Math.min(100, target.get(keyword) + weight / 2));
            } else if (target.size() < maxKeywords) {
                // 如果未满，直接添加
                target.put(keyword, weight);
            } else {
                // 如果已满，替换权重最小的项（如果新项权重更大）
                String minWeightKey = getMinWeightKey(target);
                if (minWeightKey != null && target.get(minWeightKey) < weight) {
                    target.remove(minWeightKey);
                    target.put(keyword, weight);
                }
            }
        }
    }

    /**
     * 获取权重最小的关键词
     */
    private String getMinWeightKey(Map<String, Integer> keywords) {
        if (keywords.isEmpty()) {
            return null;
        }
        
        return keywords.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public String generateSummary(String text, int maxLength) {
        // 预处理文本，清除特殊标记
        text = cleanTextForSummary(text);
        
        // 如果文本过短，直接返回
        if (text.length() <= maxLength) {
            return text.length() <= 100 ? text : text.substring(0, 100) + "...";
        }
        
        // 提取关键要素
        List<String> characters = extractCharacters(text);
        Map<String, Integer> keywords = extractKeywords(text, 8);
        double sentiment = analyzeSentiment(text);
        
        // 使用TextRank提取关键句子
        List<String> sentenceList = HanLP.extractSummary(text, Math.max(3, maxLength / 50));
        
        // 确保摘要包含主要人物
        boolean hasMainCharacter = false;
        if (!characters.isEmpty()) {
            for (String sentence : sentenceList) {
                if (sentence.contains(characters.get(0))) {
                    hasMainCharacter = true;
                    break;
                }
            }
            
            // 如果没有包含主角，尝试添加一个包含主角的句子
            if (!hasMainCharacter) {
                List<String> allSentences = splitIntoSentences(text);
                for (String sentence : allSentences) {
                    if (sentence.contains(characters.get(0))) {
                        sentenceList.add(sentence);
                        break;
                    }
                }
            }
        }
        
        // 识别章节类型
        String chapterType = identifyChapterType(text, characters);
        
        // 构建结构化摘要
        StringBuilder summary = new StringBuilder();
        
        // 添加章节类型标识
        if (chapterType != null) {
            summary.append("【").append(chapterType).append("】");
        }
        
        // 添加关键句子，确保它们连贯
        String sentenceSummary = String.join("。", sentenceList);
        if (!sentenceSummary.endsWith("。")) {
            sentenceSummary += "。";
        }
        summary.append(sentenceSummary);
        
        // 确保不超过最大长度的80%，留出空间给其他元素
        int maxSentenceLength = (int)(maxLength * 0.8);
        if (summary.length() > maxSentenceLength) {
            summary.setLength(maxSentenceLength);
            // 确保不切断句子中间
            int lastPeriod = summary.lastIndexOf("。");
            if (lastPeriod > maxSentenceLength / 2) {
                summary.setLength(lastPeriod + 1);
            }
        }
        
        // 添加情感基调
        if (sentiment > 0.7) {
            summary.append(" 本章氛围积极向上。");
        } else if (sentiment < 0.3) {
            summary.append(" 本章氛围紧张压抑。");
        }
        
        // 添加主要人物 (如果有的话)
        if (characters.size() > 0) {
            summary.append(" 主要角色：").append(String.join("、", 
                characters.subList(0, Math.min(3, characters.size()))));
        }
        
        // 添加关键词 (如果有足够空间)
        if (summary.length() + 10 < maxLength && !keywords.isEmpty()) {
            summary.append(" 关键词：");
            int count = 0;
            for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
                if (count > 0) summary.append("、");
                summary.append(entry.getKey());
                count++;
                if (count >= 3 || summary.length() >= maxLength - 3) break;
            }
        }
        
        // 确保不超过最大长度
        if (summary.length() > maxLength) {
            return summary.substring(0, maxLength - 3) + "...";
        }
        
        return summary.toString();
    }
    
    /**
     * 清理文本，移除特殊标记，为摘要生成做准备
     */
    private String cleanTextForSummary(String text) {
        if (text == null) return "";
        
        // 移除特殊标记如【群像】【插图】等
        text = text.replaceAll("【[^】]*?】|\\[[^\\]]*?\\]", "");
        
        // 移除其他可能影响摘要质量的特殊字符
        text = text.replaceAll("<[^>]*>", ""); // HTML标签
        text = text.replaceAll("\\{[^}]*\\}", ""); // 花括号内容
        
        return text;
    }
    
    /**
     * 识别章节类型
     * @param text 章节文本
     * @param characters 识别出的人物列表
     * @return 章节类型描述
     */
    private String identifyChapterType(String text, List<String> characters) {
        // 计算对话比例
        int dialogueCount = 0;
        Pattern dialoguePattern = Pattern.compile("[\"\"](.*?)[\"\"]|['']([^'']*)['']");
        Matcher dialogueMatcher = dialoguePattern.matcher(text);
        while (dialogueMatcher.find()) {
            dialogueCount++;
        }
        
        double dialogueRatio = (double) dialogueCount / (text.length() / 100);
        
        // 检测战斗/动作场景
        String[] actionWords = {"战", "打", "杀", "击", "冲", "跑", "跳", "逃", "突", "爆", "炸", "伤", "血", "死"};
        int actionCount = 0;
        for (String word : actionWords) {
            int count = text.split(word).length - 1;
            actionCount += count;
        }
        
        double actionRatio = (double) actionCount / (text.length() / 100);
        
        // 检测情感描写比例
        Set<String> emotionWords = EMOTION_DICT.keySet();
        int emotionCount = 0;
        for (String emotion : emotionWords) {
            int count = text.split(emotion).length - 1;
            emotionCount += count;
        }
        
        double emotionRatio = (double) emotionCount / (text.length() / 100);
        
        // 判断章节类型
        if (dialogueRatio > 1.0) {
            return "对话";
        } else if (actionRatio > 0.8) {
            return "动作";
        } else if (emotionRatio > 0.5) {
            return "情感";
        } else if (characters.size() > 3) {
            return "群像";
        } else if (text.length() < 1000) {
            return "短章";
        }
        
        return "叙述"; // 默认类型
    }

    // HanLP分词方法，仅供内部使用
    private List<String> segment(String text) {
        List<Term> termList = HanLP.segment(text);
        return termList.stream()
                .map(term -> term.word)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> extractCharacters(String text) {
        List<Term> termList = HanLP.segment(text);
        Map<String, Integer> characterCounts = new HashMap<>();
        
        // 获取词典工具实例
        ChineseNameDictionary nameDictionary = ApplicationContextProvider.getBean(ChineseNameDictionary.class);
        
        // 1. 从分词结果中识别人名
        for (Term term : termList) {
            // 检查是否是人名词性
            if (isPerson(term.nature.toString())) {
                String name = term.word;
                characterCounts.put(name, characterCounts.getOrDefault(name, 0) + 1);
            }
            // 使用词典进行额外的人名识别 - 降低阈值从0.6到0.4
            else if (term.word.length() >= 1 && term.word.length() <= 4) {
                double nameLikelihood = nameDictionary.evaluateNameLikelihood(term.word);
                if (nameLikelihood > 0.4) {  // 降低阈值以增加召回率
                    characterCounts.put(term.word, characterCounts.getOrDefault(term.word, 0) + 1);
                }
            }
        }
        
        // 2. 从对话中提取可能的人物名称 - 增强对话提取
        List<Map<String, String>> dialogues = extractDialogues(text);
        for (Map<String, String> dialogue : dialogues) {
            String speaker = dialogue.get("speaker");
            String content = dialogue.get("content");
            
            if (speaker != null && !speaker.equals("未知人物") && speaker.length() <= 5) {
                characterCounts.put(speaker, characterCounts.getOrDefault(speaker, 0) + 3);  // 增加对话中说话者权重
            }
            
            // 分析对话内容中的称呼词
            if (content != null && !content.isEmpty()) {
                // 称呼词模式: "XX(称呼词)+敬语词+逗号"，如"师父大人，"、"大哥，"
                Pattern addressPattern = Pattern.compile("([^，。？！\\\"\\\"]+)(大人|先生|小姐|师父|师傅|师兄|师姐|师弟|师妹|大哥|大姐|二哥|二姐|老哥|兄弟|姐姐|妹妹|叔叔|阿姨|爷爷|奶奶|爸爸|妈妈)[，,]");
                Matcher addressMatcher = addressPattern.matcher(content);
                while (addressMatcher.find()) {
                    String possibleName = addressMatcher.group(1).trim();
                    if (possibleName.length() >= 1 && possibleName.length() <= 4) {
                        characterCounts.put(possibleName, characterCounts.getOrDefault(possibleName, 0) + 2);
                    }
                }
            }
        }
        
        // 3. 识别称呼语 - 扩展称呼语范围
        Pattern titlePattern = Pattern.compile("([老小][爷子]|大人|[师父]|[爸妈][爸妈]|[叔伯舅姨姑][父母]|[哥姐弟妹]|[王帝将军]|殿下|公子|大师|教授|医生|队长|船长|先生|小姐|法师|道长|掌门|门主|会长|帮主|城主|族长|老板|老爷|夫人|大夫|上校|队员|小主|寨主|少爷|姑娘|大王|师伯|师叔)");
        Matcher matcher = titlePattern.matcher(text);
        while (matcher.find()) {
            String title = matcher.group();
            characterCounts.put(title, characterCounts.getOrDefault(title, 0) + 1);
        }
        
        // 4. 检测重复出现在段落开头的名称
        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.length() > 0) {
                // 获取段落开头的1-5个字符，可能是人名
                int endIndex = Math.min(5, paragraph.length());
                int firstPunct = paragraph.indexOf('，');
                if (firstPunct > 0 && firstPunct < endIndex) {
                    endIndex = firstPunct;
                }
                
                String possibleName = paragraph.substring(0, endIndex).trim();
                // 放宽条件，允许单字名
                if (possibleName.length() >= 1 && !possibleName.contains("第") && !possibleName.matches(".*[0-9]+.*")) {
                    // 使用词典评估名字可能性
                    double nameLikelihood = nameDictionary.evaluateNameLikelihood(possibleName);
                    if (nameLikelihood > 0.3) {  // 降低阈值以增加召回率
                        characterCounts.put(possibleName, characterCounts.getOrDefault(possibleName, 0) + 1);
                    }
                }
            }
        }
        
        // 5. 检查小说特有人名词典
        try {
            Long novelId = getCurrentNovelId();
            if (novelId != null) {
                Set<String> specificNames = nameDictionary.getNovelSpecificNames(novelId);
                for (String name : specificNames) {
                    // 特有人名词典中的名字给予更高权重
                    characterCounts.put(name, characterCounts.getOrDefault(name, 0) + 3);
                }
            }
        } catch (Exception e) {
            logger.warn("获取小说特有人名词典失败", e);
        }
        
        // 6. 提取高频词作为可能的人名 (新增)
        Map<String, Integer> wordFrequency = new HashMap<>();
        for (Term term : termList) {
            if (term.word.length() >= 1 && term.word.length() <= 3) {
                String word = term.word;
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        
        // 找出高频词并检查是否可能是人名
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            if (entry.getValue() > 10) { // 出现10次以上的词
                String word = entry.getKey();
                double nameLikelihood = nameDictionary.evaluateNameLikelihood(word);
                if (nameLikelihood > 0.2) { // 非常宽松的阈值
                    characterCounts.put(word, characterCounts.getOrDefault(word, 0) + 1);
                }
            }
        }
        
        // 7. 识别职业称谓作为角色 (新增)
        Pattern professionPattern = Pattern.compile("([^，。？！\\\"\\\"]{1,2})(侠|客|贼|匪|商|警|卒|兵|师|僧|道|医|农|工|商|隐|盗|贼|娼|仙|魔|妖|怪|鬼|龙|狐|虎|蛇|猫|犬)");
        Matcher profMatcher = professionPattern.matcher(text);
        while (profMatcher.find()) {
            String profession = profMatcher.group();
            if (profession.length() <= 3) {
                characterCounts.put(profession, characterCounts.getOrDefault(profession, 0) + 1);
            }
        }
        
        // 过滤出现次数大于等于1的人名
        List<String> result = characterCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= 1)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 保存识别到的人物到小说特有人名词典
        try {
            Long novelId = getCurrentNovelId();
            if (novelId != null) {
                // 只保存排名靠前的人物
                int topCharacterCount = Math.min(30, result.size()); // 增加保存数量
                Set<String> topCharacters = new HashSet<>(result.subList(0, topCharacterCount));
                nameDictionary.addNovelSpecificNames(novelId, topCharacters);
            }
        } catch (Exception e) {
            logger.debug("保存人物到小说特有人名词典失败", e);
        }
        
        // 确保至少返回一些角色（即使是推测的）
        if (result.isEmpty()) {
            logger.warn("未能识别出任何角色，添加默认角色");
            result.add("主角");
            result.add("配角");
        }
        
        logger.info("从文本中提取到 {} 个人物角色", result.size());
        return result;
    }
    
    /**
     * 获取当前正在处理的小说ID
     * 由于可能在不同上下文中调用，可能需要从ThreadLocal或请求上下文中获取
     */
    private Long getCurrentNovelId() {
        try {
            // 尝试从Spring上下文或ThreadLocal中获取当前小说ID
            // 这里使用简化的方法，实际实现可能需要根据项目结构调整
            return RequestContextHolder.getCurrentNovelId();
        } catch (Exception e) {
            logger.debug("获取当前小说ID失败", e);
            return null;
        }
    }

    /**
     * 判断词性是否为人名
     */
    private boolean isPerson(String nature) {
        return PERSON_NATURES.contains(nature);
    }

    @Override
    public List<Integer> detectChapterBreaks(String text) {
        List<Integer> chapterPositions = new ArrayList<>();
        
        // 1. 使用正则表达式查找明确的章节标题
        Matcher matcher = CHAPTER_PATTERN.matcher(text);
        while (matcher.find()) {
            chapterPositions.add(matcher.start());
        }
        
        // 如果通过标题标记找到足够的章节，直接返回
        if (chapterPositions.size() > 5) {
        return chapterPositions;
        }

        // 2. 尝试识别未标记但内容上有明显转换的章节
        List<String> paragraphs = splitIntoParagraphs(text);
        if (paragraphs.size() > 0) {
            // 段落间的语义相似度分析
            double[] similarityScores = calculateParagraphSimilarities(paragraphs);
            
            // 设置相似度阈值，低于此值视为可能的章节分隔点
            double threshold = calculateAdaptiveThreshold(similarityScores);
            
            int currentPos = 0;
            for (int i = 0; i < similarityScores.length; i++) {
                // 当前段落结束位置
                currentPos += paragraphs.get(i).length();
                
                if (similarityScores[i] < threshold) {
                    // 找到段落内容的开始位置
                    int breakPos = text.indexOf(paragraphs.get(i+1), currentPos);
                    if (breakPos > 0 && !isNearExistingBreak(breakPos, chapterPositions)) {
                        chapterPositions.add(breakPos);
                    }
                }
                
                // 考虑段落间的换行符和空格
                currentPos += 1;  // 假设每个段落后有一个换行符
            }
        }
        
        // 3. 如果仍然没有足够的章节分隔，使用基于时间、地点和人物变化的分析
        if (chapterPositions.size() < 3) {
            // 分析主要场景、时间和主要人物的变化点
            List<Integer> sceneChangePositions = detectMajorSceneChanges(text);
            
            // 合并到章节分隔点
            for (int pos : sceneChangePositions) {
                if (!isNearExistingBreak(pos, chapterPositions)) {
                    chapterPositions.add(pos);
                }
            }
        }
        
        // 排序章节位置
        Collections.sort(chapterPositions);
        
        return chapterPositions;
    }

    /**
     * 将文本分割成段落
     */
    private List<String> splitIntoParagraphs(String text) {
        String[] paragraphArray = text.split("\n\\s*\n");
        return Arrays.stream(paragraphArray)
                .filter(p -> p.trim().length() > 50)  // 过滤掉太短的段落
                .collect(Collectors.toList());
    }

    /**
     * 计算相邻段落之间的语义相似度
     * 返回每对相邻段落的相似度分数数组
     */
    private double[] calculateParagraphSimilarities(List<String> paragraphs) {
        double[] similarities = new double[paragraphs.size() - 1];
        
        for (int i = 0; i < paragraphs.size() - 1; i++) {
            // 使用TextRank提取每个段落的核心关键词
            Set<String> keywords1 = new HashSet<>(HanLP.extractKeyword(paragraphs.get(i), 10));
            Set<String> keywords2 = new HashSet<>(HanLP.extractKeyword(paragraphs.get(i + 1), 10));
            
            // 计算Jaccard相似度
            Set<String> union = new HashSet<>(keywords1);
            union.addAll(keywords2);
            
            Set<String> intersection = new HashSet<>(keywords1);
            intersection.retainAll(keywords2);
            
            similarities[i] = union.isEmpty() ? 1.0 : (double) intersection.size() / union.size();
            
            // 考虑人物、时间和场景的连续性
            double characterContinuity = calculateCharacterContinuity(paragraphs.get(i), paragraphs.get(i + 1));
            double timeContinuity = calculateTimeContinuity(paragraphs.get(i), paragraphs.get(i + 1));
            double sceneContinuity = calculateSceneContinuity(paragraphs.get(i), paragraphs.get(i + 1));
            
            // 综合评分
            similarities[i] = similarities[i] * 0.4 + characterContinuity * 0.3 + timeContinuity * 0.15 + sceneContinuity * 0.15;
        }
        
        return similarities;
    }

    /**
     * 计算自适应阈值，用于判断章节分隔点
     */
    private double calculateAdaptiveThreshold(double[] similarities) {
        // 计算所有相似度分数的平均值和标准差
        double sum = 0;
        for (double score : similarities) {
            sum += score;
        }
        double mean = sum / similarities.length;
        
        double variance = 0;
        for (double score : similarities) {
            variance += Math.pow(score - mean, 2);
        }
        double stdDev = Math.sqrt(variance / similarities.length);
        
        // 阈值设为平均值减去标准差的一半
        return Math.max(0.2, mean - (stdDev * 0.5));
    }

    /**
     * 计算两个段落之间人物的连续性
     */
    private double calculateCharacterContinuity(String para1, String para2) {
        List<String> characters1 = extractCharacters(para1);
        List<String> characters2 = extractCharacters(para2);
        
        if (characters1.isEmpty() || characters2.isEmpty()) {
            return 0.8;  // 没有明确人物，假设中等连续性
        }
        
        // 计算共同人物的比例
        Set<String> commonChars = new HashSet<>(characters1);
        commonChars.retainAll(characters2);
        
        int total = Math.max(characters1.size(), characters2.size());
        return total > 0 ? (double) commonChars.size() / total : 0.8;
    }

    /**
     * 计算两个段落之间时间的连续性
     */
    private double calculateTimeContinuity(String para1, String para2) {
        // 检测时间表达，如"第二天"，"三年后"，"次日"等
        Pattern timePattern = Pattern.compile("([昨今明]天|[前后]天|次日|[一二三四五六七八九十]+(天|小时|分钟|秒钟|年|月|星期|周)([前后]|以[前后])|([过了]|之后)|初[一二三四五六七八九十]|[一二三四五六七八九十]+(点|时))");
        
        Matcher matcher = timePattern.matcher(para2);
        if (matcher.find()) {
            // 找到时间跳跃表达，降低连续性
            return 0.5;
        }
        
        return 0.9;  // 默认较高连续性
    }

    /**
     * 计算两个段落之间场景的连续性
     */
    private double calculateSceneContinuity(String para1, String para2) {
        // 场景相关词汇，包括位置、环境描述等
        Pattern scenePattern = Pattern.compile("(在|到达|来到|回到|离开|进入|出去|出来)(了)?(这|那|[一二三四五六七八九十])?(个)?(地方|城市|村庄|房间|屋子|大厅|花园|街道|小镇|学校|医院|家|树林|草原|沙漠|海边|山上|河边|湖边)");
        
        Matcher matcher = scenePattern.matcher(para2);
        if (matcher.find()) {
            // 找到场景转换表达，降低连续性
            return 0.5;
        }
        
        return 0.9;  // 默认较高连续性
    }

    /**
     * 检测文本中的主要场景变化
     */
    private List<Integer> detectMajorSceneChanges(String text) {
        List<Integer> changePositions = new ArrayList<>();
        
        // 场景转换标记词
        String[] sceneMarkers = {"回到", "来到", "进入", "离开", "到达", "转眼", "与此同时", "另一边", "另一方面", "同一时间"};
        
        // 时间跳跃标记词
        String[] timeMarkers = {"第二天", "次日", "几天后", "一周后", "一个月后", "几年后", "多年以后", "转眼之间", "时光飞逝"};
        
        // 搜索这些标记词的位置
        for (String marker : sceneMarkers) {
            int pos = 0;
            while ((pos = text.indexOf(marker, pos + 1)) != -1) {
                // 找到段落的起始位置
                int paragraphStart = findParagraphStart(text, pos);
                if (paragraphStart >= 0) {
                    changePositions.add(paragraphStart);
                }
            }
        }
        
        for (String marker : timeMarkers) {
            int pos = 0;
            while ((pos = text.indexOf(marker, pos + 1)) != -1) {
                // 找到段落的起始位置
                int paragraphStart = findParagraphStart(text, pos);
                if (paragraphStart >= 0) {
                    changePositions.add(paragraphStart);
                }
            }
        }
        
        return changePositions;
    }

    /**
     * 找到包含指定位置的段落的起始位置
     */
    private int findParagraphStart(String text, int position) {
        int start = text.lastIndexOf("\n\n", position);
        if (start == -1) {
            start = text.lastIndexOf("\n", position);
        }
        return start == -1 ? 0 : start + 1;
    }

    /**
     * 检查一个位置是否与现有的任何章节分隔点过近
     */
    private boolean isNearExistingBreak(int position, List<Integer> existingBreaks) {
        final int MINIMUM_DISTANCE = 1000;  // 设置最小距离为1000字符
        
        for (int existingPos : existingBreaks) {
            if (Math.abs(position - existingPos) < MINIMUM_DISTANCE) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Map<String, Double> extractTopics(String text, int count) {
        // 简化实现：直接使用关键词作为主题
        Map<String, Integer> keywords = extractKeywords(text, count);
        Map<String, Double> topics = new LinkedHashMap<>();
        
        for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
            topics.put(entry.getKey(), entry.getValue() / 100.0);
        }
        
        return topics;
    }
    
    @Override
    public double analyzeSentiment(String text) {
        // 结果初始化为中性情感
        double baseScore = 0.5;
        
        // 情感词及其修饰词的位置跟踪
        List<Term> termList = HanLP.segment(text);
        Map<Integer, Double> sentimentMap = new HashMap<>(); // 位置 -> 情感值
        Map<Integer, Integer> degreeMap = new HashMap<>();   // 位置 -> 程度值
        Map<Integer, Boolean> negationMap = new HashMap<>(); // 位置 -> 是否被否定
        
        // 1. 识别情感词并初始赋值
        for (int i = 0; i < termList.size(); i++) {
            Term term = termList.get(i);
            String word = term.word;
            
            // 情感词识别
            if (EMOTION_DICT.containsKey(word)) {
                int score = EMOTION_DICT.get(word);
                // 归一化到0-1范围
                double normalizedScore = (score + 3) / 6.0; // 从[-3,3]映射到[0,1]
                sentimentMap.put(i, normalizedScore);
            }
        }
        
        // 2. 识别程度词（很、非常、极其等）
        for (int i = 0; i < termList.size(); i++) {
            Term term = termList.get(i);
            String word = term.word;
            
            int degreeValue = getDegreeValue(word);
            if (degreeValue > 0) {
                degreeMap.put(i, degreeValue);
            }
        }
        
        // 3. 识别否定词（不、没有、无等）
        for (int i = 0; i < termList.size(); i++) {
            Term term = termList.get(i);
            String word = term.word;
            
            if (isNegationWord(word)) {
                negationMap.put(i, true);
            }
        }
        
        // 4. 应用程度词和否定词对情感词的修饰
        double totalScore = 0.0;
        int effectiveTerms = 0;
        
        for (Map.Entry<Integer, Double> entry : sentimentMap.entrySet()) {
            int position = entry.getKey();
            double score = entry.getValue();
            
            // 寻找前后5个词位内的修饰词
            int minPos = Math.max(0, position - 5);
            int maxPos = Math.min(termList.size() - 1, position + 2);
            
            // 应用程度修饰
            for (int i = minPos; i <= maxPos; i++) {
                if (degreeMap.containsKey(i)) {
                    int degreeValue = degreeMap.get(i);
                    // 根据程度值调整情感强度
                    if (score > 0.5) {  // 正面情感
                        score = Math.min(1.0, score + (degreeValue / 10.0));
                    } else if (score < 0.5) {  // 负面情感
                        score = Math.max(0.0, score - (degreeValue / 10.0));
                    }
                }
            }
            
            // 应用否定修饰
            boolean isNegated = false;
            for (int i = minPos; i <= position; i++) {
                if (negationMap.containsKey(i)) {
                    isNegated = !isNegated;  // 双重否定变肯定
                }
            }
            
            if (isNegated) {
                // 情感反转
                score = 1.0 - score;
            }
            
            totalScore += score;
            effectiveTerms++;
        }
        
        // 5. 计算整体情感值
        if (effectiveTerms > 0) {
            return totalScore / effectiveTerms;
        }
        
        // 6. 如果没有显式情感词，尝试通过其他特征推断
        return inferSentimentFromContext(text, termList);
    }

    /**
     * 获取程度副词的强度值
     * @param word 词语
     * @return 程度值（0表示不是程度词）
     */
    private int getDegreeValue(String word) {
        // 程度副词分级
        Map<String, Integer> degreeDict = new HashMap<>();
        // 极度强烈程度：9
        degreeDict.put("极其", 9);
        degreeDict.put("极度", 9);
        degreeDict.put("极端", 9);
        degreeDict.put("极为", 9);
        degreeDict.put("极", 9);
        degreeDict.put("最为", 9);
        // 强烈程度：7
        degreeDict.put("非常", 7);
        degreeDict.put("十分", 7);
        degreeDict.put("分外", 7);
        degreeDict.put("格外", 7);
        degreeDict.put("特别", 7);
        degreeDict.put("特殊", 7);
        // 较强程度：5
        degreeDict.put("很", 5);
        degreeDict.put("太", 5);
        degreeDict.put("尤其", 5);
        degreeDict.put("更加", 5);
        degreeDict.put("更为", 5);
        degreeDict.put("颇为", 5);
        // 一般程度：3
        degreeDict.put("比较", 3);
        degreeDict.put("较为", 3);
        degreeDict.put("还", 3);
        degreeDict.put("稍微", 3);
        degreeDict.put("略微", 3);
        // 弱程度：1
        degreeDict.put("稍", 1);
        degreeDict.put("稍稍", 1);
        degreeDict.put("稍许", 1);
        degreeDict.put("些许", 1);
        degreeDict.put("有点", 1);
        degreeDict.put("有些", 1);
        
        return degreeDict.getOrDefault(word, 0);
    }

    /**
     * 判断是否是否定词
     */
    private boolean isNegationWord(String word) {
        String[] negations = {"不", "没", "无", "非", "莫", "弗", "毋", "勿", "未", "否", "别", "无须", "并非", "毫无"};
        for (String neg : negations) {
            if (word.equals(neg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当没有明确情感词时，尝试从上下文推断情感
     */
    private double inferSentimentFromContext(String text, List<Term> termList) {
        // 默认中性
        double contextScore = 0.5;
        
        // 1. 从标点符号判断
        int exclamationCount = 0;
        int questionCount = 0;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '!' || c == '！') {
                exclamationCount++;
            } else if (c == '?' || c == '？') {
                questionCount++;
            }
        }
        
        // 感叹号多可能表示强烈情感
        if (exclamationCount > 2) {
            contextScore += 0.1;
        }
        
        // 2. 从情景词判断
        Map<String, Double> sceneWords = new HashMap<>();
        sceneWords.put("笑", 0.7);
        sceneWords.put("哭", 0.3);
        sceneWords.put("喜悦", 0.8);
        sceneWords.put("悲伤", 0.2);
        sceneWords.put("快乐", 0.8);
        sceneWords.put("高兴", 0.7);
        sceneWords.put("痛苦", 0.2);
        sceneWords.put("烦恼", 0.3);
        sceneWords.put("开心", 0.8);
        
        for (Term term : termList) {
            String word = term.word;
            if (sceneWords.containsKey(word)) {
                contextScore = sceneWords.get(word);
                break;
            }
        }
        
        // 3. 分析语气词
        String[] positiveModalWords = {"啊", "哇", "哈", "嘻", "呀", "耶"};
        String[] negativeModalWords = {"唉", "哎", "呃", "嗯", "呜", "哦"};
        
        for (String word : positiveModalWords) {
            if (text.contains(word)) {
                contextScore += 0.05;
                break;
            }
        }
        
        for (String word : negativeModalWords) {
            if (text.contains(word)) {
                contextScore -= 0.05;
                break;
            }
        }
        
        // 确保分数在[0,1]范围内
        return Math.max(0, Math.min(1, contextScore));
    }
    
    /**
     * 将文本分割为句子
     */
    private List<String> splitIntoSentences(String text) {
        String[] sentenceArray = text.split("[。！？.!?]");
        return Arrays.asList(sentenceArray);
    }
    
    /**
     * 从文本中提取对话，用于人物关系识别
     * 支持中文引号和英文引号
     * @param text 需要分析的文本
     * @return 对话列表，每项包含说话者和内容
     */
    public List<Map<String, String>> extractDialogues(String text) {
        List<Map<String, String>> dialogues = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return dialogues;
        }
        
        try {
            // 模式1: 角色名+冒号+引号内容 (支持中英文引号和冒号)
            Pattern pattern1 = Pattern.compile("([^，。：:\"'！？\\s\\n]{1,15})[：:][\"]([^\"]*)[\"]", Pattern.DOTALL);
            Pattern pattern1b = Pattern.compile("([^，。：:\"'！？\\s\\n]{1,15})[：:][\u201c]([^\u201d]*)[\u201d]", Pattern.DOTALL);
            
            // 模式2: 角色名+说/道/问+冒号+引号内容
            Pattern pattern2 = Pattern.compile("([^，。：:\"'！？\\s\\n]{1,15})[说道问答喊叫][：:][\"]([^\"]*)[\"]", Pattern.DOTALL);
            Pattern pattern2b = Pattern.compile("([^，。：:\"'！？\\s\\n]{1,15})[说道问答喊叫][：:][\u201c]([^\u201d]*)[\u201d]", Pattern.DOTALL);
            
            // 模式3: 引号内容+角色名+说/道
            Pattern pattern3 = Pattern.compile("[\"]([^\"]*)[\"][\\s]*[，。！？,.。]([^，。：:\"'！？\\s\\n]{1,15})[说道问答喊叫]", Pattern.DOTALL);
            Pattern pattern3b = Pattern.compile("[\u201c]([^\u201d]*)[\u201d][\\s]*[，。！？,.。]([^，。：:\"'！？\\s\\n]{1,15})[说道问答喊叫]", Pattern.DOTALL);
            
            // 模式4: 直接匹配常见对话格式 ""xxxx" 他说道"
            Pattern pattern4 = Pattern.compile("[\u201c]([^\u201d]{2,100})[\u201d][，。？！,.!?]?\\s*([^，。：;！？\\s\\n]{1,10})(?:说|道|问|答|嘀咕|喊|叫|笑)", Pattern.DOTALL);
            
            // 模式5: 引号对话 (不确定说话者，但至少提取对话内容)
            Pattern pattern5 = Pattern.compile("[\u201c]([^\u201d]{2,100})[\u201d]", Pattern.DOTALL);
            
            // 模式6: 角色+动词+说话内容 (新增)
            Pattern pattern6 = Pattern.compile("([^，。：:\"'！？\\s\\n]{1,10})(?:对着|朝着|冲着|转向|看着|盯着)([^，。：:\"'！？\\s\\n]{1,10})(?:说|道|问|喊|叫)[：:，,][\u201c]([^\u201d]*)[\u201d]", Pattern.DOTALL);
            
            // 模式7: 说话者敬语形式 (新增) - 如"xxx大人说道"
            Pattern pattern7 = Pattern.compile("([^，。：:\"'！？\\s\\n]{1,5})(?:大人|先生|小姐|公子|师父|师傅|师兄|师姐|师弟|师妹|大哥|大姐|爷爷|奶奶|叔叔|阿姨)(?:说|道|问|答|喊|叫)[：:，,]?[\u201c]([^\u201d]*)[\u201d]", Pattern.DOTALL);
            
            // 模式8: 人物+特殊动词+引号内容 (新增)
            Pattern pattern8 = Pattern.compile("([^，。：:\"'！？\\s\\n]{1,10})(?:点头|摇头|思考|沉默|微笑|叹气|皱眉|惊讶|大笑|冷笑)[了]?[一下子]?[，：:,][\u201c]([^\u201d]*)[\u201d]", Pattern.DOTALL);
            
            // 处理模式1
            processMatcher(pattern1, text, dialogues, 1, 2);
            processMatcher(pattern1b, text, dialogues, 1, 2);
            
            // 处理模式2
            processMatcher(pattern2, text, dialogues, 1, 2);
            processMatcher(pattern2b, text, dialogues, 1, 2);
            
            // 处理模式3
            processMatcher(pattern3, text, dialogues, 2, 1);
            processMatcher(pattern3b, text, dialogues, 2, 1);
            
            // 处理模式4
            processMatcher(pattern4, text, dialogues, 2, 1);
            
            // 处理模式6 (新增)
            Matcher matcher6 = pattern6.matcher(text);
            while (matcher6.find()) {
                Map<String, String> dialogue = new HashMap<>();
                dialogue.put("speaker", matcher6.group(1).trim());
                dialogue.put("listener", matcher6.group(2).trim());
                dialogue.put("content", matcher6.group(3).trim());
                dialogues.add(dialogue);
                    }
                    
            // 处理模式7 (新增)
            Matcher matcher7 = pattern7.matcher(text);
            while (matcher7.find()) {
                Map<String, String> dialogue = new HashMap<>();
                dialogue.put("speaker", matcher7.group(1).trim());
                dialogue.put("content", matcher7.group(2).trim());
                dialogues.add(dialogue);
                    }
            
            // 处理模式8 (新增)
            Matcher matcher8 = pattern8.matcher(text);
            while (matcher8.find()) {
                Map<String, String> dialogue = new HashMap<>();
                dialogue.put("speaker", matcher8.group(1).trim());
                dialogue.put("content", matcher8.group(2).trim());
                dialogues.add(dialogue);
            }
            
            // 处理模式5 (未知说话者)
            Matcher matcher5 = pattern5.matcher(text);
            while (matcher5.find()) {
                String content = matcher5.group(1).trim();
                
                // 检查是否已经在之前的模式中捕获了这个对话
                boolean alreadyCaptured = false;
                for (Map<String, String> existingDialogue : dialogues) {
                    if (existingDialogue.get("content").equals(content)) {
                        alreadyCaptured = true;
                        break;
                    }
                }
                
                if (!alreadyCaptured) {
                    // 未收录的对话，尝试通过上下文推断说话者
                    String potentialSpeaker = inferSpeakerFromContext(text, matcher5.start());
                    
                    Map<String, String> dialogue = new HashMap<>();
                    dialogue.put("speaker", potentialSpeaker);
                    dialogue.put("content", content);
                    dialogues.add(dialogue);
                }
            }
            
        } catch (Exception e) {
            logger.error("提取对话失败", e);
        }
        
        return dialogues;
    }
    
    /**
     * 处理正则匹配结果并添加到对话列表
     */
    private void processMatcher(Pattern pattern, String text, List<Map<String, String>> dialogues, 
                              int speakerGroup, int contentGroup) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            Map<String, String> dialogue = new HashMap<>();
            dialogue.put("speaker", matcher.group(speakerGroup).trim());
            dialogue.put("content", matcher.group(contentGroup).trim());
            dialogues.add(dialogue);
        }
    }
    
    /**
     * 尝试从上下文推断说话者
     * 新增方法，通过分析对话前的内容来推断可能的说话者
     */
    private String inferSpeakerFromContext(String text, int dialogueStartPosition) {
        // 默认未知人物
        String defaultSpeaker = "未知人物";
        
        try {
            // 获取对话前的最多50个字符
            int contextStart = Math.max(0, dialogueStartPosition - 50);
            String context = text.substring(contextStart, dialogueStartPosition);
            
            // 查找可能的人名模式
            Pattern namePattern = Pattern.compile("([^，。：:\"'！？\\s\\n]{1,10})(?:说|道|问|答|看|笑|叹|喊)");
            Matcher matcher = namePattern.matcher(context);
            
            String lastMatch = null;
            while (matcher.find()) {
                lastMatch = matcher.group(1).trim();
            }
            
            return lastMatch != null ? lastMatch : defaultSpeaker;
            
        } catch (Exception e) {
            logger.debug("从上下文推断说话者失败", e);
            return defaultSpeaker;
        }
    }

    @Override
    public List<Map<String, Object>> analyzeCharacterRelationships(List<Map<String, String>> dialogues) {
        List<Map<String, Object>> relationships = new ArrayList<>();
        
        // 对话次数统计
        Map<String, Map<String, Integer>> dialogCounts = new HashMap<>();
        
        // 对话情感映射
        Map<String, Map<String, Double>> dialogSentiments = new HashMap<>();
        
        // 对话词汇共现统计
        Map<String, Map<String, Set<String>>> sharedWords = new HashMap<>();
        
        // 共现矩阵：记录角色在同一场景中出现的次数
        Map<String, Map<String, Integer>> cooccurrenceMatrix = new HashMap<>();
        
        // 提取所有可能的角色
        Set<String> allCharacters = new HashSet<>();
        
        // 获取词典工具实例
        ChineseNameDictionary nameDictionary = ApplicationContextProvider.getBean(ChineseNameDictionary.class);
        
        // 1. 统计对话数据
        for (Map<String, String> dialogue : dialogues) {
            String speaker = dialogue.get("speaker");
            String content = dialogue.get("content");
            
            if (speaker == null || content == null || speaker.isEmpty() || content.isEmpty()) {
                continue;
            }
            
            allCharacters.add(speaker);
            
            // 提取对话中提到的其他角色
            Set<String> mentionedCharacters = extractMentionedCharacters(content);
            
            // 更新共现矩阵
            for (String mentionedChar : mentionedCharacters) {
                allCharacters.add(mentionedChar);
                
                // 更新speaker与mentionedChar的共现关系
                cooccurrenceMatrix.computeIfAbsent(speaker, k -> new HashMap<>())
                    .put(mentionedChar, cooccurrenceMatrix.getOrDefault(speaker, Collections.emptyMap())
                        .getOrDefault(mentionedChar, 0) + 1);
                
                // 反向更新，确保共现矩阵的对称性
                cooccurrenceMatrix.computeIfAbsent(mentionedChar, k -> new HashMap<>())
                    .put(speaker, cooccurrenceMatrix.getOrDefault(mentionedChar, Collections.emptyMap())
                        .getOrDefault(speaker, 0) + 1);
            }
            
            // 计算对话的情感值
            double sentiment = analyzeSentiment(content);
            
            // 提取对话关键词
            List<String> keywords = HanLP.extractKeyword(content, 5);
            
            // 更新对话统计
            for (String mentionedChar : mentionedCharacters) {
                // 排除自己对自己说话的情况
                if (mentionedChar.equals(speaker)) {
                    continue;
                }
                
                // 增加对话计数
                dialogCounts.computeIfAbsent(speaker, k -> new HashMap<>());
                dialogCounts.get(speaker).put(mentionedChar, 
                        dialogCounts.get(speaker).getOrDefault(mentionedChar, 0) + 1);
                
                // 更新情感值
                dialogSentiments.computeIfAbsent(speaker, k -> new HashMap<>());
                Double currentSentiment = dialogSentiments.get(speaker).getOrDefault(mentionedChar, 0.5);
                // 将新情感值与历史情感值加权平均
                dialogSentiments.get(speaker).put(mentionedChar, 
                        currentSentiment * 0.7 + sentiment * 0.3);
                
                // 更新共享词汇
                sharedWords.computeIfAbsent(speaker, k -> new HashMap<>());
                sharedWords.get(speaker).computeIfAbsent(mentionedChar, k -> new HashSet<>());
                sharedWords.get(speaker).get(mentionedChar).addAll(keywords);
            }
        }
        
        // 2. 计算角色重要性得分
        Map<String, Double> characterImportance = calculateCharacterImportance(cooccurrenceMatrix, allCharacters);
        
        // 3. 过滤噪声角色
        Set<String> significantCharacters = filterSignificantCharacters(characterImportance, allCharacters);
        
        // 4. 生成关系数据
        for (String char1 : significantCharacters) {
            for (String char2 : significantCharacters) {
                if (char1.equals(char2)) {
                    continue;
                }
                
                // 计算交互频率
                int char1ToChar2 = dialogCounts.getOrDefault(char1, Collections.emptyMap())
                                            .getOrDefault(char2, 0);
                int char2ToChar1 = dialogCounts.getOrDefault(char2, Collections.emptyMap())
                                            .getOrDefault(char1, 0);
                
                // 共现次数
                int cooccurrences = cooccurrenceMatrix.getOrDefault(char1, Collections.emptyMap())
                                                .getOrDefault(char2, 0);
                
                // 忽略没有足够交互或共现的角色对
                if ((char1ToChar2 + char2ToChar1 < 2) && cooccurrences < 3) {
                    continue;
                }
                
                // 计算情感倾向
                double char1ToChar2Sentiment = dialogSentiments.getOrDefault(char1, Collections.emptyMap())
                                                    .getOrDefault(char2, 0.5);
                double char2ToChar1Sentiment = dialogSentiments.getOrDefault(char2, Collections.emptyMap())
                                                    .getOrDefault(char1, 0.5);
                
                // 情感平均值
                double averageSentiment = (char1ToChar2Sentiment + char2ToChar1Sentiment) / 2;
                
                // 提取共享语言特征
                Set<String> char1ToChar2Words = sharedWords.getOrDefault(char1, Collections.emptyMap())
                                                   .getOrDefault(char2, Collections.emptySet());
                Set<String> char2ToChar1Words = sharedWords.getOrDefault(char2, Collections.emptyMap())
                                                   .getOrDefault(char1, Collections.emptySet());
                
                // 合并共享词汇
                Set<String> combinedWords = new HashSet<>(char1ToChar2Words);
                combinedWords.addAll(char2ToChar1Words);
                
                // 确定关系类型
                String relationship = determineRelationshipType(
                    char1ToChar2, char2ToChar1, averageSentiment, combinedWords);
                
                // 计算关系置信度 - 考虑共现因素
                double confidence = calculateConfidence(
                    char1ToChar2, char2ToChar1, averageSentiment, cooccurrences);
                
                // 创建关系对象
                Map<String, Object> relation = new HashMap<>();
                relation.put("character1", char1);
                relation.put("character2", char2);
                relation.put("relationship", relationship);
                relation.put("confidence", confidence);
                relation.put("interactions", char1ToChar2 + char2ToChar1);
                relation.put("cooccurrences", cooccurrences);
                relation.put("sentiment", averageSentiment);
                
                // 添加关键词特征
                if (!combinedWords.isEmpty()) {
                    relation.put("shared_topics", String.join(", ", 
                        combinedWords.stream().limit(5).collect(Collectors.toList())));
                }
                
                // 检查是否为别名关系（同一角色的不同称呼）
                if (isLikelyAlias(char1, char2, cooccurrences, averageSentiment)) {
                    relation.put("relationship", "可能是同一角色");
                    relation.put("is_alias", true);
                    
                    // 记录别名关系
                    try {
                        if (nameDictionary != null) {
                            // 选择更可能是主名称的作为realName（通常是出现次数更多的）
                            if (characterImportance.getOrDefault(char1, 0.0) > 
                                characterImportance.getOrDefault(char2, 0.0)) {
                                nameDictionary.addCharacterAlias(char1, char2);
                            } else {
                                nameDictionary.addCharacterAlias(char2, char1);
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("添加角色别名失败", e);
                    }
                }
                
                relationships.add(relation);
            }
        }
        
        // 按照互动次数和置信度排序
        relationships.sort((r1, r2) -> {
            Double conf1 = (Double) r1.get("confidence");
            Double conf2 = (Double) r2.get("confidence");
            return conf2.compareTo(conf1);
        });
        
        return relationships;
    }

    /**
     * 计算角色的重要性得分
     * @param cooccurrenceMatrix 共现矩阵
     * @param allCharacters 所有角色
     * @return 角色重要性得分映射
     */
    private Map<String, Double> calculateCharacterImportance(
            Map<String, Map<String, Integer>> cooccurrenceMatrix, 
            Set<String> allCharacters) {
        
        Map<String, Double> importance = new HashMap<>();
        
        // 初始化每个角色的重要性为1.0
        for (String character : allCharacters) {
            importance.put(character, 1.0);
        }
        
        // 迭代计算重要性（简化版PageRank算法）
        double dampingFactor = 0.85;
        int iterations = 10;
        
        for (int i = 0; i < iterations; i++) {
            Map<String, Double> newImportance = new HashMap<>();
            
            for (String character : allCharacters) {
                double sum = 0.0;
                
                // 计算所有指向当前角色的其他角色的贡献
                for (String other : allCharacters) {
                    if (other.equals(character)) continue;
                    
                    int linkWeight = cooccurrenceMatrix.getOrDefault(other, Collections.emptyMap())
                                                  .getOrDefault(character, 0);
                    
                    if (linkWeight > 0) {
                        // 计算其他角色的出链总权重
                        int totalOutWeight = 0;
                        for (Integer weight : cooccurrenceMatrix.getOrDefault(other, Collections.emptyMap()).values()) {
                            totalOutWeight += weight;
                        }
                        
                        if (totalOutWeight > 0) {
                            sum += importance.get(other) * ((double)linkWeight / totalOutWeight);
                        }
                    }
                }
                
                // 应用阻尼因子
                newImportance.put(character, (1 - dampingFactor) + dampingFactor * sum);
            }
            
            // 更新重要性
            importance = newImportance;
            }
        
        return importance;
        }
        
    /**
     * 过滤出重要的角色，移除噪声
     * @param importance 角色重要性得分
     * @param allCharacters 所有角色
     * @return 重要角色集合
     */
    private Set<String> filterSignificantCharacters(Map<String, Double> importance, Set<String> allCharacters) {
        // 如果角色数量较少，不进行过滤
        if (allCharacters.size() <= 10) {
            return allCharacters;
        }
        
        // 计算重要性阈值
        List<Double> scores = new ArrayList<>(importance.values());
        Collections.sort(scores, Collections.reverseOrder());
        
        // 取前70%的角色，或者重要性大于平均值的角色
        double threshold = 0.0;
        if (scores.size() >= 5) {
            int cutoff = Math.max(5, (int)(scores.size() * 0.7));
            threshold = scores.get(Math.min(cutoff, scores.size() - 1));
        }
        
        // 创建final变量用于lambda表达式
        final double finalThreshold = threshold;
        
        // 过滤重要角色
        return importance.entrySet().stream()
                .filter(e -> e.getValue() >= finalThreshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
    
    /**
     * 判断两个角色名称是否可能是同一角色的不同称呼
     */
    private boolean isLikelyAlias(String name1, String name2, int cooccurrences, double sentiment) {
        // 1. 两个名称很少同时出现在一个场景中
        boolean lowCooccurrence = cooccurrences <= 1;
        
        // 2. 两个名称有字符重叠
        boolean hasOverlap = hasCharacterOverlap(name1, name2);
        
        // 3. 一个是另一个的简称
        boolean isAbbreviation = isAbbreviation(name1, name2);
        
        // 根据不同因素判断是否可能是别名
        return (lowCooccurrence && (hasOverlap || isAbbreviation)) ||
               (hasOverlap && isAbbreviation);
    }
    
    /**
     * 检查两个名称是否有字符重叠
     */
    private boolean hasCharacterOverlap(String name1, String name2) {
        // 如果名称太短，不考虑重叠
        if (name1.length() < 2 || name2.length() < 2) {
            return false;
        }
        
        // 检查一个名称是否包含另一个名称的一部分
        for (int i = 0; i < name1.length() - 1; i++) {
            String part = name1.substring(i, i + 2);
            if (name2.contains(part)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 判断一个名称是否可能是另一个的简称/缩写
     */
    private boolean isAbbreviation(String name1, String name2) {
        // 如果一个名称是单字，检查是否是另一个名称的第一个字
        if (name1.length() == 1 && name2.length() > 1) {
            return name2.startsWith(name1);
        }
        
        if (name2.length() == 1 && name1.length() > 1) {
            return name1.startsWith(name2);
        }
        
        // 检查复姓+单字名情况
        if (name1.length() == 3 && name2.length() == 2) {
            return name1.endsWith(name2) || name1.startsWith(name2);
        }
        
        if (name2.length() == 3 && name1.length() == 2) {
            return name2.endsWith(name1) || name2.startsWith(name1);
        }
        
        return false;
    }

    /**
     * 基于交互频率、情感值和共现分析确定关系类型
     */
    private String determineRelationshipType(int interactions1to2, int interactions2to1, 
                                          double sentiment, Set<String> sharedWords) {
        // 互动总数
        int totalInteractions = interactions1to2 + interactions2to1;
        
        // 互动对称性 (0-1，越接近1表示越对称)
        double symmetry = totalInteractions == 0 ? 0 : 
                         1.0 - Math.abs(interactions1to2 - interactions2to1) / (double) totalInteractions;
        
        // 根据情感值和互动特征判断关系类型
        if (sentiment > 0.8) {
            if (symmetry > 0.7) {
                return "亲密朋友";
            } else if (sharedWordsSuggestRomance(sharedWords)) {
                return "恋人";
            } else if (sharedWordsSuggestFamily(sharedWords)) {
                return "家人";
            } else {
                return "友好";
            }
        } else if (sentiment > 0.6) {
            if (symmetry > 0.6) {
                return "朋友";
            } else if (sharedWordsSuggestMentor(sharedWords)) {
                return "师徒";
            } else if (sharedWordsSuggestBusiness(sharedWords)) {
                return "合作伙伴";
            } else {
                return "熟人";
            }
        } else if (sentiment > 0.4) {
            if (sharedWordsSuggestBusiness(sharedWords)) {
                return "业务关系";
            } else {
                return "中性关系";
            }
        } else if (sentiment > 0.2) {
            if (totalInteractions > 10) {
                return "对立";
            } else {
                return "疏远";
            }
        } else {
            if (totalInteractions > 5) {
                return "敌对";
            } else if (sharedWordsSuggestCompetition(sharedWords)) {
                return "竞争对手";
            } else {
                return "冲突关系";
            }
        }
    }

    /**
     * 判断共享词汇是否暗示浪漫关系
     */
    private boolean sharedWordsSuggestRomance(Set<String> words) {
        String[] romanticWords = {"爱", "喜欢", "心动", "浪漫", "亲爱", "想你", "思念", 
                               "温柔", "甜蜜", "牵手", "拥抱", "吻", "爱人", "约会"};
        
        return containsAnyWord(words, romanticWords);
    }

    /**
     * 判断共享词汇是否暗示家庭关系
     */
    private boolean sharedWordsSuggestFamily(Set<String> words) {
        String[] familyWords = {"父亲", "母亲", "爸爸", "妈妈", "儿子", "女儿", "哥哥", "姐姐", 
                             "弟弟", "妹妹", "家人", "亲人", "血缘", "家庭", "亲情"};
        
        return containsAnyWord(words, familyWords);
    }

    /**
     * 判断共享词汇是否暗示师徒关系
     */
    private boolean sharedWordsSuggestMentor(Set<String> words) {
        String[] mentorWords = {"老师", "师父", "师傅", "徒弟", "弟子", "门徒", "学徒", 
                              "教导", "指导", "传授", "学习", "教导", "修行"};
        
        return containsAnyWord(words, mentorWords);
    }

    /**
     * 判断共享词汇是否暗示商业关系
     */
    private boolean sharedWordsSuggestBusiness(Set<String> words) {
        String[] businessWords = {"合作", "伙伴", "生意", "交易", "买卖", "买", "卖", 
                                "价格", "钱", "金钱", "利润", "商业", "市场", "客户"};
        
        return containsAnyWord(words, businessWords);
    }

    /**
     * 判断共享词汇是否暗示竞争关系
     */
    private boolean sharedWordsSuggestCompetition(Set<String> words) {
        String[] competitionWords = {"对手", "竞争", "争夺", "比赛", "打败", "胜利", "失败", 
                                 "赢", "输", "超越", "挑战", "实力", "强弱", "较量"};
        
        return containsAnyWord(words, competitionWords);
    }

    /**
     * 判断集合中是否包含指定词汇中的任何一个
     */
    private boolean containsAnyWord(Set<String> wordSet, String[] targetWords) {
        for (String word : targetWords) {
            for (String setWord : wordSet) {
                if (setWord.contains(word)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 计算关系的置信度，考虑共现因素
     */
    private double calculateConfidence(int interactions1to2, int interactions2to1, 
                                      double sentiment, int cooccurrences) {
        int totalInteractions = interactions1to2 + interactions2to1;
        
        // 基础置信度计算
        double baseConfidence = (Math.min(1.0, totalInteractions / 10.0) * 0.6) + 
                               (Math.abs(sentiment - 0.5) * 0.4);
        
        // 加入共现次数的因素
        double cooccurrenceBoost = Math.min(0.3, cooccurrences * 0.05);
        
        return Math.min(1.0, baseConfidence + cooccurrenceBoost);
    }

    /**
     * 生成世界观摘要
     * 
     * @param text 需要分析的文本
     * @return 生成的世界观摘要
     */
    @Override
    public String generateWorldBuildingSummary(String text) {
        logger.info("开始生成世界观摘要...");
        
        // 提取各种类型的世界观元素
        List<String> locations = extractNamedEntities(text, "LOCATION");
        List<String> organizations = extractNamedEntities(text, "ORGANIZATION");
        List<String> items = extractNamedEntities(text, "n").stream()
                .filter(item -> item.length() > 1)
                .limit(20)
                .collect(Collectors.toList());
        List<String> worldElements = identifyWorldBuildingElements(text);
        
        // 合并所有关键实体
        Set<String> allEntities = new HashSet<>();
        allEntities.addAll(locations);
        allEntities.addAll(organizations);
        allEntities.addAll(items);
        allEntities.addAll(worldElements);
        
        // 过滤太常见或不相关的实体
        List<String> filteredEntities = filterEntities(allEntities, text);
        
        // 使用关键词和相似度计算找出与世界观相关的段落
        List<String> paragraphs = Arrays.asList(text.split("\n\n"));
        Map<String, Double> paragraphScores = new HashMap<>();
        
        // 世界观相关的参考描述
        String worldBuildingReference = "世界观设定描述包含了小说中的地理位置、政治组织、种族、魔法系统、文化背景等元素";
        
        // 计算段落得分
        for (String paragraph : paragraphs) {
            if (paragraph.length() < 30 || paragraph.length() > 500) continue;
            
            // 检查段落中是否包含世界观元素
            boolean containsLocation = locations.stream().anyMatch(paragraph::contains);
            boolean containsOrganization = organizations.stream().anyMatch(paragraph::contains);
            boolean containsWorldElement = worldElements.stream().anyMatch(paragraph::contains);
            
            // 使用机器学习计算与世界观的相关性
            double relevanceScore = mlService.calculateTextSimilarity(paragraph, worldBuildingReference);
            
            // 使用深度情感分析来评估段落的客观性（世界观描述通常较为客观）
            double sentiment = mlService.deepSentimentAnalysis(paragraph);
            // 情感中性度（0.5最为中性）
            double objectivityScore = 1.0 - Math.abs(sentiment - 0.5) * 2;
            
            // 计算最终得分
            double score = relevanceScore * 0.4 + objectivityScore * 0.2;
            if (containsLocation) score += 0.15;
            if (containsOrganization) score += 0.15;
            if (containsWorldElement) score += 0.2;
            
            paragraphScores.put(paragraph, score);
        }
        
        // 选择得分最高的3-5个段落
        List<String> topParagraphs = paragraphScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 尝试对选中段落进行聚类，确保它们的主题多样性
        if (topParagraphs.size() > 1) {
            Map<Integer, List<Integer>> clusters = mlService.clusterTexts(topParagraphs, Math.min(topParagraphs.size(), 3));
            if (!clusters.isEmpty()) {
                // 从每个聚类中选择最佳段落
                List<String> diverseParagraphs = new ArrayList<>();
                for (List<Integer> cluster : clusters.values()) {
                    if (!cluster.isEmpty()) {
                        // 获取这个聚类中得分最高的段落
                        final Map<String, Double> finalParagraphScores = paragraphScores;
                        final List<String> finalTopParagraphs = topParagraphs;
                        String bestParagraph = cluster.stream()
                                .map(i -> finalTopParagraphs.get(i))
                                .max(Comparator.comparingDouble(finalParagraphScores::get))
                                .orElse(finalTopParagraphs.get(cluster.get(0)));
                        diverseParagraphs.add(bestParagraph);
            }
        }
        
                // 如果聚类能提供足够多样的段落，使用聚类结果
                if (diverseParagraphs.size() >= 1) {
                    topParagraphs = diverseParagraphs;
                }
            }
        }
        
        // 聚合相关信息
        Map<String, List<String>> entityInfo = aggregateRelatedInformation(filteredEntities, text);
        
        // 如果找不到足够的世界观元素
        if (filteredEntities.size() < 3 && topParagraphs.isEmpty()) {
            return "未能找到明确的世界观设定描述。这个故事可能没有复杂的世界背景，或者世界观信息分散在文本各处。";
        }
        
        // 按类别组织世界观元素
        StringBuilder summary = new StringBuilder();
        summary.append("【世界背景】\n\n");
        
        // 添加从段落中提取的世界观摘要
        if (!topParagraphs.isEmpty()) {
            // 按照原文中的顺序排序，确保叙述的连贯性
            topParagraphs.sort(Comparator.comparingInt(text::indexOf));
            summary.append(String.join("\n\n", topParagraphs)).append("\n\n");
        }
            
        // 地理环境
            if (!locations.isEmpty()) {
            summary.append("\n【地理环境】\n");
            for (String location : locations.subList(0, Math.min(locations.size(), 5))) {
                List<String> descriptions = entityInfo.getOrDefault(location, Collections.emptyList());
                if (!descriptions.isEmpty()) {
                    // 选择最有信息量的1-2个句子
                    List<String> bestDescriptions = descriptions.stream()
                            .sorted((s1, s2) -> Double.compare(
                                    calculateDescriptionQuality(s2, location),
                                    calculateDescriptionQuality(s1, location)))
                            .limit(2)
                            .collect(Collectors.toList());
                    
                    summary.append("- ").append(location).append("：")
                           .append(String.join(" ", bestDescriptions))
                           .append("\n");
                }
            }
        }
        
        // 组织制度
            if (!organizations.isEmpty()) {
            summary.append("\n【组织与制度】\n");
            for (String org : organizations.subList(0, Math.min(organizations.size(), 5))) {
                List<String> descriptions = entityInfo.getOrDefault(org, Collections.emptyList());
                if (!descriptions.isEmpty()) {
                    // 选择最有信息量的1-2个句子
                    List<String> bestDescriptions = descriptions.stream()
                            .sorted((s1, s2) -> Double.compare(
                                    calculateDescriptionQuality(s2, org),
                                    calculateDescriptionQuality(s1, org)))
                            .limit(2)
                            .collect(Collectors.toList());
                    
                    summary.append("- ").append(org).append("：")
                           .append(String.join(" ", bestDescriptions))
                           .append("\n");
                }
            }
        }
        
        // 其他世界元素
        if (!worldElements.isEmpty()) {
            summary.append("\n【世界特有元素】\n");
            for (String element : worldElements.subList(0, Math.min(worldElements.size(), 5))) {
                List<String> descriptions = entityInfo.getOrDefault(element, Collections.emptyList());
                if (!descriptions.isEmpty()) {
                    // 选择最有信息量的1-2个句子
                    List<String> bestDescriptions = descriptions.stream()
                            .sorted((s1, s2) -> Double.compare(
                                    calculateDescriptionQuality(s2, element),
                                    calculateDescriptionQuality(s1, element)))
                            .limit(2)
                            .collect(Collectors.toList());
                    
                    summary.append("- ").append(element).append("：")
                           .append(String.join(" ", bestDescriptions))
                           .append("\n");
                }
            }
        }
        
        // 重要物品/概念
        if (!items.isEmpty()) {
            // 过滤出出现频率高的物品
            List<String> significantItems = items.stream()
                    .filter(item -> countOccurrences(text, item) >= 3)
                    .limit(5)
                    .collect(Collectors.toList());
                    
            if (!significantItems.isEmpty()) {
                summary.append("\n【重要物品与概念】\n");
                for (String item : significantItems) {
                    List<String> descriptions = entityInfo.getOrDefault(item, Collections.emptyList());
                    if (!descriptions.isEmpty()) {
                        List<String> bestDescriptions = descriptions.stream()
                                .sorted((s1, s2) -> Double.compare(
                                        calculateDescriptionQuality(s2, item),
                                        calculateDescriptionQuality(s1, item)))
                                .limit(1)
                                .collect(Collectors.toList());
                        
                        summary.append("- ").append(item).append("：")
                               .append(String.join(" ", bestDescriptions))
                               .append("\n");
                    }
                }
            }
        }
        
        logger.info("世界观摘要生成完成，长度: {}", summary.length());
        return summary.toString();
    }

    /**
     * 从文本中提取实体的描述
     * @param text 全文
     * @param entity 实体名称
     * @return 实体的简短描述
     */
    private String extractEntityDescription(String text, String entity) {
        // 查找包含该实体的句子
        List<String> sentences = splitIntoSentences(text);
        
        // 找出最能描述该实体的句子
        String bestDescription = null;
        double bestScore = 0;
        
        for (String sentence : sentences) {
            if (sentence.contains(entity)) {
                // 计算这个句子作为描述的质量
                // 考虑句子长度适中，并且包含描述性词汇
                double score = calculateDescriptionQuality(sentence, entity);
                
                if (score > bestScore) {
                    bestScore = score;
                    
                    // 提取句子中的相关部分作为描述
                    String description = extractRelevantPart(sentence, entity);
                    if (description != null) {
                        bestDescription = description;
                    }
                }
            }
        }
        
        // 如果描述太长，进行截断
        if (bestDescription != null && bestDescription.length() > 30) {
            bestDescription = bestDescription.substring(0, 30) + "...";
        }
        
        return bestDescription;
    }

    /**
     * 计算句子作为实体描述的质量
     */
    private double calculateDescriptionQuality(String sentence, String entity) {
        double score = 0.0;
        
        // 检测定义性描述
        if (sentence.contains("是") || sentence.contains("为") || 
            sentence.contains("被称为") || sentence.contains("代表") ||
            sentence.contains("乃") || sentence.contains("指") ||
            sentence.contains("表示") || sentence.contains("象征")) {
            score += 0.3;
        }
        
        // 检测详细描述
        if (sentence.length() > 20) {
            score += 0.2;
        }
        
        // 检测独特性描述
        if (sentence.contains("独特") || sentence.contains("特殊") || 
            sentence.contains("不同") || sentence.contains("唯一") ||
            sentence.contains("特点") || sentence.contains("著名") ||
            sentence.contains("著称") || sentence.contains("有名")) {
            score += 0.2;
        }
        
        // 检测周边关系描述
        if (sentence.contains("之间") || sentence.contains("关系") || 
            sentence.contains("联系") || sentence.contains("影响") ||
            sentence.contains("相互") || sentence.contains("交流") ||
            sentence.contains("传承") || sentence.contains("统治")) {
            score += 0.3;
        }
        
        // 位置描述得分加成
        if (sentence.contains("位于") || sentence.contains("坐落") ||
            sentence.contains("在") || sentence.contains("中心") ||
            sentence.contains("边境") || sentence.contains("附近")) {
            score += 0.2;
        }
        
        // 确保分数在0-1之间
        return Math.min(1.0, score);
    }

    /**
     * 从句子中提取与实体相关的部分作为描述
     */
    private String extractRelevantPart(String sentence, String entity) {
        // 简单实现：寻找包含"是"、"为"等描述词的模式
        int entityPos = sentence.indexOf(entity);
        if (entityPos == -1) return null;
        
        // 实体后面的描述性内容
        String[] patterns = {"是", "为", "乃", "指"};
        for (String pattern : patterns) {
            int pos = sentence.indexOf(pattern, entityPos + entity.length());
            if (pos != -1 && pos < entityPos + entity.length() + 5) {
                // 提取描述部分
                return sentence.substring(pos + pattern.length()).trim();
            }
        }
        
        // 实体前面的描述性内容
        patterns = new String[]{"被称为", "被誉为", "被视为", "称为"};
        for (String pattern : patterns) {
            int pos = sentence.indexOf(pattern, 0);
            if (pos != -1 && pos < entityPos && pos + pattern.length() + 5 > entityPos) {
                // 提取描述部分
                return sentence.substring(0, pos).trim();
            }
        }
        
        return null;
    }
    
    /**
     * 生成情节发展摘要
     * 
     * @param text 需要分析的文本
     * @return 生成的情节发展摘要
     */
    @Override
    public String generatePlotProgressionSummary(String text) {
        logger.info("生成情节发展摘要");
        
        // 分析文本结构
        List<Integer> chapterBreaks = detectChapterBreaks(text);
        List<Integer> sceneChanges = detectMajorSceneChanges(text);
        
        // 分析时间线
        List<String> timeReferences = extractTimeReferences(text);
        
        // 提取情节关键点
        Map<String, Integer> plotKeywords = extractPlotKeywords(text);
        
        // 分析情感变化点
        List<String> sentences = splitIntoSentences(text);
        List<Map<String, Object>> emotionChanges = new ArrayList<>();
        
        double prevSentiment = 0;
        for (int i = 0; i < sentences.size(); i += 10) { // 每10个句子检查一次
            String sentenceGroup = String.join(" ", 
                    sentences.subList(i, Math.min(i + 10, sentences.size())));
            double sentiment = analyzeSentiment(sentenceGroup);
            
            // 如果情感有显著变化
            if (Math.abs(sentiment - prevSentiment) > 0.3) {
                Map<String, Object> change = new HashMap<>();
                change.put("position", i);
                change.put("sentiment", sentiment);
                change.put("text", sentenceGroup);
                emotionChanges.add(change);
            }
            
            prevSentiment = sentiment;
        }
        
        // 构建情节发展摘要
        StringBuilder summary = new StringBuilder();
        
        // 总体故事摘要（限制在300字以内）
        String overallSummary = generateSummary(text, 300);
        summary.append(overallSummary).append("\n\n");
        
        // 情节发展分析
        summary.append("情节发展：\n");
        
        // 如果有章节分隔，基于章节构建情节
        if (!chapterBreaks.isEmpty()) {
            for (int i = 0; i < chapterBreaks.size(); i++) {
                int startPos = chapterBreaks.get(i);
                int endPos = (i < chapterBreaks.size() - 1) ? 
                             chapterBreaks.get(i + 1) : text.length();
                
                // 提取章节标题
                String chapterText = text.substring(startPos, endPos);
                String[] lines = chapterText.split("\n", 2);
                String chapterTitle = lines[0].trim();
                
                if (i < 3 || i >= chapterBreaks.size() - 3) {  // 只显示前3章和后3章
                    summary.append("• ").append(chapterTitle);
                    
                    // 分析该章节的主要情感
                    double chapterSentiment = analyzeSentiment(chapterText);
                    String sentimentDesc = chapterSentiment > 0.6 ? "积极" : 
                                          (chapterSentiment < 0.4 ? "消极" : "中性");
                    summary.append(" (情感基调：").append(sentimentDesc).append(")\n");
                } else if (i == 3 && chapterBreaks.size() > 6) {
                    summary.append("• ... 中间章节省略 ...\n");
                }
            }
        }
        // 如果没有明确章节，则基于场景变化点
        else if (!sceneChanges.isEmpty()) {
            summary.append("• 故事开始\n");
            
            for (int i = 0; i < Math.min(sceneChanges.size(), 5); i++) {
                int scenePos = sceneChanges.get(i);
                String sceneSentence = findSentenceAtPosition(text, scenePos);
                summary.append("• 场景转换: ").append(sceneSentence).append("\n");
            }
            
            summary.append("• 故事结束\n");
        }
        
        // 添加情感高潮点
        if (!emotionChanges.isEmpty()) {
            summary.append("\n情感高潮点：\n");
            int displayed = 0;
            
            for (Map<String, Object> change : emotionChanges) {
                double sentiment = (double) change.get("sentiment");
                String emotionText = (String) change.get("text");
                
                // 只显示强烈情感变化
                if (Math.abs(sentiment - 0.5) > 0.3 && displayed < 3) {
                    summary.append("• ").append(
                            sentiment > 0.5 ? "高兴/激动" : "悲伤/紧张"
                    ).append(": ");
                    
                    // 提取句子的简短版本
                    if (emotionText.length() > 50) {
                        emotionText = emotionText.substring(0, 47) + "...";
                    }
                    summary.append(emotionText).append("\n");
                    displayed++;
                }
            }
        }
        
        // 添加时间线提示
        if (!timeReferences.isEmpty()) {
            summary.append("\n时间线索引：");
            for (int i = 0; i < Math.min(timeReferences.size(), 5); i++) {
                if (i > 0) summary.append("→");
                summary.append(timeReferences.get(i));
            }
            if (timeReferences.size() > 5) {
                summary.append("→...");
            }
        }
        
        return summary.toString();
    }
    
    /**
     * 提取文本中的时间引用
     */
    private List<String> extractTimeReferences(String text) {
        List<Term> terms = HanLP.segment(text);
        Set<String> timeSet = new LinkedHashSet<>(); // 使用LinkedHashSet保持顺序并去重
        
        // 提取时间词
        for (Term term : terms) {
            if (term.nature.toString().equals("t")) {
                if (term.word.length() > 1) { // 忽略单字时间词
                    timeSet.add(term.word);
                }
            }
        }
        
        // 正则表达式匹配时间表达
        Pattern timePattern = Pattern.compile("([0-9零一二三四五六七八九十百千万]+年|[0-9零一二三四五六七八九十]+月|[0-9零一二三四五六七八九十]+日|[0-9零一二三四五六七八九十]+点|[0-9零一二三四五六七八九十]+分)");
        Matcher matcher = timePattern.matcher(text);
        
        while (matcher.find()) {
            timeSet.add(matcher.group());
        }
        
        // 特定时间关键词
        String[] timeKeywords = {"清晨", "早晨", "上午", "中午", "下午", "傍晚", "晚上", "深夜", 
                               "昨天", "今天", "明天", "后天", "前天", "上周", "本周", "下周", 
                               "上个月", "这个月", "下个月", "去年", "今年", "明年"};
        
        for (String keyword : timeKeywords) {
            if (text.contains(keyword)) {
                timeSet.add(keyword);
            }
        }
        
        return new ArrayList<>(timeSet);
    }
    
    /**
     * 在文本指定位置附近查找完整句子
     */
    private String findSentenceAtPosition(String text, int position) {
        // 在指定位置前后100个字符内找到句子边界
        int start = Math.max(0, position - 100);
        int end = Math.min(text.length(), position + 100);
        
        String context = text.substring(start, end);
        String[] sentences = context.split("[。！？.!?]");
        
        // 返回最接近position的句子
        if (sentences.length > 0) {
            int closestIndex = 0;
            int minDistance = Integer.MAX_VALUE;
            int currentPos = start;
            
            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i];
                int sentencePos = currentPos + sentence.length() / 2;
                int distance = Math.abs(sentencePos - position);
                
                if (distance < minDistance) {
                    minDistance = distance;
                    closestIndex = i;
                }
                
                currentPos += sentence.length() + 1; // +1 for the punctuation
            }
            
            return sentences[closestIndex].trim();
        }
        
        // 如果找不到合适的句子，返回截取的上下文
        if (context.length() > 50) {
            return context.substring(0, 47) + "...";
        }
        return context.trim();
    }

    /**
     * 识别世界观特有元素的方法
     */
    private List<String> identifyWorldBuildingElements(String text) {
        List<String> elements = new ArrayList<>();
        
        // 寻找包含特定标记的句子
        String[] worldBuildingMarkers = {
            "世界", "宇宙", "大陆", "王国", "帝国", "神话", "传说", "魔法", "科技", 
            "文明", "种族", "规则", "体系", "制度", "历史", "神灵", "法则", "能力"
        };
        
        String[] sentences = text.split("[。！？.!?]+");
        for (String sentence : sentences) {
            for (String marker : worldBuildingMarkers) {
                if (sentence.contains(marker)) {
                    // 提取关键短语
                    int index = sentence.indexOf(marker);
                    int start = Math.max(0, index - 10);
                    int end = Math.min(sentence.length(), index + marker.length() + 10);
                    String element = sentence.substring(start, end).trim();
                    
                    if (element.length() > marker.length() + 2) {
                        elements.add(element);
                    }
                    break;
                }
            }
        }
        
        return elements;
    }
    
    /**
     * 整合关联信息方法
     */
    private Map<String, List<String>> aggregateRelatedInformation(List<String> entities, String text) {
        Map<String, List<String>> entityInfo = new HashMap<>();
        
        for (String entity : entities) {
            // 收集与实体相关的所有句子
            List<String> relatedSentences = new ArrayList<>();
            String[] sentences = text.split("[。！？.!?]+");
            
            for (String sentence : sentences) {
                if (sentence.contains(entity)) {
                    relatedSentences.add(sentence.trim());
                }
            }
            
            // 获取次级关联实体
            Set<String> relatedEntities = new HashSet<>();
            for (String sentence : relatedSentences) {
                for (String otherEntity : entities) {
                    if (!entity.equals(otherEntity) && sentence.contains(otherEntity)) {
                        relatedEntities.add(otherEntity);
                    }
                }
            }
            
            // 添加关联实体信息
            if (!relatedEntities.isEmpty() && relatedEntities.size() <= 3) {
                relatedSentences.add("与" + String.join("、", relatedEntities) + "有密切关联。");
            }
            
            entityInfo.put(entity, relatedSentences);
        }
        
        return entityInfo;
    }
    
    /**
     * 过滤实体的辅助方法
     */
    private List<String> filterEntities(Set<String> entities, String text) {
        // 过滤太短的实体
        List<String> filtered = entities.stream()
            .filter(e -> e.length() > 1)
            .collect(Collectors.toList());
        
        // 根据在文本中的出现频率排序
        filtered.sort((e1, e2) -> {
            int count1 = countOccurrences(text, e1);
            int count2 = countOccurrences(text, e2);
            return Integer.compare(count2, count1);
        });
        
        // 取前20个最频繁的实体
        return filtered.stream().limit(20).collect(Collectors.toList());
    }
    
    /**
     * 计算字符串在文本中出现的次数
     */
    private int countOccurrences(String text, String target) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }

    /**
     * 从文本中提取提到的人物角色
     */
    private Set<String> extractMentionedCharacters(String text) {
        Set<String> mentionedCharacters = new HashSet<>();
        
        // 提取人名
        List<Term> terms = HanLP.segment(text);
        for (Term term : terms) {
            if (isPerson(term.nature.toString())) {
                mentionedCharacters.add(term.word);
            }
        }
        
        // 尝试提取称呼语（如"爸爸"、"老师"、"大人"等）
        Pattern titlePattern = Pattern.compile("([老小][爷子]|大人|[师父]|[爸妈][爸妈]|[叔伯舅姨姑][父母]|[哥姐弟妹]|[王帝将军])");
        Matcher matcher = titlePattern.matcher(text);
        while (matcher.find()) {
            mentionedCharacters.add(matcher.group());
        }
        
        return mentionedCharacters;
    }
} 