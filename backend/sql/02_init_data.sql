-- -----------------------------------------------------
-- 02_init_data.sql - 初始化基础数据
-- -----------------------------------------------------

-- -----------------------------------------------------
-- 角色初始化
-- -----------------------------------------------------
INSERT INTO `roles` (`name`) VALUES 
('ROLE_USER'),
('ROLE_ADMIN');

-- -----------------------------------------------------
-- 管理员用户初始化 (密码使用 BCrypt 加密, 默认密码为 '123456')
-- -----------------------------------------------------
INSERT INTO `users` (`username`, `email`, `password`, `nickname`, `enabled`, `created_at`, `updated_at`) VALUES 
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsNSEYy4H3Ec.Ti19tDla', 'Administrator', TRUE, NOW(), NOW());

-- 设置管理员角色
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES 
(1, 1), -- ROLE_USER
(1, 2); -- ROLE_ADMIN

-- 创建测试用户账户 (用户名: user, 密码: 123456)
INSERT INTO users (username, email, password, nickname, enabled, created_at, updated_at) 
VALUES ('user', 'user@novelinsight.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsNSEYy4H3Ec.Ti19tDla', '普通用户', true, NOW(), NOW());

-- 为测试用户分配角色
INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM users WHERE username = 'user'),
    (SELECT id FROM roles WHERE name = 'ROLE_USER');

-- 添加示例小说数据
INSERT INTO novels (title, author_name, description, user_id, processing_status, processed_chapters, total_chapters, created_at, updated_at)
VALUES 
('三体', '刘慈欣', '地球文明向宇宙发出了一条广播，引发了外星文明对地球的入侵计划，人类文明面临生死存亡的挑战...', 
 (SELECT id FROM users WHERE username = 'admin'), 'COMPLETED', 40, 40, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
 
('红楼梦', '曹雪芹', '中国古典小说，以贾、史、王、薛四大家族的兴衰为背景，讲述了贾宝玉和林黛玉、薛宝钗的爱情故事...', 
 (SELECT id FROM users WHERE username = 'admin'), 'COMPLETED', 120, 120, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),
 
('哈利·波特与魔法石', 'J.K.罗琳', '哈利·波特在11岁生日时得知自己是一个巫师，开始了魔法世界的冒险旅程...', 
 (SELECT id FROM users WHERE username = 'user'), 'COMPLETED', 17, 17, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
 
('西游记', '吴承恩', '唐僧师徒四人历经九九八十一难，取得真经的故事...', 
 (SELECT id FROM users WHERE username = 'user'), 'PROCESSING', 50, 100, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW());

-- 为小说添加章节（仅示例）
INSERT INTO chapters (novel_id, chapter_number, title, content, summary, created_at, updated_at)
VALUES 
-- 三体章节
((SELECT id FROM novels WHERE title = '三体' LIMIT 1), 1, '科学边界', '这是第一章内容...', '叶文洁在文革时期的经历，以及她在红岸基地的工作。', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
((SELECT id FROM novels WHERE title = '三体' LIMIT 1), 2, '三体问题', '这是第二章内容...', '汪淼接触到"科学边界"游戏，开始了解三体文明。', DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY)),

-- 红楼梦章节
((SELECT id FROM novels WHERE title = '红楼梦' LIMIT 1), 1, '甄士隐梦幻识通灵 贾雨村风尘怀闺秀', '这是第一章内容...', '甄士隐梦中游历，贾雨村偶遇林黛玉的父亲。', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
((SELECT id FROM novels WHERE title = '红楼梦' LIMIT 1), 2, '贾夫人仙逝扬州城 冷子兴演说荣国府', '这是第二章内容...', '冷子兴向贾雨村介绍贾府的情况。', DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY)),

-- 哈利·波特章节
((SELECT id FROM novels WHERE title = '哈利·波特与魔法石' LIMIT 1), 1, '大难不死的男孩', '这是第一章内容...', '哈利被送到德思礼家，开始了悲惨的生活。', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
((SELECT id FROM novels WHERE title = '哈利·波特与魔法石' LIMIT 1), 2, '消失的玻璃', '这是第二章内容...', '哈利意外展现出魔法能力，让动物园的玻璃消失。', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),

-- 西游记章节
((SELECT id FROM novels WHERE title = '西游记' LIMIT 1), 1, '灵根育孕源流出 心性修持大道生', '这是第一章内容...', '孙悟空的诞生和拜师学艺。', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
((SELECT id FROM novels WHERE title = '西游记' LIMIT 1), 2, '悟彻菩提真妙理 断魔归本合元神', '这是第二章内容...', '孙悟空学习七十二变和筋斗云。', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY));

-- 添加章节关键词
INSERT INTO chapter_keywords (chapter_id, keyword)
VALUES 
((SELECT id FROM chapters WHERE novel_id = (SELECT id FROM novels WHERE title = '三体' LIMIT 1) AND chapter_number = 1), '文革'),
((SELECT id FROM chapters WHERE novel_id = (SELECT id FROM novels WHERE title = '三体' LIMIT 1) AND chapter_number = 1), '红岸基地'),
((SELECT id FROM chapters WHERE novel_id = (SELECT id FROM novels WHERE title = '三体' LIMIT 1) AND chapter_number = 2), '三体问题'),
((SELECT id FROM chapters WHERE novel_id = (SELECT id FROM novels WHERE title = '三体' LIMIT 1) AND chapter_number = 2), '游戏');

-- 添加标签数据
INSERT INTO tags (novel_id, name, type, created_at, updated_at)
VALUES 
-- 三体标签
((SELECT id FROM novels WHERE title = '三体' LIMIT 1), '科幻', 'INFO', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
((SELECT id FROM novels WHERE title = '三体' LIMIT 1), '硬科幻', 'INFO', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
((SELECT id FROM novels WHERE title = '三体' LIMIT 1), '宇宙社会学', 'INFO', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
((SELECT id FROM novels WHERE title = '三体' LIMIT 1), '深刻的哲学思考', 'POSITIVE', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),

-- 红楼梦标签
((SELECT id FROM novels WHERE title = '红楼梦' LIMIT 1), '古典文学', 'INFO', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
((SELECT id FROM novels WHERE title = '红楼梦' LIMIT 1), '家族史诗', 'INFO', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
((SELECT id FROM novels WHERE title = '红楼梦' LIMIT 1), '优美的文笔', 'POSITIVE', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
((SELECT id FROM novels WHERE title = '红楼梦' LIMIT 1), '篇幅长', 'WARNING', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),

-- 哈利波特标签
((SELECT id FROM novels WHERE title = '哈利·波特与魔法石' LIMIT 1), '魔法', 'INFO', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
((SELECT id FROM novels WHERE title = '哈利·波特与魔法石' LIMIT 1), '成长', 'INFO', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
((SELECT id FROM novels WHERE title = '哈利·波特与魔法石' LIMIT 1), '友情', 'POSITIVE', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),

-- 西游记标签
((SELECT id FROM novels WHERE title = '西游记' LIMIT 1), '神话', 'INFO', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
((SELECT id FROM novels WHERE title = '西游记' LIMIT 1), '佛教', 'INFO', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
((SELECT id FROM novels WHERE title = '西游记' LIMIT 1), '冒险', 'POSITIVE', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

