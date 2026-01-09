package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.Class;
import com.xuegongbu.dto.ClassQueryDTO;
import com.xuegongbu.vo.ClassVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ClassService extends IService<Class> {
    
    /**
     * 从Excel文件导入班级数据
     * @param file Excel文件
     * @param teacherNo 辅导员工号（从前端传入）
     * @return 导入结果
     */
    Map<String, Object> importFromExcel(MultipartFile file, String teacherNo);
    
    /**
     * 添加单个班级
     * @param classEntity 班级信息
     * @param teacherNo 辅导员工号（从前端传入）
     * @return 添加的班级对象
     */
    Class addClass(Class classEntity, String teacherNo);
    
    /**
     * 分页查询班级
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<ClassVO> queryPage(ClassQueryDTO queryDTO);
    
    /**
     * 下载班级导入模板
     * @param response HTTP响应对象
     */
    void downloadTemplate(jakarta.servlet.http.HttpServletResponse response);
}
