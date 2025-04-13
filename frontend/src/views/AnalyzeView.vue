<template>
  <div class="analyze-container">
    <el-card class="analyze-card">
      <template #header>
        <div class="card-header">
          <h2>小说处理中</h2>
        </div>
      </template>
      
      <div class="progress-info">
        <el-progress :percentage="percentage" :status="progressStatus"></el-progress>
        <p class="status-text">{{ statusText }}</p>
        
        <div class="details-container" v-if="processingStatus !== 'FAILED'">
          <div class="detail-item">
            <h4>处理章节</h4>
            <p>{{ currentChapter }} / {{ totalChapters }}</p>
          </div>
          <div class="detail-item">
            <h4>预计完成时间</h4>
            <p>{{ estimatedTime }}</p>
          </div>
        </div>
        
        <div class="error-container" v-if="processingStatus === 'FAILED'">
          <el-alert
            title="处理失败"
            type="error"
            description="小说处理过程中出现错误，请重试或联系客服。"
            show-icon
          >
          </el-alert>
          <el-button type="primary" @click="retry" class="retry-btn">重新尝试</el-button>
        </div>
      </div>
      
      <div class="action-buttons" v-if="processingStatus === 'COMPLETED'">
        <el-button type="primary" @click="viewResults">查看分析结果</el-button>
        <el-button @click="backToHome">返回首页</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import novelApi from '@/api/novel'

export default {
  name: 'AnalyzeView',
  setup() {
    const store = useStore()
    const router = useRouter()
    
    // 处理状态相关变量
    const processingStatus = computed(() => store.state.processingStatus || 'PROCESSING')
    const percentage = computed(() => store.getters.processingPercentage)
    const currentChapter = ref(0)
    const totalChapters = ref(10)
    const estimatedTime = ref('约1-2分钟')
    
    // 根据处理状态显示不同的状态文本
    const statusText = computed(() => {
      switch (processingStatus.value) {
        case 'UPLOADING': return '正在上传小说文件...'
        case 'PROCESSING': return '正在分析小说内容...'
        case 'COMPLETED': return '分析完成！'
        case 'FAILED': return '处理失败'
        default: return '准备中...'
      }
    })
    
    // 进度条状态
    const progressStatus = computed(() => {
      if (processingStatus.value === 'COMPLETED') return 'success'
      if (processingStatus.value === 'FAILED') return 'exception'
      return ''
    })
    
    // 状态轮询定时器
    let interval = null
    
    onMounted(() => {
      // 获取URL中的novelId参数
      const urlParams = new URLSearchParams(window.location.search);
      const novelId = urlParams.get('id') || 3; // 默认ID
      
      // 设置状态轮询
      setupStatusPolling(novelId);
    });
    
    onBeforeUnmount(() => {
      if (interval) {
        clearInterval(interval)
      }
    })
    
    // 轮询获取处理状态
    const setupStatusPolling = (novelId) => {
      // 如果没有novelId，不进行轮询
      if (!novelId) {
        console.warn('没有提供novelId，无法进行状态轮询');
        return;
      }
      
      interval = setInterval(async () => {
        try {
          const response = await novelApi.getNovelStatus(novelId);
          store.commit('SET_PROCESSING_STATUS', response.status.toUpperCase());
          
          // 更新进度信息
          if (response.totalChapters) {
            totalChapters.value = response.totalChapters;
          }
          
          if (response.processedChapters) {
            currentChapter.value = response.processedChapters;
          }
          
          if (response.status === 'COMPLETED' || response.status === 'FAILED') {
            clearInterval(interval);
          }
        } catch (error) {
          console.error('获取处理状态失败', error);
        }
      }, 2000);
    }
    
    // 查看结果
    const viewResults = () => {
      // 获取URL中的novelId参数
      const urlParams = new URLSearchParams(window.location.search);
      const novelId = urlParams.get('id') || 3;
      router.push({ name: 'novel-detail', params: { id: novelId } })
    }
    
    // 返回首页
    const backToHome = () => {
      router.push({ name: 'home' })
    }
    
    // 重试
    const retry = () => {
      store.commit('SET_PROCESSING_STATUS', 'PROCESSING')
      currentChapter.value = 0
      
      // 获取URL中的novelId参数
      const urlParams = new URLSearchParams(window.location.search);
      const novelId = urlParams.get('id') || 3;
      
      // 重新设置轮询
      setupStatusPolling(novelId);
    }
    
    return {
      processingStatus,
      percentage,
      currentChapter,
      totalChapters,
      estimatedTime,
      statusText,
      progressStatus,
      viewResults,
      backToHome,
      retry
    }
  }
}
</script>

<style scoped>
.analyze-container {
  max-width: 800px;
  margin: 40px auto;
}

.analyze-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.progress-info {
  padding: 20px;
}

.status-text {
  text-align: center;
  font-size: 16px;
  margin: 20px 0;
  color: #606266;
}

.details-container {
  display: flex;
  justify-content: space-around;
  margin: 30px 0;
}

.detail-item {
  text-align: center;
}

.detail-item h4 {
  margin-bottom: 8px;
  color: #909399;
}

.error-container {
  margin: 20px 0;
}

.retry-btn {
  margin-top: 20px;
  width: 100%;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}
</style> 