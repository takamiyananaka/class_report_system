-- 数据库迁移脚本 V4
-- 为教师表添加缺失的字段以匹配实体类定义

USE class_report;

-- 添加最后登录时间字段
ALTER TABLE tb_teacher
ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间' AFTER status;

-- 添加最后登录IP字段
ALTER TABLE tb_teacher
ADD COLUMN last_login_ip VARCHAR(50) COMMENT '最后登录IP' AFTER last_login_time;

-- 添加备注字段
ALTER TABLE tb_teacher
ADD COLUMN remark VARCHAR(500) COMMENT '备注' AFTER last_login_ip;

-- 添加逻辑删除字段
ALTER TABLE tb_teacher
ADD COLUMN is_deleted INT DEFAULT 0 COMMENT '是否删除：0-否，1-是' AFTER remark;
