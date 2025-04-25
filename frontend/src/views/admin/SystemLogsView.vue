<template>
  <div class="system-logs-container">
    <el-card class="system-logs-card">
      <template #header>
        <div class="card-header">
          <h2>系统日志</h2>
          <div class="header-actions">
            <el-select 
              v-model="logLevel" 
              placeholder="日志级别" 
              style="width: 120px; margin-right: 10px;"
              @change="fetchLogs"
            >
              <el-option label="全部" value="ALL" />
              <el-option label="INFO" value="INFO" />
              <el-option label="WARN" value="WARN" />
              <el-option label="ERROR" value="ERROR" />
              <el-option label="DEBUG" value="DEBUG" />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              style="width: 300px; margin-right: 10px;"
              @change="fetchLogs"
            />
            <el-input
              v-model="searchQuery"
              placeholder="搜索日志内容"
              clearable
              @clear="fetchLogs"
              style="width: 200px; margin-right: 10px;"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="fetchLogs">刷新</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else class="logs-table-container">
        <el-table
          :data="logs"
          style="width: 100%"
          border
          stripe
        >
          <el-table-column type="expand">
            <template #default="props">
              <div class="log-details">
                <div class="log-stack" v-if="props.row.stackTrace">
                  <p class="stack-title">堆栈跟踪:</p>
                  <pre>{{ props.row.stackTrace }}</pre>
                </div>
                <div class="log-params" v-if="props.row.params">
                  <p class="params-title">参数:</p>
                  <pre>{{ JSON.stringify(props.row.params, null, 2) }}</pre>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.timestamp) }}
            </template>
          </el-table-column>
          <el-table-column prop="level" label="级别" width="100">
            <template #default="scope">
              <el-tag :type="getLogLevelType(scope.row.level)">
                {{ scope.row.level }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="logger" label="Logger" width="180" />
          <el-table-column prop="thread" label="线程" width="150" />
          <el-table-column prop="message" label="消息" min-width="300" show-overflow-tooltip />
          <el-table-column prop="userId" label="用户" width="120" />
          <el-table-column prop="ip" label="IP地址" width="130" />
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            background
            layout="prev, pager, next, sizes, total"
            :total="totalLogs"
            :page-size="pageSize"
            :page-sizes="[20, 50, 100, 200]"
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
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import adminService from '@/api/admin'

const loading = ref(true)
const logs = ref([])
const searchQuery = ref('')
const logLevel = ref('ALL')
const dateRange = ref(null)
const currentPage = ref(0)
const pageSize = ref(20)
const totalLogs = ref(0)

// 获取系统日志
const fetchLogs = async () => {
  loading.value = true
  try {
    const filters = {}
    
    if (logLevel.value && logLevel.value !== 'ALL') {
      filters.level = logLevel.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      // 设置开始日期为当天的00:00:00
      const startDate = new Date(dateRange.value[0])
      startDate.setHours(0, 0, 0, 0)
      filters.startDate = startDate.toISOString().split('T')[0]
      
      // 设置结束日期为当天的23:59:59，确保包含整天
      const endDate = new Date(dateRange.value[1])
      endDate.setHours(23, 59, 59, 999)
      filters.endDate = endDate.toISOString().split('T')[0]
      
      console.log('日期范围:', {
        startDate: startDate.toISOString(),
        endDate: endDate.toISOString(),
        startDateStr: filters.startDate,
        endDateStr: filters.endDate
      })
    }
    
    if (searchQuery.value) {
      filters.query = searchQuery.value
    }
    
    console.log('请求系统日志参数:', { page: currentPage.value, size: pageSize.value, filters })
    
    // 提前构建URL以便调试
    let url = `/admin/logs?page=${currentPage.value}&size=${pageSize.value}`;
    if (filters.level) url += `&level=${filters.level}`;
    if (filters.startDate) url += `&startDate=${filters.startDate}`;
    if (filters.endDate) url += `&endDate=${filters.endDate}`;
    if (filters.query) url += `&query=${encodeURIComponent(filters.query)}`;
    console.log('请求URL:', url);
    
    const response = await adminService.getSystemLogs(currentPage.value, pageSize.value, filters)
    
    // 打印API响应以便调试
    console.log('获取系统日志响应:', response);
    
    // 增加空检查
    if (response === undefined || response === null) {
      console.warn('系统日志API返回undefined或null');
      logs.value = []
      totalLogs.value = 0
      ElMessage.warning('系统日志返回为空')
      return
    }
    
    // apiClient已经提取了response.data，直接检查logs字段
    if (response && response.logs) {
      logs.value = response.logs
      totalLogs.value = response.total || 0
      console.log(`成功获取系统日志, 共${totalLogs.value}条记录`)
      
      // 检查logs数组内容
      if (logs.value.length > 0) {
        console.log('第一条日志记录:', logs.value[0])
      } else {
        console.log('日志记录为空数组')
      }
    } else {
      logs.value = []
      totalLogs.value = 0
      console.warn('系统日志API返回格式异常:', response)
      console.warn('响应数据类型:', typeof response)
      console.warn('响应数据结构:', Object.keys(response || {}))
      ElMessage.warning('系统日志数据格式异常')
    }
  } catch (error) {
    console.error('获取系统日志失败:', error)
    console.error('错误类型:', error.name)
    console.error('错误消息:', error.message)
    if (error.response) {
      console.error('错误响应状态:', error.response.status)
      console.error('错误响应数据:', error.response.data)
    }
    ElMessage.error('获取系统日志失败: ' + (error.message || '未知错误'))
    logs.value = []
    totalLogs.value = 0
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '未知'
  const date = new Date(timestamp)
  return date.toLocaleString()
}

// 获取日志级别对应的类型
const getLogLevelType = (level) => {
  switch (level) {
    case 'ERROR':
      return 'danger'
    case 'WARN':
      return 'warning'
    case 'INFO':
      return 'info'
    case 'DEBUG':
      return 'success'
    default:
      return ''
  }
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchLogs()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page - 1 // 后端分页从0开始
  fetchLogs()
}

// 组件挂载时获取数据
onMounted(() => {
  // 设置默认日期范围为最近7天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - 7)
  dateRange.value = [startDate, endDate]
  
  fetchLogs()
})
</script>

<style scoped>
.system-logs-container {
  padding: 20px;
}

.system-logs-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.loading-container {
  padding: 20px;
}

.logs-table-container {
  overflow-x: auto;
}

.log-details {
  padding: 10px 20px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.log-stack, .log-params {
  margin-bottom: 10px;
}

.stack-title, .params-title {
  font-weight: bold;
  margin-bottom: 5px;
}

pre {
  background-color: #f0f0f0;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: monospace;
  font-size: 12px;
  white-space: pre-wrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 
 