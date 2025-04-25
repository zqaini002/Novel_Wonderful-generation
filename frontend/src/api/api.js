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
    // 调试输出原始响应
    console.log('API响应原始数据:', {
      url: response.config.url,
      data: response.data
    });
    
    // 检查并处理小说数据，确保数据格式一致
    if (response.config.url.includes('/novels') || response.config.url.includes('/admin/novels') || 
        response.config.url.includes('/user/novels')) {
      // 如果响应包含小说列表数据
      if (response.data && response.data.novels && Array.isArray(response.data.novels)) {
        console.log('检测到novels数组格式，长度:', response.data.novels.length);
        // 确保每个小说对象都有所需的属性
        response.data.novels = response.data.novels.map(novel => {
          return normalizeNovelData(novel);
        });
      }
      // 如果响应本身就是小说数组
      else if (Array.isArray(response.data)) {
        console.log('检测到直接数组格式，长度:', response.data.length);
        response.data = response.data.map(novel => {
          return normalizeNovelData(novel);
        });
      }
      // 如果响应是单个小说对象
      else if (response.data && response.data.id) {
        console.log('检测到单个小说对象');
        response.data = normalizeNovelData(response.data);
      }
      // 检查其他可能的数据格式
      else if (response.data && typeof response.data === 'object') {
        console.log('检测到其他对象格式，尝试解析');
        // 检查是否有其他类型的数据容器
        for (const key of ['data', 'content', 'items', 'list']) {
          if (response.data[key] && Array.isArray(response.data[key])) {
            console.log(`检测到${key}数组格式，长度:`, response.data[key].length);
            response.data[key] = response.data[key].map(novel => {
              return normalizeNovelData(novel);
            });
            
            // 如果没有novels字段，但有其他数组字段，将其复制到novels中
            if (!response.data.novels) {
              response.data.novels = response.data[key];
            }
          }
        }
        
        // 尝试提取非数组字段中的小说数据
        const possibleNovels = [];
        for (const key in response.data) {
          if (typeof response.data[key] === 'object' && 
              response.data[key] !== null && 
              !Array.isArray(response.data[key]) &&
              response.data[key].title) {
            possibleNovels.push(normalizeNovelData(response.data[key]));
          }
        }
        
        if (possibleNovels.length > 0 && !response.data.novels) {
          console.log('从对象中提取到小说数据，数量:', possibleNovels.length);
          response.data.novels = possibleNovels;
        }
      }
    }
    
    // 确保返回最终处理后的数据
    console.log('API响应规范化后的数据:', response.data);
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
    
    // 检查是否是关于获取结构化数据的错误
    if (error.config && error.config.url && error.config.url.includes('/structure')) {
      console.warn('结构化数据API出错，使用模拟数据');
      
      // 为结构分析模拟数据
      const mainStructure = [
        { name: '引子', value: 10, startChapter: 1, endChapter: 3 },
        { name: '故事开始', value: 20, startChapter: 4, endChapter: 10 },
        { name: '中间发展', value: 40, startChapter: 11, endChapter: 20 },
        { name: '高潮', value: 20, startChapter: 21, endChapter: 27 },
        { name: '结局', value: 10, startChapter: 28, endChapter: 30 }
      ];
      
      const detailStructure = [
        { name: '主角介绍', value: 3, startChapter: 1, endChapter: 3 },
        { name: '世界设定', value: 4, startChapter: 4, endChapter: 7 },
        { name: '冲突起源', value: 3, startChapter: 8, endChapter: 10 },
        { name: '第一次冒险', value: 6, startChapter: 11, endChapter: 16 },
        { name: '突破瓶颈', value: 4, startChapter: 17, endChapter: 20 },
        { name: '最终对决', value: 6, startChapter: 21, endChapter: 26 },
        { name: '结局', value: 4, startChapter: 27, endChapter: 30 }
      ];
      
      return Promise.resolve({
        data: {
          mainStructure,
          detailStructure,
          totalChapters: 30,
          structure: [
            {
              name: '整体架构',
              children: mainStructure.map(item => ({
                name: item.name,
                value: item.value,
                startChapter: item.startChapter,
                endChapter: item.endChapter
              }))
            },
            {
              name: '细节架构',
              children: detailStructure.map(item => ({
                name: item.name,
                value: item.value,
                startChapter: item.startChapter,
                endChapter: item.endChapter
              }))
            }
          ]
        }
      });
    }
    
    return Promise.reject(error);
  }
);

/**
 * 标准化小说数据，确保所有必要的字段都存在
 * @param {Object} novel 小说数据对象
 * @returns {Object} 处理后的小说数据对象
 */
