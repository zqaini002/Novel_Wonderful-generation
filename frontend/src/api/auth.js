import apiClient from './api';

class AuthService {
  /**
   * 用户登录
   * @param {string} username 用户名
   * @param {string} password 密码
   * @returns {Promise} 登录结果
   */
  login(username, password) {
    return apiClient
      .post('/auth/signin', {
        username,
        password
      })
      .then(response => {
        // 标准化存储格式
        const userData = {
          ...response,
          // 兼容性处理
          accessToken: response.token || response.accessToken,
          // 确保角色信息存在
          roles: response.roles || []
        };
        
        // 确保角色数组正确格式化
        if (userData.roles && !Array.isArray(userData.roles)) {
          userData.roles = typeof userData.roles === 'string' ? [userData.roles] : [];
        }
        
        // 保存用户信息到本地存储
        localStorage.setItem('user', JSON.stringify(userData));
        
        return userData;
      });
  }

  /**
   * 用户登出
   */
  logout() {
    localStorage.removeItem('user');
  }

  /**
   * 用户注册
   * @param {string} username 用户名
   * @param {string} email 邮箱
   * @param {string} password 密码
   * @returns {Promise} 注册结果
   */
  register(username, email, password) {
    return apiClient.post('/auth/signup', {
      username,
      email,
      password
    });
  }

  /**
   * 刷新Token
   * @returns {Promise} 刷新结果
   */
  refreshToken() {
    const user = this.getCurrentUser();
    if (!user || !user.refreshToken) {
      return Promise.reject(new Error('无效的刷新令牌'));
    }
    
    return apiClient.post('/auth/refresh-token', {
      refreshToken: user.refreshToken
    }).then(response => {
      if (response.token || response.accessToken) {
        // 更新本地存储中的用户信息
        const updatedUser = {
          ...user,
          token: response.token || response.accessToken,
          accessToken: response.token || response.accessToken
        };
        localStorage.setItem('user', JSON.stringify(updatedUser));
      }
      return response;
    });
  }
  
  /**
   * 检查用户是否为管理员
   * @returns {boolean} 是否为管理员
   */
  isAdmin() {
    const user = this.getCurrentUser();
    if (!user || !user.roles) return false;
    
    return Array.isArray(user.roles) 
      ? user.roles.includes('ROLE_ADMIN')
      : user.roles === 'ROLE_ADMIN';
  }
  
  /**
   * 获取当前用户信息
   * @returns {Object|null} 用户信息
   */
  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    
    try {
      return JSON.parse(userStr);
    } catch (e) {
      console.error('解析用户信息失败:', e);
      return null;
    }
  }
}

export default new AuthService(); 