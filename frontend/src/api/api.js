import axios from 'axios';

// 创建axios实例
const apiClient = axios.create({
  baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        const token = user.accessToken || user.token;
        if (token) {
          // 在请求头中添加token
          config.headers['Authorization'] = `Bearer ${token}`;
        }
      } catch (e) {
        // 解析失败时不添加token
      }
    }
    
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    return response.data;
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 未授权，清除本地存储中的用户信息
      localStorage.removeItem('user');
      
      // 如果不在登录页面，重定向到登录页面
      if (window.location.pathname !== '/login') {
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
      }
    }
    
    return Promise.reject(error);
  }
);

export default apiClient; 