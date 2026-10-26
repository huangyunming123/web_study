package com.demo.biz.controller;

import com.demo.biz.common.R;
import com.demo.biz.dto.ShipOrderDTO;
import com.demo.biz.service.LogisticsService;
import com.demo.biz.vo.LogisticsTrackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * CRM 物流管理控制器
 */
@Tag(name = "CRM物流管理", description = "订单物流发货出库、运单轨迹跟踪及主流快递承运商列表")
@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class LogisticsController {
    @Resource
    private final LogisticsService logisticsService;

    @Operation(summary = "订单物流发货出库")
    @PostMapping("/ship")
    public R<Void> ship(@RequestBody ShipOrderDTO dto) {
        logisticsService.shipOrder(dto);
        return R.ok();
    }

    @Operation(summary = "根据订单流水号查询物流轨迹")
    @GetMapping("/track/order/{orderNo}")
    public R<LogisticsTrackVO> getTrackByOrderNo(
            @Parameter(description = "订单流水编号", required = true) @PathVariable String orderNo) {
        LogisticsTrackVO vo = logisticsService.getTrackByOrderNo(orderNo);
        return R.ok(vo);
    }

    @Operation(summary = "根据快递运单号查询物流轨迹")
    @GetMapping("/track")
    public R<LogisticsTrackVO> getTrackByTrackingNo(
            @Parameter(description = "快递运单号", required = true) @RequestParam String trackingNo) {
        LogisticsTrackVO vo = logisticsService.getTrackByTrackingNo(trackingNo);
        return R.ok(vo);
    }

    @Operation(summary = "获取支持的快递公司字典")
    @GetMapping("/companies")
    public R<List<Map<String, String>>> getCompanies() {
        List<Map<String, String>> companies = logisticsService.getSupportedCompanies();
        return R.ok(companies);
    }
}
