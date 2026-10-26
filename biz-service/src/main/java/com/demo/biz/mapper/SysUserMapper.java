package com.demo.biz.mapper;

import com.demo.biz.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问层 (原生 MyBatis Mapper)
 */
@Mapper
public interface SysUserMapper {

    /**
     * 分页查询用户列表 (支持关键字匹配用户名、昵称或手机号)
     */
    List<SysUser> selectPageList(@Param("offset") long offset, @Param("size") long size, @Param("keyword") String keyword);

    /**
     * 统计用户总数
     */
    Long selectPageCount(@Param("keyword") String keyword);

    /**
     * 根据主键查询用户
     */
    SysUser selectById(@Param("id") Long id);

    /**
     * 更新用户昵称/名称
     */
    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    /**
     * 更新用户状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 物理删除用户
     */
    int deleteById(@Param("id") Long id);
}
