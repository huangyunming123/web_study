package com.demo.biz.controller;

import com.demo.biz.common.PageResult;
import com.demo.biz.common.R;
import com.demo.biz.dto.MallOrderCreateDTO;
import com.demo.biz.entity.CrmOrder;
import com.demo.biz.service.OrderService;
import com.demo.biz.vo.OrderDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRM 订单管理控制器
 */
@Tag(name = "CRM订单管理", description = "订单多条件查询、商城模拟下单、买家确认收货及状态流转追踪")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "多条件分页查询订单列表 (后台管理)")
    @GetMapping("/page")
    public R<PageResult<CrmOrder>> page(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "订单流水编号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "客户姓名") @RequestParam(required = false) String customerName,
            @Parameter(description = "客户手机号") @RequestParam(required = false) String customerPhone,
            @Parameter(description = "订单状态(1:待支付, 2:待发货, 3:已发货, 4:已完成, 5:已取消)") @RequestParam(required = false) Integer status,
            @Parameter(description = "创建起始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "创建截止时间") @RequestParam(required = false) String endTime) {
        PageResult<CrmOrder> result = orderService.page(page, size, orderNo, customerName, customerPhone, status, startTime, endTime);
        return R.ok(result);
    }

    @Operation(summary = "商城前台查询我的订单列表")
    @GetMapping("/my-page")
    public R<PageResult<OrderDetailVO>> myOrders(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<OrderDetailVO> result = orderService.myOrders(page, size, userId, status);
        return R.ok(result);
    }

    @Operation(summary = "根据订单编号查询订单完整详情")
    @GetMapping("/{orderNo}")
    public R<OrderDetailVO> getDetail(
            @Parameter(description = "订单流水号", required = true) @PathVariable String orderNo) {
        OrderDetailVO detail = orderService.getOrderDetail(orderNo);
        return R.ok(detail);
    }

    @Operation(summary = "商城前台模拟下单 (免支付直接流转为待发货)")
    @PostMapping("/mall-create")
    public R<String> mallCreateOrder(@RequestBody MallOrderCreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String orderNo = orderService.createMallOrder(dto, userId, username);
        return R.ok(orderNo);
    }

    @Operation(summary = "商城买家确认收货 (流转为已完成)")
    @PutMapping("/{orderNo}/confirm")
    public R<Void> confirmReceipt(
            @Parameter(description = "订单编号", required = true) @PathVariable String orderNo,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.confirmReceipt(orderNo, userId);
        return R.ok();
    }
}
