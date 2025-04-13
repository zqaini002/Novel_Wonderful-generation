import apiClient from './api';

// 小说相关的API服务
const novelService = {
  // 获取小说列表
  async getNovelList() {
    return apiClient.get('/api/novels');
  },

  // 获取小说详情
  async getNovelDetail(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/api/novels/${novelId}`);
  },

  // 获取小说处理状态
  async getNovelStatus(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/api/novels/${novelId}/status`);
  },

  // 上传小说
  async uploadNovel(formData) {
    const headers = { 'Content-Type': 'multipart/form-data' };
    return apiClient.post('/api/novels/upload', formData, { headers });
  },

  // 获取小说摘要分析
  async getSummaryAnalysis(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/api/novels/${novelId}/summary`);
  },

  // 获取小说章节列表
  async getChapterList(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/api/novels/${novelId}/chapters`);
  },

  // 获取标签分析
  async getTagAnalysis(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/api/novels/${novelId}/tags`);
  },

  // 获取数据可视化
  async getVisualizations(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/api/novels/${novelId}/visualizations`);
  }
};

export default novelService; 