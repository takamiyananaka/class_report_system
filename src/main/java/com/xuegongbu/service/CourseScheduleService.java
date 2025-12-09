package com.xuegongbu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuegongbu.domain.CourseSchedule;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


public interface CourseScheduleService extends IService<CourseSchedule> {
    
    /**
     * 从Excel文件导入课表数据
     * @param file Excel文件
     * @return 导入结果
     */
    Map<String, Object> importFromExcel(MultipartFile file);
}
