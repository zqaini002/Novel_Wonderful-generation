-- 修改visualization_emotional_data表的字段类型
-- 将eventDescription字段从mediumtext修改为longtext

ALTER TABLE `visualization_emotional_data` 
MODIFY COLUMN `eventDescription` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 注意：执行此脚本前请先备份数据库 