-- 修改chapters表的字段类型
-- 将title字段从varchar(255)修改为longtext
-- 将summary字段从mediumtext修改为longtext

ALTER TABLE `chapters` 
MODIFY COLUMN `title` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
MODIFY COLUMN `summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 注意：执行此脚本前请先备份数据库 