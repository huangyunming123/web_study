package com.demo.biz.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 业务服务全局统一异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务服务异常: code={}, msg={}", e.getCode(), e.getMsg());
        return R.fail(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("业务服务未知异常: ", e);
        return R.fail(500, "系统繁忙，请稍后重试: " + e.getMessage());
    }
}
