# 认证系统数据库设计

本文档介绍小说精读助手项目的认证系统数据库设计。

## 表结构

认证系统主要包含以下数据库表：

### 1. users 表

存储用户基本信息。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| email | VARCHAR(255) | 电子邮箱，唯一 |
| password | VARCHAR(255) | 加密后的密码 |
| nickname | VARCHAR(255) | 用户昵称 |
| enabled | BOOLEAN | 账号是否启用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| last_login_at | TIMESTAMP | 最后登录时间 |

### 2. roles 表

存储系统角色信息。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR(50) | 角色名称，唯一 |

### 3. user_roles 表

用户与角色的关联表，实现多对多关系。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| user_id | BIGINT | 用户ID，外键 |
| role_id | BIGINT | 角色ID，外键 |

### 4. password_reset_tokens 表

用于存储密码重置令牌，支持"忘记密码"功能。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID，外键 |
| token | VARCHAR(255) | 重置令牌，唯一 |
| expiry_date | TIMESTAMP | 过期时间 |

### 5. user_settings 表

存储用户个性化设置。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| user_id | BIGINT | 用户ID，主键 |
| notification_enabled | BOOLEAN | 通知是否启用 |
| theme | VARCHAR(50) | 界面主题 |

### 6. user_sessions 表

记录用户登录会话信息。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID，外键 |
| token | VARCHAR(255) | 会话令牌，唯一 |
| ip_address | VARCHAR(50) | IP地址 |
| device_info | VARCHAR(255) | 设备信息 |
| created_at | TIMESTAMP | 创建时间 |
| expires_at | TIMESTAMP | 过期时间 |

### 7. novel_permissions 表

控制用户对小说的访问权限。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID，外键 |
| novel_id | BIGINT | 小说ID |
| permission_type | ENUM | 权限类型：'READ'、'EDIT'、'DELETE'、'SHARE' |

## 索引设计

为提高查询性能，系统在以下字段上创建了索引：

1. `users.username`
2. `users.email`
3. `user_roles.user_id`
4. `user_sessions.user_id`
5. `novel_permissions.user_id`
6. `novel_permissions.novel_id`

## 角色与权限

系统默认包含两种角色：

1. `ROLE_USER`：普通用户，具有基本功能的访问权限
2. `ROLE_ADMIN`：管理员，具有系统管理功能的访问权限

## 使用说明

1. `auth_schema.sql`：包含建表语句和初始角色数据
2. `auth_data.sql`：包含示例用户数据，可用于测试和开发环境

## 实体关系图

```
users 1 --- * user_roles * --- 1 roles
users 1 --- 1 user_settings
users 1 --- * password_reset_tokens
users 1 --- * user_sessions
users 1 --- * novel_permissions
``` 