# 📚 NovelSight 小说智析 | Novel Assistant1

<div align="center">
  <img src="https://img.shields.io/badge/版本-v0.9.0-blue" alt="Version">
  <img src="https://img.shields.io/badge/开发阶段-beta-orange" alt="Status">
  <img src="https://img.shields.io/badge/许可证-MIT-green" alt="License">
  <img src="https://img.shields.io/badge/构建工具-Maven%2FNpm-red" alt="Build Tools">
  <br><br>
  <p>
      <strong>AI驱动的小说内容分析与摘要生成系统</strong>
  </p>
  <p>
      <a href="#核心功能">💡 功能</a> •
      <a href="#快速开始">🚀 快速开始</a> •
      <a href="#系统架构">🏗️ 架构</a> •
      <a href="#技术栈">⚙️ 技术栈</a> •
      <a href="#项目进度">📊 进度</a> •
      <a href="#联系方式">QQ: 1610494022</a>
  </p>
# NovelSight 小说智析 (Novel Assistant)


<div align="center">
    <p>
        <strong>AI驱动的小说内容分析与摘要生成系统</strong>
    </p>
    <p>
        <a href="#核心功能">功能</a> •
        <a href="#快速开始">快速开始</a> •
        <a href="#系统架构">架构</a> •
        <a href="#演示">演示</a> •
        <a href="#贡献指南">贡献</a>
    </p>
</div>



## 🌟 项目简介

> **让小说阅读更高效，内容理解更深入**
联系方式:
        qq:1610494022
### 💬 那些年读小说踩过的坑

我在看小说时遇到的问题，让我决定做这个软件
一、那些年读小说踩过的坑

- 作为一个资深小说爱好者，我在阅读中频繁遇到这些困扰：
- 筛选难：面对书架上几百本小说，不知道哪本 "不踩雷"——
- 简介写得天花乱坠，实际剧情拖沓、人设崩塌，比如某科幻小说前 10 章都在堆砌技术术语，主线剧情推进极慢。
- 想找 "反套路" 小说，却只能靠网友评论盲选，耗时又低效。

二、理解累：

- 复杂世界观小说（如修真体系、星际设定），章节内容零散，看完记不住核心设定，比如某玄幻小说里 300 个门派设定，根本理不清脉络。
- 长章节小说（如百万字网文），中间穿插大量填充内容，想跳过 "水章" 却担心错过主线，比如某历史小说每 5 章就有 2 章在描写无关的配角日常。
  -无论您是想快速了解一部新小说，还是希望深入分析您喜爱的作品，小说精读助手都能提供帮助。

身为资深小说爱好者，我在阅读中频繁遇到这些困扰：

#### 📋 筛选困难

- 面对书架上几百本小说，不知道哪本"不踩雷"
- 简介写得天花乱坠，实际剧情拖沓、人设崩塌
- 想找"反套路"小说，却只能靠网友评论盲选，耗时又低效

#### 📖 理解费力

- 复杂世界观小说（如修真体系、星际设定），章节内容零散，理不清核心设定
- 长篇小说中穿插大量填充内容，想跳过"水章"却担心错过主线剧情

**NovelSight** 通过AI技术解析小说内容，提取关键情节，生成摘要和标签，为您省去筛选和理解的烦恼。无论是快速了解一部新小说，还是深入分析您喜爱的作品，小说智析都能提供专业帮助。

## 📅 开发日志

### 2025年4月21日
| 时间 | 工作内容 | 状态 |
|------|---------|------|
| **09:00-11:30** | 修复数据库字段长度限制问题<br>修复EmotionalData实体中event_description列定义<br>修改Chapter实体中summary字段定义为MEDIUMTEXT | ✅ |
| **13:30-15:00** | 优化文本处理流程<br>添加长文本自动截断功能<br>避免数据库存储溢出 | ✅ |
| **15:30-17:00** | 测试小说上传和处理功能<br>验证文本截断逻辑<br>确认数据库表结构更新 | ✅ |
| **17:00-18:30** | 代码优化与问题排查<br>更新技术文档和开发日志<br>记录字段长度问题解决方案 | ✅ |

