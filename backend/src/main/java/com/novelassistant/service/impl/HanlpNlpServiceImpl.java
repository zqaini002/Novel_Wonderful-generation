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
        Map<String, Integer> entityScores = extractNamedEntities(terms);
        
        // 3. 提取标题中的关键词（如果文本第一行是标题）
        Map<String, Integer> titleKeywords = extractTitleKeywords(text);
        
        // 4. 检测并提取重复高频词组
        Map<String, Integer> repeatedPhrases = extractRepeatedPhrases(text);
        
        // 5. 识别情节转折点相关词
        Map<String, Integer> plotKeywords = extractPlotKeywords(text);
        
        // 将TextRank关键词添加到结果中
        for (int i = 0; i < textRankKeywords.size(); i++) {
            String keyword = textRankKeywords.get(i);
            // 根据排名计算权重，排名越靠前权重越高
            int weight = 100 - i * (100 / (maxKeywords + 1));
            result.put(keyword, weight);
        }
        
        // 合并命名实体，增加权重
        for (Map.Entry<String, Integer> entry : entityScores.entrySet()) {
            String entity = entry.getKey();
            int entityWeight = entry.getValue();
            
            if (result.containsKey(entity)) {
                // 如果已存在，增加权重
                result.put(entity, Math.min(100, result.get(entity) + entityWeight));
            } else if (result.size() < maxKeywords) {
                // 如果未满，直接添加
                result.put(entity, entityWeight);
            } else {
                // 如果已满，替换权重最小的项（如果新项权重更大）
                String minWeightKey = getMinWeightKey(result);
                if (minWeightKey != null && result.get(minWeightKey) < entityWeight) {
                    result.remove(minWeightKey);
                    result.put(entity, entityWeight);
                }
            }
        }
        
        // 合并标题关键词，优先级高
        for (Map.Entry<String, Integer> entry : titleKeywords.entrySet()) {
            String titleKeyword = entry.getKey();
            int titleWeight = entry.getValue();
            
            if (result.containsKey(titleKeyword)) {
                // 如果已存在，大幅增加权重
                result.put(titleKeyword, Math.min(100, result.get(titleKeyword) + titleWeight));
            } else if (result.size() < maxKeywords) {
                // 如果未满，直接添加
                result.put(titleKeyword, titleWeight);
            } else {
                // 如果已满，强制替换权重最小的项
                String minWeightKey = getMinWeightKey(result);
                if (minWeightKey != null) {
                    result.remove(minWeightKey);
                    result.put(titleKeyword, titleWeight);
                }
            }
        }
        
        // 合并重复词组和情节关键词
        mergeKeywordMaps(result, repeatedPhrases, maxKeywords);
        mergeKeywordMaps(result, plotKeywords, maxKeywords);
        
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
     * 从文本中提取命名实体（人名、地名、组织名等）
     */
    private Map<String, Integer> extractNamedEntities(List<Term> terms) {
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
                    int count = entityCounts.get(entity);
                    
                    // 权重计算：基础类型权重 + 出现频率加成
                    int score = typeWeight + Math.min(20, count * 2);
                    
                    // 更新最高分
                    if (!entityScores.containsKey(entity) || entityScores.get(entity) < score) {
                        entityScores.put(entity, score);
                    }
                }
            }
        }
        
        return entityScores;
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
     * 将源关键词映射合并到目标映射
     */
    private void mergeKeywordMaps(Map<String, Integer> target, Map<String, Integer> source, int maxKeywords) {
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            String keyword = entry.getKey();
            int weight = entry.getValue();
            
            if (target.containsKey(keyword)) {
                // 如果已存在，增加权重
                target.put(keyword, Math.min(100, target.get(keyword) + weight / 3));
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

    @Override
    public List<Map<String, Object>> analyzeCharacterRelationships(List<Map<String, String>> dialogues) {
        List<Map<String, Object>> relationships = new ArrayList<>();
        
        // 对话次数统计
        Map<String, Map<String, Integer>> dialogCounts = new HashMap<>();
        
        // 对话情感映射
        Map<String, Map<String, Double>> dialogSentiments = new HashMap<>();
        
        // 对话词汇共现统计
        Map<String, Map<String, Set<String>>> sharedWords = new HashMap<>();
        
        // 1. 统计对话数据
        for (Map<String, String> dialogue : dialogues) {
            String speaker = dialogue.get("speaker");
            String content = dialogue.get("content");
            
            if (speaker == null || content == null || speaker.isEmpty() || content.isEmpty()) {
                continue;
            }
            
            // 提取对话中提到的其他角色
            Set<String> mentionedCharacters = extractMentionedCharacters(content);
            
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
        
        // 2. 从对话中提取两两关系
        Set<String> allCharacters = new HashSet<>();
        for (Map<String, String> dialogue : dialogues) {
            allCharacters.add(dialogue.get("speaker"));
        }
        
        // 3. 生成关系数据
        for (String char1 : allCharacters) {
            for (String char2 : allCharacters) {
                if (char1.equals(char2)) {
                    continue;
                }
                
                // 计算交互频率
                int char1ToChar2 = dialogCounts.getOrDefault(char1, Collections.emptyMap())
                                            .getOrDefault(char2, 0);
                int char2ToChar1 = dialogCounts.getOrDefault(char2, Collections.emptyMap())
                                            .getOrDefault(char1, 0);
                
                // 忽略没有交互的角色对
                if (char1ToChar2 == 0 && char2ToChar1 == 0) {
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
                
                // 计算关系置信度
                double confidence = calculateConfidence(
                    char1ToChar2, char2ToChar1, averageSentiment);
                
                // 创建关系对象
                Map<String, Object> relation = new HashMap<>();
                relation.put("character1", char1);
                relation.put("character2", char2);
                relation.put("relationship", relationship);
                relation.put("confidence", confidence);
                relation.put("interactions", char1ToChar2 + char2ToChar1);
                relation.put("sentiment", averageSentiment);
                
                // 添加关键词特征
                if (!combinedWords.isEmpty()) {
                    relation.put("shared_topics", String.join(", ", 
                        combinedWords.stream().limit(5).collect(Collectors.toList())));
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

    /**
     * 基于交互频率、情感值和共享词汇确定关系类型
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
     * 计算关系的置信度
     */
    private double calculateConfidence(int interactions1to2, int interactions2to1, double sentiment) {
        // 基础置信度基于互动次数
        int totalInteractions = interactions1to2 + interactions2to1;
        double baseConfidence = Math.min(0.9, Math.log10(totalInteractions + 1) * 0.3);
        
        // 情感强度提升置信度
        double sentimentStrength = Math.abs(sentiment - 0.5) * 2;  // 转换为0-1的强度值
        
        // 综合置信度
        return Math.min(0.95, baseConfidence + sentimentStrength * 0.2);
    }
} 