import authService from '../api/auth';

// 从localStorage获取用户信息
const userStr = localStorage.getItem('user');
let initialUser = null;

try {
  initialUser = userStr ? JSON.parse(userStr) : null;
} catch (e) {
  // 解析失败时不设置初始用户
}

// 初始状态
const initialState = initialUser
  ? { status: { loggedIn: true }, user: initialUser }
  : { status: { loggedIn: false }, user: null };

export const auth = {
  namespaced: true,
  state: initialState,
  actions: {
    // 登录
    async login({ commit }, user) {
      try {
        // 调用登录服务
        const response = await authService.login(user.username, user.password);
        
        // 确保响应中包含必要的字段
        if (!response || (!response.token && !response.accessToken)) {
          throw new Error('登录响应格式错误');
        }
        
        // 登录成功，提交mutation
        commit('loginSuccess', response);
        
        return response;
      } catch (error) {
        commit('loginFailure');
        return Promise.reject(error);
      }
    },
    
    // 登出
    logout({ commit }) {
      authService.logout();
      commit('logout');
    },
    
    // 注册
    async register({ commit }, user) {
      try {
        const response = await authService.register(user.username, user.email, user.password);
        commit('registerSuccess');
        return Promise.resolve(response);
      } catch (error) {
        commit('registerFailure');
        return Promise.reject(error);
      }
    },
    
    // 刷新令牌
    async refreshToken({ commit }, accessToken) {
      commit('refreshToken', accessToken);
    },
    
    // 更新用户资料
    async updateProfile({ commit }, userData) {
      commit('updateUserProfile', userData);
    }
  },
  
  mutations: {
    // 登录成功
    loginSuccess(state, user) {
      state.status.loggedIn = true;
      state.user = user;
    },
    
    // 登录失败
    loginFailure(state) {
      state.status.loggedIn = false;
      state.user = null;
    },
    
    // 登出
    logout(state) {
      state.status.loggedIn = false;
      state.user = null;
    },
    
    // 注册成功
    registerSuccess(state) {
      state.status.loggedIn = false;
    },
    
    // 注册失败
    registerFailure(state) {
      state.status.loggedIn = false;
    },
    
    // 刷新令牌
    refreshToken(state, accessToken) {
      state.status.loggedIn = true;
      state.user = { ...state.user, accessToken };
    },
    
    // 更新用户资料
    updateUserProfile(state, userData) {
      if (state.user) {
        // 更新状态中的用户信息
        state.user = {
          ...state.user,
          ...userData
        };
        
        // 同时更新localStorage中的用户信息
        localStorage.setItem('user', JSON.stringify(state.user));
      }
    }
  },
  
  getters: {
    // 获取登录状态
    isLoggedIn: state => {
      return state.status.loggedIn;
    },
    
    // 获取当前登录用户
    currentUser: state => {
      return state.user;
    },
    
    // 获取用户名
    username: state => {
      if (!state.user) return ''; 
      return state.user.username || state.user.name || '';
    },
    
    // 获取用户角色
    userRoles: state => {
      if (!state.user || !state.user.roles) return [];
      return state.user.roles; 
    }
  }
};