function normalizeNovelData(novel) {
  if (!novel) return {};
  
  // 当前时间
  const currentTime = new Date().toISOString();
  
  // 默认值
  const defaults = {
    id: 0,
    title: '未知标题',
    author: '未知作者',
    status: 'UNKNOWN',
    processingStatus: 'UNKNOWN',
    description: '',
    chapterCount: 0,
    totalChapters: 0,
    processedChapters: 0,
    wordCount: 0,
    createdAt: currentTime,
    created_at: currentTime,
    created: currentTime,
    createTime: currentTime,
    uploadDate: currentTime,
    updatedAt: currentTime,
    updated_at: currentTime,
    updated: currentTime,
    updateTime: currentTime
  };
  
  // 合并数据，确保所有字段都有值
  const normalized = { ...defaults, ...novel };
  
  // 处理状态字段，有些API用status，有些用processingStatus
  if (novel.status && !novel.processingStatus) {
    normalized.processingStatus = novel.status;
  }
  if (novel.processingStatus && !novel.status) {
    normalized.status = novel.processingStatus;
  }
  
  // 处理章节数量
  if (novel.totalChapters && !novel.chapterCount) {
    normalized.chapterCount = novel.totalChapters;
  }
  if (novel.chapterCount && !novel.totalChapters) {
    normalized.totalChapters = novel.chapterCount;
  }
  
  // 处理创建/更新时间字段
  // 1. 检查并同步所有可能的日期字段命名
  // 创建时间字段同步
  const createdFields = ['createdAt', 'created_at', 'created', 'createTime', 'uploadDate'];
  // 找到第一个有值的创建时间字段
  let validCreatedValue = null;
  for (const field of createdFields) {
    if (novel[field]) {
      validCreatedValue = novel[field];
      break;
    }
  }
  
  // 更新所有创建时间字段
  if (validCreatedValue) {
    for (const field of createdFields) {
      normalized[field] = validCreatedValue;
    }
  }
  
  // 更新时间字段同步
  const updatedFields = ['updatedAt', 'updated_at', 'updated', 'updateTime'];
  // 找到第一个有值的更新时间字段
  let validUpdatedValue = null;
  for (const field of updatedFields) {
    if (novel[field]) {
      validUpdatedValue = novel[field];
      break;
    }
  }
  
  // 更新所有更新时间字段
  if (validUpdatedValue) {
    for (const field of updatedFields) {
      normalized[field] = validUpdatedValue;
    }
  }
  
  // 2. 确保日期字段是有效的ISO字符串
  try {
    // 处理各种格式的日期字符串，并转换为ISO格式
    const normalizeDate = (dateValue) => {
      if (!dateValue) return currentTime;
      
      // 已经是Date对象
      if (dateValue instanceof Date) {
        return dateValue.toISOString();
      }
      
      // 字符串类型处理
      if (typeof dateValue === 'string') {
        // MySQL日期格式 YYYY-MM-DD HH:MM:SS
        if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(dateValue)) {
          const date = new Date(dateValue.replace(' ', 'T'));
          return isNaN(date.getTime()) ? currentTime : date.toISOString();
        }
        // ISO格式日期字符串
        else if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(dateValue)) {
          const date = new Date(dateValue);
          return isNaN(date.getTime()) ? currentTime : date.toISOString();
        }
        // MySQL日期格式没有时间部分 YYYY-MM-DD
        else if (/^\d{4}-\d{2}-\d{2}$/.test(dateValue)) {
          const date = new Date(dateValue);
          return isNaN(date.getTime()) ? currentTime : date.toISOString();
        }
        // Unix时间戳（毫秒）
        else if (/^\d{13}$/.test(dateValue)) {
          const date = new Date(parseInt(dateValue));
          return isNaN(date.getTime()) ? currentTime : date.toISOString();
        }
        // Unix时间戳（秒）
        else if (/^\d{10}$/.test(dateValue)) {
          const date = new Date(parseInt(dateValue) * 1000);
          return isNaN(date.getTime()) ? currentTime : date.toISOString();
        }
      }
      // 数字类型处理
      else if (typeof dateValue === 'number') {
        // 检查是毫秒还是秒级时间戳
        const timestamp = dateValue > 10000000000 ? dateValue : dateValue * 1000;
        const date = new Date(timestamp);
        return isNaN(date.getTime()) ? currentTime : date.toISOString();
      }
      
      // 尝试直接转换
      try {
        const date = new Date(dateValue);
        return isNaN(date.getTime()) ? currentTime : date.toISOString();
      } catch (e) {
        return currentTime;
      }
    };
    
    // 标准化所有创建时间字段
    for (const field of createdFields) {
      normalized[field] = normalizeDate(normalized[field]);
    }
    
    // 标准化所有更新时间字段
    for (const field of updatedFields) {
      normalized[field] = normalizeDate(normalized[field]);
    }
  } catch (e) {
    console.error('日期格式化错误:', e);
    // 出错时使用当前时间
    for (const field of createdFields) {
      normalized[field] = currentTime;
    }
    for (const field of updatedFields) {
      normalized[field] = currentTime;
    }
  }
  
  return normalized;
}

export default apiClient; 