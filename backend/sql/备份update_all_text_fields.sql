-- 综合修改SQL脚本
-- 将所有需要修改的文本字段类型更新为longtext
-- 注意：执行此脚本前请先备份数据库

-- 1. 修改chapters表
ALTER TABLE `chapters` 
MODIFY COLUMN `title` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
MODIFY COLUMN `summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 2. 修改visualization_emotional_data表
ALTER TABLE `visualization_emotional_data` 
MODIFY COLUMN `eventDescription` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 3. 修改character_dialogues表
ALTER TABLE `character_dialogues` 
MODIFY COLUMN `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
MODIFY COLUMN `context` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 4. 修改visualization_structure_data表中的description字段
ALTER TABLE `visualization_structure_data` 
MODIFY COLUMN `description` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 5. 修改character_relationships表中的description字段
ALTER TABLE `character_relationships` 
MODIFY COLUMN `description` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 6. 修改novel_characters表中的description字段
ALTER TABLE `novel_characters` 
MODIFY COLUMN `description` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL; 