<template>
  <div class="footer-container">
    <div class="footer-content">
      <div class="footer-logo">
        <div class="logo-wrapper">
          <el-icon class="logo-icon"><Reading /></el-icon>
          <h2>小说智析</h2>
        </div>
        <p>AI驱动的小说内容分析与摘要生成系统</p>
        <div class="social-icons">
          <a href="#" class="social-icon"><el-icon><ChatDotRound /></el-icon></a>
          <a href="#" class="social-icon"><el-icon><Share /></el-icon></a>
          <a href="#" class="social-icon"><el-icon><Position /></el-icon></a>
        </div>
      </div>
      <div class="footer-links">
        <div class="link-group">
          <h3>快速链接</h3>
          <ul>
            <li><router-link to="/"><el-icon><HomeFilled /></el-icon> 首页</router-link></li>
            <li><router-link to="/upload"><el-icon><Upload /></el-icon> 上传小说</router-link></li>
            <li><router-link to="/about"><el-icon><InfoFilled /></el-icon> 关于我们</router-link></li>
          </ul>
        </div>
        <div class="link-group">
          <h3>帮助支持</h3>
          <ul>
            <li><a href="#"><el-icon><Document /></el-icon> 使用指南</a></li>
            <li><a href="#"><el-icon><QuestionFilled /></el-icon> 常见问题</a></li>
            <li><a href="#"><el-icon><ChatLineSquare /></el-icon> 反馈建议</a></li>
          </ul>
        </div>
        <div class="link-group">
          <h3>联系我们</h3>
          <ul>
            <li><a href="mailto:contact@novelassistant.com"><el-icon><Message /></el-icon> 邮箱联系</a></li>
            <li><a href="#"><el-icon><Promotion /></el-icon> 社交媒体</a></li>
            <li><a href="#"><el-icon><Link /></el-icon> 合作伙伴</a></li>
          </ul>
        </div>
      </div>
    </div>
    <div class="footer-wave">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 60">
        <path fill="var(--primary-color)" fill-opacity="0.05" d="M0,32L80,42.7C160,53,320,75,480,69.3C640,64,800,32,960,21.3C1120,11,1280,21,1360,26.7L1440,32L1440,0L1360,0C1280,0,1120,0,960,0C800,0,640,0,480,0C320,0,160,0,80,0L0,0Z"></path>
      </svg>
    </div>
    <div class="footer-bottom">
      <p>© {{ currentYear }} 小说智析 - 版权所有</p>
      <p>本网站仅供学习交流使用，请勿用于任何商业目的</p>
    </div>
  </div>
</template>

<script>
import { defineComponent, computed } from 'vue'
import { 
  Reading, 
  ChatDotRound, 
  Share, 
  Position, 
  HomeFilled, 
  Upload, 
  InfoFilled, 
  Document, 
  QuestionFilled, 
  ChatLineSquare, 
  Message, 
  Promotion, 
  Link 
} from '@element-plus/icons-vue'

export default defineComponent({
  name: 'FooterComponent',
  components: {
    Reading, 
    ChatDotRound, 
    Share, 
    Position, 
    HomeFilled, 
    Upload, 
    InfoFilled, 
    Document, 
    QuestionFilled, 
    ChatLineSquare, 
    Message, 
    Promotion, 
    Link
  },
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
  position: relative;
}

.footer-wave {
  position: absolute;
  top: -60px;
  left: 0;
  width: 100%;
  height: 60px;
  overflow: hidden;
}

.footer-content {
  display: flex;
  flex-wrap: wrap;
  padding: 40px 24px;
  max-width: 1280px;
  margin: 0 auto;
}

.footer-logo {
  flex: 1;
  min-width: 250px;
  margin-right: 40px;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.logo-icon {
  font-size: 24px;
  color: var(--primary-color);
  margin-right: 10px;
}

.footer-logo h2 {
  margin: 0;
  color: var(--primary-color);
  font-weight: 600;
}

.footer-logo p {
  margin: 0 0 20px 0;
  color: #909399;
  line-height: 1.6;
}

.social-icons {
  display: flex;
  margin-top: 15px;
  gap: 12px;
}

.social-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: rgba(77, 123, 239, 0.1);
  color: var(--primary-color);
  transition: all 0.3s;
}

.social-icon:hover {
  background-color: var(--primary-color);
  color: white;
  transform: translateY(-2px);
}

.footer-links {
  flex: 2;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.link-group {
  min-width: 180px;
  margin-bottom: 30px;
}

.link-group h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-bottom: 10px;
}

.link-group h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  border-radius: 1px;
}

.link-group ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.link-group li {
  margin-bottom: 12px;
}

.link-group a {
  display: flex;
  align-items: center;
  color: #606266;
  text-decoration: none;
  transition: color 0.2s, transform 0.2s;
}

.link-group a:hover {
  color: var(--primary-color);
  transform: translateX(3px);
}

.link-group .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.footer-bottom {
  text-align: center;
  padding: 20px;
  border-top: 1px solid var(--border-color);
  font-size: 14px;
  color: #909399;
  background-color: #f9fafc;
}

.footer-bottom p {
  margin: 5px 0;
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    padding: 30px 16px;
  }
  
  .footer-logo {
    margin-right: 0;
    margin-bottom: 30px;
  }
  
  .footer-links {
    flex-direction: column;
  }
  
  .link-group {
    margin-right: 0;
  }
}
</style> 