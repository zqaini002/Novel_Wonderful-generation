import authService from '../api/auth';

// 从localStorage获取用户信息
const user = JSON.parse(localStorage.getItem('user'));

// 初始状态
const initialState = user
  ? { status: { loggedIn: true }, user }
  : { status: { loggedIn: false }, user: null };

export const auth = {
  namespaced: true,
  state: initialState,
  actions: {
    // 登录
    async login({ commit }, user) {
      try {
        const response = await authService.login(user.username, user.password);
        commit('loginSuccess', response);
        return Promise.resolve(response);
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