-- 数据库迁移脚本 V3
-- 创建课程表（用于Excel导入）

USE class_report;

-- 创建课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_code VARCHAR(50) COMMENT '课程编码',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    classroom VARCHAR(50) NOT NULL COMMENT '教室号',
    course_time VARCHAR(100) NOT NULL COMMENT '上课时间（如：周一 1-2节）',
    course_date DATE NOT NULL COMMENT '上课日期',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    week_day INT COMMENT '星期几：1-周一，7-周日',
    expected_count INT COMMENT '预到人数',
    semester VARCHAR(20) COMMENT '学期（如：2024-2025-1）',
    status INT DEFAULT 2 COMMENT '状态：0-已结束，1-进行中，2-未开始',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_course_date (course_date),
    INDEX idx_course_name (course_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';
