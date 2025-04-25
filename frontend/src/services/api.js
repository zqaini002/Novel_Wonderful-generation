import axios from 'axios';

// 创建axios实例
const API = axios.create({
  baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8080/api',
  timeout: 10000,  // 请求超时时间
  withCredentials: true,  // 允许携带跨域cookie
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

// 请求拦截器
API.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        // 支持多种token字段名
        const token = user.token || user.accessToken;
        if (token) {
          // 在请求头中添加token
          config.headers['Authorization'] = `Bearer ${token}`;
        }
      } catch (e) {
        // 解析用户信息失败，不添加token
      }
    }
    
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
API.interceptors.response.use(
  response => {
    return response.data;
  },
  error => {
    // 处理401错误 - 未认证
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('user');
      
      // 如果不是在登录页，则重定向到登录页
      if (window.location.pathname !== '/login') {
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
      }
    }
    
    return Promise.reject(error);
  }
);

export default API; 