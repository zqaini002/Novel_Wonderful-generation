-- 创建小说角色表
CREATE TABLE IF NOT EXISTS novel_characters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    importance INTEGER DEFAULT 50,
    category VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE
);

-- 创建角色关系表
CREATE TABLE IF NOT EXISTS character_relationships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id BIGINT NOT NULL,
    source_character_id BIGINT NOT NULL,
    target_character_id BIGINT NOT NULL,
    relationship_type VARCHAR(50),
    importance INTEGER DEFAULT 1,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (source_character_id) REFERENCES novel_characters(id) ON DELETE CASCADE,
    FOREIGN KEY (target_character_id) REFERENCES novel_characters(id) ON DELETE CASCADE
);

-- 创建角色对话表
CREATE TABLE IF NOT EXISTS character_dialogues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id BIGINT NOT NULL,
    chapter_id BIGINT NOT NULL,
    character_id BIGINT NOT NULL,
    dialogue_text TEXT NOT NULL,
    dialogue_position INTEGER,
    sentiment FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES novel_characters(id) ON DELETE CASCADE
); 