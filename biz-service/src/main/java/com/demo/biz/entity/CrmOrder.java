package com.demo.biz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CRM 订单主表实体
 * 对应表: crm_order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmOrder implements Serializable {

    private Long id;

    /**
     * 订单流水编号
     */
    private String orderNo;

    /**
     * 下单客户用户ID
     */
    private Long userId;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 客户联系电话
     */
    private String customerPhone;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态 (1:待支付, 2:待发货, 3:已发货, 4:已完成, 5:已取消)
     */
    private Integer status;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人电话
     */
    private String receiverPhone;

    /**
     * 收货详细地址
     */
    private String receiverAddress;

    /**
     * 订单客户备注
     */
    private String remark;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
