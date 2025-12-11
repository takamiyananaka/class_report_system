-- ====================================
-- 学工部课程考勤系统 - 完整数据库初始化脚本
-- 版本: v5.0
-- 说明: 使用雪花算法生成ID，教师工号为字符串类型
-- ====================================

CREATE DATABASE IF NOT EXISTS class_report DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE class_report;

-- ====================================
-- 1. 管理员表
-- ====================================
CREATE TABLE IF NOT EXISTS admin (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
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

-- ====================================
-- 2. 教师表
-- ====================================
CREATE TABLE IF NOT EXISTS teacher (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    teacher_no VARCHAR(50) NOT NULL UNIQUE COMMENT '教师工号',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    department VARCHAR(255) COMMENT '所属部门（辅导员身份时格式为：专业名+年级，多个值用分号分隔）',
    identity TINYINT DEFAULT 2 COMMENT '身份：1-只是教师，2-教师且是辅导员',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_username (username),
    INDEX idx_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- ====================================
-- 3. 班级表
-- ====================================
CREATE TABLE IF NOT EXISTS class (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    class_name VARCHAR(20) NOT NULL COMMENT '班级名字',
    teacher_no VARCHAR(50) NOT NULL COMMENT '辅导员工号',
    count INT NOT NULL COMMENT '班级人数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    INDEX idx_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- ====================================
-- 4. 课表表
-- ====================================
CREATE TABLE IF NOT EXISTS course_schedule (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher_no BIGINT NOT NULL COMMENT '教师工号',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    weekday TINYINT NOT NULL COMMENT '星期几（1-7）',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    classroom VARCHAR(100) NOT NULL COMMENT '教室',
    semester VARCHAR(50) NOT NULL COMMENT '学期',
    school_year VARCHAR(20) NOT NULL COMMENT '学年',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_class_name (class_name),
    INDEX idx_weekday (weekday)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表表';

-- ====================================
-- 5. 课程表
-- ====================================
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_code VARCHAR(50) COMMENT '课程编码',
    teacher_no BIGINT NOT NULL COMMENT '教师工号',
    classroom VARCHAR(50) NOT NULL COMMENT '教室号',
    course_time VARCHAR(100) NOT NULL COMMENT '上课时间（如：周一 1-2节）',
    course_date DATE NOT NULL COMMENT '上课日期',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    week_day INT COMMENT '星期几：1-周一，7-周日',
    expected_count INT COMMENT '预到人数',
    class_name VARCHAR(100) COMMENT '上课的班级名字',
    semester VARCHAR(20) COMMENT '学期（如：2024-2025-1）',
    status INT DEFAULT 2 COMMENT '状态：0-已结束，1-进行中，2-未开始',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_course_date (course_date),
    INDEX idx_course_name (course_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ====================================
-- 6. 考勤记录表
-- ====================================
CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    course_id BIGINT NOT NULL COMMENT '课程ID',
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

-- ====================================
-- 7. 预警记录表
-- ====================================
CREATE TABLE IF NOT EXISTS alert (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    attendance_id BIGINT COMMENT '考勤记录ID',
    alert_type INT NOT NULL COMMENT '预警类型：1-人数不足，2-迟到过多，3-旷课严重',
    alert_level INT COMMENT '预警级别：1-低，2-中，3-高',
    expected_count INT COMMENT '预到人数',
    actual_count INT COMMENT '实到人数',
    alert_message VARCHAR(500) COMMENT '预警信息',
    notify_status INT COMMENT '通知状态：0-未发送，1-已发送，2-发送失败',
    notify_time DATETIME COMMENT '通知时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_course_id (course_id),
    INDEX idx_attendance_id (attendance_id),
    INDEX idx_alert_type (alert_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- ====================================
-- 8. 图片抓取记录表
-- ====================================
CREATE TABLE IF NOT EXISTS image_capture (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法生成）',
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

-- ====================================
-- 初始化数据
-- ====================================

-- 插入默认管理员（用户名：admin，密码：admin123）
-- 注意：ID使用固定的雪花ID格式，确保在应用启动时不会冲突
INSERT INTO admin (id, username, password, real_name, phone, email) 
VALUES (1000000000000000001, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKg8kK.i', '系统管理员', '13800138000', 'admin@example.com');

-- 插入测试教师（用户名：teacher001，密码：123456）
INSERT INTO teacher (id, username, password, real_name, teacher_no, phone, email, department, identity)
VALUES (1000000000000000002, 'teacher001', '$2a$10$IlQZy.G6fQqbVZ1dYtFW7.5VHVHEGG2Js1eH/ULU1kUxfd9E2.1kO', '张老师', 'T001', '13900139000', 'teacher@example.com', '计算机学院', 1);

-- ====================================
-- 说明
-- ====================================
-- 1. 所有表的ID字段都使用BIGINT类型，支持雪花算法生成的长整型ID
-- 2. 教师工号(teacher_no)使用VARCHAR类型，支持字符串格式
-- 3. 班级表的teacher_no字段为VARCHAR类型，存储教师工号而非ID
-- 4. 移除了外键约束，使用应用层面的数据一致性控制
-- 5. 所有表都支持逻辑删除（除了image_capture表）
-- 6. 使用utf8mb4字符集，支持emoji和特殊字符
