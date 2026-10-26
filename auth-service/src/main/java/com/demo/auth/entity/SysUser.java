package com.demo.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体 (原生 MyBatis POJO，无三方侵入注解)
 * 对应表: sys_user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUser implements Serializable {

    /**
     * 主键ID (自增)
     */
    private Long id;

    /**
     * 登录用户名 (唯一)
     */
    private String username;

    /**
     * BCrypt 加密密码
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态 (1:启用, 0:禁用)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
