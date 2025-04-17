<template>
  <div class="header-container">
    <el-menu
      :default-active="activeIndex"
      class="menu"
      mode="horizontal"
      router
      :ellipsis="false">
      <div class="logo">
        <router-link to="/">
          <div class="logo-content">
            <el-icon class="logo-icon"><Reading /></el-icon>
            <h1>小说智析</h1>
          </div>
        </router-link>
      </div>
      <div class="menu-items">
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/upload">
          <el-icon><Upload /></el-icon>
          <span>上传小说</span>
        </el-menu-item>
        <el-menu-item index="/about">
          <el-icon><InfoFilled /></el-icon>
          <span>关于</span>
        </el-menu-item>
      </div>
      <div class="user-area">
        <el-dropdown v-if="isLoggedIn" trigger="click">
          <div class="avatar-wrapper">
            <el-avatar :size="32" :src="avatarUrl"></el-avatar>
            <span class="username">{{ displayName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToProfile">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-dropdown-item>
              <el-dropdown-item @click="goToMyNovels">
                <el-icon><Collection /></el-icon>
                <span>我的小说</span>
              </el-dropdown-item>
              <el-dropdown-item v-if="isAdmin" @click="goToDashboard" class="admin-function">
                <el-icon><Monitor /></el-icon>
                <span>管理员仪表盘</span>
              </el-dropdown-item>
              <el-dropdown-item v-if="isAdmin" @click="goToSystemLogs" class="admin-function">
                <el-icon><Document /></el-icon>
                <span>系统日志</span>
              </el-dropdown-item>
              <el-dropdown-item divided @click="logout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <div v-else class="auth-buttons">
          <el-button type="primary" size="small" @click="login">
            <el-icon><Key /></el-icon>
            <span>登录</span>
          </el-button>
          <el-button size="small" @click="register">
            <el-icon><UserFilled /></el-icon>
            <span>注册</span>
          </el-button>
        </div>
      </div>
    </el-menu>
  </div>
</template>

<script>
import { defineComponent, computed } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { 
  HomeFilled, 
  Upload, 
  InfoFilled, 
  User, 
  ArrowDown, 
  Collection, 
  SwitchButton,
  Reading,
  Key,
  UserFilled,
  Monitor,
  Document
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default defineComponent({
  name: 'HeaderComponent',
  components: {
    HomeFilled, 
    Upload, 
    InfoFilled, 
    User, 
    ArrowDown, 
    Collection, 
    SwitchButton,
    Reading,
    Key,
    UserFilled,
    Monitor,
    Document
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()
    
    // 计算当前激活的菜单项
    const activeIndex = computed(() => route.path)
    
    // 从store获取登录状态
    const isLoggedIn = computed(() => store.getters['auth/isLoggedIn'])
    
    // 获取当前用户信息
    const currentUser = computed(() => store.getters['auth/currentUser'])
    
    // 获取用户显示名称
    const displayName = computed(() => {
      if (!currentUser.value) return '用户'
      
      // 优先显示昵称，其次是用户名
      return currentUser.value.nickname || 
             currentUser.value.username || 
             currentUser.value.name || 
             '用户'
    })
    
    // 获取用户头像
    const avatarUrl = computed(() => {
      if (currentUser.value && currentUser.value.avatar) {
        return currentUser.value.avatar
      }
      // 默认头像
      return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
    })
    
    // 检查用户是否为管理员
    const isAdmin = computed(() => {
      if (!currentUser.value || !currentUser.value.roles) {
        return false
      }
      
      return currentUser.value.roles.includes('ROLE_ADMIN')
    })
    
    // 登录功能
    const login = () => {
      router.push('/login')
    }
    
    // 注册功能
    const register = () => {
      router.push('/register')
    }
    
    // 登出功能
    const logout = () => {
      store.dispatch('auth/logout')
      router.push('/login')
      ElMessage.success('已退出登录')
    }
    
    // 跳转到管理员仪表盘
    const goToDashboard = () => {
      router.push('/admin/dashboard')
    }
    
    // 跳转到个人中心
    const goToProfile = () => {
      router.push('/profile')
    }

    // 跳转到我的小说
    const goToMyNovels = () => {
      router.push('/my-novels')
    }
    
    // 跳转到系统日志
    const goToSystemLogs = () => {
      router.push('/admin/logs')
    }
    
    return {
      activeIndex,
      isLoggedIn,
      displayName,
      avatarUrl,
      isAdmin,
      login,
      register,
      logout,
      goToDashboard,
      goToProfile,
      goToMyNovels,
      goToSystemLogs
    }
  }
})
</script>

<style scoped>
.header-container {
  width: 100%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
}

.menu {
  display: flex;
  height: 64px;
  padding: 0 20px;
  background: linear-gradient(135deg, #4d7bef 0%, #5e60ce 100%);
  border: none;
}

:deep(.el-menu--horizontal .el-menu-item) {
  height: 64px;
  line-height: 64px;
  border: none !important;
  margin: 0 5px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9) !important;
  padding: 0 20px;
  position: relative;
  transition: all 0.3s;
}

:deep(.el-menu--horizontal .el-menu-item:hover),
:deep(.el-menu--horizontal .el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.15) !important;
  border-radius: 4px;
  color: #ffffff !important;
}

:deep(.el-menu--horizontal .el-menu-item.is-active::after) {
  content: '';
  position: absolute;
  bottom: 10px;
  left: 20px;
  right: 20px;
  height: 3px;
  background-color: #fff;
  border-radius: 1.5px;
}

.logo {
  margin-right: 20px;
  display: flex;
  align-items: center;
  padding-right: 20px;
}

.logo a {
  text-decoration: none;
}

.logo-content {
  display: flex;
  align-items: center;
}

.logo-icon {
  font-size: 28px;
  color: #fff;
  margin-right: 10px;
}

.logo h1 {
  color: #fff;
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
}

.menu-items {
  flex: 1;
  display: flex;
}

.menu-items .el-icon {
  margin-right: 4px;
  font-size: 18px;
}

.user-area {
  display: flex;
  align-items: center;
  color: #fff;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 2px 10px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.avatar-wrapper:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.username {
  margin: 0 8px;
  font-weight: 500;
}

.auth-buttons {
  display: flex;
  gap: 10px;
}

:deep(.auth-buttons .el-button--primary) {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  padding: 8px 20px;
  min-width: 80px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

:deep(.auth-buttons .el-button--primary:hover) {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

:deep(.auth-buttons .el-button--primary:active) {
  transform: translateY(0);
}

:deep(.auth-buttons .el-button--primary::before) {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0),
    rgba(255, 255, 255, 0.2),
    rgba(255, 255, 255, 0)
  );
  transition: left 0.7s ease;
}

:deep(.auth-buttons .el-button--primary:hover::before) {
  left: 100%;
}

:deep(.auth-buttons .el-button--default) {
  border: 2px solid rgba(255, 255, 255, 0.6);
  color: #fff;
  border-radius: 20px;
  padding: 8px 20px;
  min-width: 80px;
  font-weight: 500;
  transition: all 0.3s ease;
  background-color: transparent;
}

:deep(.auth-buttons .el-button--default:hover) {
  background-color: #fff;
  border-color: #fff;
  color: var(--primary-color, #4d7bef);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

:deep(.auth-buttons .el-button--default:active) {
  transform: translateY(0);
}

:deep(.auth-buttons .el-icon) {
  margin-right: 4px;
  font-size: 16px;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  line-height: 1.5;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 8px;
  font-size: 16px;
}

/* 管理员菜单项样式 */
:deep(.el-dropdown-menu .admin-function) {
  color: var(--admin-color);
}

:deep(.el-dropdown-menu .admin-function .el-icon) {
  color: var(--admin-color);
}
</style> 