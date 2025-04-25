<template>
  <div class="app-container">
    <el-config-provider :locale="zhCn">
      <el-container>
        <el-header>
          <header-component />
        </el-header>
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
        <el-footer>
          <footer-component />
        </el-footer>
      </el-container>
    </el-config-provider>
  </div>
</template>

<script>
import { defineComponent, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElConfigProvider, ElMessage } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import HeaderComponent from '@/components/layout/HeaderComponent.vue'
import FooterComponent from '@/components/layout/FooterComponent.vue'
import authService from '@/services/auth'

export default defineComponent({
  name: 'App',
  components: {
    HeaderComponent,
    FooterComponent,
    ElConfigProvider
  },
  setup() {
    const store = useStore();
    
    // 在组件挂载时验证身份认证状态
    onMounted(() => {
      // 检查本地存储中是否有用户信息
      const userStr = localStorage.getItem('user');
      if (userStr) {
        try {
          const userData = JSON.parse(userStr);
          // 检查token是否存在
          if (!userData.token && !userData.accessToken) {
            localStorage.removeItem('user');
            store.dispatch('auth/logout');
            return;
          }
          
          // 开发环境暂时不验证令牌有效性，直接使用本地存储的信息
          if (process.env.NODE_ENV === 'development') {
            store.commit('auth/loginSuccess', userData);
            return;
          }
          
          // 向后端验证令牌有效性
          authService.checkAuthStatus()
            .then(response => {
              const isAuthenticated = response.data?.isAuthenticated;
              
              if (!isAuthenticated) {
                localStorage.removeItem('user');
                store.dispatch('auth/logout');
                ElMessage.warning('登录已过期，请重新登录');
              } else {
                // 更新用户信息
                store.commit('auth/loginSuccess', userData);
              }
            })
            .catch(() => {
              // API拦截器会处理401错误
            });
        } catch (e) {
          localStorage.removeItem('user');
        }
      }
    });
    
    return {
      zhCn
    }
  }
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700&display=swap');

:root {
  --primary-color: #4d7bef;
  --secondary-color: #5e60ce;
  --background-color: #f9fafc;
  --text-color: #333;
  --admin-color: #0b3d91;
  --border-color: #eaedf2;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --radius: 8px;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Noto Sans SC', 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: var(--text-color);
  background-color: var(--background-color);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-image: 
    radial-gradient(circle at 100% 0%, rgba(77, 123, 239, 0.05) 0%, transparent 25%),
    radial-gradient(circle at 0% 80%, rgba(94, 96, 206, 0.05) 0%, transparent 25%);
  background-attachment: fixed;
  background-size: cover;
}

.el-header {
  padding: 0;
  z-index: 999;
}

.el-main {
  flex: 1;
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
}

.el-footer {
  padding: 30px 0;
  background-color: #fff;
  border-top: 1px solid var(--border-color);
}

/* 卡片样式优化 */
.el-card {
  border-radius: var(--radius);
  border: none;
  box-shadow: var(--shadow-sm) !important;
  transition: box-shadow 0.3s;
  overflow: hidden;
}

.el-card:hover {
  box-shadow: var(--shadow-md) !important;
}

/* 按钮样式优化 */
.el-button {
  border-radius: 4px;
  font-weight: 500;
}

.el-button--primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border: none;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 移动设备适配 */
@media (max-width: 768px) {
  .el-main {
    padding: 16px;
  }
}
</style> 