package com.demo.biz.service.impl;

import com.demo.biz.common.BusinessException;
import com.demo.biz.common.PageResult;
import com.demo.biz.dto.OrderCreateDTO;
import com.demo.biz.entity.Product;
import com.demo.biz.mapper.ProductMapper;
import com.demo.biz.service.ProductService;
import com.demo.biz.vo.OrderResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * CRM 商品服务实现类 (原生 MyBatis + Redis 缓存 + Redisson 分布式锁)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PRODUCT_DETAIL_CACHE_PREFIX = "product:detail:";
    private static final String PRODUCT_ORDER_LOCK_PREFIX = "lock:order:product:";

    @Override
    public PageResult<Product> page(long current, long size, String keyword, String category, Integer status) {
        long offset = (Math.max(current, 1) - 1) * size;
        String trimKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String trimCategory = StringUtils.hasText(category) ? category.trim() : null;

        List<Product> records = productMapper.selectPageList(offset, size, trimKeyword, trimCategory, status);
        Long total = productMapper.selectPageCount(trimKeyword, trimCategory, status);

        return PageResult.of(records, total, current, size);
    }

    @Override
    public Product getById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = PRODUCT_DETAIL_CACHE_PREFIX + id;

        // 1. 先查 Redis 缓存
        try {
            String cacheJson = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cacheJson)) {
                log.info("【Redis 缓存命中】商品详情: id={}", id);
                return objectMapper.readValue(cacheJson, Product.class);
            }
        } catch (Exception e) {
            log.error("读取商品 Redis 缓存异常，降级查询数据库: ", e);
        }

        // 2. 缓存未命中，查 MySQL 数据库
        Product product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }

        // 3. 写入 Redis 缓存 (TTL 10分钟)
        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(product),
                    Duration.ofMinutes(10)
            );
            log.info("【Redis 缓存写入】商品详情写入缓存成功: id={}", id);
        } catch (Exception e) {
            log.error("写入商品 Redis 缓存异常: ", e);
        }

        return product;
    }

    @Override
    public void add(Product product) {
        if (!StringUtils.hasText(product.getName())) {
            throw new BusinessException(500, "商品名称不能为空");
        }
        if (!StringUtils.hasText(product.getCategory())) {
            product.setCategory("数码电子");
        }
        // 发布的新商品默认进入待审核状态 (0: 待审核)
        if (product.getStatus() == null) {
            product.setStatus(0);
        }
        product.setId(null);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
        log.info("CRM 发布新商品(待审核): id={}, name={}", product.getId(), product.getName());
    }

    @Override
    public void update(Long id, Product product) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(500, "目标商品不存在");
        }
        product.setId(id);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);
        log.info("CRM 修改商品成功: id={}", id);

        // 修改后主动淘汰 Redis 缓存，保证数据一致性
        clearProductCache(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(500, "目标商品不存在");
        }
        productMapper.updateStatus(id, status);
        log.info("CRM 切换商品状态: id={}, status={}", id, status);
        clearProductCache(id);
    }

    @Override
    public void audit(Long id, Integer status, String rejectReason) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(500, "目标商品不存在");
        }
        if (status != 1 && status != 2) {
            throw new BusinessException(500, "审核状态非法 (1:通过, 2:驳回)");
        }
        if (status == 2 && !StringUtils.hasText(rejectReason)) {
            throw new BusinessException(500, "审核驳回时必须填写驳回原因");
        }

        productMapper.audit(id, status, status == 2 ? rejectReason.trim() : null);
        log.info("CRM 审核商品操作成功: id={}, status={}, reason={}", id, status, rejectReason);
        clearProductCache(id);
    }

    @Override
    public OrderResultVO createOrder(OrderCreateDTO dto) {
        if (dto.getProductId() == null) {
            throw new BusinessException(500, "请指定下单商品");
        }
        int buyCount = (dto.getCount() == null || dto.getCount() <= 0) ? 1 : dto.getCount();

        // 1. 获取商品粒度的 Redisson 分布式锁
        String lockKey = PRODUCT_ORDER_LOCK_PREFIX + dto.getProductId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("获取商品分布式锁超时，排队人数过多: productId={}", dto.getProductId());
                throw new BusinessException(500, "购买人数过多，请稍后重试");
            }

            log.info("【Redisson 分布式锁加锁成功】线程: {}, lockKey={}", Thread.currentThread().getName(), lockKey);

            Product product = productMapper.selectById(dto.getProductId());
            if (product == null) {
                throw new BusinessException(500, "所选商品不存在");
            }

            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(500, "该商品未通过审核或已下架，无法购买");
            }

            if (product.getStock() < buyCount) {
                log.warn("商品库存不足: productId={}, 当前库存={}, 请求购买={}", product.getId(), product.getStock(), buyCount);
                throw new BusinessException(500, "商品库存不足，当前剩余库存仅 " + product.getStock() + " 件");
            }

            int affectedRows = productMapper.deductStock(product.getId(), buyCount);
            if (affectedRows <= 0) {
                throw new BusinessException(500, "库存扣减并发冲突，下单失败");
            }

            int surplusStock = product.getStock() - buyCount;
            log.info("【下单扣减库存成功】商品: {}, 扣减件数: {}, 剩余库存: {}", product.getName(), buyCount, surplusStock);

            clearProductCache(product.getId());

            String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(buyCount));

            return OrderResultVO.builder()
                    .orderNo(orderNo)
                    .productId(product.getId())
                    .productName(product.getName())
                    .count(buyCount)
                    .price(product.getPrice())
                    .totalAmount(totalAmount)
                    .surplusStock(surplusStock)
                    .createTime(LocalDateTime.now())
                    .build();

        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "系统处理中断，请重试");
        }
        finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("【Redisson 分布式锁已安全释放】lockKey={}", lockKey);
            }
        }
    }

    @Override
    public void delete(Long id) {
        int rows = productMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(500, "待删除商品不存在");
        }
        log.info("物理删除商品成功: id={}", id);
        clearProductCache(id);
    }

    private void clearProductCache(Long id) {
        try {
            String cacheKey = PRODUCT_DETAIL_CACHE_PREFIX + id;
            Boolean deleted = stringRedisTemplate.delete(cacheKey);
            log.info("淘汰商品 Redis 缓存: key={}, success={}", cacheKey, deleted);
        } catch (Exception e) {
            log.error("淘汰商品 Redis 缓存异常: ", e);
        }
    }
}
