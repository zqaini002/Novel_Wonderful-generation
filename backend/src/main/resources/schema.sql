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