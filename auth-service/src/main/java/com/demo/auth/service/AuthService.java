package com.demo.auth.service;

import com.demo.auth.dto.LoginDTO;
import com.demo.auth.dto.RegisterDTO;
import com.demo.auth.vo.LoginVO;
import com.demo.auth.vo.VerifyVO;

/**
 * 鉴权服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param dto 注册参数
     * @return 新增用户ID
     */
    Long register(RegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return 登录结果(含 Token 及用户信息)
     */
    LoginVO login(LoginDTO dto);

    /**
     * 校验 Token
     *
     * @param authHeader 请求头 Authorization 字段
     * @return 校验详情
     */
    VerifyVO verify(String authHeader);
}
