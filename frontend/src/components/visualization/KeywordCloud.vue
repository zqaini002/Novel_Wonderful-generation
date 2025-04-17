<template>
  <div class="chart-container">
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>
    <div v-else-if="!hasData" class="no-data-message">
      <i class="el-icon-warning-outline"></i>
      <p>暂无关键词数据</p>
    </div>
    <div ref="chartRef" class="keyword-cloud-chart" :style="{ opacity: hasData ? 1 : 0 }"></div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import * as echarts from 'echarts'
import 'echarts-wordcloud'

export default {
  name: 'KeywordCloud',
  props: {
    keywords: {
      type: Array,
      required: true,
      default: () => []
    },
    title: {
      type: String,
      default: '关键词云'
    },
    height: {
      type: String,
      default: '400px'
    },
    width: {
      type: String,
      default: '100%'
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const chartRef = ref(null)
    let chart = null
    
    // 计算是否有数据
    const hasData = computed(() => {
      return props.keywords && props.keywords.length > 0;
    });

    // 初始化图表
    const initChart = () => {
      // 确保DOM已挂载
      if (!chartRef.value) return

      // 销毁旧图表
      if (chart) {
        chart.dispose()
      }

      // 初始化图表实例
      chart = echarts.init(chartRef.value)
      
      // 如果没有数据
      if (!hasData.value) {
        return;
      }
      
      // 设置图表选项
      const option = {
        title: {
          text: props.title,
          left: 'center',
          textStyle: {
            fontWeight: 'normal',
            fontSize: 16
          }
        },
        tooltip: {
          show: true,
          formatter: function(params) {
            return params.data.name + ': ' + params.data.value
          },
          backgroundColor: 'rgba(50,50,50,0.7)',
          borderColor: '#ccc',
          borderWidth: 0,
          padding: [5, 10],
          textStyle: {
            color: '#fff'
          }
        },
        series: [{
          type: 'wordCloud',
          shape: 'circle',
          left: 'center',
          top: 'center',
          width: '90%',
          height: '90%',
          right: null,
          bottom: null,
          sizeRange: [12, 60],
          rotationRange: [-60, 60],
          rotationStep: 15,
          gridSize: 8,
          drawOutOfBound: false,
          layoutAnimation: true,
          textStyle: {
            fontFamily: 'sans-serif',
            fontWeight: 'bold',
            color: function() {
              // 更多柔和的调色板
              return 'rgb(' + 
                [
                  Math.round(Math.random() * 100 + 80),
                  Math.round(Math.random() * 140 + 60),
                  Math.round(Math.random() * 180 + 40)
                ].join(',') + ')'
            }
          },
          emphasis: {
            focus: 'self',
            textStyle: {
              shadowBlur: 10,
              shadowColor: '#333'
            }
          },
          data: formatKeywords(props.keywords)
        }]
      }

      // 渲染图表
      chart.setOption(option)
      
      // 添加窗口大小改变事件
      window.addEventListener('resize', handleResize)
    }

    // 格式化关键词数据
    const formatKeywords = (keywords) => {
      if (!keywords || !keywords.length) return []
      
      return keywords.map(item => {
        if (typeof item === 'string') {
          return {
            name: item,
            value: Math.floor(Math.random() * 90) + 10 // 如果只有词，随机生成权重
          }
        } else if (typeof item === 'object') {
          return {
            name: item.word || item.name || item.keyword || '',
            value: item.weight || item.value || item.count || Math.floor(Math.random() * 90) + 10
          }
        }
        return { name: '', value: 0 }
      }).filter(item => item.name)
    }

    // 处理窗口大小改变
    const handleResize = () => {
      chart && chart.resize()
    }

    // 监听关键词变化
    watch(() => props.keywords, (newVal) => {
      if (chart) {
        chart.setOption({
          series: [{
            data: formatKeywords(newVal)
          }]
        })
      } else {
        // 如果图表还未初始化，则初始化
        initChart()
      }
    }, { deep: true })
    
    // 监听加载状态
    watch(() => props.loading, (newVal) => {
      if (!newVal && !chart && chartRef.value) {
        // 加载完成且图表未初始化
        initChart()
      }
    })

    // 组件挂载时初始化图表
    onMounted(() => {
      if (!props.loading) {
        setTimeout(initChart, 0)
      }
    })

    // 组件卸载前销毁图表
    onBeforeUnmount(() => {
      if (chart) {
        chart.dispose()
        chart = null
      }
      window.removeEventListener('resize', handleResize)
    })

    return {
      chartRef,
      hasData,
      chart
    }
  }
}
</script>

<style scoped>
.chart-container {
  width: v-bind(width);
  height: v-bind(height);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.keyword-cloud-chart {
  width: 100%;
  height: 100%;
  transition: opacity 0.3s ease;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 10;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 10px;
  color: #666;
}

.no-data-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.no-data-message i {
  font-size: 32px;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 