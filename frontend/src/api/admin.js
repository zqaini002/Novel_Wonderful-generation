import apiClient from './api';

// 管理员相关的API服务
const adminService = {
  // 获取仪表盘统计数据
  async getDashboardStats() {
    return apiClient.get('/admin/dashboard');
  },

  // 获取所有用户列表
  async getAllUsers() {
    return apiClient.get('/admin/users');
  },

  // 获取用户详情
  async getUserDetail(userId) {
    if (!userId) {
      throw new Error('用户ID不能为空');
    }
    return apiClient.get(`/admin/users/${userId}`);
  },

  // 修改用户状态（启用/禁用）
  async updateUserStatus(userId, enabled) {
    if (!userId) {
      throw new Error('用户ID不能为空');
    }
    return apiClient.put(`/admin/users/${userId}/status?enabled=${enabled}`);
  },

  // 删除用户
  async deleteUser(userId) {
    if (!userId) {
      throw new Error('用户ID不能为空');
    }
    return apiClient.delete(`/admin/users/${userId}`);
  },

  // 获取所有小说列表（管理员视图）
  async getAllNovels() {
    return apiClient.get('/admin/novels');
  },

  // 删除小说
  async deleteNovel(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.delete(`/admin/novels/${novelId}`);
  },

  // 获取系统日志
  async getSystemLogs(page = 0, size = 20) {
    return apiClient.get(`/admin/logs?page=${page}&size=${size}`);
  },

  // 清理系统缓存
  async clearSystemCache() {
    return apiClient.post('/admin/cache/clear');
  }
};

export default adminService; 
 