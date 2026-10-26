import request from '../utils/request';

/**
 * 订单发货出库
 */
export function shipOrder(data) {
  return request({
    url: '/api/logistics/ship',
    method: 'post',
    data
  });
}

/**
 * 根据订单流水编号查询物流轨迹
 */
export function getLogisticsTrackByOrderNo(orderNo) {
  return request({
    url: `/api/logistics/track/order/${orderNo}`,
    method: 'get'
  });
}

/**
 * 根据运单号查询物流轨迹
 */
export function getLogisticsTrackByTrackingNo(trackingNo) {
  return request({
    url: '/api/logistics/track',
    method: 'get',
    params: { trackingNo }
  });
}

/**
 * 获取支持的快递公司字典
 */
export function getLogisticsCompanies() {
  return request({
    url: '/api/logistics/companies',
    method: 'get'
  });
}
