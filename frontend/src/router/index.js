/* eslint-disable */
import { createRouter, createWebHistory } from 'vue-router'
// 预加载核心组件
import HomeView from '../views/HomeView.vue'
import NovelDetailView from '../views/NovelDetailView.vue'
import UploadView from '../views/UploadView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProfileView from '../views/ProfileView.vue'
import MyNovelsView from '../views/MyNovelsView.vue'
import NovelVisualizationView from '../views/NovelVisualizationView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { title: '首页 - 小说智析' }
  },
  {
    path: '/upload',
    name: 'upload',
    component: UploadView,
    meta: {
      title: '上传小说 - 小说智析',
      requiresAuth: true
    }
  },
  {
    path: '/novel/:id',
    name: 'novel-detail',
    component: NovelDetailView,
    meta: { title: '小说分析结果 - 小说智析' }
  },
  {
    path: '/novel/:id/visualization',
    name: 'novel-visualization',
    component: NovelVisualizationView,
    meta: { title: '小说可视化分析 - 小说智析' }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      title: '登录 - 小说智析',
      hideForAuth: true
    }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: {
      title: '注册 - 小说智析',
      hideForAuth: true
    }
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfileView,
    meta: { 
      title: '个人中心 - 小说智析',
      requiresAuth: true
    }
  },
  {
    path: '/my-novels',
    name: 'myNovels',
    component: MyNovelsView,
    meta: {
      title: '我的小说 - 小说智析',
      requiresAuth: true
    }
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue'),
    meta: { title: '关于 - 小说智析' }
  },
  // 管理员路由
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/AdminView.vue'),
    meta: {
      title: '小说智析 - 管理后台',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/dashboard',
    name: 'admin-dashboard',
    component: () => import('../views/admin/DashboardView.vue'),
    meta: { 
      title: '管理员仪表盘 - 小说智析',
      requiresAuth: true,
      adminOnly: true
    }
  },
  {
    path: '/admin/novel/:id/visualization',
    name: 'admin-novel-visualization',
    component: () => import('../views/NovelVisualizationView.vue'),
    meta: { 
      title: '小说可视化分析 - 小说智析',
      requiresAuth: true,
      adminOnly: true
    }
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('../views/admin/UserManagementView.vue'),
    meta: { 
      title: '用户管理 - 小说智析',
      requiresAuth: true,
      adminOnly: true
    }
  },
  {
    path: '/admin/novels',
    name: 'admin-novels',
    component: () => import('../views/admin/NovelManagementView.vue'),
    meta: { 
      title: '小说管理 - 小说智析',
      requiresAuth: true,
      adminOnly: true
    }
  },
  {
    path: '/admin/logs',
    name: 'admin-logs',
    component: () => import('../views/admin/SystemLogsView.vue'),
    meta: { 
      title: '系统日志 - 小说智析',
      requiresAuth: true,
      adminOnly: true 
    }
  },
  {
    path: '/admin/visualization',
    name: 'admin-visualization',
    component: () => import('../views/admin/VisualizationView.vue'),
    meta: { 
      title: '小说可视化 - 小说智析',
      requiresAuth: true,
      adminOnly: true 
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/NotFoundView.vue'),
    meta: { title: '404 - 小说智析' }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 设置页面标题和权限检查
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '小说智析'
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const userStr = localStorage.getItem('user')
    
    if (!userStr) {
      // 未登录，跳转到登录页面
      next({ name: 'login' })
      return
    }
    
    const user = JSON.parse(userStr)
    
    // 检查是否是管理员页面，需要管理员权限
    if (to.meta.adminOnly) {
      // 检查用户是否有ADMIN角色
      const isAdmin = user.roles && user.roles.includes('ROLE_ADMIN')
      
      if (!isAdmin) {
        // 不是管理员，跳转到首页
        next({ name: 'home' })
        return
      }
    }
  }
  
  next()
})

export default router 