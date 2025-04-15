<template>
  <div class="my-novels-container">
    <h1>我的小说</h1>
    
    <div v-if="loading" class="loading-container">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">加载中...</span>
      </div>
      <p>正在加载您的小说列表...</p>
    </div>
    
    <div v-else-if="error" class="error-container">
      <div class="alert alert-danger" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        {{ error }}
      </div>
      <button class="btn btn-primary mt-3" @click="fetchUserNovels">
        <i class="bi bi-arrow-clockwise me-1"></i> 重试
      </button>
    </div>
    
    <div v-else-if="novels.length === 0" class="empty-container">
      <div class="alert alert-info" role="alert">
        <i class="bi bi-info-circle-fill me-2"></i>
        您还没有上传任何小说。
      </div>
      <router-link to="/upload" class="btn btn-primary mt-3">
        <i class="bi bi-upload me-1"></i> 去上传小说
      </router-link>
    </div>
    
    <div v-else class="novels-list">
      <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
        <div v-for="novel in novels" :key="novel.id" class="col">
          <div class="card h-100 novel-card">
            <div class="card-body">
              <h5 class="card-title">{{ novel.title }}</h5>
              <p class="card-text text-muted">
                <small>
                  <i class="bi bi-calendar-event me-1"></i>
                  上传时间: {{ formatDate(novel.createTime) }}
                </small>
              </p>
              <p class="card-text status-badge">
                <span :class="getStatusBadgeClass(novel.status)">{{ getStatusText(novel.status) }}</span>
              </p>
              <p class="card-text description">{{ novel.description || '暂无简介' }}</p>
            </div>
            <div class="card-footer bg-transparent border-top-0">
              <router-link :to="`/novel/${novel.id}`" class="btn btn-outline-primary btn-sm me-2">
                <i class="bi bi-book me-1"></i> 查看详情
              </router-link>
              <button 
                v-if="novel.status === 'COMPLETED'" 
                class="btn btn-outline-success btn-sm me-2"
                @click="goToAnalyze(novel.id)"
              >
                <i class="bi bi-graph-up me-1"></i> 查看分析
              </button>
              <button 
                class="btn btn-outline-danger btn-sm"
                @click="confirmDelete(novel)"
              >
                <i class="bi bi-trash me-1"></i> 删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessageBox, ElMessage } from 'element-plus';
import userService from '@/services/user';
import novelService from '@/services/novel';

export default {
  name: 'MyNovelsView',
  data() {
    return {
      novels: [],
      loading: true,
      error: null,
      selectedNovel: null
    };
  },
  created() {
    this.fetchUserNovels();
  },
  methods: {
    async fetchUserNovels() {
      this.loading = true;
      this.error = null;
      
      try {
        const data = await userService.getUserNovels();
        console.log('获取用户小说原始响应:', data);
        
        // 确保正确处理返回格式
        let novels = [];
        if (data) {
          if (Array.isArray(data)) {
            novels = data;
          } else if (data.novels && Array.isArray(data.novels)) {
            novels = data.novels;
          }
        }
        
        this.novels = novels;
        console.log('获取用户小说成功:', this.novels);
      } catch (error) {
        console.error('获取小说列表失败:', error);
        
        if (error.message === '未登录' || error.message === '用户ID不存在') {
          this.error = '请先登录后再查看您的小说';
          
          // 可选：重定向到登录页
          setTimeout(() => {
            this.$router.push('/login?redirect=/my-novels');
          }, 2000);
        } else {
          this.error = error.response?.data?.error || '获取小说列表失败，请稍后重试';
        }
        
        // 如果获取失败设置空列表
        this.novels = [];
      } finally {
        this.loading = false;
      }
    },
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'COMPLETED': '已完成',
        'FAILED': '处理失败'
      };
      return statusMap[status] || status;
    },
    getStatusBadgeClass(status) {
      const classMap = {
        'PENDING': 'badge bg-warning',
        'PROCESSING': 'badge bg-info',
        'COMPLETED': 'badge bg-success',
        'FAILED': 'badge bg-danger'
      };
      return classMap[status] || 'badge bg-secondary';
    },
    goToAnalyze(novelId) {
      this.$router.push(`/analyze/${novelId}`);
    },
    confirmDelete(novel) {
      this.selectedNovel = novel;
      
      ElMessageBox.confirm(
        `您确定要删除小说 "${novel.title}" 吗？此操作不可逆，删除后将无法恢复！`,
        '确认删除',
        {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          this.deleteNovel();
        })
        .catch(() => {
          // 用户取消，不执行操作
        });
    },
    async deleteNovel() {
      if (!this.selectedNovel) return;
      
      try {
        await novelService.deleteNovel(this.selectedNovel.id);
        this.novels = this.novels.filter(novel => novel.id !== this.selectedNovel.id);
        ElMessage({
          type: 'success',
          message: '小说删除成功'
        });
      } catch (error) {
        console.error('删除小说失败:', error);
        ElMessage({
          type: 'error',
          message: error.response?.data?.error || '删除小说失败，请稍后重试'
        });
      } finally {
        this.selectedNovel = null;
      }
    }
  }
};
</script>

<style scoped>
.my-novels-container {
  padding: 2rem 0;
}

.loading-container, .error-container, .empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
}

.novel-card {
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.novel-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.status-badge {
  margin-bottom: 1rem;
}

.description {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  min-height: 4.5rem;
}
</style> 