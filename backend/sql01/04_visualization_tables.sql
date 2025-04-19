USE novel_assistant;

-- 关键词云数据表
CREATE TABLE IF NOT EXISTS visualization_keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    weight INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_keyword (keyword),
    UNIQUE KEY novel_keyword_idx (novel_id, keyword)
);

-- 情节波动图数据表
CREATE TABLE IF NOT EXISTS visualization_emotional_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    chapter_id BIGINT,
    chapter_number INT NOT NULL,
    chapter_title VARCHAR(255),
    emotion_value DOUBLE NOT NULL,
    is_important BOOLEAN DEFAULT FALSE,
    is_climax_start BOOLEAN DEFAULT FALSE,
    is_climax_end BOOLEAN DEFAULT FALSE,
    event_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_chapter_number (chapter_number)
);

-- 小说结构分析数据表
CREATE TABLE IF NOT EXISTS visualization_structure_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    section_name VARCHAR(100) NOT NULL,
    percentage DOUBLE NOT NULL,
    start_chapter INT NOT NULL,
    end_chapter INT NOT NULL,
    chapter_count INT NOT NULL,
    description TEXT,
    color VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id)
);

-- 章节字数分布数据表
CREATE TABLE IF NOT EXISTS visualization_word_count_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    range_start INT NOT NULL,
    range_end INT NOT NULL,
    chapter_count INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id)
);

-- 可视化缓存表（存储生成的可视化数据的缓存）
CREATE TABLE IF NOT EXISTS visualization_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    visualization_type VARCHAR(50) NOT NULL,
    data_json JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_type (visualization_type),
    UNIQUE KEY novel_vis_type_idx (novel_id, visualization_type)
); 