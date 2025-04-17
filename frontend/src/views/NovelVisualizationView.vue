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
      <el-icon class="is-loading" :size="30"><loading /></el-icon>
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
import { Back, Refresh, Download, FullScreen, Loading } from '@element-plus/icons-vue'

export default {
  name: 'NovelVisualizationView',
  components: {
    KeywordCloud,
    PlotTrendChart,
    StructureAnalysisChart,
    Back,
    Refresh,
    Download,
    FullScreen,
    Loading
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
          console.log('获取到的可视化数据响应:', response);
          
          // 处理返回数据
          if (response) {
            // 处理关键词数据
            if (Array.isArray(response.keywords)) {
              keywordData.value = response.keywords;
            }
            
            // 处理情感波动数据
            if (response.emotional) {
              emotionalData.value = response.emotional;
            }
            
            // 处理结构分析数据
            if (response.structure) {
              structureData.value = response.structure;
            }
          } else {
            console.warn('未获取到可视化数据，服务器返回空响应');
            // 如果服务器返回空响应，使用演示数据
            keywordData.value = generateDemoKeywords();
            emotionalData.value = generateDemoPlotTrend();
            structureData.value = generateDemoStructureData();
          }
        } catch (error) {
          console.warn('获取所有数据失败，尝试单独获取各部分:', error);
          
          // 单独获取各部分数据
          await Promise.allSettled([
            (async () => {
              try {
                const response = await visualizationService.getKeywordCloudData(novelId);
                console.log('获取关键词云数据响应:', response);
                if (response && Array.isArray(response)) {
                  keywordData.value = response;
                } else {
                  console.warn('关键词云数据格式不正确:', response);
                  keywordData.value = generateDemoKeywords(); // 使用演示数据
                }
              } catch (e) {
                console.error('获取关键词云数据失败:', e);
                keywordData.value = generateDemoKeywords(); // 使用演示数据
              }
            })(),
            (async () => {
              try {
                const response = await visualizationService.getEmotionalFluctuationData(novelId);
                console.log('获取情节波动图数据响应:', response);
                // 处理多种可能的返回格式
                if (response) {
                  if (Array.isArray(response)) {
                    emotionalData.value = response;
                  } else if (response.emotional && Array.isArray(response.emotional)) {
                    emotionalData.value = response.emotional;
                  } else if (typeof response === 'object') {
                    emotionalData.value = response;
                  } else {
                    emotionalData.value = generateDemoPlotTrend(); // 使用演示数据
                  }
                } else {
                  emotionalData.value = generateDemoPlotTrend(); // 使用演示数据
                }
              } catch (e) {
                console.error('获取情节波动图数据失败:', e);
                emotionalData.value = generateDemoPlotTrend(); // 使用演示数据
              }
            })(),
            (async () => {
              try {
                const response = await visualizationService.getStructureAnalysisData(novelId);
                console.log('获取结构分析数据响应:', response);
                if (response && typeof response === 'object') {
                  structureData.value = response;
                } else {
                  console.warn('结构分析数据格式不正确:', response);
                  structureData.value = generateDemoStructureData(); // 使用演示数据
                }
              } catch (e) {
                console.error('获取结构分析数据失败:', e);
                structureData.value = generateDemoStructureData(); // 使用演示数据
              }
            })()
          ]);
        }
      } catch (error) {
        console.error('获取可视化数据失败:', error);
        errorMessage.value = '获取可视化数据失败: ' + (error.response?.data?.error || error.message);
        
        // 使用演示数据作为后备
        keywordData.value = generateDemoKeywords();
        emotionalData.value = generateDemoPlotTrend();
        structureData.value = generateDemoStructureData();
      } finally {
        loading.value = false;
        loadingKeywords.value = false;
        loadingEmotional.value = false;
        loadingStructure.value = false;
      }
    };
    
    // 生成演示关键词数据
    const generateDemoKeywords = () => {
      return [
        { name: '主角', value: 100 },
        { name: '爱情', value: 85 },
        { name: '冒险', value: 70 },
        { name: '战斗', value: 65 },
        { name: '友情', value: 62 },
        { name: '背叛', value: 58 },
        { name: '成长', value: 54 },
        { name: '复仇', value: 52 },
        { name: '魔法', value: 50 },
        { name: '宿命', value: 48 },
        { name: '奇幻', value: 45 },
        { name: '阴谋', value: 40 },
        { name: '正义', value: 38 },
        { name: '邪恶', value: 35 },
        { name: '王国', value: 32 },
        { name: '家族', value: 30 },
        { name: '战争', value: 28 },
        { name: '和平', value: 25 },
        { name: '勇气', value: 22 },
        { name: '智慧', value: 20 },
        { name: '牺牲', value: 18 },
        { name: '羁绊', value: 16 },
        { name: '命运', value: 15 },
        { name: '生死', value: 12 },
        { name: '传说', value: 10 }
      ];
    };
    
    // 生成演示情节波动数据
    const generateDemoPlotTrend = () => {
      const chapters = [];
      for (let i = 1; i <= 30; i++) {
        // 基础情感值
        let emotion = 0;
        
        // 创建典型的小说情节曲线
        if (i < 5) {
          // 开头平缓
          emotion = 20 + Math.random() * 10;
        } else if (i < 10) {
          // 初步上升
          emotion = 30 + (i - 5) * 5 + Math.random() * 10;
        } else if (i < 15) {
          // 第一个小高潮
          emotion = 50 + Math.sin((i - 10) * 0.8) * 20 + Math.random() * 10;
        } else if (i < 20) {
          // 中间起伏
          emotion = 40 + Math.sin((i - 15) * 0.5) * 15 + Math.random() * 15;
        } else if (i < 25) {
          // 逐渐攀升到高潮
          emotion = 50 + (i - 20) * 8 + Math.random() * 10;
        } else {
          // 最后的高潮和结局
          emotion = 90 - (i - 25) * 6 + Math.random() * 10;
        }
        
        const isImportant = i === 14 || i === 24; // 重要章节标记
        
        // 处理特殊章节
        const isClimaxStart = i === 22;
        const isClimaxEnd = i === 27;
        
        chapters.push({
          chapterNumber: `第${i}章`,
          emotion: Math.round(emotion),
          isImportant,
          isClimaxStart,
          isClimaxEnd,
          event: isImportant ? `章节${i}关键事件` : ''
        });
      }
      return chapters;
    };
    
    // 生成演示结构数据
    const generateDemoStructureData = () => {
      return {
        mainStructure: [
          { name: '开端', value: 5, color: '#5470c6' },
          { name: '铺垫', value: 7, color: '#91cc75' },
          { name: '发展', value: 10, color: '#fac858' },
          { name: '高潮', value: 5, color: '#ee6666' },
          { name: '结局', value: 3, color: '#73c0de' }
        ],
        detailStructure: [
          { name: '角色介绍', value: 2, color: '#5470c6' },
          { name: '世界设定', value: 3, color: '#91cc75' },
          { name: '初始冲突', value: 2, color: '#6492ed' },
          { name: '情节递进', value: 5, color: '#fac858' },
          { name: '次要冲突', value: 3, color: '#ee6666' },
          { name: '关系发展', value: 4, color: '#a065d5' },
          { name: '主要冲突', value: 3, color: '#73c0de' },
          { name: '危机', value: 2, color: '#3ba272' },
          { name: '转折点', value: 1, color: '#fc8452' },
          { name: '高潮', value: 3, color: '#9a60b4' },
          { name: '解决', value: 2, color: '#ea7ccc' },
          { name: '结局', value: 1, color: '#457fd6' }
        ]
      };
    };
    
    // 当路由参数变化时重新加载数据
    watch(() => route.params.id, (newId) => {
      if (newId) {
        try {
          const novelId = parseInt(newId);
          if (isNaN(novelId)) {
            console.error('非法的小说ID:', newId);
            errorMessage.value = '无效的小说ID';
            return;
          }
          console.log('路由参数变化，加载小说ID:', novelId);
          fetchNovelInfo(novelId);
          fetchAllVisualizationData(novelId);
        } catch (error) {
          console.error('处理路由参数失败:', error);
          errorMessage.value = '处理路由参数失败';
        }
      }
    });
    
    // 组件挂载时加载数据
    onMounted(() => {
      if (route.params.id) {
        try {
          const novelId = parseInt(route.params.id);
          if (isNaN(novelId)) {
            console.error('非法的小说ID:', route.params.id);
            errorMessage.value = '无效的小说ID';
            return;
          }
          console.log('组件挂载，加载小说ID:', novelId);
          fetchNovelInfo(novelId);
          fetchAllVisualizationData(novelId);
        } catch (error) {
          console.error('处理路由参数失败:', error);
          errorMessage.value = '处理路由参数失败';
        }
      } else {
        console.error('缺少小说ID');
        errorMessage.value = '缺少小说ID';
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
  position: relative;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-section h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.author-info {
  margin-top: -15px;
  margin-bottom: 20px;
  color: #606266;
}

.visualization-tabs {
  margin-bottom: 20px;
}

.global-loading {
  position: absolute;
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

.global-loading .el-icon {
  font-size: 40px;
  color: #409eff;
  margin-bottom: 10px;
}

.global-loading p {
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