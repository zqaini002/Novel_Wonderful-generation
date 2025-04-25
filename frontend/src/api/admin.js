import apiClient from './api';

// 管理员相关的API服务
const adminService = {
  // 获取仪表盘统计数据
  async getDashboardStats() {
    return apiClient.get('/admin/dashboard');
  },

  // 获取详细系统统计信息
  async getDetailedSystemStats() {
    return apiClient.get('/admin/dashboard/detailed');
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
    return apiClient.put(`/admin/users/${userId}/status`, { enabled });
  },

  // 修改用户信息（包括密码）
  async updateUserInfo(userId, userData) {
    if (!userId) {
      throw new Error('用户ID不能为空');
    }
    return apiClient.put(`/admin/users/${userId}`, userData);
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
  
  // 获取所有小说列表（包括已删除的）
  async getAllNovelsIncludeDeleted() {
    return apiClient.get('/admin/novels/all');
  },
  
  // 获取已删除的小说列表
  async getDeletedNovels() {
    return apiClient.get('/admin/novels/deleted');
  },

  // 删除小说
  async deleteNovel(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.delete(`/admin/novels/${novelId}`);
  },
  
  // 恢复小说
  async restoreNovel(novelId) {
    if (!novelId) {
      throw new Error('小说ID不能为空');
    }
    return apiClient.post(`/admin/novels/${novelId}/restore`);
  },

  // 获取系统日志
  async getSystemLogs(page = 0, size = 20, filters = {}) {
    let url = `/admin/logs?page=${page}&size=${size}`;
    
    // 添加日志级别筛选
    if (filters.level && filters.level !== 'ALL') {
      url += `&level=${filters.level}`;
    }
    
    // 添加日期范围筛选
    if (filters.startDate) {
      url += `&startDate=${filters.startDate}`;
    }
    
    if (filters.endDate) {
      url += `&endDate=${filters.endDate}`;
    }
    
    // 添加搜索关键词
    if (filters.query) {
      url += `&query=${encodeURIComponent(filters.query)}`;
    }
    
    // 添加用户ID筛选
    if (filters.userId) {
      // 确保userId作为字符串传递
      url += `&userId=${String(filters.userId)}`;
    }
    
    // 输出完整URL供调试
    console.log('系统日志请求URL:', url);
    
    return apiClient.get(url);
  },

  // 清理系统缓存
  async clearSystemCache() {
    return apiClient.post('/admin/cache/clear');
  }
};

export default adminService; 
 