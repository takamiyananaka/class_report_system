package com.xuegongbu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Constants;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Class;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.ClassQueryDTO;
import com.xuegongbu.service.ClassService;
import com.xuegongbu.service.TeacherService;
import com.xuegongbu.util.AuthenticationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 班级管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/class")
@Api(tags = "班级管理")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private TeacherService teacherService;

    /**
     * Excel导入班级
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Excel导入班级", notes = "通过上传Excel文件批量导入班级数据。辅导员工号自动从当前登录用户获取。Excel格式要求：第一行为表头，列顺序为：班级名称、班级人数")
    public Result<Map<String, Object>> importFromExcel(
            @Parameter(description = "Excel文件", required = true, 
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file) {
        try {
            log.info("开始导入班级，文件名：{}", file.getOriginalFilename());
            
            // 获取当前登录教师的ID，如果未登录则使用默认值
            Long teacherId = AuthenticationUtil.getCurrentTeacherNo();
            log.info("当前教师ID: {}", teacherId);
            
            // 根据教师ID查询教师工号
            Teacher teacher = teacherService.getById(teacherId);
            String teacherNo;
            if (teacher == null) {
                // 如果找不到教师信息，使用默认值
                teacherNo = Constants.DEFAULT_TEACHER_NO_STR;
                log.warn("未找到教师信息，使用默认教师工号: {}", teacherNo);
            } else {
                teacherNo = teacher.getTeacherNo();
                log.info("当前登录教师工号: {}", teacherNo);
            }
            
            Map<String, Object> result = classService.importFromExcel(file, teacherNo);
            log.info("班级导入完成：{}", result.get("message"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("班级导入失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询班级
     */
    @GetMapping("/query")
    @ApiOperation(value = "分页查询班级", notes = "分页查询班级，支持多条件查询。可通过className（模糊）、teacherNo等参数进行过滤查询。不提供任何条件则查询全部")
    public Result<Page<Class>> query(ClassQueryDTO queryDTO) {
        log.info("查询班级请求，参数：{}", queryDTO);
        Page<Class> result = classService.queryPage(queryDTO);
        log.info("查询班级完成，共{}条记录，当前第{}页", result.getTotal(), result.getCurrent());
        return Result.success(result);
    }

    /**
     * 创建班级
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建班级", notes = "创建新班级，ID自动生成")
    public Result<Class> addClass(@RequestBody Class classEntity) {
        log.info("创建班级，班级信息：{}", classEntity);
        
        // 验证必填字段
        if (classEntity.getClass_name() == null || classEntity.getClass_name().trim().isEmpty()) {
            return Result.error("班级名称不能为空");
        }
        if (classEntity.getTeacher_no() == null || classEntity.getTeacher_no().trim().isEmpty()) {
            return Result.error("辅导员工号不能为空");
        }
        if (classEntity.getCount() == null || classEntity.getCount() <= 0) {
            return Result.error("班级人数必须大于0");
        }
        
        // ID会由MyBatis-Plus自动生成（雪花算法）
        classService.save(classEntity);
        log.info("创建班级完成，班级ID：{}", classEntity.getId());
        return Result.success(classEntity);
    }

    /**
     * 更新班级
     */
    @PutMapping("/update")
    @ApiOperation(value = "更新班级", notes = "通过班级名称更新班级信息")
    public Result<String> updateClass(@RequestBody Class classEntity) {
        log.info("更新班级，班级名称：{}，班级信息：{}", classEntity.getClass_name(), classEntity);
        
        if (classEntity.getClass_name() == null || classEntity.getClass_name().trim().isEmpty()) {
            return Result.error("班级名称不能为空");
        }
        
        // 根据班级名称查询班级
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(Class::getClass_name, classEntity.getClass_name().trim());
        
        Class existing = classService.getOne(queryWrapper);
        if (existing == null) {
            return Result.error("班级不存在");
        }
        
        // 设置ID以便更新
        classEntity.setId(existing.getId());
        
        classService.updateById(classEntity);
        log.info("更新班级完成");
        return Result.success("更新成功");
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除班级", notes = "通过班级名称删除班级（逻辑删除）")
    public Result<String> deleteClass(@RequestParam(value = "className", required = true) String className) {
        log.info("删除班级，班级名称：{}", className);
        
        // 根据班级名称查询班级
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(Class::getClass_name, className.trim());
        
        Class existing = classService.getOne(queryWrapper);
        if (existing == null) {
            return Result.error("班级不存在");
        }
        
        classService.removeById(existing.getId());
        log.info("删除班级完成");
        return Result.success("删除成功");
    }

    /**
     * 查询班级详情
     */
    @GetMapping("/get")
    @ApiOperation(value = "查询班级详情", notes = "根据班级名称查询班级详情")
    public Result<Class> getClass(@RequestParam(value = "className", required = true) String className) {
        log.info("查询班级详情，班级名称：{}", className);
        
        // 根据班级名称查询班级
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(Class::getClass_name, className.trim());
        
        Class classEntity = classService.getOne(queryWrapper);
        if (classEntity == null) {
            return Result.error("班级不存在");
        }
        log.info("查询班级详情完成");
        return Result.success(classEntity);
    }

    /**
     * 下载班级导入模板
     */
    @GetMapping("/downloadTemplate")
    @ApiOperation(value = "下载班级导入模板", notes = "下载Excel格式的班级导入模板文件")
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            log.info("下载班级导入模板");
            classService.downloadTemplate(response);
            log.info("下载班级导入模板完成");
        } catch (Exception e) {
            log.error("下载班级导入模板失败", e);
            throw new com.xuegongbu.common.exception.BusinessException("下载模板失败: " + e.getMessage());
        }
    }
}
