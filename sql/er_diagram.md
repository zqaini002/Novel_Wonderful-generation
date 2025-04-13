# 小说精读助手数据库 ER 图说明

## 核心实体关系

```
+------------------+       +-------------------+       +------------------+
|      novels      |1     *|     chapters      |1     *|  chapter_keywords|
+------------------+-------+-------------------+-------+------------------+
| id (PK)          |       | id (PK)           |       | id (PK)          |
| title            |       | novel_id (FK)     |       | chapter_id (FK)  |
| description      |       | chapter_number    |       | keyword          |
| author_name      |       | title             |       +------------------+
| source_url       |       | content           |
| total_chapters   |       | summary           |       +------------------+
| processed_chpt   |       | is_processed      +-------+------------------+
| created_at       |       | updated_at        |       | id (PK)          |
| updated_at       |       | created_at        |       | chapter_id (FK)  |
| processing_status|       | event_intensity   |       | character_name   |
| overall_summary  |       | chapter_type      |       +------------------+
| world_building   |       | word_count        |1     *|chapter_characters|
| character_dev    |       +-------------------+
| plot_progression |
+--------+---------+
         |1
         |
         |*
+--------v---------+       +------------------+
|    novel_tags    |1     *|   novel_keywords |
+------------------+       +------------------+
| id (PK)          |       | id (PK)          |
| novel_id (FK)    |       | novel_id (FK)    |
| name             |       | keyword          |
| description      |       +------------------+
| tag_type         |
| confidence_score |
| created_at       |
| updated_at       |
+------------------+

+------------------+       +------------------+       +------------------+
| analysis_results |1     *|  style_features  |       |  genre_features  |
+------------------+-------+------------------+       +------------------+
| id (PK)          |       | id (PK)          |       | id (PK)          |
| novel_id (FK)    |       | analysis_id (FK) |       | analysis_id (FK) |
| created_at       |       | feature_name     |       | feature_name     |
+--------+---------+       | score            |       | score            |
         |1                +------------------+       +------------------+
         |                        ^                          ^
         |                        |1                         |1
         |                        |                          |
         |                        |*                         |*
         |*                +------+------+             +-----+-------+
+--------v---------+       |potential_   |             |chapter_     |
|content_          |       |issues       |             |suggestions  |
|recommendations   |       +-------------+             +-------------+
+------------------+       | id (PK)     |             | id (PK)     |
| id (PK)          |       | analysis_id |             | analysis_id |
| analysis_id (FK) |       | issue_type  |             | sugg_type   |
| recomm_type      |       | chapter_num |             | reason      |
| recommendation   |       | chapter_titl|             | suggestion  |
+------------------+       | keyword     |             +-------------+
                           | suggestion  |
                           +-------------+
```

## 实体关系说明

### 小说与章节关系
- 一本小说(`novels`)包含多个章节(`chapters`) - 一对多关系
- 每个章节属于一本小说
- 章节按章节号(`chapter_number`)顺序排列

### 章节与关键词/人物关系
- 一个章节(`chapters`)可以有多个关键词(`chapter_keywords`) - 一对多关系
- 一个章节(`chapters`)可以出现多个人物(`chapter_characters`) - 一对多关系

### 小说与标签/关键词关系
- 一本小说(`novels`)可以有多个标签(`novel_tags`) - 一对多关系
- 一本小说(`novels`)可以有多个关键词(`novel_keywords`) - 一对多关系
- 标签分为推荐标签、避雷标签和阅读门槛标签三种类型

### 分析结果关系
- 一本小说(`novels`)可以有一个分析结果(`analysis_results`) - 一对一关系
- 一个分析结果包含多个风格特征(`style_features`) - 一对多关系
- 一个分析结果包含多个题材特征(`genre_features`) - 一对多关系
- 一个分析结果包含多个潜在问题(`potential_issues`) - 一对多关系
- 一个分析结果包含多个章节建议(`chapter_suggestions`) - 一对多关系
- 一个分析结果包含多个内容推荐(`content_recommendations`) - 一对多关系

## 数据流说明

1. 小说从文件上传或URL抓取后，创建`novels`记录
2. 文本处理服务提取章节，创建多条`chapters`记录
3. 每章内容分析后，提取关键词(`chapter_keywords`)和人物(`chapter_characters`)
4. 摘要生成服务为每章创建摘要，并计算事件强度
5. 决策支持引擎分析整本小说，生成`analysis_results`及相关分析数据
6. 根据分析结果，系统生成标签(`novel_tags`)、推荐/避雷提示等

## 主要索引

- `novels.title`: 用于小说标题搜索
- `novels.processing_status`: 用于查询处理状态
- `chapters.novel_id, chapters.chapter_number`: 用于章节顺序查询
- `novel_tags.novel_id, novel_tags.tag_type`: 用于按标签类型查询 