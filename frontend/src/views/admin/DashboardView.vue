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
            
            <el-button @click="goToVisualization">
              <el-icon><PieChart /></el-icon>
              小说可视化
            </el-button>
            
            <el-button @click="clearCache">
              <el-icon><Delete /></el-icon>
              清理缓存
            </el-button>
            
            <el-button @click="showStatsDetails">
              <el-icon><InfoFilled /></el-icon>
              系统详情
            </el-button>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 系统详情对话框 -->
    <el-dialog
      v-model="statsDialogVisible"
      title="系统详细信息"
      width="580px"
    >
      <div v-if="loading" class="dialog-loading">
        <el-skeleton :rows="10" animated />
      </div>
      <div v-else class="stats-details">
        <el-descriptions title="用户统计" :column="2" border>
          <el-descriptions-item label="总用户数">{{ stats.totalUsers || 0 }}</el-descriptions-item>
          <el-descriptions-item label="新增用户(7天)">{{ stats.newUsers || 0 }}</el-descriptions-item>
          <el-descriptions-item label="活跃用户(30天)">{{ stats.activeUsers || 0 }}</el-descriptions-item>
          <el-descriptions-item label="管理员数">{{ stats.adminUsers || 0 }}</el-descriptions-item>
        </el-descriptions>
        
        <el-descriptions title="小说统计" :column="2" border class="detail-section">
          <el-descriptions-item label="总小说数">{{ stats.totalNovels || 0 }}</el-descriptions-item>
          <el-descriptions-item label="已完成处理">{{ stats.completedNovels || 0 }}</el-descriptions-item>
          <el-descriptions-item label="处理中">{{ stats.processingNovels || 0 }}</el-descriptions-item>
          <el-descriptions-item label="待处理">{{ stats.pendingNovels || 0 }}</el-descriptions-item>
          <el-descriptions-item label="处理失败">{{ stats.failedNovels || 0 }}</el-descriptions-item>
          <el-descriptions-item label="今日上传">{{ stats.todayNovels || 0 }}</el-descriptions-item>
        </el-descriptions>
        
        <el-descriptions title="系统信息" :column="2" border class="detail-section">
          <el-descriptions-item label="数据库大小">{{ stats.dbSize || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="文件存储大小">{{ stats.storageSize || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="系统运行时间">{{ stats.uptime || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="最近错误数">{{ stats.recentErrors || 0 }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="refreshDetailData">刷新数据</el-button>
          <el-button type="primary" @click="statsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { User, UserFilled, Reading, Loading, Check, Document, Delete, PieChart, InfoFilled } from '@element-plus/icons-vue'
import adminService from '@/api/admin'

const router = useRouter()
const loading = ref(true)
const statsDialogVisible = ref(false)
const stats = ref({
  totalUsers: 0,
  newUsers: 0,
  totalNovels: 0,
  processingNovels: 0,
  completedNovels: 0,
  // 扩展的详细统计数据
  activeUsers: 0,
  adminUsers: 0,
  pendingNovels: 0,
  failedNovels: 0, 
  todayNovels: 0,
  dbSize: '未知',
  storageSize: '未知',
  uptime: '未知',
  recentErrors: 0
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

// 导航到小说可视化
const goToVisualization = () => {
  router.push('/admin/visualization')
}

// 清理缓存
const clearCache = async () => {
  try {
    // 确认提示
    await ElMessageBox.confirm(
      `<div style="text-align: left;">
        <h3 style="margin-top: 0;">即将清理以下缓存:</h3>
        <ul style="padding-left: 20px;">
          <li>JVM内存垃圾回收</li>
          <li>临时文件</li>
          <li>上传目录临时文件</li>
          <li>Hibernate二级缓存</li>
        </ul>
        <p>这可能会暂时影响系统性能，确定要继续吗？</p>
      </div>`,
      '系统缓存清理',
      {
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )
    
    // 显示加载中
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在清理系统缓存...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    // 执行清理
    try {
      const response = await adminService.clearSystemCache();
      
      // 关闭加载
      loadingInstance.close();
      
      // 根据响应显示不同的成功消息
      if (response && response.message) {
        ElMessage({
          type: 'success',
          message: response.message,
          duration: 3000
        });
      } else {
        ElMessage({
          type: 'success',
          message: '系统缓存清理成功',
          duration: 3000
        });
      }
      
      // 刷新数据
      fetchDashboardData();
    } catch (error) {
      // 关闭加载
      loadingInstance.close();
      
      // 显示错误信息
      ElMessage({
        type: 'error',
        message: `清理缓存失败: ${error.message || '未知错误'}`,
        duration: 5000
      });
    }
  } catch (err) {
    // 用户取消，不做任何操作
    if (err !== 'cancel') {
      console.error('清理缓存失败:', err);
      ElMessage.error('清理缓存失败');
    }
  }
}

// 显示系统统计详情
const showStatsDetails = () => {
  statsDialogVisible.value = true
  // 如果已经有数据了，不需要重新获取
  if (stats.value.dbSize === '未知') {
    fetchDetailedStats()
  }
}

// 获取详细的统计数据
const fetchDetailedStats = async () => {
  loading.value = true;
  try {
    // 调用详细统计API获取真实数据，不再使用模拟数据
    const response = await adminService.getDetailedSystemStats();
    
    if (response) {
      console.log('详细系统统计数据:', response);
      // 直接使用后端返回的完整数据
      stats.value = response;
    } else {
      // 如果返回为空，保留基本统计数据
      ElMessage.warning('未能获取完整的系统详情数据');
    }
  } catch (error) {
    console.error('获取详细统计数据失败:', error);
    ElMessage.error('获取详细统计数据失败: ' + (error.message || '未知错误'));
    
    // 出错时回退到基本数据
    fetchDashboardData();
  } finally {
    loading.value = false;
  }
}

// 刷新详细数据
const refreshDetailData = () => {
  fetchDetailedStats()
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

.dialog-loading {
  padding: 20px;
}

.stats-details {
  padding: 10px;
}

.detail-section {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 
 