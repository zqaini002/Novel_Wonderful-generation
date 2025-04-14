<template>
  <div class="login-test-container">
    <h1>API访问权限测试</h1>
    
    <el-card class="auth-status-card">
      <template #header>
        <div class="card-header">
          <span>认证状态</span>
        </div>
      </template>
      <div class="auth-status">
        <p><strong>登录状态:</strong> {{ isLoggedIn ? '已登录' : '未登录' }}</p>
        <p v-if="isLoggedIn"><strong>用户名:</strong> {{ currentUser?.username }}</p>
        <p v-if="isLoggedIn"><strong>角色:</strong> {{ currentUser?.roles?.join(', ') }}</p>
        <p v-if="isLoggedIn"><strong>令牌:</strong> {{ tokenDisplay }}</p>
      </div>
      <div class="auth-actions">
        <el-button type="primary" @click="handleLogin" v-if="!isLoggedIn">登录</el-button>
        <el-button type="danger" @click="handleLogout" v-else>登出</el-button>
      </div>
    </el-card>

    <el-divider content-position="center">API测试</el-divider>

    <div class="api-test-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>公开API (无需认证)</span>
              </div>
            </template>
            <el-button type="success" @click="testPublicApi">测试公开API</el-button>
            <div class="api-result" v-if="publicApiResult">
              <pre>{{ JSON.stringify(publicApiResult, null, 2) }}</pre>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>用户API (需要USER角色)</span>
              </div>
            </template>
            <el-button type="warning" @click="testUserApi">测试用户API</el-button>
            <div class="api-result" v-if="userApiResult">
              <pre>{{ JSON.stringify(userApiResult, null, 2) }}</pre>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>管理员API (需要ADMIN角色)</span>
              </div>
            </template>
            <el-button type="danger" @click="testAdminApi">测试管理员API</el-button>
            <div class="api-result" v-if="adminApiResult">
              <pre>{{ JSON.stringify(adminApiResult, null, 2) }}</pre>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { useStore } from 'vuex';
import { ElMessage } from 'element-plus';
import axios from 'axios';

export default {
  name: 'LoginTestView',
  setup() {
    const store = useStore();
    const publicApiResult = ref(null);
    const userApiResult = ref(null);
    const adminApiResult = ref(null);

    // 从Vuex获取认证状态
    const isLoggedIn = computed(() => store.getters.isLoggedIn);
    const currentUser = computed(() => store.getters.currentUser);
    
    // 显示Token的前10个字符
    const tokenDisplay = computed(() => {
      if (!currentUser.value) return '';
      const token = currentUser.value.accessToken || currentUser.value.token || '';
      if (!token) return '无令牌';
      return token.substring(0, 10) + '...';
    });

    // 获取令牌
    const getAuthHeader = () => {
      const userStr = localStorage.getItem('user');
      if (!userStr) return {};
      
      try {
        const user = JSON.parse(userStr);
        const token = user.accessToken || user.token;
        if (!token) return {};
        
        return { 
          Authorization: `Bearer ${token}`
        };
      } catch (error) {
        console.error('解析用户信息失败:', error);
        return {};
      }
    };

    // 登录
    const handleLogin = async () => {
      try {
        const response = await axios.post('http://localhost:8080/api/auth/signin', {
          username: 'admin',
          password: 'admin123'
        });
        
        const userData = response.data;
        console.log('登录响应数据:', userData);
        
        // 保存到localStorage
        localStorage.setItem('user', JSON.stringify(userData));
        store.commit('auth/loginSuccess', userData);
        
        ElMessage.success('登录成功');
      } catch (error) {
        console.error('登录失败', error);
        store.commit('auth/loginFailure');
        ElMessage.error('登录失败: ' + (error.response?.data?.message || error.message));
      }
    };

    // 登出
    const handleLogout = () => {
      localStorage.removeItem('user');
      store.commit('auth/logout');
      ElMessage.success('已登出');
      // 清空API测试结果
      publicApiResult.value = null;
      userApiResult.value = null;
      adminApiResult.value = null;
    };

    // API测试函数
    const testPublicApi = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/test/public');
        publicApiResult.value = response.data;
      } catch (error) {
        publicApiResult.value = { 
          error: true, 
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        };
      }
    };

    const testUserApi = async () => {
      try {
        const headers = getAuthHeader();
        const response = await axios.get('http://localhost:8080/api/test/user', { headers });
        userApiResult.value = response.data;
      } catch (error) {
        userApiResult.value = { 
          error: true, 
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        };
      }
    };

    const testAdminApi = async () => {
      try {
        const headers = getAuthHeader();
        const response = await axios.get('http://localhost:8080/api/test/admin', { headers });
        adminApiResult.value = response.data;
      } catch (error) {
        adminApiResult.value = { 
          error: true, 
          message: error.response?.data?.message || error.message,
          status: error.response?.status
        };
      }
    };

    return {
      isLoggedIn,
      currentUser,
      tokenDisplay,
      handleLogin,
      handleLogout,
      testPublicApi,
      testUserApi,
      testAdminApi,
      publicApiResult,
      userApiResult,
      adminApiResult
    };
  }
}
</script>

<style scoped>
.login-test-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.auth-status-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.auth-status {
  margin-bottom: 20px;
}

.auth-actions {
  display: flex;
  justify-content: flex-end;
}

.api-test-section {
  margin-top: 20px;
}

.api-result {
  margin-top: 15px;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}

pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
</style> 