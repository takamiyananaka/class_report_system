package com.xuegongbu.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "success"),
    ERROR(500, "error"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    USERNAME_OR_PASSWORD_ERROR(4001, "用户名或密码错误"),
    USER_DISABLED(4002, "用户已被禁用"),
    TOKEN_INVALID(4003, "Token无效"),
    TOKEN_EXPIRED(4004, "Token已过期");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
