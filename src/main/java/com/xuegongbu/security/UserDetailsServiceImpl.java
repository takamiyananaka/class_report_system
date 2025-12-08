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


        throw new UsernameNotFoundException("用户不存在：" + username);
    }
}
