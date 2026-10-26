import request from '../utils/request';

/**
 * 分页多条件查询商品列表
 */
export function getProductPage(params) {
  return request({
    url: '/api/product/page',
    method: 'get',
    params
  });
}

/**
 * 查询商品详情 (优先命中 Redis 缓存)
 */
export function getProductDetail(id) {
  return request({
    url: `/api/product/${id}`,
    method: 'get'
  });
}

/**
 * 发布新商品 (提交后进入待审核状态)
 */
export function addProduct(data) {
  return request({
    url: '/api/product',
    method: 'post',
    data
  });
}

/**
 * 修改商品信息
 */
export function updateProduct(id, data) {
  return request({
    url: `/api/product/${id}`,
    method: 'put',
    data
  });
}

/**
 * 切换商品上下架状态
 */
export function updateProductStatus(id, status) {
  return request({
    url: `/api/product/${id}/status`,
    method: 'put',
    params: { status }
  });
}

/**
 * 审核商品 (1:通过, 2:驳回)
 */
export function auditProduct(id, status, rejectReason) {
  return request({
    url: `/api/product/${id}/audit`,
    method: 'put',
    params: { status, rejectReason }
  });
}

/**
 * 模拟秒杀下单 (Redisson 分布式锁保障高并发)
 */
export function createOrder(data) {
  return request({
    url: '/api/product/order',
    method: 'post',
    data
  });
}

/**
 * 物理删除商品
 */
export function deleteProduct(id) {
  return request({
    url: `/api/product/${id}`,
    method: 'delete'
  });
}
