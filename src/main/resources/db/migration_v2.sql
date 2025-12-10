-- 数据库迁移脚本 V2
-- 为已存在的数据库添加新字段和表

USE class_report;

-- 修改department字段长度以支持多个专业
ALTER TABLE teacher
MODIFY COLUMN department VARCHAR(255) COMMENT '所属部门（辅导员身份时格式为：专业名+年级，多个值用分号分隔）';

-- 创建课表表
CREATE TABLE IF NOT EXISTS course_schedule (
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
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表表';