### 2025年4月20日
| 时间 | 工作内容 | 状态 |
|------|---------|------|
| **09:00-11:30** | 前端代码清理与优化<br>删除冗余的AnalyzeView组件<br>更新路由系统移除/analyze路由 | ✅ |
| **13:30-15:30** | 修复上传小说后的页面跳转逻辑<br>添加lastUploadedNovelId状态管理<br>优化状态轮询和错误处理 | ✅ |
| **16:00-17:00** | 重构小说可视化流程<br>统一使用novel/:id/visualization路由<br>更新相关组件中的goToAnalyze方法 | ✅ |
| **17:00-18:30** | 增强NLP服务对话提取功能<br>改进中文引号识别正则表达式<br>实现extractDialogues方法支持更准确的角色识别 | ✅ |

### 2025年4月18日
| 时间 | 工作内容 | 状态 |
|------|---------|------|
| **09:00-11:30** | 编写可视化系统技术文档<br>详细记录系统架构和数据流程<br>补充数据库表设计说明 | ✅ |
| **13:30-15:30** | 为结构分析图表组件添加详细注释<br>创建可视化组件目录说明文档<br>补充API接口文档和使用示例 | ✅ |
| **16:00-17:30** | 更新主README文档可视化部分<br>添加可视化系统常见问题解决方案<br>校对所有文档确保一致性 | ✅ |

### 2025年4月17日
| 时间 | 工作内容 | 状态 |
|------|---------|------|
| **09:00-11:30** | 修复前端可视化组件的ECharts库依赖问题<br>更新所有图表组件直接导入echarts | ✅ |
| **13:30-15:30** | 优化小说结构分析图表组件<br>修复关键词云组件wordcloud依赖问题<br>统一echarts导入和初始化方式 | ✅ |
| **16:00-17:30** | 调整echarts引用<br>成功构建前端项目验证修复<br>解决编译警告并提升加载性能<br>优化系统日志格式与存储方式 | ✅ |

### 2025年4月15日
| 时间 | 工作内容 | 状态 |
|------|---------|------|
| **09:00-11:00** | 修复前端API调用问题<br>修正用户统计接口响应处理<br>优化MyNovelsView组件错误处理 | ✅ |
| **13:30-15:30** | 升级Spring Boot至3.x版本<br>更新所有javax.*包引用为jakarta.*<br>修复JwtUtils兼容性问题 | ✅ |
| **16:00-17:30** | 增强错误处理和日志记录<br>添加API响应调试功能<br>实现数据模型默认值设置 | ✅ |

### 2025年4月14日
| 时间            | 工作内容                                                     | 状态 |
| --------------- | ------------------------------------------------------------ | ---- |
| **09:30-11:30** | 解决Spring Security循环依赖问题<br>修复AccessDeniedException错误 | ✅    |
| **13:00-15:00** | 完善管理员仪表盘路由配置<br>创建NovelManagementView和SystemLogsView组件<br>修复ESLint错误，优化代码结构 | ✅    |
| **15:30-17:30** | 实现用户登录状态管理和动态显示<br>优化Vuex存储，改进组件显示<br>完善登录流程，基于角色跳转 | ✅    |
| **17:30-18:00** | 代码优化与清理<br>美化登录按钮和用户下拉菜单样式             | ✅    |

## 📊 项目进度

### ✅ 已完成内容

<table>
  <tr>
    <td><b>基础架构搭建</b></td>
    <td>
      ✓ 前端框架搭建 (Vue.js 3 + Element Plus)<br>
      ✓ 后端框架搭建 (Spring Boot 3.2.4)<br>
      ✓ 数据库配置 (MySQL 8.2.0)<br>
      ✓ 基本API设计和实现
    </td>
  </tr>
  <tr>
    <td><b>用户认证系统</b></td>
    <td>
      ✓ 用户注册/登录功能<br>
      ✓ JWT令牌认证<br>
      ✓ 基于角色的权限控制<br>
      ✓ 登录状态管理和动态显示用户信息
    </td>
  </tr>
  <tr>
    <td><b>管理员后台</b></td>
    <td>
      ✓ 管理员仪表盘<br>
      ✓ 用户管理界面<br>
      ✓ 小说管理界面<br>
      ✓ 系统日志查看界面
    </td>
  </tr>
  <tr>
    <td><b>基础UI组件</b></td>
    <td>
      ✓ 导航栏和页面布局<br>
      ✓ 登录和注册表单<br>
      ✓ 用户下拉菜单<br>
      ✓ 小说列表显示
    </td>
  </tr>
  <tr>
    <td><b>核心业务流程</b></td>
    <td>
      ✓ 小说文件上传功能<br>
      ✓ 简单的章节识别与提取<br>
      ✓ 小说处理状态跟踪<br>
      ✓ 基本的小说详情查看<br>
      ✓ 用户小说统计功能<br>
      ✓ 我的小说列表展示<br>
      ✓ 文本处理安全性增强（长文本自动截断）
    </td>
  </tr>
  <tr>
    <td><b>系统安全性</b></td>
    <td>
      ✓ 解决Spring Security循环依赖问题<br>
      ✓ 修复AccessDeniedException错误<br>
      ✓ 优化认证授权流程<br>
      ✓ 升级至Spring Boot 3.x兼容<br>
      ✓ 增强API错误处理机制<br>
      ✓ 数据库字段长度溢出问题修复
    </td>
  </tr>
  <tr>
    <td><b>可视化系统</b></td>
    <td>
      ✓ 关键词云实现 (基于ECharts-wordcloud)<br>
      ✓ 情节波动图绘制 (基于ECharts折线图)<br>
      ✓ 小说结构分析可视化 (双层饼图)<br>
      ✓ 人物关系网络可视化 (基于力导向图)<br>
      ✓ 可视化数据库设计与实现<br>
      ✓ 可视化API接口开发<br>
      ✓ 可视化技术文档编写
    </td>
  </tr>
  <tr>
    <td><b>技术文档</b></td>
    <td>
      ✓ 系统架构文档<br>
      ✓ API接口文档<br>
      ✓ 可视化组件使用指南<br>
      ✓ 开发者文档<br>
      ✓ 问题排查指南
    </td>
  </tr>
