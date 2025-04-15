<template>
  <div class="home-container">
    <section class="hero-section">
      <div class="hero-content">
        <h1>小说智析</h1>
        <p class="subtitle">AI驱动的小说内容分析与摘要生成系统</p>
        <p class="description">快速了解小说内容，做出明智的阅读决策</p>
        
        <div class="action-buttons">
          <el-button type="primary" size="large" @click="$router.push('/upload')">立即体验</el-button>
          <el-button size="large" @click="$router.push('/about')">了解更多</el-button>
        </div>
      </div>
      <div class="hero-image">
        <!-- 使用Element Plus图标代替图片 -->
        <el-icon :size="280" color="#409EFF"><ReadingLamp /></el-icon>
      </div>
    </section>
    
    <section class="features-section">
      <h2>主要功能</h2>
      <div class="features-container">
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>自动化文本处理</span>
            </div>
          </template>
          <div class="card-content">
            <p>支持多种来源的小说内容导入，包括网页链接解析和本地文件上传（TXT/EPUB格式）。</p>
            <p>自动识别章节标题，过滤广告内容，提取有效文本。</p>
          </div>
        </el-card>
        
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon><Memo /></el-icon>
              <span>智能摘要生成</span>
            </div>
          </template>
          <div class="card-content">
            <p>单章摘要：提取每章核心情节，约200字左右。</p>
            <p>情节脉络图：对小说内容进行主题聚类分析，展示故事脉络。</p>
            <p>全书概述：整合前100章内容，生成完整故事大纲。</p>
          </div>
        </el-card>
        
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon><PriceTag /></el-icon>
              <span>个性化标签系统</span>
            </div>
          </template>
          <div class="card-content">
            <p>推荐标签：突出小说的积极特点。</p>
            <p>避雷标签：提示小说的潜在问题。</p>
            <p>阅读门槛标签：说明需要的前置知识。</p>
            <p>根据读者反馈不断优化标签精准度。</p>
          </div>
        </el-card>
        
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据可视化分析</span>
            </div>
          </template>
          <div class="card-content">
            <p>关键词云：展示小说关键词及其出现频率。</p>
            <p>情节波动图：图形化展示故事情节起伏变化。</p>
            <p>风格分析：多维度分析小说的文风特点。</p>
          </div>
        </el-card>
      </div>
    </section>
    
    <section class="novels-section" v-if="novels.length > 0">
      <h2>热门小说</h2>
      <div class="novels-container">
        <el-card v-for="novel in novels" :key="novel.id" class="novel-card" 
                @click="$router.push(`/novel/${novel.id}`)">
          <div class="novel-info">
            <h3>{{ novel.title }}</h3>
            <p class="author">作者：{{ novel.author }}</p>
            <el-progress 
              :text-inside="true" 
              :stroke-width="16" 
              :percentage="novel.totalChapters && novel.totalChapters > 0 ? Math.round((novel.processedChapters || 0) / novel.totalChapters * 100) : 0"
              :status="novel.processedChapters >= novel.totalChapters ? 'success' : ''"
            />
            <p class="chapters">
              已处理: {{ novel.processedChapters || 0 }}/{{ novel.totalChapters || 0 }} 章
            </p>
          </div>
        </el-card>
      </div>
    </section>
    
    <section class="cta-section">
      <h2>开始探索您的小说</h2>
      <p>上传您的小说文件或输入网页链接，让AI为您生成摘要和分析</p>
      <el-button type="primary" size="large" @click="$router.push('/upload')">立即开始</el-button>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { Document, Memo, PriceTag, DataAnalysis, ReadingLamp } from '@element-plus/icons-vue'

export default {
  name: 'HomeView',
  components: {
    Document,
    Memo,
    PriceTag,
    DataAnalysis,
    ReadingLamp
  },
  setup() {
    const store = useStore()
    const loading = ref(false)
    const novels = computed(() => store.state.novels)
    
    onMounted(async () => {
      // 获取小说列表
      loading.value = true
      try {
        await store.dispatch('fetchNovels')
      } catch (error) {
        console.error('获取小说列表失败:', error)
      } finally {
        loading.value = false
      }
    })
    
    return {
      novels,
      loading
    }
  }
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-section {
  display: flex;
  align-items: center;
  padding: 60px 0;
  min-height: 400px;
}

.hero-content {
  flex: 1;
}

.hero-content h1 {
  font-size: 42px;
  margin-bottom: 20px;
  color: #303133;
}

.subtitle {
  font-size: 24px;
  color: #606266;
  margin-bottom: 15px;
}

.description {
  font-size: 18px;
  color: #909399;
  margin-bottom: 30px;
}

.action-buttons {
  margin-top: 25px;
  display: flex;
  gap: 15px;
  align-items: center;
  justify-content: center;
}

.hero-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.features-section,
.novels-section,
.cta-section {
  padding: 60px 0;
  text-align: center;
}

.features-section h2,
.novels-section h2,
.cta-section h2 {
  font-size: 32px;
  margin-bottom: 40px;
  color: #303133;
}

.features-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  margin-top: 40px;
}

.feature-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
}

.card-header .el-icon {
  margin-right: 10px;
  font-size: 20px;
  color: #409EFF;
}

.card-header span {
  font-size: 18px;
  font-weight: bold;
}

.card-content {
  text-align: left;
  color: #606266;
  line-height: 1.6;
}

.novels-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 40px;
}

.novel-card {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.novel-card:hover {
  transform: translateY(-5px);
}

.novel-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.novel-info .author {
  color: #909399;
  margin-bottom: 15px;
}

.novel-info .chapters {
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}

.cta-section {
  background-color: #f5f7fa;
  padding: 60px 20px;
  border-radius: 8px;
  margin-bottom: 40px;
}

.cta-section p {
  color: #606266;
  margin-bottom: 30px;
  font-size: 18px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}
</style> 