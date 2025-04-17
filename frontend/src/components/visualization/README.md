# 小说智析 - 可视化组件

该目录包含小说智析系统的可视化组件，用于展示小说的关键词分布、情节波动和结构分析等数据。

## 版本信息

- **当前版本**: v1.0.0 (2025-04-18)
- **兼容性**: Vue.js 3.2+, ECharts 5.4+
- **依赖**: echarts, echarts-wordcloud 2.1.0+

## 组件列表

### KeywordCloud.vue

关键词云组件，使用ECharts和echarts-wordcloud扩展实现，展示小说中的高频词汇和关键词。

```vue
<template>
  <KeywordCloud 
    :keywords="keywordData" 
    :loading="loadingKeywords" 
    title="小说关键词分布" 
    height="500px" 
  />
</template>

<script setup>
import { ref } from 'vue'
import KeywordCloud from '@/components/visualization/KeywordCloud.vue'
import visualizationService from '@/api/visualization'

const keywordData = ref([])
const loadingKeywords = ref(true)

const fetchKeywordData = async (novelId) => {
  try {
    const response = await visualizationService.getKeywordCloudData(novelId)
    keywordData.value = response || []
    loadingKeywords.value = false
  } catch (error) {
    console.error('获取关键词数据失败:', error)
    keywordData.value = []
    loadingKeywords.value = false
  }
}

// 初始化时调用
fetchKeywordData(1) // 小说ID
</script>
```

**Props:**
- `keywords`: Array - 关键词数据数组，格式为 `[{name: '词语', value: 权重}, ...]`
- `loading`: Boolean - 加载状态
- `title`: String - 图表标题
- `height`: String - 图表高度
- `width`: String - 图表宽度

### PlotTrendChart.vue

情节波动图组件，使用ECharts折线图实现，展示小说情节的起伏和情感变化。

```vue
<template>
  <PlotTrendChart 
    :emotional-data="emotionalData" 
    :loading="loadingEmotional" 
    title="小说情节波动趋势" 
  />
</template>

<script setup>
import { ref } from 'vue'
import PlotTrendChart from '@/components/visualization/PlotTrendChart.vue'
import visualizationService from '@/api/visualization'

const emotionalData = ref({})
const loadingEmotional = ref(true)

const fetchEmotionalData = async (novelId) => {
  try {
    const response = await visualizationService.getEmotionalFluctuationData(novelId)
    emotionalData.value = response || {}
    loadingEmotional.value = false
  } catch (error) {
    console.error('获取情节波动数据失败:', error)
    emotionalData.value = {}
    loadingEmotional.value = false
  }
}

// 初始化时调用
fetchEmotionalData(1) // 小说ID
</script>
```

**Props:**
- `emotionalData`: Object - 情感数据对象，包含情节波动数据和关键点标记
- `loading`: Boolean - 加载状态
- `title`: String - 图表标题
- `height`: String - 图表高度

### StructureAnalysisChart.vue

小说结构分析图表组件，使用ECharts双层饼图实现，展示小说的整体结构和章节分布。

```vue
<template>
  <StructureAnalysisChart 
    :structure-data="structureData" 
    title="小说结构分析" 
  />
</template>

<script setup>
import { ref } from 'vue'
import StructureAnalysisChart from '@/components/visualization/StructureAnalysisChart.vue'
import visualizationService from '@/api/visualization'

const structureData = ref({})
const loadingStructure = ref(true)

const fetchStructureData = async (novelId) => {
  try {
    const response = await visualizationService.getStructureAnalysisData(novelId)
    structureData.value = response || {}
    loadingStructure.value = false
  } catch (error) {
    console.error('获取结构分析数据失败:', error)
    structureData.value = {}
    loadingStructure.value = false
  }
}

// 初始化时调用
fetchStructureData(1) // 小说ID
</script>
```

**Props:**
- `structureData`: Object - 结构数据对象，包含主要结构和详细结构
- `title`: String - 图表标题
- `height`: String - 图表高度
- `width`: String - 图表宽度

## 依赖管理

### ECharts安装

所有可视化组件都依赖于ECharts库，确保项目已安装ECharts:

```bash
npm install echarts@5.4.2 --save
```

### 词云扩展安装

关键词云组件依赖于echarts-wordcloud扩展:

```bash
npm install echarts-wordcloud@2.1.0 --save
```

### 推荐的导入方式

在组件中导入ECharts的推荐方式:

```javascript
// 完整导入
import * as echarts from 'echarts'

// 或按需导入（性能更优）
import * as echarts from 'echarts/core'
import { 
  PieChart, 
  LineChart 
} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { 
  CanvasRenderer 
} from 'echarts/renderers'

// 注册组件
echarts.use([
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer
])
```

### 本地依赖备份

如果CDN或npm安装出现问题，可以将依赖文件下载到本地:

1. 创建 `public/lib` 目录
2. 下载 echarts.min.js 和 echarts-wordcloud.min.js 到该目录
3. 在 `index.html` 中引入:

```html
<script src="<%= BASE_URL %>lib/echarts.min.js"></script>
<script src="<%= BASE_URL %>lib/echarts-wordcloud.min.js"></script>
```

## 常见问题

### 词云组件不显示

如果词云组件无法正常显示，可能是echarts-wordcloud扩展未正确加载。解决方法:

1. 检查控制台错误信息
2. 确认echarts-wordcloud已正确安装
3. 在KeywordCloud.vue中增加以下调试代码:

```javascript
// 显式导入并确保注册wordcloud组件
import 'echarts-wordcloud'
// 确认wordCloud系列类型已注册的调试日志
console.log('KeywordCloud - echarts registered series types:', echarts.getMap('series'));
console.log('KeywordCloud - echarts version:', echarts.version);
```

### 图表尺寸问题

如果图表未正确展示或尺寸异常，可能是DOM元素尺寸计算有误。解决方法:

1. 确保容器元素有明确的宽度和高度
2. 使用Vue的`nextTick`确保DOM已渲染完成再初始化图表
3. 添加窗口resize事件监听以适应尺寸变化

```javascript
window.addEventListener('resize', () => {
  chart && chart.resize()
})
```

### 数据格式化问题

如果图表无法正确显示数据，可能是数据格式不匹配。解决方法:

1. 确保组件接收的数据符合预期格式
2. 在组件中添加数据格式化和验证逻辑
3. 处理空数据和异常情况

## 性能优化建议

1. **延迟加载**: 考虑使用Vue的动态导入功能延迟加载大型组件
2. **按需引入ECharts**: 只引入所需的ECharts组件而非整个库
3. **数据过滤**: 对大数据集进行预处理，只保留必要的数据点
4. **缓存结果**: 缓存计算结果，避免重复处理相同数据 

## 更新日志

### v1.0.0 (2025-04-18)
- 为所有组件添加详细文档和使用示例
- 优化图表初始化和数据处理逻辑
- 解决DOM渲染和响应式问题
- 完善错误处理和边缘情况

### v0.9.0 (2025-04-17)
- 修复echarts-wordcloud依赖问题
- 统一组件接口和属性命名
- 优化组件渲染性能
- 解决移动端兼容性问题 