package com.demo.biz.service.impl;

import com.demo.biz.common.BusinessException;
import com.demo.biz.dto.ShipOrderDTO;
import com.demo.biz.entity.CrmLogistics;
import com.demo.biz.entity.CrmLogisticsTrack;
import com.demo.biz.entity.CrmOrder;
import com.demo.biz.mapper.LogisticsMapper;
import com.demo.biz.mapper.LogisticsTrackMapper;
import com.demo.biz.mapper.OrderMapper;
import com.demo.biz.service.LogisticsService;
import com.demo.biz.vo.LogisticsTrackVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CRM 物流服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final LogisticsMapper logisticsMapper;
    private final LogisticsTrackMapper logisticsTrackMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(ShipOrderDTO dto) {
        if (!StringUtils.hasText(dto.getOrderNo())) {
            throw new BusinessException(500, "订单编号不能为空");
        }
        if (!StringUtils.hasText(dto.getCompanyName())) {
            throw new BusinessException(500, "承运快递公司不能为空");
        }
        if (!StringUtils.hasText(dto.getTrackingNo())) {
            throw new BusinessException(500, "快递单号不能为空");
        }

        String orderNo = dto.getOrderNo().trim();
        CrmOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(500, "所选订单不存在");
        }

        if (order.getStatus() != null && order.getStatus() >= 3) {
            throw new BusinessException(500, "该订单已处于已发货或已完成状态，请勿重复发货");
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. 更新订单主表状态为已发货 (status = 3)，记录发货时间
        orderMapper.updateShipStatus(orderNo, 3, now);

        // 2. 检查是否已存在物流记录
        CrmLogistics existingLogistics = logisticsMapper.selectByOrderNo(orderNo);
        if (existingLogistics != null) {
            throw new BusinessException(500, "该订单已绑定物流运单: " + existingLogistics.getTrackingNo());
        }

        // 3. 插入物流运单主表
        String operator = StringUtils.hasText(dto.getOperatorName()) ? dto.getOperatorName().trim() : "仓储物流专员";
        CrmLogistics logistics = CrmLogistics.builder()
                .orderNo(orderNo)
                .companyName(dto.getCompanyName().trim())
                .companyCode(StringUtils.hasText(dto.getCompanyCode()) ? dto.getCompanyCode().trim() : "COMMON")
                .trackingNo(dto.getTrackingNo().trim())
                .status(2) // 运输中
                .operatorName(operator)
                .shipTime(now)
                .build();
        logisticsMapper.insert(logistics);

        // 4. 自动生成初始揽收与出库轨迹节点
        CrmLogisticsTrack track1 = CrmLogisticsTrack.builder()
                .orderNo(orderNo)
                .trackingNo(dto.getTrackingNo().trim())
                .content("包裹已从【华南总仓发货中心】完成出库打包，交接至【" + dto.getCompanyName().trim() + "】")
                .location("华南总仓发货中心")
                .operator(operator)
                .trackTime(now)
                .build();
        logisticsTrackMapper.insert(track1);

        CrmLogisticsTrack track2 = CrmLogisticsTrack.builder()
                .orderNo(orderNo)
                .trackingNo(dto.getTrackingNo().trim())
                .content("【" + dto.getCompanyName().trim() + "】已正式揽收，运单号：" + dto.getTrackingNo().trim() + "，正发往区域枢纽中心")
                .location(dto.getCompanyName().trim() + "揽收网点")
                .operator("揽收员")
                .trackTime(now.plusMinutes(15))
                .build();
        logisticsTrackMapper.insert(track2);

        log.info("CRM 订单发货成功: orderNo={}, trackingNo={}, company={}", orderNo, dto.getTrackingNo(), dto.getCompanyName());
    }

    @Override
    public LogisticsTrackVO getTrackByOrderNo(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            throw new BusinessException(500, "订单编号不能为空");
        }
        CrmLogistics logistics = logisticsMapper.selectByOrderNo(orderNo.trim());
        if (logistics == null) {
            return null;
        }

        List<CrmLogisticsTrack> tracks = logisticsTrackMapper.selectByOrderNo(orderNo.trim());
        return LogisticsTrackVO.builder()
                .logistics(logistics)
                .tracks(tracks != null ? tracks : new ArrayList<>())
                .build();
    }

    @Override
    public LogisticsTrackVO getTrackByTrackingNo(String trackingNo) {
        if (!StringUtils.hasText(trackingNo)) {
            throw new BusinessException(500, "运单号不能为空");
        }
        CrmLogistics logistics = logisticsMapper.selectByTrackingNo(trackingNo.trim());
        if (logistics == null) {
            return null;
        }

        List<CrmLogisticsTrack> tracks = logisticsTrackMapper.selectByTrackingNo(trackingNo.trim());
        return LogisticsTrackVO.builder()
                .logistics(logistics)
                .tracks(tracks != null ? tracks : new ArrayList<>())
                .build();
    }

    @Override
    public List<Map<String, String>> getSupportedCompanies() {
        return List.of(
                Map.of("name", "顺丰速运", "code", "SF"),
                Map.of("name", "京东快递", "code", "JD"),
                Map.of("name", "中通快递", "code", "ZTO"),
                Map.of("name", "圆通速递", "code", "YTO"),
                Map.of("name", "申通快递", "code", "STO"),
                Map.of("name", "极兔速递", "code", "JTS"),
                Map.of("name", "中国邮政EMS", "code", "EMS")
        );
    }
}
