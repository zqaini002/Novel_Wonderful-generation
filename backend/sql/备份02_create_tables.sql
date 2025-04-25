/*
 Navicat Premium Dump SQL

 Source Server         : 8.0
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : novel_assistant

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 20/04/2025 21:48:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chapter_keywords
-- ----------------------------
DROP TABLE IF EXISTS `chapter_keywords`;
CREATE TABLE `chapter_keywords`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chapter_id` bigint NOT NULL,
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chapter_keywords_chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `idx_chapter_keywords_keyword`(`keyword` ASC) USING BTREE,
  CONSTRAINT `chapter_keywords_ibfk_1` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 155 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chapter_keywords
-- ----------------------------
INSERT INTO `chapter_keywords` VALUES (1, 1, '文革');
INSERT INTO `chapter_keywords` VALUES (2, 1, '红岸基地');
INSERT INTO `chapter_keywords` VALUES (3, 2, '三体问题');
INSERT INTO `chapter_keywords` VALUES (4, 2, '游戏');
INSERT INTO `chapter_keywords` VALUES (5, 9, '江湖');
INSERT INTO `chapter_keywords` VALUES (6, 9, '初入');
INSERT INTO `chapter_keywords` VALUES (7, 9, '师父');
INSERT INTO `chapter_keywords` VALUES (8, 9, '父母');
INSERT INTO `chapter_keywords` VALUES (9, 9, '剑');
INSERT INTO `chapter_keywords` VALUES (10, 9, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (11, 9, '剑雨');
INSERT INTO `chapter_keywords` VALUES (12, 9, '李');
INSERT INTO `chapter_keywords` VALUES (13, 9, '会');
INSERT INTO `chapter_keywords` VALUES (14, 9, '知道');
INSERT INTO `chapter_keywords` VALUES (15, 10, '女子');
INSERT INTO `chapter_keywords` VALUES (16, 10, '剑雨');
INSERT INTO `chapter_keywords` VALUES (17, 10, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (18, 10, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (19, 10, '\"\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (20, 10, '李剑雨');
INSERT INTO `chapter_keywords` VALUES (21, 10, '李');
INSERT INTO `chapter_keywords` VALUES (22, 10, '林月如');
INSERT INTO `chapter_keywords` VALUES (23, 10, '应道');
INSERT INTO `chapter_keywords` VALUES (24, 10, '马上来');
INSERT INTO `chapter_keywords` VALUES (25, 11, '剑雨');
INSERT INTO `chapter_keywords` VALUES (26, 11, '李');
INSERT INTO `chapter_keywords` VALUES (27, 11, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (28, 11, '。\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (29, 11, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (30, 11, '李剑雨');
INSERT INTO `chapter_keywords` VALUES (31, 11, '三当家');
INSERT INTO `chapter_keywords` VALUES (32, 11, '双刀');
INSERT INTO `chapter_keywords` VALUES (33, 11, '刀势');
INSERT INTO `chapter_keywords` VALUES (34, 11, '向李');
INSERT INTO `chapter_keywords` VALUES (35, 12, '林');
INSERT INTO `chapter_keywords` VALUES (36, 12, '门规');
INSERT INTO `chapter_keywords` VALUES (37, 12, '林月如');
INSERT INTO `chapter_keywords` VALUES (38, 12, '父亲');
INSERT INTO `chapter_keywords` VALUES (39, 12, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (40, 12, '张小凡');
INSERT INTO `chapter_keywords` VALUES (41, 12, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (42, 12, '挣扎');
INSERT INTO `chapter_keywords` VALUES (43, 12, '\"\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (44, 12, '迷茫');
INSERT INTO `chapter_keywords` VALUES (45, 13, '剑雨');
INSERT INTO `chapter_keywords` VALUES (46, 13, '林月如');
INSERT INTO `chapter_keywords` VALUES (47, 13, '李');
INSERT INTO `chapter_keywords` VALUES (48, 13, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (49, 13, '。\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (50, 13, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (51, 13, '李剑雨');
INSERT INTO `chapter_keywords` VALUES (52, 13, '\"李剑雨');
INSERT INTO `chapter_keywords` VALUES (53, 13, '林');
INSERT INTO `chapter_keywords` VALUES (54, 13, '青龙会');
INSERT INTO `chapter_keywords` VALUES (55, 14, '江湖');
INSERT INTO `chapter_keywords` VALUES (56, 14, '初入');
INSERT INTO `chapter_keywords` VALUES (57, 14, '师父');
INSERT INTO `chapter_keywords` VALUES (58, 14, '父母');
INSERT INTO `chapter_keywords` VALUES (59, 14, '剑');
INSERT INTO `chapter_keywords` VALUES (60, 14, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (61, 14, '剑雨');
INSERT INTO `chapter_keywords` VALUES (62, 14, '李');
INSERT INTO `chapter_keywords` VALUES (63, 14, '会');
INSERT INTO `chapter_keywords` VALUES (64, 14, '知道');
INSERT INTO `chapter_keywords` VALUES (65, 15, '女子');
INSERT INTO `chapter_keywords` VALUES (66, 15, '剑雨');
INSERT INTO `chapter_keywords` VALUES (67, 15, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (68, 15, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (69, 15, '\"\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (70, 15, '李剑雨');
INSERT INTO `chapter_keywords` VALUES (71, 15, '李');
INSERT INTO `chapter_keywords` VALUES (72, 15, '林月如');
INSERT INTO `chapter_keywords` VALUES (73, 15, '应道');
INSERT INTO `chapter_keywords` VALUES (74, 15, '马上来');
INSERT INTO `chapter_keywords` VALUES (75, 16, '剑雨');
INSERT INTO `chapter_keywords` VALUES (76, 16, '李');
INSERT INTO `chapter_keywords` VALUES (77, 16, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (78, 16, '。\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (79, 16, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (80, 16, '李剑雨');
INSERT INTO `chapter_keywords` VALUES (81, 16, '三当家');
INSERT INTO `chapter_keywords` VALUES (82, 16, '双刀');
INSERT INTO `chapter_keywords` VALUES (83, 16, '刀势');
INSERT INTO `chapter_keywords` VALUES (84, 16, '向李');
INSERT INTO `chapter_keywords` VALUES (85, 17, '林');
INSERT INTO `chapter_keywords` VALUES (86, 17, '门规');
INSERT INTO `chapter_keywords` VALUES (87, 17, '林月如');
INSERT INTO `chapter_keywords` VALUES (88, 17, '父亲');
INSERT INTO `chapter_keywords` VALUES (89, 17, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (90, 17, '张小凡');
INSERT INTO `chapter_keywords` VALUES (91, 17, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (92, 17, '挣扎');
INSERT INTO `chapter_keywords` VALUES (93, 17, '\"\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (94, 17, '迷茫');
INSERT INTO `chapter_keywords` VALUES (95, 18, '剑雨');
INSERT INTO `chapter_keywords` VALUES (96, 18, '林月如');
INSERT INTO `chapter_keywords` VALUES (97, 18, '李');
INSERT INTO `chapter_keywords` VALUES (98, 18, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (99, 18, '。\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (100, 18, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (101, 18, '李剑雨');
INSERT INTO `chapter_keywords` VALUES (102, 18, '\"李剑雨');
INSERT INTO `chapter_keywords` VALUES (103, 18, '林');
INSERT INTO `chapter_keywords` VALUES (104, 18, '青龙会');
INSERT INTO `chapter_keywords` VALUES (105, 19, '江湖');
INSERT INTO `chapter_keywords` VALUES (106, 19, '初入');
INSERT INTO `chapter_keywords` VALUES (107, 19, '师父');
INSERT INTO `chapter_keywords` VALUES (108, 19, '父母');
INSERT INTO `chapter_keywords` VALUES (109, 19, '剑');
INSERT INTO `chapter_keywords` VALUES (110, 19, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (111, 19, '剑雨');
INSERT INTO `chapter_keywords` VALUES (112, 19, '李');
INSERT INTO `chapter_keywords` VALUES (113, 19, '会');
INSERT INTO `chapter_keywords` VALUES (114, 19, '知道');
INSERT INTO `chapter_keywords` VALUES (115, 20, '女子');
INSERT INTO `chapter_keywords` VALUES (116, 20, '剑雨');
INSERT INTO `chapter_keywords` VALUES (117, 20, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (118, 20, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (119, 20, '\"\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (120, 20, '李剑雨');
INSERT INTO `chapter_keywords` VALUES (121, 20, '李');
INSERT INTO `chapter_keywords` VALUES (122, 20, '林月如');
INSERT INTO `chapter_keywords` VALUES (123, 20, '应道');
INSERT INTO `chapter_keywords` VALUES (124, 20, '马上来');
INSERT INTO `chapter_keywords` VALUES (125, 21, '剑雨');
INSERT INTO `chapter_keywords` VALUES (126, 21, '李');
INSERT INTO `chapter_keywords` VALUES (127, 21, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (128, 21, '。\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (129, 21, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (130, 21, '李剑雨');
INSERT INTO `chapter_keywords` VALUES (131, 21, '三当家');
INSERT INTO `chapter_keywords` VALUES (132, 21, '双刀');
INSERT INTO `chapter_keywords` VALUES (133, 21, '刀势');
INSERT INTO `chapter_keywords` VALUES (134, 21, '向李');
INSERT INTO `chapter_keywords` VALUES (135, 22, '林');
INSERT INTO `chapter_keywords` VALUES (136, 22, '门规');
INSERT INTO `chapter_keywords` VALUES (137, 22, '林月如');
INSERT INTO `chapter_keywords` VALUES (138, 22, '父亲');
INSERT INTO `chapter_keywords` VALUES (139, 22, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (140, 22, '张小凡');
INSERT INTO `chapter_keywords` VALUES (141, 22, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (142, 22, '挣扎');
INSERT INTO `chapter_keywords` VALUES (143, 22, '\"\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (144, 22, '迷茫');
INSERT INTO `chapter_keywords` VALUES (145, 23, '剑雨');
INSERT INTO `chapter_keywords` VALUES (146, 23, '林月如');
INSERT INTO `chapter_keywords` VALUES (147, 23, '李');
INSERT INTO `chapter_keywords` VALUES (148, 23, '\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (149, 23, '。\r\n\r\n\"');
INSERT INTO `chapter_keywords` VALUES (150, 23, '。\r\n\r\n');
INSERT INTO `chapter_keywords` VALUES (151, 23, '李剑雨');
INSERT INTO `chapter_keywords` VALUES (152, 23, '\"李剑雨');
INSERT INTO `chapter_keywords` VALUES (153, 23, '林');
INSERT INTO `chapter_keywords` VALUES (154, 23, '青龙会');

-- ----------------------------
-- Table structure for chapters
-- ----------------------------
DROP TABLE IF EXISTS `chapters`;
CREATE TABLE `chapters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `chapter_number` int NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `summary` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `word_count` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `novel_id`(`novel_id` ASC) USING BTREE,
  INDEX `chapter_number`(`chapter_number` ASC) USING BTREE,
  INDEX `title`(`title`(191)) USING BTREE,
  CONSTRAINT `chapters_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chapters
-- ----------------------------
INSERT INTO `chapters` VALUES (1, 1, 1, '科学边界', '这是第一章内容...', '叶文洁在文革时期的经历，以及她在红岸基地的工作。', '2025-03-21 20:43:17', '2025-03-21 20:43:17', NULL);
INSERT INTO `chapters` VALUES (2, 1, 2, '三体问题', '这是第二章内容...', '汪淼接触到\"科学边界\"游戏，开始了解三体文明。', '2025-03-22 20:43:17', '2025-03-22 20:43:17', NULL);
INSERT INTO `chapters` VALUES (3, 2, 1, '甄士隐梦幻识通灵 贾雨村风尘怀闺秀', '这是第一章内容...', '甄士隐梦中游历，贾雨村偶遇林黛玉的父亲。', '2025-03-26 20:43:17', '2025-03-26 20:43:17', NULL);
INSERT INTO `chapters` VALUES (4, 2, 2, '贾夫人仙逝扬州城 冷子兴演说荣国府', '这是第二章内容...', '冷子兴向贾雨村介绍贾府的情况。', '2025-03-27 20:43:17', '2025-03-27 20:43:17', NULL);
INSERT INTO `chapters` VALUES (5, 3, 1, '大难不死的男孩', '这是第一章内容...', '哈利被送到德思礼家，开始了悲惨的生活。', '2025-04-05 20:43:17', '2025-04-05 20:43:17', NULL);
INSERT INTO `chapters` VALUES (6, 3, 2, '消失的玻璃', '这是第二章内容...', '哈利意外展现出魔法能力，让动物园的玻璃消失。', '2025-04-06 20:43:17', '2025-04-06 20:43:17', NULL);
INSERT INTO `chapters` VALUES (7, 4, 1, '灵根育孕源流出 心性修持大道生', '这是第一章内容...', '孙悟空的诞生和拜师学艺。', '2025-04-15 20:43:17', '2025-04-15 20:43:17', NULL);
INSERT INTO `chapters` VALUES (8, 4, 2, '悟彻菩提真妙理 断魔归本合元神', '这是第二章内容...', '孙悟空学习七十二变和筋斗云。', '2025-04-16 20:43:17', '2025-04-16 20:43:17', NULL);
INSERT INTO `chapters` VALUES (9, 5, 1, '第一章 初入江湖', '第一章 初入江湖\r\n\r\n李剑雨站在山顶，望着脚下的浓雾翻滚。今天是他下山的日子，师父说江湖险恶，让他要小心行事。他摸了摸腰间的剑，心中充满了期待与不安。\r\n\r\n十五年的苦修，终于要面对真实的世界了。他还记得那个雨夜，父母被仇家所杀，是师父救了他，带他上山学艺。如今，他已掌握了\"青云剑法\"，是时候下山寻找仇人，为父母报仇了。\r\n\r\n李剑雨整理了一下行囊，最后看了一眼住了十五年的小木屋，深吸一口气，转身走向山下。他不知道江湖会给他带来什么，但他已准备好迎接一切挑战。\r\n\r\n', '【动作】师父说江湖险恶。第一章 初入江湖。为父母报仇了。李剑雨整理了一下行囊。 主要角色：李 关键词：江湖、初入、师父', '2025-04-20 20:44:47', '2025-04-20 20:44:47', 237);
INSERT INTO `chapters` VALUES (10, 5, 2, '第二章 酒馆相遇', '第二章 酒馆相遇\r\n\r\n\"小二，再来一壶酒！\"李剑雨坐在角落里，观察着酒馆内的每一个人。\r\n\r\n小二笑着应道：\"客官稍等，马上来！\"\r\n\r\n李剑雨已经在这个镇子上停留了三天，他听说这里可能有关于\"黑风寨\"的消息。黑风寨的寨主就是当年杀害他父母的凶手之一。\r\n\r\n就在他思索时，酒馆的门被推开，一个身着青衣的女子走了进来。女子身材修长，面容姣好，腰间却别着一把短剑，显然也是习武之人。\r\n\r\n\"姑娘，一个人在外要小心啊！\"一个醉汉站起来，挡在女子面前。\r\n\r\n女子冷笑一声：\"让开。\"\r\n\r\n\"哟，还挺有脾气！来陪大爷喝一杯嘛！\"醉汉伸手就要去拉女子的手腕。\r\n\r\n\"我说了，让开。\"女子眼中闪过一丝寒光。\r\n\r\n醉汉不以为然，继续上前，却不料女子手腕一翻，短剑出鞘，抵在了醉汉的喉咙上。\r\n\r\n\"我再说最后一次，让开。\"\r\n\r\n李剑雨看得出女子武功不俗，手法干净利落，应该是名门正派弟子。\r\n\r\n醉汉吓得后退三步，嘟囔着：\"臭婆娘，不识好歹！\"\r\n\r\n女子收剑入鞘，走到吧台前：\"掌柜的，来碗面，再要壶温酒。\"\r\n\r\n李剑雨饶有兴趣地观察着这位女子，不知为何，他感觉她身上有种熟悉的气息。\r\n\r\n\"客官，您的酒。\"小二将酒壶放在李剑雨桌上。\r\n\r\n李剑雨道谢后问道：\"小二哥，那位姑娘是何人？\"\r\n\r\n小二低声道：\"听说是青云门的弟子，叫林月如，在追查一个叫\'黑风寨\'的匪窝。\"\r\n\r\n\"青云门？黑风寨？\"李剑雨心中一震，难道她也在寻找那些杀害他父母的凶手？\r\n\r\n', '【对话】李剑雨饶有兴趣地观察着这位女子。再来一壶酒。\"醉汉伸手就要去拉女子的手腕。李剑雨看得出女子武功不俗。 主要角色：李 关键词：女子、剑雨、\r\n\r\n\"', '2025-04-20 20:44:47', '2025-04-20 20:44:47', 633);
INSERT INTO `chapters` VALUES (11, 5, 3, '第三章 生死一战', '第三章 生死一战\r\n\r\n山谷间，刀光剑影。李剑雨面对的是黑风寨的三当家，一个使双刀的精壮汉子。\r\n\r\n\"小子，敢闯我黑风寨，真是活得不耐烦了！\"三当家手中双刀挥舞如风，向李剑雨劈来。\r\n\r\n李剑雨神色凝重，青云剑法展开，剑气如虹。\"十五年前，你们杀我父母，今日我李剑雨前来索命！\"\r\n\r\n\"哈哈哈！原来是报仇的！十五年前杀的人太多，我可记不清了！\"三当家狂笑着，刀势更加凌厉。\r\n\r\n李剑雨怒气上涌，剑招越发凌厉，但三当家经验丰富，刀法狠辣，一时间二人难分高下。\r\n\r\n\"砰！\"一声巨响，李剑雨被逼退三步，胸口传来阵阵剧痛，显然是受了内伤。\r\n\r\n三当家趁机连劈三刀，每一刀都带着凌厉的杀气。李剑雨勉强抵挡，却被震得气血翻涌，嘴角溢出鲜血。\r\n\r\n\"小子，受死吧！\"三当家一记横扫，直取李剑雨咽喉。\r\n\r\n危急时刻，李剑雨脑中闪过师父教导的最后一招\"青云逆命\"，这是青云剑法中最危险的一招，稍有不慎就会走火入魔。\r\n\r\n\"为父母报仇，死又何惧！\"李剑雨一声厉喝，体内真气逆行，手中长剑光芒大盛。\r\n\r\n\"嗤！\"\r\n\r\n血花飞溅，两人交错而过。\r\n\r\n李剑雨单膝跪地，胸口的伤势更重了，但他知道，胜利是他的。\r\n\r\n果然，三当家向前踉跄几步，然后轰然倒地，胸口有一道深可见骨的剑伤。\r\n\r\n\"说！黑风寨的寨主在哪里？\"李剑雨强撑着走到三当家面前。\r\n\r\n\"哈...哈哈...你杀不了他的...他现在...是...青...龙...会的...人...了...\"三当家断断续续地说完，气绝身亡。\r\n\r\n李剑雨呆立当场。青龙会，江湖上最神秘最强大的组织，如果黑风寨寨主真的加入了青龙会，那他的复仇之路将更加艰难。\r\n\r\n', '【对话】李剑雨面对的是黑风寨的三当家。剑招越发凌厉。\"李剑雨强撑着走到三当家面前。李剑雨神色凝重。 主要角色：李、双刀 关键词：剑雨、李、\r\n\r\n\"', '2025-04-20 20:44:47', '2025-04-20 20:44:47', 705);
INSERT INTO `chapters` VALUES (12, 5, 4, '第四章 心之挣扎', '第四章 心之挣扎\r\n\r\n林月如坐在溪边，看着水中自己的倒影，心中思绪万千。\r\n\r\n今天，她再次看到了那个叫李剑雨的年轻剑客。他为父母报仇的决心让她想起了自己的过去。青云门虽是名门正派，但门规森严，不允许弟子私自复仇。然而，她又怎能忘记父亲临终前的嘱托？\r\n\r\n\"月如，不要被仇恨蒙蔽双眼...\"父亲的话常在她耳边回响。\r\n\r\n林月如叹了口气，拾起一块小石子，投入溪水中，激起层层涟漪。她不知道该如何选择：是遵循门规，还是完成父亲的遗愿？更让她困扰的是，她对李剑雨产生了异样的感情。\r\n\r\n\"为什么会这样？\"林月如喃喃自语，\"我们素不相识，只是有着相似的过去罢了。\"\r\n\r\n溪水清澈，倒映着她忧愁的面容。林月如感到一种前所未有的孤独和迷茫。\r\n\r\n\"师姐，原来你在这里。\"身后传来一个熟悉的声音，是她的师弟张小凡。\r\n\r\n\"小凡，你来了。\"林月如勉强挤出一丝微笑。\r\n\r\n张小凡在她身旁坐下：\"师姐，你最近心事重重，是不是又在想复仇的事？\"\r\n\r\n林月如沉默了片刻，轻声道：\"小凡，你说人活着到底是为了什么？\"\r\n\r\n\"我想...是为了遵从本心吧。\"张小凡望着远处的山峦，\"无论门规如何，最重要的是不辜负自己的内心。\"\r\n\r\n林月如望着师弟，突然觉得这个平日里腼腆的少年今天格外睿智。\r\n\r\n\"谢谢你，小凡。\"林月如站起身，眼中的迷茫渐渐散去，取而代之的是坚定，\"我知道该怎么做了。\"\r\n\r\n', '【对话】林月如望着师弟。是不是又在想复仇的事。林月如沉默了片刻。林月如感到一种前所未有的孤独和迷茫。青云门虽是名门正派，但门规森严，不允许弟子私自复仇。 本章氛围紧张压抑。 主要角色：门规、李 关键词：林、门规、林月如', '2025-04-20 20:44:47', '2025-04-20 20:44:47', 595);
INSERT INTO `chapters` VALUES (13, 5, 5, '第五章 联手探险', '第五章 联手探险\r\n\r\n\"你确定这里就是青龙会的秘密据点？\"李剑雨低声问道，眼睛盯着前方的山洞。\r\n\r\n林月如点点头：\"我跟踪那个信使三天了，他每次都到这个山洞，然后就消失不见。\"\r\n\r\n两人蹑手蹑脚地接近山洞入口。过去的一个月里，他们从对手变成了搭档，共同调查青龙会的下落。虽然各有目的，但他们发现彼此配合默契，一个擅长正面突破，一个长于隐蔽行动。\r\n\r\n\"小心，有埋伏。\"李剑雨突然拉住林月如，指了指地上几乎不可见的细线。\r\n\r\n林月如心中一惊，若不是李剑雨眼尖，她可能已经触发了机关。\r\n\r\n\"这里的防备如此严密，看来青龙会确实有问题。\"林月如谨慎地跨过细线。\r\n\r\n两人小心翼翼地进入山洞，洞内漆黑一片。\r\n\r\n\"等等。\"李剑雨从怀中取出一块发光的石头，顿时，周围亮起微弱的光芒。\r\n\r\n\"夜明珠？\"林月如惊讶地看着这块罕见的宝物。\r\n\r\n\"是师父留给我的，说是关键时刻用。\"李剑雨解释道。\r\n\r\n借着夜明珠的光，他们发现山洞深处有一条暗道。\r\n\r\n\"走吧，小心点。\"李剑雨走在前面，林月如紧随其后。\r\n\r\n暗道曲折蜿蜒，时而上升，时而下降，让人难以辨别方向。终于，他们来到一个宽阔的地下空间，空间中央是一个巨大的石台，上面摆放着各种奇怪的器具和卷轴。\r\n\r\n\"这是...炼丹的工具？\"林月如惊讶地看着那些器具。\r\n\r\n李剑雨走向石台，拿起一卷竹简，打开一看，脸色立刻变得凝重：\"这是\'九阴真经\'的残篇，传说中能让人练就盖世武功的秘籍，没想到竟在青龙会手中！\"\r\n\r\n\"不仅如此，\"林月如指着另一边的图纸，\"这些都是江湖各大门派的地形图和人员布置，青龙会是想...\"\r\n\r\n\"图谋不轨！\"李剑雨接过话头，\"我们得把这些证据带出去，告诉江湖各派！\"\r\n\r\n就在此时，远处传来脚步声和谈话声。\r\n\r\n\"快躲起来！\"李剑雨拉着林月如，躲到了一处暗影中。 ', '【对话】他们发现山洞深处有一条暗道。\"李剑雨拉着林月如。\"林月如惊讶地看着那些器具。\"李剑雨突然拉住林月如。 主要角色：李、林月如 关键词：剑雨、林月如、李', '2025-04-20 20:44:47', '2025-04-20 20:44:47', 778);
INSERT INTO `chapters` VALUES (14, 6, 1, '第一章 初入江湖', '第一章 初入江湖\r\n\r\n李剑雨站在山顶，望着脚下的浓雾翻滚。今天是他下山的日子，师父说江湖险恶，让他要小心行事。他摸了摸腰间的剑，心中充满了期待与不安。\r\n\r\n十五年的苦修，终于要面对真实的世界了。他还记得那个雨夜，父母被仇家所杀，是师父救了他，带他上山学艺。如今，他已掌握了\"青云剑法\"，是时候下山寻找仇人，为父母报仇了。\r\n\r\n李剑雨整理了一下行囊，最后看了一眼住了十五年的小木屋，深吸一口气，转身走向山下。他不知道江湖会给他带来什么，但他已准备好迎接一切挑战。\r\n\r\n', '【动作】师父说江湖险恶。第一章 初入江湖。为父母报仇了。李剑雨整理了一下行囊。 主要角色：李 关键词：江湖、初入、师父', '2025-04-20 20:49:24', '2025-04-20 20:49:24', 237);
INSERT INTO `chapters` VALUES (15, 6, 2, '第二章 酒馆相遇', '第二章 酒馆相遇\r\n\r\n\"小二，再来一壶酒！\"李剑雨坐在角落里，观察着酒馆内的每一个人。\r\n\r\n小二笑着应道：\"客官稍等，马上来！\"\r\n\r\n李剑雨已经在这个镇子上停留了三天，他听说这里可能有关于\"黑风寨\"的消息。黑风寨的寨主就是当年杀害他父母的凶手之一。\r\n\r\n就在他思索时，酒馆的门被推开，一个身着青衣的女子走了进来。女子身材修长，面容姣好，腰间却别着一把短剑，显然也是习武之人。\r\n\r\n\"姑娘，一个人在外要小心啊！\"一个醉汉站起来，挡在女子面前。\r\n\r\n女子冷笑一声：\"让开。\"\r\n\r\n\"哟，还挺有脾气！来陪大爷喝一杯嘛！\"醉汉伸手就要去拉女子的手腕。\r\n\r\n\"我说了，让开。\"女子眼中闪过一丝寒光。\r\n\r\n醉汉不以为然，继续上前，却不料女子手腕一翻，短剑出鞘，抵在了醉汉的喉咙上。\r\n\r\n\"我再说最后一次，让开。\"\r\n\r\n李剑雨看得出女子武功不俗，手法干净利落，应该是名门正派弟子。\r\n\r\n醉汉吓得后退三步，嘟囔着：\"臭婆娘，不识好歹！\"\r\n\r\n女子收剑入鞘，走到吧台前：\"掌柜的，来碗面，再要壶温酒。\"\r\n\r\n李剑雨饶有兴趣地观察着这位女子，不知为何，他感觉她身上有种熟悉的气息。\r\n\r\n\"客官，您的酒。\"小二将酒壶放在李剑雨桌上。\r\n\r\n李剑雨道谢后问道：\"小二哥，那位姑娘是何人？\"\r\n\r\n小二低声道：\"听说是青云门的弟子，叫林月如，在追查一个叫\'黑风寨\'的匪窝。\"\r\n\r\n\"青云门？黑风寨？\"李剑雨心中一震，难道她也在寻找那些杀害他父母的凶手？\r\n\r\n', '【对话】李剑雨饶有兴趣地观察着这位女子。再来一壶酒。\"醉汉伸手就要去拉女子的手腕。李剑雨看得出女子武功不俗。 主要角色：李 关键词：女子、剑雨、\r\n\r\n\"', '2025-04-20 20:49:24', '2025-04-20 20:49:24', 633);
INSERT INTO `chapters` VALUES (16, 6, 3, '第三章 生死一战', '第三章 生死一战\r\n\r\n山谷间，刀光剑影。李剑雨面对的是黑风寨的三当家，一个使双刀的精壮汉子。\r\n\r\n\"小子，敢闯我黑风寨，真是活得不耐烦了！\"三当家手中双刀挥舞如风，向李剑雨劈来。\r\n\r\n李剑雨神色凝重，青云剑法展开，剑气如虹。\"十五年前，你们杀我父母，今日我李剑雨前来索命！\"\r\n\r\n\"哈哈哈！原来是报仇的！十五年前杀的人太多，我可记不清了！\"三当家狂笑着，刀势更加凌厉。\r\n\r\n李剑雨怒气上涌，剑招越发凌厉，但三当家经验丰富，刀法狠辣，一时间二人难分高下。\r\n\r\n\"砰！\"一声巨响，李剑雨被逼退三步，胸口传来阵阵剧痛，显然是受了内伤。\r\n\r\n三当家趁机连劈三刀，每一刀都带着凌厉的杀气。李剑雨勉强抵挡，却被震得气血翻涌，嘴角溢出鲜血。\r\n\r\n\"小子，受死吧！\"三当家一记横扫，直取李剑雨咽喉。\r\n\r\n危急时刻，李剑雨脑中闪过师父教导的最后一招\"青云逆命\"，这是青云剑法中最危险的一招，稍有不慎就会走火入魔。\r\n\r\n\"为父母报仇，死又何惧！\"李剑雨一声厉喝，体内真气逆行，手中长剑光芒大盛。\r\n\r\n\"嗤！\"\r\n\r\n血花飞溅，两人交错而过。\r\n\r\n李剑雨单膝跪地，胸口的伤势更重了，但他知道，胜利是他的。\r\n\r\n果然，三当家向前踉跄几步，然后轰然倒地，胸口有一道深可见骨的剑伤。\r\n\r\n\"说！黑风寨的寨主在哪里？\"李剑雨强撑着走到三当家面前。\r\n\r\n\"哈...哈哈...你杀不了他的...他现在...是...青...龙...会的...人...了...\"三当家断断续续地说完，气绝身亡。\r\n\r\n李剑雨呆立当场。青龙会，江湖上最神秘最强大的组织，如果黑风寨寨主真的加入了青龙会，那他的复仇之路将更加艰难。\r\n\r\n', '【对话】李剑雨面对的是黑风寨的三当家。剑招越发凌厉。\"李剑雨强撑着走到三当家面前。李剑雨神色凝重。 主要角色：李、双刀 关键词：剑雨、李、\r\n\r\n\"', '2025-04-20 20:49:24', '2025-04-20 20:49:24', 705);
INSERT INTO `chapters` VALUES (17, 6, 4, '第四章 心之挣扎', '第四章 心之挣扎\r\n\r\n林月如坐在溪边，看着水中自己的倒影，心中思绪万千。\r\n\r\n今天，她再次看到了那个叫李剑雨的年轻剑客。他为父母报仇的决心让她想起了自己的过去。青云门虽是名门正派，但门规森严，不允许弟子私自复仇。然而，她又怎能忘记父亲临终前的嘱托？\r\n\r\n\"月如，不要被仇恨蒙蔽双眼...\"父亲的话常在她耳边回响。\r\n\r\n林月如叹了口气，拾起一块小石子，投入溪水中，激起层层涟漪。她不知道该如何选择：是遵循门规，还是完成父亲的遗愿？更让她困扰的是，她对李剑雨产生了异样的感情。\r\n\r\n\"为什么会这样？\"林月如喃喃自语，\"我们素不相识，只是有着相似的过去罢了。\"\r\n\r\n溪水清澈，倒映着她忧愁的面容。林月如感到一种前所未有的孤独和迷茫。\r\n\r\n\"师姐，原来你在这里。\"身后传来一个熟悉的声音，是她的师弟张小凡。\r\n\r\n\"小凡，你来了。\"林月如勉强挤出一丝微笑。\r\n\r\n张小凡在她身旁坐下：\"师姐，你最近心事重重，是不是又在想复仇的事？\"\r\n\r\n林月如沉默了片刻，轻声道：\"小凡，你说人活着到底是为了什么？\"\r\n\r\n\"我想...是为了遵从本心吧。\"张小凡望着远处的山峦，\"无论门规如何，最重要的是不辜负自己的内心。\"\r\n\r\n林月如望着师弟，突然觉得这个平日里腼腆的少年今天格外睿智。\r\n\r\n\"谢谢你，小凡。\"林月如站起身，眼中的迷茫渐渐散去，取而代之的是坚定，\"我知道该怎么做了。\"\r\n\r\n', '【对话】林月如望着师弟。是不是又在想复仇的事。林月如沉默了片刻。林月如感到一种前所未有的孤独和迷茫。青云门虽是名门正派，但门规森严，不允许弟子私自复仇。 本章氛围紧张压抑。 主要角色：门规、李 关键词：林、门规、林月如', '2025-04-20 20:49:24', '2025-04-20 20:49:24', 595);
INSERT INTO `chapters` VALUES (18, 6, 5, '第五章 联手探险', '第五章 联手探险\r\n\r\n\"你确定这里就是青龙会的秘密据点？\"李剑雨低声问道，眼睛盯着前方的山洞。\r\n\r\n林月如点点头：\"我跟踪那个信使三天了，他每次都到这个山洞，然后就消失不见。\"\r\n\r\n两人蹑手蹑脚地接近山洞入口。过去的一个月里，他们从对手变成了搭档，共同调查青龙会的下落。虽然各有目的，但他们发现彼此配合默契，一个擅长正面突破，一个长于隐蔽行动。\r\n\r\n\"小心，有埋伏。\"李剑雨突然拉住林月如，指了指地上几乎不可见的细线。\r\n\r\n林月如心中一惊，若不是李剑雨眼尖，她可能已经触发了机关。\r\n\r\n\"这里的防备如此严密，看来青龙会确实有问题。\"林月如谨慎地跨过细线。\r\n\r\n两人小心翼翼地进入山洞，洞内漆黑一片。\r\n\r\n\"等等。\"李剑雨从怀中取出一块发光的石头，顿时，周围亮起微弱的光芒。\r\n\r\n\"夜明珠？\"林月如惊讶地看着这块罕见的宝物。\r\n\r\n\"是师父留给我的，说是关键时刻用。\"李剑雨解释道。\r\n\r\n借着夜明珠的光，他们发现山洞深处有一条暗道。\r\n\r\n\"走吧，小心点。\"李剑雨走在前面，林月如紧随其后。\r\n\r\n暗道曲折蜿蜒，时而上升，时而下降，让人难以辨别方向。终于，他们来到一个宽阔的地下空间，空间中央是一个巨大的石台，上面摆放着各种奇怪的器具和卷轴。\r\n\r\n\"这是...炼丹的工具？\"林月如惊讶地看着那些器具。\r\n\r\n李剑雨走向石台，拿起一卷竹简，打开一看，脸色立刻变得凝重：\"这是\'九阴真经\'的残篇，传说中能让人练就盖世武功的秘籍，没想到竟在青龙会手中！\"\r\n\r\n\"不仅如此，\"林月如指着另一边的图纸，\"这些都是江湖各大门派的地形图和人员布置，青龙会是想...\"\r\n\r\n\"图谋不轨！\"李剑雨接过话头，\"我们得把这些证据带出去，告诉江湖各派！\"\r\n\r\n就在此时，远处传来脚步声和谈话声。\r\n\r\n\"快躲起来！\"李剑雨拉着林月如，躲到了一处暗影中。 ', '【对话】他们发现山洞深处有一条暗道。\"李剑雨拉着林月如。\"林月如惊讶地看着那些器具。\"李剑雨突然拉住林月如。 主要角色：李、林月如 关键词：剑雨、林月如、李', '2025-04-20 20:49:24', '2025-04-20 20:49:24', 778);
INSERT INTO `chapters` VALUES (19, 7, 1, '第一章 初入江湖', '第一章 初入江湖\r\n\r\n李剑雨站在山顶，望着脚下的浓雾翻滚。今天是他下山的日子，师父说江湖险恶，让他要小心行事。他摸了摸腰间的剑，心中充满了期待与不安。\r\n\r\n十五年的苦修，终于要面对真实的世界了。他还记得那个雨夜，父母被仇家所杀，是师父救了他，带他上山学艺。如今，他已掌握了\"青云剑法\"，是时候下山寻找仇人，为父母报仇了。\r\n\r\n李剑雨整理了一下行囊，最后看了一眼住了十五年的小木屋，深吸一口气，转身走向山下。他不知道江湖会给他带来什么，但他已准备好迎接一切挑战。\r\n\r\n', '【动作】师父说江湖险恶。第一章 初入江湖。为父母报仇了。李剑雨整理了一下行囊。 主要角色：李 关键词：江湖、初入、师父', '2025-04-20 20:57:45', '2025-04-20 20:57:45', 237);
INSERT INTO `chapters` VALUES (20, 7, 2, '第二章 酒馆相遇', '第二章 酒馆相遇\r\n\r\n\"小二，再来一壶酒！\"李剑雨坐在角落里，观察着酒馆内的每一个人。\r\n\r\n小二笑着应道：\"客官稍等，马上来！\"\r\n\r\n李剑雨已经在这个镇子上停留了三天，他听说这里可能有关于\"黑风寨\"的消息。黑风寨的寨主就是当年杀害他父母的凶手之一。\r\n\r\n就在他思索时，酒馆的门被推开，一个身着青衣的女子走了进来。女子身材修长，面容姣好，腰间却别着一把短剑，显然也是习武之人。\r\n\r\n\"姑娘，一个人在外要小心啊！\"一个醉汉站起来，挡在女子面前。\r\n\r\n女子冷笑一声：\"让开。\"\r\n\r\n\"哟，还挺有脾气！来陪大爷喝一杯嘛！\"醉汉伸手就要去拉女子的手腕。\r\n\r\n\"我说了，让开。\"女子眼中闪过一丝寒光。\r\n\r\n醉汉不以为然，继续上前，却不料女子手腕一翻，短剑出鞘，抵在了醉汉的喉咙上。\r\n\r\n\"我再说最后一次，让开。\"\r\n\r\n李剑雨看得出女子武功不俗，手法干净利落，应该是名门正派弟子。\r\n\r\n醉汉吓得后退三步，嘟囔着：\"臭婆娘，不识好歹！\"\r\n\r\n女子收剑入鞘，走到吧台前：\"掌柜的，来碗面，再要壶温酒。\"\r\n\r\n李剑雨饶有兴趣地观察着这位女子，不知为何，他感觉她身上有种熟悉的气息。\r\n\r\n\"客官，您的酒。\"小二将酒壶放在李剑雨桌上。\r\n\r\n李剑雨道谢后问道：\"小二哥，那位姑娘是何人？\"\r\n\r\n小二低声道：\"听说是青云门的弟子，叫林月如，在追查一个叫\'黑风寨\'的匪窝。\"\r\n\r\n\"青云门？黑风寨？\"李剑雨心中一震，难道她也在寻找那些杀害他父母的凶手？\r\n\r\n', '【对话】李剑雨饶有兴趣地观察着这位女子。再来一壶酒。\"醉汉伸手就要去拉女子的手腕。李剑雨看得出女子武功不俗。 主要角色：李 关键词：女子、剑雨、\r\n\r\n\"', '2025-04-20 20:57:45', '2025-04-20 20:57:45', 633);
INSERT INTO `chapters` VALUES (21, 7, 3, '第三章 生死一战', '第三章 生死一战\r\n\r\n山谷间，刀光剑影。李剑雨面对的是黑风寨的三当家，一个使双刀的精壮汉子。\r\n\r\n\"小子，敢闯我黑风寨，真是活得不耐烦了！\"三当家手中双刀挥舞如风，向李剑雨劈来。\r\n\r\n李剑雨神色凝重，青云剑法展开，剑气如虹。\"十五年前，你们杀我父母，今日我李剑雨前来索命！\"\r\n\r\n\"哈哈哈！原来是报仇的！十五年前杀的人太多，我可记不清了！\"三当家狂笑着，刀势更加凌厉。\r\n\r\n李剑雨怒气上涌，剑招越发凌厉，但三当家经验丰富，刀法狠辣，一时间二人难分高下。\r\n\r\n\"砰！\"一声巨响，李剑雨被逼退三步，胸口传来阵阵剧痛，显然是受了内伤。\r\n\r\n三当家趁机连劈三刀，每一刀都带着凌厉的杀气。李剑雨勉强抵挡，却被震得气血翻涌，嘴角溢出鲜血。\r\n\r\n\"小子，受死吧！\"三当家一记横扫，直取李剑雨咽喉。\r\n\r\n危急时刻，李剑雨脑中闪过师父教导的最后一招\"青云逆命\"，这是青云剑法中最危险的一招，稍有不慎就会走火入魔。\r\n\r\n\"为父母报仇，死又何惧！\"李剑雨一声厉喝，体内真气逆行，手中长剑光芒大盛。\r\n\r\n\"嗤！\"\r\n\r\n血花飞溅，两人交错而过。\r\n\r\n李剑雨单膝跪地，胸口的伤势更重了，但他知道，胜利是他的。\r\n\r\n果然，三当家向前踉跄几步，然后轰然倒地，胸口有一道深可见骨的剑伤。\r\n\r\n\"说！黑风寨的寨主在哪里？\"李剑雨强撑着走到三当家面前。\r\n\r\n\"哈...哈哈...你杀不了他的...他现在...是...青...龙...会的...人...了...\"三当家断断续续地说完，气绝身亡。\r\n\r\n李剑雨呆立当场。青龙会，江湖上最神秘最强大的组织，如果黑风寨寨主真的加入了青龙会，那他的复仇之路将更加艰难。\r\n\r\n', '【对话】李剑雨面对的是黑风寨的三当家。剑招越发凌厉。\"李剑雨强撑着走到三当家面前。李剑雨神色凝重。 主要角色：李、双刀 关键词：剑雨、李、\r\n\r\n\"', '2025-04-20 20:57:45', '2025-04-20 20:57:45', 705);
INSERT INTO `chapters` VALUES (22, 7, 4, '第四章 心之挣扎', '第四章 心之挣扎\r\n\r\n林月如坐在溪边，看着水中自己的倒影，心中思绪万千。\r\n\r\n今天，她再次看到了那个叫李剑雨的年轻剑客。他为父母报仇的决心让她想起了自己的过去。青云门虽是名门正派，但门规森严，不允许弟子私自复仇。然而，她又怎能忘记父亲临终前的嘱托？\r\n\r\n\"月如，不要被仇恨蒙蔽双眼...\"父亲的话常在她耳边回响。\r\n\r\n林月如叹了口气，拾起一块小石子，投入溪水中，激起层层涟漪。她不知道该如何选择：是遵循门规，还是完成父亲的遗愿？更让她困扰的是，她对李剑雨产生了异样的感情。\r\n\r\n\"为什么会这样？\"林月如喃喃自语，\"我们素不相识，只是有着相似的过去罢了。\"\r\n\r\n溪水清澈，倒映着她忧愁的面容。林月如感到一种前所未有的孤独和迷茫。\r\n\r\n\"师姐，原来你在这里。\"身后传来一个熟悉的声音，是她的师弟张小凡。\r\n\r\n\"小凡，你来了。\"林月如勉强挤出一丝微笑。\r\n\r\n张小凡在她身旁坐下：\"师姐，你最近心事重重，是不是又在想复仇的事？\"\r\n\r\n林月如沉默了片刻，轻声道：\"小凡，你说人活着到底是为了什么？\"\r\n\r\n\"我想...是为了遵从本心吧。\"张小凡望着远处的山峦，\"无论门规如何，最重要的是不辜负自己的内心。\"\r\n\r\n林月如望着师弟，突然觉得这个平日里腼腆的少年今天格外睿智。\r\n\r\n\"谢谢你，小凡。\"林月如站起身，眼中的迷茫渐渐散去，取而代之的是坚定，\"我知道该怎么做了。\"\r\n\r\n', '【对话】林月如望着师弟。是不是又在想复仇的事。林月如沉默了片刻。林月如感到一种前所未有的孤独和迷茫。青云门虽是名门正派，但门规森严，不允许弟子私自复仇。 本章氛围紧张压抑。 主要角色：门规、李 关键词：林、门规、林月如', '2025-04-20 20:57:45', '2025-04-20 20:57:45', 595);
INSERT INTO `chapters` VALUES (23, 7, 5, '第五章 联手探险', '第五章 联手探险\r\n\r\n\"你确定这里就是青龙会的秘密据点？\"李剑雨低声问道，眼睛盯着前方的山洞。\r\n\r\n林月如点点头：\"我跟踪那个信使三天了，他每次都到这个山洞，然后就消失不见。\"\r\n\r\n两人蹑手蹑脚地接近山洞入口。过去的一个月里，他们从对手变成了搭档，共同调查青龙会的下落。虽然各有目的，但他们发现彼此配合默契，一个擅长正面突破，一个长于隐蔽行动。\r\n\r\n\"小心，有埋伏。\"李剑雨突然拉住林月如，指了指地上几乎不可见的细线。\r\n\r\n林月如心中一惊，若不是李剑雨眼尖，她可能已经触发了机关。\r\n\r\n\"这里的防备如此严密，看来青龙会确实有问题。\"林月如谨慎地跨过细线。\r\n\r\n两人小心翼翼地进入山洞，洞内漆黑一片。\r\n\r\n\"等等。\"李剑雨从怀中取出一块发光的石头，顿时，周围亮起微弱的光芒。\r\n\r\n\"夜明珠？\"林月如惊讶地看着这块罕见的宝物。\r\n\r\n\"是师父留给我的，说是关键时刻用。\"李剑雨解释道。\r\n\r\n借着夜明珠的光，他们发现山洞深处有一条暗道。\r\n\r\n\"走吧，小心点。\"李剑雨走在前面，林月如紧随其后。\r\n\r\n暗道曲折蜿蜒，时而上升，时而下降，让人难以辨别方向。终于，他们来到一个宽阔的地下空间，空间中央是一个巨大的石台，上面摆放着各种奇怪的器具和卷轴。\r\n\r\n\"这是...炼丹的工具？\"林月如惊讶地看着那些器具。\r\n\r\n李剑雨走向石台，拿起一卷竹简，打开一看，脸色立刻变得凝重：\"这是\'九阴真经\'的残篇，传说中能让人练就盖世武功的秘籍，没想到竟在青龙会手中！\"\r\n\r\n\"不仅如此，\"林月如指着另一边的图纸，\"这些都是江湖各大门派的地形图和人员布置，青龙会是想...\"\r\n\r\n\"图谋不轨！\"李剑雨接过话头，\"我们得把这些证据带出去，告诉江湖各派！\"\r\n\r\n就在此时，远处传来脚步声和谈话声。\r\n\r\n\"快躲起来！\"李剑雨拉着林月如，躲到了一处暗影中。 ', '【对话】他们发现山洞深处有一条暗道。\"李剑雨拉着林月如。\"林月如惊讶地看着那些器具。\"李剑雨突然拉住林月如。 主要角色：李、林月如 关键词：剑雨、林月如、李', '2025-04-20 20:57:45', '2025-04-20 20:57:45', 778);

-- ----------------------------
-- Table structure for character_dialogues
-- ----------------------------
DROP TABLE IF EXISTS `character_dialogues`;
CREATE TABLE `character_dialogues`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `chapter_id` bigint NOT NULL,
  `character_id` bigint NOT NULL,
  `dialogue_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `dialogue_position` int NULL DEFAULT NULL,
  `sentiment` float NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `context` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `emotion` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `importance` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `novel_id`(`novel_id` ASC) USING BTREE,
  INDEX `chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `character_id`(`character_id` ASC) USING BTREE,
  CONSTRAINT `character_dialogues_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `character_dialogues_ibfk_2` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `character_dialogues_ibfk_3` FOREIGN KEY (`character_id`) REFERENCES `novel_characters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of character_dialogues
-- ----------------------------

-- ----------------------------
-- Table structure for character_relationships
-- ----------------------------
DROP TABLE IF EXISTS `character_relationships`;
CREATE TABLE `character_relationships`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `source_character_id` bigint NOT NULL,
  `target_character_id` bigint NOT NULL,
  `relationship_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `importance` int NULL DEFAULT 1,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `novel_id`(`novel_id` ASC) USING BTREE,
  INDEX `source_character_id`(`source_character_id` ASC) USING BTREE,
  INDEX `target_character_id`(`target_character_id` ASC) USING BTREE,
  CONSTRAINT `character_relationships_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `character_relationships_ibfk_2` FOREIGN KEY (`source_character_id`) REFERENCES `novel_characters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `character_relationships_ibfk_3` FOREIGN KEY (`target_character_id`) REFERENCES `novel_characters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of character_relationships
-- ----------------------------
INSERT INTO `character_relationships` VALUES (1, 7, 9, 10, '合作', 50, '合作', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `character_relationships` VALUES (2, 7, 10, 9, '合作', 50, '合作', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `character_relationships` VALUES (3, 7, 9, 11, '认识', 50, '认识', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `character_relationships` VALUES (4, 7, 11, 9, '认识', 50, '认识', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `character_relationships` VALUES (5, 7, 10, 11, '合作', 50, '合作', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `character_relationships` VALUES (6, 7, 11, 10, '合作', 50, '合作', '2025-04-20 20:57:46', '2025-04-20 20:57:46');

-- ----------------------------
-- Table structure for novel_characters
-- ----------------------------
DROP TABLE IF EXISTS `novel_characters`;
CREATE TABLE `novel_characters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `importance` int NULL DEFAULT 50,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `novel_id`(`novel_id` ASC) USING BTREE,
  CONSTRAINT `novel_characters_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of novel_characters
-- ----------------------------
INSERT INTO `novel_characters` VALUES (1, 5, '李', 100, NULL, '小说中的角色：李', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `novel_characters` VALUES (2, 5, '林月如', 95, NULL, '小说中的角色：林月如', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `novel_characters` VALUES (3, 5, '门规', 90, NULL, '小说中的角色：门规', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `novel_characters` VALUES (4, 5, '双刀', 85, NULL, '小说中的角色：双刀', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `novel_characters` VALUES (5, 6, '李', 100, '主要角色', '小说中的角色：李', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `novel_characters` VALUES (6, 6, '林月如', 95, '主要角色', '小说中的角色：林月如', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `novel_characters` VALUES (7, 6, '门规', 90, '主要角色', '小说中的角色：门规', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `novel_characters` VALUES (8, 6, '双刀', 85, '主要角色', '小说中的角色：双刀', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `novel_characters` VALUES (9, 7, '林月如', 95, '主要角色', '小说中的角色：林月如', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `novel_characters` VALUES (10, 7, '门规', 90, '主要角色', '小说中的角色：门规', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `novel_characters` VALUES (11, 7, '双刀', 85, '主要角色', '小说中的角色：双刀', '2025-04-20 20:57:46', '2025-04-20 20:57:46');

-- ----------------------------
-- Table structure for novels
-- ----------------------------
DROP TABLE IF EXISTS `novels`;
CREATE TABLE `novels`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `author_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `source_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `processing_status` enum('PENDING','PROCESSING','COMPLETED','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `processed_chapters` int NULL DEFAULT 0,
  `total_chapters` int NULL DEFAULT 0,
  `overall_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `world_building_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `character_development_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `plot_progression_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_novels_title`(`title` ASC) USING BTREE,
  INDEX `idx_novels_author`(`author_name` ASC) USING BTREE,
  INDEX `idx_novels_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_novels_status`(`processing_status` ASC) USING BTREE,
  INDEX `idx_novels_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `novels_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of novels
-- ----------------------------
INSERT INTO `novels` VALUES (1, '三体', '刘慈欣', NULL, '地球文明向宇宙发出了一条广播，引发了外星文明对地球的入侵计划，人类文明面临生死存亡的挑战...', 1, 'COMPLETED', 40, 40, NULL, NULL, NULL, NULL, '2025-03-21 20:43:17', '2025-04-20 20:43:17');
INSERT INTO `novels` VALUES (2, '红楼梦', '曹雪芹', NULL, '中国古典小说，以贾、史、王、薛四大家族的兴衰为背景，讲述了贾宝玉和林黛玉、薛宝钗的爱情故事...', 1, 'COMPLETED', 120, 120, NULL, NULL, NULL, NULL, '2025-03-26 20:43:17', '2025-04-20 20:43:17');
INSERT INTO `novels` VALUES (3, '哈利·波特与魔法石', 'J.K.罗琳', NULL, '哈利·波特在11岁生日时得知自己是一个巫师，开始了魔法世界的冒险旅程...', 2, 'COMPLETED', 17, 17, NULL, NULL, NULL, NULL, '2025-04-05 20:43:17', '2025-04-20 20:43:17');
INSERT INTO `novels` VALUES (4, '西游记', '吴承恩', NULL, '唐僧师徒四人历经九九八十一难，取得真经的故事...', 2, 'PROCESSING', 50, 100, NULL, NULL, NULL, NULL, '2025-04-15 20:43:17', '2025-04-20 20:43:17');
INSERT INTO `novels` VALUES (5, '测试1', '测试1', NULL, '《测试1》，作者: 测试1，共5章', 1, 'COMPLETED', 5, 5, '【对话】李剑雨面对的是黑风寨的三当家。\"李剑雨拉着林月如。\"李剑雨突然拉住林月如。\"李剑雨心中一震。李剑雨饶有兴趣地观察着这位女子。李剑雨走向石台。李剑雨看得出女子武功不俗。李剑雨脑中闪过师父教导的最后一招\"青云逆命\"。\"李剑雨低声问道。李剑雨站在山顶。 主要角色：李、林月如、门规 关键词：剑雨、林月如、李', '【世界观分析】\n小说中的世界背景描述。', '主要角色：李、林月如、门规、双刀\n\n李：【短章】李剑雨面对的是黑风寨的三当家。李剑雨饶有兴趣地观察着这位女子。李剑雨神色凝重。 主要角色：李、双刀 关键词：剑雨、初入、李\n\n林月如：【对话】\"李剑雨突然拉住林月如。林月如心中一惊。叫林月如。 主要角色：林月如、李 关键词：林、林月如、剑雨\n\n门规：青云门虽是名门正派，但门规森严，不允许弟子私自复仇。她不知道该如何选择：是遵循门规，还是完成父亲的遗愿。\"张小凡望着远处的山峦，\"无论门规如何，最重要的是不辜负自己的内心。\n\n双刀：李剑雨面对的是黑风寨的三当家，一个使双刀的精壮汉子。\"三当家手中双刀挥舞如风，向李剑雨劈来。\n\n', '【剧情分析】\n小说中的主要情节发展和转折点。', '2025-04-20 20:44:47', '2025-04-20 20:44:48');
INSERT INTO `novels` VALUES (6, '测试2', '测试2', NULL, '《测试2》，作者: 测试2，共5章', 1, 'COMPLETED', 5, 5, '【对话】李剑雨面对的是黑风寨的三当家。\"李剑雨拉着林月如。\"李剑雨突然拉住林月如。\"李剑雨心中一震。李剑雨饶有兴趣地观察着这位女子。李剑雨走向石台。李剑雨看得出女子武功不俗。李剑雨脑中闪过师父教导的最后一招\"青云逆命\"。\"李剑雨低声问道。李剑雨站在山顶。 主要角色：李、林月如、门规 关键词：剑雨、林月如、李', '【世界观分析】\n小说中的世界背景描述。', '主要角色：李、林月如、门规、双刀\n\n李：【短章】李剑雨面对的是黑风寨的三当家。李剑雨饶有兴趣地观察着这位女子。李剑雨神色凝重。 主要角色：李、双刀 关键词：剑雨、初入、李\n\n林月如：【对话】\"李剑雨突然拉住林月如。林月如心中一惊。叫林月如。 主要角色：林月如、李 关键词：林、林月如、剑雨\n\n门规：青云门虽是名门正派，但门规森严，不允许弟子私自复仇。她不知道该如何选择：是遵循门规，还是完成父亲的遗愿。\"张小凡望着远处的山峦，\"无论门规如何，最重要的是不辜负自己的内心。\n\n双刀：李剑雨面对的是黑风寨的三当家，一个使双刀的精壮汉子。\"三当家手中双刀挥舞如风，向李剑雨劈来。\n\n', '【剧情分析】\n小说中的主要情节发展和转折点。', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `novels` VALUES (7, '测试3', '测试3', NULL, '《测试3》，作者: 测试3，共5章', 1, 'COMPLETED', 5, 5, '【对话】李剑雨面对的是黑风寨的三当家。\"李剑雨拉着林月如。\"李剑雨突然拉住林月如。\"李剑雨心中一震。李剑雨饶有兴趣地观察着这位女子。李剑雨走向石台。李剑雨看得出女子武功不俗。李剑雨脑中闪过师父教导的最后一招\"青云逆命\"。\"李剑雨低声问道。李剑雨站在山顶。 主要角色：李、林月如、门规 关键词：剑雨、林月如、李', '【世界观分析】\n小说中的世界背景描述。', '主要角色：李、林月如、门规、双刀\n\n李：【短章】李剑雨面对的是黑风寨的三当家。李剑雨饶有兴趣地观察着这位女子。李剑雨神色凝重。 主要角色：李、双刀 关键词：剑雨、初入、李\n\n林月如：【对话】\"李剑雨突然拉住林月如。林月如心中一惊。叫林月如。 主要角色：林月如、李 关键词：林、林月如、剑雨\n\n门规：青云门虽是名门正派，但门规森严，不允许弟子私自复仇。她不知道该如何选择：是遵循门规，还是完成父亲的遗愿。\"张小凡望着远处的山峦，\"无论门规如何，最重要的是不辜负自己的内心。\n\n双刀：李剑雨面对的是黑风寨的三当家，一个使双刀的精壮汉子。\"三当家手中双刀挥舞如风，向李剑雨劈来。\n\n', '【剧情分析】\n小说中的主要情节发展和转折点。', '2025-04-20 20:57:45', '2025-04-20 20:57:46');

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` enum('ROLE_USER','ROLE_ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, 'ROLE_USER');
INSERT INTO `roles` VALUES (2, 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for system_logs
-- ----------------------------
DROP TABLE IF EXISTS `system_logs`;
CREATE TABLE `system_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `logger_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `thread_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `stack_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_logs
-- ----------------------------
INSERT INTO `system_logs` VALUES (1, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/15', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-1', NULL, '{\"args\":{\"id\":\"15\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/15\"}', '2025-04-20 20:44:05');
INSERT INTO `system_logs` VALUES (2, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/15', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-2', NULL, '{\"args\":{\"id\":\"15\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/15\"}', '2025-04-20 20:44:06');
INSERT INTO `system_logs` VALUES (3, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/15', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-2', NULL, '{\"args\":{\"id\":\"15\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/15\"}', '2025-04-20 20:44:31');
INSERT INTO `system_logs` VALUES (4, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-4', NULL, '{\"args\":{\"userId\":\"null\"},\"method\":\"getNovelList\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels\"}', '2025-04-20 20:44:33');
INSERT INTO `system_logs` VALUES (5, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/user/novels', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-6', NULL, '{\"args\":{\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@52c38a9c\"},\"method\":\"getUserNovels\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.UserController\",\"url\":\"http://localhost:8080/user/novels\"}', '2025-04-20 20:44:36');
INSERT INTO `system_logs` VALUES (6, 'INFO', 'API_REQUEST', 'API请求 - POST http://localhost:8080/novels/upload', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-7', NULL, '{\"args\":{\"file\":\"org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@6c139bde\",\"author\":\"测试1\",\"title\":\"测试1\",\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@5a2efefd\"},\"method\":\"uploadNovel\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"POST\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/upload\"}', '2025-04-20 20:44:47');
INSERT INTO `system_logs` VALUES (7, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/api/novels/5', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-1', NULL, '{\"args\":{\"id\":\"5\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/api/novels/5\"}', '2025-04-20 20:44:48');
INSERT INTO `system_logs` VALUES (8, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/visualization/5/all', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-2', NULL, '{\"args\":{\"novelId\":\"5\"},\"method\":\"getAllVisualizationData\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.VisualizationController\",\"url\":\"http://localhost:8080/novels/visualization/5/all\"}', '2025-04-20 20:44:48');
INSERT INTO `system_logs` VALUES (9, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/5/status', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-4', NULL, '{\"args\":{\"id\":\"5\"},\"method\":\"getNovelStatus\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/5/status\"}', '2025-04-20 20:44:50');
INSERT INTO `system_logs` VALUES (10, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/5', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-6', NULL, '{\"args\":{\"id\":\"5\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/5\"}', '2025-04-20 20:44:50');
INSERT INTO `system_logs` VALUES (11, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/user/novels', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-7', NULL, '{\"args\":{\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@148f55de\"},\"method\":\"getUserNovels\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.UserController\",\"url\":\"http://localhost:8080/user/novels\"}', '2025-04-20 20:44:53');
INSERT INTO `system_logs` VALUES (12, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/5', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-9', NULL, '{\"args\":{\"id\":\"5\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/5\"}', '2025-04-20 20:44:55');
INSERT INTO `system_logs` VALUES (13, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/visualization/5/all', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-1', NULL, '{\"args\":{\"novelId\":\"5\"},\"method\":\"getAllVisualizationData\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.VisualizationController\",\"url\":\"http://localhost:8080/novels/visualization/5/all\"}', '2025-04-20 20:44:55');
INSERT INTO `system_logs` VALUES (14, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-3', NULL, '{\"args\":{\"userId\":\"null\"},\"method\":\"getNovelList\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels\"}', '2025-04-20 20:45:42');
INSERT INTO `system_logs` VALUES (15, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/user/novels', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-5', NULL, '{\"args\":{\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@6ac3586e\"},\"method\":\"getUserNovels\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.UserController\",\"url\":\"http://localhost:8080/user/novels\"}', '2025-04-20 20:45:44');
INSERT INTO `system_logs` VALUES (16, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/2', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-8', NULL, '{\"args\":{\"id\":\"2\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/2\"}', '2025-04-20 20:45:46');
INSERT INTO `system_logs` VALUES (17, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/visualization/2/all', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-9', NULL, '{\"args\":{\"novelId\":\"2\"},\"method\":\"getAllVisualizationData\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.VisualizationController\",\"url\":\"http://localhost:8080/novels/visualization/2/all\"}', '2025-04-20 20:45:46');
INSERT INTO `system_logs` VALUES (18, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/user/novels', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-3', NULL, '{\"args\":{\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@78379787\"},\"method\":\"getUserNovels\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.UserController\",\"url\":\"http://localhost:8080/user/novels\"}', '2025-04-20 20:49:08');
INSERT INTO `system_logs` VALUES (19, 'INFO', 'API_REQUEST', 'API请求 - POST http://localhost:8080/novels/upload', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-4', NULL, '{\"args\":{\"file\":\"org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@1fb36ef5\",\"author\":\"测试2\",\"title\":\"测试2\",\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@476b32\"},\"method\":\"uploadNovel\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"POST\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/upload\"}', '2025-04-20 20:49:24');
INSERT INTO `system_logs` VALUES (20, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/api/novels/6', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-8', NULL, '{\"args\":{\"id\":\"6\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/api/novels/6\"}', '2025-04-20 20:49:25');
INSERT INTO `system_logs` VALUES (21, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/visualization/6/all', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-6', NULL, '{\"args\":{\"novelId\":\"6\"},\"method\":\"getAllVisualizationData\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.VisualizationController\",\"url\":\"http://localhost:8080/novels/visualization/6/all\"}', '2025-04-20 20:49:25');
INSERT INTO `system_logs` VALUES (22, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/6/status', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-10', NULL, '{\"args\":{\"id\":\"6\"},\"method\":\"getNovelStatus\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/6/status\"}', '2025-04-20 20:49:26');
INSERT INTO `system_logs` VALUES (23, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/6', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-3', NULL, '{\"args\":{\"id\":\"6\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/6\"}', '2025-04-20 20:49:27');
INSERT INTO `system_logs` VALUES (24, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/user/novels', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-4', NULL, '{\"args\":{\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@c52cb90\"},\"method\":\"getUserNovels\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.UserController\",\"url\":\"http://localhost:8080/user/novels\"}', '2025-04-20 20:49:37');
INSERT INTO `system_logs` VALUES (25, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/6', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-5', NULL, '{\"args\":{\"id\":\"6\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/6\"}', '2025-04-20 20:49:39');
INSERT INTO `system_logs` VALUES (26, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/visualization/6/all', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-6', NULL, '{\"args\":{\"novelId\":\"6\"},\"method\":\"getAllVisualizationData\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.VisualizationController\",\"url\":\"http://localhost:8080/novels/visualization/6/all\"}', '2025-04-20 20:49:39');
INSERT INTO `system_logs` VALUES (27, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/6', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-3', NULL, '{\"args\":{\"id\":\"6\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/6\"}', '2025-04-20 20:57:23');
INSERT INTO `system_logs` VALUES (28, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/visualization/6/all', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-2', NULL, '{\"args\":{\"novelId\":\"6\"},\"method\":\"getAllVisualizationData\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.VisualizationController\",\"url\":\"http://localhost:8080/novels/visualization/6/all\"}', '2025-04-20 20:57:23');
INSERT INTO `system_logs` VALUES (29, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/user/novels', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-6', NULL, '{\"args\":{\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@55924e72\"},\"method\":\"getUserNovels\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.UserController\",\"url\":\"http://localhost:8080/user/novels\"}', '2025-04-20 20:57:33');
INSERT INTO `system_logs` VALUES (30, 'INFO', 'API_REQUEST', 'API请求 - POST http://localhost:8080/novels/upload', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-8', NULL, '{\"args\":{\"file\":\"org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@10856e08\",\"author\":\"测试3\",\"title\":\"测试3\",\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@234f81b\"},\"method\":\"uploadNovel\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"POST\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/upload\"}', '2025-04-20 20:57:45');
INSERT INTO `system_logs` VALUES (31, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/api/novels/7', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-1', NULL, '{\"args\":{\"id\":\"7\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/api/novels/7\"}', '2025-04-20 20:57:46');
INSERT INTO `system_logs` VALUES (32, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/visualization/7/all', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-3', NULL, '{\"args\":{\"novelId\":\"7\"},\"method\":\"getAllVisualizationData\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.VisualizationController\",\"url\":\"http://localhost:8080/novels/visualization/7/all\"}', '2025-04-20 20:57:46');
INSERT INTO `system_logs` VALUES (33, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/7/status', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-2', NULL, '{\"args\":{\"id\":\"7\"},\"method\":\"getNovelStatus\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/7/status\"}', '2025-04-20 20:57:48');
INSERT INTO `system_logs` VALUES (34, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/7', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-6', NULL, '{\"args\":{\"id\":\"7\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/7\"}', '2025-04-20 20:57:48');
INSERT INTO `system_logs` VALUES (35, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/user/novels', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-8', NULL, '{\"args\":{\"userDetails\":\"com.novelassistant.security.services.UserDetailsImpl@29cf6631\"},\"method\":\"getUserNovels\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.UserController\",\"url\":\"http://localhost:8080/user/novels\"}', '2025-04-20 20:57:54');
INSERT INTO `system_logs` VALUES (36, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/7', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-10', NULL, '{\"args\":{\"id\":\"7\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/7\"}', '2025-04-20 20:57:56');
INSERT INTO `system_logs` VALUES (37, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/visualization/7/all', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-3', NULL, '{\"args\":{\"novelId\":\"7\"},\"method\":\"getAllVisualizationData\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.VisualizationController\",\"url\":\"http://localhost:8080/novels/visualization/7/all\"}', '2025-04-20 20:57:56');
INSERT INTO `system_logs` VALUES (38, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/7', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-3', NULL, '{\"args\":{\"id\":\"7\"},\"method\":\"getNovelDetail\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.NovelController\",\"url\":\"http://localhost:8080/novels/7\"}', '2025-04-20 21:09:44');
INSERT INTO `system_logs` VALUES (39, 'INFO', 'API_REQUEST', 'API请求 - GET http://localhost:8080/novels/visualization/7/all', '1', '0:0:0:0:0:0:0:1', 'http-nio-8080-exec-6', NULL, '{\"args\":{\"novelId\":\"7\"},\"method\":\"getAllVisualizationData\",\"ip\":\"0:0:0:0:0:0:0:1\",\"httpMethod\":\"GET\",\"class\":\"com.novelassistant.controller.VisualizationController\",\"url\":\"http://localhost:8080/novels/visualization/7/all\"}', '2025-04-20 21:09:45');

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('POSITIVE','WARNING','INFO') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tags_novel_id`(`novel_id` ASC) USING BTREE,
  INDEX `idx_tags_name`(`name` ASC) USING BTREE,
  INDEX `idx_tags_type`(`type` ASC) USING BTREE,
  CONSTRAINT `tags_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tags
-- ----------------------------
INSERT INTO `tags` VALUES (1, 1, '科幻', 'INFO', '2025-03-21 20:43:17', '2025-03-21 20:43:17');
INSERT INTO `tags` VALUES (2, 1, '硬科幻', 'INFO', '2025-03-21 20:43:17', '2025-03-21 20:43:17');
INSERT INTO `tags` VALUES (3, 1, '宇宙社会学', 'INFO', '2025-03-21 20:43:17', '2025-03-21 20:43:17');
INSERT INTO `tags` VALUES (4, 1, '深刻的哲学思考', 'POSITIVE', '2025-03-21 20:43:17', '2025-03-21 20:43:17');
INSERT INTO `tags` VALUES (5, 2, '古典文学', 'INFO', '2025-03-26 20:43:17', '2025-03-26 20:43:17');
INSERT INTO `tags` VALUES (6, 2, '家族史诗', 'INFO', '2025-03-26 20:43:17', '2025-03-26 20:43:17');
INSERT INTO `tags` VALUES (7, 2, '优美的文笔', 'POSITIVE', '2025-03-26 20:43:17', '2025-03-26 20:43:17');
INSERT INTO `tags` VALUES (8, 2, '篇幅长', 'WARNING', '2025-03-26 20:43:17', '2025-03-26 20:43:17');
INSERT INTO `tags` VALUES (9, 3, '魔法', 'INFO', '2025-04-05 20:43:17', '2025-04-05 20:43:17');
INSERT INTO `tags` VALUES (10, 3, '成长', 'INFO', '2025-04-05 20:43:17', '2025-04-05 20:43:17');
INSERT INTO `tags` VALUES (11, 3, '友情', 'POSITIVE', '2025-04-05 20:43:17', '2025-04-05 20:43:17');
INSERT INTO `tags` VALUES (12, 4, '神话', 'INFO', '2025-04-15 20:43:17', '2025-04-15 20:43:17');
INSERT INTO `tags` VALUES (13, 4, '佛教', 'INFO', '2025-04-15 20:43:17', '2025-04-15 20:43:17');
INSERT INTO `tags` VALUES (14, 4, '冒险', 'POSITIVE', '2025-04-15 20:43:17', '2025-04-15 20:43:17');
INSERT INTO `tags` VALUES (15, 5, '剑雨', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (16, 5, '林月如', 'WARNING', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (17, 5, '初入', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (18, 5, '李', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (19, 5, '林', 'WARNING', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (20, 5, '会', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (21, 5, '门规', 'WARNING', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (22, 5, '\r\n\r\n李剑雨', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (23, 5, '李剑雨', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (24, 5, '黑风寨', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (25, 5, '角色形象鲜明', 'POSITIVE', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (26, 5, '短篇', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (27, 5, '篇幅较短', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (28, 5, '章节简短', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (29, 5, '轻松易读', 'POSITIVE', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (30, 5, '儿童读物', 'INFO', '2025-04-20 20:44:48', '2025-04-20 20:44:48');
INSERT INTO `tags` VALUES (31, 6, '剑雨', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (32, 6, '林月如', 'WARNING', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (33, 6, '初入', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (34, 6, '李', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (35, 6, '林', 'WARNING', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (36, 6, '会', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (37, 6, '门规', 'WARNING', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (38, 6, '\r\n\r\n李剑雨', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (39, 6, '李剑雨', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (40, 6, '黑风寨', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (41, 6, '角色形象鲜明', 'POSITIVE', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (42, 6, '短篇', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (43, 6, '篇幅较短', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (44, 6, '章节简短', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (45, 6, '轻松易读', 'POSITIVE', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (46, 6, '儿童读物', 'INFO', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `tags` VALUES (47, 7, '剑雨', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (48, 7, '林月如', 'WARNING', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (49, 7, '初入', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (50, 7, '李', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (51, 7, '林', 'WARNING', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (52, 7, '会', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (53, 7, '门规', 'WARNING', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (54, 7, '\r\n\r\n李剑雨', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (55, 7, '李剑雨', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (56, 7, '黑风寨', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (57, 7, '角色形象鲜明', 'POSITIVE', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (58, 7, '短篇', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (59, 7, '篇幅较短', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (60, 7, '章节简短', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (61, 7, '轻松易读', 'POSITIVE', '2025-04-20 20:57:46', '2025-04-20 20:57:46');
INSERT INTO `tags` VALUES (62, 7, '儿童读物', 'INFO', '2025-04-20 20:57:46', '2025-04-20 20:57:46');

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, 1);
INSERT INTO `user_roles` VALUES (2, 1);
INSERT INTO `user_roles` VALUES (1, 2);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `enabled` tinyint(1) NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_login_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `UKr43af9ap4edm43mmtq01oddj6`(`username` ASC) USING BTREE,
  UNIQUE INDEX `UK6dotkott2kjsp8vw4d0m25fb7`(`email` ASC) USING BTREE,
  INDEX `idx_users_username`(`username` ASC) USING BTREE,
  INDEX `idx_users_email`(`email` ASC) USING BTREE,
  INDEX `idx_users_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_users_last_login_at`(`last_login_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$tfIztCUbVHQ5wgyM3coVd.J1zA/Oj1E99kmT4E2U/VEjUZSE0fLN6', 'admin@novelinsight.com', '系统管理员', 1, '2025-04-20 20:43:17', '2025-04-20 20:44:26', NULL);
INSERT INTO `users` VALUES (2, 'user', '$2a$10$VRWVZYeGnqqyeVEgNotkuOaVyVKjw6gVBCgJJO88s.nrZsDmB4Pfa', 'user@novelinsight.com', '普通用户', 1, '2025-04-20 20:43:17', '2025-04-20 20:43:17', NULL);

-- ----------------------------
-- Table structure for visualization_cache
-- ----------------------------
DROP TABLE IF EXISTS `visualization_cache`;
CREATE TABLE `visualization_cache`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `visualization_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_json` json NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `expires_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `novel_vis_type_idx`(`novel_id` ASC, `visualization_type` ASC) USING BTREE,
  INDEX `idx_novel_id`(`novel_id` ASC) USING BTREE,
  INDEX `idx_type`(`visualization_type` ASC) USING BTREE,
  CONSTRAINT `visualization_cache_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visualization_cache
-- ----------------------------

-- ----------------------------
-- Table structure for visualization_emotional_data
-- ----------------------------
DROP TABLE IF EXISTS `visualization_emotional_data`;
CREATE TABLE `visualization_emotional_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `chapter_id` bigint NULL DEFAULT NULL,
  `chapter_number` int NOT NULL,
  `chapter_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `emotion_value` double NOT NULL,
  `is_important` tinyint(1) NULL DEFAULT 0,
  `is_climax_start` tinyint(1) NULL DEFAULT 0,
  `is_climax_end` tinyint(1) NULL DEFAULT 0,
  `event_description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_novel_id`(`novel_id` ASC) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `idx_chapter_number`(`chapter_number` ASC) USING BTREE,
  CONSTRAINT `visualization_emotional_data_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `visualization_emotional_data_ibfk_2` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visualization_emotional_data
-- ----------------------------
INSERT INTO `visualization_emotional_data` VALUES (1, 5, 9, 1, '第一章 初入江湖', 66.66666666666666, 0, 0, 0, '【动作】师父说江湖险恶。第一章 初入江湖。为父母报仇了。李剑雨整理了一下行囊。 主要角色：李 关键词：江湖、初入、师父', '2025-04-20 20:44:47', '2025-04-20 20:44:47');
INSERT INTO `visualization_emotional_data` VALUES (2, 5, 10, 2, '第二章 酒馆相遇', 65, 1, 0, 0, '【对话】李剑雨饶有兴趣地观察着这位女子。再来一壶酒。\"醉汉伸手就要去拉女子的手腕。李剑雨看得出女子武功不俗。 主要角色：李 关键词：女子、剑雨、\r\n\r\n\"', '2025-04-20 20:44:47', '2025-04-20 20:44:47');
INSERT INTO `visualization_emotional_data` VALUES (3, 5, 11, 3, '第三章 生死一战', 65, 0, 0, 0, '【对话】李剑雨面对的是黑风寨的三当家。剑招越发凌厉。\"李剑雨强撑着走到三当家面前。李剑雨神色凝重。 主要角色：李、双刀 关键词：剑雨、李、\r\n\r\n\"', '2025-04-20 20:44:47', '2025-04-20 20:44:47');
INSERT INTO `visualization_emotional_data` VALUES (4, 5, 12, 4, '第四章 心之挣扎', 25, 1, 0, 0, '【对话】林月如望着师弟。是不是又在想复仇的事。林月如沉默了片刻。林月如感到一种前所未有的孤独和迷茫。青云门虽是名门正派，但门规森严，不允许弟子私自复仇。 本章氛围紧张压抑。 主要角色：门规、李 关键词：林、门规、林月如', '2025-04-20 20:44:47', '2025-04-20 20:44:47');
INSERT INTO `visualization_emotional_data` VALUES (5, 5, 13, 5, '第五章 联手探险', 50, 1, 0, 0, '【对话】他们发现山洞深处有一条暗道。\"李剑雨拉着林月如。\"林月如惊讶地看着那些器具。\"李剑雨突然拉住林月如。 主要角色：李、林月如 关键词：剑雨、林月如、李', '2025-04-20 20:44:47', '2025-04-20 20:44:47');
INSERT INTO `visualization_emotional_data` VALUES (6, 6, 14, 1, '第一章 初入江湖', 66.66666666666666, 0, 0, 0, '【动作】师父说江湖险恶。第一章 初入江湖。为父母报仇了。李剑雨整理了一下行囊。 主要角色：李 关键词：江湖、初入、师父', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `visualization_emotional_data` VALUES (7, 6, 15, 2, '第二章 酒馆相遇', 65, 1, 0, 0, '【对话】李剑雨饶有兴趣地观察着这位女子。再来一壶酒。\"醉汉伸手就要去拉女子的手腕。李剑雨看得出女子武功不俗。 主要角色：李 关键词：女子、剑雨、\r\n\r\n\"', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `visualization_emotional_data` VALUES (8, 6, 16, 3, '第三章 生死一战', 65, 0, 0, 0, '【对话】李剑雨面对的是黑风寨的三当家。剑招越发凌厉。\"李剑雨强撑着走到三当家面前。李剑雨神色凝重。 主要角色：李、双刀 关键词：剑雨、李、\r\n\r\n\"', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `visualization_emotional_data` VALUES (9, 6, 17, 4, '第四章 心之挣扎', 25, 1, 0, 0, '【对话】林月如望着师弟。是不是又在想复仇的事。林月如沉默了片刻。林月如感到一种前所未有的孤独和迷茫。青云门虽是名门正派，但门规森严，不允许弟子私自复仇。 本章氛围紧张压抑。 主要角色：门规、李 关键词：林、门规、林月如', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `visualization_emotional_data` VALUES (10, 6, 18, 5, '第五章 联手探险', 50, 1, 0, 0, '【对话】他们发现山洞深处有一条暗道。\"李剑雨拉着林月如。\"林月如惊讶地看着那些器具。\"李剑雨突然拉住林月如。 主要角色：李、林月如 关键词：剑雨、林月如、李', '2025-04-20 20:49:24', '2025-04-20 20:49:24');
INSERT INTO `visualization_emotional_data` VALUES (11, 7, 19, 1, '第一章 初入江湖', 66.66666666666666, 0, 0, 0, '【动作】师父说江湖险恶。第一章 初入江湖。为父母报仇了。李剑雨整理了一下行囊。 主要角色：李 关键词：江湖、初入、师父', '2025-04-20 20:57:45', '2025-04-20 20:57:45');
INSERT INTO `visualization_emotional_data` VALUES (12, 7, 20, 2, '第二章 酒馆相遇', 65, 1, 0, 0, '【对话】李剑雨饶有兴趣地观察着这位女子。再来一壶酒。\"醉汉伸手就要去拉女子的手腕。李剑雨看得出女子武功不俗。 主要角色：李 关键词：女子、剑雨、\r\n\r\n\"', '2025-04-20 20:57:45', '2025-04-20 20:57:45');
INSERT INTO `visualization_emotional_data` VALUES (13, 7, 21, 3, '第三章 生死一战', 65, 0, 0, 0, '【对话】李剑雨面对的是黑风寨的三当家。剑招越发凌厉。\"李剑雨强撑着走到三当家面前。李剑雨神色凝重。 主要角色：李、双刀 关键词：剑雨、李、\r\n\r\n\"', '2025-04-20 20:57:45', '2025-04-20 20:57:45');
INSERT INTO `visualization_emotional_data` VALUES (14, 7, 22, 4, '第四章 心之挣扎', 25, 1, 0, 0, '【对话】林月如望着师弟。是不是又在想复仇的事。林月如沉默了片刻。林月如感到一种前所未有的孤独和迷茫。青云门虽是名门正派，但门规森严，不允许弟子私自复仇。 本章氛围紧张压抑。 主要角色：门规、李 关键词：林、门规、林月如', '2025-04-20 20:57:45', '2025-04-20 20:57:45');
INSERT INTO `visualization_emotional_data` VALUES (15, 7, 23, 5, '第五章 联手探险', 50, 1, 0, 0, '【对话】他们发现山洞深处有一条暗道。\"李剑雨拉着林月如。\"林月如惊讶地看着那些器具。\"李剑雨突然拉住林月如。 主要角色：李、林月如 关键词：剑雨、林月如、李', '2025-04-20 20:57:45', '2025-04-20 20:57:45');

-- ----------------------------
-- Table structure for visualization_keywords
-- ----------------------------
DROP TABLE IF EXISTS `visualization_keywords`;
CREATE TABLE `visualization_keywords`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `weight` int NOT NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `novel_keyword_idx`(`novel_id` ASC, `keyword` ASC) USING BTREE,
  INDEX `idx_novel_id`(`novel_id` ASC) USING BTREE,
  INDEX `idx_keyword`(`keyword` ASC) USING BTREE,
  CONSTRAINT `visualization_keywords_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visualization_keywords
-- ----------------------------

-- ----------------------------
-- Table structure for visualization_structure_data
-- ----------------------------
DROP TABLE IF EXISTS `visualization_structure_data`;
CREATE TABLE `visualization_structure_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `section_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `percentage` double NOT NULL,
  `start_chapter` int NOT NULL,
  `end_chapter` int NOT NULL,
  `chapter_count` int NOT NULL,
  `description` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_novel_id`(`novel_id` ASC) USING BTREE,
  CONSTRAINT `visualization_structure_data_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visualization_structure_data
-- ----------------------------

-- ----------------------------
-- Table structure for visualization_word_count_data
-- ----------------------------
DROP TABLE IF EXISTS `visualization_word_count_data`;
CREATE TABLE `visualization_word_count_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `novel_id` bigint NOT NULL,
  `range_start` int NOT NULL,
  `range_end` int NOT NULL,
  `chapter_count` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_novel_id`(`novel_id` ASC) USING BTREE,
  CONSTRAINT `visualization_word_count_data_ibfk_1` FOREIGN KEY (`novel_id`) REFERENCES `novels` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visualization_word_count_data
-- ----------------------------

-- ----------------------------
-- View structure for novel_details
-- ----------------------------
DROP VIEW IF EXISTS `novel_details`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `novel_details` AS select `n`.`id` AS `id`,`n`.`title` AS `title`,`n`.`author_name` AS `author_name`,`n`.`description` AS `description`,`n`.`processing_status` AS `processing_status`,`n`.`processed_chapters` AS `processed_chapters`,`n`.`total_chapters` AS `total_chapters`,`u`.`username` AS `uploaded_by`,`n`.`created_at` AS `created_at`,`n`.`updated_at` AS `updated_at`,(select count(0) from `chapters` `c` where (`c`.`novel_id` = `n`.`id`)) AS `chapter_count`,(select count(0) from `tags` `t` where (`t`.`novel_id` = `n`.`id`)) AS `tag_count` from (`novels` `n` left join `users` `u` on((`n`.`user_id` = `u`.`id`)));

-- ----------------------------
-- View structure for novel_tag_stats
-- ----------------------------
DROP VIEW IF EXISTS `novel_tag_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `novel_tag_stats` AS select `n`.`id` AS `novel_id`,`n`.`title` AS `title`,count((case when (`t`.`type` = 'POSITIVE') then 1 end)) AS `positive_tag_count`,count((case when (`t`.`type` = 'WARNING') then 1 end)) AS `warning_tag_count`,count((case when (`t`.`type` = 'INFO') then 1 end)) AS `info_tag_count`,group_concat(distinct (case when (`t`.`type` = 'POSITIVE') then `t`.`name` end) separator ',') AS `positive_tags`,group_concat(distinct (case when (`t`.`type` = 'WARNING') then `t`.`name` end) separator ',') AS `warning_tags`,group_concat(distinct (case when (`t`.`type` = 'INFO') then `t`.`name` end) separator ',') AS `info_tags` from (`novels` `n` left join `tags` `t` on((`n`.`id` = `t`.`novel_id`))) group by `n`.`id`;

-- ----------------------------
-- View structure for user_details
-- ----------------------------
DROP VIEW IF EXISTS `user_details`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `user_details` AS select `u`.`id` AS `id`,`u`.`username` AS `username`,`u`.`email` AS `email`,`u`.`nickname` AS `nickname`,`u`.`enabled` AS `enabled`,`u`.`created_at` AS `created_at`,`u`.`updated_at` AS `updated_at`,`u`.`last_login_at` AS `last_login_at`,group_concat(`r`.`name` separator ',') AS `roles` from ((`users` `u` left join `user_roles` `ur` on((`u`.`id` = `ur`.`user_id`))) left join `roles` `r` on((`ur`.`role_id` = `r`.`id`))) group by `u`.`id`;

-- ----------------------------
-- View structure for user_upload_stats
-- ----------------------------
DROP VIEW IF EXISTS `user_upload_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `user_upload_stats` AS select `u`.`id` AS `user_id`,`u`.`username` AS `username`,count(`n`.`id`) AS `total_novels`,count((case when (`n`.`processing_status` = 'COMPLETED') then 1 end)) AS `completed_novels`,count((case when (`n`.`processing_status` = 'PROCESSING') then 1 end)) AS `processing_novels`,count((case when (`n`.`processing_status` = 'FAILED') then 1 end)) AS `failed_novels`,max(`n`.`created_at`) AS `last_upload_date` from (`users` `u` left join `novels` `n` on((`u`.`id` = `n`.`user_id`))) group by `u`.`id`;

-- ----------------------------
-- Procedure structure for clean_old_logs
-- ----------------------------
DROP PROCEDURE IF EXISTS `clean_old_logs`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `clean_old_logs`(
    IN days_to_keep INT
)
BEGIN
    DELETE FROM system_logs
    WHERE timestamp < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    SELECT CONCAT('已删除 ', ROW_COUNT(), ' 条过期日志') AS result;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for register_user
-- ----------------------------
DROP PROCEDURE IF EXISTS `register_user`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `register_user`(
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
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for update_novel_processing_status
-- ----------------------------
DROP PROCEDURE IF EXISTS `update_novel_processing_status`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_novel_processing_status`(
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
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for update_user_login
-- ----------------------------
DROP PROCEDURE IF EXISTS `update_user_login`;
delimiter ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_user_login`(
    IN p_username VARCHAR(50)
)
BEGIN
    UPDATE users 
    SET 
        last_login_at = NOW(),
        updated_at = NOW()
    WHERE 
        username = p_username;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
