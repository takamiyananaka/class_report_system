package com.xuegongbu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.Semester;
import com.xuegongbu.dto.SemesterQueryDTO;
import com.xuegongbu.dto.SemesterRequest;
import com.xuegongbu.mapper.SemesterMapper;
import com.xuegongbu.service.SemesterService;
import com.xuegongbu.vo.SemesterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学期Service实现类
 */
@Slf4j
@Service
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements SemesterService {

    @Override
    public Page<Semester> queryPage(SemesterQueryDTO queryDTO) {
        // 设置分页参数
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        Page<Semester> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Semester> queryWrapper = new LambdaQueryWrapper<>();

        // 学期名条件（模糊查询）
        if (queryDTO.getSemesterName() != null && !queryDTO.getSemesterName().trim().isEmpty()) {
            queryWrapper.like(Semester::getSemesterName, queryDTO.getSemesterName().trim());
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Semester::getCreateTime);

        log.info("查询学期，条件：semesterName={}, pageNum={}, pageSize={}", 
                queryDTO.getSemesterName(), pageNum, pageSize);

        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSemester(SemesterRequest semesterRequest) {
        // 检查学期名是否已存在
        LambdaQueryWrapper<Semester> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Semester::getSemesterName, semesterRequest.getSemesterName());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("学期名已存在：" + semesterRequest.getSemesterName());
        }

        // 创建学期对象
        Semester semester = new Semester();
        BeanUtils.copyProperties(semesterRequest, semester);

        log.info("添加学期：{}", semester);

        return this.save(semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSemester(String id, SemesterRequest semesterRequest) {
        // 检查学期是否存在
        Semester existingSemester = this.getById(id);
        if (existingSemester == null) {
            throw new BusinessException("学期不存在，ID：" + id);
        }

        // 检查学期名是否已被其他学期使用
        LambdaQueryWrapper<Semester> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Semester::getSemesterName, semesterRequest.getSemesterName());
        queryWrapper.ne(Semester::getId, id); // 排除当前学期
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("学期名已存在：" + semesterRequest.getSemesterName());
        }

        // 更新学期对象
        BeanUtils.copyProperties(semesterRequest, existingSemester);
        existingSemester.setId(id);

        log.info("更新学期，ID：{}，信息：{}", id, existingSemester);

        return this.updateById(existingSemester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSemester(String id) {
        // 检查学期是否存在
        Semester existingSemester = this.getById(id);
        if (existingSemester == null) {
            throw new BusinessException("学期不存在，ID：" + id);
        }

        log.info("删除学期，ID：{}", id);

        return this.removeById(id);
    }

    @Override
    public List<SemesterVO> getAllSemesters() {
        // 查询所有未删除的学期
        LambdaQueryWrapper<Semester> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Semester::getCreateTime);

        List<Semester> semesters = this.list(queryWrapper);

        // 转换为VO对象
        return semesters.stream().map(semester -> {
            SemesterVO vo = new SemesterVO();
            BeanUtils.copyProperties(semester, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SemesterVO getSemesterById(String id) {
        Semester semester = this.getById(id);
        if (semester == null) {
            return null;
        }

        SemesterVO vo = new SemesterVO();
        BeanUtils.copyProperties(semester, vo);
        return vo;
    }
}