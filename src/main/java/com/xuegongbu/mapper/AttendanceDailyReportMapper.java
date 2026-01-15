package com.xuegongbu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuegongbu.domain.AttendanceDailyReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级每日考勤报表 Mapper 接口
 */
@Mapper
public interface AttendanceDailyReportMapper extends BaseMapper<AttendanceDailyReport> {
}