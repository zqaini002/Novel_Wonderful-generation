<template>
  <div class="novel-detail-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ name: 'home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>小说详情</el-breadcrumb-item>
      <el-breadcrumb-item>{{ novel?.title || '加载中...' }}</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        title="获取数据失败"
        :sub-title="error"
      >
        <template #extra>
          <el-button type="primary" @click="fetchData">重试</el-button>
          <el-button @click="$router.push({ name: 'home' })">返回首页</el-button>
        </template>
      </el-result>
    </div>
    
    <template v-else>
      <div class="novel-header">
        <div class="card-header">
          <span class="title">{{ novel.title }}</span>
          <div class="tags">
            <el-tag v-if="novel.processingStatus === 'PROCESSING'" type="warning">处理中</el-tag>
            <el-tag v-else-if="novel.processingStatus === 'COMPLETED'" type="success">已完成</el-tag>
          </div>
          <span class="author">作者: {{ novel.author }}</span>
        </div>
        <p class="description">{{ novel.description }}</p>
      </div>
      
      <el-alert
        v-if="isDemo"
        title="当前处于演示模式，显示的是预设数据，而非实际处理结果"
        type="warning"
        class="demo-notice"
        show-icon
        :closable="false"
      />
      
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="摘要分析" name="summary">
          <div class="summary-section">
            <h3>总体摘要</h3>
            <div class="summary-content">
              <p>{{ novel.overallSummary }}</p>
            </div>
            
            <h3>世界观设定</h3>
            <div class="summary-content">
              <p>{{ novel.worldBuildingSummary }}</p>
            </div>
            
            <h3>人物发展</h3>
            <div class="summary-content">
              <p>{{ novel.characterDevelopmentSummary }}</p>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="章节列表" name="chapters">
          <div class="chapters-section" v-if="showChaptersTable">
            <el-table :data="novel.chapters" style="width: 100%" height="450" v-loading="chaptersLoading">
              <el-table-column prop="chapterNumber" label="章节" width="80" />
              <el-table-column prop="title" label="标题" />
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button link @click="viewChapterSummary(scope.row)">查看摘要</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="标签分析" name="tags">
          <div class="tags-section">
            <h3>推荐特点</h3>
            <div class="tags-group">
              <el-tag 
                v-for="tag in getTagsByType('POSITIVE')" 
                :key="tag.id" 
                type="success" 
                effect="dark">
                {{ tag.name }}
              </el-tag>
            </div>
            
            <h3>潜在问题</h3>
            <div class="tags-group">
              <el-tag 
                v-for="tag in getTagsByType('WARNING')" 
                :key="tag.id" 
                type="warning" 
                effect="dark">
                {{ tag.name }}
              </el-tag>
            </div>
            
            <h3>阅读门槛</h3>
            <div class="tags-group">
              <el-tag 
                v-for="tag in getTagsByType('INFO')" 
                :key="tag.id" 
                type="info" 
                effect="dark">
                {{ tag.name }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="数据可视化" name="visualization">
          <div class="visualization-section">
            <el-empty description="数据分析中，敬请期待..." />
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
    
    <el-dialog v-model="dialogVisible" title="章节摘要" width="50%" :append-to-body="true" destroy-on-close>
      <div v-if="selectedChapter">
        <h4>{{ selectedChapter.title }}</h4>
        <p>{{ selectedChapter.summary }}</p>
        
        <h5>关键词</h5>
        <div class="chapter-keywords">
          <el-tag 
            v-for="keyword in selectedChapter.keywords" 
            :key="keyword" 
            size="small">
            {{ keyword }}
          </el-tag>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="dialogVisible = false">查看详情</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import novelApi from '@/api/novel'

export default {
  name: 'NovelDetailView',
  setup() {
    const route = useRoute()
    
    const loading = ref(true)
    const error = ref(null)
    const dialogVisible = ref(false)
    const selectedChapter = ref(null)
    const activeTab = ref('summary')
    const showChaptersTable = ref(false)
    const chaptersLoading = ref(false)
    
    // 小说数据
    const novel = ref(null)
    
    // 定义变量
    const novelId = computed(() => Number(route.params.id))
    const isDemo = computed(() => {
      return route.query.demo === 'true' || route.query.mode === 'demo'
    })
    
    // 根据类型获取标签
    const getTagsByType = (type) => {
      if (!novel.value || !novel.value.tags) return []
      return novel.value.tags.filter(tag => tag.type === type)
    }
    
    // 获取小说详情
    const fetchNovelDetails = async () => {
      try {
        loading.value = true
        error.value = null
        
        if (!novelId.value) {
          error.value = '未指定小说ID'
          return
        }

        // 如果是演示模式，延迟加载以模拟网络请求
        if (isDemo.value) {
          await new Promise(resolve => setTimeout(resolve, 1000))
        }
        
        const response = await novelApi.getNovelDetail(novelId.value)
        novel.value = response
        
      } catch (err) {
        console.error('获取小说详情失败:', err)
        error.value = '获取小说详情失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }
    
    const viewChapterSummary = (chapter) => {
      selectedChapter.value = chapter
      setTimeout(() => {
        dialogVisible.value = true
      }, 50)
    }
    
    // 监听标签页切换，延迟加载章节表格
    watch(activeTab, (newVal) => {
      if (newVal === 'chapters' && !showChaptersTable.value) {
        chaptersLoading.value = true
        setTimeout(() => {
          showChaptersTable.value = true
          chaptersLoading.value = false
        }, 100)
      }
    })
    
    onMounted(() => {
      fetchNovelDetails()
    })
    
    return {
      novel,
      loading,
      error,
      fetchData: fetchNovelDetails,
      dialogVisible,
      selectedChapter,
      viewChapterSummary,
      activeTab,
      showChaptersTable,
      chaptersLoading,
      getTagsByType,
      isDemo
    }
  }
}
</script>

<style scoped>
.novel-detail-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.loading-container, .error-container {
  margin: 50px 0;
}

.novel-header {
  margin: 30px 0;
}

.novel-header h1 {
  margin-bottom: 15px;
}

.novel-meta {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.author {
  margin-right: 15px;
  color: #606266;
}

.description {
  line-height: 1.6;
  color: #303133;
}

.summary-section,
.tags-section,
.visualization-section,
.chapters-section {
  padding: 20px 0;
}

.summary-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  line-height: 1.6;
}

h3 {
  margin: 20px 0 10px;
  color: #303133;
}

.tags-group {
  margin-bottom: 20px;
}

.tags-group .el-tag {
  margin-right: 10px;
  margin-bottom: 10px;
}

.chapter-keywords {
  margin-top: 10px;
}

.chapter-keywords .el-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.card-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-right: 10px;
}

.demo-tag {
  margin-right: 10px;
}

.author {
  font-size: 0.9rem;
  color: #606266;
  margin-left: auto;
}

.demo-notice {
  margin-bottom: 16px;
}

.mb-3 {
  margin-bottom: 1rem;
}
</style> 