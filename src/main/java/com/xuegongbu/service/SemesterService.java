package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Semester;
import com.xuegongbu.dto.SemesterQueryDTO;
import com.xuegongbu.dto.SemesterRequest;
import com.xuegongbu.vo.SemesterVO;

import java.util.List;

/**
 * 学期Service接口
 */
public interface SemesterService extends IService<Semester> {

    /**
     * 分页查询学期
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<Semester> queryPage(SemesterQueryDTO queryDTO);

    /**
     * 添加学期
     * @param semesterRequest 学期信息
     * @return 添加结果
     */
    boolean addSemester(SemesterRequest semesterRequest);

    /**
     * 更新学期
     * @param id 学期ID
     * @param semesterRequest 学期信息
     * @return 更新结果
     */
    boolean updateSemester(String id, SemesterRequest semesterRequest);

    /**
     * 删除学期
     * @param id 学期ID
     * @return 删除结果
     */
    boolean deleteSemester(String id);

    /**
     * 查询所有学期
     * @return 学期列表
     */
    List<SemesterVO> getAllSemesters();

    /**
     * 根据ID获取学期信息
     * @param id 学期ID
     * @return 学期信息
     */
    SemesterVO getSemesterById(String id);
}