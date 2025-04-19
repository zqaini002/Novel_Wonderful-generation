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