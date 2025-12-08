package com.xuegongbu.service.impl;

import com.xuegongbu.domain.Admin;
import com.xuegongbu.mapper.AdminMapper;
import com.xuegongbu.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Admin findByUsername(String username) {
        return adminMapper.findByUsername(username);
    }

    @Override
    public Admin findById(Long id) {
        return adminMapper.findById(id);
    }
}
