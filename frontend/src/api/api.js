import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const apiClient = axios.create({
  // 不设置baseURL，由各个API调用自行加上前缀
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000
});

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 这里可以添加鉴权token等
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers['Authorization'] = `Bearer ${token}`;
    // }
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
    return response.data;
  },
  error => {
    // 统一处理错误
    let message = '服务器错误，请稍后再试';
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status;
      switch (status) {
        case 400:
          message = '请求参数错误';
          break;
        case 401:
          message = '未授权，请重新登录';
          // 可以在这里处理登出逻辑
          break;
        case 403:
          message = '拒绝访问';
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          message = '服务器内部错误';
          break;
        default:
          message = `请求错误 (${status})`;
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