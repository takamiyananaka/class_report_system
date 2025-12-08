package com.xuegongbu.service;

import com.xuegongbu.domain.Admin;

public interface AdminService {
    Admin findByUsername(String username);
    
    Admin findById(Long id);
}
