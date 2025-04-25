USE novel_assistant;

-- 用户详情视图（包含角色信息）
CREATE OR REPLACE VIEW user_details AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.nickname,
    u.enabled,
    u.created_at,
    u.updated_at,
    u.last_login_at,
    GROUP_CONCAT(r.name) AS roles
FROM 
    users u
LEFT JOIN 
    user_roles ur ON u.id = ur.user_id
LEFT JOIN 
    roles r ON ur.role_id = r.id
GROUP BY 
    u.id;

-- 小说详情视图（包含基本统计信息）
CREATE OR REPLACE VIEW novel_details AS
SELECT 
    n.id,
    n.title,
    n.author_name,
    n.description,
    n.processing_status,
    n.processed_chapters,
    n.total_chapters,
    u.username AS uploaded_by,
    n.created_at,
    n.updated_at,
    (SELECT COUNT(*) FROM chapters c WHERE c.novel_id = n.id) AS chapter_count,
    (SELECT COUNT(*) FROM tags t WHERE t.novel_id = n.id) AS tag_count
FROM 
    novels n
LEFT JOIN 
    users u ON n.user_id = u.id;

-- 小说标签统计视图
CREATE OR REPLACE VIEW novel_tag_stats AS
SELECT 
    n.id AS novel_id,
    n.title,
    COUNT(CASE WHEN t.type = 'POSITIVE' THEN 1 END) AS positive_tag_count,
    COUNT(CASE WHEN t.type = 'WARNING' THEN 1 END) AS warning_tag_count,
    COUNT(CASE WHEN t.type = 'INFO' THEN 1 END) AS info_tag_count,
    GROUP_CONCAT(DISTINCT CASE WHEN t.type = 'POSITIVE' THEN t.name END) AS positive_tags,
    GROUP_CONCAT(DISTINCT CASE WHEN t.type = 'WARNING' THEN t.name END) AS warning_tags,
    GROUP_CONCAT(DISTINCT CASE WHEN t.type = 'INFO' THEN t.name END) AS info_tags
FROM 
    novels n
LEFT JOIN 
    tags t ON n.id = t.novel_id
GROUP BY 
    n.id;

-- 用户上传统计视图
CREATE OR REPLACE VIEW user_upload_stats AS
SELECT 
    u.id AS user_id,
    u.username,
    COUNT(n.id) AS total_novels,
    COUNT(CASE WHEN n.processing_status = 'COMPLETED' THEN 1 END) AS completed_novels,
    COUNT(CASE WHEN n.processing_status = 'PROCESSING' THEN 1 END) AS processing_novels,
    COUNT(CASE WHEN n.processing_status = 'FAILED' THEN 1 END) AS failed_novels,
    MAX(n.created_at) AS last_upload_date
FROM 
    users u
LEFT JOIN 
    novels n ON u.id = n.user_id
GROUP BY 
    u.id;

-- 系统统计视图（仪表盘数据源）
CREATE OR REPLACE VIEW system_stats AS
SELECT 
    (SELECT COUNT(*) FROM users) AS total_users,
    (SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)) AS new_users_7days,
    (SELECT COUNT(*) FROM novels) AS total_novels,
    (SELECT COUNT(*) FROM novels WHERE processing_status = 'COMPLETED') AS completed_novels,
    (SELECT COUNT(*) FROM novels WHERE processing_status = 'PROCESSING') AS processing_novels,
    (SELECT COUNT(*) FROM novels WHERE processing_status = 'FAILED') AS failed_novels,
    (SELECT COUNT(*) FROM chapters) AS total_chapters,
    (SELECT COUNT(*) FROM tags) AS total_tags,
    (SELECT COUNT(*) FROM system_logs WHERE level = 'ERROR' AND timestamp >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)) AS error_logs_7days;

-- 最近活动视图
CREATE OR REPLACE VIEW recent_activities AS
SELECT 
    'novel_upload' AS activity_type,
    n.id AS object_id,
    n.title AS object_name,
    u.username AS user_name,
    n.created_at AS activity_time
FROM 
    novels n
JOIN 
    users u ON n.user_id = u.id
UNION ALL
SELECT 
    'user_login' AS activity_type,
    u.id AS object_id,
    u.username AS object_name,
    u.username AS user_name,
    u.last_login_at AS activity_time
FROM 
    users u
WHERE 
    u.last_login_at IS NOT NULL
ORDER BY 
    activity_time DESC
LIMIT 100;

-- -----------------------------------------------------
-- 小说信息概览视图
-- -----------------------------------------------------
CREATE OR REPLACE VIEW `novel_overview` AS
SELECT 
    n.id,
    n.title,
    n.author_name,
    n.processing_status,
    n.processed_chapters,
    n.total_chapters,
    n.created_at,
    n.updated_at,
    n.is_deleted,
    u.username as owner_username,
    (SELECT COUNT(*) FROM chapters c WHERE c.novel_id = n.id) as chapter_count,
    (SELECT COUNT(*) FROM tags t WHERE t.novel_id = n.id) as tag_count,
    (SELECT COUNT(*) FROM novel_characters nc WHERE nc.novel_id = n.id) as character_count
FROM 
    novels n
LEFT JOIN 
    users u ON n.user_id = u.id
WHERE 
    n.is_deleted = FALSE;

-- -----------------------------------------------------
-- 章节详情视图
-- -----------------------------------------------------
CREATE OR REPLACE VIEW `chapter_details` AS
SELECT 
    c.id,
    c.novel_id,
    c.chapter_number,
    c.title,
    c.word_count,
    n.title as novel_title,
    n.author_name,
    c.created_at,
    c.updated_at,
    (SELECT COUNT(*) FROM character_dialogues cd WHERE cd.chapter_id = c.id) as dialogue_count
FROM 
    chapters c
JOIN 
    novels n ON c.novel_id = n.id
WHERE 
    n.is_deleted = FALSE
ORDER BY 
    c.novel_id, c.chapter_number;

-- -----------------------------------------------------
-- 角色详情视图
-- -----------------------------------------------------
CREATE OR REPLACE VIEW `character_details` AS
SELECT 
    nc.id,
    nc.novel_id,
    nc.name,
    nc.importance,
    nc.category,
    n.title as novel_title,
    n.author_name,
    (SELECT COUNT(*) FROM character_dialogues cd WHERE cd.character_id = nc.id) as dialogue_count,
    (SELECT COUNT(*) FROM character_relationships cr WHERE cr.source_character_id = nc.id OR cr.target_character_id = nc.id) as relationship_count
FROM 
    novel_characters nc
JOIN 
    novels n ON nc.novel_id = n.id
WHERE 
    n.is_deleted = FALSE
ORDER BY 
    nc.novel_id, nc.importance DESC;

-- -----------------------------------------------------
-- 标签统计视图
-- -----------------------------------------------------
CREATE OR REPLACE VIEW `tag_statistics` AS
SELECT 
    t.name,
    t.type,
    COUNT(*) as usage_count,
    COUNT(DISTINCT t.novel_id) as novel_count
FROM 
    tags t
JOIN 
    novels n ON t.novel_id = n.id
WHERE 
    n.is_deleted = FALSE
GROUP BY 
    t.name, t.type
ORDER BY 
    usage_count DESC; 