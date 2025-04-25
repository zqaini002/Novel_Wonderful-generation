<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>注册账号</h2>
        <p>加入小说智析，开始您的阅读之旅。</p>
      </div>
      
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top">
        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          show-icon
          :closable="true"
          @close="errorMessage = ''"
          class="register-alert"
        />
        
        <el-alert
          v-if="successMessage"
          :title="successMessage"
          type="success"
          show-icon
          :closable="true"
          @close="successMessage = ''"
          class="register-alert"
        />
        
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="registerForm.username" 
            prefix-icon="el-icon-user"
            placeholder="请输入用户名" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="电子邮箱" prop="email">
          <el-input 
            v-model="registerForm.email" 
            prefix-icon="el-icon-message"
            placeholder="请输入电子邮箱" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input 
            v-model="registerForm.nickname" 
            prefix-icon="el-icon-s-custom"
            placeholder="请输入昵称（可选）" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            prefix-icon="el-icon-lock"
            placeholder="请输入密码"
            type="password" 
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            prefix-icon="el-icon-lock"
            placeholder="请再次输入密码"
            type="password" 
            show-password
            clearable
          />
        </el-form-item>
        
        <div class="agreement">
          <el-checkbox v-model="agreement">我已阅读并同意<el-link type="primary" :underline="false">服务条款</el-link></el-checkbox>
        </div>
        
        <el-form-item>
          <el-button 
            type="primary" 
            class="register-button" 
            :loading="loading" 
            @click="handleRegister"
            :disabled="!agreement"
          >
            注册
          </el-button>
        </el-form-item>
        
        <div class="login-link">
          <span>已经有账号?</span>
          <router-link to="/login">立即登录</router-link>
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
  name: 'RegisterView',
  setup() {
    const store = useStore()
    const router = useRouter()
    const registerFormRef = ref(null)
    
    const formState = reactive({
      username: '',
      email: '',
      nickname: '',
      password: '',
      confirmPassword: ''
    })
    
    const agreement = ref(false)
    const loading = ref(false)
    const errorMessage = ref('')
    const successMessage = ref('')
    
    const validatePassword = (rule, value, callback) => {
      if (value !== formState.password) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    
    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入电子邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的电子邮箱地址', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 40, message: '密码长度应在6-40个字符之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validatePassword, trigger: 'blur' }
      ]
    }
    
    const handleRegister = () => {
      registerFormRef.value.validate(valid => {
        if (valid) {
          if (!agreement.value) {
            ElMessage.warning('请先同意服务条款')
            return
          }
          
          loading.value = true
          errorMessage.value = ''
          successMessage.value = ''
          
          store.dispatch('auth/register', {
            username: formState.username,
            email: formState.email,
            nickname: formState.nickname,
            password: formState.password
          })
          .then(
            // eslint-disable-next-line no-unused-vars
            response => {
            successMessage.value = '注册成功！请登录您的账号'
            // 3秒后跳转到登录页面
            setTimeout(() => {
              router.push('/login')
            }, 3000)
          })
          .catch(error => {
            console.error('注册失败:', error)
            errorMessage.value = error.response?.data?.message || '注册失败，请稍后再试'
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
      registerForm: formState,
      registerRules,
      agreement,
      loading,
      errorMessage,
      successMessage,
      handleRegister,
      registerFormRef
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 250px);
  padding: 40px 20px;
}

.register-card {
  width: 100%;
  max-width: 440px;
  padding: 30px;
  border-radius: var(--radius, 8px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background-color: #fff;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  font-size: 28px;
  color: var(--primary-color, #4d7bef);
  margin-bottom: 10px;
}

.register-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.register-alert {
  margin-bottom: 20px;
}

.agreement {
  margin-bottom: 20px;
}

.register-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 10px;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.login-link a {
  margin-left: 5px;
  color: var(--primary-color, #4d7bef);
  text-decoration: none;
  font-weight: bold;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
 