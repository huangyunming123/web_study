package com.demo.biz.mapper;

import com.demo.biz.entity.CrmOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * CRM 订单明细项数据访问层
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 根据订单ID查询明细项
     */
    List<CrmOrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单流水号查询明细项
     */
    List<CrmOrderItem> selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 批量插入订单明细
     */
    int batchInsert(@Param("items") List<CrmOrderItem> items);
}
