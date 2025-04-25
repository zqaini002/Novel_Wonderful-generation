package com.novelassistant.controller;

import com.novelassistant.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 导出控制器
 * 处理图表导出为图片或PDF的请求
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {
    
    @Autowired
    private ExportService exportService;
    
    /**
     * 导出图表为PDF
     */
    @PostMapping("/chart/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportChartToPdf(@RequestBody Map<String, Object> requestData) {
        try {
            String title = (String) requestData.getOrDefault("title", "图表");
            String description = (String) requestData.getOrDefault("description", "");
            Map<String, Object> chartData = (Map<String, Object>) requestData.get("chartData");
            
            byte[] pdfBytes = exportService.exportChartToPdf(chartData, title, description);
            
            String encodedFilename = URLEncoder.encode(title + ".pdf", StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", encodedFilename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 导出图表为图片
     */
    @PostMapping("/chart/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportChartToImage(@RequestBody Map<String, Object> requestData) {
        try {
            String title = (String) requestData.getOrDefault("title", "图表");
            Map<String, Object> chartData = (Map<String, Object>) requestData.get("chartData");
            
            byte[] imageBytes = exportService.exportChartToImage(chartData, title);
            
            String encodedFilename = URLEncoder.encode(title + ".png", StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", encodedFilename);
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 导出小说分析报告
     */
    @GetMapping("/novel/{novelId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportNovelReport(@PathVariable Long novelId) {
        try {
            byte[] pdfBytes = exportService.exportNovelReport(novelId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "novel-report-" + novelId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 
 