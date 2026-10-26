package com.demo.biz.vo;

import com.demo.biz.entity.CrmLogistics;
import com.demo.biz.entity.CrmOrder;
import com.demo.biz.entity.CrmOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 订单详情响应 VO (包含订单主信息、明细列表及物流信息)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单详情数据")
public class OrderDetailVO implements Serializable {

    @Schema(description = "订单基础信息")
    private CrmOrder order;

    @Schema(description = "订单购买商品明细")
    private List<CrmOrderItem> items;

    @Schema(description = "订单对应物流信息 (已发货后存在)")
    private CrmLogistics logistics;
}
