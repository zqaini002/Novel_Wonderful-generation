package com.novelassistant.controller;

import com.novelassistant.service.VisualizationService;
import com.novelassistant.util.LogUtil;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小说可视化控制器
 * 提供各类小说数据可视化API
 */
@RestController
@RequestMapping({"/novels/visualization", "/api/novels/visualization"})
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class VisualizationController {

    private static final Logger logger = LogUtil.getLogger(VisualizationController.class);
    
    @Autowired
    private VisualizationService visualizationService;
    
    /**
     * 获取小说关键词云数据
     */
    @GetMapping("/{novelId}/keywords")
    public ResponseEntity<?> getKeywordCloudData(@PathVariable Long novelId) {
        if (novelId == null) {
            logger.error("获取小说关键词云数据失败: 小说ID不能为空");
            Map<String, String> error = new HashMap<>();
            error.put("error", "小说ID不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        logger.info("接收到获取小说关键词云数据请求, 小说ID: {}", novelId);
        try {
            List<Map<String, Object>> result = visualizationService.getKeywordCloudData(novelId);
            logger.info("获取小说关键词云数据成功, 小说ID: {}", novelId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取小说关键词云数据失败, 小说ID: {}, 错误信息: {}", novelId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取小说情节波动图数据
     */
    @GetMapping("/{novelId}/emotional")
    public ResponseEntity<?> getEmotionalFluctuationData(@PathVariable Long novelId) {
        if (novelId == null) {
            logger.error("获取小说情节波动图数据失败: 小说ID不能为空");
            Map<String, String> error = new HashMap<>();
            error.put("error", "小说ID不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        logger.info("接收到获取小说情节波动图数据请求, 小说ID: {}", novelId);
        try {
            Map<String, Object> result = visualizationService.getEmotionalFluctuationData(novelId);
            logger.info("获取小说情节波动图数据成功, 小说ID: {}", novelId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取小说情节波动图数据失败, 小说ID: {}, 错误信息: {}", novelId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取小说结构分析数据
     */
    @GetMapping("/{novelId}/structure")
    public ResponseEntity<?> getStructureAnalysisData(@PathVariable Long novelId) {
        if (novelId == null) {
            logger.error("获取小说结构分析数据失败: 小说ID不能为空");
            Map<String, String> error = new HashMap<>();
            error.put("error", "小说ID不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        logger.info("接收到获取小说结构分析数据请求, 小说ID: {}", novelId);
        try {
            Map<String, Object> result = visualizationService.getStructureAnalysisData(novelId);
            logger.info("获取小说结构分析数据成功, 小说ID: {}", novelId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取小说结构分析数据失败, 小说ID: {}, 错误信息: {}", novelId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取小说人物关系网络数据
     */
    @GetMapping("/{novelId}/characters")
    public ResponseEntity<?> getCharacterRelationshipData(@PathVariable Long novelId) {
        if (novelId == null) {
            logger.error("获取小说人物关系网络数据失败: 小说ID不能为空");
            Map<String, String> error = new HashMap<>();
            error.put("error", "小说ID不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        logger.info("接收到获取小说人物关系网络数据请求, 小说ID: {}", novelId);
        try {
            Map<String, Object> result = visualizationService.getCharacterRelationshipData(novelId);
            logger.info("获取小说人物关系网络数据成功, 小说ID: {}", novelId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取小说人物关系网络数据失败, 小说ID: {}, 错误信息: {}", novelId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取小说所有可视化数据（整合所有数据）
     */
    @GetMapping("/{novelId}/all")
    public ResponseEntity<?> getAllVisualizationData(@PathVariable Long novelId) {
        if (novelId == null) {
            logger.error("获取小说所有可视化数据失败: 小说ID不能为空");
            Map<String, String> error = new HashMap<>();
            error.put("error", "小说ID不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        logger.info("接收到获取小说所有可视化数据请求, 小说ID: {}", novelId);
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 收集所有可视化数据
            // 前端期望的变量名为keywords, emotional, structure
            result.put("keywords", visualizationService.getKeywordCloudData(novelId));
            
            // 获取情节波动图数据，从emotional字段中提取
            Map<String, Object> emotionalData = visualizationService.getEmotionalFluctuationData(novelId);
            if (emotionalData != null && emotionalData.containsKey("emotional")) {
                result.put("emotional", emotionalData.get("emotional"));
            } else {
                result.put("emotional", new HashMap<>());
            }
            
            // 获取结构分析数据
            result.put("structure", visualizationService.getStructureAnalysisData(novelId));
            
            // 获取人物关系网络数据
            result.put("characters", visualizationService.getCharacterRelationshipData(novelId));
            
            logger.info("获取小说所有可视化数据成功, 小说ID: {}", novelId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取小说所有可视化数据失败, 小说ID: {}, 错误信息: {}", novelId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 