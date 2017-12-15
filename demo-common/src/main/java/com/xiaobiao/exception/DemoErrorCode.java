package com.xiaobiao.exception;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * 异常枚举
 */
public enum DemoErrorCode {

    ERROR_CODE_341FFF("341FFF", "程序异常"),
    ERROR_CODE_341001("341001", "亲，您查询的信息不存在哦"),
    ERROR_CODE_341002("341002", "请输入正确的用户名!"),
    ERROR_CODE_341003("341003", "请输入正确的密码!"),
    ERROR_CODE_341004("341004", "*认证失败!"),
    ERROR_CODE_341005("341005", "*登录失败!");

    @Getter
    private String errorCode;
    @Getter
    private String errorDesc;


    DemoErrorCode(String errorCode, String errorDesc) {
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    public static String explain(String errorCode) {
        for (DemoErrorCode demoErrorCode : DemoErrorCode.values()) {
            if (Objects.equal(errorCode, demoErrorCode.errorCode)) {
                return demoErrorCode.errorDesc;
            }
        }
        return errorCode;
    }
}
