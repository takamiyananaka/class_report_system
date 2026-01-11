package com.xuegongbu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuegongbu.domain.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT course_id FROM course WHERE class_id = #{id}")
    List<String> selectCourseIdsByClassId(String id);

    @Select("SELECT class_id FROM course WHERE course_id = #{courseId}")
    List<String> selectClassIdsByCourseId(String courseId);
}
