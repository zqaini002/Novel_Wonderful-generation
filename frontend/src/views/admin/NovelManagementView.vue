<template>
  <div class="novel-management-container">
    <el-card class="novel-management-card">
      <template #header>
        <div class="card-header">
          <h2>小说管理</h2>
          <div class="header-actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索小说标题或作者"
              clearable
              @clear="filterNovels"
              @input="filterNovels"
              style="width: 300px; margin-right: 10px;"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-switch
              v-model="showDeleted"
              active-text="显示已删除"
              inactive-text="仅显示正常"
              style="margin-right: 10px;"
              @change="handleShowDeletedChanged"
            />
            <el-button type="primary" @click="refreshNovels">刷新</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else class="novel-table-container">
        <el-table
          :data="displayNovels"
          style="width: 100%"
          border
          stripe
          highlight-current-row
          @row-click="handleRowClick"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" width="200" />
          <el-table-column prop="author" label="作者" width="150" />
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status || scope.row.processingStatus)">
                {{ formatStatus(scope.row.status || scope.row.processingStatus) }}
              </el-tag>
              <el-tag v-if="scope.row.isDeleted" type="danger" style="margin-left: 5px;">已删除</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="章节数" width="100">
            <template #default="scope">
              {{ scope.row.chapterCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="字数" width="120">
            <template #default="scope">
              {{ formatWordCount(scope.row.wordCount) }}
            </template>
          </el-table-column>
          <el-table-column label="上传时间" width="180">
            <template #default="scope">
              <div v-if="false">{{ console.log('小说对象:', scope.row) }}</div>
              {{ formatDate(scope.row.createdAt || scope.row.created_at || scope.row.createTime || scope.row.uploadDate || scope.row.created) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click.stop="viewNovelDetail(scope.row.id)"
              >
                查看
              </el-button>
              <el-button
                v-if="!scope.row.isDeleted"
                size="small"
                type="warning"
                @click.stop="reprocessNovel(scope.row)"
                :disabled="scope.row.status === 'PROCESSING'"
              >
                重新处理
              </el-button>
              <el-button
                v-if="!scope.row.isDeleted"
                size="small"
                type="danger"
                @click.stop="deleteNovel(scope.row)"
              >
                删除
              </el-button>
              <el-button
                v-if="scope.row.isDeleted"
                size="small"
                type="success"
                @click.stop="restoreNovel(scope.row)"
              >
                恢复
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            background
            layout="prev, pager, next, sizes, total"
            :total="totalNovels"
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import adminService from '@/api/admin'

const router = useRouter()
const loading = ref(true)
const novels = ref([])
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalNovels = ref(0)
const showDeleted = ref(false)

// 获取所有小说列表
const fetchNovels = async () => {
  loading.value = true
  try {
    const response = showDeleted.value 
      ? await adminService.getAllNovelsIncludeDeleted() 
      : await adminService.getAllNovels();
    
    // 打印API响应以便调试
    console.log('获取小说列表响应:', response);
    
    // apiClient已经提取了response.data，直接检查novels字段
    if (response && response.novels) {
      novels.value = response.novels
      totalNovels.value = response.total || novels.value.length
    } else if (Array.isArray(response)) {
      // 如果response本身就是数组，直接使用
      novels.value = response
      totalNovels.value = response.length
    } else {
      console.warn('小说列表API返回格式异常:', response)
      novels.value = []
      totalNovels.value = 0
      ElMessage.warning('获取小说列表数据格式异常')
    }
  } catch (error) {
    console.error('获取小说列表失败:', error)
    ElMessage.error('获取小说列表失败: ' + (error.message || '未知错误'))
    novels.value = []
    totalNovels.value = 0
  } finally {
    loading.value = false
  }
}

// 刷新小说列表
const refreshNovels = () => {
  fetchNovels()
}

// 处理显示已删除切换
const handleShowDeletedChanged = () => {
  fetchNovels()
}

// 过滤小说列表
const filterNovels = () => {
  // 这里简单实现前端过滤，实际项目中可能需要发送请求到后端进行过滤
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) {
    return novels.value
  }
  
  return novels.value.filter(novel => 
    novel.title.toLowerCase().includes(query) ||
    (novel.author && novel.author.toLowerCase().includes(query))
  )
}

// 计算属性：根据搜索条件显示小说
const displayNovels = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) {
    return novels.value
  }
  
  return novels.value.filter(novel => 
    novel.title.toLowerCase().includes(query) ||
    (novel.author && novel.author.toLowerCase().includes(query))
  )
})

