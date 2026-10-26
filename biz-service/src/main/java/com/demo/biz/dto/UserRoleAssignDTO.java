package com.demo.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户分配角色请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户角色分配参数")
public class UserRoleAssignDTO implements Serializable {

    @Schema(description = "角色ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> roleIds;
}
