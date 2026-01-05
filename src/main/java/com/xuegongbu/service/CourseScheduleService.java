package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.dto.CourseScheduleQueryDTO;
import com.xuegongbu.dto.CourseScheduleVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


public interface CourseScheduleService extends IService<CourseSchedule> {
    
    /**
     * 从Excel文件导入课表数据
     * @param file Excel文件
     * @return 导入结果
     */
    Map<String, Object> importFromExcel(MultipartFile file);
    
    /**
     * 分页查询课表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<CourseScheduleVO> queryPage(CourseScheduleQueryDTO queryDTO);
    
    /**
     * 下载课表导入模板
     * @param response HTTP响应对象
     */
    void downloadTemplate(jakarta.servlet.http.HttpServletResponse response);

    Result<String> addClass(List<String> classList, String courseId);

    Page<CourseScheduleVO> queryByClass(String id,int pageNum,int pageSize);

    List<String> queryClassCurrentCourse(String classId);
}
