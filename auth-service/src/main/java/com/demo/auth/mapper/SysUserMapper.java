package com.demo.auth.mapper;

import com.demo.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表 MyBatis Mapper 接口
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据用户名查询用户
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据用户名统计数量 (用于重名校验)
     */
    Long countByUsername(@Param("username") String username);

    /**
     * 插入新用户并回填自增 ID
     */
    int insert(SysUser user);

    /**
     * 根据主键 ID 查询用户
     */
    SysUser selectById(@Param("id") Long id);
}
