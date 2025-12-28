package com.xuegongbu.service;

import com.xuegongbu.domain.Alert;
import com.xuegongbu.domain.Teacher;

public interface MailService {
    /**
     * 发送预警邮件通知
     * @param alert 预警对象
     */
    void sendAlertNotification(Alert alert, Teacher  teacher);
}