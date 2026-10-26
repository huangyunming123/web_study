package com.demo.biz.service.impl;

import com.demo.biz.common.BusinessException;
import com.demo.biz.common.PageResult;
import com.demo.biz.dto.MallOrderCreateDTO;
import com.demo.biz.entity.CrmLogistics;
import com.demo.biz.entity.CrmLogisticsTrack;
import com.demo.biz.entity.CrmOrder;
import com.demo.biz.entity.CrmOrderItem;
import com.demo.biz.entity.Product;
import com.demo.biz.mapper.LogisticsMapper;
import com.demo.biz.mapper.LogisticsTrackMapper;
import com.demo.biz.mapper.OrderItemMapper;
import com.demo.biz.mapper.OrderMapper;
import com.demo.biz.mapper.ProductMapper;
import com.demo.biz.service.OrderService;
import com.demo.biz.vo.OrderDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * CRM 订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final LogisticsMapper logisticsMapper;
    private final LogisticsTrackMapper logisticsTrackMapper;
    private final ProductMapper productMapper;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String PRODUCT_ORDER_LOCK_PREFIX = "lock:order:product:";
    private static final String PRODUCT_DETAIL_CACHE_PREFIX = "product:detail:";

    @Override
    public PageResult<CrmOrder> page(long current, long size, String orderNo, String customerName,
                                     String customerPhone, Integer status, String startTime, String endTime) {
        long offset = (Math.max(current, 1) - 1) * size;
        String trimOrderNo = StringUtils.hasText(orderNo) ? orderNo.trim() : null;
        String trimName = StringUtils.hasText(customerName) ? customerName.trim() : null;
        String trimPhone = StringUtils.hasText(customerPhone) ? customerPhone.trim() : null;
        String trimStart = StringUtils.hasText(startTime) ? startTime.trim() : null;
        String trimEnd = StringUtils.hasText(endTime) ? endTime.trim() : null;

        List<CrmOrder> records = orderMapper.selectPageList(offset, size, trimOrderNo, trimName, trimPhone, status, trimStart, trimEnd);
        Long total = orderMapper.selectPageCount(trimOrderNo, trimName, trimPhone, status, trimStart, trimEnd);

        return PageResult.of(records, total, current, size);
    }

    @Override
    public PageResult<OrderDetailVO> myOrders(long current, long size, Long userId, Integer status) {
        if (userId == null) {
            return PageResult.of(Collections.emptyList(), 0L, current, size);
        }
        long offset = (Math.max(current, 1) - 1) * size;
        List<CrmOrder> orders = orderMapper.selectMyOrderList(offset, size, userId, status);
        Long total = orderMapper.selectMyOrderCount(userId, status);

        List<OrderDetailVO> voList = new ArrayList<>();
        if (orders != null && !orders.isEmpty()) {
            for (CrmOrder order : orders) {
                List<CrmOrderItem> items = orderItemMapper.selectByOrderNo(order.getOrderNo());
                CrmLogistics logistics = logisticsMapper.selectByOrderNo(order.getOrderNo());
                voList.add(OrderDetailVO.builder()
                        .order(order)
                        .items(items != null ? items : Collections.emptyList())
                        .logistics(logistics)
                        .build());
            }
        }

        return PageResult.of(voList, total, current, size);
    }

    @Override
    public OrderDetailVO getOrderDetail(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            throw new BusinessException(500, "订单编号不能为空");
        }

        CrmOrder order = orderMapper.selectByOrderNo(orderNo.trim());
        if (order == null) {
            throw new BusinessException(500, "未查询到对应订单信息");
        }

        List<CrmOrderItem> items = orderItemMapper.selectByOrderNo(orderNo.trim());
        if (items == null) {
            items = Collections.emptyList();
        }

        CrmLogistics logistics = logisticsMapper.selectByOrderNo(orderNo.trim());

        return OrderDetailVO.builder()
                .order(order)
                .items(items)
                .logistics(logistics)
                .build();
    }

    @Override
    public String createMallOrder(MallOrderCreateDTO dto, Long userId, String username) {
        if (dto.getProductId() == null) {
            throw new BusinessException(500, "请指定要购买的商品");
        }
        int buyCount = (dto.getCount() == null || dto.getCount() <= 0) ? 1 : dto.getCount();

        if (!StringUtils.hasText(dto.getReceiverName()) || !StringUtils.hasText(dto.getReceiverPhone()) || !StringUtils.hasText(dto.getReceiverAddress())) {
            throw new BusinessException(500, "收件人姓名、联系电话与收货地址均不能为空");
        }

        // 1. 获取商品粒度的 Redisson 分布式锁保障高并发扣库存不超卖
        String lockKey = PRODUCT_ORDER_LOCK_PREFIX + dto.getProductId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new BusinessException(500, "购买人数过多，请稍后重试");
            }

            // 2. 查验商品在库最新状态 (必须是审核通过且在售状态 status == 1)
            Product product = productMapper.selectById(dto.getProductId());
            if (product == null) {
                throw new BusinessException(500, "所选商品不存在");
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(500, "该商品尚未审核通过或已下架，无法购买");
            }

            // 3. 校验库存充足性
            if (product.getStock() < buyCount) {
                throw new BusinessException(500, "商品库存不足，当前剩余库存仅 " + product.getStock() + " 件");
            }

            // 4. 原子扣减库存
            int affected = productMapper.deductStock(product.getId(), buyCount);
            if (affected <= 0) {
                throw new BusinessException(500, "库存扣减并发冲突，下单失败");
            }

            // 5. 淘汰 Redis 缓存
            clearProductCache(product.getId());

            // 6. 生成订单编号与金额
            String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(buyCount));
            LocalDateTime now = LocalDateTime.now();

            // 7. 插入订单主表 (模拟下单直接免支付，订单状态为 2: 待发货)
            CrmOrder order = CrmOrder.builder()
                    .orderNo(orderNo)
                    .userId(userId != null ? userId : 1L)
                    .customerName(StringUtils.hasText(username) ? username : dto.getReceiverName())
                    .customerPhone(dto.getReceiverPhone())
                    .totalAmount(totalAmount)
                    .status(2) // 待发货
                    .receiverName(dto.getReceiverName().trim())
                    .receiverPhone(dto.getReceiverPhone().trim())
                    .receiverAddress(dto.getReceiverAddress().trim())
                    .remark(dto.getRemark())
                    .payTime(now)
                    .createTime(now)
                    .build();
            orderMapper.insert(order);

            // 8. 插入订单明细项
            CrmOrderItem item = CrmOrderItem.builder()
                    .orderId(order.getId())
                    .orderNo(orderNo)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(buyCount)
                    .subtotal(totalAmount)
                    .build();
            orderItemMapper.batchInsert(List.of(item));

            log.info("商城前台模拟下单成功: orderNo={}, product={}, count={}, status=2(待发货)",
                    orderNo, product.getName(), buyCount);

            return orderNo;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "系统处理中断，请重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(String orderNo, Long userId) {
        if (!StringUtils.hasText(orderNo)) {
            throw new BusinessException(500, "订单编号不能为空");
        }

        CrmOrder order = orderMapper.selectByOrderNo(orderNo.trim());
        if (order == null) {
            throw new BusinessException(500, "目标订单不存在");
        }

        if (order.getStatus() == null || order.getStatus() != 3) {
            throw new BusinessException(500, "只有已发货状态的订单才能进行确认收货操作");
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. 更新订单状态为 4: 已完成
        orderMapper.updateConfirmStatus(orderNo.trim(), 4, now);

        // 2. 同步更新物流运单状态为 4: 已签收
        CrmLogistics logistics = logisticsMapper.selectByOrderNo(orderNo.trim());
        if (logistics != null) {
            logisticsMapper.updateStatus(logistics.getTrackingNo(), 4, now);

            // 3. 自动追加终点签收轨迹
            CrmLogisticsTrack finishTrack = CrmLogisticsTrack.builder()
                    .orderNo(orderNo.trim())
                    .trackingNo(logistics.getTrackingNo())
                    .content("买家已在商城确认收货，订单交易完成。感谢您的光临！")
                    .location(order.getReceiverAddress())
                    .operator("客户本人")
                    .trackTime(now)
                    .build();
            logisticsTrackMapper.insert(finishTrack);
        }

        log.info("买家确认收货成功: orderNo={}, status=4(已完成)", orderNo);
    }

    private void clearProductCache(Long id) {
        try {
            String cacheKey = PRODUCT_DETAIL_CACHE_PREFIX + id;
            stringRedisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.error("淘汰商品 Redis 缓存异常: ", e);
        }
    }
}
