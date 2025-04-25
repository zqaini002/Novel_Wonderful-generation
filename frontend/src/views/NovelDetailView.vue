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
          <el-button @click="$router.push({name: 'home'})">返回首页</el-button>
        </template>
      </el-result>
    </div>
    
    <div v-else class="novel-content">
      <el-alert
        v-if="isDemo"
        title="当前处于演示模式"
        type="warning"
        class="demo-notice"
        show-icon
        :closable="false"
        effect="dark"
      >
        <template #default>
          <div class="demo-alert-content">
            <el-icon class="demo-icon"><InfoFilled /></el-icon>
            <span>显示的是预设数据，而非实际处理结果。您可以浏览所有功能，但数据仅供展示</span>
          </div>
        </template>
      </el-alert>
      
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
            <div class="tags-header">
              <h3>标签分析</h3>
              <el-button 
                type="primary" 
                size="small"
                :loading="refreshingTags"
                @click="refreshTags"
                :disabled="novel.processingStatus !== 'COMPLETED'">
                <el-icon><Refresh /></el-icon>
                重新分析标签
              </el-button>
            </div>
            
            <h3>推荐特点</h3>
            <div class="tags-group">
              <el-tag 
                v-for="tag in getTagsByType('POSITIVE')" 
                :key="tag.id" 
                type="success" 
                effect="dark">
                {{ tag.name }}
              </el-tag>
              <el-empty v-if="getTagsByType('POSITIVE').length === 0" description="暂无标签" :image-size="60" />
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
              <el-empty v-if="getTagsByType('WARNING').length === 0" description="暂无标签" :image-size="60" />
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
              <el-empty v-if="getTagsByType('INFO').length === 0" description="暂无标签" :image-size="60" />
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="数据可视化" name="visualization">
          <div class="visualization-section">
            <div class="vis-header">
              <h3>数据可视化</h3>
              <el-button 
                type="primary" 
                @click="navigateToVisualization"
                size="small">
                <el-icon><FullScreen /></el-icon>
                查看完整可视化
              </el-button>
            </div>
            <el-tabs v-model="visualTab" type="card" class="vis-tabs">
              <el-tab-pane 
                v-for="tab in visualTabs" 
                :key="tab.name" 
                :label="tab.label" 
                :name="tab.name"
              >
                <!-- 关键词云 -->
                <div v-if="visualTab === 'keywords'" class="vis-container">
                  <p class="vis-description">该关键词云展示了小说中出现频率最高的关键词，词汇大小表示其在小说中的重要程度。</p>
                  <keyword-cloud 
                    :keywords="novel.keywords || []" 
                    title="小说关键词分布"
                    height="400px" />
                </div>

                <!-- 情节波动图 -->
                <div v-if="visualTab === 'plotTrend'" class="vis-container">
                  <p class="vis-description">情节波动图展示了小说情感和紧张度随章节的变化，帮助理解故事结构和节奏。</p>
                  <plot-trend-chart
                    :plot-data="novel.plotTrendData || []"
                    title="情节波动趋势"
                    height="400px" />
                </div>

                <!-- 结构分析 -->
                <div v-if="visualTab === 'structure'" class="vis-container">
                  <p class="vis-description">小说结构分析图展示了小说各部分的比例和关系，帮助理解整体结构布局。</p>
                  <structure-analysis-chart
                    :structure-data="novel.structureData || {}"
                    title="小说结构分析"
                    height="400px" />
                </div>

                <!-- 人物关系 -->
                <div v-if="visualTab === 'characters'" class="vis-container">
                  <p class="vis-description">人物关系网络图展示了小说中主要角色之间的关系，节点大小表示角色重要性，连线表示角色间关系。</p>
                  <character-network-chart
                    :network-data="novel.characters || { nodes: [], links: [] }"
                    title="人物关系网络"
                    height="400px" />
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
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
import { useRoute, useRouter } from 'vue-router'
import novelApi from '@/api/novel'
import visualizationService from '@/api/visualization'
import novelService from '@/services/novel'
import { InfoFilled, FullScreen, Refresh } from '@element-plus/icons-vue'
import KeywordCloud from '@/components/visualization/KeywordCloud.vue'
import PlotTrendChart from '@/components/visualization/PlotTrendChart.vue'
import StructureAnalysisChart from '@/components/visualization/StructureAnalysisChart.vue'
import CharacterNetworkChart from '@/components/visualization/CharacterNetworkChart.vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'NovelDetailView',
  components: {
    InfoFilled,
    FullScreen,
    Refresh,
    KeywordCloud,
    PlotTrendChart,
    StructureAnalysisChart,
    CharacterNetworkChart
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const loading = ref(true)
    const error = ref(null)
    const dialogVisible = ref(false)
    const selectedChapter = ref(null)
    const activeTab = ref('summary')
    const visualTab = ref('keywords')
    const showChaptersTable = ref(false)
    const chaptersLoading = ref(false)
    const refreshingTags = ref(false)
    
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
        
        // 获取可视化数据
        if (novel.value && novel.value.processingStatus === 'COMPLETED') {
          try {
            // 获取可视化数据
            const visualData = await visualizationService.getAllVisualizationData(novelId.value)
            
            // 整合数据到novel对象中
            novel.value = {
              ...novel.value,
              keywords: visualData.keywords || [],
              plotTrendData: visualData.emotional || [],
              structureData: visualData.structure || {},
              characters: visualData.characters || { nodes: [], links: [] }
            }
          } catch (visError) {
            console.warn('获取可视化数据失败:', visError)
            // 设置默认的演示数据
            novel.value.keywords = generateDemoKeywords()
            novel.value.plotTrendData = generateDemoPlotTrend()
            novel.value.structureData = generateDemoStructureData()
            novel.value.characters = generateDemoCharacters()
          }
        } else if (isDemo.value) {
          // 设置默认的演示数据
          novel.value.keywords = generateDemoKeywords()
          novel.value.plotTrendData = generateDemoPlotTrend()
          novel.value.structureData = generateDemoStructureData()
          novel.value.characters = generateDemoCharacters()
        }
        
      } catch (err) {
        console.error('获取小说详情失败:', err)
        error.value = '获取小说详情失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }
    
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
      ]
    }
    
    // 生成演示情节波动数据
    const generateDemoPlotTrend = () => {
      const chapters = []
      for (let i = 1; i <= 30; i++) {
        // 基础情感值
        let emotion = 0
        
        // 创建典型的小说情节曲线
        if (i < 5) {
          // 开头平缓
          emotion = 20 + Math.random() * 10
        } else if (i < 10) {
          // 初步上升
          emotion = 30 + (i - 5) * 5 + Math.random() * 10
        } else if (i < 15) {
          // 第一个小高潮
          emotion = 50 + Math.sin((i - 10) * 0.8) * 20 + Math.random() * 10
        } else if (i < 20) {
          // 中间起伏
          emotion = 40 + Math.sin((i - 15) * 0.5) * 15 + Math.random() * 15
        } else if (i < 25) {
          // 逐渐攀升到高潮
          emotion = 50 + (i - 20) * 8 + Math.random() * 10
        } else {
          // 最后的高潮和结局
          emotion = 90 - (i - 25) * 6 + Math.random() * 10
        }
        
        const isImportant = i === 14 || i === 24 // 重要章节标记
        
        // 处理特殊章节
        const isClimaxStart = i === 22
        const isClimaxEnd = i === 27
        
        chapters.push({
          chapterNumber: `第${i}章`,
          emotion: Math.round(emotion),
          isImportant,
          isClimaxStart,
          isClimaxEnd,
          event: isImportant ? `章节${i}关键事件` : ''
        })
      }
      return chapters
    }
    
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
      }
    }
    
    // 生成演示人物关系数据
    const generateDemoCharacters = () => {
      return {
        nodes: [
          { id: '1', name: '主角', value: 80, category: '主要角色', desc: `${novel.value.title}的主人公` },
          { id: '2', name: '女主角', value: 70, category: '主要角色', desc: '主要女性角色' },
          { id: '3', name: '反派', value: 65, category: '主要角色', desc: '故事中的反派角色' },
          { id: '4', name: '挚友', value: 50, category: '主要角色', desc: '主角的好友' },
          { id: '5', name: '导师', value: 55, category: '主要角色', desc: '指导主角的人物' },
          { id: '6', name: '家人1', value: 40, category: '次要角色', desc: '主角的家庭成员' },
          { id: '7', name: '家人2', value: 35, category: '次要角色', desc: '主角的家庭成员' },
          { id: '8', name: '朋友1', value: 30, category: '次要角色', desc: '次要朋友角色' },
          { id: '9', name: '朋友2', value: 30, category: '次要角色', desc: '次要朋友角色' },
          { id: '10', name: '对手', value: 45, category: '主要角色', desc: '与主角竞争的角色' }
        ],
        links: [
          { source: '1', target: '2', relation: '恋人', value: 5 },
          { source: '1', target: '3', relation: '宿敌', value: 5 },
          { source: '1', target: '4', relation: '好友', value: 4 },
          { source: '1', target: '5', relation: '师徒', value: 4 },
          { source: '1', target: '6', relation: '亲子', value: 3 },
          { source: '1', target: '7', relation: '亲子', value: 3 },
          { source: '2', target: '3', relation: '敌对', value: 2 },
          { source: '2', target: '8', relation: '朋友', value: 2 },
          { source: '4', target: '8', relation: '熟识', value: 1 },
          { source: '4', target: '9', relation: '熟识', value: 1 },
          { source: '5', target: '3', relation: '旧识', value: 2 },
          { source: '5', target: '10', relation: '师徒', value: 2 },
          { source: '6', target: '7', relation: '伴侣', value: 3 },
          { source: '3', target: '10', relation: '合作', value: 2 }
        ]
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
    
    const navigateToVisualization = () => {
      if (!novel.value || !novel.value.id) {
        console.error('无法导航到可视化页面: 缺少小说ID');
        ElMessage.error('无法查看可视化分析：无法获取小说ID');
        return;
      }
      
      const novelId = novel.value.id;
      console.log('Navigating to visualization for novel:', novelId);
      
      // 使用字符串类型的ID确保路由参数正确传递
      router.push({
        name: 'novel-visualization', 
        params: { id: String(novelId) }
      }).catch(err => {
        console.error('导航到可视化页面失败:', err);
        ElMessage.error('导航到可视化页面失败');
      });
    }
    
    // 可视化标签页选项
    const visualTabs = ref([
      { name: 'keywords', label: '关键词云' },
      { name: 'plotTrend', label: '情节波动图' },
      { name: 'structure', label: '结构分析' },
      { name: 'characters', label: '人物关系' }
    ])
    
    const refreshTags = async () => {
      if (!novel.value || !novel.value.id) {
        console.error('无法刷新标签: 缺少小说ID');
        ElMessage.error('无法刷新标签：无法获取小说ID');
        return;
      }
      
      try {
        refreshingTags.value = true;
        // 调用刷新标签API
        const response = await novelService.refreshNovelTags(novel.value.id);
        console.log('标签刷新结果:', response.data);
        
        // 重新获取小说详情，包括更新的标签
        await fetchNovelDetails();
        
        ElMessage.success(`标签分析已更新，共${response.data.tagCount}个标签`);
      } catch (err) {
        console.error('刷新标签失败:', err);
        ElMessage.error('刷新标签失败: ' + (err.response?.data?.error || err.message));
      } finally {
        refreshingTags.value = false;
      }
    };
    
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
      visualTab,
      showChaptersTable,
      chaptersLoading,
      getTagsByType,
      isDemo,
      navigateToVisualization,
      visualTabs,
      refreshingTags,
      refreshTags
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
  margin: 15px 0;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.demo-alert-content {
  display: flex;
  align-items: center;
  margin-top: 5px;
}

.demo-icon {
  margin-right: 5px;
  font-size: 16px;
  color: #ffcc00;
}

.mb-3 {
  margin-bottom: 1rem;
}

.visualization-section {
  padding: 10px;
}

.vis-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
}

.vis-tabs {
  margin-top: 10px;
}

.vis-container {
  margin: 10px 0;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.vis-description {
  color: #666;
  margin-bottom: 15px;
  font-size: 14px;
  line-height: 1.5;
}

.tags-section {
  padding: 20px 0;
}

.tags-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tags-header h3 {
  margin: 0;
}

.tags-group {
  margin-bottom: 20px;
}

.tags-group .el-tag {
  margin-right: 10px;
  margin-bottom: 10px;
}
</style> 