package com.demo.biz.mapper;

import com.demo.biz.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限数据访问层
 */
@Mapper
public interface SysPermissionMapper {

    /**
     * 查询系统全部权限定义 (按排序字段升序)
     */
    List<SysPermission> selectAll();

    /**
     * 根据用户ID查询其具备的权限代码集合
     */
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);
}
