-- 添加软删除功能到novels表
-- 作者: [你的名字]
-- 日期: 2025-04-22

-- 添加is_deleted字段
ALTER TABLE `novels` ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标志: 0=正常,1=已删除';

-- 将现有数据标记为未删除状态
UPDATE `novels` SET `is_deleted` = 0;

-- 为is_deleted字段添加索引以提高查询性能
CREATE INDEX `idx_novels_is_deleted` ON `novels` (`is_deleted`);

-- 更新视图和相关函数（如果需要）

-- 在此脚本中添加其他相关表的修改（如有需要） 
 