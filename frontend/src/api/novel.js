import apiClient from './api';

// 小说相关的API服务
const novelService = {
  // 获取小说列表
  async getNovelList() {
    return apiClient.get('/novels');
  },

  // 获取小说详情
  async getNovelDetail(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/${novelId}`);
  },

  // 获取小说处理状态
  async getNovelStatus(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    try {
      return await apiClient.get(`/novels/${novelId}/status`);
    } catch (error) {
      // 如果是404错误，返回一个格式化的状态对象而不是抛出异常
      if (error.response && error.response.status === 404) {
        return {
          status: 'NOT_FOUND',
          error: `小说不存在: ${novelId}`,
          processedChapters: 0,
          totalChapters: 0
        };
      }
      // 其他错误仍然抛出
      throw error;
    }
  },

  // 上传小说文件
  async uploadNovel(formData) {
    const headers = { 'Content-Type': 'multipart/form-data' };
    return apiClient.post('/novels/upload', formData, { headers });
  },
  
  // 从URL上传小说
  async uploadNovelFromUrl(formData) {
    const headers = { 'Content-Type': 'multipart/form-data' };
    return apiClient.post('/novels/upload-from-url', formData, { headers });
  },

  // 获取小说摘要分析
  async getSummaryAnalysis(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/${novelId}/summary`);
  },

  // 获取小说章节列表
  async getChapterList(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/${novelId}/chapters`);
  },

  // 获取标签分析
  async getTagAnalysis(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/${novelId}/tags`);
  },

  // 获取数据可视化
  async getVisualizations(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/${novelId}/visualizations`);
  }
};

export default novelService; 