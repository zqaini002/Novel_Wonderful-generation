<template>
  <div class="about-container">
    <div class="about-header">
      <h1>关于小说智析</h1>
      <p class="subtitle">AI驱动的小说内容分析与摘要生成系统</p>
    </div>
    
    <el-card class="about-card">
      <template #header>
        <div class="card-header">
          <h2>项目介绍</h2>
        </div>
      </template>
      
      <div class="about-content">
        <p>在信息爆炸的时代，我们面临着越来越多的内容选择，但时间却十分有限。尤其对于小说爱好者而言，如何在众多作品中快速找到符合自己口味的好书，成为了一个现实问题。</p>
        <p>小说智析是一个基于人工智能的小说内容分析与摘要生成系统，旨在帮助读者快速了解小说内容并做出阅读决策。</p>
        <p>通过先进的自然语言处理技术，我们能够自动化分析小说文本，提取核心情节，生成分层级摘要，并提供多维度的内容评价和标签，让用户在短时间内把握小说的精髓。</p>
      </div>
    </el-card>
    
    <el-card class="about-card">
      <template #header>
        <div class="card-header">
          <h2>核心功能</h2>
        </div>
      </template>
      
      <div class="about-content">
        <div class="feature-item">
          <div class="feature-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="feature-details">
            <h3>自动化文本处理</h3>
            <p>支持网页链接解析和本地文件上传（TXT/EPUB格式），自动识别章节标题，过滤广告内容，提取正文。</p>
          </div>
        </div>
        
        <div class="feature-item">
          <div class="feature-icon">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="feature-details">
            <h3>分层级智能摘要生成</h3>
            <p>单章摘要：提取每章200字左右的核心情节；情节脉络图：对内容进行主题聚类分析；全书概述：整合内容，生成完整故事大纲。</p>
          </div>
        </div>
        
        <div class="feature-item">
          <div class="feature-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="feature-details">
            <h3>辅助决策功能</h3>
            <p>关键词云和情节波动图可视化展示，个性化推荐/避雷标签系统，互动式筛选工具，助您做出阅读决策。</p>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card class="about-card">
      <template #header>
        <div class="card-header">
          <h2>技术实现</h2>
        </div>
      </template>
      
      <div class="about-content">
        <div class="tech-section">
          <h3>前端技术栈</h3>
          <div class="tech-items">
            <el-tag>Vue.js 3</el-tag>
            <el-tag>Element Plus</el-tag>
            <el-tag>ECharts</el-tag>
            <el-tag>Vuex</el-tag>
          </div>
        </div>
        
        <div class="tech-section">
          <h3>后端技术栈</h3>
          <div class="tech-items">
            <el-tag>Spring Boot 2.7</el-tag>
            <el-tag>MySQL 8.0</el-tag>
            <el-tag>HanLP</el-tag>
            <el-tag>JSoup</el-tag>
            <el-tag>Epublib</el-tag>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card class="about-card">
      <template #header>
        <div class="card-header">
          <h2>联系我们</h2>
        </div>
      </template>
      
      <div class="about-content">
        <p>有任何问题、建议或合作意向，欢迎通过以下方式联系我：</p>
        <p><el-icon><Message /></el-icon> 邮箱：1610494022@qq.com</p>
        <p><el-icon><Promotion /></el-icon> 微信公众号：没得公众号</p>
        
        <div class="support-section">
          <el-button type="primary" round>提交反馈</el-button>
          <el-button type="success" round>加入内测</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { Document, Reading, DataAnalysis, Message, Promotion } from '@element-plus/icons-vue'

export default {
  name: 'AboutView',
  components: {
    Document,
    Reading,
    DataAnalysis,
    Message,
    Promotion
  }
}
</script>

<style scoped>
.about-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.about-header {
  text-align: center;
  margin: 40px 0;
}

.subtitle {
  color: #909399;
  font-size: 18px;
  margin-top: 10px;
}

.about-card {
  margin-bottom: 30px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.about-content {
  line-height: 1.8;
  color: #303133;
}

.feature-item {
  display: flex;
  margin-bottom: 25px;
}

.feature-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 60px;
  background-color: #ecf5ff;
  border-radius: 50%;
  margin-right: 20px;
  flex-shrink: 0;
}

.feature-icon .el-icon {
  font-size: 28px;
  color: #409EFF;
}

.feature-details h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.tech-section {
  margin-bottom: 20px;
}

.tech-items {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.support-section {
  margin-top: 25px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style> 