</table>


### 🔄 待完成内容

<table>
  <tr>
    <th>类别</th>
    <th>任务</th>
    <th>优先级</th>
  </tr>
  <tr>
    <td rowspan="3"><b>核心功能强化</b></td>
    <td>增强中文网络小说的章节识别</td>
    <td>高</td>
  </tr>
  <tr>
    <td>改进摘要生成算法，减少重复内容</td>
    <td>高</td>
  </tr>
  <tr>
    <td>实现关键词提取和标签生成</td>
    <td>高</td>
  </tr>
  <tr>
    <td rowspan="2"><b>数据可视化增强</b></td>
    <td>实现字数分布统计图表</td>
    <td>中</td>
  </tr>
  <tr>
    <td>添加可视化数据导出功能</td>
    <td>低</td>
  </tr>
  <tr>
    <td rowspan="3"><b>用户体验优化</b></td>
    <td>添加用户偏好设置</td>
    <td>中</td>
  </tr>
  <tr>
    <td>实现暗黑模式</td>
    <td>低</td>
  </tr>
  <tr>
    <td>优化移动端响应式设计</td>
    <td>中</td>
  </tr>
  <tr>
    <td rowspan="3"><b>高级分析功能</b></td>
    <td>小说内容情感分析</td>
    <td>低</td>
  </tr>
  <tr>
    <td>写作风格识别</td>
    <td>低</td>
  </tr>
  <tr>
    <td>小说相似度比较功能</td>
    <td>低</td>
  </tr>
  <tr>
    <td rowspan="3"><b>系统性能优化</b></td>
    <td>大文件处理性能优化</td>
    <td>中</td>
  </tr>
  <tr>
    <td>数据库查询优化</td>
    <td>高</td>
  </tr>
  <tr>
    <td>前端资源加载优化</td>
    <td>低</td>
  </tr>
  <tr>
    <td rowspan="3"><b>部署与CI/CD</b></td>
    <td>容器化部署方案</td>
    <td>中</td>
  </tr>
  <tr>
    <td>自动化测试流程</td>
    <td>中</td>
  </tr>
  <tr>
    <td>持续集成与部署流程</td>
    <td>低</td>
  </tr>
</table>


## 💡 核心功能

### 🔍 自动化文本处理

- 🗃️ **多源输入**: 支持TXT、EPUB格式文件上传和网页链接解析
- 📑 **章节识别**: 自动识别章节标题，提取正文内容，过滤广告
- 🔄 **批量处理**: 支持批量导入和处理多部小说

### 📝 智能摘要生成

- 📌 **分层摘要**: 提供章节摘要、情节脉络图和全书概述
- 🧠 **内容理解**: 通过自然语言处理理解小说情节和主题
- 📊 **结构化输出**: 将非结构化文本转换为结构化信息

### 📊 小说数据可视化

- 🔍 **关键词云**: 直观展示小说中的高频词汇和关键概念，帮助快速把握核心主题
  - 基于词频和重要性权重自动生成
  - 支持自定义颜色和布局
  - 响应式设计，适配各种屏幕尺寸

