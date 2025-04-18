package com.novelassistant.service.impl;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.mining.word2vec.DocVectorModel;
import com.hankcs.hanlp.mining.word2vec.WordVectorModel;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.summary.TextRankKeyword;
import com.hankcs.hanlp.summary.TextRankSentence;
import com.novelassistant.service.NlpService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

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

    // 章节标题识别的正则表达式
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("第[0-9零一二三四五六七八九十百千万亿]+[章节集卷]\\s*[^\\n]{2,20}");
    
    // 人名词性集合
    private static final Set<String> PERSON_NATURES = new HashSet<>(Arrays.asList("nr", "nrj", "nrf"));
    
    // 情感词典权重
    private static final Map<String, Integer> EMOTION_DICT = new HashMap<>();
    
    static {
        // 初始化情感词典（简化版，实际应使用更完整的词典）
        EMOTION_DICT.put("喜悦", 2);
        EMOTION_DICT.put("高兴", 2);
        EMOTION_DICT.put("愉快", 2);
        EMOTION_DICT.put("激动", 1);
        EMOTION_DICT.put("兴奋", 2);
        EMOTION_DICT.put("幸福", 3);
        EMOTION_DICT.put("开心", 2);
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
    }

    @Override
    public Map<String, Integer> extractKeywords(String text, int maxKeywords) {
        List<String> keywordList = HanLP.extractKeyword(text, Math.min(100, maxKeywords));
        Map<String, Integer> result = new LinkedHashMap<>();
        
        // 使用TextRank算法重新获取关键词及其权重
        TextRankKeyword textRankKeyword = new TextRankKeyword();
        // 直接获取关键词，使用索引位置作为权重的参考
        List<String> textRankKeywords = textRankKeyword.getKeywordList(text, maxKeywords);
        
        // 按照TextRank结果的顺序添加到结果集
        for (int i = 0; i < textRankKeywords.size(); i++) {
            String keyword = textRankKeywords.get(i);
            // 根据排名计算权重，排名越靠前权重越高
            int weight = 100 - i * (100 / (maxKeywords + 1));
            result.put(keyword, weight);
        }
        
        // 如果TextRank结果不足，添加使用HanLP提取的关键词
        if (result.size() < maxKeywords) {
            for (String keyword : keywordList) {
                if (!result.containsKey(keyword)) {
                    // 对于仅通过HanLP提取的关键词，赋予默认权重
                    result.put(keyword, 50);
                    if (result.size() >= maxKeywords) break;
                }
            }
        }
        
        return result;
    }

    @Override
    public String generateSummary(String text, int maxLength) {
        // 使用TextRank自动摘要
        List<String> sentenceList = HanLP.extractSummary(text, Math.max(3, maxLength / 30));
        return String.join("。", sentenceList);
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
        
        for (Term term : termList) {
            // 检查是否是人名词性
            if (isPerson(term.nature.toString())) {
                String name = term.word;
                characterCounts.put(name, characterCounts.getOrDefault(name, 0) + 1);
            }
        }
        
        // 过滤出现次数大于1的人名，减少误识别
        return characterCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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
        
        // 使用正则表达式查找章节标题
        Matcher matcher = CHAPTER_PATTERN.matcher(text);
        while (matcher.find()) {
            chapterPositions.add(matcher.start());
        }
        
        return chapterPositions;
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
        // 基于词典的简单情感分析
        List<Term> termList = HanLP.segment(text);
        int total = 0;
        int positiveScore = 0;
        
        for (Term term : termList) {
            String word = term.word;
            if (EMOTION_DICT.containsKey(word)) {
                int score = EMOTION_DICT.get(word);
                if (score > 0) {
                    positiveScore += score;
                }
                total += Math.abs(score);
            }
        }
        
        // 如果没有情感词，返回中性得分0.5
        if (total == 0) {
            return 0.5;
        }
        
        // 计算正面情感的比例（0-1之间的值）
        return (double) positiveScore / total;
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
        
        // 改进的正则表达式，同时支持中文引号和英文引号，且对角色名称长度做更灵活的限制
        Pattern dialoguePattern = Pattern.compile("([^，。：\"！？\\s\\n]{1,10})：\"(.*?)\"", Pattern.DOTALL);

        Matcher matcher = dialoguePattern.matcher(text);
        
        while (matcher.find()) {
            String speaker = matcher.group(1).trim();
            String content = matcher.group(2).trim();
            
            // 如果说话内容不为空
            if (!content.isEmpty()) {
                Map<String, String> dialogue = new HashMap<>();
                dialogue.put("speaker", speaker);
                dialogue.put("content", content);
                dialogues.add(dialogue);
            }
        }
        
        return dialogues;
    }
} 