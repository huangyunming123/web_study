package com.demo.biz.service;

import com.demo.biz.common.PageResult;
import com.demo.biz.dto.MallOrderCreateDTO;
import com.demo.biz.entity.CrmOrder;
import com.demo.biz.vo.OrderDetailVO;

/**
 * CRM 订单服务接口
 */
public interface OrderService {

    /**
     * 多条件分页查询订单 (后台管理)
     */
    PageResult<CrmOrder> page(long current, long size, String orderNo, String customerName,
                              String customerPhone, Integer status, String startTime, String endTime);

    /**
     * 商城前台分页查询当前登录用户的订单
     */
    PageResult<OrderDetailVO> myOrders(long current, long size, Long userId, Integer status);

    /**
     * 根据订单编号获取完整详情 (包含商品明细快照与物流运单信息)
     */
    OrderDetailVO getOrderDetail(String orderNo);

    /**
     * 商城前台模拟下单 (扣减库存 -> 直接流转为待发货状态)
     */
    String createMallOrder(MallOrderCreateDTO dto, Long userId, String username);

    /**
     * 商城买家确认收货 (流转为已完成，同步更新物流为已签收)
     */
    void confirmReceipt(String orderNo, Long userId);
}
