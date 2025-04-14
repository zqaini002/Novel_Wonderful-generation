-- 认证系统示例数据
-- 注意：这些是示例数据，密码使用BCrypt加密
-- 默认密码都是'password'，加密后的值为'$2a$10$fJ3jTj4yh2Y3USYmSgQRy.mnHVD/9vXnJzuYdvqKNUd8LsihQVPUO'

-- 插入示例用户
INSERT INTO users (username, email, password, nickname, enabled, created_at) VALUES
('admin', 'admin@example.com', '$2a$10$fJ3jTj4yh2Y3USYmSgQRy.mnHVD/9vXnJzuYdvqKNUd8LsihQVPUO', '管理员', TRUE, NOW()),
('user1', 'user1@example.com', '$2a$10$fJ3jTj4yh2Y3USYmSgQRy.mnHVD/9vXnJzuYdvqKNUd8LsihQVPUO', '普通用户1', TRUE, NOW()),
('user2', 'user2@example.com', '$2a$10$fJ3jTj4yh2Y3USYmSgQRy.mnHVD/9vXnJzuYdvqKNUd8LsihQVPUO', '普通用户2', TRUE, NOW()),
('demo', 'demo@example.com', '$2a$10$fJ3jTj4yh2Y3USYmSgQRy.mnHVD/9vXnJzuYdvqKNUd8LsihQVPUO', '演示账号', TRUE, NOW());

-- 确保角色已经存在
INSERT INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 获取角色ID
SET @admin_role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');
SET @user_role_id = (SELECT id FROM roles WHERE name = 'ROLE_USER');

-- 获取用户ID
SET @admin_id = (SELECT id FROM users WHERE username = 'admin');
SET @user1_id = (SELECT id FROM users WHERE username = 'user1');
SET @user2_id = (SELECT id FROM users WHERE username = 'user2');
SET @demo_id = (SELECT id FROM users WHERE username = 'demo');

-- 分配角色给用户
INSERT INTO user_roles (user_id, role_id) VALUES
(@admin_id, @admin_role_id),
(@admin_id, @user_role_id),  -- 管理员同时拥有USER和ADMIN角色
(@user1_id, @user_role_id),
(@user2_id, @user_role_id),
(@demo_id, @user_role_id);

-- 添加用户设置
INSERT INTO user_settings (user_id, notification_enabled, theme) VALUES
(@admin_id, TRUE, 'dark'),
(@user1_id, TRUE, 'light'),
(@user2_id, FALSE, 'light'),
(@demo_id, TRUE, 'light');

-- 添加示例小说权限
-- 假设已有小说ID 1, 2, 3
INSERT INTO novel_permissions (user_id, novel_id, permission_type) VALUES
(@admin_id, 1, 'READ'),
(@admin_id, 1, 'EDIT'),
(@admin_id, 1, 'DELETE'),
(@admin_id, 1, 'SHARE'),
(@admin_id, 2, 'READ'),
(@admin_id, 2, 'EDIT'),
(@admin_id, 2, 'DELETE'),
(@admin_id, 2, 'SHARE'),
(@admin_id, 3, 'READ'),
(@admin_id, 3, 'EDIT'),
(@admin_id, 3, 'DELETE'),
(@admin_id, 3, 'SHARE'),
(@user1_id, 1, 'READ'),
(@user1_id, 2, 'READ'),
(@user2_id, 1, 'READ'),
(@demo_id, 3, 'READ'); 