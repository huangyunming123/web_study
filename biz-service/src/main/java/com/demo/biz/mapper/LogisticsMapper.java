package com.demo.biz.mapper;

import com.demo.biz.entity.CrmLogistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * CRM 物流数据访问层
 */
@Mapper
public interface LogisticsMapper {

    /**
     * 根据订单号查询物流运单
     */
    CrmLogistics selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据运单号查询物流运单
     */
    CrmLogistics selectByTrackingNo(@Param("trackingNo") String trackingNo);

    /**
     * 插入物流运单
     */
    int insert(CrmLogistics logistics);

    /**
     * 更新物流状态与签收时间
     */
    int updateStatus(@Param("trackingNo") String trackingNo,
                     @Param("status") Integer status,
                     @Param("deliveredTime") LocalDateTime deliveredTime);
}
