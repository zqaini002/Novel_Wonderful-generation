<template>
  <div class="novel-visualization-container">
    <div class="header">
      <div class="title-section">
        <el-button @click="goBack" size="small">
          <el-icon><Back /></el-icon>
          返回
        </el-button>
        <h2>{{ novel ? novel.title : '小说' }}可视化分析</h2>
      </div>
      <div class="action-buttons">
        <el-tooltip content="刷新数据" placement="top" :show-after="500">
          <el-button @click="refreshData" :loading="loading" circle size="small">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="下载当前图表" placement="top" :show-after="500">
          <el-button @click="downloadChart" circle size="small">
            <el-icon><Download /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="全屏显示" placement="top" :show-after="500">
          <el-button @click="toggleFullScreen" circle size="small">
            <el-icon><FullScreen /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
    
    <p v-if="novel" class="author-info">作者: {{ novel.author }}</p>
    
    <el-tabs v-model="activeTab" type="border-card" class="visualization-tabs">
      <el-tab-pane label="关键词云" name="keywords">
        <keyword-cloud 
          :keywords="keywordData" 
          :loading="loadingKeywords"
          title="小说关键词分布"
          height="500px"
          ref="keywordCloudRef"
        />
      </el-tab-pane>
      
      <el-tab-pane label="情节波动图" name="emotional">
        <plot-trend-chart 
          :plot-data="emotionalData" 
          :loading="loadingEmotional"
          title="情节波动趋势"
          height="500px"
          ref="plotTrendRef"
        />
      </el-tab-pane>
      
      <el-tab-pane label="结构分析" name="structure">
        <structure-analysis-chart 
          :structure-data="structureData" 
          :loading="loadingStructure"
          height="550px"
          ref="structureChartRef"
        />
      </el-tab-pane>
    </el-tabs>
    
    <!-- 加载中提示 -->
    <div v-if="loading" class="global-loading">
      <el-spinner size="large" />
      <p>正在加载可视化数据...</p>
    </div>
    
    <!-- 错误提示 -->
    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      @close="errorMessage = ''"
    />
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { KeywordCloud, PlotTrendChart, StructureAnalysisChart } from '@/components/visualization'
import visualizationService from '@/api/visualization'
import novelService from '@/services/novel'
import { ElMessage } from 'element-plus'
import { Back, Refresh, Download, FullScreen } from '@element-plus/icons-vue'

