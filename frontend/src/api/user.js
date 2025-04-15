import apiClient from './api'

// 用户相关的API服务
const userService = {
  /**
   * 获取当前用户信息
   * @returns {Promise} 返回用户详细信息
   */
  getCurrentUser() {
    return apiClient.get('/user/me');
  },

  /**
   * 更新用户个人资料
   * @param {Object} userData 用户数据对象，包含需要更新的字段
   * @returns {Promise} 返回更新结果
   */
  updateUserProfile(userData) {
    return apiClient.put('/user/profile', userData);
  },

  /**
   * 修改用户密码
   * @param {String} oldPassword 旧密码
   * @param {String} newPassword 新密码
   * @returns {Promise} 返回修改结果
   */
  changePassword(oldPassword, newPassword) {
    return apiClient.post('/user/change-password', { oldPassword, newPassword });
  },

  /**
   * 获取用户统计信息
   * @returns {Promise} 返回用户统计数据
   */
  getUserStats() {
    return apiClient.get('/user/stats');
  },

  /**
   * 获取用户上传的小说列表
   * @returns {Promise} 返回用户上传的小说列表
   */
  getUserNovels() {
    return apiClient.get('/user/novels');
  },

  /**
   * 上传用户头像
   * @param {FormData} formData 包含头像文件的FormData
   * @returns {Promise} 返回上传结果
   */
  uploadAvatar(formData) {
    const headers = { 'Content-Type': 'multipart/form-data' };
    return apiClient.post('/user/avatar', formData, { headers });
  },

  /**
   * 删除用户账户
   * @param {String} password 用户密码（确认身份）
   * @returns {Promise} 返回删除结果
   */
  deleteAccount(password) {
    return apiClient.post('/user/delete-account', { password });
  }
};

export default userService; 