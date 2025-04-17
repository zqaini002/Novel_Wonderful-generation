import axios from 'axios';

// 创建axios实例
const apiClient = axios.create({
  baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8080',
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
    // 打印详细的错误信息以便调试
    console.log('API错误拦截器捕获到错误:', error);
    if (error.response) {
      console.log('错误响应详情:', {
        status: error.response.status,
        statusText: error.response.statusText,
        data: error.response.data
      });
    }
    
    // 判断是否是禁用账户错误的方式更全面
    const isDisabledAccount = (() => {
      // 1. 检查错误对象是否已被标记
      if (error.isDisabledAccount) return true;
      
      // 2. 如果没有响应，则不是禁用账户错误
      if (!error.response) return false;
      
      // 3. 检查响应数据
      const responseData = error.response.data;
      
      // 4. 尝试从多种数据格式中提取错误消息
      const errorMessage = 
        (typeof responseData === 'string' ? responseData : '') || 
        (responseData?.message) || 
        (responseData?.error) || 
        error.message || '';
      
      // 5. 只有明确包含禁用关键词才认为是禁用账户错误
      return (errorMessage.includes('禁用') || 
          errorMessage.includes('disabled') || 
          errorMessage.includes('账号已被禁用'));
    })();
    
    // 处理禁用账户错误
    if (isDisabledAccount) {
      console.log('检测到禁用账户错误');
      // 标记错误对象
      error.isDisabledAccount = true;
      error.disabledMessage = '账号已被禁用，请联系管理员';
      
      // 创建一个新的错误对象，添加自定义的message以便于UI层识别
      const disabledError = new Error('账号已被禁用，请联系管理员');
      disabledError.isDisabledAccount = true;
      disabledError.response = error.response;
      return Promise.reject(disabledError);
    }
    
    // 处理其他身份验证错误
    if (error.response && error.response.status === 401 && !isDisabledAccount) {
      // 非禁用导致的401错误，清除用户信息并重定向
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