package com.demo.biz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CRM 物流运单主表实体
 * 对应表: crm_logistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmLogistics implements Serializable {

    private Long id;

    /**
     * 关联订单编号
     */
    private String orderNo;

    /**
     * 承运快递公司 (如: 顺丰速运/京东快递)
     */
    private String companyName;

    /**
     * 承运商简码 (SF/JD/ZTO/YTO)
     */
    private String companyCode;

    /**
     * 快递运单号
     */
    private String trackingNo;

    /**
     * 物流状态 (1:待揽件, 2:运输中, 3:派送中, 4:已签收, 5:异常滞留)
     */
    private Integer status;

    /**
     * 发货经办人员
     */
    private String operatorName;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 交付签收时间
     */
    private LocalDateTime deliveredTime;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;
}
