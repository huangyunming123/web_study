package com.demo.biz.mapper;

import com.demo.biz.entity.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 行政地区数据访问层 (原生 MyBatis Mapper)
 */
@Mapper
public interface RegionMapper {

    /**
     * 查询全部地区列表
     */
    List<Region> selectAll();

    /**
     * 根据主键查询地区
     */
    Region selectById(@Param("id") Long id);

    /**
     * 根据父级ID统计子节点数量
     */
    Long countByParentId(@Param("parentId") Long parentId);

    /**
     * 新增地区
     */
    int insert(Region region);

    /**
     * 更新地区
     */
    int update(Region region);

    /**
     * 物理删除地区
     */
    int deleteById(@Param("id") Long id);
}