- 📈 **情节波动图**: 展示小说情节起伏和情感变化
  - 可视化故事情节的紧张度和情感强度
  - 标记重要剧情点和高潮转折
  - 帮助读者理解叙事结构和节奏

- 🥧 **结构分析图表**: 通过双层饼图展示小说的整体结构和章节分布
  - 内层圆环展示主要结构（开端、发展、高潮、结局等）
  - 外层圆环展示详细分段（各子情节、转折点等）
  - 交互式设计，点击查看各部分详情

- 👨‍👩‍👧‍👦 **人物关系网络图**: 可视化小说中角色之间的关系
  - 基于力导向图展示人物关系
  - 节点大小反映角色重要性
  - 连接线表示角色之间的关系类型和强度
  - 支持交互式探索，鼠标悬停查看角色简介
  - 自适应布局，智能排布复杂关系网络
  - 自动识别对话内容中的角色关系
  - 优化内存使用，支持大量角色节点显示

- ⚙️ **技术实现**:
  - 基于ECharts可视化库构建的响应式图表
  - 前后端分离架构，数据缓存优化
  - 结构化数据存储，支持实时更新

- 🔗 **API接口**:
  - `/novels/visualization/{novelId}/keywords`: 获取关键词云数据
  - `/novels/visualization/{novelId}/emotional`: 获取情节波动数据
  - `/novels/visualization/{novelId}/structure`: 获取结构分析数据
  - `/novels/visualization/{novelId}/characters`: 获取人物关系网络数据
  - `/novels/visualization/{novelId}/all`: 获取所有可视化数据

### 🏷️ 个性化标签系统

- 👍 **推荐标签**: 突出小说的积极特点
- ⚠️ **避雷标签**: 提示小说的潜在问题
- 🚪 **阅读门槛**: 说明阅读所需的前置知识

## ⚙️ 技术栈

### 后端技术详情

#### 🔧 核心框架

* **JDK**: Oracle JDK 11.0.21 (LTS版本)
  * Modular System Support
  * Enhanced String API
  * Improved Garbage Collector
  * Sealed Classes
* **Spring生态系统**:
  * Spring Boot 3.2.4
    * Auto-configuration
    * Embedded Server (Tomcat 10.1.x)
    * Production-ready Features
    * Jakarta EE 10 Support
  * Spring Framework 6.1.x
    * IoC Container
    * AOP Support
    * WebMVC Framework
  * Spring Security 6.2.x
    * Authentication & Authorization
    * Password Encryption (BCrypt)
    * CORS Configuration
  * Spring Data JPA 3.2.x
    * Repository Pattern
    * Dynamic Query Generation
    * Audit Features

#### 💾 数据持久层

* **ORM框架**: 
  * Hibernate 6.4.x
    * Second-level Cache
    * Lazy Loading
    * Batch Processing
* **数据库**: 
  * MySQL 8.2.0
    * InnoDB引擎
    * UTF8MB4字符集
    * 优化索引

#### 🔐 安全框架

* **JWT**: jjwt 0.11.5
  * Stateless Authentication
  * Configurable Expiration
  * Refresh Token Support
* **加密算法**:
  * BCrypt (10轮加密)
  * HTTPS/TLS 1.3
* **安全防护**:
  * XSS防护
  * CSRF防护
  * SQL注入防护

#### 📝 文档处理

* **文本解析**:
  * JSoup 1.16.2 (HTML解析)
  * Apache PDFBox 3.0.1
  * Apache POI 5.2.5 (Office文档)
* **自然语言处理**:
  * HanLP portable-1.8.4
    * 中文分词
    * 命名实体识别
    * 关键词提取
  * OpenNLP 2.3.1

### 前端技术详情

#### 🎨 核心框架

* **Vue.js 3.2.47**:
  * Composition API
  * 响应式系统
  * 虚拟DOM
* **状态管理**:
  * Vuex 4.0.2
    * 状态持久化
    * 模块化管理
* **路由管理**:
  * Vue Router 4.1.6
    * 动态路由
    * 路由守卫
    * 懒加载

#### 🎯 UI框架

* **Element Plus 2.3.5**:
  * 响应式布局
  * 主题定制
  * 国际化支持
* **可视化**:
  * ECharts 5.4.2
    * 图表组件
    * 数据可视化
  * D3.js 7.8.5

#### 🔄 工具链

* **构建工具**:
  * Vite 4.3.9
    * 快速热重载
    * 按需编译
* **包管理**:
  * Node.js 16.20.1
  * npm 8.19.4
