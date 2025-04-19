<template>
  <div class="chart-container">
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>
    <div v-else-if="!hasData" class="no-data-message">
      <i class="el-icon-warning-outline"></i>
      <p>暂无人物关系数据</p>
    </div>
    <div ref="chartRef" class="character-network-chart" :style="{ opacity: hasData ? 1 : 0 }"></div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'CharacterNetworkChart',
  props: {
    networkData: {
      type: Object,
      required: true,
      default: () => ({ nodes: [], links: [] })
    },
    title: {
      type: String,
      default: '人物关系网络'
    },
    height: {
      type: String,
      default: '500px'
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
      return props.networkData && 
             props.networkData.nodes && 
             props.networkData.nodes.length > 0 &&
             props.networkData.links &&
             props.networkData.links.length > 0;
    });

    // 初始化图表
    const initChart = () => {
      // 确保DOM已挂载
      if (!chartRef.value) return

      // 检查DOM元素是否有尺寸
      if (chartRef.value.clientWidth === 0 || chartRef.value.clientHeight === 0) {
        // 如果没有尺寸，延迟初始化
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
      
      // 格式化关系网络数据
      const formattedData = formatNetworkData(props.networkData)
      
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
          trigger: 'item',
          formatter: function(params) {
            if (params.dataType === 'node') {
              return `<div style="font-weight:bold">${params.name}</div>` +
                     `<div>重要度: ${params.value}</div>` + 
                     `${params.data.desc ? `<div>${params.data.desc}</div>` : ''}`;
            } else {
              return `${params.data.source} → ${params.data.target}<br/>` +
                     `关系: ${params.data.relation || '未知'}` +
                     `${params.data.desc ? `<br/>${params.data.desc}` : ''}`;
            }
          }
        },
        legend: {
          data: formattedData.categories.map(category => category.name),
          orient: 'vertical',
          right: 10,
          top: 'center'
        },
        animationDuration: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [{
          name: '人物关系',
          type: 'graph',
          layout: 'force',
          data: formattedData.nodes,
          links: formattedData.links,
          categories: formattedData.categories,
          roam: true,
          draggable: true,
          label: {
            position: 'right',
            show: true,
            formatter: '{b}'
          },
          force: {
            repulsion: 200,
            edgeLength: [50, 100],
            layoutAnimation: true
          },
          lineStyle: {
            color: 'source',
            curveness: 0.3
          },
          emphasis: {
            focus: 'adjacency',
            lineStyle: {
              width: 4
            }
          }
        }]
      };

      // 渲染图表
      chart.setOption(option)
      
      // 添加双击节点事件 - 显示角色详细信息
      chart.on('dblclick', (params) => {
        if (params.dataType === 'node') {
          showCharacterDetail(params.data);
        }
      });
    }
    
    // 显示角色详细信息
    const showCharacterDetail = (character) => {
      if (!character) return;
      
      // 显示角色详情的逻辑保持不变
    }
    
    // 格式化网络数据，添加必要的视觉元素
    const formatNetworkData = (data) => {
      if (!data || !data.nodes || !data.links) {
        return { nodes: [], links: [], categories: [] };
      }
      
      // 提取所有角色类型作为分类
      const categoryMap = new Map();
      data.nodes.forEach(node => {
        if (node.category && !categoryMap.has(node.category)) {
          categoryMap.set(node.category, {
            name: node.category
          });
        }
      });
      
      // 转换为数组
      const categories = Array.from(categoryMap.values());
      
      // 如果没有定义类别，添加默认分类
      if (categories.length === 0) {
        categories.push({ name: '主要角色' });
        categories.push({ name: '次要角色' });
      }
      
      // 处理节点数据 - 添加视觉元素
      const nodes = data.nodes.map((node, index) => {
        // 确保每个节点有值且有分类
        const value = node.value !== undefined ? node.value : Math.random() * 30 + 20;
        const categoryIndex = node.category ? 
                            categories.findIndex(c => c.name === node.category) : 
                            (value > 30 ? 0 : 1); // 大值为主要角色，小值为次要角色
        
        // 生成随机颜色，如果节点没有指定颜色
        const getRandomColor = () => {
          const colors = [
            '#4992ff', '#7cffb2', '#fddd60', '#ff6e76', '#58d9f9', 
            '#05c091', '#ff8a45', '#8d48e3', '#dd79ff'
          ];
          return colors[Math.floor(Math.random() * colors.length)];
        };
        
        return {
          ...node,
          id: node.id || index,
          name: node.name,
          value: value,
          symbolSize: Math.sqrt(value) * 5, // 根据重要度计算节点大小
          category: categoryIndex,
          itemStyle: {
            color: node.color || getRandomColor()
          }
        };
      });
      
      // 处理关系数据 - 添加视觉元素
      const links = data.links.map(link => {
        // 计算线宽，基于关系强度
        const value = link.value !== undefined ? link.value : 1;
        const width = value * 2 > 5 ? 5 : value * 2 < 1 ? 1 : value * 2;
        
        return {
          ...link,
          lineStyle: {
            width: width
          },
          label: {
            show: !!link.relation,
            formatter: link.relation || ''
          }
        };
      });
      
      return {
        nodes,
        links,
        categories
      };
    }

    // 处理窗口大小改变
    const handleResize = () => {
      chart && chart.resize()
    }

    // 监听数据变化
    watch(() => props.networkData, (newVal) => {
      if (chart) {
        const formattedData = formatNetworkData(newVal)
        chart.setOption({
          legend: {
            data: formattedData.categories.map(category => category.name)
          },
          series: [{
            data: formattedData.nodes,
            links: formattedData.links,
            categories: formattedData.categories
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

.character-network-chart {
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