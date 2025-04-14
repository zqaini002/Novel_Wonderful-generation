import { createStore } from 'vuex'
import novelApi from '@/api/novel'
import { auth } from './auth.module'

export default createStore({
  state: {
    novels: [],
    currentNovel: null,
    processingStatus: null,
    loading: false,
    error: null
  },
  mutations: {
    SET_NOVELS(state, novels) {
      state.novels = novels
    },
    SET_CURRENT_NOVEL(state, novel) {
      state.currentNovel = novel
    },
    SET_PROCESSING_STATUS(state, status) {
      state.processingStatus = status
    },
    SET_LOADING(state, status) {
      state.loading = status
    },
    SET_ERROR(state, error) {
      state.error = error
    }
  },
  actions: {
    // 获取小说列表
    async fetchNovels({ commit }) {
      try {
        commit('SET_LOADING', true)
        const response = await novelApi.getNovelList()
        console.log('获取小说列表响应:', response)
        
        // 根据实际返回格式处理数据
        let novels = []
        if (response) {
          // 可能的返回格式处理
          if (response.novels) {
            novels = response.novels
          } else if (Array.isArray(response)) {
            novels = response
          } else if (response.data && response.data.novels) {
            novels = response.data.novels
          } else if (response.data && Array.isArray(response.data)) {
            novels = response.data
          }
        }
        
        // 如果获取不到数据，设置示例数据
        if (novels.length === 0) {
          novels = [
            {
              id: 1,
              title: '示例小说1',
              author: '作者A',
              processedChapters: 10,
              totalChapters: 20,
              description: '这是一部示例小说，用于开发测试'
            },
            {
              id: 2,
              title: '示例小说2',
              author: '作者B',
              processedChapters: 15,
              totalChapters: 15,
              description: '这是另一部示例小说，已处理完成'
            }
          ]
          console.log('使用示例数据')
        }
        
        commit('SET_NOVELS', novels)
        commit('SET_ERROR', null)
      } catch (error) {
        console.error('获取小说列表失败:', error)
        // 出错时设置示例数据
        const demoNovels = [
          {
            id: 1,
            title: '示例小说1',
            author: '作者A',
            processedChapters: 10,
            totalChapters: 20,
            description: '这是一部示例小说，用于开发测试'
          },
          {
            id: 2,
            title: '示例小说2',
            author: '作者B',
            processedChapters: 15,
            totalChapters: 15,
            description: '这是另一部示例小说，已处理完成'
          }
        ]
        commit('SET_NOVELS', demoNovels)
        commit('SET_ERROR', '获取小说列表失败，显示示例数据')
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 获取小说详情
    async fetchNovelDetail({ commit }, novelId) {
      try {
        commit('SET_LOADING', true)
        const response = await novelApi.getNovelDetail(novelId)
        commit('SET_CURRENT_NOVEL', response)
        commit('SET_ERROR', null)
      } catch (error) {
        commit('SET_ERROR', '获取小说详情失败')
        console.error(error)
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 上传小说
    async uploadNovel({ commit, dispatch }, { file, title, author, url }) {
      try {
        commit('SET_LOADING', true)
        commit('SET_PROCESSING_STATUS', 'UPLOADING')
        
        // 准备上传参数和表单数据
        const formData = new FormData();
        
        if (file) {
          formData.append('file', file);
          formData.append('title', title);
          if (author) formData.append('author', author);
        } else if (url) {
          formData.append('url', url);
          formData.append('title', title);
          if (author) formData.append('author', author);
        }
        
        // 使用统一的uploadNovel方法
        const response = await novelApi.uploadNovel(formData);
        
        const novelId = response.id || response.novelId;
        // 处理状态由后端API更新
        commit('SET_PROCESSING_STATUS', 'PROCESSING')
        
        // 启动定时器轮询处理状态
        const statusCheckInterval = setInterval(async () => {
          try {
            const statusResponse = await novelApi.getNovelStatus(novelId)
            
            // 处理小说不存在的情况
            if (statusResponse.status === 'NOT_FOUND') {
              console.error('小说不存在:', statusResponse.error)
              clearInterval(statusCheckInterval)
              commit('SET_PROCESSING_STATUS', 'FAILED')
              commit('SET_ERROR', `小说处理失败: ${statusResponse.error}`)
              return
            }
            
            commit('SET_PROCESSING_STATUS', statusResponse.status.toUpperCase())
            
            if (statusResponse.status === 'COMPLETED' || statusResponse.status === 'FAILED') {
              clearInterval(statusCheckInterval)
              
              if (statusResponse.status === 'COMPLETED') {
                // 获取完整数据
                await dispatch('fetchNovelDetail', novelId)
              }
            }
          } catch (error) {
            console.error('检查状态失败', error)
          }
        }, 2000)
        
        // 返回小说ID
        return novelId
      } catch (error) {
        commit('SET_ERROR', '上传小说失败')
        commit('SET_PROCESSING_STATUS', 'FAILED')
        console.error(error)
        throw error
      } finally {
        commit('SET_LOADING', false)
      }
    }
  },
  getters: {
    getNovelById: (state) => (id) => {
      return state.novels.find(novel => novel.id === parseInt(id))
    },
    processingPercentage: (state) => {
      if (!state.processingStatus) return 0
      
      switch (state.processingStatus) {
        case 'UPLOADING': return 20
        case 'PROCESSING': return 60
        case 'COMPLETED': return 100
        case 'FAILED': return 0
        default: return 0
      }
    },
    // 获取用户登录状态
    isLoggedIn: (state) => {
      return state.auth && state.auth.status && state.auth.status.loggedIn
    },
    // 获取当前登录用户
    currentUser: (state) => {
      return state.auth && state.auth.user ? state.auth.user : null
    }
  },
  modules: {
    auth
  }
}) 