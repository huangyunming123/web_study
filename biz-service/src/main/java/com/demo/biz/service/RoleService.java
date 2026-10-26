package com.demo.biz.service;

import com.demo.biz.entity.SysRole;
import com.demo.biz.vo.PermissionTreeVO;

import java.util.List;

/**
 * CRM 角色与权限管理服务接口
 */
public interface RoleService {

    /**
     * 获取系统全部角色列表
     */
    List<SysRole> getAllRoles();

    /**
     * 获取完整的系统权限字典树
     */
    List<PermissionTreeVO> getPermissionTree();

    /**
     * 获取指定角色已关联的权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 为角色分配权限点
     */
    void assignRolePermissions(Long roleId, List<Long> permissionIds);

    /**
     * 为用户分配角色
     */
    void assignUserRoles(Long userId, List<Long> roleIds);
}
