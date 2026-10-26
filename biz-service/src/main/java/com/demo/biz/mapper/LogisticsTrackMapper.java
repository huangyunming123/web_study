package com.demo.biz.mapper;

import com.demo.biz.entity.CrmLogisticsTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * CRM 物流轨迹数据访问层
 */
@Mapper
public interface LogisticsTrackMapper {

    /**
     * 根据订单号查询轨迹列表 (按时间正序)
     */
    List<CrmLogisticsTrack> selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据运单号查询轨迹列表 (按时间正序)
     */
    List<CrmLogisticsTrack> selectByTrackingNo(@Param("trackingNo") String trackingNo);

    /**
     * 插入轨迹记录
     */
    int insert(CrmLogisticsTrack track);
}
