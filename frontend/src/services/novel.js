import api from './api';

const novelService = {
  /**
   * 获取所有小说
   * @returns {Promise} 包含所有小说的Promise
   */
  getAllNovels() {
    return api.get('/novels');
  },
  
  /**
   * 根据ID获取小说
   * @param {number} id - 小说ID
   * @returns {Promise} 包含小说数据的Promise
   */
  getNovelById(id) {
    return api.get(`/novels/${id}`);
  },
  
  /**
   * 获取小说处理状态
   * @param {number} id - 小说ID
   * @returns {Promise} 包含处理状态的Promise
   */
  getNovelStatus(id) {
    return api.get(`/novels/${id}/status`);
  },
  
  /**
   * 上传小说文件
   * @param {FormData} formData - 包含文件和元数据的表单数据
   * @returns {Promise} 包含上传结果的Promise
   */
  uploadNovel(formData) {
    return api.post('/novels/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },
  
  /**
   * 获取小说章节
   * @param {number} novelId - 小说ID
   * @returns {Promise} 包含章节的Promise
   */
  getChapters(novelId) {
    return api.get(`/novels/${novelId}/chapters`);
  },
  
  /**
   * 获取小说标签
   * @param {number} novelId - 小说ID
   * @returns {Promise} 包含标签的Promise
   */
  getTags(novelId) {
    return api.get(`/novels/${novelId}/tags`);
  },
  
  /**
   * 删除小说
   * @param {number} id - 小说ID
   * @returns {Promise} 包含删除结果的Promise
   */
  deleteNovel(id) {
    return api.delete(`/novels/${id}`);
  },
  
  /**
   * 刷新小说标签
   * 基于深度内容分析更新小说标签
   * @param {number} id - 小说ID
   * @returns {Promise} 包含刷新结果的Promise
   */
  refreshNovelTags(id) {
    return api.post(`/novels/${id}/refresh-tags`);
  }
};

export default novelService; 