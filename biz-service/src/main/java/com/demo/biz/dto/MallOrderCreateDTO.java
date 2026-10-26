package com.demo.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商城前台模拟下单请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商城下单参数")
public class MallOrderCreateDTO implements Serializable {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer count;

    @Schema(description = "收货人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张先生")
    private String receiverName;

    @Schema(description = "收货人联系手机", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String receiverPhone;

    @Schema(description = "收货详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "广东省广州市天河区天河路208号")
    private String receiverAddress;

    @Schema(description = "买家备注留言", example = "发顺丰特快，感谢！")
    private String remark;
}
