package com.xuegongbu.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.College;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.*;
import com.xuegongbu.service.CollegeService;
import com.xuegongbu.service.TeacherService;
import com.xuegongbu.vo.CollegeVO;
import com.xuegongbu.vo.TeacherVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;

@Slf4j
@RestController
@RequestMapping("/college")
@Tag(name = "学院管理", description = "学院相关接口,用于学校管理员对学院的管理")
public class CollegeController {

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * 查询所有学院
     */
    @GetMapping("/colleges")
    @Operation(summary = "查询所有学院", description = "管理员查询所有学院列表")
    @SaCheckRole("admin")
    public Result<List<CollegeVO>> listColleges() {
        log.info("管理员查询所有学院");
        List<College> colleges = collegeService.list();
        List<CollegeVO> collegeVOList = colleges.stream().map(college -> {
            CollegeVO vo = new CollegeVO();
            BeanUtils.copyProperties(college, vo);
            return vo;
        }).collect(Collectors.toList());
        log.info("查询到{}个学院", collegeVOList.size());
        return Result.success(collegeVOList);
    }

    

    /**
     * 根据ID查询学院
     */
    @GetMapping("/colleges/{id}")
    @Operation(summary = "根据ID查询学院", description = "管理员根据学院ID查询学院详情")
    @SaCheckRole("admin")
    public Result<CollegeVO> getCollege(@Parameter(description = "学院ID") @PathVariable String id) {
        log.info("管理员查询学院详情，ID：{}", id);
        College college = collegeService.getById(id);
        if (college == null) {
            return Result.error("学院不存在");
        }
        CollegeVO vo = new CollegeVO();
        BeanUtils.copyProperties(college, vo);
        return Result.success(vo);
    }

    /**
     * 创建学院
     */
    @PostMapping("/colleges")
    @Operation(summary = "创建学院", description = "管理员创建新学院，密码必须至少6位字符")
    @SaCheckRole("admin")
    public Result<String> createCollege(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "学院信息") @Valid @RequestBody CollegeRequest request) {
        log.info("管理员创建学院，学院名：{}", request.getName());

        // 检查学院号是否已存在
        College existingCollegeNo = collegeService.lambdaQuery()
                .eq(College::getCollegeNo, request.getCollegeNo())
                .one();
        if (existingCollegeNo != null) {
            return Result.error("学院号已存在");
        }
        College college = new College();
        college.setName(request.getName());
        college.setCollegeNo(request.getCollegeNo());
        college.setDescription(request.getDescription());
        collegeService.save(college);
        log.info("管理员创建学院成功，ID：{}", college.getId());
        return Result.success("创建成功");
    }

    /**
     * 更新学院
     */
    @PutMapping("/colleges/{id}")
    @Operation(summary = "更新学院", description = "管理员更新学院信息，如提供新密码则必须至少6位字符")
    @SaCheckRole("admin")
    public Result<String> updateCollege(@Parameter(description = "学院ID") @PathVariable String id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "学院信息") @Valid @RequestBody CollegeRequest request) {
        log.info("管理员更新学院，ID：{}", id);

        College college = collegeService.getById(id);
        if (college == null) {
            return Result.error("学院不存在");
        }

        // 检查学院号是否被其他学院使用
        College existingCollegeNo = collegeService.lambdaQuery()
                .eq(College::getCollegeNo, request.getCollegeNo())
                .ne(College::getId, id)
                .one();
        if (existingCollegeNo != null) {
            return Result.error("学院号已被其他学院使用");
        }

        // 更新字段
        college.setName(request.getName());
        college.setCollegeNo(request.getCollegeNo());
        college.setDescription(request.getDescription());

        collegeService.updateById(college);
        log.info("管理员更新学院成功");
        return Result.success("更新成功");
    }

    /**
     * 删除学院
     */
    @DeleteMapping("/colleges/{id}")
    @Operation(summary = "删除学院", description = "管理员删除学院")
    @SaCheckRole("admin")
    public Result<String> deleteCollege(@Parameter(description = "学院ID") @PathVariable String id) {
        log.info("管理员删除学院，ID：{}", id);

        College college = collegeService.getById(id);
        if (college == null) {
            return Result.error("学院不存在");
        }

        collegeService.removeById(id);
        log.info("管理员删除学院成功");
        return Result.success("删除成功");
    }
}
