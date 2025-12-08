package com.xuegongbu.security;

import com.xuegongbu.common.Constants;
import com.xuegongbu.domain.Admin;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.mapper.AdminMapper;
import com.xuegongbu.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 尝试从管理员表查找
        Admin admin = adminMapper.findByUsername(username);
        if (admin != null) {
            return new SecurityUser(
                    admin.getId(),
                    admin.getUsername(),
                    admin.getPassword(),
                    Constants.ROLE_ADMIN,
                    admin.getStatus()
            );
        }

        // 尝试从教师表查找
        Teacher teacher = teacherMapper.findByUsername(username);
        if (teacher != null) {
            return new SecurityUser(
                    teacher.getId(),
                    teacher.getUsername(),
                    teacher.getPassword(),
                    Constants.ROLE_TEACHER,
                    teacher.getStatus()
            );
        }

        throw new UsernameNotFoundException("用户不存在：" + username);
    }
}
