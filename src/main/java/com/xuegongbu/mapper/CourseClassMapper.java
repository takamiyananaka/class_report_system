package com.xuegongbu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuegongbu.domain.CourseClass;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程班级关联Mapper
 */
@Mapper
public interface CourseClassMapper extends BaseMapper<CourseClass> {
}
