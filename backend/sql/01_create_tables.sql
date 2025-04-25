-- -----------------------------------------------------
-- 01_create_tables.sql - 表结构定义
-- -----------------------------------------------------

-- -----------------------------------------------------
-- 用户相关表
-- -----------------------------------------------------

-- 角色表
CREATE TABLE IF NOT EXISTS `roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_roles_name` (`name` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `nickname` VARCHAR(50),
  `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_login_at` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_users_username` (`username` ASC),
  UNIQUE INDEX `UK_users_email` (`email` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  INDEX `fk_user_roles_role_id` (`role_id` ASC),
  CONSTRAINT `fk_user_roles_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_user_roles_role_id`
    FOREIGN KEY (`role_id`)
    REFERENCES `roles` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 小说相关表
-- -----------------------------------------------------

-- 小说表
CREATE TABLE IF NOT EXISTS `novels` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `author_name` VARCHAR(100),
  `source_url` VARCHAR(512),
  `description` VARCHAR(1000),
  `user_id` BIGINT,
  `processing_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `processed_chapters` INT NOT NULL DEFAULT 0,
  `total_chapters` INT NOT NULL DEFAULT 0,
  `overall_summary` LONGTEXT,
  `world_building_summary` LONGTEXT,
  `character_development_summary` LONGTEXT,
  `plot_progression_summary` LONGTEXT,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`),
  INDEX `idx_novels_user_id` (`user_id` ASC),
  INDEX `idx_novels_title` (`title` ASC),
  INDEX `idx_novels_created_at` (`created_at` DESC),
  CONSTRAINT `fk_novels_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 章节表
CREATE TABLE IF NOT EXISTS `chapters` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `chapter_number` INT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` LONGTEXT,
  `word_count` INT,
  `summary` MEDIUMTEXT,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_chapters_novel_id` (`novel_id` ASC),
  INDEX `idx_chapters_number` (`chapter_number` ASC),
  CONSTRAINT `fk_chapters_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 标签表
CREATE TABLE IF NOT EXISTS `tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_tags_novel_id` (`novel_id` ASC),
  INDEX `idx_tags_type` (`type` ASC),
  CONSTRAINT `fk_tags_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 小说角色表
CREATE TABLE IF NOT EXISTS `novel_characters` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `importance` INT DEFAULT 50,
  `category` VARCHAR(50),
  `description` LONGTEXT,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_novel_characters_novel_id` (`novel_id` ASC),
  CONSTRAINT `fk_novel_characters_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色关系表
CREATE TABLE IF NOT EXISTS `character_relationships` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `source_character_id` BIGINT NOT NULL,
  `target_character_id` BIGINT NOT NULL,
  `relationship_type` VARCHAR(50) NOT NULL,
  `description` TEXT,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_character_relationships_novel_id` (`novel_id` ASC),
  INDEX `idx_character_relationships_source` (`source_character_id` ASC),
  INDEX `idx_character_relationships_target` (`target_character_id` ASC),
  CONSTRAINT `fk_character_relationships_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_character_relationships_source`
    FOREIGN KEY (`source_character_id`)
    REFERENCES `novel_characters` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_character_relationships_target`
    FOREIGN KEY (`target_character_id`)
    REFERENCES `novel_characters` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色对话表
CREATE TABLE IF NOT EXISTS `character_dialogues` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `chapter_id` BIGINT NOT NULL,
  `character_id` BIGINT,
  `character_name` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `position` INT NOT NULL,
  `sentiment` FLOAT,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_character_dialogues_novel_id` (`novel_id` ASC),
  INDEX `idx_character_dialogues_chapter_id` (`chapter_id` ASC),
  INDEX `idx_character_dialogues_character_id` (`character_id` ASC),
  CONSTRAINT `fk_character_dialogues_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_character_dialogues_chapter_id`
    FOREIGN KEY (`chapter_id`)
    REFERENCES `chapters` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_character_dialogues_character_id`
    FOREIGN KEY (`character_id`)
    REFERENCES `novel_characters` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 可视化相关表
-- -----------------------------------------------------

-- 可视化缓存表
CREATE TABLE IF NOT EXISTS `visualization_cache` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `visualization_type` VARCHAR(50) NOT NULL,
  `data_json` JSON NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `expires_at` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_visualization_cache_novel_id` (`novel_id` ASC),
  INDEX `idx_visualization_cache_type` (`visualization_type` ASC),
  INDEX `idx_visualization_cache_expires` (`expires_at` ASC),
  CONSTRAINT `fk_visualization_cache_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 情感数据表
CREATE TABLE IF NOT EXISTS `emotional_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `chapter_id` BIGINT NOT NULL,
  `chapter_number` INT NOT NULL,
  `positive_score` FLOAT NOT NULL,
  `negative_score` FLOAT NOT NULL,
  `neutral_score` FLOAT NOT NULL,
  `dominant_emotion` VARCHAR(50),
  `data_json` JSON,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_emotional_data_chapter` (`novel_id` ASC, `chapter_id` ASC),
  INDEX `idx_emotional_data_novel_id` (`novel_id` ASC),
  CONSTRAINT `fk_emotional_data_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_emotional_data_chapter_id`
    FOREIGN KEY (`chapter_id`)
    REFERENCES `chapters` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 结构数据表
CREATE TABLE IF NOT EXISTS `structure_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `chapter_id` BIGINT NOT NULL,
  `chapter_number` INT NOT NULL,
  `narrative_percentage` FLOAT,
  `dialogue_percentage` FLOAT,
  `description_percentage` FLOAT,
  `action_percentage` FLOAT,
  `data_json` JSON,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_structure_data_chapter` (`novel_id` ASC, `chapter_id` ASC),
  INDEX `idx_structure_data_novel_id` (`novel_id` ASC),
  CONSTRAINT `fk_structure_data_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_structure_data_chapter_id`
    FOREIGN KEY (`chapter_id`)
    REFERENCES `chapters` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 字数统计数据表
CREATE TABLE IF NOT EXISTS `word_count_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `chapter_id` BIGINT NOT NULL,
  `chapter_number` INT NOT NULL,
  `word_count` INT NOT NULL,
  `paragraph_count` INT,
  `sentence_count` INT,
  `dialogue_count` INT,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_word_count_data_chapter` (`novel_id` ASC, `chapter_id` ASC),
  INDEX `idx_word_count_data_novel_id` (`novel_id` ASC),
  CONSTRAINT `fk_word_count_data_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_word_count_data_chapter_id`
    FOREIGN KEY (`chapter_id`)
    REFERENCES `chapters` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 可视化关键词表
CREATE TABLE IF NOT EXISTS `visualization_keywords` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `novel_id` BIGINT NOT NULL,
  `category` VARCHAR(50) NOT NULL,
  `keyword` VARCHAR(100) NOT NULL,
  `weight` FLOAT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_visualization_keywords_novel_id` (`novel_id` ASC),
  INDEX `idx_visualization_keywords_category` (`category` ASC),
  CONSTRAINT `fk_visualization_keywords_novel_id`
    FOREIGN KEY (`novel_id`)
    REFERENCES `novels` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 系统相关表
-- -----------------------------------------------------

-- 系统日志表
CREATE TABLE IF NOT EXISTS `system_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `log_type` VARCHAR(20) NOT NULL,
  `user_id` BIGINT,
  `action` VARCHAR(255) NOT NULL,
  `ip_address` VARCHAR(50),
  `user_agent` VARCHAR(255),
  `details` TEXT,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_system_logs_log_type` (`log_type` ASC),
  INDEX `idx_system_logs_user_id` (`user_id` ASC),
  INDEX `idx_system_logs_created_at` (`created_at` DESC),
  CONSTRAINT `fk_system_logs_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 