<template>
  <div class="header-container">
    <el-menu
      :default-active="activeIndex"
      class="menu"
      mode="horizontal"
      router
      background-color="#409EFF"
      text-color="#fff"
      active-text-color="#ffd04b">
      <div class="logo">
        <router-link to="/">
          <h1>小说精读助手</h1>
        </router-link>
      </div>
      <div class="menu-items">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/upload">上传小说</el-menu-item>
        <el-menu-item index="/about">关于</el-menu-item>
      </div>
      <div class="user-area">
        <el-dropdown v-if="isLoggedIn">
          <span class="el-dropdown-link">
            {{ username }}<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人中心</el-dropdown-item>
              <el-dropdown-item>我的小说</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <div v-else>
          <el-button link @click="login">登录</el-button> / 
          <el-button link @click="register">注册</el-button>
        </div>
      </div>
    </el-menu>
  </div>
</template>

<script>
import { defineComponent, computed } from 'vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'

export default defineComponent({
  name: 'HeaderComponent',
  setup() {
    const store = useStore()
    const route = useRoute()
    
    // 计算当前激活的菜单项
    const activeIndex = computed(() => route.path)
    
    // 从store获取登录状态
    const isLoggedIn = computed(() => store.getters.isLoggedIn || false)
    
    // 从store获取用户名
    const username = computed(() => store.state.user ? store.state.user.name : '')
    
    // 登录功能（示例）
    const login = () => {
      console.log('登录功能待实现')
    }
    
    // 注册功能（示例）
    const register = () => {
      console.log('注册功能待实现')
    }
    
    // 登出功能（示例）
    const logout = () => {
      store.commit('SET_USER', null)
    }
    
    return {
      activeIndex,
      isLoggedIn,
      username,
      login,
      register,
      logout
    }
  }
})
</script>

<style scoped>
.header-container {
  width: 100%;
}

.menu {
  display: flex;
  height: 60px;
  padding: 0 20px;
}

.logo {
  margin-right: 40px;
  display: flex;
  align-items: center;
}

.logo a {
  text-decoration: none;
}

.logo h1 {
  color: #fff;
  margin: 0;
  font-size: 20px;
}

.menu-items {
  flex: 1;
  display: flex;
}

.user-area {
  display: flex;
  align-items: center;
  color: #fff;
}

.el-dropdown-link {
  color: #fff;
  cursor: pointer;
}

.el-button--text {
  color: #fff;
}
</style> 