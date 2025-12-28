-- ====================================
-- 学工部课程考勤系统 - 完整数据库初始化脚本
-- 版本: v6.0
-- 说明: 所有ID字段使用VARCHAR类型，支持字符串ID
-- 更新说明: 本脚本可在已有数据库上运行，会自动修改表结构
-- ====================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS class_report DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE class_report;

-- ====================================
-- 1. 管理员表
-- ====================================
CREATE TABLE IF NOT EXISTS admin (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 修改已存在的admin表的id字段类型
ALTER TABLE admin MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（字符串类型）';

-- ====================================
-- 2. 学院信息表
-- ====================================
CREATE TABLE IF NOT EXISTS college (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '学院名',
    college_no VARCHAR(50) NOT NULL UNIQUE COMMENT '学院号',
    description VARCHAR(500) COMMENT '学院描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_college_no (college_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院信息表';

-- 修改已存在的college表的id字段类型
ALTER TABLE college MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（字符串类型）';

-- ====================================
-- 2.1 学院管理员表
-- ====================================
CREATE TABLE IF NOT EXISTS college_admin (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    username VARCHAR(50) NOT NULL COMMENT '学院管理员账号',
    password VARCHAR(255) NOT NULL COMMENT '学院管理员密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    college_id VARCHAR(64) NOT NULL COMMENT '所属学院ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_username (username),
    INDEX idx_college_id (college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院管理员表';

-- 修改已存在的college_admin表的id字段类型
ALTER TABLE college_admin MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（字符串类型）';

-- ====================================
-- 3. 教师表
-- ====================================
CREATE TABLE IF NOT EXISTS teacher (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    teacher_no VARCHAR(50) NOT NULL UNIQUE COMMENT '教师工号',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    department VARCHAR(255) COMMENT '所属部门（辅导员身份时格式为：专业名+年级，多个值用分号分隔）',
    college_no VARCHAR(50) COMMENT '学院号',
    identity TINYINT DEFAULT 2 COMMENT '身份：1-只是教师，2-教师且是辅导员',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    attendance_threshold DECIMAL(5,2) DEFAULT 0.90 COMMENT '考勤预警阈值（如0.90表示90%）',
    enable_email_notification TINYINT DEFAULT 1 COMMENT '是否开启邮件通知：0-否，1-是',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_username (username),
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_college_no (college_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 修改已存在的teacher表的id字段类型
ALTER TABLE teacher MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（字符串类型）';
-- 添加college_no字段（使用存储过程检查是否存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'teacher' 
  AND COLUMN_NAME = 'college_no';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE teacher ADD COLUMN college_no VARCHAR(50) COMMENT ''学院号''',
    'SELECT ''Column college_no already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加索引（使用存储过程检查是否存在）
SET @idx_exists = 0;
SELECT COUNT(*) INTO @idx_exists 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'teacher' 
  AND INDEX_NAME = 'idx_college_no';

SET @sql = IF(@idx_exists = 0, 
    'ALTER TABLE teacher ADD INDEX idx_college_no (college_no)',
    'SELECT ''Index idx_college_no already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ====================================
-- 4. 班级表
-- ====================================
CREATE TABLE IF NOT EXISTS class (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    class_name VARCHAR(20) NOT NULL COMMENT '班级名字',
    teacher_no VARCHAR(50) NOT NULL COMMENT '辅导员工号',
    count INT NOT NULL COMMENT '班级人数',
    grade VARCHAR(50) NOT NULL COMMENT '年级',
    major VARCHAR(100) NOT NULL COMMENT '专业',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_grade (grade),
    INDEX idx_major (major)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 修改已存在的class表的id字段类型
ALTER TABLE class MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（字符串类型）';

-- ====================================
-- 5. 课表表
-- ====================================
CREATE TABLE IF NOT EXISTS course_schedule (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_no VARCHAR(50) COMMENT '课程号',
    order_no VARCHAR(50) COMMENT '课序号',
    weekday VARCHAR(20) NOT NULL COMMENT '星期几（汉字：星期一至星期日）',
    expected_count INT COMMENT '预到人数',
    week_range VARCHAR(50) NOT NULL COMMENT '周次范围（格式：x-x周，例如：3-16周）',
    start_period TINYINT NOT NULL COMMENT '开始节次（1-12）',
    end_period TINYINT NOT NULL COMMENT '结束节次（1-12）',
    classroom VARCHAR(100) NOT NULL COMMENT '教室',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_weekday (weekday),
    CONSTRAINT chk_start_period CHECK (start_period >= 1 AND start_period <= 12),
    CONSTRAINT chk_end_period CHECK (end_period >= 1 AND end_period <= 12),
    CONSTRAINT chk_period_range CHECK (end_period >= start_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表表';

-- 修改已存在的course_schedule表的字段类型和字段
ALTER TABLE course_schedule MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（字符串类型）';

-- 修改weekday字段为VARCHAR类型（汉字星期）
ALTER TABLE course_schedule MODIFY COLUMN weekday VARCHAR(20) NOT NULL COMMENT '星期几（汉字：星期一至星期日）';

-- 添加course_no字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course_schedule' 
  AND COLUMN_NAME = 'course_no';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE course_schedule ADD COLUMN course_no VARCHAR(50) COMMENT ''课程号''',
    'SELECT ''Column course_no already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加order_no字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course_schedule' 
  AND COLUMN_NAME = 'order_no';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE course_schedule ADD COLUMN order_no VARCHAR(50) COMMENT ''课序号''',
    'SELECT ''Column order_no already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加week_range字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course_schedule' 
  AND COLUMN_NAME = 'week_range';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE course_schedule ADD COLUMN week_range VARCHAR(50) COMMENT ''周次范围（格式：x-x周，例如：3-16周）''',
    'SELECT ''Column week_range already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加start_period字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course_schedule' 
  AND COLUMN_NAME = 'start_period';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE course_schedule ADD COLUMN start_period TINYINT COMMENT ''开始节次（1-12）''',
    'SELECT ''Column start_period already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加end_period字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course_schedule' 
  AND COLUMN_NAME = 'end_period';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE course_schedule ADD COLUMN end_period TINYINT COMMENT ''结束节次（1-12）''',
    'SELECT ''Column end_period already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除teacher_no字段（如果存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course_schedule' 
  AND COLUMN_NAME = 'teacher_no';

SET @sql = IF(@col_exists > 0, 
    'ALTER TABLE course_schedule DROP COLUMN teacher_no',
    'SELECT ''Column teacher_no does not exist'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除class_name字段（如果存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course_schedule' 
  AND COLUMN_NAME = 'class_name';

SET @sql = IF(@col_exists > 0, 
    'ALTER TABLE course_schedule DROP COLUMN class_name',
    'SELECT ''Column class_name does not exist'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加expected_count字段（如果不存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course_schedule' 
  AND COLUMN_NAME = 'expected_count';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE course_schedule ADD COLUMN expected_count INT COMMENT ''预到人数''',
    'SELECT ''Column expected_count already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ====================================
-- 6. 课程班级关联表（course表作为关联表）
-- ====================================
CREATE TABLE IF NOT EXISTS course (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    course_id VARCHAR(64) NOT NULL COMMENT '课程ID',
    class_id VARCHAR(64) NOT NULL COMMENT '班级ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_course_id (course_id),
    INDEX idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程班级关联表';

-- 修改已存在的course表结构（将其改造为关联表）
ALTER TABLE course MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（雪花算法生成）';

-- 删除冗余字段，只保留关联表必需字段
SET @col_list = '';
SELECT GROUP_CONCAT(COLUMN_NAME) INTO @col_list
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course'
  AND COLUMN_NAME NOT IN ('id', 'course_id', 'class_id', 'create_time', 'update_time', 'is_delete');

-- 删除不需要的字段
SET @drop_cols = '';
SELECT GROUP_CONCAT(
    CONCAT('DROP COLUMN ', COLUMN_NAME)
    SEPARATOR ', '
) INTO @drop_cols
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course'
  AND COLUMN_NAME NOT IN ('id', 'course_id', 'class_id', 'create_time', 'update_time', 'is_delete', 'is_deleted');

SET @sql = IF(@drop_cols IS NOT NULL AND @drop_cols != '', 
    CONCAT('ALTER TABLE course ', @drop_cols),
    'SELECT ''No columns to drop'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 确保is_deleted字段名称为is_delete
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course' 
  AND COLUMN_NAME = 'is_deleted';

SET @sql = IF(@col_exists > 0, 
    'ALTER TABLE course CHANGE COLUMN is_deleted is_delete TINYINT DEFAULT 0 COMMENT ''是否删除：0-否，1-是''',
    'SELECT ''Column is_deleted not found'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加course_id字段（如果不存在，先添加为允许NULL，稍后更新）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course' 
  AND COLUMN_NAME = 'course_id';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE course ADD COLUMN course_id VARCHAR(64) NULL COMMENT ''课程ID'' AFTER id',
    'SELECT ''Column course_id already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加class_id字段（如果不存在，先添加为允许NULL，稍后更新）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course' 
  AND COLUMN_NAME = 'class_id';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE course ADD COLUMN class_id VARCHAR(64) NULL COMMENT ''班级ID'' AFTER course_id',
    'SELECT ''Column class_id already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 注意：如果表中有数据，需要手动填充course_id和class_id的值
-- 然后再修改为NOT NULL
-- ALTER TABLE course MODIFY COLUMN course_id VARCHAR(64) NOT NULL COMMENT '课程ID';
-- ALTER TABLE course MODIFY COLUMN class_id VARCHAR(64) NOT NULL COMMENT '班级ID';

-- 添加索引
SET @idx_exists = 0;
SELECT COUNT(*) INTO @idx_exists 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course' 
  AND INDEX_NAME = 'idx_course_id';

SET @sql = IF(@idx_exists = 0, 
    'ALTER TABLE course ADD INDEX idx_course_id (course_id)',
    'SELECT ''Index idx_course_id already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = 0;
SELECT COUNT(*) INTO @idx_exists 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'course' 
  AND INDEX_NAME = 'idx_class_id';

SET @sql = IF(@idx_exists = 0, 
    'ALTER TABLE course ADD INDEX idx_class_id (class_id)',
    'SELECT ''Index idx_class_id already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ====================================
-- 删除course_class表（不再需要）
-- ====================================
DROP TABLE IF EXISTS course_class;

-- ====================================
-- 8. 考勤记录表
-- ====================================
CREATE TABLE IF NOT EXISTS attendance (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    course_id VARCHAR(64) NOT NULL COMMENT '课程ID',
    check_time DATETIME NOT NULL COMMENT '考勤时间',
    actual_count INT COMMENT '实到人数',
    expected_count INT COMMENT '预到人数',
    attendance_rate DECIMAL(5,2) COMMENT '出勤率（%）',
    image_url VARCHAR(255) COMMENT '抓取的图片URL',
    check_type INT COMMENT '考勤类型：1-自动，2-手动',
    status INT COMMENT '状态：1-正常，2-异常',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_course_id (course_id),
    INDEX idx_check_time (check_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- 修改已存在的attendance表的字段类型
ALTER TABLE attendance MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（字符串类型）';
ALTER TABLE attendance MODIFY COLUMN course_id VARCHAR(64) NOT NULL COMMENT '课程ID';

-- ====================================
-- 7. 预警记录表
-- ====================================
CREATE TABLE IF NOT EXISTS alert (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    course_id VARCHAR(64) NOT NULL COMMENT '课程ID',
    class_id VARCHAR(64) NOT NULL COMMENT '班级ID',
    attendance_id VARCHAR(64) COMMENT '考勤记录ID',
    alert_type INT NOT NULL COMMENT '预警类型：1-人数不足，2-迟到过多，3-旷课严重',
    alert_level INT COMMENT '预警级别：1-低，2-中，3-高',
    expected_count INT COMMENT '预到人数',
    actual_count INT COMMENT '实到人数',
    alert_message VARCHAR(500) COMMENT '预警信息',
    notify_status INT COMMENT '通知状态：0-未发送，1-已发送，2-发送失败',
    read_status INT COMMENT '阅读状态：0-未读，1-已读',
    notify_time DATETIME COMMENT '通知时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_course_id (course_id),
    INDEX idx_attendance_id (attendance_id),
    INDEX idx_alert_type (alert_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- 修改已存在的alert表的字段类型
ALTER TABLE alert MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（字符串类型）';
ALTER TABLE alert MODIFY COLUMN course_id VARCHAR(64) NOT NULL COMMENT '课程ID';
ALTER TABLE alert MODIFY COLUMN attendance_id VARCHAR(64) COMMENT '考勤记录ID';

-- 添加class_id字段（如果不存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'alert' 
  AND COLUMN_NAME = 'class_id';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE alert ADD COLUMN class_id VARCHAR(64) NOT NULL COMMENT ''班级ID''',
    'SELECT ''Column class_id already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加read_status字段（如果不存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'alert' 
  AND COLUMN_NAME = 'read_status';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE alert ADD COLUMN read_status INT COMMENT ''阅读状态：0-未读，1-已读''',
    'SELECT ''Column read_status already exists'' AS msg');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ====================================
-- 10. 图片抓取记录表
-- ====================================
CREATE TABLE IF NOT EXISTS image_capture (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    source_url VARCHAR(500) NOT NULL COMMENT '来源URL',
    image_url VARCHAR(255) COMMENT '保存的图片URL',
    classroom VARCHAR(50) COMMENT '教室号',
    capture_time DATETIME NOT NULL COMMENT '抓取时间',
    person_count INT COMMENT '识别人数',
    recognition_status INT COMMENT '识别状态：0-未识别，1-识别成功，2-识别失败',
    error_message VARCHAR(500) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_classroom (classroom),
    INDEX idx_capture_time (capture_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片抓取记录表';

-- 修改已存在的image_capture表的id字段类型
ALTER TABLE image_capture MODIFY COLUMN id VARCHAR(64) COMMENT '主键ID（字符串类型）';

-- ====================================
-- 初始化数据
-- ====================================

-- 插入默认管理员（用户名：admin，密码：admin123）
-- 注意：ID使用字符串格式
INSERT INTO admin (id, username, password, real_name, phone, email) 
VALUES ('1000000000000000001', 'admin', '$2a$10$jBDsivzkzIdPZGj0Cv1aYOfNC3MBrpoVXgeSTAXOhy0Z55wRkKT4K', '系统管理员', '13800138000', 'admin@example.com')
ON DUPLICATE KEY UPDATE username = username;

-- 插入测试教师（用户名：teacher001，密码：123456）
INSERT INTO teacher (id, username, password, real_name, teacher_no, phone, email, department, identity)
VALUES ('1000000000000000002', 'teacher001', '$2a$10$IlQZy.G6fQqbVZ1dYtFW7.5VHVHEGG2Js1eH/ULU1kUxfd9E2.1kO', '张老师', 'T001', '13900139000', 'teacher@example.com', '计算机学院', 1)
ON DUPLICATE KEY UPDATE username = username;

-- 插入测试学院管理员（用户名：collegeAdmin，密码：123456）
INSERT INTO college_admin (id, username, password, real_name, phone, email, college_id)
VALUES ('1000000000000000003', 'collegeAdmin', '$2a$10$IlQZy.G6fQqbVZ1dYtFW7.5VHVHEGG2Js1eH/ULU1kUxfd9E2.1kO', '学院管理员', '13700137000', 'collegeadmin@example.com', '1')
ON DUPLICATE KEY UPDATE username = username;

-- ====================================
-- 说明
-- ====================================
-- 1. 所有表的ID字段都使用VARCHAR(64)类型，支持字符串格式的ID（包括雪花算法生成的字符串ID）
-- 2. 教师工号(teacher_no)使用VARCHAR类型，支持字符串格式
-- 3. 班级表的teacher_no字段为VARCHAR类型，存储教师工号而非ID
-- 4. 课程表和课表表的teacher_no字段为VARCHAR类型
-- 5. 考勤记录表的course_id字段为VARCHAR类型
-- 6. 预警记录表的course_id和attendance_id字段为VARCHAR类型
-- 7. 移除了外键约束，使用应用层面的数据一致性控制
-- 8. 所有表都支持逻辑删除（除了image_capture表）
-- 9. 使用utf8mb4字符集，支持emoji和特殊字符
-- 10. 本脚本使用ALTER TABLE语句，可以在已有数据库上运行，会自动修改现有表结构
-- 11. 使用ON DUPLICATE KEY UPDATE确保初始化数据可以重复执行
