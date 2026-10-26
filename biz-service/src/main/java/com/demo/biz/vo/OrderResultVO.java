package com.demo.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟下单成功响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模拟下单响应数据")
public class OrderResultVO implements Serializable {

    @Schema(description = "生成模拟订单号", example = "ORD20261023214500123")
    private String orderNo;

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "iPhone 15 Pro Max")
    private String productName;

    @Schema(description = "购买数量", example = "1")
    private Integer count;

    @Schema(description = "商品单价", example = "9999.00")
    private BigDecimal price;

    @Schema(description = "订单总金额", example = "9999.00")
    private BigDecimal totalAmount;

    @Schema(description = "扣减后最新剩余库存", example = "99")
    private Integer surplusStock;

    @Schema(description = "下单成功时间")
    private LocalDateTime createTime;
}
