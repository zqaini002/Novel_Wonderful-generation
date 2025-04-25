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
CREATE INDEX `idx_novels_processing_status` ON `novels` (`processing_status`);
CREATE INDEX `idx_novels_is_deleted_created` ON `novels` (`is_deleted`, `created_at` DESC);

-- 章节表索引
CREATE INDEX idx_chapters_novel_id ON chapters(novel_id);
CREATE INDEX idx_chapters_number ON chapters(chapter_number);
CREATE INDEX idx_chapters_title ON chapters(title);
CREATE INDEX `idx_chapters_title` ON `chapters` (`title`(191));
CREATE INDEX `idx_chapters_novel_chapter` ON `chapters` (`novel_id`, `chapter_number`);

-- 章节关键词索引
CREATE INDEX idx_chapter_keywords_chapter_id ON chapter_keywords(chapter_id);
CREATE INDEX idx_chapter_keywords_keyword ON chapter_keywords(keyword);

-- 标签表索引
CREATE INDEX idx_tags_novel_id ON tags(novel_id);
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_tags_type ON tags(type);
CREATE INDEX `idx_tags_name` ON `tags` (`name`);
CREATE INDEX `idx_tags_novel_type` ON `tags` (`novel_id`, `type`);

-- 系统日志索引
CREATE INDEX idx_system_logs_timestamp ON system_logs(timestamp);
CREATE INDEX idx_system_logs_level ON system_logs(level);
CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);
CREATE INDEX `idx_system_logs_action` ON `system_logs` (`action`);
CREATE INDEX `idx_system_logs_type_user` ON `system_logs` (`log_type`, `user_id`);

-- 复合索引（针对常见查询优化）
CREATE INDEX idx_novels_status_created ON novels(processing_status, created_at);
CREATE INDEX idx_users_enabled_created ON users(enabled, created_at);
CREATE INDEX idx_tags_novel_type ON tags(novel_id, type);
CREATE INDEX idx_chapters_novel_num ON chapters(novel_id, chapter_number);
CREATE INDEX idx_system_logs_level_timestamp ON system_logs(level, timestamp);

-- 角色表索引
CREATE INDEX `idx_novel_characters_name` ON `novel_characters` (`name`);
CREATE INDEX `idx_novel_characters_novel_importance` ON `novel_characters` (`novel_id`, `importance` DESC);

-- 对话表索引
CREATE INDEX `idx_character_dialogues_character_name` ON `character_dialogues` (`character_name`);
CREATE INDEX `idx_character_dialogues_novel_chapter` ON `character_dialogues` (`novel_id`, `chapter_id`);
CREATE INDEX `idx_character_dialogues_sentiment` ON `character_dialogues` (`sentiment`);

-- 可视化缓存表索引
CREATE INDEX `idx_visualization_cache_type_novel` ON `visualization_cache` (`visualization_type`, `novel_id`);

-- 情感数据表索引
CREATE INDEX `idx_emotional_data_dominant` ON `emotional_data` (`dominant_emotion`);
CREATE INDEX `idx_emotional_data_novel_chapter` ON `emotional_data` (`novel_id`, `chapter_number`);

-- 结构数据表索引
CREATE INDEX `idx_structure_data_novel_chapter` ON `structure_data` (`novel_id`, `chapter_number`);

-- 字数统计数据表索引
CREATE INDEX `idx_word_count_data_word_count` ON `word_count_data` (`word_count`);
CREATE INDEX `idx_word_count_data_novel_chapter` ON `word_count_data` (`novel_id`, `chapter_number`);

-- 可视化关键词表索引
CREATE INDEX `idx_visualization_keywords_category_weight` ON `visualization_keywords` (`category`, `weight` DESC);
CREATE INDEX `idx_visualization_keywords_keyword` ON `visualization_keywords` (`keyword`); 