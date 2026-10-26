package com.demo.biz.controller;

import com.demo.biz.common.PageResult;
import com.demo.biz.common.R;
import com.demo.biz.dto.UserRoleAssignDTO;
import com.demo.biz.dto.UserUpdateDTO;
import com.demo.biz.entity.SysUser;
import com.demo.biz.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRM 用户管理控制器
 */
@Tag(name = "CRM用户管理", description = "用户综合查询、角色标签分配、编辑用户昵称与状态管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询系统用户 (带角色标签)")
    @GetMapping("/page")
    public R<PageResult<SysUser>> page(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键字(用户名/昵称/手机号)") @RequestParam(required = false) String keyword) {
        PageResult<SysUser> result = userService.page(page, size, keyword);
        return R.ok(result);
    }

    @Operation(summary = "修改用户名称/昵称")
    @PutMapping("/{id}")
    public R<Void> update(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @RequestBody UserUpdateDTO dto) {
        userService.updateNickname(id, dto.getNickname());
        return R.ok();
    }

    @Operation(summary = "修改用户状态 (启用/禁用)")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态 (1:启用, 0:禁用)", required = true) @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "为用户分配角色")
    @PutMapping("/{id}/roles")
    public R<Void> assignRoles(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @RequestBody UserRoleAssignDTO dto) {
        userService.assignRoles(id, dto.getRoleIds());
        return R.ok();
    }

    @Operation(summary = "物理删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }
}
