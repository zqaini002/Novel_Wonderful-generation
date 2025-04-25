// 导入各个可视化组件
import KeywordCloud from './KeywordCloud.vue'
import PlotTrendChart from './PlotTrendChart.vue'
import StructureAnalysisChart from './StructureAnalysisChart.vue'
import CharacterNetworkChart from './CharacterNetworkChart.vue'

// 单独导出每个组件，方便按需引入
export { KeywordCloud, PlotTrendChart, StructureAnalysisChart, CharacterNetworkChart }

// 默认导出为一个组件对象，方便批量注册
export default {
  KeywordCloud,
  PlotTrendChart,
  StructureAnalysisChart,
  CharacterNetworkChart
} 