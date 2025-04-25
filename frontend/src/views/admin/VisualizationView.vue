<template>
  <div class="visualization-container">
    <el-card class="visualization-card">
      <template #header>
        <div class="card-header">
          <h2>小说可视化分析</h2>
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else>
        <el-alert
          v-if="novels.length === 0"
          title="暂无小说数据"
          type="info"
          show-icon
          :closable="false"
        >
          系统中目前没有可供可视化分析的小说数据
        </el-alert>
        
        <el-table v-else :data="novels" border style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="小说标题" min-width="200" />
          <el-table-column prop="author" label="作者" width="150" />
          <el-table-column prop="totalChapters" label="章节数" width="100" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="上传时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="showNovelVisualization(scope.row.id)"
                :disabled="!isNovelComplete(scope.row.status)"
              >
                <el-icon><DataAnalysis /></el-icon>
                可视化分析
              </el-button>
              <el-button
                size="small"
                type="info"
                @click="showNovelDetails(scope.row)"
              >
                <el-icon><InfoFilled /></el-icon>
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            background
            layout="prev, pager, next, sizes, total"
            :total="total"
            :page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 小说详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="小说详细信息"
      width="650px"
    >
      <div v-if="!selectedNovel" class="dialog-loading">
        <el-skeleton :rows="10" animated />
      </div>
      <div v-else class="novel-details">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="ID">{{ selectedNovel.id }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ selectedNovel.title }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ selectedNovel.author }}</el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ formatDateTime(selectedNovel.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedNovel.status)">{{ getStatusText(selectedNovel.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最后更新">{{ formatDateTime(selectedNovel.updateTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <el-descriptions title="内容信息" :column="2" border class="detail-section">
          <el-descriptions-item label="总章节数">{{ selectedNovel.totalChapters || 0 }}</el-descriptions-item>
          <el-descriptions-item label="已处理章节">{{ selectedNovel.processedChapters || 0 }}</el-descriptions-item>
          <el-descriptions-item label="总字数">{{ formatWordCount(selectedNovel.wordCount) }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ selectedNovel.category || '未分类' }}</el-descriptions-item>
          <el-descriptions-item label="语言">{{ selectedNovel.language || '中文' }}</el-descriptions-item>
          <el-descriptions-item label="处理进度">
            <el-progress 
              :percentage="calculateProgress(selectedNovel)" 
              :status="isNovelComplete(selectedNovel.status) ? 'success' : ''"
            ></el-progress>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-descriptions title="可视化分析内容" :column="1" border class="detail-section">
          <el-descriptions-item>
            <div class="visualization-list">
              <ul>
                <li><el-icon><Check /></el-icon> 关键词云图 - 展示小说中的重要关键词及其频率</li>
                <li><el-icon><Check /></el-icon> 情节波动图 - 跟踪小说情节的情感起伏和关键点</li>
                <li><el-icon><Check /></el-icon> 人物关系网络 - 可视化小说中角色之间的复杂关系</li>
                <li><el-icon><Check /></el-icon> 小说结构分析 - 分析小说的整体结构和章节分布</li>
                <li><el-icon><Check /></el-icon> 章节情感分析 - 每个章节的情感基调和变化趋势</li>
              </ul>
            </div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="selectedNovel.description" class="novel-description">
          <h4>小说简介：</h4>
          <p>{{ selectedNovel.description }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button 
            type="primary" 
            @click="showNovelVisualization(selectedNovel.id)"
            :disabled="!isNovelComplete(selectedNovel?.status)"
          >
            <el-icon><DataAnalysis /></el-icon>
            查看可视化分析
          </el-button>
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis, InfoFilled, Check } from '@element-plus/icons-vue'
import adminService from '@/api/admin'

const router = useRouter()
const loading = ref(true)
const novels = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailsDialogVisible = ref(false)
const selectedNovel = ref(null)

// 获取小说列表
const fetchNovels = async () => {
  loading.value = true
  try {
    const response = await adminService.getAllNovels()
    if (response && response.novels) {
      novels.value = response.novels
      total.value = response.total || response.novels.length
    } else {
      novels.value = []
      total.value = 0
      console.warn('获取小说列表返回格式异常')
    }
  } catch (error) {
    console.error('获取小说列表失败:', error)
    ElMessage.error('获取小说列表失败: ' + (error.message || '未知错误'))
    novels.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 显示小说详情
const showNovelDetails = (novel) => {
  selectedNovel.value = novel
  detailsDialogVisible.value = true
}

// 计算处理进度百分比
const calculateProgress = (novel) => {
  if (!novel || !novel.totalChapters || novel.totalChapters === 0) {
    return 0
  }
  return Math.min(100, Math.round((novel.processedChapters || 0) / novel.totalChapters * 100))
}

// 格式化字数
const formatWordCount = (count) => {
  if (!count) return '0'
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)} 万字`
  }
  return `${count} 字`
}

// 刷新数据
const refreshData = () => {
  fetchNovels()
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '未知'
  const date = new Date(dateTime)
  return date.toLocaleString()
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'success'
    case 'PROCESSING':
      return 'warning'
    case 'FAILED':
      return 'danger'
    case 'PENDING':
      return 'info'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'COMPLETED':
      return '已完成'
    case 'PROCESSING':
      return '处理中'
    case 'FAILED':
      return '处理失败'
    case 'PENDING':
      return '等待处理'
    default:
      return '未知状态'
  }
}

// 检查小说是否已完成分析
const isNovelComplete = (status) => {
  return status === 'COMPLETED'
}

// 跳转到小说可视化分析页面
const showNovelVisualization = (novelId) => {
  router.push(`/admin/novel/${novelId}/visualization`)
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchNovels()
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchNovels()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchNovels()
})
</script>

<style scoped>
.visualization-container {
  padding: 20px;
}

.visualization-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container, .dialog-loading {
  padding: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-section {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.visualization-list ul {
  list-style-type: none;
  padding-left: 0;
}

.visualization-list li {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.visualization-list .el-icon {
  color: #67c23a;
  margin-right: 8px;
}

.novel-description {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.novel-description h4 {
  margin-top: 0;
  margin-bottom: 8px;
  color: #606266;
}

.novel-description p {
  margin: 0;
  line-height: 1.6;
  white-space: pre-line;
}
</style> 