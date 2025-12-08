package com.xuegongbu.mapper;

import com.xuegongbu.domain.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {
    Admin findByUsername(@Param("username") String username);
    
    Admin findById(@Param("id") Long id);
    
    int insert(Admin admin);
    
    int update(Admin admin);
}
