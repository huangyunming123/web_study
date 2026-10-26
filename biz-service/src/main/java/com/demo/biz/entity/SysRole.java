package com.demo.biz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CRM 系统角色实体
 * 对应表: sys_role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysRole implements Serializable {

    private Long id;

    /**
     * 角色名称 (如: 超级管理员, 运营专员, 仓储物流专员)
     */
    private String roleName;

    /**
     * 角色标识编码 (如: ROLE_ADMIN, ROLE_OPERATOR, ROLE_LOGISTICS)
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
