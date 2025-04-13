<template>
  <div class="upload-container">
    <h1>上传小说</h1>
    <p class="description">上传小说文件或输入小说链接，系统将自动分析内容并生成摘要、标签等</p>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane label="本地文件上传" name="file">
        <div class="upload-area">
          <el-upload
            class="upload-dragger"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            :file-list="fileList"
            :multiple="false">
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">
                支持 TXT、EPUB 格式的小说文件，文件大小不超过 20MB
              </div>
            </template>
          </el-upload>
        </div>
        
        <div class="upload-form">
          <el-form :model="uploadForm" label-width="100px">
            <el-form-item label="小说标题" required>
              <el-input v-model="uploadForm.title" placeholder="请输入小说标题"></el-input>
            </el-form-item>
            <el-form-item label="作者">
              <el-input v-model="uploadForm.author" placeholder="请输入作者名（可选）"></el-input>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="网页链接解析" name="url">
        <div class="url-form">
          <el-form :model="urlForm" label-width="100px">
            <el-form-item label="小说链接" required>
              <el-input v-model="urlForm.url" placeholder="请输入小说目录页链接"></el-input>
              <div class="form-tip">
                支持主流小说网站，请确保链接指向小说目录页或章节页
              </div>
            </el-form-item>
            <el-form-item label="小说标题" required>
              <el-input v-model="urlForm.title" placeholder="请输入小说标题"></el-input>
            </el-form-item>
            <el-form-item label="作者">
              <el-input v-model="urlForm.author" placeholder="请输入作者名（可选）"></el-input>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <div class="upload-actions">
      <el-button @click="goBack">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">开始分析</el-button>
    </div>
    
    <el-dialog
      v-model="dialogVisible"
      title="版权提醒"
      width="30%">
      <span>请确保您有合法权利使用上传的小说内容，本工具仅供个人学习和研究使用，请勿用于侵犯版权的目的。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmUpload">确认上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export default defineComponent({
  name: 'UploadView',
  setup() {
    const router = useRouter()
    const store = useStore()
    
    // 当前激活的标签页
    const activeTab = ref('file')
    
    // 文件上传相关
    const fileList = ref([])
    const uploadForm = reactive({
      title: '',
      author: ''
    })
    
    // URL解析相关
    const urlForm = reactive({
      url: '',
      title: '',
      author: ''
    })
    
    // 上传状态
    const loading = ref(false)
    
    // 对话框状态
    const dialogVisible = ref(false)
    
    // 返回首页
    const goBack = () => {
      router.push('/')
    }
    
    // 文件变更处理
    const handleFileChange = (file) => {
      fileList.value = [file]
      
      // 尝试从文件名中提取标题
      if (file.name && !uploadForm.title) {
        const fileName = file.name.replace(/\.(txt|epub)$/i, '')
        uploadForm.title = fileName
      }
    }
    
    // 表单提交处理
    const handleSubmit = () => {
      if (activeTab.value === 'file') {
        // 验证文件上传表单
        if (!fileList.value.length) {
          ElMessage.warning('请上传小说文件')
          return
        }
        
        if (!uploadForm.title) {
          ElMessage.warning('请输入小说标题')
          return
        }
        
        // 显示版权提醒
        dialogVisible.value = true
      } else {
        // 验证URL表单
        if (!urlForm.url) {
          ElMessage.warning('请输入小说链接')
          return
        }
        
        if (!urlForm.title) {
          ElMessage.warning('请输入小说标题')
          return
        }
        
        // 显示版权提醒
        dialogVisible.value = true
      }
    }
    
    // 确认上传
    const confirmUpload = async () => {
      dialogVisible.value = false
      loading.value = true
      
      try {
        // 准备上传参数
        const params = activeTab.value === 'file' 
          ? { 
              file: fileList.value[0].raw, 
              title: uploadForm.title,
              author: uploadForm.author
            }
          : { 
              url: urlForm.url, 
              title: urlForm.title,
              author: urlForm.author
            };
        
        // 使用API上传
        await store.dispatch('uploadNovel', params)
        
        ElMessage.success('提交成功，正在处理中...')
        router.push('/analyze')
      } catch (error) {
        ElMessage.error('提交失败，请重试: ' + error.message)
        console.error(error)
      } finally {
        loading.value = false
      }
    }
    
    return {
      activeTab,
      fileList,
      uploadForm,
      urlForm,
      loading,
      dialogVisible,
      goBack,
      handleFileChange,
      handleSubmit,
      confirmUpload
    }
  }
})
</script>

<style scoped>
.upload-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  margin-bottom: 10px;
}

.description {
  text-align: center;
  color: #606266;
  margin-bottom: 30px;
}

.upload-area {
  margin-bottom: 30px;
}

.upload-dragger {
  width: 100%;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.upload-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style> 