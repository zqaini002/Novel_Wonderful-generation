<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>登录</h2>
        <p>欢迎回来！登录您的账户以继续使用小说精读助手。</p>
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
            prefix-icon="el-icon-user"
            placeholder="请输入用户名" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            prefix-icon="el-icon-lock"
            placeholder="请输入密码"
            type="password" 
            show-password
            clearable
          />
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'LoginView',
  setup() {
    const store = useStore()
    const router = useRouter()
    const loginFormRef = ref(null)
    
    const formState = reactive({
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
      loginFormRef.value.validate(valid => {
        if (valid) {
          loading.value = true
          errorMessage.value = ''
          
          store.dispatch('auth/login', {
            username: formState.username,
            password: formState.password
          })
          .then((response) => {
            console.log('登录成功，用户信息:', response)
            ElMessage.success('登录成功!')
            
            // 获取用户信息
            const user = store.state.auth.user
            
            // 根据用户角色决定跳转页面
            if (user && user.roles) {
              // 如果用户是管理员，跳转到管理员仪表盘
              if (user.roles.includes('ROLE_ADMIN')) {
                console.log('管理员登录成功，跳转到仪表盘')
                router.push('/admin/dashboard')
              } else {
                // 普通用户跳转到首页
                router.push('/')
              }
            } else {
              // 角色信息不存在，跳转到首页
              router.push('/')
            }
          })
          .catch(error => {
            console.error('登录失败:', error)
            errorMessage.value = error.response?.data?.message || '登录失败，请检查用户名和密码'
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
      loginForm: formState,
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