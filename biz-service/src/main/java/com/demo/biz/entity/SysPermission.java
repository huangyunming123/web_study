package com.demo.biz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * CRM 系统权限实体
 * 对应表: sys_permission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysPermission implements Serializable {

    private Long id;

    /**
     * 父级权限ID (0为根)
     */
    private Long parentId;

    /**
     * 权限/功能名称
     */
    private String name;

    /**
     * 权限标识编码 (如: crm:order:list, crm:product:publish)
     */
    private String permCode;

    /**
     * 类型 (1:菜单, 2:按钮/操作)
     */
    private Integer type;

    /**
     * 排序权重
     */
    private Integer sort;
}
