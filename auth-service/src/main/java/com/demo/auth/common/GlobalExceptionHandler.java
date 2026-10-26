package com.demo.auth.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局统一异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常触发: code={}, msg={}", e.getCode(), e.getMsg());
        return R.fail(e.getCode(), e.getMsg());
    }

    /**
     * 捕获系统未知兜底异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统未知异常: ", e);
        return R.fail(500, "系统内部繁忙，请稍后重试: " + e.getMessage());
    }
}
