<template>
  <div class="chart-container">
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>
    <div v-else-if="!hasData" class="no-data-message">
      <el-icon><WarningFilled /></el-icon>
      <p>暂无关键词数据</p>
    </div>
    <div ref="chartRef" class="keyword-cloud-chart" :style="{ opacity: hasData ? 1 : 0 }"></div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
// 导入并确保注册wordcloud组件
import 'echarts-wordcloud'
import { WarningFilled } from '@element-plus/icons-vue'

export default {
  name: 'KeywordCloud',
  components: {
    WarningFilled
  },
  props: {
    keywords: {
      type: [Array, Object],
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
      // 如果是数组，检查长度
      if (Array.isArray(props.keywords)) {
        return props.keywords.length > 0;
      }
      // 如果是对象，检查是否有子项可以包含数据
      else if (props.keywords && typeof props.keywords === 'object') {
        return Object.keys(props.keywords).length > 0;
      }
      return false;
    });

    // 初始化图表
    const initChart = () => {
      // 确保DOM已挂载
      if (!chartRef.value) {
        // 静默处理，不输出警告，避免控制台污染
        // 如果组件正在加载中或还未完全挂载，稍后重试
        setTimeout(() => {
          if (document.body.contains(chartRef.value?.parentNode) || !chartRef.value) {
            initChart();
          }
        }, 500);
        return;
      }

      // 销毁旧图表
      if (chart) {
        chart.dispose();
        chart = null;
      }

      // 使用 nextTick 确保DOM已完全渲染并且有正确的尺寸
      nextTick(() => {
        // 检查DOM是否可见且有尺寸
        if (!chartRef.value || chartRef.value.clientWidth === 0 || chartRef.value.clientHeight === 0) {
          // 如果DOM尺寸为0，静默延迟初始化，不输出警告
          // 添加递归终止条件，避免无限循环
          let retryCount = parseInt(chartRef.value?.dataset?.retryCount || '0');
          if (retryCount < 5) { // 最多重试5次
            // 将重试次数存储在DOM元素上
            if (chartRef.value) {
              chartRef.value.dataset.retryCount = (retryCount + 1).toString();
            }
            setTimeout(() => {
              if (document.body.contains(chartRef.value?.parentNode)) {
                initChart();
              }
            }, 500);
          }
          return;
        }

        try {
          console.log('KeywordCloud: Initializing chart...');
          // 初始化图表实例
          chart = echarts.init(chartRef.value);
          
          // 验证chart实例
          if (!chart) {
            console.error('KeywordCloud: Failed to initialize chart');
            return;
          }
          console.log('KeywordCloud: Chart initialized successfully');

          // 如果没有数据，渲染空图表
          if (!hasData.value) {
            chart.setOption({
              title: {
                text: props.title || '关键词云',
                left: 'center'
              },
              graphic: {
                type: 'text',
                left: 'center',
                top: 'middle',
                style: {
                  text: '暂无数据',
                  font: '14px sans-serif',
                  fill: '#999'
                }
              },
              series: [] // 确保series存在但为空数组
            });
            return;
          }
          
          // 格式化并检查处理后的数据
          const formattedData = formatKeywords(props.keywords);
          
          if (!formattedData.length) {
            chart.setOption({
              title: {
                text: props.title || '关键词云',
                left: 'center'
              },
              graphic: {
                type: 'text',
                left: 'center',
                top: 'middle',
                style: {
                  text: '无有效关键词数据',
                  font: '14px sans-serif',
                  fill: '#999'
                }
              },
              series: [] // 确保series存在但为空数组
            });
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
                return params.data.name + ': ' + params.data.value;
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
              name: '关键词云',
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
                    Math.round(Math.random() * 95 + 100) + ',' + 
                    Math.round(Math.random() * 95 + 100) + ',' + 
                    Math.round(Math.random() * 95 + 160) + 
                    ')';
                }
              },
              emphasis: {
                textStyle: {
                  shadowBlur: 10,
                  shadowColor: '#333'
                }
              },
              data: formattedData
            }]
          };
          
          // 渲染图表
          chart.setOption(option);
          
          // 绑定窗口大小改变事件，自动调整图表大小
          window.addEventListener('resize', resizeChart);
        } catch (error) {
          // 处理初始化或渲染错误
        }
      });
    };
    
    // 格式化关键词数据
    const formatKeywords = (keywords) => {
      if (!keywords || keywords.length === 0) {
        console.log('Keywords data is empty or undefined');
        return [];
      }
      
      console.log('Formatting keywords data:', keywords);
      
      // 检查数据格式
      // 格式1: 数组对象 [{name: 'keyword', value: 10}, ...]
      // 格式2: 简单数组 ['keyword1', 'keyword2', ...]
      // 格式3: {keywords: [...]} 或其他包含关键词数组的对象
      
      // 尝试格式1: 对象数组包含name和value
      if (keywords.length > 0 && typeof keywords[0] === 'object' && 'name' in keywords[0] && 'value' in keywords[0]) {
        console.log('Format 1 detected: Array of objects with name and value');
        return keywords;
      }
      
      // 尝试格式2: 字符串数组
      if (keywords.length > 0 && typeof keywords[0] === 'string') {
        console.log('Format 2 detected: Array of strings');
        // 转换为wordcloud需要的格式
        return keywords.map(word => ({
          name: word,
          value: Math.random() * 50 + 50 // 随机值，使得词云更丰富
        }));
      }
      
      // 尝试格式3: 对象中可能包含关键词
      if (keywords.length === 1 && typeof keywords[0] === 'object') {
        console.log('Format 3 detected: Object possibly containing keywords array');
        for (const key in keywords[0]) {
          const item = keywords[0][key];
          if (Array.isArray(item)) {
            if (item.length > 0) {
              if (typeof item[0] === 'object' && 'name' in item[0] && 'value' in item[0]) {
                console.log('Found valid keyword data in object property:', key);
                return item;
              } else if (typeof item[0] === 'string') {
                console.log('Found string array in object property:', key);
                return item.map(word => ({
                  name: word,
                  value: Math.random() * 50 + 50
                }));
              }
            }
          }
        }
      }
      
      // 检查keywords是否直接是一个对象（而非数组）
      if (!Array.isArray(keywords) && typeof keywords === 'object') {
        console.log('Keywords is an object, not an array');
        // 尝试从对象中提取关键词数组
        for (const key in keywords) {
          const item = keywords[key];
          if (Array.isArray(item) && item.length > 0) {
            console.log('Found array in keywords object property:', key);
            if (typeof item[0] === 'object' && 'name' in item[0] && 'value' in item[0]) {
              return item;
            } else if (typeof item[0] === 'string') {
              return item.map(word => ({
                name: word,
                value: Math.random() * 50 + 50
              }));
            }
          }
        }
      }
      
      // 最后尝试：寻找任何可能是关键词数组的字段
      if (keywords && typeof keywords === 'object') {
        console.log('Attempting to find any array in the keywords object');
        for (const key in keywords) {
          if (Array.isArray(keywords[key]) && keywords[key].length > 0) {
            const item = keywords[key];
            if (typeof item[0] === 'object' && 'name' in item[0] && 'value' in item[0]) {
              console.log('Found valid keyword array in property:', key);
              return item;
            } else if (typeof item[0] === 'string') {
              console.log('Found string array in property:', key);
              return item.map(word => ({
                name: word,
                value: Math.random() * 50 + 50
              }));
            }
          }
        }
      }
      
      console.warn('Could not find valid keyword data in the provided structure');
      // 无法找到有效的关键词数据
      return [];
    };
    
    // 调整图表大小
    const resizeChart = () => {
      if (chart) {
        chart.resize();
      }
    };
    
    // 监听关键词数据变化
    watch(() => props.keywords, (newVal) => {
      if (newVal && newVal.length > 0) {
        const formattedData = formatKeywords(newVal);
        
        if (chart) {
          if (formattedData.length > 0) {
            // 有数据时设置正常的series
            chart.setOption({
              series: [{
                name: '关键词云',
                type: 'wordCloud',
                data: formattedData
              }]
            });
          } else {
            // 没有数据时设置空的series数组
            chart.setOption({
              series: []
            });
          }
        } else if (formattedData.length > 0) {
          initChart();
        }
      }
    }, { deep: true });
    
    // 监听加载状态变化
    watch(() => props.loading, (newVal) => {
      if (!newVal && !chart && hasData.value) {
        initChart();
      }
    });
    
    // 组件挂载时初始化图表
    onMounted(() => {
      // 检查是否为浏览器环境
      if (typeof window !== 'undefined' && typeof document !== 'undefined') {
        // 检查文档是否已加载完成
        if (document.readyState === 'complete') {
          initChart();
        } else {
          // 等待文档加载完成
          window.addEventListener('load', initChart);
        }
      }
    });
    
    // 组件卸载时清理
    onBeforeUnmount(() => {
      if (chart) {
        chart.dispose();
        chart = null;
      }
      window.removeEventListener('resize', resizeChart);
      window.removeEventListener('load', initChart);
    });
    
    return {
      chartRef,
      hasData,
      chart,
      initChart
    };
  }
}
</script>

<style scoped>
.chart-container {
  position: relative;
  width: v-bind(width);
  height: v-bind(height);
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.keyword-cloud-chart {
  width: 100%;
  height: 100%;
  transition: opacity 0.5s ease;
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
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.no-data-message {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #fff;
  z-index: 5;
}

.no-data-message i {
  font-size: 30px;
  color: #ccc;
  margin-bottom: 10px;
}

.no-data-message p {
  font-size: 14px;
  color: #999;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 