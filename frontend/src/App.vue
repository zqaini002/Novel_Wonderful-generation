<template>
  <div class="app-container">
    <el-config-provider :locale="zhCn">
      <el-container>
        <el-header>
          <header-component />
        </el-header>
        <el-main>
          <router-view />
        </el-main>
        <el-footer>
          <footer-component />
        </el-footer>
      </el-container>
    </el-config-provider>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import HeaderComponent from '@/components/layout/HeaderComponent.vue'
import FooterComponent from '@/components/layout/FooterComponent.vue'

export default defineComponent({
  name: 'App',
  components: {
    HeaderComponent,
    FooterComponent,
    ElConfigProvider
  },
  setup() {
    return {
      zhCn
    }
  }
})
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.el-header {
  padding: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, .12);
  z-index: 999;
}

.el-main {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
}

.el-footer {
  padding: 20px 0;
  background-color: #f5f7fa;
  border-top: 1px solid #e6e6e6;
}
</style> 