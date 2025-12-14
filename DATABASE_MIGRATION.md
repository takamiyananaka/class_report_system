# 数据库迁移指南 - ID类型从BIGINT转换为VARCHAR

## 概述

本次更新将所有表的ID字段从 `BIGINT` 类型改为 `VARCHAR(50)` 类型，以支持字符串格式的ID（如雪花算法生成的ID）。

## 变更内容

### 1. 数据库表变更

以下表的ID字段已从BIGINT改为VARCHAR(50)：

- `admin` - 管理员表
- `teacher` - 教师表  
- `class` - 班级表
- `course_schedule` - 课表表
- `course` - 课程表
- `attendance` - 考勤记录表
- `alert` - 预警记录表
- `image_capture` - 图片抓取记录表

### 2. 外键字段变更

以下字段也从BIGINT改为VARCHAR(50)：

- `course_schedule.teacher_no` - 教师工号
- `course.teacher_no` - 教师工号
- `attendance.course_id` - 课程ID
- `alert.course_id` - 课程ID
- `alert.attendance_id` - 考勤记录ID

### 3. Java代码变更

所有实体类、DTO、VO、控制器和服务类中的ID字段都已从 `Long` 改为 `String`。

## 迁移方案

### 方案一：全新安装（推荐用于新数据库）

如果是全新安装，直接运行更新后的建表脚本：

```bash
mysql -u username -p database_name < src/main/resources/db/complete_schema.sql
```

### 方案二：现有数据库迁移（推荐用于已有数据）

如果数据库中已经有表和数据，运行迁移脚本：

```bash
mysql -u username -p database_name < src/main/resources/db/migrate_id_to_string.sql
```

**重要提示：**
- 此脚本会自动修改所有表的ID字段类型
- 会自动转换现有的数值ID为字符串格式
- 会重新创建相关索引
- 建议在执行前备份数据库

### 迁移脚本说明

迁移脚本 `migrate_id_to_string.sql` 的特点：

1. **安全性**：临时禁用外键检查，确保修改顺利进行
2. **自动转换**：MySQL会自动将现有的BIGINT值转换为VARCHAR
3. **索引重建**：删除并重新创建受影响的索引
4. **可重复执行**：如果字段已经是VARCHAR类型，脚本不会报错

## 执行步骤

### 步骤1：备份数据库

```bash
mysqldump -u username -p database_name > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 步骤2：执行迁移脚本

```bash
mysql -u username -p class_report < src/main/resources/db/migrate_id_to_string.sql
```

### 步骤3：验证迁移结果

登录MySQL并检查表结构：

```sql
USE class_report;

-- 检查admin表
DESCRIBE admin;

-- 检查teacher表
DESCRIBE teacher;

-- 检查course表
DESCRIBE course;

-- 检查其他表...
```

确保ID字段显示为 `varchar(50)`。

### 步骤4：重启应用

迁移完成后，重启Spring Boot应用：

```bash
mvn spring-boot:run
```

或者如果已经打包：

```bash
java -jar target/class_report_system-0.0.1-SNAPSHOT.jar
```

## 注意事项

1. **数据兼容性**：MySQL会自动将现有的BIGINT ID值转换为字符串，例如 `1234567890` 会变成 `"1234567890"`

2. **新ID生成**：应用程序会使用雪花算法生成字符串格式的ID，格式如 `"1867890123456789012"`

3. **索引影响**：VARCHAR类型的索引性能略低于BIGINT，但对于50字符长度的ID，影响可忽略不计

4. **字符集**：确保数据库使用 `utf8mb4` 字符集，以支持所有字符

5. **回滚**：如果需要回滚，请使用步骤1创建的备份文件

## 故障排除

### 问题1：外键约束错误

如果遇到外键约束错误，确保迁移脚本中的 `SET FOREIGN_KEY_CHECKS = 0;` 正常执行。

### 问题2：索引重建失败

如果索引删除失败（因为不存在），可以忽略这些错误，脚本会继续执行。

### 问题3：数据转换问题

如果某些ID值包含非数字字符，迁移脚本会保留这些值。确保应用程序能够处理这些情况。

## 测试建议

1. 在测试环境先执行迁移
2. 验证所有API接口正常工作
3. 检查ID生成是否正确
4. 确认查询和关联操作正常

## 联系支持

如有问题，请查看项目文档或提交Issue。
