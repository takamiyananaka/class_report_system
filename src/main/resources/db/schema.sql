CREATE DATABASE IF NOT EXISTS class_report DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE class_report;

-- 管理员表
CREATE TABLE tb_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 教师表
CREATE TABLE tb_teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    teacher_no VARCHAR(50) NOT NULL UNIQUE COMMENT '教师工号',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    department VARCHAR(255) COMMENT '所属部门（辅导员身份时格式为：专业名+年级，多个值用分号分隔）',
    identity TINYINT DEFAULT 1 COMMENT '身份：1-只是教师，2-教师且是辅导员',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 课表表
CREATE TABLE tb_course_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    weekday TINYINT NOT NULL COMMENT '星期几（1-7）',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    classroom VARCHAR(100) NOT NULL COMMENT '教室',
    semester VARCHAR(50) NOT NULL COMMENT '学期',
    school_year VARCHAR(20) NOT NULL COMMENT '学年',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_class_name (class_name),
    INDEX idx_weekday (weekday),
    FOREIGN KEY (teacher_id) REFERENCES tb_teacher(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表表';

-- 默认管理员（密码：admin123）
INSERT INTO tb_admin (username, password, real_name, phone, email) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKg8kK.i', '系统管理员', '13800138000', 'admin@example.com');

-- 测试教师（密码：teacher123）
INSERT INTO tb_teacher (username, password, real_name, teacher_no, phone, email, department, identity) 
VALUES ('teacher001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKg8kK.i', '张老师', 'T001', '13900139000', 'teacher@example.com', '计算机学院', 1);
