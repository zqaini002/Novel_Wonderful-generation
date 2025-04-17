<template>
  <div class="user-management-container">
    <el-card class="user-management-card">
      <template #header>
        <div class="card-header">
          <h2>用户管理</h2>
          <div class="header-actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索用户名或邮箱"
              clearable
              @clear="filterUsers"
              @input="filterUsers"
              style="width: 300px; margin-right: 10px;"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="refreshUsers">刷新</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else class="user-table-container">
        <el-table
          :data="displayUsers"
          style="width: 100%"
          border
          stripe
          highlight-current-row
          @row-click="handleRowClick"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="email" label="邮箱" width="200" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
                {{ scope.row.enabled ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="角色" width="120">
            <template #default="scope">
              <el-tag 
                v-for="role in scope.row.roles" 
                :key="role.name"
                :type="role.name === 'ROLE_ADMIN' ? 'danger' : 'primary'"
                style="margin-right: 5px;"
              >
                {{ formatRoleName(role.name) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click.stop="viewUserDetail(scope.row)"
              >
                查看
              </el-button>
              <el-button
                size="small"
                type="warning"
                @click.stop="openEditPasswordDialog(scope.row)"
              >
                修改密码
              </el-button>
              <el-button
                size="small"
                :type="scope.row.enabled ? 'danger' : 'success'"
                @click.stop="toggleUserStatus(scope.row)"
              >
                {{ scope.row.enabled ? '禁用' : '启用' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click.stop="deleteUser(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="userDetailDialogVisible"
      title="用户详情"
      width="600px"
    >
      <div v-if="selectedUser" class="user-detail">
        <div class="detail-item">
          <span class="detail-label">ID:</span>
          <span class="detail-value">{{ selectedUser.id }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">用户名:</span>
          <span class="detail-value">{{ selectedUser.username }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">邮箱:</span>
          <span class="detail-value">{{ selectedUser.email }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">昵称:</span>
          <span class="detail-value">{{ selectedUser.nickname || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态:</span>
          <span class="detail-value">
            <el-tag :type="selectedUser.enabled ? 'success' : 'danger'">
              {{ selectedUser.enabled ? '启用' : '禁用' }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">角色:</span>
          <span class="detail-value">
            <el-tag 
              v-for="role in selectedUser.roles" 
              :key="role.name"
              :type="role.name === 'ROLE_ADMIN' ? 'danger' : 'primary'"
              style="margin-right: 5px;"
            >
              {{ formatRoleName(role.name) }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">注册时间:</span>
          <span class="detail-value">{{ formatDate(selectedUser.createdAt) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">最后登录:</span>
          <span class="detail-value">{{ selectedUser.lastLoginAt ? formatDate(selectedUser.lastLoginAt) : '从未登录' }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDetailDialogVisible = false">关闭</el-button>
          <el-button
            type="primary"
            @click="openEditPasswordDialog(selectedUser)"
          >
            修改密码
          </el-button>
          <el-button
            :type="selectedUser?.enabled ? 'danger' : 'success'"
            @click="toggleUserStatus(selectedUser)"
          >
            {{ selectedUser?.enabled ? '禁用账号' : '启用账号' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑密码对话框 -->
    <el-dialog
      v-model="editPasswordDialog"
      title="修改用户密码"
      width="500px"
    >
      <el-form
        ref="passwordForm"
        :model="passwordData"
        :rules="passwordRules"
        label-width="120px"
      >
        <el-form-item label="用户名">
          <el-input :value="currentEditUser ? currentEditUser.username : ''" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordData.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordData.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span>
          <el-button @click="editPasswordDialog = false">取消</el-button>
          <el-button
            type="primary"
            :loading="updating"
            @click="updateUserPassword"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/* eslint-disable */
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import adminService from '@/api/admin'

const loading = ref(true)
const users = ref([])
const searchQuery = ref('')
const selectedUser = ref(null)
const userDetailDialogVisible = ref(false)
const editPasswordDialog = ref(false)
const currentEditUser = ref(null)
const passwordForm = ref(null)
const passwordData = ref({
  newPassword: '',
  confirmPassword: '',
})
const updating = ref(false)
const passwordRules = {
  newPassword: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordData.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 获取所有用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await adminService.getAllUsers()
    
    // 打印API响应以便调试
    console.log('获取用户列表响应:', response);
    
    // apiClient已经提取了response.data，我们只需要检查是否有users字段
    if (response && response.users) {
      users.value = response.users
    } else if (Array.isArray(response)) {
      // 如果response本身就是数组，直接使用
      users.value = response
    } else {
      users.value = []
      console.warn('用户列表API返回格式异常:', response)
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败: ' + (error.message || '未知错误'))
    users.value = []
  } finally {
    loading.value = false
  }
}

// 刷新用户列表
const refreshUsers = () => {
  fetchUsers()
}

// 过滤用户列表
const filterUsers = () => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) {
    return users.value
  }
  
  return users.value.filter(user => 
    user.username.toLowerCase().includes(query) ||
    user.email.toLowerCase().includes(query) ||
    (user.nickname && user.nickname.toLowerCase().includes(query))
  )
}

// 计算属性：根据搜索条件显示用户
const displayUsers = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) {
    return users.value
  }
  
  return users.value.filter(user => 
    user.username.toLowerCase().includes(query) ||
    user.email.toLowerCase().includes(query) ||
    (user.nickname && user.nickname.toLowerCase().includes(query))
  )
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 格式化角色名称
const formatRoleName = (roleName) => {
  switch (roleName) {
    case 'ROLE_ADMIN':
      return '管理员'
    case 'ROLE_USER':
      return '用户'
    default:
      return roleName
  }
}

// 查看用户详情
const viewUserDetail = (user) => {
  selectedUser.value = user
  userDetailDialogVisible.value = true
}

// 处理行点击
const handleRowClick = (row) => {
  viewUserDetail(row)
}

// 切换用户状态（启用/禁用）
const toggleUserStatus = async (user) => {
  if (!user) return
  
  try {
    const newStatus = !user.enabled
    const statusText = newStatus ? '启用' : '禁用'
    
    // 确认操作
    await ElMessageBox.confirm(
      `确定要${statusText}用户 "${user.username}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API更新状态
    console.log(`正在发送请求更新用户 ${user.id} 的状态为 ${newStatus}`)
    const response = await adminService.updateUserStatus(user.id, newStatus)
    console.log('更新用户状态响应:', response)
    
    // 刷新用户列表，确保数据是最新的
    await fetchUsers()
    
    // 如果正在查看的用户是被修改的用户，刷新选中的用户
    if (selectedUser.value && selectedUser.value.id === user.id) {
      // 从刷新后的列表中找到用户并更新选中的用户
      const updatedUser = users.value.find(u => u.id === user.id)
      if (updatedUser) {
        selectedUser.value = updatedUser
      }
    }
    
    ElMessage.success(`用户已${statusText}`)
  } catch (err) {
    if (err === 'cancel') {
      console.log('用户取消了操作')
      return
    }
    
    console.error('更新用户状态失败:', err)
    let errorMessage = '更新用户状态失败'
    
    // 尝试提取详细错误信息
    if (err.response && err.response.data) {
      if (err.response.data.message) {
        errorMessage = err.response.data.message
      } else if (err.response.data.error) {
        errorMessage = err.response.data.error
      }
    } else if (err.message) {
      errorMessage = err.message
    }
    
    ElMessage.error(errorMessage)
  }
}

// 删除用户
const deleteUser = async (user) => {
  if (!user) return
  
  try {
    // 确认删除
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
      '危险操作',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    
    // 调用API删除用户
    await adminService.deleteUser(user.id)
    
    // 从本地列表中移除
    users.value = users.value.filter(u => u.id !== user.id)
    
    // 如果正在查看的就是被删除的用户，关闭详情对话框
    if (selectedUser.value && selectedUser.value.id === user.id) {
      userDetailDialogVisible.value = false
    }
    
    ElMessage.success('用户已删除')
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除用户失败:', err)
      ElMessage.error('删除用户失败')
    }
  }
}

// 在用户详情对话框中添加"修改密码"按钮
const openEditPasswordDialog = (user) => {
  currentEditUser.value = user
  passwordData.value = {
    newPassword: '',
    confirmPassword: ''
  }
  editPasswordDialog.value = true
}

// 更新用户密码
const updateUserPassword = async () => {
  if (!passwordForm.value) return

  // 验证表单
  await passwordForm.value.validate(async (valid) => {
    if (valid) {
      try {
        updating.value = true
        await adminService.updateUserInfo(currentEditUser.value.id, {
          password: passwordData.value.newPassword
        })
        editPasswordDialog.value = false
        ElMessage.success('密码修改成功')
        
        // 重新获取用户列表以确保数据是最新的
        fetchUsers()
      } catch (error) {
        console.error('修改密码失败', error)
        ElMessage.error(`修改密码失败: ${error.response?.data?.message || error.message}`)
      } finally {
        updating.value = false
      }
    }
  })
}

// 添加修改密码选项到用户详情弹窗
const userDetailActions = [
  {
    label: '修改密码',
    action: openEditPasswordDialog,
    type: 'primary'
  }
]

// 组件挂载时获取数据
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management-container {
  padding: 20px;
}

.user-management-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.loading-container {
  padding: 20px;
}

.user-table-container {
  margin-top: 20px;
}

.user-detail {
  padding: 10px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
}

.detail-label {
  font-weight: bold;
  width: 100px;
}

.detail-value {
  flex: 1;
}
</style> 
 