-- 数据库schema创建
CREATE DATABASE IF NOT EXISTS novel_assistant CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE novel_assistant;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nickname VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL
);

-- 角色表
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 小说表
CREATE TABLE IF NOT EXISTS novels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_name VARCHAR(100),
    source_url VARCHAR(255),
    description TEXT,
    user_id BIGINT,
    processing_status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    processed_chapters INT DEFAULT 0,
    total_chapters INT DEFAULT 0,
    overall_summary TEXT,
    world_building_summary TEXT,
    character_development_summary TEXT,
    plot_progression_summary TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 章节表
CREATE TABLE IF NOT EXISTS chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    chapter_number INT,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT,
    summary TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE
);

-- 章节关键词表
CREATE TABLE IF NOT EXISTS chapter_keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE
);

-- 系统日志表
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level VARCHAR(10) NOT NULL,
    logger_name VARCHAR(255),
    message TEXT NOT NULL,
    user_id VARCHAR(100),
    ip_address VARCHAR(50),
    thread_name VARCHAR(100),
    stack_trace TEXT,
    params TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created_at (created_at),
    INDEX idx_level (level),
    INDEX idx_user_id (user_id)
); 