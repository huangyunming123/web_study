package com.demo.auth.common;

import lombok.Getter;

/**
 * 业务自定义异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;
    private final String msg;

    public BusinessException(String msg) {
        super(msg);
        this.code = 500;
        this.msg = msg;
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
