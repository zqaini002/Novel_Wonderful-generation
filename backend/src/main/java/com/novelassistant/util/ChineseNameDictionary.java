package com.novelassistant.util;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 中文人名词典工具类，用于提高人名识别准确率
 */
@Component
public class ChineseNameDictionary {

    private static final Logger logger = LoggerFactory.getLogger(ChineseNameDictionary.class);

    // 常见姓氏集合
    private final Set<String> surnames = new HashSet<>();
    
    // 常见名字字符集合
    private final Set<String> nameCharacters = new HashSet<>();
    
    // 小说特有人名词典 (小说ID -> 人名集合)
    private final Map<Long, Set<String>> novelSpecificNames = new HashMap<>();
    
    // 角色别名映射 (本名 -> 别名集合)
    private final Map<String, Set<String>> characterAliases = new HashMap<>();

    @PostConstruct
    public void init() {
        loadSurnames();
        loadNameCharacters();
        logger.info("中文人名词典初始化完成，加载姓氏{}个，常见名字字符{}个", 
                surnames.size(), nameCharacters.size());
    }

    /**
     * 加载常见姓氏
     */
    private void loadSurnames() {
        // 常见姓氏列表
        String[] commonSurnames = {
            "李", "王", "张", "刘", "陈", "杨", "赵", "黄", "周", "吴", 
            "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗", 
            "梁", "宋", "郑", "谢", "韩", "唐", "冯", "于", "董", "萧", 
            "程", "曹", "袁", "邓", "许", "傅", "沈", "曾", "彭", "吕", 
            "苏", "卢", "蒋", "蔡", "贾", "丁", "魏", "薛", "叶", "阎", 
            "余", "潘", "杜", "戴", "夏", "钟", "汪", "田", "任", "姜", 
            "范", "方", "石", "姚", "谭", "廖", "邹", "熊", "金", "陆", 
            "郝", "孔", "白", "崔", "康", "毛", "邱", "秦", "江", "史", 
            "顾", "侯", "邵", "孟", "龙", "万", "段", "漕", "钱", "汤", 
            "尹", "黎", "易", "常", "武", "乔", "贺", "赖", "龚", "文",
            "欧阳", "太史", "端木", "上官", "司马", "东方", "独孤", "南宫", "万俟", "闻人",
            "夏侯", "诸葛", "尉迟", "公羊", "赫连", "澹台", "皇甫", "宗政", "濮阳", "公冶",
            "太叔", "申屠", "公孙", "慕容", "仲孙", "钟离", "长孙", "宇文", "司徒", "鲜于",
            "司空", "闾丘", "子车", "亓官", "司寇", "巫马", "公西", "颛孙", "壤驷", "公良",
            "漆雕", "乐正", "宰父", "谷梁", "拓跋", "夹谷", "轩辕", "令狐", "段干", "百里"
        };
        
        for (String surname : commonSurnames) {
            surnames.add(surname);
        }
        
        // 尝试从资源文件加载更多姓氏
        try (InputStream is = getClass().getResourceAsStream("/dictionaries/chinese_surnames.txt")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            surnames.add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("从资源文件加载姓氏失败: {}", e.getMessage());
        }
    }

