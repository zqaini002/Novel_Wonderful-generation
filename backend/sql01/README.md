# 小说智析 - 数据库文件

本目录包含小说智析项目的数据库相关SQL文件，提供了完整的数据库结构、测试数据、视图、存储过程和索引。

## 文件说明

1. **01_create_tables.sql** - 创建数据库和所有表结构
   - 用户表 (users)
   - 角色表 (roles)
   - 用户-角色关联表 (user_roles)
   - 小说表 (novels)
   - 章节表 (chapters)
   - 章节关键词表 (chapter_keywords)
   - 标签表 (tags)
   - 系统日志表 (system_logs)

2. **02_init_data.sql** - 初始化基础数据
   - 默认角色 (ROLE_USER, ROLE_ADMIN)
   - 默认管理员账户
   - 默认测试用户账户
   - 示例小说数据
   - 示例章节和标签数据
   - 示例系统日志

3. **03_views.sql** - 各种数据库视图
   - 用户详情视图 (user_details)
   - 小说详情视图 (novel_details)
   - 小说标签统计视图 (novel_tag_stats)
   - 用户上传统计视图 (user_upload_stats)
   - 系统统计视图 (system_stats)
   - 最近活动视图 (recent_activities)

4. **04_stored_procedures.sql** - 存储过程和触发器
   - 更新小说处理状态 (update_novel_processing_status)
   - 用户注册过程 (register_user)
   - 用户登录更新 (update_user_login)
   - 清理系统日志 (clean_old_logs)
   - 系统统计函数 (get_dashboard_stats)
   - 添加小说标签 (add_novel_tag)
   - 分析用户活跃度 (analyze_user_activity)
   - 章节插入触发器 (after_chapter_insert)

5. **05_indexes.sql** - 性能优化索引
   - 为所有表创建单字段索引
   - 常用查询复合索引

## 使用方法

按照顺序执行SQL文件：

```bash
# 使用命令行执行
mysql -u your_username -p < 01_create_tables.sql
mysql -u your_username -p < 02_init_data.sql
mysql -u your_username -p < 03_views.sql
mysql -u your_username -p < 04_stored_procedures.sql
mysql -u your_username -p < 05_indexes.sql
```

或者在MySQL客户端中直接执行各个文件内容。

## 默认账户信息

- 管理员账户：
  - 用户名: admin
  - 密码: admin123

- 未测试用户账户：
  - 用户名: user
  - 密码: user123

## 数据库配置

默认数据库名称: `novel_assistant`

应用中配置Spring Boot数据源：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/novel_assistant?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## 注意事项

- 所有密码都使用BCrypt加密
- 执行前请确保MySQL用户具有创建数据库、表、视图和存储过程的权限
- 在生产环境中请更改默认密码
- 可以根据需要调整数据库表结构和示例数据 