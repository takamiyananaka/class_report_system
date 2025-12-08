package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.Course;
import com.xuegongbu.mapper.CourseMapper;
import com.xuegongbu.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importCoursesFromExcel(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".xlsx")) {
            throw new BusinessException("只支持.xlsx格式的Excel文件");
        }

        List<Course> courses = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            
            log.info("开始解析Excel文件，共{}行数据", rowCount);
            
            // 从第二行开始读取（第一行是表头）
            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                try {
                    Course course = new Course();
                    
                    // 课程名称 (必填)
                    Cell cell0 = row.getCell(0);
                    if (cell0 == null || cell0.getCellType() == CellType.BLANK) {
                        log.warn("第{}行：课程名称为空，跳过", i + 1);
                        continue;
                    }
                    course.setCourseName(getCellValueAsString(cell0));
                    
                    // 课程编码
                    Cell cell1 = row.getCell(1);
                    if (cell1 != null && cell1.getCellType() != CellType.BLANK) {
                        course.setCourseCode(getCellValueAsString(cell1));
                    }
                    
                    // 教师ID (必填)
                    Cell cell2 = row.getCell(2);
                    if (cell2 == null || cell2.getCellType() == CellType.BLANK) {
                        log.warn("第{}行：教师ID为空，跳过", i + 1);
                        continue;
                    }
                    try {
                        course.setTeacherId(Long.valueOf(getCellValueAsString(cell2)));
                    } catch (NumberFormatException e) {
                        log.warn("第{}行：教师ID格式错误，跳过", i + 1);
                        continue;
                    }
                    
                    // 教室号 (必填)
                    Cell cell3 = row.getCell(3);
                    if (cell3 == null || cell3.getCellType() == CellType.BLANK) {
                        log.warn("第{}行：教室号为空，跳过", i + 1);
                        continue;
                    }
                    course.setClassroom(getCellValueAsString(cell3));
                    
                    // 上课时间 (必填)
                    Cell cell4 = row.getCell(4);
                    if (cell4 == null || cell4.getCellType() == CellType.BLANK) {
                        log.warn("第{}行：上课时间为空，跳过", i + 1);
                        continue;
                    }
                    course.setCourseTime(getCellValueAsString(cell4));
                    
                    // 上课日期 (必填)
                    Cell cell5 = row.getCell(5);
                    if (cell5 == null || cell5.getCellType() == CellType.BLANK) {
                        log.warn("第{}行：上课日期为空，跳过", i + 1);
                        continue;
                    }
                    LocalDate courseDate = parseDateCell(cell5);
                    if (courseDate == null) {
                        log.warn("第{}行：上课日期格式错误，跳过", i + 1);
                        continue;
                    }
                    course.setCourseDate(courseDate);
                    
                    // 开始时间
                    Cell cell6 = row.getCell(6);
                    if (cell6 != null && cell6.getCellType() != CellType.BLANK) {
                        LocalTime startTime = parseTimeCell(cell6);
                        course.setStartTime(startTime);
                    }
                    
                    // 结束时间
                    Cell cell7 = row.getCell(7);
                    if (cell7 != null && cell7.getCellType() != CellType.BLANK) {
                        LocalTime endTime = parseTimeCell(cell7);
                        course.setEndTime(endTime);
                    }
                    
                    // 星期几
                    Cell cell8 = row.getCell(8);
                    if (cell8 != null && cell8.getCellType() != CellType.BLANK) {
                        try {
                            course.setWeekDay(Integer.valueOf(getCellValueAsString(cell8)));
                        } catch (NumberFormatException e) {
                            log.warn("第{}行：星期几格式错误", i + 1);
                        }
                    }
                    
                    // 预到人数
                    Cell cell9 = row.getCell(9);
                    if (cell9 != null && cell9.getCellType() != CellType.BLANK) {
                        try {
                            course.setExpectedCount(Integer.valueOf(getCellValueAsString(cell9)));
                        } catch (NumberFormatException e) {
                            log.warn("第{}行：预到人数格式错误", i + 1);
                        }
                    }
                    
                    // 学期
                    Cell cell10 = row.getCell(10);
                    if (cell10 != null && cell10.getCellType() != CellType.BLANK) {
                        course.setSemester(getCellValueAsString(cell10));
                    }
                    
                    // 状态
                    Cell cell11 = row.getCell(11);
                    if (cell11 != null && cell11.getCellType() != CellType.BLANK) {
                        try {
                            course.setStatus(Integer.valueOf(getCellValueAsString(cell11)));
                        } catch (NumberFormatException e) {
                            log.warn("第{}行：状态格式错误，使用默认值", i + 1);
                            course.setStatus(2); // 默认未开始
                        }
                    } else {
                        course.setStatus(2); // 默认未开始
                    }
                    
                    // 备注
                    Cell cell12 = row.getCell(12);
                    if (cell12 != null && cell12.getCellType() != CellType.BLANK) {
                        course.setRemark(getCellValueAsString(cell12));
                    }
                    
                    // 删除标记，默认为0
                    course.setIsDeleted(0);
                    
                    courses.add(course);
                    
                } catch (Exception e) {
                    log.error("第{}行数据解析失败: {}", i + 1, e.getMessage());
                }
            }
        }
        
        if (courses.isEmpty()) {
            throw new BusinessException("没有有效的课程数据可导入");
        }
        
        // 批量插入
        boolean success = this.saveBatch(courses);
        if (!success) {
            throw new BusinessException("批量导入课程失败");
        }
        
        log.info("成功导入{}条课程数据", courses.size());
        return courses.size();
    }

    @Override
    public Course findById(Long id) {
        return this.getById(id);
    }

    @Override
    public List<Course> findAll() {
        return this.list();
    }

    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // 去除数字的小数点
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    }
                    return String.valueOf(value);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 解析日期单元格
     */
    private LocalDate parseDateCell(Cell cell) {
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } else {
                String dateStr = getCellValueAsString(cell);
                // 尝试解析常见日期格式
                return LocalDate.parse(dateStr);
            }
        } catch (Exception e) {
            String cellValue = getCellValueAsString(cell);
            log.error("日期解析失败，输入值: {}, 错误: {}", cellValue, e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析时间单元格
     */
    private LocalTime parseTimeCell(Cell cell) {
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            } else {
                String timeStr = getCellValueAsString(cell);
                // 尝试解析常见时间格式
                return LocalTime.parse(timeStr);
            }
        } catch (Exception e) {
            String cellValue = getCellValueAsString(cell);
            log.error("时间解析失败，输入值: {}, 错误: {}", cellValue, e.getMessage());
            return null;
        }
    }
}
