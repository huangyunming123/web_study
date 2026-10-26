package com.demo.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 地区树形节点视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "地区树形节点")
public class RegionTreeVO implements Serializable {

    @Schema(description = "地区ID")
    private Long id;

    @Schema(description = "父级地区ID")
    private Long parentId;

    @Schema(description = "地区名称")
    private String name;

    @Schema(description = "地区层级 (1:省/直辖市, 2:市, 3:区/县)")
    private Integer level;

    @Builder.Default
    @Schema(description = "下级子地区列表")
    private List<RegionTreeVO> children = new ArrayList<>();
}
