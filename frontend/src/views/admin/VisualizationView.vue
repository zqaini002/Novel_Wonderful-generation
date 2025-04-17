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
              <el-popover
                placement="top"
                width="200"
                trigger="hover"
              >
                <template #reference>
                  <el-button
                    size="small"
                    type="info"
                    :disabled="!isNovelComplete(scope.row.status)"
                  >
                    <el-icon><InfoFilled /></el-icon>
                    详情
                  </el-button>
                </template>
                <div class="visualization-popover-content">
                  <p><b>可视化内容包括：</b></p>
                  <ul>
                    <li>关键词云图</li>
                    <li>情节波动图</li>
                    <li>人物关系网络</li>
                    <li>小说结构分析</li>
                    <li>章节情感分析</li>
                  </ul>
                </div>
              </el-popover>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis, InfoFilled } from '@element-plus/icons-vue'
import adminService from '@/api/admin'

const router = useRouter()
const loading = ref(true)
const novels = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

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

.loading-container {
  padding: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.visualization-popover-content ul {
  padding-left: 20px;
  margin-top: 5px;
}

.visualization-popover-content li {
  margin-bottom: 5px;
}
</style> 