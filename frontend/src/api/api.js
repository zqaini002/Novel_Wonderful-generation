import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const apiClient = axios.create({
  // 添加baseURL，指向后端API
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000
});

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 添加认证令牌
    try {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        if (user.accessToken) {
          config.headers['Authorization'] = `Bearer ${user.accessToken}`;
        } else if (user.token) {
          // 兼容旧格式
          config.headers['Authorization'] = `Bearer ${user.token}`;
        }
      }
    } catch (error) {
      console.error('处理认证令牌时出错:', error);
    }
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    // 直接返回响应数据
    return response;
  },
  error => {
    // 统一处理错误
    let message = '服务器错误，请稍后再试';
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status;
      switch (status) {
        case 400:
          message = error.response.data.message || '请求参数错误';
          break;
        case 401:
          message = error.response.data.message || '未授权，请重新登录';
          // 清除本地存储的令牌
          localStorage.removeItem('user');
          break;
        case 403:
          message = error.response.data.message || '拒绝访问';
          break;
        case 404:
          message = error.response.data.message || '请求的资源不存在';
          break;
        case 500:
          message = error.response.data.message || '服务器内部错误';
          break;
        default:
          message = error.response.data.message || `请求错误 (${status})`;
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      message = '服务器无响应，请检查网络';
    }
    
    // 使用Element-Plus的消息提示
    ElMessage.error(message);
    console.error('API请求失败:', error);
    
    return Promise.reject(error);
  }
);

export default apiClient; 