# 数据库重建指南

本目录包含用于构建NovelAssistant应用数据库的SQL脚本。

## 文件说明

- `schema.sql` - 创建数据库和基础设置
- `01_create_tables.sql` - 创建所有数据表
- `02_init_data.sql` - 初始化基本数据（角色、管理员账户等）
- `03_views.sql` - 创建视图
- `04_stored_procedures.sql` - 创建存储过程
- `05_indexes.sql` - 创建额外的索引优化查询性能
- `rebuild_database.sql` - 主脚本，一次性执行所有上述脚本

## 使用方法

### 方法一：使用主脚本（推荐）

1. 登录MySQL:

```bash
mysql -u root -p
```

2. 执行主脚本:

```sql
SOURCE /path/to/backend/sql/rebuild_database.sql;
```

### 方法二：分步执行

如果需要分步执行或者单独执行某个脚本，可以按照以下顺序执行：

1. 先执行schema.sql创建数据库:

```sql
SOURCE /path/to/backend/sql/schema.sql;
```

2. 依次执行其他脚本:

```sql
SOURCE /path/to/backend/sql/01_create_tables.sql;
SOURCE /path/to/backend/sql/02_init_data.sql;
SOURCE /path/to/backend/sql/03_views.sql;
SOURCE /path/to/backend/sql/04_stored_procedures.sql;
SOURCE /path/to/backend/sql/05_indexes.sql;
```

## 注意事项

1. 执行这些脚本将会删除并重新创建名为`novel_assistant`的数据库，请确保已备份任何重要数据
2. 默认创建的管理员账户:
   - 用户名: `admin`
   - 密码: `admin`
   - 建议在生产环境中立即修改默认密码
3. 这些脚本设计用于与JPA实体类匹配，如果JPA实体发生变化，可能需要更新这些脚本

## 与现有应用兼容性

这些SQL脚本是基于项目中的JPA实体类生成的，确保与应用程序的数据模型保持一致。如果应用中使用了Spring Data JPA的自动表生成功能，可能需要在application.properties中临时禁用它：

```properties
spring.jpa.hibernate.ddl-auto=none
```

执行完这些脚本后再改回适当的设置。 