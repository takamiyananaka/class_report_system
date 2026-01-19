package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.AttendanceDailyReport;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.vo.AttendanceChartVO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 班级每日考勤报表 Service 接口
 */
public interface AttendanceDailyReportService extends IService<AttendanceDailyReport> {

  void updateReport(Attendance attendance, CourseSchedule course);
  
  /**
   * 根据班级ID和查询类型获取考勤日报列表
   * @return 考勤日报列表
   */
  List<AttendanceDailyReport> getAttendanceChartByClassIdAndType(List<String> classIds, @NotNull(message = "请选择精细度") Integer granularity, String semesterName);
}