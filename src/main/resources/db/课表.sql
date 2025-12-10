-- 课表表
CREATE TABLE course_schedule (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                                    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
                                    teacher_id BIGINT NOT NULL COMMENT '教师ID',
                                    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
                                    weekday INT NOT NULL COMMENT '星期几（1-7）',
                                    start_time TIME NOT NULL COMMENT '开始时间',
                                    end_time TIME NOT NULL COMMENT '结束时间',
                                    classroom VARCHAR(100) NOT NULL COMMENT '教室',
                                    semester VARCHAR(50) NOT NULL COMMENT '学期',
                                    school_year VARCHAR(20) NOT NULL COMMENT '学年',
                                    create_time DATETIME COMMENT '创建时间',
                                    update_time DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表表';