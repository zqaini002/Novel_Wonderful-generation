<template>
  <div class="footer-container">
    <div class="footer-content">
      <div class="footer-logo">
        <h2>小说精读助手</h2>
        <p>AI驱动的小说内容分析与摘要生成系统</p>
      </div>
      <div class="footer-links">
        <div class="link-group">
          <h3>快速链接</h3>
          <ul>
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/upload">上传小说</router-link></li>
            <li><router-link to="/about">关于我们</router-link></li>
          </ul>
        </div>
        <div class="link-group">
          <h3>帮助支持</h3>
          <ul>
            <li><a href="#">使用指南</a></li>
            <li><a href="#">常见问题</a></li>
            <li><a href="#">反馈建议</a></li>
          </ul>
        </div>
        <div class="link-group">
          <h3>联系我们</h3>
          <ul>
            <li><a href="mailto:contact@novelassistant.com">邮箱联系</a></li>
            <li><a href="#">社交媒体</a></li>
          </ul>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <p>© {{ currentYear }} 小说精读助手 - 版权所有</p>
      <p>本网站仅供学习交流使用，请勿用于任何商业目的</p>
    </div>
  </div>
</template>

<script>
import { defineComponent, computed } from 'vue'

export default defineComponent({
  name: 'FooterComponent',
  setup() {
    // 获取当前年份
    const currentYear = computed(() => new Date().getFullYear())
    
    return {
      currentYear
    }
  }
})
</script>

<style scoped>
.footer-container {
  width: 100%;
  color: #606266;
}

.footer-content {
  display: flex;
  flex-wrap: wrap;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.footer-logo {
  flex: 1;
  min-width: 250px;
  margin-right: 40px;
}

.footer-logo h2 {
  margin: 0 0 10px 0;
  color: #409EFF;
}

.footer-logo p {
  margin: 0;
}

.footer-links {
  flex: 2;
  display: flex;
  flex-wrap: wrap;
}

.link-group {
  min-width: 180px;
  margin-right: 20px;
  margin-bottom: 20px;
}

.link-group h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
}

.link-group ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.link-group li {
  margin-bottom: 8px;
}

.link-group a {
  color: #606266;
  text-decoration: none;
}

.link-group a:hover {
  color: #409EFF;
}

.footer-bottom {
  text-align: center;
  padding: 15px;
  border-top: 1px solid #e6e6e6;
  font-size: 14px;
}

.footer-bottom p {
  margin: 5px 0;
}
</style> 