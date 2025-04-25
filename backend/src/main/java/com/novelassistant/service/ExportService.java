package com.novelassistant.service;

import java.util.Map;

/**
 * 导出服务接口
 * 提供将图表导出为PDF或图片的功能
 */
public interface ExportService {
    
    /**
     * 将图表导出为PDF
     * 
     * @param chartData 图表数据
     * @param title 标题
     * @param description 描述
     * @return PDF字节数组
     */
    byte[] exportChartToPdf(Map<String, Object> chartData, String title, String description);
    
    /**
     * 将图表导出为图片
     * 
     * @param chartData 图表数据
     * @param title 标题
     * @return 图片字节数组
     */
    byte[] exportChartToImage(Map<String, Object> chartData, String title);
    
    /**
     * 导出小说分析报告
     * 
     * @param novelId 小说ID
     * @return PDF字节数组
     */
    byte[] exportNovelReport(Long novelId);
} 
 