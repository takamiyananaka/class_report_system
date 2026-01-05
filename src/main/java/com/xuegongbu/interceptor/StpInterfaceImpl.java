package com.xuegongbu.interceptor;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object o, String s) {
        return List.of("*");
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        List<String> list = new ArrayList<>();
        
        // 从会话中获取角色
        Object roleObj = StpUtil.getSession().get("role");
        if (roleObj == null) {
            log.warn("用户 {} 未设置角色", o);
            return list;
        }
        
        String role = roleObj.toString();
        log.info("用户 {} 的角色: {}", o, role);
        
        // 添加当前角色
        list.add(role);
        
        // 实现角色向上兼容：teacher < college_admin < admin
        // 如果是college_admin，自动拥有teacher权限
        if ("college_admin".equals(role)) {
            list.add("teacher");
        }
        // 如果是admin，自动拥有college_admin和teacher权限
        else if ("admin".equals(role)) {
            list.add("college_admin");
            list.add("teacher");
        }
        
        log.info("用户 {} 拥有的有效角色列表: {}", o, list);
        return list;
    }
}
