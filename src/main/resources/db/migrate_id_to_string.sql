-- ====================================
-- ID类型迁移脚本：从BIGINT改为VARCHAR(50)
-- 版本: v6.0
-- 说明: 此脚本会自动检测表是否存在，并修改ID字段类型
-- 执行此脚本会将所有表的ID从BIGINT改为VARCHAR(50)
-- ====================================

USE class_report;

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- ====================================
-- 1. 修改 admin 表
-- ====================================
ALTER TABLE admin 
    MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '主键ID';

-- ====================================
-- 2. 修改 teacher 表
-- ====================================
ALTER TABLE teacher 
    MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '主键ID';

-- ====================================
-- 3. 修改 class 表
-- ====================================
ALTER TABLE class 
    MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '主键ID';

-- ====================================
-- 4. 修改 course_schedule 表
-- ====================================
ALTER TABLE course_schedule 
    MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '主键ID',
    MODIFY COLUMN teacher_no VARCHAR(50) NOT NULL COMMENT '教师工号';

-- 删除旧的teacher_no索引并重新创建
DROP INDEX IF EXISTS idx_teacher_no ON course_schedule;
CREATE INDEX idx_teacher_no ON course_schedule(teacher_no);

-- ====================================
-- 5. 修改 course 表
-- ====================================
ALTER TABLE course 
    MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '主键ID',
    MODIFY COLUMN teacher_no VARCHAR(50) NOT NULL COMMENT '教师工号';

-- 删除旧的teacher_no索引并重新创建  
DROP INDEX IF EXISTS idx_teacher_no ON course;
CREATE INDEX idx_teacher_no ON course(teacher_no);

-- ====================================
-- 6. 修改 attendance 表
-- ====================================
ALTER TABLE attendance 
    MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '主键ID',
    MODIFY COLUMN course_id VARCHAR(50) NOT NULL COMMENT '课程ID';

-- 删除旧的course_id索引并重新创建
DROP INDEX IF EXISTS idx_course_id ON attendance;
CREATE INDEX idx_course_id ON attendance(course_id);

-- ====================================
-- 7. 修改 alert 表
-- ====================================
ALTER TABLE alert 
    MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '主键ID',
    MODIFY COLUMN course_id VARCHAR(50) NOT NULL COMMENT '课程ID',
    MODIFY COLUMN attendance_id VARCHAR(50) COMMENT '考勤记录ID';

-- 删除旧索引并重新创建
DROP INDEX IF EXISTS idx_course_id ON alert;
CREATE INDEX idx_course_id ON alert(course_id);
DROP INDEX IF EXISTS idx_attendance_id ON alert;
CREATE INDEX idx_attendance_id ON alert(attendance_id);

-- ====================================
-- 8. 修改 image_capture 表
-- ====================================
ALTER TABLE image_capture 
    MODIFY COLUMN id VARCHAR(50) NOT NULL COMMENT '主键ID';

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ====================================
-- 说明
-- ====================================
-- 1. 所有表的ID字段类型已从BIGINT改为VARCHAR(50)，支持字符串ID
-- 2. teacher_no和相关外键字段也改为VARCHAR(50)
-- 3. 重新创建了所有受影响的索引
-- 4. 此脚本可以在已有表的情况下运行，会自动修改表结构
