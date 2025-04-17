# 小说智析 - 可视化系统技术文档

本文档提供小说智析系统中可视化功能的技术细节，包括系统架构、数据流、组件说明和开发者指南。

## 目录

- [系统概述](#系统概述)
- [技术架构](#技术架构)
- [数据流程](#数据流程)
- [数据库设计](#数据库设计)
- [前端组件](#前端组件)
- [API接口](#api接口)
- [开发指南](#开发指南)
- [常见问题](#常见问题)
- [版本历史](#版本历史)

## 系统概述

小说可视化系统将小说文本数据转化为直观的图表呈现，帮助用户快速了解小说的关键特征。系统包含三种主要可视化类型：

1. **关键词云** - 展示小说中的高频词汇和关键概念
2. **情节波动图** - 显示小说情节的起伏和情感变化
3. **结构分析图** - 通过双层饼图展示小说的整体结构和章节分布

## 技术架构

### 前端技术栈

- **Vue.js 3** - 前端框架
- **Element Plus** - UI组件库
- **ECharts** - 可视化图表库
- **echarts-wordcloud** - 词云扩展

### 后端技术栈

- **Spring Boot** - 后端框架
- **JPA/Hibernate** - ORM框架
- **MySQL** - 数据库
- **Redis** (可选) - 缓存层

## 数据流程

1. **数据生成**：后端通过文本分析算法从小说内容中提取关键词、情感值和结构划分
2. **数据存储**：分析结果存储在专用的可视化数据表中
3. **数据缓存**：使用`visualization_cache`表缓存生成的数据，避免重复计算
4. **数据获取**：前端通过API请求获取所需的可视化数据
5. **数据渲染**：前端组件对数据进行格式化处理并使用ECharts渲染

## 数据库设计

### visualization_keywords表

```sql
CREATE TABLE IF NOT EXISTS visualization_keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    weight INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_keyword (keyword),
    UNIQUE KEY novel_keyword_idx (novel_id, keyword)
);
```

### visualization_emotional_data表

```sql
CREATE TABLE IF NOT EXISTS visualization_emotional_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    chapter_id BIGINT,
    chapter_number INT NOT NULL,
    chapter_title VARCHAR(255),
    emotion_value DOUBLE NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    is_climax_start BOOLEAN DEFAULT FALSE,
    is_climax_end BOOLEAN DEFAULT FALSE,
    event_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_chapter_number (chapter_number)
);
```

### visualization_structure_data表

```sql
CREATE TABLE IF NOT EXISTS visualization_structure_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    section_name VARCHAR(100) NOT NULL,
    percentage DOUBLE NOT NULL,
    start_chapter INT NOT NULL,
    end_chapter INT NOT NULL,
    chapter_count INT NOT NULL,
    description TEXT,
    color VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id)
);
```

### visualization_cache表

```sql
CREATE TABLE IF NOT EXISTS visualization_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    visualization_type VARCHAR(50) NOT NULL,
    data_json JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_type (visualization_type),
    UNIQUE KEY novel_vis_type_idx (novel_id, visualization_type)
);
```

## 前端组件

### KeywordCloud.vue

关键词云组件负责将关键词数据渲染为交互式词云图表。

**主要功能**：
- 接收关键词数据并格式化为ECharts所需格式
- 配置词云样式、颜色和布局
- 处理空数据情况并显示适当的提示
- 支持响应式调整大小

**代码示例**：
```javascript
// 格式化关键词数据
const formatKeywords = (keywords) => {
  if (!keywords || !Array.isArray(keywords) || keywords.length === 0) {
    return [];
  }
  
  return keywords.map(item => ({
    name: item.keyword || item.name,
    value: item.weight || item.value || 1
  }));
};
```

### PlotTrendChart.vue

情节波动图组件将章节情感值渲染为折线图，展示情节起伏。

**主要功能**：
- 接收情节波动数据并格式化
- 渲染主折线图和辅助指示线
- 标记重要情节点和高潮转折
- 支持图表交互和缩放

### StructureAnalysisChart.vue

结构分析图表组件使用双层饼图展示小说的整体结构和章节分布。

**主要功能**：
- 渲染内外两层环形图
- 格式化结构数据为ECharts所需格式
- 分配颜色和计算百分比
- 支持点击交互，展示详细信息

**代码示例**：
```javascript
// 格式化结构数据
const formatStructureData = (structureData) => {
  if (!structureData || Object.keys(structureData).length === 0) {
    return {
      mainData: [],
      detailData: [],
      legendData: []
    }
  }
  
  // 处理主要结构和详细结构
  const mainData = [];
  const detailData = [];
  const legendData = [];
  
  // ... 格式化逻辑
  
  return { mainData, detailData, legendData };
};
```

## API接口

### 获取关键词云数据

```
GET /novels/visualization/{novelId}/keywords
```

**响应示例**：
```json
[
  {"name": "魔法", "value": 128},
  {"name": "学院", "value": 86},
  {"name": "咒语", "value": 72},
  {"name": "魔杖", "value": 63},
  {"name": "巫师", "value": 58}
]
```

### 获取情节波动数据

```
GET /novels/visualization/{novelId}/emotional
```

**响应示例**：
```json
{
  "emotional": [
    {"chapter": 1, "title": "初入魔法世界", "value": 0.3},
    {"chapter": 2, "title": "神秘的信件", "value": 0.5},
    {"chapter": 3, "title": "魔法入学考试", "value": 0.7},
    {"chapter": 4, "title": "遭遇黑魔法", "value": 0.9}
  ],
  "climaxPoints": [3],
  "importantEvents": [{"chapter": 3, "description": "主角觉醒魔法天赋"}]
}
```

### 获取结构分析数据

```
GET /novels/visualization/{novelId}/structure
```

**响应示例**：
```json
{
  "mainStructure": [
    {"name": "引入", "value": 5},
    {"name": "发展", "value": 12},
    {"name": "高潮", "value": 8},
    {"name": "结局", "value": 5}
  ],
  "detailStructure": [
    {"name": "角色介绍", "value": 2, "startChapter": 1, "endChapter": 2},
    {"name": "世界设定", "value": 3, "startChapter": 3, "endChapter": 5},
    {"name": "初始冲突", "value": 3, "startChapter": 6, "endChapter": 8},
    {"name": "危机加剧", "value": 6, "startChapter": 9, "endChapter": 14},
    {"name": "高潮战斗", "value": 6, "startChapter": 15, "endChapter": 20},
    {"name": "结局", "value": 5, "startChapter": 21, "endChapter": 25}
  ]
}
```

### 获取所有可视化数据

```
GET /novels/visualization/{novelId}/all
```

**响应**：包含上述所有数据的组合

## 开发指南

### 添加新的可视化类型

1. **创建数据库表**：为新的可视化类型创建数据表
2. **添加实体类**：创建对应的JPA实体类
3. **扩展服务接口**：在`VisualizationService`中添加新方法
4. **实现服务方法**：在`VisualizationServiceImpl`中实现数据处理逻辑
5. **添加控制器端点**：在`VisualizationController`中添加新的API端点
6. **创建前端组件**：开发新的Vue组件来渲染可视化
7. **更新视图**：将新组件集成到`NovelVisualizationView.vue`中

### 常见问题解决

1. **ECharts组件未正确加载**：
   - 确保正确导入ECharts库和扩展
   - 检查DOM元素是否已挂载
   - 使用`nextTick`确保DOM渲染完成

2. **词云扩展问题**：
   - 确保`echarts-wordcloud`正确导入
   - 检查词云组件是否已注册
   - 验证ECharts版本兼容性

3. **数据格式化错误**：
   - 确保后端返回的数据结构符合预期
   - 添加适当的数据验证和格式化逻辑
   - 处理空数据和边缘情况

## 常见问题

### 加载ECharts词云组件失败

**问题**：加载或运行`echarts-wordcloud.min.js`时报错。

**解决方案**：
1. 确保依赖已正确安装：`npm install echarts-wordcloud --save`
2. 在组件中正确导入：`import 'echarts-wordcloud'`
3. 如果CDN加载失败，可以下载文件至本地`public/lib`目录并通过本地路径引用
4. 检查ECharts版本兼容性，确保使用兼容版本

### 可视化数据未显示

**问题**：图表组件加载但没有显示数据。

**解决方案**：
1. 检查API响应是否正确，使用浏览器开发工具观察网络请求
2. 验证前端组件是否正确处理接收到的数据格式
3. 检查是否存在数据格式化错误
4. 确认DOM元素有正确的尺寸（宽度和高度）

### 饼图颜色分配问题

**问题**：结构分析图表中的颜色分配不一致或不美观。

**解决方案**：
1. 在数据库中为每个结构段落预设颜色值
2. 使用一致的颜色生成逻辑
3. 为每个数据点明确分配颜色，而不是依赖ECharts自动分配

## 版本历史

### v1.0.0 (2025-04-18)
- 完成三种核心可视化组件：关键词云、情节波动图、结构分析图
- 实现可视化数据缓存机制
- 编写详细技术文档和组件使用指南
- 为结构分析图表组件添加全面注释

### v0.9.0 (2025-04-17)
- 修复ECharts库依赖问题
- 优化图表组件的加载效率
- 统一echarts导入和初始化方式
- 解决echarts-wordcloud组件兼容性问题

### v0.8.0 (2025-04-15)
- 初步实现三种可视化图表
- 设计并创建可视化数据表
- 开发后端API接口
- 构建前端数据获取服务

## 未来计划

### 即将推出 (v1.1.0)
- 人物关系网络图 - 展示小说主要角色之间的关系
- 字数分布统计图 - 分析章节长度分布
- 可视化数据导出功能 - 支持导出为图片或PDF

### 远期规划 (v2.0.0)
- 交互式情节时间线
- 地理位置可视化 (小说场景分布图)
- 多部小说对比分析
- 自定义可视化配置 