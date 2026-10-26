package com.demo.biz.vo;

import com.demo.biz.entity.CrmLogistics;
import com.demo.biz.entity.CrmLogisticsTrack;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 物流轨迹详情响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "物流运单与流转轨迹详情")
public class LogisticsTrackVO implements Serializable {

    @Schema(description = "物流主信息")
    private CrmLogistics logistics;

    @Schema(description = "物流轨迹流转节点列表")
    private List<CrmLogisticsTrack> tracks;
}
