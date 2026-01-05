package com.xuegongbu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuegongbu.domain.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    //通过学院名称查询教师工号(子查询获得学院号，通过学院号查)
    @Select( "SELECT teacher_no FROM teacher WHERE college_no = (SELECT college_no FROM college WHERE college_name = #{collegeName})")
     List<String> queryTeacherNoByCollegeName(@Param("collegeName") String collegeName);


}
