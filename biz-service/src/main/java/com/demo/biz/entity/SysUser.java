package com.demo.biz.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户实体
 * 对应表: sys_user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUser implements Serializable {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * BCrypt 密码 (向前端传输时隐藏敏感信息)
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 昵称 / 姓名展示
     */
    private String nickname;

    /**
     * 联系手机号
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

    /**
     * 所属角色列表
     */
    private List<SysRole> roles;
}
