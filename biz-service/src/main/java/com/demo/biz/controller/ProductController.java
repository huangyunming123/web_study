package com.demo.biz.controller;

import com.demo.biz.common.PageResult;
import com.demo.biz.common.R;
import com.demo.biz.dto.OrderCreateDTO;
import com.demo.biz.entity.Product;
import com.demo.biz.service.ProductService;
import com.demo.biz.vo.OrderResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRM 商品管理控制器
 */
@Tag(name = "CRM商品管理", description = "商品的增删改查、分类筛选、审核、上下架切换及库存管理")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页多条件查询商品列表")
    @GetMapping("/page")
    public R<PageResult<Product>> page(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "商品分类") @RequestParam(required = false) String category,
            @Parameter(description = "状态(0:待审核, 1:在售, 2:已驳回, 3:已下架)") @RequestParam(required = false) Integer status) {
        PageResult<Product> result = productService.page(page, size, keyword, category, status);
        return R.ok(result);
    }

    @Operation(summary = "查询商品详情 (优先命中 Redis 缓存)")
    @GetMapping("/{id}")
    public R<Product> getById(@Parameter(description = "商品ID", required = true) @PathVariable Long id) {
        Product product = productService.getById(id);
        return R.ok(product);
    }

    @Operation(summary = "发布新商品 (提交后进入待审核状态)")
    @PostMapping
    public R<Void> add(@RequestBody Product product) {
        productService.add(product);
        return R.ok();
    }

    @Operation(summary = "修改商品 (同步淘汰 Redis 缓存)")
    @PutMapping("/{id}")
    public R<Void> update(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @RequestBody Product product) {
        productService.update(id, product);
        return R.ok();
    }

    @Operation(summary = "切换商品上下架状态")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Parameter(description = "目标状态 (1:在售, 3:已下架)", required = true) @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "商品发布审核 (通过 / 驳回)")
    @PutMapping("/{id}/audit")
    public R<Void> audit(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Parameter(description = "目标状态 (1:审核通过在售, 2:审核驳回)", required = true) @RequestParam Integer status,
            @Parameter(description = "驳回理由 (驳回时必填)") @RequestParam(required = false) String rejectReason) {
        productService.audit(id, status, rejectReason);
        return R.ok();
    }

    @Operation(summary = "模拟秒杀下单 (Redisson 分布式锁保障高并发不超卖)")
    @PostMapping("/order")
    public R<OrderResultVO> createOrder(@RequestBody OrderCreateDTO dto) {
        OrderResultVO result = productService.createOrder(dto);
        return R.ok(result);
    }

    @Operation(summary = "物理删除商品 (同步淘汰 Redis 缓存)")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "商品ID", required = true) @PathVariable Long id) {
        productService.delete(id);
        return R.ok();
    }
}
