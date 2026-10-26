package com.demo.biz.mapper;

import com.demo.biz.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据访问层
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 查询系统全部角色
     */
    List<SysRole> selectAll();

    /**
     * 根据角色ID查询
     */
    SysRole selectById(@Param("id") Long id);

    /**
     * 查询指定用户拥有的角色列表
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 物理清空指定用户的角色关联
     */
    int deleteUserRolesByUserId(@Param("userId") Long userId);

    /**
     * 批量为用户插入角色关联
     */
    int batchInsertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 查询指定角色拥有的权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 物理清空角色的权限关联
     */
    int deleteRolePermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量为角色插入权限关联
     */
    int batchInsertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
}
