import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 导入ECharts和wordcloud
import * as echarts from 'echarts'
import 'echarts-wordcloud'

// 将ECharts挂载到全局window对象上，兼容现有组件
window.echarts = echarts;

// 忽略 ResizeObserver 循环错误
const originalConsoleError = window.console.error;
window.console.error = (...args) => {
  if (args[0] && typeof args[0] === 'string' && args[0].includes('ResizeObserver loop')) {
    return;
  }
  
  // 记录ECharts错误以便于调试，提供更详细的日志
  if (args[0] && typeof args[0] === 'string' && args[0].includes('[ECharts]')) {
    console.warn('ECharts Warning/Error:', ...args);
    
    // 添加堆栈跟踪以便于定位问题
    console.warn('ECharts Error Stack:', new Error().stack);
    
    // 如果是严重错误，记录当前echarts实例状态
    if (args[0].includes('Unknown series') || args[0].includes('undefined') || args[0].includes('null')) {
      console.warn('ECharts 严重错误，尝试输出调试信息');
    }
  }
  
  originalConsoleError(...args);
};

// 创建Vue应用
const app = createApp(App)

// 使用插件
app.use(router)
app.use(store)
app.use(ElementPlus, {
  locale: zhCn
})

// 挂载应用
app.mount('#app') 