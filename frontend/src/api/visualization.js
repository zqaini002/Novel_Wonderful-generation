import apiClient from './api';

// 小说可视化相关的API服务
const visualizationService = {
  /**
   * 获取小说关键词云数据
   * @param {number} novelId 小说ID
   * @returns {Promise} 关键词云数据
   */
  getKeywordCloudData(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/visualization/${novelId}/keywords`);
  },

  /**
   * 获取小说情节波动图数据
   * @param {number} novelId 小说ID
   * @returns {Promise} 情节波动图数据
   */
  getEmotionalFluctuationData(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/visualization/${novelId}/emotional`);
  },

  /**
   * 获取小说结构分析数据
   * @param {number} novelId 小说ID
   * @returns {Promise} 结构分析数据
   */
  getStructureAnalysisData(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/visualization/${novelId}/structure`);
  },

  /**
   * 获取小说人物关系网络数据
   * @param {number} novelId 小说ID
   * @returns {Promise} 人物关系网络数据
   */
  getCharacterRelationshipData(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/visualization/${novelId}/characters`);
  },

  /**
   * 获取小说场景分布数据
   * @param {number} novelId 小说ID
   * @returns {Promise} 场景分布数据
   */
  getSceneDistributionData(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/visualization/${novelId}/scenes`);
  },

  /**
   * 获取小说综合统计数据
   * @param {number} novelId 小说ID
   * @returns {Promise} 综合统计数据
   */
  getNovelStatisticsData(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/visualization/${novelId}/statistics`);
  },

  /**
   * 获取小说所有可视化数据
   * @param {number} novelId 小说ID
   * @returns {Promise} 所有可视化数据
   */
  getAllVisualizationData(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.get(`/novels/visualization/${novelId}/all`);
  }
};

export default visualizationService; 