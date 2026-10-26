package com.demo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录请求 DTO
 */
@Data
@Schema(description = "用户登录请求参数")
public class LoginDTO {

    @Schema(description = "登录用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String username;

    @Schema(description = "明文密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;
}
