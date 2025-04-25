<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人中心</h2>
        </div>
      </template>
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px" class="profile-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" disabled></el-input>
          <small>用户名不可修改</small>
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="设置您的昵称"></el-input>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="您的邮箱地址"></el-input>
        </el-form-item>
        
        <el-divider content-position="center">密码修改（可选）</el-divider>
        
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="userForm.oldPassword" type="password" placeholder="输入旧密码" show-password></el-input>
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="userForm.newPassword" type="password" placeholder="输入新密码" show-password></el-input>
          <small>留空表示不修改密码</small>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">保存修改</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="profile-stats-card">
      <template #header>
        <div class="card-header">
          <h2>账户统计</h2>
        </div>
      </template>
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-value">{{ userStats.novelCount }}</div>
          <div class="stat-label">上传小说</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ userStats.totalViews || 0 }}</div>
          <div class="stat-label">总浏览量</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ userStats.favoriteCount || 0 }}</div>
          <div class="stat-label">收藏数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">
            {{ userForm.lastLoginAt ? formatDate(userForm.lastLoginAt) : '首次登录' }}
          </div>
          <div class="stat-label">上次登录</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import userService from '@/api/user'
import store from '@/store'

export default {
  name: 'ProfileView',
  setup() {
    const userFormRef = ref(null)
    const loading = ref(false)
    
    // 从 localStorage 获取当前用户信息
    const currentUser = computed(() => store.state.auth.user)
    
    const userStats = reactive({
      novelCount: 0,
      totalViews: 0,
      favoriteCount: 0
    })
    
    const userForm = reactive({
      id: '',
      username: '',
      email: '',
      nickname: '',
      createdAt: '',
      lastLoginAt: '',
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const rules = {
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      nickname: [
        { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
      ],
      newPassword: [
        { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { 
          validator: (rule, value, callback) => {
            if (userForm.newPassword && value !== userForm.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    // 初始化表单数据
    const initUserForm = () => {
      if (currentUser.value) {
        userForm.id = currentUser.value.id
        userForm.username = currentUser.value.username
        userForm.email = currentUser.value.email
        userForm.nickname = currentUser.value.nickname || ''
        userForm.createdAt = currentUser.value.createdAt
        userForm.lastLoginAt = currentUser.value.lastLoginAt
        
        // 尝试从后端获取最新的用户信息
        fetchCurrentUser()
      }
    }
    
    // 从后端获取当前用户的最新信息
    const fetchCurrentUser = async () => {
      try {
        const data = await userService.getCurrentUser()
        if (data) {
          // 更新表单数据
          userForm.id = data.id
          userForm.email = data.email
          userForm.nickname = data.nickname || ''
          userForm.createdAt = data.createdAt
          userForm.lastLoginAt = data.lastLoginAt
          
          // 更新 store 中的用户信息
          store.commit('auth/updateUserProfile', {
            email: data.email,
            nickname: data.nickname,
            createdAt: data.createdAt,
            lastLoginAt: data.lastLoginAt
          })
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }
    
    // 获取用户统计信息
    const fetchUserStats = async () => {
      try {
        console.log('开始获取用户统计信息...')
        const data = await userService.getUserStats()
        console.log('获取用户统计信息成功，响应数据:', data)
        
        if (data) {
          console.log('用户统计数据:', data)
          userStats.novelCount = data.novelCount || 0
          userStats.totalViews = data.totalViews || 0
          userStats.favoriteCount = data.favoriteCount || 0
        } else {
          console.warn('获取用户统计信息响应数据为空')
          // 设置默认值
          userStats.novelCount = 0
          userStats.totalViews = 0
          userStats.favoriteCount = 0
        }
      } catch (error) {
        console.error('获取用户统计信息失败:', error)
        // 出错时设置默认值
        userStats.novelCount = 0
        userStats.totalViews = 0
        userStats.favoriteCount = 0
      }
    }
    
    // 提交表单
    const submitForm = async () => {
      if (!userFormRef.value) return
      
      await userFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            // 准备要发送的数据
            const userData = {
              id: userForm.id,
              email: userForm.email,
              nickname: userForm.nickname
            }
            
            // 如果有输入新密码，则包含密码字段
            if (userForm.newPassword) {
              if (!userForm.oldPassword) {
                ElMessage.error('修改密码需要输入旧密码')
                loading.value = false
                return
              }
              
              userData.oldPassword = userForm.oldPassword
              userData.newPassword = userForm.newPassword
            }
            
            // 发送更新请求
            await userService.updateUserProfile(userData)
            
            ElMessage.success('个人信息更新成功')
            
            // 更新 store 中的用户信息
            store.commit('auth/updateUserProfile', {
              email: userForm.email,
              nickname: userForm.nickname
            })
            
            // 清空密码字段
            userForm.oldPassword = ''
            userForm.newPassword = ''
            userForm.confirmPassword = ''
          } catch (error) {
            console.error('更新个人信息失败:', error)
            ElMessage.error(error.response?.data?.message || '更新个人信息失败')
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    // 重置表单
    const resetForm = () => {
      if (userFormRef.value) {
        userFormRef.value.resetFields()
        initUserForm()
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '未知';
      
      try {
        const date = new Date(dateString);
        // 检查日期是否有效
        if (isNaN(date.getTime())) return '未知';
        
        // 对于当天的日期，显示"今天 HH:MM"
        const today = new Date();
        if (date.getDate() === today.getDate() &&
            date.getMonth() === today.getMonth() &&
            date.getFullYear() === today.getFullYear()) {
          return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
        }
        
        // 1970年表示日期未设置
        if (date.getFullYear() === 1970) {
          return '未知';
        }
        
        return date.toLocaleDateString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (error) {
        console.error('日期格式化错误:', error);
        return '未知';
      }
    }
    
    onMounted(() => {
      initUserForm()
      fetchUserStats()
    })
    
    return {
      userFormRef,
      userForm,
      userStats,
      rules,
      loading,
      submitForm,
      resetForm,
      formatDate
    }
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 20px auto;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.profile-card {
  flex: 1;
  min-width: 600px;
}

.profile-stats-card {
  flex: 1;
  min-width: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-form {
  margin-top: 20px;
}

small {
  color: #909399;
  font-size: 12px;
  margin-left: 5px;
}

.stats-container {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }
  
  .profile-card, .profile-stats-card {
    min-width: 100%;
  }
}
</style> 