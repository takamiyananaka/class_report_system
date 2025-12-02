package com.xuegongbu.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeacherValidationUtilsTest {

    @Test
    void validateIdentity_shouldPassForTeacher() {
        assertNull(TeacherValidationUtils.validateIdentity(1));
    }

    @Test
    void validateIdentity_shouldPassForCounselor() {
        assertNull(TeacherValidationUtils.validateIdentity(2));
    }

    @Test
    void validateIdentity_shouldPassForNull() {
        assertNull(TeacherValidationUtils.validateIdentity(null));
    }

    @Test
    void validateIdentity_shouldFailForInvalidValue() {
        assertNotNull(TeacherValidationUtils.validateIdentity(3));
        assertNotNull(TeacherValidationUtils.validateIdentity(0));
    }

    @Test
    void validateDepartmentForCounselor_shouldPassForTeacherIdentity() {
        // 教师身份不需要验证部门格式
        assertNull(TeacherValidationUtils.validateDepartmentForCounselor(1, "计算机学院"));
        assertNull(TeacherValidationUtils.validateDepartmentForCounselor(1, null));
    }

    @Test
    void validateDepartmentForCounselor_shouldFailWhenDepartmentIsEmptyForCounselor() {
        assertNotNull(TeacherValidationUtils.validateDepartmentForCounselor(2, null));
        assertNotNull(TeacherValidationUtils.validateDepartmentForCounselor(2, ""));
        assertNotNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "  "));
    }

    @Test
    void validateDepartmentForCounselor_shouldPassForValidFormat() {
        // 单个专业+年级
        assertNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "软件工程25"));
        assertNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "计算机科学24"));
        assertNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "数据科学23"));
    }

    @Test
    void validateDepartmentForCounselor_shouldPassForMultipleValues() {
        // 多个专业+年级，用分号分隔
        assertNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "软件工程25;计算机科学24"));
        assertNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "软件工程25;计算机科学24;数据科学25"));
    }

    @Test
    void validateDepartmentForCounselor_shouldFailForInvalidFormat() {
        // 缺少年级
        assertNotNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "软件工程"));
        // 年级格式不对
        assertNotNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "软件工程2025"));
        // 缺少专业名
        assertNotNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "25"));
        // 中间有空格
        assertNotNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "软件工程 25"));
    }

    @Test
    void validateDepartmentForCounselor_shouldFailForPartiallyInvalidMultipleValues() {
        // 其中一个格式不对
        assertNotNull(TeacherValidationUtils.validateDepartmentForCounselor(2, "软件工程25;计算机学院"));
    }
}
