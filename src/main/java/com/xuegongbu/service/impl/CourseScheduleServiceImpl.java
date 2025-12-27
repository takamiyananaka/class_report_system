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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CourseScheduleServiceImpl extends ServiceImpl<CourseScheduleMapper, CourseSchedule> implements CourseScheduleService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(MultipartFile file, String teacherNo) {
        Map<String, Object> result = new HashMap<>();
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (teacherNo == null) {
            throw new IllegalArgumentException("教师工号不能为空");
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
                    if (isBlank(dto.getWeekRange())) {
                        errorMessages.add(String.format("第%d行：周次范围不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (dto.getStartPeriod() == null || dto.getStartPeriod() < 1 || dto.getStartPeriod() > 12) {
                        errorMessages.add(String.format("第%d行：开始节次必须是1-12之间的数字", i + 2));
                        failCount++;
                        continue;
                    }
                    if (dto.getEndPeriod() == null || dto.getEndPeriod() < 1 || dto.getEndPeriod() > 12) {
                        errorMessages.add(String.format("第%d行：结束节次必须是1-12之间的数字", i + 2));
                        failCount++;
                        continue;
                    }
                    if (dto.getEndPeriod() < dto.getStartPeriod()) {
                        errorMessages.add(String.format("第%d行：结束节次必须大于或等于开始节次", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getClassroom())) {
                        errorMessages.add(String.format("第%d行：教室不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    
                    CourseSchedule courseSchedule = new CourseSchedule();
                    courseSchedule.setCourseName(dto.getCourseName().trim());
                    courseSchedule.setCourseNo(isBlank(dto.getCourseNo()) ? null : dto.getCourseNo().trim());
                    courseSchedule.setOrderNo(isBlank(dto.getOrderNo()) ? null : dto.getOrderNo().trim());
                    courseSchedule.setTeacherNo(teacherNo); // 使用当前登录教师的工号
                    courseSchedule.setClassName(dto.getClassName().trim());
                    courseSchedule.setWeekday(dto.getWeekday());
                    courseSchedule.setWeekRange(dto.getWeekRange().trim());
                    courseSchedule.setStartPeriod(dto.getStartPeriod());
                    courseSchedule.setEndPeriod(dto.getEndPeriod());
                    courseSchedule.setClassroom(dto.getClassroom().trim());
                    
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
        
        // 教师工号条件
        if (!isBlank(queryDTO.getTeacherNo())) {
            queryWrapper.eq(CourseSchedule::getTeacherNo, queryDTO.getTeacherNo().trim());
        }
        
        // 班级名称条件（模糊查询）
        if (!isBlank(queryDTO.getClassName())) {
            queryWrapper.like(CourseSchedule::getClassName, queryDTO.getClassName().trim());
        }
        
        // 课程名称条件（模糊查询）
        if (!isBlank(queryDTO.getCourseName())) {
            queryWrapper.like(CourseSchedule::getCourseName, queryDTO.getCourseName().trim());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(CourseSchedule::getCreateTime);
        
        log.info("查询课表，条件：teacherNo={}, className={}, courseName={}, pageNum={}, pageSize={}", 
                queryDTO.getTeacherNo(), queryDTO.getClassName(), queryDTO.getCourseName(), pageNum, pageSize);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("课表导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 创建模板数据
            List<CourseScheduleExcelDTO> templateData = new ArrayList<>();
            CourseScheduleExcelDTO example = new CourseScheduleExcelDTO();
            example.setCourseName("高等数学");
            example.setCourseNo("MATH101");
            example.setOrderNo("01");
            example.setClassName("25计算机类-1班");
            example.setWeekday(1); // 星期一
            example.setWeekRange("3-16周"); // 周次范围
            example.setStartPeriod(1);
            example.setEndPeriod(2);
            example.setClassroom("成都校区/思学楼/A101");
            // 不再包含教师ID，将由系统根据当前登录教师自动填充
            templateData.add(example);
            
            // 写入Excel
            EasyExcel.write(response.getOutputStream(), CourseScheduleExcelDTO.class)
                    .sheet("课表模板")
                    .doWrite(templateData);
            
            log.info("课表导入模板下载成功");
        } catch (Exception e) {
            log.error("生成课表导入模板失败", e);
            throw new com.xuegongbu.common.exception.BusinessException("生成课表导入模板失败: " + e.getMessage());
        }
    }
}
