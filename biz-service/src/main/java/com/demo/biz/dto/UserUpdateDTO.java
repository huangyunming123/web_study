package com.demo.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户更新请求 DTO
 */
@Data
@Schema(description = "编辑用户请求参数")
public class UserUpdateDTO {

    @Schema(description = "用户昵称/展示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "技术总监")
    private String nickname;
}
