package com.xuegongbu.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Semester;
import com.xuegongbu.dto.SemesterQueryDTO;
import com.xuegongbu.dto.SemesterRequest;
import com.xuegongbu.service.SemesterService;
import com.xuegongbu.vo.SemesterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学期控制器
 */
@Slf4j
@RestController
@RequestMapping("/semester")
@Tag(name = "学期管理", description = "学期相关接口")
public class SemesterController {

    @Autowired
    private SemesterService semesterService;

    /**
     * 分页查询学期
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询学期", description = "根据条件分页查询学期信息")
    @SaCheckRole("admin")
    public Result<Page<Semester>> query(@RequestBody SemesterQueryDTO queryDTO) {
        log.info("分页查询学期，条件：{}", queryDTO);
        Page<Semester> pageResult = semesterService.queryPage(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取所有学期名
     */
    @GetMapping("/names")
    @Operation(summary = "获取所有学期名", description = "获取所有学期名")
    public Result<List<String>> names() {
        log.info("获取所有学期名");
        List<String> names = semesterService.list().stream().map(Semester::getSemesterName).toList();
        return Result.success(names);
    }

    /**
     * 添加学期
     */
    @PostMapping("/add")
    @Operation(summary = "添加学期", description = "添加新的学期信息")
    @SaCheckRole("admin")
    public Result<String> add(@RequestBody SemesterRequest semesterRequest) {
        log.info("添加学期，信息：{}", semesterRequest);
        boolean result = semesterService.addSemester(semesterRequest);
        return result ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 更新学期
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "更新学期", description = "根据ID更新学期信息")
    @SaCheckRole("admin")
    public Result<String> update(@PathVariable String id, @RequestBody SemesterRequest semesterRequest) {
        log.info("更新学期，ID：{}，信息：{}", id, semesterRequest);
        boolean result = semesterService.updateSemester(id, semesterRequest);
        return result ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除学期
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除学期", description = "根据ID删除学期信息")
    @SaCheckRole("admin")
    public Result<String> delete(@PathVariable String id) {
        log.info("删除学期，ID：{}", id);
        boolean result = semesterService.deleteSemester(id);
        return result ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 查询所有学期
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有学期", description = "查询所有学期信息,用于添加课程时获取学期列表")
    @SaCheckRole("admin")
    public Result<List<SemesterVO>> list() {
        log.info("查询所有学期");
        List<SemesterVO> semesters = semesterService.getAllSemesters();
        return Result.success(semesters);
    }

    /**
     * 根据ID获取学期信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取学期信息", description = "根据ID获取学期详细信息")
    @SaCheckRole("admin")
    public Result<SemesterVO> getById(@PathVariable String id) {
        log.info("根据ID获取学期信息，ID：{}", id);
        SemesterVO semester = semesterService.getSemesterById(id);
        return Result.success(semester);
    }
}