package com.demo.biz.service.impl;

import com.demo.biz.common.BusinessException;
import com.demo.biz.common.PageResult;
import com.demo.biz.entity.SysRole;
import com.demo.biz.entity.SysUser;
import com.demo.biz.mapper.SysRoleMapper;
import com.demo.biz.mapper.SysUserMapper;
import com.demo.biz.service.RoleService;
import com.demo.biz.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * CRM 用户服务实现类 (基于原生 MyBatis 框架)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final RoleService roleService;

    @Override
    public PageResult<SysUser> page(long current, long size, String keyword) {
        long offset = (Math.max(current, 1) - 1) * size;
        String trimKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        List<SysUser> records = sysUserMapper.selectPageList(offset, size, trimKeyword);
        Long total = sysUserMapper.selectPageCount(trimKeyword);

        // 批量装配用户绑定的角色列表
        if (records != null && !records.isEmpty()) {
            for (SysUser user : records) {
                List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getId());
                user.setRoles(roles);
            }
        }

        return PageResult.of(records, total, current, size);
    }

    @Override
    public void updateNickname(Long id, String nickname) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(500, "目标用户不存在");
        }
        if (!StringUtils.hasText(nickname)) {
            throw new BusinessException(500, "用户昵称/名称不能为空");
        }

        sysUserMapper.updateNickname(id, nickname.trim());
        log.info("修改用户名称成功: id={}, newNickname={}", id, nickname.trim());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(500, "目标用户不存在");
        }
        sysUserMapper.updateStatus(id, status);
        log.info("更新用户状态成功: id={}, status={}", id, status);
    }

    @Override
    public void assignRoles(Long id, List<Long> roleIds) {
        roleService.assignUserRoles(id, roleIds);
    }

    @Override
    public void delete(Long id) {
        int rows = sysUserMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(500, "待删除用户不存在");
        }
        // 同时清理关联的角色
        sysRoleMapper.deleteUserRolesByUserId(id);
        log.info("物理删除用户成功: id={}", id);
    }
}
