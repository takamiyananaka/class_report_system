package com.xuegongbu.service;

import com.xuegongbu.entity.Admin;

public interface AdminService {
    Admin findByUsername(String username);
    
    Admin findById(Long id);
}
