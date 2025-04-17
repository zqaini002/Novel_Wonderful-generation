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
      })
      .catch(error => {
        console.log('登录错误详情:', error);
        // 打印完整错误对象的详细内容，便于调试
        console.log('错误响应数据:', error.response ? {
          status: error.response.status,
          statusText: error.response.statusText,
          data: error.response.data,
          headers: error.response.headers
        } : '无响应数据');

        // 1. 检查响应对象
        if (error.response) {
          // 2. 检查响应数据
          const responseData = error.response.data;
          
          // 3. 尝试多种方式判断是否是禁用账户错误
          const errorMessage = 
            (typeof responseData === 'string' ? responseData : '') || 
            (responseData?.message) || 
            (responseData?.error) || 
            error.message || '';
          
          // 4. 检查错误消息是否包含禁用关键词
          if (errorMessage.includes('禁用') || 
              errorMessage.includes('disabled') || 
              errorMessage.includes('账号已被禁用')) {
            // 标记为禁用账户错误
            error.isDisabledAccount = true;
            error.disabledMessage = '账号已被禁用，请联系管理员';
            
            // 创建一个特定的错误对象，覆盖原始错误
            const disabledError = new Error('账号已被禁用，请联系管理员');
            disabledError.isDisabledAccount = true;
            disabledError.response = error.response;
            throw disabledError;
          }
        }
        
        // 继续抛出原始错误
        throw error;
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