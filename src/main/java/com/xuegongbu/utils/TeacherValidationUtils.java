package com.xuegongbu.utils;

import java.util.regex.Pattern;

/**
 * 教师相关验证工具类
 */
public class TeacherValidationUtils {

    /**
     * 辅导员身份标识
     */
    public static final Integer IDENTITY_TEACHER = 1;
    public static final Integer IDENTITY_COUNSELOR = 2;

    /**
     * 辅导员部门格式正则表达式
     * 格式要求：专业名+年级，例如：软件工程25
     * 支持中文专业名和两位数年级
     */
    private static final Pattern DEPARTMENT_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z]+\\d{2}$");

    /**
     * 验证辅导员身份时的部门格式
     * 
     * @param identity 身份标识
     * @param department 部门字段
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    public static String validateDepartmentForCounselor(Integer identity, String department) {
        // 如果不是辅导员身份，不需要验证部门格式
        if (identity == null || !identity.equals(IDENTITY_COUNSELOR)) {
            return null;
        }

        // 辅导员身份时，部门字段不能为空
        if (department == null || department.trim().isEmpty()) {
            return "辅导员身份时，部门字段不能为空";
        }

        // 分割多个部门值
        String[] departments = department.split(";");
        for (String dept : departments) {
            String trimmedDept = dept.trim();
            if (trimmedDept.isEmpty()) {
                continue;
            }
            if (!DEPARTMENT_PATTERN.matcher(trimmedDept).matches()) {
                return "辅导员部门格式错误，正确格式为：专业名+年级（如：软件工程25），多个值用分号分隔";
            }
        }

        return null;
    }

    /**
     * 验证身份字段值是否有效
     * 
     * @param identity 身份标识
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    public static String validateIdentity(Integer identity) {
        if (identity != null && !identity.equals(IDENTITY_TEACHER) && !identity.equals(IDENTITY_COUNSELOR)) {
            return "身份字段值无效，只能为1（教师）或2（辅导员）";
        }
        return null;
    }
}
