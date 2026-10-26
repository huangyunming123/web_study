package com.demo.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限字典树 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统权限树节点")
public class PermissionTreeVO implements Serializable {

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "父级权限ID")
    private Long parentId;

    @Schema(description = "权限/功能名称")
    private String name;

    @Schema(description = "权限唯一标识代码")
    private String permCode;

    @Schema(description = "类型(1:菜单, 2:按钮)")
    private Integer type;

    @Schema(description = "子权限列表")
    @Builder.Default
    private List<PermissionTreeVO> children = new ArrayList<>();
}
