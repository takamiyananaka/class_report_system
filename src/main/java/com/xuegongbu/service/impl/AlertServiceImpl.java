package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.domain.Alert;
import com.xuegongbu.mapper.AlertMapper;
import com.xuegongbu.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {
}