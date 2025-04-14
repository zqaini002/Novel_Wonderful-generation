/* eslint-disable */
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue' // 预加载首页组件
import NovelDetailView from '../views/NovelDetailView.vue'
import LoginTestView from '../views/LoginTestView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView, // 直接使用导入的组件而非懒加载
    meta: { title: '首页 - 小说精读助手' }
  },
  {
    path: '/upload',
    name: 'upload',
    component: () => import('../views/UploadView.vue'),
    meta: { title: '上传小说 - 小说精读助手' }
  },
  {
    path: '/analyze',
    name: 'analyze',
    component: () => import('../views/AnalyzeView.vue'),
    meta: { title: '处理中 - 小说精读助手' }
  },
  {
    path: '/novel/:id',
    name: 'novel-detail',
    component: () => import('../views/NovelDetailView.vue'),
    meta: { title: '小说分析结果 - 小说精读助手' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录 - 小说精读助手' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '注册 - 小说精读助手' }
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue'),
    meta: { title: '关于 - 小说精读助手' }
  },
  {
    path: '/logintest',
    name: 'logintest',
    component: () => import('../views/LoginTestView.vue'),
    meta: { title: '登录测试 - 小说精读助手' }
  },
  {
    path: '/novel/detail/:id',
    name: 'NovelDetail',
    component: NovelDetailView
  },
  {
    path: '/test/login',
    name: 'LoginTest',
    component: LoginTestView
  },
  // 管理员路由
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/admin/DashboardView.vue'),
    meta: { 
      title: '管理员仪表盘 - 小说精读助手',
      requiresAuth: true,
      adminOnly: true
    }
  },
  {
    path: '/admin/dashboard',
    name: 'admin-dashboard',
    component: () => import('../views/admin/DashboardView.vue'),
    meta: { 
      title: '管理员仪表盘 - 小说精读助手',
      requiresAuth: true,
      adminOnly: true
    }
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('../views/admin/UserManagementView.vue'),
    meta: { 
      title: '用户管理 - 小说精读助手',
      requiresAuth: true,
      adminOnly: true
    }
  },
  // 注释掉缺失的组件引用，现在已创建
  {
    path: '/admin/novels',
    name: 'admin-novels',
    component: () => import('../views/admin/NovelManagementView.vue'),
    meta: { 
      title: '小说管理 - 小说精读助手',
      requiresAuth: true,
      adminOnly: true
    }
  },
  {
    path: '/admin/logs',
    name: 'admin-logs',
    component: () => import('../views/admin/SystemLogsView.vue'),
    meta: { 
      title: '系统日志 - 小说精读助手',
      requiresAuth: true,
      adminOnly: true 
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/NotFoundView.vue'),
    meta: { title: '404 - 小说精读助手' }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 设置页面标题和权限检查
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '小说精读助手'
  
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