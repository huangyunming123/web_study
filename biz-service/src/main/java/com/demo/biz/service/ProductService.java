package com.demo.biz.service;

import com.demo.biz.common.PageResult;
import com.demo.biz.dto.OrderCreateDTO;
import com.demo.biz.entity.Product;
import com.demo.biz.vo.OrderResultVO;

/**
 * CRM 商品业务接口
 */
public interface ProductService {

    /**
     * 多条件分页查询商品
     */
    PageResult<Product> page(long current, long size, String keyword, String category, Integer status);

    /**
     * 根据主键查询商品详情 (优先命中 Redis 缓存)
     */
    Product getById(Long id);

    /**
     * 新增/发布商品 (默认进入待审核状态)
     */
    void add(Product product);

    /**
     * 更新商品信息 (同步淘汰 Redis 缓存)
     */
    void update(Long id, Product product);

    /**
     * 切换商品上下架状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 审核商品 (1:通过/上架, 2:驳回)
     */
    void audit(Long id, Integer status, String rejectReason);

    /**
     * 模拟秒杀下单 (Redisson 分布式锁扣减库存)
     */
    OrderResultVO createOrder(OrderCreateDTO dto);

    /**
     * 物理删除商品 (同步淘汰 Redis 缓存)
     */
    void delete(Long id);
}
