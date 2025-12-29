package com.xuegongbu.util;

import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 课程时间映射工具类
 * 提供第几节课与具体时间的转换功能
 */
@Component
public class ClassTimeUtil {
    
    // 课程开始时间映射，key为第几节课，value为开始时间
    public static final Map<Integer, LocalTime> CLASS_START_TIME_MAP = new HashMap<>();
    
    // 课程结束时间映射，key为第几节课，value为结束时间
    public static final Map<Integer, LocalTime> CLASS_END_TIME_MAP = new HashMap<>();
    
    // 课程时间区间映射，key为第几节课，value为时间区间字符串
    public static final Map<Integer, String> CLASS_TIME_RANGE_MAP = new HashMap<>();
    
    static {
        // 初始化课程时间映射
        // 第1节课: 08:00-08:45
        addClassTime(1, "08:00", "08:45");
        
        // 第2节课: 08:50-09:35
        addClassTime(2, "08:50", "09:35");
        
        // 第3节课: 09:50-10:35
        addClassTime(3, "09:50", "10:35");
        
        // 第4节课: 10:40-11:25
        addClassTime(4, "10:40", "11:25");
        
        // 第5节课: 11:30-12:15
        addClassTime(5, "11:30", "12:15");
        
        // 第6节课: 14:00-14:45
        addClassTime(6, "14:30", "15:15");
        
        // 第7节课: 14:50-15:35
        addClassTime(7, "15:20", "16:05");
        
        // 第8节课: 15:50-16:35
        addClassTime(8, "16:20", "17:05");
        
        // 第9节课: 16:40-17:25
        addClassTime(9, "17:10", "17:55");
        
        // 第10节课: 17:30-18:15
        addClassTime(10, "19:00", "19:45");
        
        // 第11节课: 19:00-19:45
        addClassTime(11, "19:50", "20:35");
        
        // 第12节课: 19:50-20:35
        addClassTime(12, "20:40", "21:25");
    }
    
    /**
     * 添加课程时间映射
     * @param classNumber 第几节课
     * @param startTime 开始时间 (HH:MM格式)
     * @param endTime 结束时间 (HH:MM格式)
     */
    private static void addClassTime(int classNumber, String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        CLASS_START_TIME_MAP.put(classNumber, start);
        CLASS_END_TIME_MAP.put(classNumber, end);
        CLASS_TIME_RANGE_MAP.put(classNumber, startTime + "-" + endTime);
    }
    
    /**
     * 根据第几节课获取开始时间
     * @param classNumber 第几节课
     * @return 开始时间，格式为 HH:MM
     */
    public static String getStartTime(int classNumber) {
        LocalTime time = CLASS_START_TIME_MAP.get(classNumber);
        return time != null ? time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : null;
    }
    
    /**
     * 根据第几节课获取结束时间
     * @param classNumber 第几节课
     * @return 结束时间，格式为 HH:MM
     */
    public static String getEndTime(int classNumber) {
        LocalTime time = CLASS_END_TIME_MAP.get(classNumber);
        return time != null ? time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : null;
    }
    
    /**
     * 根据第几节课获取时间区间
     * @param classNumber 第几节课
     * @return 时间区间，格式为 HH:MM-HH:MM
     */
    public static String getTimeRange(int classNumber) {
        return CLASS_TIME_RANGE_MAP.get(classNumber);
    }
    
    /**
     * 根据第几节课获取开始时间 (LocalTime格式)
     * @param classNumber 第几节课
     * @return 开始时间
     */
    public static LocalTime getStartTimeAsLocalTime(int classNumber) {
        return CLASS_START_TIME_MAP.get(classNumber);
    }
    
    /**
     * 根据第几节课获取结束时间 (LocalTime格式)
     * @param classNumber 第几节课
     * @return 结束时间
     */
    public static LocalTime getEndTimeAsLocalTime(int classNumber) {
        return CLASS_END_TIME_MAP.get(classNumber);
    }
    
    /**
     * 验证第几节课是否有效
     * @param classNumber 第几节课
     * @return 是否有效
     */
    public static boolean isValidClassNumber(int classNumber) {
        return CLASS_START_TIME_MAP.containsKey(classNumber);
    }
    
    /**
     * 获取所有课程节数
     * @return 课程节数数组
     */
    public static Integer[] getAllClassNumbers() {
        return CLASS_START_TIME_MAP.keySet().toArray(new Integer[0]);
    }

    /**
     * 根据时间获取对应的课程节数
     * @param time LocalTime格式的时间
     * @return 课程节数，如果不在任何课程时间内则返回-1
     */
    public static int getClassNumberByTime(LocalTime time) {
        // 只比较小时和分钟，忽略秒和纳秒
        LocalTime timeOnlyHourMinute = LocalTime.of(time.getHour(), time.getMinute());
        
        for (Map.Entry<Integer, LocalTime> entry : CLASS_START_TIME_MAP.entrySet()) {
            Integer classNumber = entry.getKey();
            LocalTime startTime = entry.getValue();
            LocalTime endTime = CLASS_END_TIME_MAP.get(classNumber);
            
            // 只比较小时和分钟
            LocalTime startOnlyHourMinute = LocalTime.of(startTime.getHour(), startTime.getMinute());
            LocalTime endOnlyHourMinute = LocalTime.of(endTime.getHour(), endTime.getMinute());
            
            if (timeOnlyHourMinute.isAfter(startOnlyHourMinute) && timeOnlyHourMinute.isBefore(endOnlyHourMinute) || 
                timeOnlyHourMinute.equals(startOnlyHourMinute) || timeOnlyHourMinute.equals(endOnlyHourMinute)) {
                return classNumber;
            }
        }
        return -1; // 未找到匹配的课程时间
    }

    /**
     * 根据时间字符串获取对应的课程节数
     * @param timeStr 时间字符串，格式为 "HH:MM"
     * @return 课程节数，如果不在任何课程时间内则返回-1
     */
    public static int getClassNumberByTimeString(String timeStr) {
        try {
            LocalTime time = LocalTime.parse(timeStr);
            return getClassNumberByTime(time);
        } catch (Exception e) {
            return -1; // 时间格式错误或未找到匹配的课程时间
        }
    }

    /**
     * 将DayOfWeek枚举转换为中文星期
     * @param dayOfWeek DayOfWeek枚举
     * @return 中文星期字符串
     */
    public static String convertDayOfWeekToChinese(java.time.DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "星期一";
            case TUESDAY:
                return "星期二";
            case WEDNESDAY:
                return "星期三";
            case THURSDAY:
                return "星期四";
            case FRIDAY:
                return "星期五";
            case SATURDAY:
                return "星期六";
            case SUNDAY:
                return "星期日";
            default:
                return "星期一"; // 默认值
        }
    }
}