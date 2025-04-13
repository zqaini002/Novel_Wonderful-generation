<template>
  <div class="notfound-container">
    <el-result
      icon="error"
      title="404"
      sub-title="抱歉，您访问的页面不存在"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
    
    <div class="suggestions">
      <h3>您可能想要寻找：</h3>
      <ul>
        <li>
          <el-link type="primary" @click="$router.push('/')">首页</el-link>
          <span> - 探索精彩小说</span>
        </li>
        <li>
          <el-link type="primary" @click="$router.push('/upload')">上传小说</el-link>
          <span> - 分析您的小说</span>
        </li>
        <li>
          <el-link type="primary" @click="$router.push('/about')">关于我们</el-link>
          <span> - 了解项目详情</span>
        </li>
      </ul>
    </div>
    
    <div class="error-details">
      <p>如果您认为这是一个错误，请联系我们：contact@novel-assistant.com</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFoundView'
}
</script>

<style scoped>
.notfound-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  max-width: 800px;
  margin: 0 auto;
}

.suggestions {
  margin-top: 30px;
  width: 100%;
  max-width: 500px;
}

.suggestions h3 {
  margin-bottom: 15px;
  color: #303133;
}

.suggestions ul {
  list-style-type: none;
  padding: 0;
}

.suggestions li {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.suggestions span {
  color: #606266;
  margin-left: 5px;
}

.error-details {
  margin-top: 40px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style> 