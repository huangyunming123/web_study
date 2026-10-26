import request from '../utils/request';

/**
 * 分页多条件查询订单列表 (后台管理)
 */
export function getOrderPage(params) {
  return request({
    url: '/api/order/page',
    method: 'get',
    params
  });
}

/**
 * 商城前台查询当前登录用户的个人订单列表
 */
export function getMyOrderPage(params) {
  return request({
    url: '/api/order/my-page',
    method: 'get',
    params
  });
}

/**
 * 根据订单编号获取订单完整详情 (含商品明细快照与物流运单)
 */
export function getOrderDetail(orderNo) {
  return request({
    url: `/api/order/${orderNo}`,
    method: 'get'
  });
}

/**
 * 商城前台模拟下单 (免支付直接流转为待发货)
 */
export function createMallOrder(data) {
  return request({
    url: '/api/order/mall-create',
    method: 'post',
    data
  });
}

/**
 * 商城买家确认收货 (流转为已完成，同步更新物流为已签收)
 */
export function confirmOrderReceipt(orderNo) {
  return request({
    url: `/api/order/${orderNo}/confirm`,
    method: 'put'
  });
}
