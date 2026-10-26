package com.demo.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模拟下单请求 DTO
 */
@Data
@Schema(description = "模拟下单请求参数")
public class OrderCreateDTO {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer count;
}
