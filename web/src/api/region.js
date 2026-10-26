import request from '../utils/request';

/**
 * 获取行政地区树
 */
export function getRegionTree() {
  return request({
    url: '/api/region/tree',
    method: 'get'
  });
}

/**
 * 新增行政地区
 */
export function addRegion(data) {
  return request({
    url: '/api/region',
    method: 'post',
    data
  });
}

/**
 * 修改行政地区
 */
export function updateRegion(id, data) {
  return request({
    url: `/api/region/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除行政地区 (存在子级将失败)
 */
export function deleteRegion(id) {
  return request({
    url: `/api/region/${id}`,
    method: 'delete'
  });
}
