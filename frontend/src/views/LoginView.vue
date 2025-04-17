<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>登录</h2>
        <p>欢迎回来！登录您的账户以继续使用小说智析。</p>
      </div>
      
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top">
        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          show-icon
          :closable="true"
          @close="errorMessage = ''"
          class="login-alert"
        />
        
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            clearable
          >
            <template #prefix>
              <el-icon><user /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            placeholder="请输入密码"
            type="password" 
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <div class="login-actions">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码?</el-link>
        </div>
        
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-button" 
            :loading="loading" 
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        
        <div class="register-link">
          <span>还没有账号?</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

export default {
  name: 'LoginView',
  components: {
    User,
    Lock
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    const loginFormRef = ref(null)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const rememberMe = ref(false)
    const loading = ref(false)
    const errorMessage = ref('')
    
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 40, message: '密码长度应在6-40个字符之间', trigger: 'blur' }
      ]
    }
    
    const handleLogin = () => {
      loginFormRef.value.validate((valid) => {
        if (valid) {
          loading.value = true
          errorMessage.value = ''
          
          const loginData = {
            username: loginForm.username,
            password: loginForm.password
          };
          
          store.dispatch('auth/login', loginData)
          .then(() => {
            ElMessage.success('登录成功!')
            
            // 获取用户信息
            const user = store.getters['auth/currentUser']
            
            // 检查是否有重定向地址
            const redirectPath = route.query.redirect;
            
            // 根据用户角色决定跳转页面
            if (user && user.roles) {
              // 如果有重定向地址则使用重定向地址
              if (redirectPath) {
                router.push(redirectPath);
              } 
              // 如果用户是管理员，跳转到管理员仪表盘
              else if (user.roles.includes('ROLE_ADMIN')) {
                router.push('/admin/dashboard')
              } else {
                // 普通用户跳转到首页
                router.push('/')
              }
            } else {
              // 角色信息不存在，跳转到首页或重定向页面
              router.push(redirectPath || '/')
            }
          })
          .catch(error => {
            console.log('登录界面捕获到错误:', error);
            
            // 处理账号禁用的错误消息
            if (error.isDisabledAccount || 
                (error.message && error.message.includes('禁用'))) {
              errorMessage.value = '账号已被禁用，请联系管理员';
              return;
            }
            
            // 处理密码错误
            if (error.response && error.response.status === 401) {
              errorMessage.value = '用户名或密码错误';
              return;
            }
            
            // 处理服务器返回的其他明确错误信息
            if (error.response && error.response.data) {
              const responseData = error.response.data;
              let responseMsg = '';
              
              if (typeof responseData === 'string') {
                responseMsg = responseData;
              } else if (responseData.message) {
                responseMsg = responseData.message;
              } else if (responseData.error) {
                responseMsg = responseData.error;
              }
              
              if (responseMsg) {
                errorMessage.value = responseMsg;
                return;
              }
            }
            
            // 默认错误消息
            errorMessage.value = '登录失败，请稍后再试';
            console.error('登录失败:', error);
          })
          .finally(() => {
            loading.value = false
          })
        } else {
          return false
        }
      })
    }
    
    return {
      loginForm,
      loginRules,
      rememberMe,
      loading,
      errorMessage,
      handleLogin,
      loginFormRef
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 250px);
  padding: 40px 20px;
}

.login-card {
  width: 100%;
  max-width: 440px;
  padding: 30px;
  border-radius: var(--radius, 8px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background-color: #fff;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 28px;
  color: var(--primary-color, #4d7bef);
  margin-bottom: 10px;
}

.login-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-alert {
  margin-bottom: 20px;
}

.login-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 10px;
}

.register-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.register-link a {
  margin-left: 5px;
  color: var(--primary-color, #4d7bef);
  text-decoration: none;
  font-weight: bold;
}

.register-link a:hover {
  text-decoration: underline;
}
</style> 