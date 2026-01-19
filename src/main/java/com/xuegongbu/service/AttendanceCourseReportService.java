package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.AttendanceCourseReport;
import com.xuegongbu.domain.CourseSchedule;

import java.util.List;

/**
 * 课程考勤报表 Service 接口
 */
public interface AttendanceCourseReportService extends IService<AttendanceCourseReport> {
    /**
     * 更新课程报表
     * @param attendance 考勤记录
     * @param course 课程安排
     */
    void updateReport(Attendance attendance, CourseSchedule course);

    /**
     * 根据课序号和时间范围类型获取考勤报表列表
     * @param orderNo 课序号
     * @param periodType 时间范围类型：1-按日，2-按周(往前七天)，3-按月(往前30天)
     * @return 考勤报表列表
     */
    List<AttendanceCourseReport> getReportsByOrderNoAndType(String orderNo, int periodType);

    /**
     * 根据图表查询参数获取课程考勤报表数据
     * @param queryDTO 图表查询参数
     * @return 考勤报表列表
     */
    
}