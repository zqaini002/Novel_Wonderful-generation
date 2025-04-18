<template>
  <div class="my-novels-container">
    <div class="page-header">
      <h1>我的小说</h1>
      <router-link to="/upload" class="el-button el-button--primary">
        <el-icon><Upload /></el-icon>
        <span>上传新小说</span>
      </router-link>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-empty description="加载中..." :image-size="120">
        <template #image>
          <el-icon class="loading-icon"><Loading /></el-icon>
        </template>
      </el-empty>
    </div>
    
    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        :title="error"
        sub-title="无法获取小说列表"
      >
        <template #extra>
          <el-button type="primary" @click="fetchUserNovels">
            <el-icon><RefreshRight /></el-icon>
            重新加载
          </el-button>
        </template>
      </el-result>
    </div>
    
    <div v-else-if="novels.length === 0" class="empty-container">
      <el-result
        icon="info"
        title="暂无小说"
        sub-title="您还没有上传任何小说，立即开始添加吧！"
      >
        <template #extra>
          <router-link to="/upload">
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              上传小说
            </el-button>
          </router-link>
        </template>
      </el-result>
    </div>
    
    <div v-else>
      <div class="novels-summary">共 {{ novels.length }} 本小说</div>
      
      <div class="novels-grid">
        <el-card 
          v-for="novel in displayedNovels" 
          :key="novel.id" 
          class="novel-card"
          :body-style="{ padding: '0' }"
          shadow="hover"
        >
          <div class="card-status-badge" :class="getStatusClass(novel.status)">
            {{ getStatusText(novel.status) }}
          </div>
          
          <div class="card-content">
            <h3 class="novel-title">{{ novel.title }}</h3>
            
            <div class="novel-meta">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(novel.createTime) }}</span>
            </div>
            
            <p class="novel-description">{{ novel.description || '暂无简介' }}</p>
            
            <div class="novel-progress" v-if="novel.totalChapters">
              <span class="progress-text">处理进度: {{ novel.processedChapters || 0 }}/{{ novel.totalChapters }}</span>
              <el-progress 
                :percentage="novel.totalChapters ? Math.round((novel.processedChapters || 0) / novel.totalChapters * 100) : 0"
                :status="novel.processedChapters >= novel.totalChapters ? 'success' : ''"
                :stroke-width="8"
                :text-inside="true"
              />
            </div>
          </div>
          
          <div class="card-actions">
            <el-button 
              type="primary" 
              @click="$router.push(`/novel/${novel.id}`)"
            >
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button 
              v-if="novel.status === 'COMPLETED'" 
              type="success" 
              @click="goToAnalyze(novel.id)"
            >
              <el-icon><DataAnalysis /></el-icon>
              查看分析
            </el-button>
            <el-button 
              type="danger" 
              @click="confirmDelete(novel)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </el-card>
      </div>
      
      <div class="pagination-container" v-if="novels.length > pageSize">
        <el-pagination
          layout="prev, pager, next, jumper"
          :total="novels.length"
          :page-size="pageSize"
          v-model:current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessageBox, ElMessage } from 'element-plus';
import { View, DataAnalysis, Delete, Upload, Loading, RefreshRight, Calendar } from '@element-plus/icons-vue';
import userService from '@/services/user';
import novelService from '@/services/novel';

export default {
  name: 'MyNovelsView',
  components: {
    View,
    DataAnalysis,
    Delete,
    Upload,
    Loading,
    RefreshRight,
    Calendar
  },
  data() {
    return {
      novels: [],
      loading: true,
      error: null,
      selectedNovel: null,
      currentPage: 1,
      pageSize: 6
    };
  },
  computed: {
    displayedNovels() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      return this.novels.slice(startIndex, endIndex);
    }
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
          } else if (data.data && Array.isArray(data.data)) {
            novels = data.data;
          } else if (data.content && Array.isArray(data.content)) {
            novels = data.content;
          } else if (typeof data === 'object') {
            // 打印对象的所有顶级键，帮助调试
            console.log('API返回对象的所有键:', Object.keys(data));
            
            // 遍历所有键，查找可能包含小说数据的数组
            for (const key in data) {
              if (Array.isArray(data[key]) && data[key].length > 0 && 
                  typeof data[key][0] === 'object' && data[key][0] !== null) {
                console.log(`发现可能的小说数组在键 "${key}"，长度:`, data[key].length);
                novels = [...novels, ...data[key]];
              }
            }
            
            // 如果上面的方法没有找到小说，尝试将对象转换为数组
            if (novels.length === 0) {
              novels = Object.values(data).filter(item => 
                typeof item === 'object' && 
                item !== null && 
                !Array.isArray(item) && 
                item.title
              );
            }
          }
        }
        
        // 过滤掉任何null或undefined值
        novels = novels.filter(novel => novel);
        
        // 如果小说对象不是期望的格式，打印详细日志
        if (novels.length > 0) {
          console.log('小说对象示例:', novels[0]);
        }
        
        this.novels = novels;
        console.log('获取用户小说成功，共' + novels.length + '本:', this.novels);
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
    getStatusClass(status) {
      const classMap = {
        'PENDING': 'status-pending',
        'PROCESSING': 'status-processing',
        'COMPLETED': 'status-completed',
        'FAILED': 'status-failed'
      };
      return classMap[status] || 'status-default';
    },
    goToAnalyze(novelId) {
      this.$router.push(`/novel/${novelId}/visualization`);
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
        
        // 如果当前页没有小说了，且不是第一页，返回上一页
        if (this.displayedNovels.length === 0 && this.currentPage > 1) {
          this.currentPage--;
        }
        
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
    },
    handlePageChange(page) {
      this.currentPage = page;
    }
  }
};
</script>

<style scoped>
.my-novels-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.loading-container, .error-container, .empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 350px;
}

.loading-icon {
  font-size: 48px;
  color: #409EFF;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.novels-summary {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}

.novels-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 25px;
  width: 100%;
}

.novel-card {
  position: relative;
  transition: transform 0.3s ease;
  border-radius: 10px;
  overflow: hidden;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.novel-card:hover {
  transform: translateY(-5px);
}

.card-status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.status-pending {
  background-color: #e6a23c;
}

.status-processing {
  background-color: #409eff;
}

.status-completed {
  background-color: #67c23a;
}

.status-failed {
  background-color: #f56c6c;
}

.card-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.novel-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-top: 0;
  margin-bottom: 16px;
  padding-right: 70px; /* 空间给状态标签 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.novel-meta {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 13px;
  margin-bottom: 14px;
}

.novel-meta .el-icon {
  margin-right: 5px;
}

.novel-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
  min-height: 65px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  flex: 1;
}

.novel-progress {
  margin-bottom: 16px;
}

.progress-text {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.card-actions {
  display: flex;
  gap: 10px;
  padding: 0 20px 20px;
  margin-top: auto;
}

.card-actions .el-button {
  flex: 1;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

@media screen and (max-width: 768px) {
  .novels-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style> 