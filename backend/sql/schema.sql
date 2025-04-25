-- -----------------------------------------------------
-- schema.sql - 完整数据库架构创建脚本
-- -----------------------------------------------------

-- 删除数据库（如果存在）并重新创建
DROP DATABASE IF EXISTS novel_assistant;
CREATE DATABASE novel_assistant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE novel_assistant;

-- 设置SQL模式以兼容MySQL 8
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+08:00";

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