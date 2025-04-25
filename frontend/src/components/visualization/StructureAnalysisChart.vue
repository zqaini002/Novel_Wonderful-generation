<template>
  <div class="chart-container">
    <div ref="chartRef" class="structure-chart"></div>
  </div>
</template>

<script>
/* eslint-disable */
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'

/**
 * 小说结构分析图表组件
 * 
 * 该组件使用ECharts绘制双层饼图来可视化小说的结构:
 * - 内层饼图: 展示小说的主要结构部分(开端、发展、高潮、结局等)
 * - 外层环形图: 展示更详细的结构划分(各个子情节、转折点等)
 * 
 * 使用双层图表可以同时提供宏观结构和微观细节，帮助理解小说架构。
 */
export default {
  name: 'StructureAnalysisChart',
  props: {
    /**
     * 小说结构数据，预期格式：
     * {
     *   mainStructure: [{ name: '开端', value: 5, color: '#5470c6' }, ...],
     *   detailStructure: [{ name: '角色介绍', value: 2, startChapter: 1, endChapter: 2 }, ...]
     * }
     */
    structureData: {
      type: Object,
      required: true,
      default: () => ({})
    },
    // 图表标题
    title: {
      type: String,
      default: '小说结构分析'
    },
    // 图表高度
    height: {
      type: String,
      default: '500px'
    },
    // 图表宽度
    width: {
      type: String,
      default: '100%'
    }
  },
  setup(props) {
    // 图表DOM引用
    const chartRef = ref(null)
    // 图表实例
    let chart = null

    /**
     * 初始化图表
     * 
     * 该方法执行以下操作:
     * 1. 检查DOM元素是否就绪并有有效尺寸
     * 2. 格式化结构数据为ECharts所需格式
     * 3. 配置并渲染双层饼图
     * 4. 设置交互事件监听器
     */
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
      
      // 格式化结构数据
      const formattedData = formatStructureData(props.structureData)
      
      // 设置图表选项
      const option = {
        title: {
          text: props.title,
          left: 'center',
          top: 0
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)' // 显示名称、章节数和百分比
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: formattedData.legendData
        },
        series: [
          // 内层饼图 - 主要章节分布(如开端、发展、高潮、结局)
          {
            name: '章节分布',
            type: 'pie',
            selectedMode: 'single',
            radius: [0, '40%'], // 内层饼图半径
            label: {
              position: 'inner', // 标签放在扇区内部
              fontSize: 14
            },
            labelLine: {
              show: false // 不显示引导线
            },
            data: formattedData.mainData
          },
          // 外层环形图 - 详细结构划分
          {
            name: '详细结构',
            type: 'pie',
            radius: ['50%', '70%'], // 外层环形图半径，形成环形
            labelLine: {
              length: 30 // 引导线长度
            },
            label: {
              // 复杂格式的标签，包含多种样式
              formatter: '{a|{a}}{abg|}\n{hr|}\n  {b|{b}：}{c}  {per|{d}%}  ',
              backgroundColor: '#F6F8FC',
              borderColor: '#8C8D8E',
              borderWidth: 1,
              borderRadius: 4,
              rich: {
                // 富文本配置，为不同部分设置不同样式
                a: {
                  color: '#6E7079',
                  lineHeight: 22,
                  align: 'center'
                },
                hr: {
                  borderColor: '#8C8D8E',
                  width: '100%',
                  borderWidth: 1,
                  height: 0
                },
                b: {
                  color: '#4C5058',
                  fontSize: 14,
                  fontWeight: 'bold',
                  lineHeight: 33
                },
                per: {
                  color: '#fff',
                  backgroundColor: '#4C5058',
                  padding: [3, 4],
                  borderRadius: 4
                }
              }
            },
            data: formattedData.detailData
          }
        ]
      }

      // 渲染图表
      chart.setOption(option)
      
      // 添加窗口大小改变事件
      window.addEventListener('resize', handleResize)
      
      // 添加点击事件 - 点击详细结构显示更多信息
      chart.on('click', params => {
        if (params.seriesIndex === 1) { // 点击详细部分
          showSectionDetail(params.data)
        }
      })
    }

    /**
     * 格式化结构数据为ECharts所需的格式
     * 
     * @param {Object} structureData - 原始结构数据对象
     * @returns {Object} 包含mainData、detailData和legendData的对象
     * 
     * 函数处理两种情况:
     * 1. 如果有有效的结构数据，则格式化为ECharts所需格式
     * 2. 如果没有数据或数据无效，则使用默认结构数据
     */
    const formatStructureData = (structureData) => {
      // 检查数据有效性
      if (!structureData || Object.keys(structureData).length === 0) {
        return {
          mainData: [],
          detailData: [],
          legendData: []
        }
      }
      
      // 初始化数据数组
      const mainData = [] // 主要结构数据
      const detailData = [] // 详细结构数据
      const legendData = [] // 图例数据
      
      // 默认颜色调色板 - 确保图表美观一致
      const defaultColors = [
        '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
        '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'
      ]
      
      // 处理主要结构数据(如开端、发展、高潮、结局)
      if (structureData.mainStructure && Array.isArray(structureData.mainStructure)) {
        // 转换API返回的数据格式为ECharts所需格式
        structureData.mainStructure.forEach((item, index) => {
          mainData.push({
            name: item.name, // 结构部分名称
            value: item.value || item.chapterCount || 1, // 章节数量
            itemStyle: {
              color: item.color || defaultColors[index % defaultColors.length] // 颜色分配
            }
          })
          legendData.push(item.name)
        })
      } else {
        // 如果没有数据，使用默认的5大结构
        const defaultStructure = [
          { name: '开端', value: 1, color: '#5470c6' },
          { name: '铺垫', value: 2, color: '#91cc75' },
          { name: '发展', value: 3, color: '#fac858' },
          { name: '高潮', value: 2, color: '#ee6666' },
          { name: '结局', value: 1, color: '#73c0de' }
        ]
        
        defaultStructure.forEach(item => {
          mainData.push({
            name: item.name,
            value: item.value,
            itemStyle: { color: item.color }
          })
          legendData.push(item.name)
        })
      }
      
      // 处理详细结构数据(更具体的情节划分)
      if (structureData.detailStructure && Array.isArray(structureData.detailStructure)) {
        structureData.detailStructure.forEach((item, index) => {
          detailData.push({
            name: item.name, // 小节名称
            value: item.value || item.chapterCount || 1, // 章节数量
            itemStyle: {
              color: item.color || defaultColors[index % defaultColors.length] // 颜色分配
            },
            description: item.description || '', // 小节描述
            chapters: item.chapters || [] // 包含的章节
          })
          legendData.push(item.name)
        })
      } else {
        // 如果没有详细数据，使用默认的详细结构
        const defaultDetailStructure = [
          { name: '角色介绍', value: 1 },
          { name: '世界设定', value: 1 },
          { name: '初始冲突', value: 1 },
          { name: '情节递进', value: 2 },
          { name: '次要冲突', value: 1 },
          { name: '关系发展', value: 1 },
          { name: '主要冲突', value: 1 },
          { name: '危机', value: 1 },
          { name: '转折点', value: 1 },
          { name: '高潮', value: 1 },
          { name: '解决', value: 1 },
          { name: '结局', value: 1 }
        ]
        
        defaultDetailStructure.forEach((item, index) => {
          detailData.push({
            name: item.name,
            value: item.value,
            description: '',
            chapters: []
          })
          legendData.push(item.name)
        })
      }
      
      // 为分段数据分配颜色
      const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'];
      // eslint-disable-next-line no-unused-vars
      detailData.forEach((section, index) => {
        section.color = colors[index % colors.length];
      });
      
      return {
        mainData,
        detailData,
        legendData: [...new Set(legendData)] // 去重
      }
    }
    
    // 显示结构部分详情
    const showSectionDetail = (sectionData) => {
      if (sectionData) {
        // 移除console.log语句
      }
    }

    // 处理窗口大小改变
    const handleResize = () => {
      chart && chart.resize()
    }

    // 监听数据变化
    watch(() => props.structureData, (newVal) => {
      if (chart) {
        const formattedData = formatStructureData(newVal)
        chart.setOption({
          legend: {
            data: formattedData.legendData
          },
          series: [
            {
              data: formattedData.mainData
            },
            {
              data: formattedData.detailData
            }
          ]
        })
      }
    }, { deep: true })

    // 组件挂载时初始化图表
    onMounted(() => {
      // 使用延迟初始化确保DOM已渲染
      if (document.readyState === 'complete') {
        setTimeout(() => {
          initChart();
        }, 300);
      } else {
        // 否则等待窗口完全加载
        window.addEventListener('load', () => {
          setTimeout(() => {
            initChart();
          }, 300);
        });
      }

      // 添加窗口大小改变事件
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
}

.structure-chart {
  width: 100%;
  height: 100%;
}
</style> 