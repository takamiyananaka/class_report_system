-- 数据库迁移脚本 V5
-- 创建班级表（class table）

USE class_report;

-- 创建班级表
CREATE TABLE IF NOT EXISTS class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    class_name VARCHAR(20) NOT NULL COMMENT '班级名字',
    teacher_id BIGINT NOT NULL COMMENT '辅导员ID（教师ID）',
    count INT NOT NULL COMMENT '班级人数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_class_name (class_name),
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';
