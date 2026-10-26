package com.demo.biz.service;

import com.demo.biz.common.PageResult;
import com.demo.biz.entity.SysUser;

import java.util.List;

/**
 * CRM 用户服务接口
 */
public interface UserService {

    /**
     * 分页查询系统用户 (原生 MyBatis 并聚合关联角色列表)
     */
    PageResult<SysUser> page(long current, long size, String keyword);

    /**
     * 编辑修改用户昵称/名称
     */
    void updateNickname(Long id, String nickname);

    /**
     * 修改用户状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 为用户分配角色
     */
    void assignRoles(Long id, List<Long> roleIds);

    /**
     * 物理删除用户
     */
    void delete(Long id);
}
