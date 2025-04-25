package com.novelassistant.service.impl;

import com.novelassistant.entity.Novel;
import com.novelassistant.entity.Chapter;
import com.novelassistant.entity.NovelCharacter;
import com.novelassistant.entity.visualization.EmotionalData;
import com.novelassistant.repository.NovelRepository;
import com.novelassistant.repository.ChapterRepository;
import com.novelassistant.repository.NovelCharacterRepository;
import com.novelassistant.repository.visualization.EmotionalDataRepository;
import com.novelassistant.service.ExportService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;

import com.novelassistant.exception.BadRequestException;
import com.novelassistant.exception.ResourceNotFoundException;

/**
 * 导出服务实现类
 * 提供图表和报告的导出功能
 */
@Service
public class ExportServiceImpl implements ExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportServiceImpl.class);
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private NovelCharacterRepository characterRepository;
    
    @Autowired
    private EmotionalDataRepository emotionalDataRepository;
    
    // 定义字体
    private static Font TITLE_FONT;
    private static Font HEADING_FONT;
    private static Font NORMAL_FONT;
    
    // 静态初始化块，设置中文字体
    static {
        try {
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            TITLE_FONT = new Font(baseFont, 18, Font.BOLD);
            HEADING_FONT = new Font(baseFont, 14, Font.BOLD);
            NORMAL_FONT = new Font(baseFont, 12, Font.NORMAL);
        } catch (Exception e) {
            // 使用默认字体
            TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
            HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
            NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
        }
    }
    
    @Override
    public byte[] exportChartToPdf(Map<String, Object> chartData, String title, String description) {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            // 生成图表图像
            JFreeChart chart = createChartFromData(chartData, title);
            BufferedImage chartImage = chart.createBufferedImage(500, 400);
            
            // 将图表转为PDF格式
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // 添加标题
            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(title);
            contentStream.endText();
            
            // 添加描述
            if (description != null && !description.isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 12);
                contentStream.newLineAtOffset(50, 730);
                contentStream.showText(description);
                contentStream.endText();
            }
            
            // 添加图表
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, chartImage);
            contentStream.drawImage(pdImage, 50, 300, 500, 400);
            
            // 添加页脚
            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 10);
            contentStream.newLineAtOffset(50, 50);
            contentStream.showText("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            contentStream.endText();
            
            contentStream.close();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export chart to PDF", e);
        }
    }
    
    @Override
    public byte[] exportChartToImage(Map<String, Object> chartData, String title) {
        try {
            // 生成图表图像
            JFreeChart chart = createChartFromData(chartData, title);
            BufferedImage chartImage = chart.createBufferedImage(800, 600);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export chart to image", e);
        }
    }
    
    @Override
    public byte[] exportNovelReport(Long novelId) {
        try {
            Novel novel = novelRepository.findById(novelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Novel not found with id: " + novelId));
            
            List<NovelCharacter> characters = characterRepository.findByNovelId(novelId);
            
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // 添加标题
            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Novel Analysis Report: " + novel.getTitle());
            contentStream.endText();
            
            // 添加小说基本信息
            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 12);
            contentStream.newLineAtOffset(50, 720);
            contentStream.showText("Author: " + novel.getAuthor());
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 12);
            contentStream.newLineAtOffset(50, 700);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            contentStream.showText("Published: " + (novel.getCreatedAt() != null ? 
                dateFormat.format(novel.getCreatedAt()) : "Unknown"));
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 12);
            contentStream.newLineAtOffset(50, 680);
            contentStream.showText("Total Chapters: " + 
                chapterRepository.countByNovelId(novel.getId()));
            contentStream.endText();
            
            // 添加角色信息标题
            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 14);
            contentStream.newLineAtOffset(50, 650);
            contentStream.showText("Character Analysis");
            contentStream.endText();
            
            // 添加角色列表
            int yPosition = 630;
            for (NovelCharacter character : characters) {
                if (yPosition < 100) {
                    // 创建新页面继续写入
                    contentStream.close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    contentStream = new PDPageContentStream(document, newPage);
                    yPosition = 750;
                }
                
                contentStream.beginText();
                contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 12);
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(character.getName() + " (" + character.getCategory() + ")");
                contentStream.endText();
                yPosition -= 20;
                
                if (character.getDescription() != null && !character.getDescription().isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 10);
                    contentStream.newLineAtOffset(70, yPosition);
                    // 限制描述长度以避免溢出页面
                    String description = character.getDescription();
                    if (description.length() > 100) {
                        description = description.substring(0, 97) + "...";
                    }
                    contentStream.showText(description);
                    contentStream.endText();
                    yPosition -= 20;
                }
                
                contentStream.beginText();
                contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 10);
                contentStream.newLineAtOffset(70, yPosition);
                contentStream.showText("Importance: " + character.getImportance());
                contentStream.endText();
                yPosition -= 30;
            }
            
            // 添加页脚
            contentStream.beginText();
            contentStream.setFont(PDType0Font.load(document, ExportServiceImpl.class.getResourceAsStream("/fonts/SimSun.ttf")), 10);
            contentStream.newLineAtOffset(50, 50);
            contentStream.showText("Report generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            contentStream.endText();
            
            contentStream.close();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();
            
            return baos.toByteArray();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export novel report", e);
        }
    }
    
    /**
     * 根据图表数据创建JFreeChart图表
     * 
     * @param chartData 图表数据
     * @param title 图表标题
     * @return JFreeChart对象
     */
    private JFreeChart createChartFromData(Map<String, Object> chartData, String title) {
        if (chartData == null) {
            throw new BadRequestException("Chart data cannot be null");
        }
        
        String chartType = (String) chartData.get("type");
        if (chartType == null) {
            throw new BadRequestException("Chart type must be specified");
        }
        
        switch (chartType.toLowerCase()) {
            case "pie":
                return createPieChart(chartData, title);
            case "bar":
                return createBarChart(chartData, title);
            case "line":
                return createLineChart(chartData, title);
            default:
                throw new BadRequestException("Unsupported chart type: " + chartType);
        }
    }
    
    /**
     * 创建饼图
     */
    @SuppressWarnings("unchecked")
    private JFreeChart createPieChart(Map<String, Object> chartData, String title) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        List<Map<String, Object>> data = (List<Map<String, Object>>) chartData.get("data");
        if (data == null || data.isEmpty()) {
            throw new BadRequestException("No data provided for pie chart");
        }
        
        for (Map<String, Object> item : data) {
            String label = (String) item.get("label");
            Number value = (Number) item.get("value");
            
            if (label == null || value == null) {
                continue;
            }
            
            dataset.setValue(label, value);
        }
        
        return ChartFactory.createPieChart(
                title,
                dataset,
                true,
                true,
                false
        );
    }
    
    /**
     * 创建柱状图
     */
    @SuppressWarnings("unchecked")
    private JFreeChart createBarChart(Map<String, Object> chartData, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        List<Map<String, Object>> data = (List<Map<String, Object>>) chartData.get("data");
        if (data == null || data.isEmpty()) {
            throw new BadRequestException("No data provided for bar chart");
        }
        
        String xAxisLabel = (String) chartData.getOrDefault("xAxisLabel", "Categories");
        String yAxisLabel = (String) chartData.getOrDefault("yAxisLabel", "Values");
        
        for (Map<String, Object> item : data) {
            String category = (String) item.get("category");
            String series = (String) item.getOrDefault("series", "Series 1");
            Number value = (Number) item.get("value");
            
            if (category == null || value == null) {
                continue;
            }
            
            dataset.addValue(value, series, category);
        }
        
        return ChartFactory.createBarChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
    
    /**
     * 创建折线图
     */
    @SuppressWarnings("unchecked")
    private JFreeChart createLineChart(Map<String, Object> chartData, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        List<Map<String, Object>> data = (List<Map<String, Object>>) chartData.get("data");
        if (data == null || data.isEmpty()) {
            throw new BadRequestException("No data provided for line chart");
        }
        
        String xAxisLabel = (String) chartData.getOrDefault("xAxisLabel", "X-Axis");
        String yAxisLabel = (String) chartData.getOrDefault("yAxisLabel", "Y-Axis");
        
        for (Map<String, Object> item : data) {
            String category = (String) item.get("category");
            String series = (String) item.getOrDefault("series", "Series 1");
            Number value = (Number) item.get("value");
            
            if (category == null || value == null) {
                continue;
            }
            
            dataset.addValue(value, series, category);
        }
        
        return ChartFactory.createLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
} 
 