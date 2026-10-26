package com.demo.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 角色权限分配请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色权限配置参数")
public class RolePermAssignDTO implements Serializable {

    @Schema(description = "权限ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> permissionIds;
}
