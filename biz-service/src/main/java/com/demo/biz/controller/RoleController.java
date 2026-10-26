package com.demo.biz.controller;

import com.demo.biz.common.R;
import com.demo.biz.dto.RolePermAssignDTO;
import com.demo.biz.entity.SysRole;
import com.demo.biz.service.RoleService;
import com.demo.biz.vo.PermissionTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRM 角色与权限管理控制器
 */
@Tag(name = "CRM权限分配", description = "角色列表、权限字典树及角色权限矩阵配置")
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取系统全部角色列表")
    @GetMapping("/list")
    public R<List<SysRole>> list() {
        List<SysRole> roles = roleService.getAllRoles();
        return R.ok(roles);
    }

    @Operation(summary = "获取全量权限树 (菜单及按钮操作字典)")
    @GetMapping("/permission/tree")
    public R<List<PermissionTreeVO>> getPermissionTree() {
        List<PermissionTreeVO> tree = roleService.getPermissionTree();
        return R.ok(tree);
    }

    @Operation(summary = "获取指定角色已配置的权限ID列表")
    @GetMapping("/{roleId}/permissions")
    public R<List<Long>> getRolePermissions(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        List<Long> permIds = roleService.getRolePermissionIds(roleId);
        return R.ok(permIds);
    }

    @Operation(summary = "为指定角色配置权限列表")
    @PutMapping("/{roleId}/permissions")
    public R<Void> assignPermissions(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @RequestBody RolePermAssignDTO dto) {
        roleService.assignRolePermissions(roleId, dto.getPermissionIds());
        return R.ok();
    }
}
