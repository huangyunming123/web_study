package com.demo.biz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CRM 商品实体 (原生 MyBatis POJO)
 * 对应表: product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {

    /**
     * 商品ID (自增主键)
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品类目/分类
     */
    private String category;

    /**
     * 销售价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 审核/在售状态 (0:待审核, 1:在售, 2:已驳回, 3:已下架)
     */
    private Integer status;

    /**
     * 审核驳回原因
     */
    private String rejectReason;

    /**
     * 商品详细规格与图文说明
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
