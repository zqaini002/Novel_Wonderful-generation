import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue' // 预加载首页组件

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
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue'),
    meta: { title: '关于 - 小说精读助手' }
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

// 设置页面标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '小说精读助手'
  next()
})

export default router 