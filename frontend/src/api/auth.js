import axios from 'axios';

const API_URL = process.env.VUE_APP_API_URL || 'http://localhost:8080/api/auth/';

class AuthService {
  /**
   * 用户登录
   * @param {string} username 用户名
   * @param {string} password 密码
   * @returns {Promise} 登录结果
   */
  login(username, password) {
    return axios
      .post(API_URL + 'signin', {
        username,
        password
      })
      .then(response => {
        // 检查响应中是否包含令牌
        console.log('登录响应:', response.data);
        
        // 标准化存储格式
        const userData = {
          ...response.data,
          // 兼容性处理
          accessToken: response.data.token || response.data.accessToken,
          // 确保角色信息存在
          roles: response.data.roles || []
        };
        
        // 确保角色数组正确格式化
        if (userData.roles && !Array.isArray(userData.roles)) {
          if (typeof userData.roles === 'string') {
            userData.roles = [userData.roles];
          } else {
            userData.roles = [];
          }
        }
        
        // 打印角色信息方便调试
        console.log('用户角色:', userData.roles);
        
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
    return axios.post(API_URL + 'signup', {
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
    const user = JSON.parse(localStorage.getItem('user'));
    return axios.post(API_URL + 'refresh-token', {
      refreshToken: user.refreshToken
    }).then(response => {
      if (response.data.token) {
        localStorage.setItem('user', JSON.stringify(response.data));
      }
      return response.data;
    });
  }
  
  /**
   * 检查用户是否为管理员
   * @returns {boolean} 是否为管理员
   */
  isAdmin() {
    const user = JSON.parse(localStorage.getItem('user'));
    if (user && user.roles) {
      return Array.isArray(user.roles) 
        ? user.roles.includes('ROLE_ADMIN')
        : user.roles === 'ROLE_ADMIN';
    }
    return false;
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