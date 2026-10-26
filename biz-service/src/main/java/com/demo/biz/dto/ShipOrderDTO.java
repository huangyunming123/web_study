package com.demo.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 物流发货请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单发货提交参数")
public class ShipOrderDTO implements Serializable {

    @Schema(description = "订单流水编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD202610250001A01")
    private String orderNo;

    @Schema(description = "物流快递承运商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "顺丰速运")
    private String companyName;

    @Schema(description = "物流承运商简码", example = "SF")
    private String companyCode;

    @Schema(description = "快递运单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SF168892026102501")
    private String trackingNo;

    @Schema(description = "发货经办人员姓名", example = "张仓管")
    private String operatorName;
}
