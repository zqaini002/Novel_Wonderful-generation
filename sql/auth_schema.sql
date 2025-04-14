-- 认证系统数据库表结构
-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一',
  email VARCHAR(255) NOT NULL UNIQUE COMMENT '电子邮箱，唯一',
  password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
  nickname VARCHAR(255) COMMENT '用户昵称',
  enabled BOOLEAN DEFAULT TRUE COMMENT '账号是否启用',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  last_login_at TIMESTAMP COMMENT '最后登录时间'
) COMMENT '用户表';

-- 创建角色表
CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称'
) COMMENT '角色表';

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) COMMENT '用户角色关联表';

-- 插入初始角色数据
INSERT INTO roles (name) VALUES
  ('ROLE_USER'),
  ('ROLE_ADMIN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 创建密码重置令牌表
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE,
  expiry_date TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '密码重置令牌表';

-- 创建用户设置表
CREATE TABLE IF NOT EXISTS user_settings (
  user_id BIGINT PRIMARY KEY,
  notification_enabled BOOLEAN DEFAULT TRUE COMMENT '通知是否启用',
  theme VARCHAR(50) DEFAULT 'light' COMMENT '界面主题',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户设置表';

-- 创建会话表（用于记录登录设备信息）
CREATE TABLE IF NOT EXISTS user_sessions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE COMMENT '会话令牌',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  device_info VARCHAR(255) COMMENT '设备信息',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户会话表';

-- 创建小说权限表（用于控制用户对小说的访问权限）
CREATE TABLE IF NOT EXISTS novel_permissions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  novel_id BIGINT NOT NULL,
  permission_type ENUM('READ', 'EDIT', 'DELETE', 'SHARE') NOT NULL,
  UNIQUE KEY unique_permission (user_id, novel_id, permission_type),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '小说权限表';

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_novel_permissions_user_id ON novel_permissions(user_id);
CREATE INDEX idx_novel_permissions_novel_id ON novel_permissions(novel_id); 