    /**
     * 加载常见名字字符
     */
    private void loadNameCharacters() {
        // 常用于人名的字符
        String[] commonNameChars = {
            "伟", "刚", "勇", "毅", "俊", "峰", "强", "军", "平", "保", "东", "文", "辉", "力", "明", "永", "健", "世", "广", "志",
            "义", "兴", "良", "海", "山", "仁", "波", "宁", "贵", "福", "生", "龙", "元", "全", "国", "胜", "学", "祥", "才", "发",
            "武", "新", "利", "清", "飞", "彬", "富", "顺", "信", "子", "杰", "涛", "昌", "成", "康", "星", "光", "天", "达", "安",
            "岩", "中", "茂", "进", "林", "有", "坚", "和", "彪", "博", "诚", "先", "敬", "震", "振", "壮", "会", "思", "群", "豪",
            "心", "邦", "承", "乐", "绍", "功", "松", "善", "厚", "庆", "磊", "民", "友", "裕", "河", "哲", "江", "超", "浩", "亮",
            "政", "谦", "亨", "奇", "固", "之", "轮", "翰", "朗", "伯", "宏", "言", "若", "鸣", "朋", "斌", "梁", "栋", "维", "启",
            "克", "伦", "翔", "旭", "鹏", "泽", "晨", "辰", "士", "以", "建", "家", "致", "树", "炎", "德", "行", "时", "泰", "盛",
            "雄", "琛", "钧", "冠", "策", "腾", "榕", "风", "航", "弘", "秀", "娟", "英", "华", "慧", "巧", "美", "娜", "静", "淑",
            "惠", "珠", "翠", "雅", "芝", "玉", "萍", "红", "娥", "玲", "芬", "芳", "燕", "彩", "春", "菊", "兰", "凤", "洁", "梅",
            "琳", "素", "云", "莲", "真", "环", "雪", "荣", "爱", "妹", "霞", "香", "月", "莺", "媛", "艳", "瑞", "凡", "佳", "嘉",
            "琼", "勤", "珍", "贞", "莉", "桂", "娣", "叶", "璐", "璧", "雯", "蕾", "菁", "琪", "瑶", "慧", "巧", "柔", "蓉", "婉",
            "晴", "瑾", "岚", "妮", "丽", "君", "茜", "倩", "婵", "姣", "婧", "姬", "嫦", "婕", "姝", "娆", "婉", "姹"
        };
        
        for (String nameChar : commonNameChars) {
            nameCharacters.add(nameChar);
        }
        
        // 尝试从资源文件加载更多名字字符
        try (InputStream is = getClass().getResourceAsStream("/dictionaries/chinese_name_characters.txt")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            // 可能一行包含多个字符
                            for (int i = 0; i < line.length(); i++) {
                                nameCharacters.add(String.valueOf(line.charAt(i)));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("从资源文件加载名字字符失败: {}", e.getMessage());
        }
    }

    /**
     * 判断是否是常见姓氏
     * @param name 可能的姓氏
     * @return 是否是常见姓氏
     */
    public boolean isSurname(String name) {
        return surnames.contains(name);
    }
    
    /**
     * 判断字符是否常用于人名
     * @param character 单个字符
     * @return 是否常用于人名
     */
    public boolean isNameCharacter(String character) {
        return nameCharacters.contains(character);
    }
    
    /**
     * 添加小说特有人名
     * @param novelId 小说ID
     * @param name 人名
     */
    public void addNovelSpecificName(Long novelId, String name) {
        novelSpecificNames.computeIfAbsent(novelId, k -> new HashSet<>()).add(name);
        logger.debug("添加小说特有人名: 小说ID={}, 人名={}", novelId, name);
    }
    
    /**
     * 批量添加小说特有人名
     * @param novelId 小说ID
     * @param names 人名集合
     */
    public void addNovelSpecificNames(Long novelId, Set<String> names) {
        novelSpecificNames.computeIfAbsent(novelId, k -> new HashSet<>()).addAll(names);
        logger.debug("批量添加小说特有人名: 小说ID={}, 人名数量={}", novelId, names.size());
    }
    
    /**
     * 获取小说特有人名集合
     * @param novelId 小说ID
     * @return 人名集合
     */
    public Set<String> getNovelSpecificNames(Long novelId) {
        return novelSpecificNames.getOrDefault(novelId, new HashSet<>());
    }
    
    /**
     * 添加角色别名
     * @param realName 本名
     * @param alias 别名
     */
    public void addCharacterAlias(String realName, String alias) {
        characterAliases.computeIfAbsent(realName, k -> new HashSet<>()).add(alias);
    }
    
    /**
     * 获取角色所有别名
     * @param realName 本名
     * @return 别名集合
     */
    public Set<String> getCharacterAliases(String realName) {
        return characterAliases.getOrDefault(realName, new HashSet<>());
    }
    
    /**
     * 根据别名查找可能的本名
     * @param alias 别名
     * @return 可能的本名，如果没有匹配则返回null
     */
    public String findRealNameByAlias(String alias) {
        for (Map.Entry<String, Set<String>> entry : characterAliases.entrySet()) {
            if (entry.getValue().contains(alias)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * 评估字符串是否可能是人名
     * @param name 待评估的字符串
     * @return 人名可能性评分 (0-1)
     */
    public double evaluateNameLikelihood(String name) {
        if (name == null || name.isEmpty() || name.length() > 5) {
            return 0.0;
        }
        
        double score = 0.0;
        
        // 单字名评分较低
        if (name.length() == 1) {
            return isNameCharacter(name) ? 0.3 : 0.1;
        }
        
        // 检查姓氏
        String possibleSurname = name.substring(0, 1);
        if (name.length() >= 2 && (name.startsWith("欧阳") || name.startsWith("司马") || 
                name.startsWith("诸葛") || name.startsWith("上官") || 
                name.startsWith("东方") || name.startsWith("独孤"))) {
            possibleSurname = name.substring(0, 2);
            score += 0.5;
        } else if (isSurname(possibleSurname)) {
            score += 0.4;
        }
        
        // 检查名字部分
        int nameStartIndex = possibleSurname.length();
        double nameScore = 0.0;
        int nameChars = 0;
        
        for (int i = nameStartIndex; i < name.length(); i++) {
            String character = String.valueOf(name.charAt(i));
            if (isNameCharacter(character)) {
                nameChars++;
            }
        }
        
        if (name.length() - nameStartIndex > 0) {
            nameScore = (double) nameChars / (name.length() - nameStartIndex) * 0.6;
        }
        
        score += nameScore;
        
        return Math.min(1.0, score);
    }
    
    /**
     * 清除小说特有人名
     * @param novelId 小说ID
     */
    public void clearNovelSpecificNames(Long novelId) {
        novelSpecificNames.remove(novelId);
    }
} 