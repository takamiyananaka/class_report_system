package com.xuegongbu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuegongbu.domain.CourseSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseScheduleMapper extends BaseMapper<CourseSchedule> {
    
    /**
     * 查询课表列表
     */
    List<CourseSchedule> findList(@Param("courseName") String courseName,
                                   @Param("teacherId") Long teacherId,
                                   @Param("className") String className,
                                   @Param("weekday") Integer weekday,
                                   @Param("semester") String semester,
                                   @Param("schoolYear") String schoolYear,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);
    
    /**
     * 查询课表总数
     */
    int count(@Param("courseName") String courseName,
              @Param("teacherId") Long teacherId,
              @Param("className") String className,
              @Param("weekday") Integer weekday,
              @Param("semester") String semester,
              @Param("schoolYear") String schoolYear);
    
    /**
     * 根据教师ID查询课表
     */
    List<CourseSchedule> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 根据班级名称查询课表
     */
    List<CourseSchedule> findByClassName(@Param("className") String className);
}
