package com.xuegongbu.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.dto.CourseScheduleExcelDTO;
import com.xuegongbu.dto.CourseScheduleQueryDTO;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.service.CourseScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CourseScheduleServiceImpl extends ServiceImpl<CourseScheduleMapper, CourseSchedule> implements CourseScheduleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 检查文件扩展名和Content-Type
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("文件格式不正确，只支持.xlsx或.xls格式");
        }
        
        // 验证Content-Type
        if (contentType == null || (!contentType.contains("spreadsheetml") && !contentType.contains("excel") && !contentType.contains("ms-excel"))) {
            throw new IllegalArgumentException("文件类型不正确，只支持Excel文件");
        }
        
        try {
            // 读取Excel数据
            List<CourseScheduleExcelDTO> excelDataList = EasyExcel.read(file.getInputStream())
                    .head(CourseScheduleExcelDTO.class)
                    .sheet()
                    .doReadSync();
            
            if (excelDataList == null || excelDataList.isEmpty()) {
                throw new IllegalArgumentException("Excel文件中没有数据");
            }
            
            log.info("从Excel读取到 {} 条数据", excelDataList.size());
            
            // 转换为CourseSchedule对象并保存
            List<CourseSchedule> courseScheduleList = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            for (int i = 0; i < excelDataList.size(); i++) {
                try {
                    CourseScheduleExcelDTO dto = excelDataList.get(i);
                    
                    // 验证必填字段
                    if (isBlank(dto.getCourseName())) {
                        errorMessages.add(String.format("第%d行：课程名称不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (dto.getTeacherId() == null) {
                        errorMessages.add(String.format("第%d行：教师ID不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getClassName())) {
                        errorMessages.add(String.format("第%d行：班级名称不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (dto.getWeekday() == null || dto.getWeekday() < 1 || dto.getWeekday() > 7) {
                        errorMessages.add(String.format("第%d行：星期几必须是1-7之间的数字", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getStartTime())) {
                        errorMessages.add(String.format("第%d行：开始时间不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getEndTime())) {
                        errorMessages.add(String.format("第%d行：结束时间不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getClassroom())) {
                        errorMessages.add(String.format("第%d行：教室不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getSemester())) {
                        errorMessages.add(String.format("第%d行：学期不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getSchoolYear())) {
                        errorMessages.add(String.format("第%d行：学年不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    
                    CourseSchedule courseSchedule = new CourseSchedule();
                    courseSchedule.setCourseName(dto.getCourseName().trim());
                    courseSchedule.setTeacherId(dto.getTeacherId());
                    courseSchedule.setClassName(dto.getClassName().trim());
                    courseSchedule.setWeekday(dto.getWeekday());
                    courseSchedule.setStartTime(parseTime(dto.getStartTime()));
                    courseSchedule.setEndTime(parseTime(dto.getEndTime()));
                    courseSchedule.setClassroom(dto.getClassroom().trim());
                    courseSchedule.setSemester(dto.getSemester().trim());
                    courseSchedule.setSchoolYear(dto.getSchoolYear().trim());
                    
                    courseScheduleList.add(courseSchedule);
                    successCount++;
                } catch (Exception e) {
                    log.error("处理第{}行数据时出错: {}", i + 2, e.getMessage(), e);
                    errorMessages.add(String.format("第%d行：%s", i + 2, e.getMessage()));
                    failCount++;
                }
            }
            
            // 批量保存
            if (!courseScheduleList.isEmpty()) {
                this.saveBatch(courseScheduleList);
            }
            
            log.info("导入完成，成功：{}条，失败：{}条", successCount, failCount);
            
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalCount", excelDataList.size());
            result.put("message", String.format("成功导入%d条课表数据，失败%d条", successCount, failCount));
            
            if (!errorMessages.isEmpty()) {
                result.put("errors", errorMessages);
            }
            
            return result;
            
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new IllegalArgumentException("读取Excel文件失败: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // 重新抛出参数异常
            throw e;
        } catch (Exception e) {
            log.error("导入课表数据失败", e);
            throw new IllegalStateException("导入课表数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析时间字符串为LocalTime
     * 支持格式：HH:mm:ss, HH:mm, H:mm, H:mm:ss
     */
    private LocalTime parseTime(String timeStr) {
        if (timeStr == null) {
            throw new IllegalArgumentException("时间不能为空");
        }
        
        timeStr = timeStr.trim();
        
        // 定义支持的时间格式，按最严格到最宽松的顺序
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm")
        };
        
        // 尝试使用每个格式解析
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(timeStr, formatter);
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }
        
        // 所有格式都失败
        throw new IllegalArgumentException("时间格式不正确: " + timeStr + "，支持的格式为 HH:mm、HH:mm:ss、H:mm 或 H:mm:ss");
    }
    
    /**
     * 检查字符串是否为空
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    @Override
    public Page<CourseSchedule> queryPage(CourseScheduleQueryDTO queryDTO) {
        // 设置分页参数
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        Page<CourseSchedule> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<>();
        
        // 教师ID条件
        if (queryDTO.getTeacherId() != null) {
            queryWrapper.eq(CourseSchedule::getTeacherId, queryDTO.getTeacherId());
        }
        
        // 班级名称条件（模糊查询）
        if (!isBlank(queryDTO.getClassName())) {
            queryWrapper.like(CourseSchedule::getClassName, queryDTO.getClassName().trim());
        }
        
        // 课程名称条件（模糊查询）
        if (!isBlank(queryDTO.getCourseName())) {
            queryWrapper.like(CourseSchedule::getCourseName, queryDTO.getCourseName().trim());
        }
        
        // 星期几条件
        if (queryDTO.getWeekday() != null) {
            queryWrapper.eq(CourseSchedule::getWeekday, queryDTO.getWeekday());
        }
        
        // 学期条件
        if (!isBlank(queryDTO.getSemester())) {
            queryWrapper.eq(CourseSchedule::getSemester, queryDTO.getSemester().trim());
        }
        
        // 学年条件
        if (!isBlank(queryDTO.getSchoolYear())) {
            queryWrapper.eq(CourseSchedule::getSchoolYear, queryDTO.getSchoolYear().trim());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(CourseSchedule::getCreateTime);
        
        log.info("查询课表，条件：teacherId={}, className={}, courseName={}, weekday={}, semester={}, schoolYear={}, pageNum={}, pageSize={}", 
                queryDTO.getTeacherId(), queryDTO.getClassName(), queryDTO.getCourseName(), 
                queryDTO.getWeekday(), queryDTO.getSemester(), queryDTO.getSchoolYear(), pageNum, pageSize);
        
        return this.page(page, queryWrapper);
    }
}
