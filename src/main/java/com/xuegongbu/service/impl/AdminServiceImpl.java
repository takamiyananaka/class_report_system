package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.domain.Admin;
import com.xuegongbu.mapper.AdminMapper;
import com.xuegongbu.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Admin findById(Long id) {
        // Use MyBatis-Plus's inherited getById method instead of mapper.selectById
        return this.getById(id);
    }
}
