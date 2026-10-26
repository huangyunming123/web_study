package com.demo.biz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * CRM 订单明细项实体
 * 对应表: crm_order_item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmOrderItem implements Serializable {

    private Long id;

    /**
     * 关联订单主键ID
     */
    private Long orderId;

    /**
     * 关联订单流水编号
     */
    private String orderNo;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称快照
     */
    private String productName;

    /**
     * 销售单价快照
     */
    private BigDecimal productPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 小计金额
     */
    private BigDecimal subtotal;
}
