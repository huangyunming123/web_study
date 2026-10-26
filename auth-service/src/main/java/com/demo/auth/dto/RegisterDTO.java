package com.demo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户注册请求 DTO
 */
@Data
@Schema(description = "用户注册请求参数")
public class RegisterDTO {

    @Schema(description = "登录用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "zhangsan")
    private String username;

    @Schema(description = "明文密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;
}
