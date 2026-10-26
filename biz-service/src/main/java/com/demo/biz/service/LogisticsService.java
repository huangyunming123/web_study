package com.demo.biz.service;

import com.demo.biz.dto.ShipOrderDTO;
import com.demo.biz.vo.LogisticsTrackVO;

import java.util.List;
import java.util.Map;

/**
 * CRM 物流服务接口
 */
public interface LogisticsService {

    /**
     * 订单出库物流发货
     */
    void shipOrder(ShipOrderDTO dto);

    /**
     * 根据订单号查询物流运单及完整轨迹
     */
    LogisticsTrackVO getTrackByOrderNo(String orderNo);

    /**
     * 根据快递运单号查询物流运单及完整轨迹
     */
    LogisticsTrackVO getTrackByTrackingNo(String trackingNo);

    /**
     * 获取支持的主流物流承运商列表
     */
    List<Map<String, String>> getSupportedCompanies();
}
