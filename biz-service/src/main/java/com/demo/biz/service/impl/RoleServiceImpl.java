package com.demo.biz.service.impl;

import com.demo.biz.common.BusinessException;
import com.demo.biz.entity.SysPermission;
import com.demo.biz.entity.SysRole;
import com.demo.biz.mapper.SysPermissionMapper;
import com.demo.biz.mapper.SysRoleMapper;
import com.demo.biz.service.RoleService;
import com.demo.biz.vo.PermissionTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CRM 角色与权限管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public List<SysRole> getAllRoles() {
        return sysRoleMapper.selectAll();
    }

    @Override
    public List<PermissionTreeVO> getPermissionTree() {
        List<SysPermission> all = sysPermissionMapper.selectAll();
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, PermissionTreeVO> nodeMap = new LinkedHashMap<>();
        for (SysPermission p : all) {
            nodeMap.put(p.getId(), PermissionTreeVO.builder()
                    .id(p.getId())
                    .parentId(p.getParentId())
                    .name(p.getName())
                    .permCode(p.getPermCode())
                    .type(p.getType())
                    .children(new ArrayList<>())
                    .build());
        }

        List<PermissionTreeVO> roots = new ArrayList<>();
        for (PermissionTreeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() == 0L) {
                roots.add(node);
            } else {
                PermissionTreeVO parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        return sysRoleMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        if (roleId == null) {
            throw new BusinessException(500, "角色ID不能为空");
        }
        // 1. 先清空当前角色的所有旧绑定
        sysRoleMapper.deleteRolePermissionsByRoleId(roleId);

        // 2. 插入新绑定
        if (permissionIds != null && !permissionIds.isEmpty()) {
            sysRoleMapper.batchInsertRolePermissions(roleId, permissionIds);
        }
        log.info("CRM 角色权限分配成功: roleId={}, permCount={}", roleId, permissionIds != null ? permissionIds.size() : 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            throw new BusinessException(500, "用户ID不能为空");
        }
        // 1. 先清空当前用户的旧角色绑定
        sysRoleMapper.deleteUserRolesByUserId(userId);

        // 2. 插入新角色绑定
        if (roleIds != null && !roleIds.isEmpty()) {
            sysRoleMapper.batchInsertUserRoles(userId, roleIds);
        }
        log.info("CRM 用户角色分配成功: userId={}, roleCount={}", userId, roleIds != null ? roleIds.size() : 0);
    }
}
