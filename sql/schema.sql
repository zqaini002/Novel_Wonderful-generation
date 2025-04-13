-- 小说精读助手数据库表结构
-- 创建数据库
CREATE DATABASE IF NOT EXISTS novel_assistant CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE novel_assistant;

-- 小说表
CREATE TABLE IF NOT EXISTS novels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    author_name VARCHAR(100),
    source_url VARCHAR(255),
    total_chapters INT,
    processed_chapters INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    processing_status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    overall_summary TEXT,
    world_building_summary TEXT,
    character_development_summary TEXT,
    plot_progression_summary TEXT,
    INDEX(title),
    INDEX(processing_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 小说关键词表
CREATE TABLE IF NOT EXISTS novel_keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    keyword VARCHAR(50) NOT NULL,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    UNIQUE KEY novel_keyword_unique (novel_id, keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 章节表
CREATE TABLE IF NOT EXISTS chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    chapter_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    summary TEXT,
    word_count INT,
    is_processed BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    event_intensity FLOAT DEFAULT 0,
    chapter_type ENUM('MAIN_PLOT', 'SUB_PLOT', 'WORLD_BUILDING', 'CHARACTER_DEVELOPMENT', 'FILLER') DEFAULT 'MAIN_PLOT',
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    UNIQUE KEY novel_chapter_unique (novel_id, chapter_number),
    INDEX(novel_id, chapter_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 章节关键词表
CREATE TABLE IF NOT EXISTS chapter_keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    keyword VARCHAR(50) NOT NULL,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    UNIQUE KEY chapter_keyword_unique (chapter_id, keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 章节人物表
CREATE TABLE IF NOT EXISTS chapter_characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    character_name VARCHAR(50) NOT NULL,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    UNIQUE KEY chapter_character_unique (chapter_id, character_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 小说标签表
CREATE TABLE IF NOT EXISTS novel_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    tag_type ENUM('RECOMMEND', 'WARNING', 'THRESHOLD') NOT NULL,
    confidence_score FLOAT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX(novel_id, tag_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 分析结果表
CREATE TABLE IF NOT EXISTS analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    UNIQUE KEY novel_result_unique (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 风格特征表
CREATE TABLE IF NOT EXISTS style_features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    feature_name VARCHAR(50) NOT NULL,
    score FLOAT NOT NULL,
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE,
    UNIQUE KEY analysis_style_unique (analysis_id, feature_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 题材特征表
CREATE TABLE IF NOT EXISTS genre_features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    feature_name VARCHAR(50) NOT NULL,
    score FLOAT NOT NULL,
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE,
    UNIQUE KEY analysis_genre_unique (analysis_id, feature_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 潜在问题表
CREATE TABLE IF NOT EXISTS potential_issues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    issue_type VARCHAR(50) NOT NULL,
    chapter_number INT,
    chapter_title VARCHAR(255),
    keyword VARCHAR(50),
    suggestion TEXT,
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 章节建议表
CREATE TABLE IF NOT EXISTS chapter_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    suggestion_type VARCHAR(50) NOT NULL,
    reason TEXT,
    suggestion TEXT,
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 内容推荐表
CREATE TABLE IF NOT EXISTS content_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    recommendation_type VARCHAR(50) NOT NULL,
    recommendation TEXT,
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 