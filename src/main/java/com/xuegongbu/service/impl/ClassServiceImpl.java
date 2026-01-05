package com.xuegongbu.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Class;
import com.xuegongbu.dto.ClassExcelDTO;
import com.xuegongbu.dto.ClassQueryDTO;
import com.xuegongbu.mapper.ClassMapper;
import com.xuegongbu.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements ClassService {

    /**
     * 添加单个班级
     * @param className 班级名称
     * @param count 班级人数
     * @param grade 年级
     * @param major 专业
     * @param teacherNo 辅导员工号
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Class> addSingleClass(String className, Integer count, String grade, String major, String teacherNo) {
        try {
            // 验证必填字段
            if (isBlank(className)) {
                return Result.error("班级名称不能为空");
            }
            if (count == null || count <= 0) {
                return Result.error("班级人数必须大于0");
            }
            if (isBlank(grade)) {
                return Result.error("年级不能为空");
            }
            if (isBlank(major)) {
                return Result.error("专业不能为空");
            }
            if (isBlank(teacherNo)) {
                return Result.error("辅导员工号不能为空");
            }
            
            // 创建班级对象
            Class classEntity = new Class();
            classEntity.setClassName(className.trim());
            classEntity.setCount(count);
            classEntity.setGrade(grade.trim());
            classEntity.setMajor(major.trim());
            classEntity.setTeacherNo(teacherNo.trim());
            
            // 保存班级
            this.save(classEntity);
            
            return Result.success(classEntity);
            
        } catch (Exception e) {
            log.error("添加班级失败", e);
            return Result.error("添加班级失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(MultipartFile file, String teacherNo) {
        Map<String, Object> result = new HashMap<>();
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (teacherNo == null || teacherNo.trim().isEmpty()) {
            throw new IllegalArgumentException("辅导员工号不能为空");
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
            List<ClassExcelDTO> excelDataList = EasyExcel.read(file.getInputStream())
                    .head(ClassExcelDTO.class)
                    .sheet()
                    .doReadSync();
            
            if (excelDataList == null || excelDataList.isEmpty()) {
                throw new IllegalArgumentException("Excel文件中没有数据");
            }
            
            log.info("从Excel读取到 {} 条数据", excelDataList.size());
            
            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            // 逐行处理数据
            for (int i = 0; i < excelDataList.size(); i++) {
                int rowNum = i + 2; // Excel行号从2开始（第1行是表头）
                
                try {
                    ClassExcelDTO dto = excelDataList.get(i);
                    
                    // 验证必填字段是否完整
                    if (isBlank(dto.getClassName())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：班级名称不能为空", rowNum));
                        failCount++;
                        continue;
                    }
                    if (dto.getCount() == null || dto.getCount() <= 0) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：班级人数必须大于0", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getGrade())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：年级不能为空", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getMajor())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：专业不能为空", rowNum));
                        failCount++;
                        continue;
                    }
                    
                    // 调用添加班级方法
                    Result<Class> addResult = addSingleClass(
                        dto.getClassName(),
                        dto.getCount(),
                        dto.getGrade(),
                        dto.getMajor(),
                        teacherNo
                    );
                    
                    if (addResult.getCode() == 0) {
                        successCount++;
                    } else {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：%s", rowNum, addResult.getMessage()));
                        failCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("处理第{}行数据时出错: {}", rowNum, e.getMessage(), e);
                    errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                    failCount++;
                }
            }
            
            log.info("导入完成，成功：{}条，失败：{}条", successCount, failCount);
            
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalCount", excelDataList.size());
            result.put("message", String.format("成功导入%d条班级数据，失败%d条", successCount, failCount));
            
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
            log.error("导入班级数据失败", e);
            throw new IllegalStateException("导入班级数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查字符串是否为空
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    @Override
    public Page<Class> queryPage(ClassQueryDTO queryDTO) {
        // 设置分页参数
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        Page<Class> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<Class> queryWrapper = new LambdaQueryWrapper<>();
        
        // 班级名称条件（模糊查询）
        if (!isBlank(queryDTO.getClassName())) {
            queryWrapper.like(Class::getClassName, queryDTO.getClassName().trim());
        }
        
        // 辅导员工号条件
        if (!isBlank(queryDTO.getTeacherNo())) {
            queryWrapper.eq(Class::getTeacherNo, queryDTO.getTeacherNo().trim());
        }
        
        // 年级条件（多选）
        if (queryDTO.getGrades() != null && !queryDTO.getGrades().isEmpty()) {
            // 过滤掉空白字符串
            List<String> validGrades = queryDTO.getGrades().stream()
                    .filter(grade -> grade != null && !grade.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
            if (!validGrades.isEmpty()) {
                queryWrapper.in(Class::getGrade, validGrades);
            }
        }
        
        // 专业条件（多选）
        if (queryDTO.getMajors() != null && !queryDTO.getMajors().isEmpty()) {
            // 过滤掉空白字符串
            List<String> validMajors = queryDTO.getMajors().stream()
                    .filter(major -> major != null && !major.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
            if (!validMajors.isEmpty()) {
                queryWrapper.in(Class::getMajor, validMajors);
            }
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Class::getCreateTime);
        
        log.info("查询班级，条件：className={}, teacherNo={}, grades={}, majors={}, pageNum={}, pageSize={}", 
                queryDTO.getClassName(), queryDTO.getTeacherNo(), queryDTO.getGrades(), queryDTO.getMajors(), pageNum, pageSize);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("班级导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 创建模板数据
            List<ClassExcelDTO> templateData = new ArrayList<>();
            ClassExcelDTO example = new ClassExcelDTO();
            example.setClassName("25计算机类-1班");
            example.setCount(45);
            example.setGrade("2025级");
            example.setMajor("计算机科学与技术");
            templateData.add(example);
            
            // 写入Excel
            EasyExcel.write(response.getOutputStream(), ClassExcelDTO.class)
                    .sheet("班级模板")
                    .doWrite(templateData);
            
            log.info("班级导入模板下载成功");
        } catch (Exception e) {
            log.error("生成班级导入模板失败", e);
            throw new com.xuegongbu.common.exception.BusinessException("生成班级导入模板失败: " + e.getMessage());
        }
    }
}
