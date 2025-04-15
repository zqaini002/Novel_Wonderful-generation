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
          <el-table-column label="操作" width="200" fixed="right">
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
            :type="selectedUser?.enabled ? 'danger' : 'success'"
            @click="toggleUserStatus(selectedUser)"
          >
            {{ selectedUser?.enabled ? '禁用账号' : '启用账号' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import adminService from '@/api/admin'

const loading = ref(true)
const users = ref([])
const searchQuery = ref('')
const selectedUser = ref(null)
const userDetailDialogVisible = ref(false)

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
    const response = await adminService.updateUserStatus(user.id, newStatus)
    
    // 更新本地数据 - response已经是返回的用户对象
    const updatedUser = response
    const index = users.value.findIndex(u => u.id === user.id)
    if (index !== -1) {
      users.value[index] = updatedUser
    }
    
    // 如果正在查看的用户是被修改的用户，更新选中的用户
    if (selectedUser.value && selectedUser.value.id === user.id) {
      selectedUser.value = updatedUser
    }
    
    ElMessage.success(`用户已${statusText}`)
  } catch (err) {
    if (err !== 'cancel') {
      console.error('更新用户状态失败:', err)
      ElMessage.error('更新用户状态失败')
    }
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
 