export default {
  name: 'NovelVisualizationView',
  components: {
    KeywordCloud,
    PlotTrendChart,
    StructureAnalysisChart,
    Back,
    Refresh,
    Download,
    FullScreen
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    
    // 当前活动的标签页
    const activeTab = ref('keywords');
    
    // 小说信息
    const novel = ref(null);
    const loading = ref(false);
    const errorMessage = ref('');
    
    // 可视化数据
    const keywordData = ref([]);
    const emotionalData = ref([]);
    const structureData = ref({});
    
    // 图表引用
    const keywordCloudRef = ref(null);
    const plotTrendRef = ref(null);
    const structureChartRef = ref(null);
    
    // 单项加载状态
    const loadingKeywords = ref(false);
    const loadingEmotional = ref(false);
    const loadingStructure = ref(false);
    
    // 返回上一页
    const goBack = () => {
      router.back();
    };
    
    // 刷新数据
    const refreshData = () => {
      const novelId = route.params.id ? parseInt(route.params.id) : null;
      if (novelId) {
        fetchNovelInfo(novelId);
        fetchAllVisualizationData(novelId);
        ElMessage.success('数据刷新中...');
      } else {
        ElMessage.warning('无法刷新，缺少小说ID');
      }
    };
    
    // 下载当前图表
    const downloadChart = () => {
      let chartInstance = null;
      let filename = 'chart';
      
      // 根据当前活动的标签页获取对应的图表实例
      switch(activeTab.value) {
        case 'keywords':
          chartInstance = keywordCloudRef.value?.chartRef?.chart;
          filename = '关键词云';
          break;
        case 'emotional':
          chartInstance = plotTrendRef.value?.chartRef?.chart;
          filename = '情节波动图';
          break;
        case 'structure':
          chartInstance = structureChartRef.value?.chartRef?.chart;
          filename = '结构分析图';
          break;
      }
      
      if (chartInstance) {
        try {
          // 获取图表的数据URL
          const dataUrl = chartInstance.getDataURL({
            pixelRatio: 2,
            backgroundColor: '#fff'
          });
          
          // 创建下载链接
          const link = document.createElement('a');
          link.download = `${novel.value?.title || '小说'}-${filename}.png`;
          link.href = dataUrl;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          
          ElMessage.success('图表已下载');
        } catch (error) {
          console.error('下载图表失败:', error);
          ElMessage.error('下载图表失败');
        }
      } else {
        ElMessage.warning('当前没有可下载的图表');
      }
    };
    
    // 全屏显示
    const toggleFullScreen = () => {
      // 获取当前图表容器
      let container = null;
      
      switch(activeTab.value) {
        case 'keywords':
          container = keywordCloudRef.value?.$el;
          break;
        case 'emotional':
          container = plotTrendRef.value?.$el;
          break;
        case 'structure':
          container = structureChartRef.value?.$el;
          break;
      }
      
      if (!container) {
        ElMessage.warning('无法进入全屏模式');
        return;
      }
      
      try {
        if (!document.fullscreenElement) {
          // 进入全屏
          if (container.requestFullscreen) {
            container.requestFullscreen();
          } else if (container.webkitRequestFullscreen) {
            container.webkitRequestFullscreen();
          } else if (container.msRequestFullscreen) {
            container.msRequestFullscreen();
          }
        } else {
          // 退出全屏
          if (document.exitFullscreen) {
            document.exitFullscreen();
          } else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
          } else if (document.msExitFullscreen) {
            document.msExitFullscreen();
          }
        }
      } catch (error) {
        console.error('全屏切换失败:', error);
        ElMessage.error('全屏切换失败');
      }
    };
    
    // 获取小说信息
    const fetchNovelInfo = async (novelId) => {
      try {
        const response = await novelService.getNovelById(novelId);
        novel.value = response.data;
      } catch (error) {
        console.error('获取小说信息失败:', error);
        errorMessage.value = '获取小说信息失败: ' + (error.response?.data?.error || error.message);
      }
    };
    
    // 获取关键词云数据
    // eslint-disable-next-line no-unused-vars
    const fetchKeywordData = async (novelId) => {
      loadingKeywords.value = true;
      try {
        const response = await visualizationService.getKeywordCloudData(novelId);
        keywordData.value = response.data || [];
      } catch (error) {
        console.error('获取关键词云数据失败:', error);
        errorMessage.value = '获取关键词云数据失败: ' + (error.response?.data?.error || error.message);
      } finally {
        loadingKeywords.value = false;
      }
    };
    
    // 获取情节波动图数据
    // eslint-disable-next-line no-unused-vars
    const fetchEmotionalData = async (novelId) => {
      loadingEmotional.value = true;
      try {
        const response = await visualizationService.getEmotionalFluctuationData(novelId);
        // 如果数据格式正确，解析emotional字段
        emotionalData.value = response.data && response.data.emotional ? 
                             response.data.emotional : 
                             response.data || [];
      } catch (error) {
        console.error('获取情节波动图数据失败:', error);
        errorMessage.value = '获取情节波动图数据失败: ' + (error.response?.data?.error || error.message);
      } finally {
        loadingEmotional.value = false;
      }
    };
    
    // 获取结构分析数据
    // eslint-disable-next-line no-unused-vars
    const fetchStructureData = async (novelId) => {
      loadingStructure.value = true;
      try {
        const response = await visualizationService.getStructureAnalysisData(novelId);
        structureData.value = response.data || {};
      } catch (error) {
        console.error('获取结构分析数据失败:', error);
        errorMessage.value = '获取结构分析数据失败: ' + (error.response?.data?.error || error.message);
      } finally {
        loadingStructure.value = false;
      }
    };
    
    // 一次性获取所有可视化数据
    const fetchAllVisualizationData = async (novelId) => {
      loading.value = true;
      errorMessage.value = '';
      loadingKeywords.value = true;
      loadingEmotional.value = true;
      loadingStructure.value = true;
      
      try {
        // 尝试获取所有数据，如果失败则单独获取各部分
        try {
          const response = await visualizationService.getAllVisualizationData(novelId);
          const data = response.data;
          
          // 解析各项数据
          if (data.keywords) {
            keywordData.value = data.keywords;
          }
          
          if (data.emotional) {
            emotionalData.value = data.emotional;
          }
          
          if (data.structure) {
            structureData.value = data.structure;
          }
        } catch (error) {
          console.warn('获取所有数据失败，尝试单独获取各部分:', error);
          
          // 单独获取各部分数据
          await Promise.allSettled([
            (async () => {
              try {
                const response = await visualizationService.getKeywordCloudData(novelId);
                keywordData.value = response.data || [];
              } catch (e) {
                console.error('获取关键词云数据失败:', e);
              }
            })(),
            (async () => {
              try {
                const response = await visualizationService.getEmotionalFluctuationData(novelId);
                emotionalData.value = response.data && response.data.emotional ? 
                                     response.data.emotional : 
                                     response.data || [];
              } catch (e) {
                console.error('获取情节波动图数据失败:', e);
              }
            })(),
            (async () => {
              try {
                const response = await visualizationService.getStructureAnalysisData(novelId);
                structureData.value = response.data || {};
              } catch (e) {
                console.error('获取结构分析数据失败:', e);
              }
            })()
          ]);
        }
      } catch (error) {
        console.error('获取可视化数据失败:', error);
        errorMessage.value = '获取可视化数据失败: ' + (error.response?.data?.error || error.message);
      } finally {
        loading.value = false;
        loadingKeywords.value = false;
        loadingEmotional.value = false;
        loadingStructure.value = false;
      }
    };
    
    // 当路由参数变化时重新加载数据
    watch(() => route.params.id, (newId) => {
      if (newId) {
        const novelId = parseInt(newId);
        fetchNovelInfo(novelId);
        fetchAllVisualizationData(novelId);
      }
    });
    
    // 组件挂载时加载数据
    onMounted(() => {
      const novelId = route.params.id ? parseInt(route.params.id) : null;
      if (novelId) {
        fetchNovelInfo(novelId);
        fetchAllVisualizationData(novelId);
      }
    });
    
    return {
      novel,
      loading,
      errorMessage,
      keywordData,
      emotionalData,
      structureData,
      loadingKeywords,
      loadingEmotional,
      loadingStructure,
      activeTab,
      keywordCloudRef,
      plotTrendRef,
      structureChartRef,
      goBack,
      refreshData,
      downloadChart,
      toggleFullScreen
    };
  }
};
</script>

<style scoped>
.novel-visualization-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-section h2 {
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.author-info {
  margin: 0 0 20px 0;
  color: #606266;
  font-size: 14px;
}

.visualization-tabs {
  margin-top: 20px;
}

.visualization-tabs :deep(.el-tabs__content) {
  padding: 20px;
}

.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.global-loading p {
  margin-top: 10px;
  color: #606266;
}

/* 全屏模式样式 */
:deep(.chart-container) {
  transition: all 0.3s ease;
}

:fullscreen :deep(.chart-container) {
  width: 100vw !important;
  height: 100vh !important;
  background-color: white;
  padding: 20px;
  box-sizing: border-box;
}

:fullscreen :deep(.keyword-cloud-chart),
:fullscreen :deep(.plot-trend-chart),
:fullscreen :deep(.structure-chart) {
  height: 90vh !important;
}
</style> 