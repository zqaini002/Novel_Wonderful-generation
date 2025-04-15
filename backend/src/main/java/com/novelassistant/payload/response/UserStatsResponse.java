package com.novelassistant.payload.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 用户统计信息响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsResponse {
    
    // 用户上传的小说数量
    private Integer novelCount;
    
    // 用户总阅读数
    private Integer totalViews;
    
    // 用户收藏数量
    private Integer favoriteCount;
} 