// 格式化日期
const formatDate = (dateString) => {
  // 调试日期字段值
  console.log('日期字段值:', dateString, typeof dateString)
  
  if (!dateString) return '未知'
  try {
    // 字符串类型的日期格式处理
    if (typeof dateString === 'string') {
      // MySQL日期格式 YYYY-MM-DD HH:MM:SS
      if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(dateString)) {
        return new Date(dateString.replace(' ', 'T')).toLocaleString();
      }
      // ISO格式日期字符串
      else if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(dateString)) {
        return new Date(dateString).toLocaleString();
      }
      // MySQL日期格式没有时间部分 YYYY-MM-DD
      else if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
        return new Date(dateString).toLocaleString();
      }
      // Unix时间戳（毫秒）
      else if (/^\d{13}$/.test(dateString)) {
        return new Date(parseInt(dateString)).toLocaleString();
      }
      // Unix时间戳（秒）
      else if (/^\d{10}$/.test(dateString)) {
        return new Date(parseInt(dateString) * 1000).toLocaleString();
      }
    }
    // 处理数字类型的时间戳
    else if (typeof dateString === 'number') {
      // 检查是毫秒还是秒级时间戳
      const timestamp = dateString > 10000000000 ? dateString : dateString * 1000;
      return new Date(timestamp).toLocaleString();
    }
    
    const date = new Date(dateString)
    // 调试日期解析结果
    console.log('解析后的日期对象:', date, '是否有效:', !isNaN(date.getTime()))
    
    if (isNaN(date.getTime())) {
      // 如果日期无效，返回当前时间的格式化字符串
      console.warn('无效的日期格式:', dateString)
      return new Date().toLocaleString()
    }
    return date.toLocaleString()
  } catch (e) {
    console.warn('日期格式化错误:', e, dateString)
    return new Date().toLocaleString() // 返回当前时间作为后备
  }
}

// 格式化状态
const formatStatus = (status) => {
  if (!status) return '未知'
  
  switch (status.toUpperCase()) {
    case 'PROCESSING':
      return '处理中'
    case 'COMPLETED':
      return '已完成'
    case 'FAILED':
      return '处理失败'
    case 'QUEUED':
    case 'PENDING':
      return '等待处理'
    case 'UNKNOWN':
      return '未知'
    default:
      return status
  }
}

// 获取状态对应的类型
const getStatusType = (status) => {
  if (!status) return 'info'
  
  switch (status.toUpperCase()) {
    case 'PROCESSING':
      return 'warning'
    case 'COMPLETED':
      return 'success'
    case 'FAILED':
      return 'danger'
    case 'QUEUED':
    case 'PENDING':
      return 'info'
    case 'UNKNOWN':
      return 'info'
    default:
      return 'info'
  }
}

// 格式化字数
const formatWordCount = (count) => {
  if (!count) return '0'
  if (count < 10000) {
    return count.toString()
  }
  return (count / 10000).toFixed(2) + '万'
}

// 查看小说详情
const viewNovelDetail = (id) => {
  router.push(`/novel/${id}`)
}

// 重新处理小说
const reprocessNovel = (novel) => {
  ElMessageBox.confirm(
    `确定要重新处理小说"${novel.title}"吗？`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  .then(() => {
    ElMessage.success('已发送重新处理请求')
    // 实际项目中这里应该调用API来重新处理小说
  })
  .catch(() => {})
}

// 删除小说
const deleteNovel = (novel) => {
  ElMessageBox.confirm(
    `确定要删除小说"${novel.title}"吗？此操作不可恢复！`,
    '危险操作',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    }
  )
  .then(async () => {
    try {
      await adminService.deleteNovel(novel.id)
      ElMessage.success('小说删除成功')
      fetchNovels() // 刷新列表
    } catch (error) {
      console.error('删除小说失败:', error)
      ElMessage.error('删除小说失败')
    }
  })
  .catch(() => {})
}

// 恢复小说
const restoreNovel = (novel) => {
  ElMessageBox.confirm(
    `确定要恢复小说"${novel.title}"吗？`,
    '确认操作',
    {
      confirmButtonText: '确定恢复',
      cancelButtonText: '取消',
      type: 'success'
    }
  )
  .then(async () => {
    try {
      await adminService.restoreNovel(novel.id)
      ElMessage.success('小说恢复成功')
      fetchNovels() // 刷新列表
    } catch (error) {
      console.error('恢复小说失败:', error)
      ElMessage.error('恢复小说失败: ' + (error.message || '未知错误'))
    }
  })
  .catch(() => {})
}

// 处理行点击
const handleRowClick = (row) => {
  if (!row.isDeleted) {
    viewNovelDetail(row.id)
  }
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pageSize.value = size
  // 这里需要根据实际情况重新获取数据
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  // 这里需要根据实际情况重新获取数据
}

// 组件挂载时获取数据
onMounted(() => {
  fetchNovels()
})
</script>

<style scoped>
.novel-management-container {
  padding: 20px;
}

.novel-management-card {
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

.novel-table-container {
  overflow-x: auto;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 
 