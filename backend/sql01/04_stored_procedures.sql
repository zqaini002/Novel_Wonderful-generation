USE novel_assistant;

-- 更新小说处理状态的存储过程
DELIMITER //
CREATE PROCEDURE update_novel_processing_status(
    IN novel_id BIGINT,
    IN new_status VARCHAR(20),
    IN processed INT
)
BEGIN
    DECLARE total INT;
    
    -- 获取小说总章节数
    SELECT total_chapters INTO total FROM novels WHERE id = novel_id;
    
    -- 更新处理状态和已处理章节数
    UPDATE novels
    SET 
        processing_status = new_status,
        processed_chapters = processed,
        updated_at = NOW()
    WHERE 
        id = novel_id;
        
    -- 如果已处理章节数达到总章节数，自动设置为完成状态
    IF processed >= total AND new_status != 'FAILED' THEN
        UPDATE novels
        SET processing_status = 'COMPLETED'
        WHERE id = novel_id;
    END IF;
END //
DELIMITER ;

-- 用户注册存储过程（包含角色分配）
DELIMITER //
CREATE PROCEDURE register_user(
    IN p_username VARCHAR(50),
    IN p_email VARCHAR(100),
    IN p_password VARCHAR(255),
    IN p_nickname VARCHAR(100),
    OUT p_user_id BIGINT
)
BEGIN
    DECLARE user_role_id BIGINT;
    
    -- 获取默认用户角色ID
    SELECT id INTO user_role_id FROM roles WHERE name = 'ROLE_USER';
    
    -- 插入用户记录
    INSERT INTO users (username, email, password, nickname, enabled, created_at, updated_at)
    VALUES (p_username, p_email, p_password, p_nickname, TRUE, NOW(), NOW());
    
    -- 获取新插入用户的ID
    SET p_user_id = LAST_INSERT_ID();
    
    -- 分配用户角色
    INSERT INTO user_roles (user_id, role_id)
    VALUES (p_user_id, user_role_id);
END //
DELIMITER ;

-- 用户登录更新存储过程
DELIMITER //
CREATE PROCEDURE update_user_login(
    IN p_username VARCHAR(50)
)
BEGIN
    UPDATE users 
    SET 
        last_login_at = NOW(),
        updated_at = NOW()
    WHERE 
        username = p_username;
END //
DELIMITER ;

-- 清理系统过期日志存储过程
DELIMITER //
CREATE PROCEDURE clean_old_logs(
    IN days_to_keep INT
)
BEGIN
    DELETE FROM system_logs
    WHERE timestamp < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    SELECT CONCAT('已删除 ', ROW_COUNT(), ' 条过期日志') AS result;
END //
DELIMITER ;

-- 系统统计信息函数
DELIMITER //
CREATE FUNCTION get_dashboard_stats() RETURNS JSON
BEGIN
    DECLARE stats JSON;
    
    SET stats = (
        SELECT JSON_OBJECT(
            'totalUsers', (SELECT COUNT(*) FROM users),
            'newUsers', (SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)),
            'totalNovels', (SELECT COUNT(*) FROM novels),
            'processingNovels', (SELECT COUNT(*) FROM novels WHERE processing_status = 'PROCESSING'),
            'completedNovels', (SELECT COUNT(*) FROM novels WHERE processing_status = 'COMPLETED'),
            'errorLogs', (SELECT COUNT(*) FROM system_logs WHERE level = 'ERROR' AND timestamp >= DATE_SUB(NOW(), INTERVAL 7 DAY))
        )
    );
    
    RETURN stats;
END //
DELIMITER ;

-- 添加标签存储过程
DELIMITER //
CREATE PROCEDURE add_novel_tag(
    IN p_novel_id BIGINT,
    IN p_tag_name VARCHAR(100),
    IN p_tag_type VARCHAR(20)
)
BEGIN
    DECLARE tag_exists INT;
    
    -- 检查标签是否已存在
    SELECT COUNT(*) INTO tag_exists 
    FROM tags 
    WHERE novel_id = p_novel_id AND name = p_tag_name;
    
    -- 如果标签不存在，则添加
    IF tag_exists = 0 THEN
        INSERT INTO tags (novel_id, name, type, created_at, updated_at)
        VALUES (p_novel_id, p_tag_name, p_tag_type, NOW(), NOW());
    END IF;
END //
DELIMITER ;

-- 分析用户活跃度存储过程
DELIMITER //
CREATE PROCEDURE analyze_user_activity(
    IN days_back INT
)
BEGIN
    SELECT 
        u.id,
        u.username,
        COUNT(DISTINCT n.id) AS novel_uploads,
        (
            CASE 
                WHEN u.last_login_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 'active'
                WHEN u.last_login_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 'semi-active'
                ELSE 'inactive'
            END
        ) AS activity_status,
        DATEDIFF(NOW(), u.last_login_at) AS days_since_last_login
    FROM 
        users u
    LEFT JOIN 
        novels n ON u.id = n.user_id AND n.created_at >= DATE_SUB(NOW(), INTERVAL days_back DAY)
    GROUP BY 
        u.id, u.username
    ORDER BY 
        novel_uploads DESC, days_since_last_login ASC;
END //
DELIMITER ;

-- 创建触发器更新小说统计信息
DELIMITER //
CREATE TRIGGER after_chapter_insert
AFTER INSERT ON chapters
FOR EACH ROW
BEGIN
    UPDATE novels
    SET 
        total_chapters = (SELECT COUNT(*) FROM chapters WHERE novel_id = NEW.novel_id),
        updated_at = NOW()
    WHERE 
        id = NEW.novel_id;
END //
DELIMITER ; 