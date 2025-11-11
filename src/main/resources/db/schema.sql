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
    department VARCHAR(100) COMMENT '所属部门',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 默认管理员（密码：admin123）
INSERT INTO tb_admin (username, password, real_name, phone, email) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKg8kK.i', '系统管理员', '13800138000', 'admin@example.com');

-- 测试教师（密码：teacher123）
INSERT INTO tb_teacher (username, password, real_name, teacher_no, phone, email, department) 
VALUES ('teacher001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKg8kK.i', '张老师', 'T001', '13900139000', 'teacher@example.com', '计算机学院');
