<template>
  <div class="chart-container">
    <div ref="chartRef" class="structure-chart"></div>
  </div>
</template>

<script>
/* eslint-disable */
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'StructureAnalysisChart',
  props: {
    structureData: {
      type: Object,
      required: true,
      default: () => ({})
    },
    title: {
      type: String,
      default: '小说结构分析'
    },
    height: {
      type: String,
      default: '500px'
    },
    width: {
      type: String,
      default: '100%'
    }
  },
  setup(props) {
    const chartRef = ref(null)
    let chart = null

    // 初始化图表
    const initChart = () => {
      // 确保DOM已挂载
      if (!chartRef.value) return

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
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: formattedData.legendData
        },
        series: [
          // 外层饼图 - 主要章节分布
          {
            name: '章节分布',
            type: 'pie',
            selectedMode: 'single',
            radius: [0, '40%'],
            label: {
              position: 'inner',
              fontSize: 14
            },
            labelLine: {
              show: false
            },
            data: formattedData.mainData
          },
          // 内层饼图 - 详细结构
          {
            name: '详细结构',
            type: 'pie',
            radius: ['50%', '70%'],
            labelLine: {
              length: 30
            },
            label: {
              formatter: '{a|{a}}{abg|}\n{hr|}\n  {b|{b}：}{c}  {per|{d}%}  ',
              backgroundColor: '#F6F8FC',
              borderColor: '#8C8D8E',
              borderWidth: 1,
              borderRadius: 4,
              rich: {
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
      
      // 添加点击事件
      chart.on('click', params => {
        if (params.seriesIndex === 1) { // 点击详细部分
          showSectionDetails(params.data)
        }
      })
    }

    // 格式化结构数据
    const formatStructureData = (structureData) => {
      if (!structureData || Object.keys(structureData).length === 0) {
        return {
          mainData: [],
          detailData: [],
          legendData: []
        }
      }
      
      // 主要结构数据 - 通常是开端、发展、高潮、结局等
      const mainData = []
      const detailData = []
      const legendData = []
      
      // 默认颜色
      const defaultColors = [
        '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
        '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'
      ]
      
      // 处理主要结构
      if (structureData.mainStructure && Array.isArray(structureData.mainStructure)) {
        structureData.mainStructure.forEach((item, index) => {
          mainData.push({
            name: item.name,
            value: item.value || item.chapterCount || 1,
            itemStyle: {
              color: item.color || defaultColors[index % defaultColors.length]
            }
          })
          legendData.push(item.name)
        })
      } else {
        // 默认5大结构
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
      
      // 处理详细结构
      if (structureData.detailStructure && Array.isArray(structureData.detailStructure)) {
        structureData.detailStructure.forEach((item, index) => {
          detailData.push({
            name: item.name,
            value: item.value || item.chapterCount || 1,
            itemStyle: {
              color: item.color || defaultColors[index % defaultColors.length]
            },
            description: item.description || '',
            chapters: item.chapters || []
          })
          legendData.push(item.name)
        })
      } else {
        // 默认详细结构
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
    const showSectionDetails = (sectionData) => {
      if (!sectionData) return
      
      // 可以在这里实现点击后的详情显示逻辑
      // 例如显示一个包含章节列表的弹窗
      console.log('显示结构部分详情:', sectionData)
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
      setTimeout(initChart, 0)
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
      chart
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