* **代码质量**:
  * ESLint 8.41.0
  * Prettier 2.8.8
* **测试框架**:
  * Jest 29.5.0
  * Vue Test Utils 2.3.2

### 🛠️ 开发与部署工具

#### 构建工具

* **Maven 3.8.8**:
  * 依赖管理
  * 生命周期管理
  * 多模块构建
* **Gradle 7.6.1** (可选):
  * 增量构建
  * 构建缓存

#### 容器化

* **Docker 24.0.2**:
  * 多阶段构建
  * 容器编排
* **Docker Compose 2.18.1**:
  * 服务定义
  * 环境配置

#### CI/CD

* **Jenkins 2.401.1**:
  * 流水线即代码
  * 自动化部署
* **Git 2.40.1**:
  * 版本控制
  * 分支管理

#### 监控工具

* **Spring Boot Actuator**:
  * 健康检查
  * 指标收集
* **Prometheus + Grafana**:
  * 性能监控
  * 可视化面板

## 🏗️ 系统架构

```mermaid
graph TD
    A[前端 Vue.js] -->|API请求| B[后端 Spring Boot]
    B -->|数据存储| C[MySQL数据库]
    B -->|文本处理| D[NLP处理模块]
    D -->|返回分析结果| B
    B -->|返回数据| A
    
    style A fill:#b8e0d2,stroke:#5d9b9b,stroke-width:2px
    style B fill:#f0d0b0,stroke:#d09a45,stroke-width:2px
    style C fill:#c3b8e0,stroke:#8778ba,stroke-width:2px
    style D fill:#f0b8e0,stroke:#ba5d9b,stroke-width:2px
```

## 🚀 快速开始

### 📋 系统要求

- ☕ Java 11.0+ (推荐使用JDK 11.0.21)
- 🟢 Node.js 16+ (推荐使用16.20.1)
- 🐬 MySQL 8.0+
- 🏗️ Maven 3.6+

### 🔧 安装步骤

<details>
<summary><b>1. 配置数据库</b></summary>
1. **配置数据库**
```bash
# 创建数据库和表结构
mysql -u root -p < sql/schema.sql


# 可选：导入示例数据

mysql -u root -p < sql/sample_data.sql

```
<details>
<summary><b>2. 配置后端</b></summary>

2. **配置后端**
```bash
cd backend
# 修改application.properties中的数据库连接信息
mvn clean install
```

</details>

<details>
<summary><b>3. 启动后端服务</b></summary>
3. **启动后端服务**
```bash
mvn spring-boot:run
```
<details>
<summary><b>4. 安装和启动前端</b></summary>


4. **安装和启动前端**

```bash
cd ../frontend
npm install
npm run serve
```

<details>
<summary><b>5. 访问应用</b></summary>
打开浏览器访问 http://localhost:8081
📱 使用流程


<div align="center">
  <table>
    <tr>
      <td align="center"><b>步骤1</b><br>登录/注册</td>
      <td align="center"><b>步骤2</b><br>上传小说</td>
      <td align="center"><b>步骤3</b><br>等待处理</td>
      <td align="center"><b>步骤4</b><br>浏览分析</td>
      <td align="center"><b>步骤5</b><br>保存/分享</td>
    </tr>
    <tr>
      <td align="center">👤</td>
      <td align="center">📤</td>
      <td align="center">⏳</td>
      <td align="center">📊</td>
      <td align="center">💾</td>
    </tr>
  </table>
</div>


## 🎯 性能指标

- 📦 支持单文件最大**150MB** (约300万字)
- ⚡ 平均处理速度: **100万字/5分钟**
- 📈 摘要准确率: **>85%** (基于人工评估)

## 🔮 版本历史

- **v1.1.0** (2025-04-20): 人物关系网络与代码优化版本
  - 实现人物关系网络可视化功能
  - 增强NLP服务对话提取能力
  - 重构前端路由系统，移除冗余组件
  - 优化小说上传与处理流程
  - 改进状态管理和错误处理机制

- **v1.0.0** (2025-04-18): 可视化功能版本
  - 完成三种核心可视化组件
  - 编写详细技术文档和使用指南
  - 优化性能和响应式布局

- **v0.9.0** (2025-04-14): 基础版发布
  - 完成基础架构搭建
  - 实现用户认证系统
  - 开发管理员后台
  - 构建基础UI组件
  - 实现核心业务流程

## 📝 贡献指南

我们欢迎各种形式的贡献，包括但不限于:

