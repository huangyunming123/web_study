package com.demo.biz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 行政地区实体 (原生 MyBatis POJO)
 * 对应表: region (无时间字段，物理删除)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region implements Serializable {

    /**
     * 地区ID (自增主键)
     */
    private Long id;

    /**
     * 父级地区ID (0 为顶级)
     */
    private Long parentId;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 层级 (1:省/直辖市, 2:市, 3:区/县)
     */
    private Integer level;

    /**
     * 子地区列表
     */
    @Builder.Default
    private List<Region> children = new ArrayList<>();
}
