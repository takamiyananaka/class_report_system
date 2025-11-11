package com.xuegongbu.common;

public class Constants {
    public static final String REDIS_USER_KEY_PREFIX = "user:";
    public static final String REDIS_TOKEN_KEY_PREFIX = "token:";
    public static final Long REDIS_TOKEN_EXPIRATION = 86400L; // 24小时，单位：秒

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";

    public static final Integer STATUS_ENABLED = 1;
    public static final Integer STATUS_DISABLED = 0;
}
