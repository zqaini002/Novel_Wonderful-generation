<template>
  <div class="admin-container">
    <h1>管理员控制台</h1>
    
    <div class="admin-stats">
      <div class="stat-card">
        <div class="stat-icon">
          <i class="bi bi-people-fill"></i>
        </div>
        <div class="stat-content">
          <h3>{{ stats.users || 0 }}</h3>
          <p>用户数量</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <i class="bi bi-book-fill"></i>
        </div>
        <div class="stat-content">
          <h3>{{ stats.novels || 0 }}</h3>
          <p>小说数量</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <i class="bi bi-lightning-fill"></i>
        </div>
        <div class="stat-content">
          <h3>{{ stats.uploads || 0 }}</h3>
          <p>上传数量</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <i class="bi bi-eye-fill"></i>
        </div>
        <div class="stat-content">
          <h3>{{ stats.views || 0 }}</h3>
          <p>浏览数量</p>
        </div>
      </div>
    </div>
    
    <div class="admin-actions">
      <h2>管理功能</h2>
      <div class="actions-grid">
        <router-link to="/admin/dashboard" class="action-card">
          <i class="bi bi-speedometer2"></i>
          <span>仪表盘</span>
        </router-link>
        
        <router-link to="/admin/users" class="action-card">
          <i class="bi bi-people"></i>
          <span>用户管理</span>
        </router-link>
        
        <router-link to="/admin/novels" class="action-card">
          <i class="bi bi-book"></i>
          <span>小说管理</span>
        </router-link>
        
        <router-link to="/admin/logs" class="action-card">
          <i class="bi bi-list-ul"></i>
          <span>系统日志</span>
        </router-link>
      </div>
    </div>
    
    <div class="recent-activities">
      <h2>最近活动</h2>
      <div v-if="loading" class="loading">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
      </div>
      <div v-else-if="activities.length === 0" class="no-data">
        <p>暂无活动记录</p>
      </div>
      <div v-else class="activity-list">
        <div v-for="(activity, index) in activities" :key="index" class="activity-item">
          <div class="activity-time">{{ formatDate(activity.time) }}</div>
          <div class="activity-content">
            <i :class="getActivityIcon(activity.type)"></i>
            <span>{{ activity.content }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import adminService from '@/api/admin'

export default defineComponent({
  name: 'AdminView',
  data() {
    return {
      loading: false,
      stats: {
        users: 0,
        novels: 0,
        uploads: 0,
        views: 0
      },
      activities: [
        { type: 'login', content: '管理员登录系统', time: new Date(Date.now() - 1000 * 60 * 5) },
        { type: 'upload', content: '用户上传小说《三体》', time: new Date(Date.now() - 1000 * 60 * 30) },
        { type: 'register', content: '新用户注册: zhang_san', time: new Date(Date.now() - 1000 * 60 * 60 * 2) },
        { type: 'error', content: '系统发生错误：数据库连接中断', time: new Date(Date.now() - 1000 * 60 * 60 * 12) }
      ]
    }
  },
  methods: {
    fetchAdminData() {
      this.loading = true;
      adminService.getDashboardStats()
        .then(response => {
          if (response) {
            this.stats.users = response.totalUsers || 0;
            this.stats.novels = response.totalNovels || 0;
            this.stats.uploads = response.processingNovels || 0;
            this.stats.views = response.totalViews || 0;
          }
          
          // 获取最近的活动日志
          return adminService.getRecentLogs(5);
        })
        .then(logsResponse => {
          if (logsResponse && Array.isArray(logsResponse)) {
            this.activities = logsResponse.map(log => ({
              type: this.getActivityType(log.action),
              content: log.message || log.description || '系统活动',
              time: new Date(log.timestamp || log.createdAt)
            }));
          }
        })
        .catch(error => {
          console.error('获取管理员数据失败:', error);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    getActivityType(action) {
      if (!action) return 'default';
      
      if (action.includes('login')) return 'login';
      if (action.includes('upload')) return 'upload';
      if (action.includes('register')) return 'register';
      if (action.includes('error')) return 'error';
      
      return 'default';
    },
    formatDate(date) {
      return new Date(date).toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    getActivityIcon(type) {
      const iconMap = {
        login: 'bi bi-box-arrow-in-right',
        upload: 'bi bi-cloud-upload',
        register: 'bi bi-person-plus',
        error: 'bi bi-exclamation-triangle',
        default: 'bi bi-info-circle'
      };
      return iconMap[type] || iconMap.default;
    }
  },
  mounted() {
    this.fetchAdminData();
  }
})
</script>

<style scoped>
.admin-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

h1 {
  margin-bottom: 2rem;
  color: #333;
  font-weight: 600;
}

h2 {
  margin: 2rem 0 1rem;
  color: #444;
  font-weight: 500;
}

.admin-stats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 2.5rem;
  margin-right: 1.5rem;
  color: #4d7bef;
}

.stat-content h3 {
  font-size: 2rem;
  margin: 0;
  font-weight: 700;
  color: #333;
}

.stat-content p {
  margin: 0;
  color: #666;
  font-size: 1rem;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  text-decoration: none;
  color: #333;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  background: #4d7bef;
  color: #fff;
}

.action-card i {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.action-card span {
  font-weight: 500;
}

.activity-list {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #eee;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-time {
  flex: 0 0 150px;
  color: #666;
  font-size: 0.9rem;
}

.activity-content {
  flex: 1;
  display: flex;
  align-items: center;
}

.activity-content i {
  margin-right: 0.5rem;
  font-size: 1.1rem;
  color: #4d7bef;
}

.loading, .no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.no-data {
  color: #666;
}
</style> 