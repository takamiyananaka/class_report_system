-- ====================================
-- 班级每日考勤报表表
-- ====================================
CREATE TABLE IF NOT EXISTS attendance_daily_report (
    id VARCHAR(64) PRIMARY KEY COMMENT '主键ID（字符串类型）',
    class_id VARCHAR(64) NOT NULL COMMENT '班级ID',
    report_date DATE NOT NULL COMMENT '报表日期',
    attendance_record_count INT DEFAULT 0 COMMENT '考勤记录数',
    average_attendance_rate DECIMAL(5,2) COMMENT '平均考勤率（%）',
    alert_record_count INT DEFAULT 0 COMMENT '预警记录数',
    alert_rate DECIMAL(5,2) COMMENT '预警率（%）',
    total_expected_count INT DEFAULT 0 COMMENT '总预到人数',
    total_actual_count INT DEFAULT 0 COMMENT '总实到人数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_class_date (class_id, report_date),
    INDEX idx_class_id (class_id),
    INDEX idx_report_date (report_date),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级每日考勤报表表';

-- 添加外键约束（如果需要）
-- ALTER TABLE attendance_daily_report ADD CONSTRAINT fk_attendance_daily_report_class 
--     FOREIGN KEY (class_id) REFERENCES class (id);