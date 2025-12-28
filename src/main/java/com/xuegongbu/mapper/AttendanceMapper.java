package com.xuegongbu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuegongbu.domain.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {
    
    /**
     * 根据课程ID列表查询每个课程的最新考勤记录
     * @param courseIds 课程ID列表
     * @return 考勤记录列表
     */
    @Select("<script>" +
            "SELECT a.* FROM attendance a " +
            "INNER JOIN (" +
            "  SELECT course_id, MAX(check_time) as latest_time " +
            "  FROM attendance " +
            "  WHERE course_id IN " +
            "  <foreach collection='courseIds' item='id' open='(' separator=',' close=')'>" +
            "    #{id}" +
            "  </foreach>" +
            "  GROUP BY course_id" +
            ") latest ON a.course_id = latest.course_id AND a.check_time = latest.latest_time" +
            "</script>")
    List<Attendance> selectLatestAttendanceByCourseIds(@Param("courseIds") List<String> courseIds);
}