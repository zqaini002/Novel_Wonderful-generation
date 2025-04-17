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
// 显式导入并确保注册wordcloud组件
import 'echarts-wordcloud'
import { WarningFilled } from '@element-plus/icons-vue'

// 确认wordCloud系列类型已注册的调试日志
console.log('KeywordCloud - echarts registered series types:', echarts.getMap('series'));
console.log('KeywordCloud - echarts version:', echarts.version);

export default {
  name: 'KeywordCloud',
  components: {
    WarningFilled
  },
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
      const hasKeywords = props.keywords && props.keywords.length > 0;
      console.log('KeywordCloud - hasData计算:', hasKeywords, props.keywords);
      return hasKeywords;
    });

    // 初始化图表
    const initChart = () => {
      console.log('KeywordCloud - 开始初始化图表');
      // 确保DOM已挂载
      if (!chartRef.value) {
        console.warn('KeywordCloud - chartRef.value不存在，跳过初始化');
        return;
      }

      // 销毁旧图表
      if (chart) {
        console.log('KeywordCloud - 销毁旧图表实例');
        chart.dispose();
        chart = null;
      }

      // 使用 nextTick 确保DOM已完全渲染并且有正确的尺寸
      nextTick(() => {
        console.log('KeywordCloud - nextTick 回调开始执行');
        // 检查DOM是否可见且有尺寸
        if (!chartRef.value || chartRef.value.clientWidth === 0 || chartRef.value.clientHeight === 0) {
          // 如果DOM尺寸为0，延迟初始化
          console.warn('KeywordCloud - DOM尺寸为0，延迟初始化');
          setTimeout(() => {
            initChart();
          }, 500);
          return;
        }

        try {
          // 初始化图表实例
          console.log('KeywordCloud - 创建echarts实例');
          chart = echarts.init(chartRef.value);
          
          // 验证chart实例
          if (!chart) {
            console.error('KeywordCloud - 创建图表实例失败');
            return;
          }

          // 检查数据状态
          console.log('KeywordCloud - 数据检查: hasData=', hasData.value, 'keywords=', props.keywords);
          
          // 如果没有数据，渲染空图表
          if (!hasData.value) {
            console.log('KeywordCloud - 无数据，显示空图表');
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
              }
            });
            return;
          }
          
          // 格式化并检查处理后的数据
          const formattedData = formatKeywords(props.keywords);
          console.log('KeywordCloud - 格式化后的数据:', formattedData);
          
          if (!formattedData.length) {
            console.warn('KeywordCloud - 格式化后数据为空');
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
              }
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
                    [
                      Math.round(Math.random() * 100 + 80),
                      Math.round(Math.random() * 140 + 60),
                      Math.round(Math.random() * 180 + 40)
                    ].join(',') + ')';
                }
              },
              emphasis: {
                focus: 'self',
                textStyle: {
                  shadowBlur: 10,
                  shadowColor: '#333'
                }
              },
              data: formattedData
            }]
          };

          // 在渲染前检查数据
          console.log('KeywordCloud - 最终图表配置:', option);

          // 渲染图表
          try {
            chart.setOption(option);
            console.log('KeywordCloud - 图表渲染成功');
          } catch (error) {
            console.error('KeywordCloud - 图表渲染失败:', error);
          }
        } catch (error) {
          console.error('KeywordCloud - 图表初始化过程出错:', error);
        }
      });
    };

    // 格式化关键词数据
    const formatKeywords = (keywords) => {
      console.log('KeywordCloud - 开始格式化关键词数据:', keywords);
      
      // 首先检查数据是否存在
      if (!keywords) {
        console.warn('KeywordCloud - 关键词数据为null或undefined');
        return [];
      }
      
      // 确保keywords是一个数组
      if (!Array.isArray(keywords)) {
        console.warn('KeywordCloud - 关键词数据不是数组格式:', keywords);
        
        // 尝试从对象中提取关键词数据
        if (typeof keywords === 'object' && keywords !== null) {
          // 查找可能的数组属性
          for (const key in keywords) {
            if (Array.isArray(keywords[key])) {
              console.log('KeywordCloud - 从对象中找到关键词数组:', key);
              keywords = keywords[key];
              break;
            }
          }
        }
        
        // 如果仍然不是数组，返回空数组
        if (!Array.isArray(keywords)) {
          console.error('KeywordCloud - 无法处理的关键词数据格式，返回空数组');
          return [];
        }
      }
      
      // 如果数组为空直接返回
      if (keywords.length === 0) {
        console.warn('KeywordCloud - 关键词数组为空');
        return [];
      }
      
      try {
        const result = keywords.map(item => {
          // 字符串类型直接处理
          if (typeof item === 'string') {
            return {
              name: item,
              value: Math.floor(Math.random() * 90) + 10 // 如果只有词，随机生成权重
            };
          } 
          // 对象类型，尝试提取name和value
          else if (typeof item === 'object' && item !== null) {
            const name = item.word || item.name || item.keyword || '';
            const value = item.weight || item.value || item.count || Math.floor(Math.random() * 90) + 10;
            return { name, value };
          }
          // 其他类型返回空对象
          return { name: '', value: 0 };
        }).filter(item => item.name); // 过滤掉没有name的项
        
        console.log('KeywordCloud - 格式化后的关键词数据:', result);
        return result;
      } catch (error) {
        console.error('KeywordCloud - 格式化关键词数据出错:', error);
        return [];
      }
    };

    // 处理窗口大小改变
    const handleResize = () => {
      if (chart) {
        console.log('KeywordCloud - 窗口大小改变，重新调整图表大小');
        chart.resize();
      }
    };

    // 监听关键词变化
    watch(() => props.keywords, (newVal, oldVal) => {
      console.log('KeywordCloud - 关键词数据变化:', 
                 '新值长度:', newVal ? newVal.length : 0, 
                 '旧值长度:', oldVal ? oldVal.length : 0);
      
      if (chart) {
        const formattedData = formatKeywords(newVal);
        console.log('KeywordCloud - 更新图表数据:', formattedData);
        
        chart.setOption({
          series: [{
            data: formattedData
          }]
        });
      } else {
        // 如果图表还未初始化，则初始化
        console.log('KeywordCloud - 图表未初始化，开始初始化');
        initChart();
      }
    }, { deep: true });
    
    // 监听加载状态
    watch(() => props.loading, (newVal) => {
      console.log('KeywordCloud - 加载状态变更:', newVal);
      
      if (!newVal && !chart && chartRef.value) {
        // 加载完成且图表未初始化
        console.log('KeywordCloud - 加载完成，图表未初始化，开始初始化');
        initChart();
      }
    });

    // 组件挂载时初始化图表
    onMounted(() => {
      console.log('KeywordCloud - 组件挂载完成');
      
      // 在初始化前确认下父组件是否已传入数据
      console.log('KeywordCloud - 挂载时数据状态:', 
                 '关键词数量:', props.keywords ? props.keywords.length : 0, 
                 '加载状态:', props.loading);
      
      // 使用setTimeout确保DOM完全渲染
      setTimeout(() => {
        // 如果窗口已经加载完成
        if (document.readyState === 'complete') {
          console.log('KeywordCloud - 文档已加载完成，初始化图表');
          nextTick().then(() => initChart());
        } else {
          // 否则等待窗口完全加载
          console.log('KeywordCloud - 等待文档加载完成');
          window.addEventListener('load', () => {
            console.log('KeywordCloud - 文档加载事件触发，初始化图表');
            nextTick().then(() => initChart());
          });
        }
      }, 200);
      
      // 确保在组件销毁时移除事件监听
      window.addEventListener('resize', handleResize);
    });

    // 组件卸载前销毁图表
    onBeforeUnmount(() => {
      console.log('KeywordCloud - 组件卸载');
      if (chart) {
        console.log('KeywordCloud - 销毁图表实例');
        chart.dispose();
        chart = null;
      }
      window.removeEventListener('resize', handleResize);
    });

    return {
      chartRef,
      hasData,
      chart
    };
  }
};
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