USE novel_assistant;

-- 用户表索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_last_login_at ON users(last_login_at);

-- 小说表索引
CREATE INDEX idx_novels_title ON novels(title);
CREATE INDEX idx_novels_author ON novels(author_name);
CREATE INDEX idx_novels_user_id ON novels(user_id);
CREATE INDEX idx_novels_status ON novels(processing_status);
CREATE INDEX idx_novels_created_at ON novels(created_at);

-- 章节表索引
CREATE INDEX idx_chapters_novel_id ON chapters(novel_id);
CREATE INDEX idx_chapters_number ON chapters(chapter_number);
CREATE INDEX idx_chapters_title ON chapters(title);

-- 章节关键词索引
CREATE INDEX idx_chapter_keywords_chapter_id ON chapter_keywords(chapter_id);
CREATE INDEX idx_chapter_keywords_keyword ON chapter_keywords(keyword);

-- 标签表索引
CREATE INDEX idx_tags_novel_id ON tags(novel_id);
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_tags_type ON tags(type);

-- 系统日志索引
CREATE INDEX idx_system_logs_timestamp ON system_logs(timestamp);
CREATE INDEX idx_system_logs_level ON system_logs(level);
CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);

-- 复合索引（针对常见查询优化）
CREATE INDEX idx_novels_status_created ON novels(processing_status, created_at);
CREATE INDEX idx_users_enabled_created ON users(enabled, created_at);
CREATE INDEX idx_tags_novel_type ON tags(novel_id, type);
CREATE INDEX idx_chapters_novel_num ON chapters(novel_id, chapter_number);
CREATE INDEX idx_system_logs_level_timestamp ON system_logs(level, timestamp); 