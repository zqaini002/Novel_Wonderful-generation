<template>
  <div class="chart-container">
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>
    <div v-else-if="!hasData" class="no-data-message">
      <i class="el-icon-warning-outline"></i>
      <p>暂无情节波动数据</p>
    </div>
    <div ref="chartRef" class="plot-trend-chart" :style="{ opacity: hasData ? 1 : 0 }"></div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'PlotTrendChart',
  props: {
    plotData: {
      type: Array,
      required: true,
      default: () => []
    },
    title: {
      type: String,
      default: '情节波动图'
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
      return props.plotData && props.plotData.length > 0;
    });

    // 初始化图表
    const initChart = () => {
      // 确保DOM已挂载
      if (!chartRef.value) return

      // 检查DOM元素是否有尺寸
      if (chartRef.value.clientWidth === 0 || chartRef.value.clientHeight === 0) {
        // 如果没有尺寸，静默延迟初始化
        setTimeout(() => {
          initChart();
        }, 300);
        return;
      }

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
      
      const formattedData = formatPlotData(props.plotData)
      
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
          trigger: 'axis',
          formatter: function(params) {
            const dataIndex = params[0].dataIndex
            const data = formattedData.tooltips[dataIndex] || {}
            let result = `章节 ${params[0].name}<br/>`
            result += `情感波动值: ${params[0].value.toFixed(1)}<br/>`
            if (data.event) {
              result += `<strong>关键事件:</strong> ${data.event}<br/>`
            }
            return result
          },
          backgroundColor: 'rgba(50,50,50,0.7)',
          borderColor: '#ccc',
          borderWidth: 0,
          padding: [5, 10],
          textStyle: {
            color: '#fff'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: formattedData.chapters,
          axisLabel: {
            interval: Math.floor(formattedData.chapters.length / 10), // 动态调整标签显示频率
            rotate: formattedData.chapters.length > 20 ? 30 : 0 // 当章节过多时，斜角显示标签
          }
        },
        yAxis: {
          type: 'value',
          name: '情感强度',
          min: function(value) {
            return Math.floor(value.min * 1.1); // 稍微扩大范围
          },
          max: function(value) {
            return Math.ceil(value.max * 1.1);
          },
          splitLine: {
            lineStyle: {
              type: 'dashed',
              opacity: 0.6
            }
          }
        },
        series: [
          {
            name: '情感变化',
            type: 'line',
            smooth: true,
            // 加入动画设置
            animationDuration: 2000,
            animationEasing: 'quadraticOut',
            lineStyle: {
              width: 3,
              shadowColor: 'rgba(0,0,0,0.3)',
              shadowBlur: 10,
              shadowOffsetY: 8
            },
            // 波峰波谷标记
            markPoint: {
              symbolSize: 45,
              data: [
                { type: 'max', name: '最高点' },
                { type: 'min', name: '最低点' }
              ]
            },
            // 重要章节标记
            markArea: {
              itemStyle: {
                color: 'rgba(255, 173, 177, 0.2)'
              },
              data: formattedData.markAreas
            },
            // 关键事件标记
            markLine: {
              symbol: 'none',
              lineStyle: {
                type: 'dashed'
              },
              data: formattedData.markLines
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: 'rgba(58, 77, 233, 0.8)'
                },
                {
                  offset: 1,
                  color: 'rgba(58, 77, 233, 0.1)'
                }
              ])
            },
            data: formattedData.emotionValues
          }
        ]
      }

      // 渲染图表
      chart.setOption(option)
      
      // 添加窗口大小改变事件
      window.addEventListener('resize', handleResize)
    }

    // 格式化情节数据
    const formatPlotData = (plotData) => {
      if (!plotData || !plotData.length) {
        return {
          chapters: [],
          emotionValues: [],
          tooltips: [],
          markLines: [],
          markAreas: []
        }
      }
      
      const chapters = []
      const emotionValues = []
      const tooltips = []
      const markLines = []
      const markAreas = []
      
      // 处理各章节数据
      plotData.forEach((item, index) => {
        // 章节标题处理
        const chapterTitle = item.chapterNumber || item.chapter || `第${index + 1}章`
        chapters.push(chapterTitle)
        
        // 情感值
        const emotionValue = item.emotion || item.emotionValue || item.tension || 0
        emotionValues.push(emotionValue)
        
        // 工具提示
        tooltips.push({
          event: item.event || item.keyEvent || item.summary || '',
          emotion: emotionValue
        })
        
        // 标记重要事件
        if (item.isImportant || item.isKey || item.isCritical) {
          markLines.push({
            xAxis: index,
            label: {
              formatter: '重要事件',
              position: 'middle'
            }
          })
        }
        
        // 标记重要章节范围
        if (item.isClimaxStart && index < plotData.length - 1) {
          let endIndex = plotData.findIndex((endItem, endIdx) => endIdx > index && endItem.isClimaxEnd)
          if (endIndex === -1) endIndex = Math.min(index + 3, plotData.length - 1)
          
          markAreas.push([
            { xAxis: index },
            { xAxis: endIndex }
          ])
        }
      })
      
      return {
        chapters,
        emotionValues,
        tooltips,
        markLines,
        markAreas
      }
    }

    // 处理窗口大小改变
    const handleResize = () => {
      chart && chart.resize()
    }

    // 监听数据变化
    watch(() => props.plotData, (newVal) => {
      if (chart) {
        const formattedData = formatPlotData(newVal)
        chart.setOption({
          xAxis: {
            data: formattedData.chapters
          },
          series: [{
            data: formattedData.emotionValues,
            markArea: {
              data: formattedData.markAreas
            },
            markLine: {
              data: formattedData.markLines
            }
          }]
        })
      } else if (chartRef.value && hasData.value) {
        // 如果图表还未初始化，则初始化
        initChart()
      }
    }, { deep: true })
    
    // 监听加载状态
    watch(() => props.loading, (newVal) => {
      if (!newVal && !chart && chartRef.value && hasData.value) {
        // 加载完成且图表未初始化
        initChart()
      }
    })

    // 组件挂载时初始化图表
    onMounted(() => {
      // 使用nextTick确保DOM已渲染
      // 如果窗口已经加载完成
      if (document.readyState === 'complete') {
        setTimeout(() => {
          if (!props.loading && hasData.value) {
            initChart();
          }
        }, 300);
      } else {
        // 否则等待窗口完全加载
        window.addEventListener('load', () => {
          setTimeout(() => {
            if (!props.loading && hasData.value) {
              initChart();
            }
          }, 300);
        });
      }
      
      // 确保在组件销毁时移除事件监听
      window.addEventListener('resize', handleResize)
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
      chart,
      initChart
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

.plot-trend-chart {
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