- 🐛 提交Bug报告和功能请求
- 📚 改进文档
- 💻 提交代码修复或新功能
- 🔍 分享使用体验和建议

### 编码规范

- 后端代码遵循Google Java代码规范
- 前端代码遵循Vue.js风格指南

## 📜 许可证

本项目采用MIT许可证。详见 [LICENSE](LICENSE) 文件。

## 📞 联系方式

- 项目维护者: [七七](mailto:tanqi03@126.com)

---

<div align="center">
  <sub>用❤️构建 | MIT许可证</sub>
</div>


## ⚙️ Technical Stack (English Version)

### Backend Technical Details

#### 🔧 Core Framework

* **JDK**: Oracle JDK 11.0.21 (LTS Version)
  * Modular System Support
  * Enhanced String API
  * Improved Garbage Collector
* **Spring Ecosystem**:
  * Spring Boot 2.7.12
    * Auto-configuration
    * Embedded Server (Tomcat 9.0.73)
    * Production-ready Features
  * Spring Framework 5.3.27
    * IoC Container
    * AOP Support
    * WebMVC Framework
  * Spring Security 5.7.8
    * Authentication & Authorization
    * Password Encryption (BCrypt)
    * CORS Configuration
  * Spring Data JPA 2.7.12
    * Repository Pattern
    * Dynamic Query Generation
    * Audit Features

#### 💾 Data Persistence Layer

* **ORM Framework**: 
  * Hibernate 5.6.15.Final
    * Second-level Cache
    * Lazy Loading
    * Batch Processing
* **Database**: 
  * MySQL 8.0.33
    * InnoDB Engine
    * UTF8MB4 Character Set
    * Optimized Indexes

#### 🔐 Security Framework

* **JWT**: jjwt 0.11.5
  * Stateless Authentication
  * Configurable Expiration
  * Refresh Token Support
* **Encryption**:
  * BCrypt (10 Rounds)
  * HTTPS/TLS 1.3
* **Security Protection**:
  * XSS Protection
  * CSRF Protection
  * SQL Injection Protection

#### 📝 Document Processing

* **Text Parsing**:
  * JSoup 1.16.1 (HTML Parsing)
  * Apache PDFBox 2.0.27
  * Apache POI 5.2.3 (Office Documents)
* **Natural Language Processing**:
  * HanLP portable-1.8.4
    * Chinese Word Segmentation
    * Named Entity Recognition
    * Keyword Extraction
  * OpenNLP 1.9.4

### Frontend Technical Details

#### 🎨 Core Framework

* **Vue.js 3.2.47**:
  * Composition API
  * Reactive System
  * Virtual DOM
* **State Management**:
  * Vuex 4.0.2
    * State Persistence
    * Modular Management
* **Routing**:
  * Vue Router 4.1.6
    * Dynamic Routing
    * Route Guards
    * Lazy Loading

#### 🎯 UI Framework

* **Element Plus 2.3.5**:
  * Responsive Layout
  * Theme Customization
  * Internationalization
* **Visualization**:
  * ECharts 5.4.2
    * Chart Components
    * Data Visualization
  * D3.js 7.8.5

#### 🔄 Tool Chain

* **Build Tools**:
  * Vite 4.3.9
    * Fast Hot Reload
    * On-demand Compilation
* **Package Management**:
  * Node.js 16.20.1
  * npm 8.19.4
* **Code Quality**:
  * ESLint 8.41.0
  * Prettier 2.8.8
* **Testing Framework**:
  * Jest 29.5.0
  * Vue Test Utils 2.3.2

### 🛠️ Development & Deployment Tools

#### Build Tools

* **Maven 3.8.8**:
  * Dependency Management
  * Lifecycle Management
  * Multi-module Builds
* **Gradle 7.6.1** (Optional):
  * Incremental Builds
  * Build Cache

#### Containerization

* **Docker 24.0.2**:
  * Multi-stage Builds
  * Container Orchestration
* **Docker Compose 2.18.1**:
  * Service Definition
  * Environment Configuration

#### CI/CD

* **Jenkins 2.401.1**:
  * Pipeline as Code
  * Automated Deployment
* **Git 2.40.1**:
  * Version Control
  * Branch Management

#### Monitoring Tools

* **Spring Boot Actuator**:
  * Health Checks
  * Metrics Collection
* **Prometheus + Grafana**:
  * Performance Monitoring
  * Visualization Dashboard

---

<div align="center">
  <sub>Built with ❤️ | MIT License</sub>
</div> 

