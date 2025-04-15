<template>
  <div class="dashboard-container">
    <el-card class="dashboard-card">
      <template #header>
        <div class="card-header">
          <h2>系统仪表盘</h2>
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>
      
      <div v-else class="dashboard-content">
        <!-- 数据统计卡片 -->
        <div class="stats-cards">
          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <span>总用户数</span>
                <el-icon><User /></el-icon>
              </div>
            </template>
            <div class="stat-value">{{ stats?.totalUsers || 0 }}</div>
          </el-card>
          
          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <span>新增用户</span>
                <el-icon><UserFilled /></el-icon>
              </div>
            </template>
            <div class="stat-value">{{ stats?.newUsers || 0 }}</div>
            <div class="stat-desc">过去7天</div>
          </el-card>
          
          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <span>小说总数</span>
                <el-icon><Reading /></el-icon>
              </div>
            </template>
            <div class="stat-value">{{ stats?.totalNovels || 0 }}</div>
          </el-card>
          
          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <span>处理中</span>
                <el-icon><Loading /></el-icon>
              </div>
            </template>
            <div class="stat-value">{{ stats?.processingNovels || 0 }}</div>
          </el-card>
          
          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <span>已完成</span>
                <el-icon><Check /></el-icon>
              </div>
            </template>
            <div class="stat-value">{{ stats?.completedNovels || 0 }}</div>
          </el-card>
        </div>
        
        <!-- 快捷功能区 -->
        <el-card class="quick-actions" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>快捷操作</h3>
            </div>
          </template>
          
          <div class="actions-grid">
            <el-button @click="goToUserManagement">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
            
            <el-button @click="goToNovelManagement">
              <el-icon><Reading /></el-icon>
              小说管理
            </el-button>
            
            <el-button @click="goToSystemLogs">
              <el-icon><Document /></el-icon>
              系统日志
            </el-button>
            
            <el-button @click="clearCache">
              <el-icon><Delete /></el-icon>
              清理缓存
            </el-button>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, UserFilled, Reading, Loading, Check, Document, Delete } from '@element-plus/icons-vue'
import adminService from '@/api/admin'

const router = useRouter()
const loading = ref(true)
const stats = ref({
  totalUsers: 0,
  newUsers: 0,
  totalNovels: 0,
  processingNovels: 0,
  completedNovels: 0
})

// 获取仪表盘数据
const fetchDashboardData = async () => {
  loading.value = true
  try {
    const response = await adminService.getDashboardStats()
    
    // 打印响应对象以调试
    console.log('仪表盘API响应:', response);
    
    // 确保响应存在
    if (response) {
      // 由于前面修改了apiClient，response已经是直接的数据对象，不需要再通过response.data访问
      stats.value = response;
    } else {
      // 设置默认数据
      stats.value = {
        totalUsers: 0,
        newUsers: 0,
        totalNovels: 0,
        processingNovels: 0,
        completedNovels: 0
      }
      console.warn('仪表盘数据返回为空，使用默认值')
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('获取仪表盘数据失败: ' + (error.message || '未知错误'))
    
    // 出错时也设置默认值
    stats.value = {
      totalUsers: 0,
      newUsers: 0,
      totalNovels: 0,
      processingNovels: 0,
      completedNovels: 0
    }
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  fetchDashboardData()
}

// 导航到用户管理
const goToUserManagement = () => {
  router.push('/admin/users')
}

// 导航到小说管理
const goToNovelManagement = () => {
  router.push('/admin/novels')
}

// 导航到系统日志
const goToSystemLogs = () => {
  router.push('/admin/logs')
}

// 清理缓存
const clearCache = async () => {
  try {
    // 确认提示
    await ElMessageBox.confirm(
      '确定要清理系统缓存吗？这可能会暂时影响系统性能。',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 执行清理
    await adminService.clearSystemCache()
    ElMessage.success('缓存清理成功')
  } catch (err) {
    if (err !== 'cancel') {
      console.error('清理缓存失败:', err)
      ElMessage.error('清理缓存失败')
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-card {
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

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.stat-card {
  text-align: center;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin: 10px 0;
}

.stat-desc {
  color: #909399;
  font-size: 12px;
}

.quick-actions {
  margin-top: 10px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}

.actions-grid .el-button {
  display: flex;
  flex-direction: column;
  height: 80px;
  justify-content: center;
  align-items: center;
}

.actions-grid .el-icon {
  font-size: 24px;
  margin-bottom: 5px;
}
</style> 
 