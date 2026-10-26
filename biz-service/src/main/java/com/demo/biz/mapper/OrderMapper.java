package com.demo.biz.mapper;

import com.demo.biz.entity.CrmOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CRM 订单数据访问层
 */
@Mapper
public interface OrderMapper {

    /**
     * 分页多条件查询订单
     */
    List<CrmOrder> selectPageList(@Param("offset") long offset,
                                  @Param("size") long size,
                                  @Param("orderNo") String orderNo,
                                  @Param("customerName") String customerName,
                                  @Param("customerPhone") String customerPhone,
                                  @Param("status") Integer status,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime);

    /**
     * 统计符合条件的订单总数
     */
    Long selectPageCount(@Param("orderNo") String orderNo,
                         @Param("customerName") String customerName,
                         @Param("customerPhone") String customerPhone,
                         @Param("status") Integer status,
                         @Param("startTime") String startTime,
                         @Param("endTime") String endTime);

    /**
     * 分页查询指定用户的订单
     */
    List<CrmOrder> selectMyOrderList(@Param("offset") long offset,
                                     @Param("size") long size,
                                     @Param("userId") Long userId,
                                     @Param("status") Integer status);

    /**
     * 统计指定用户的订单数
     */
    Long selectMyOrderCount(@Param("userId") Long userId,
                            @Param("status") Integer status);

    /**
     * 根据主键 ID 查询订单
     */
    CrmOrder selectById(@Param("id") Long id);

    /**
     * 根据订单流水号查询订单
     */
    CrmOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新订单状态与发货时间
     */
    int updateShipStatus(@Param("orderNo") String orderNo,
                         @Param("status") Integer status,
                         @Param("shipTime") LocalDateTime shipTime);

    /**
     * 更新订单状态为已完成 (确认收货)
     */
    int updateConfirmStatus(@Param("orderNo") String orderNo,
                            @Param("status") Integer status,
                            @Param("finishTime") LocalDateTime finishTime);

    /**
     * 插入新订单
     */
    int insert(CrmOrder order);
}
