package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.AttendanceDailyReport;
import com.xuegongbu.domain.CourseSchedule;

import java.util.List;

/**
 * 班级每日考勤报表 Service 接口
 */
public interface AttendanceDailyReportService extends IService<AttendanceDailyReport> {

  void updateReport(Attendance attendance, CourseSchedule course);
  
  /**
   * 根据班级ID和查询类型获取考勤日报列表
   * @param classId 班级ID
   * @param periodType 时间范围类型：1-按日，2-按周(往前七天)，3-按月(往前30天)
   * @return 考勤日报列表
   */
  List<AttendanceDailyReport> getReportsByClassIdAndType(String classId, int periodType);


}