-- -----------------------------------------------------
-- rebuild_database.sql - 数据库重建主脚本
-- -----------------------------------------------------

-- 执行Schema脚本，创建数据库
SOURCE schema.sql;

-- 执行表结构创建脚本
SOURCE 01_create_tables.sql;

-- 执行初始数据脚本
SOURCE 02_init_data.sql;

-- 执行视图创建脚本
SOURCE 03_views.sql;

-- 执行存储过程创建脚本
SOURCE 04_stored_procedures.sql;

-- 执行索引创建脚本
SOURCE 05_indexes.sql;

-- 完成后打印提示信息
SELECT 'Database rebuild completed successfully!' AS Message; 