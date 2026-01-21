package com.xuegongbu.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.Result;
import com.xuegongbu.common.ResultCode;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.College;
import com.xuegongbu.domain.CollegeAdmin;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.dto.TeacherExcelDTO;
import com.xuegongbu.dto.TeacherQueryDTO;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.service.CollegeService;
import com.xuegongbu.service.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    // BCrypt密码加密器 - 用于验证数据库中BCrypt加密的密码
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CollegeService collegeService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 根据用户名查询教师
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teacher::getUsername, loginRequest.getUsername());
        Teacher teacher = this.getOne(queryWrapper);

        if (teacher == null) {
            throw new BusinessException("用户名错误");
        }

        // 检查教师状态
        if (teacher.getStatus() != null && teacher.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 验证密码 - 使用BCrypt验证明文密码与数据库中的加密密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), teacher.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 更新最后登录时间
        teacher.setLastLoginTime(LocalDateTime.now());
        this.updateById(teacher);

        // Sa-Token 登录认证，使用教师工号作为登录标识，并存储完整的用户信息
        StpUtil.login(teacher.getTeacherNo(),"teacher");
        StpUtil.getSession().set("role", "teacher");
        StpUtil.getSession().set("userInfo", teacher);
        String token = StpUtil.getTokenValue();

        // 构造用户信息（不包含密码）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", teacher.getId());
        userInfo.put("username", teacher.getUsername());
        userInfo.put("realName", teacher.getRealName());
        userInfo.put("teacherNo", teacher.getTeacherNo());
        userInfo.put("phone", teacher.getPhone());
        userInfo.put("email", teacher.getEmail());
        userInfo.put("department", teacher.getDepartment());
        userInfo.put("collegeNo", teacher.getCollegeNo());
        userInfo.put("role", "teacher");
        userInfo.put("attendanceThreshold",teacher.getAttendanceThreshold());
        userInfo.put("status", teacher.getStatus());
        userInfo.put("lastLoginTime", teacher.getLastLoginTime());
        userInfo.put("enableEmailNotification", teacher.getEnableEmailNotification());
        userInfo.put("lastLoginIp", teacher.getLastLoginIp());
        userInfo.put("remark", teacher.getRemark());
        log.info("教师登录成功: {}", teacher.getUsername());
        return new LoginResponse(token, userInfo);
    }

    @Override
    public Page<Teacher> queryPage(TeacherQueryDTO queryDTO) {
        // 设置分页参数
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        Page<Teacher> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
        
        // 教师工号条件（精确查询）
        if (queryDTO.getTeacherNo() != null && !queryDTO.getTeacherNo().trim().isEmpty()) {
            queryWrapper.eq(Teacher::getTeacherNo, queryDTO.getTeacherNo().trim());
        }

        // 真实姓名条件（模糊查询）
        if (queryDTO.getRealName() != null && !queryDTO.getRealName().trim().isEmpty()) {
            queryWrapper.like(Teacher::getRealName, queryDTO.getRealName().trim());
        }

        Object roleObj = StpUtil.getSession().get("role");
        String role = roleObj != null ? roleObj.toString() : null;
        if("college_admin".equals(role)){
            College college = (College) StpUtil.getSession().get("collegeInfo");
            queryWrapper.eq(Teacher::getCollegeNo, college.getCollegeNo());
        }

        //学院条件（精确查询）
        if (queryDTO.getDepartment() != null && !queryDTO.getDepartment().trim().isEmpty()) {
            LambdaQueryWrapper<College> collegeQueryWrapper = new LambdaQueryWrapper<>();
            collegeQueryWrapper.eq(College::getName, queryDTO.getDepartment());
            College college = collegeService.getOne(collegeQueryWrapper);
            String collegeNo = college != null ? college.getCollegeNo() : null;
            queryWrapper.eq(Teacher::getCollegeNo, collegeNo);
        }

        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Teacher::getCreateTime);
        
       Page<Teacher> result = this.page(page, queryWrapper);
        
        // 移除密码字段
        result.getRecords().forEach(teacher -> teacher.setPassword(null));
        log.info("查询教师列表成功: {}", result);
        
        return result;
    }

    @Override
    public Result<String> addTeacher(Teacher teacher, String collegeNo) {
        // 检查用户名是否已存在
        Teacher existingTeacher = lambdaQuery()
                .eq(Teacher::getUsername, teacher.getUsername())
                .one();
        if (existingTeacher != null) {
            return Result.error("用户名已存在");
        }

        // 检查教师工号是否已存在
        Teacher existingTeacherNo = lambdaQuery()
                .eq(Teacher::getTeacherNo, teacher.getTeacherNo())
                .one();
        if (existingTeacherNo != null) {
            return Result.error("教师工号已存在");
        }

        // 验证密码
        if (teacher.getPassword() == null || teacher.getPassword().isEmpty()) {
            return Result.error("密码不能为空，请设置初始密码");
        }
        if (teacher.getPassword().length() < 6) {
            return Result.error("密码长度不能少于6位");
        }

        // 密码加密
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));

        // 设置为指定学院
        teacher.setCollegeNo(collegeNo);
        // 默认状态为启用
        if (teacher.getStatus() == null) {
            teacher.setStatus(1);
        }

        save(teacher);
        log.info("创建教师成功，ID：{}", teacher.getId());
        return Result.success("创建成功");
    }

    @Override
    public Result<String> importTeachers(MultipartFile file) {
        try {
            // 读取Excel文件
            List<TeacherExcelDTO> teacherExcelList = EasyExcel.read(file.getInputStream())
                    .head(TeacherExcelDTO.class)
                    .sheet()
                    .doReadSync();

            if (teacherExcelList.isEmpty()) {
                return Result.error("Excel文件中没有数据");
            }
            
            // 预加载所有学院信息到Map，避免N+1查询问题
            List<College> allColleges = collegeService.list();
            Map<String, College> collegeMap = new HashMap<>();
            for (College college : allColleges) {
                if (college.getName() != null) {
                    collegeMap.put(college.getName().trim(), college);
                }
            }
            
            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            // 逐行处理数据
            for (int i = 0; i < teacherExcelList.size(); i++) {
                int rowNum = i + 2; // Excel行号从2开始（第1行是表头）
                
                try {
                    TeacherExcelDTO dto = teacherExcelList.get(i);
                    
                    // 验证必填字段是否完整
                    if (dto.getTeacherNo() == null || dto.getTeacherNo().trim().isEmpty()) {
                        errorMessages.add(String.format("第%d行导入失败，工号为空", rowNum));
                        failCount++;
                        continue;
                    }
                    if (dto.getRealName() == null || dto.getRealName().trim().isEmpty()) {
                        errorMessages.add(String.format("第%d行导入失败，真实姓名为空", rowNum));
                        failCount++;
                        continue;
                    }
                    if (dto.getDepartment() == null || dto.getDepartment().trim().isEmpty()) {
                        errorMessages.add(String.format("第%d行导入失败，学院名为空", rowNum));
                        failCount++;
                        continue;
                    }
                    
                    // 通过学院名从预加载的Map中查询学院信息
                    College college = collegeMap.get(dto.getDepartment().trim());
                    
                    // 如果学院名有误，查询不到学院ID
                    if (college == null) {
                        errorMessages.add(String.format("第%d行导入失败，学院名有误", rowNum));
                        failCount++;
                        log.warn("第{}行: 学院名'{}'查询不到对应学院，跳过该行", rowNum, dto.getDepartment().trim());
                        continue;
                    }
                    
                    // 检查工号是否已存在
                    Teacher existingTeacher = lambdaQuery()
                            .eq(Teacher::getTeacherNo, dto.getTeacherNo().trim())
                            .one();
                    
                    if (existingTeacher != null) {
                        log.info("第{}行：教师工号已存在，跳过", rowNum);
                        // 不增加failCount，也不添加到错误消息
                        continue;
                    }
                    
                    // 创建教师对象
                    Teacher teacher = new Teacher();
                    teacher.setTeacherNo(dto.getTeacherNo().trim());
                    teacher.setRealName(dto.getRealName().trim());
                    teacher.setUsername(dto.getTeacherNo().trim()); // 用户名为工号
                    teacher.setPassword("123456"); // 初始密码为123456（将由addTeacher加密）
                    teacher.setDepartment(college.getName() != null ? college.getName().trim() : null);
                    teacher.setPhone(null); // 手机号初始为空
                    teacher.setEmail(null); // 邮箱初始为空
                    teacher.setEnableEmailNotification(Boolean.valueOf("1")); // 邮箱通知默认开启
                    teacher.setAttendanceThreshold(java.math.BigDecimal.valueOf(0.90)); // 考勤阈值默认0.90
                    teacher.setStatus(1); // 默认启用状态
                    
                    // 使用统一的addTeacher方法
                    Result<String> result = addTeacher(teacher, college.getCollegeNo().trim());
                    if (result != null && ResultCode.SUCCESS.getCode().equals(result.getCode())) {
                        successCount++;
                        log.debug("第{}行: 成功导入教师，工号：{}，姓名：{}，学院：{}", 
                                rowNum, dto.getTeacherNo().trim(), dto.getRealName().trim(), dto.getDepartment().trim());
                    } else {
                        String errorMsg = result != null ? result.getMessage() : "未知错误";
                        errorMessages.add(String.format("第%d行导入失败: %s", rowNum, errorMsg));
                        failCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("处理第{}行数据时出错: {}", rowNum, e.getMessage(), e);
                    errorMessages.add(String.format("第%d行导入失败，数据格式错误", rowNum));
                    failCount++;
                }
            }

            log.info("批量导入教师完成，成功{}个，失败{}个", successCount, failCount);
            
            String message = String.format("批量导入完成，成功导入%d个教师", successCount);
            if (failCount > 0) {
                message += String.format("，失败%d个", failCount);
            }
            
            if (!errorMessages.isEmpty()) {
                // 如果有错误消息，将其附加到结果中
                message += "。错误详情：" + String.join("；", errorMessages);
            }
            
            return Result.success(message);
        } catch (IOException e) {
            log.error("批量导入教师时发生错误", e);
            return Result.error("文件读取失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("批量导入教师时发生错误", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }

}