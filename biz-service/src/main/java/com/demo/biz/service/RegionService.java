package com.demo.biz.service;

import com.demo.biz.entity.Region;
import com.demo.biz.vo.RegionTreeVO;

import java.util.List;

/**
 * 地区字典服务接口
 */
public interface RegionService {

    /**
     * 获取全量地区树 (带 10 分钟 Redis 缓存)
     */
    List<RegionTreeVO> tree();

    /**
     * 新增地区
     */
    void add(Region region);

    /**
     * 修改地区
     */
    void update(Long id, Region region);

    /**
     * 删除地区 (若存在下级子节点则拒绝删除)
     */
    void delete(Long id);
}
