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
        //会话中获取角色
        String role = StpUtil.getSession().get("role").toString();
        log.info("ro1le: {}", role);
        list.add(role);
        return list;
    }
}
