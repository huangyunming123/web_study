package com.demo.biz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CRM 物流轨迹节点实体
 * 对应表: crm_logistics_track
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmLogisticsTrack implements Serializable {

    private Long id;

    /**
     * 关联订单编号
     */
    private String orderNo;

    /**
     * 快递运单号
     */
    private String trackingNo;

    /**
     * 轨迹流转事件描述
     */
    private String content;

    /**
     * 流转网点/地理位置
     */
    private String location;

    /**
     * 经办网点/配送员
     */
    private String operator;

    /**
     * 轨迹事件时间
     */
    private LocalDateTime trackTime;
}
