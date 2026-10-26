import request from '../utils/request';

/**
 * 分页查询用户列表 (带角色信息)
 */
export function getUserPage(params) {
  return request({
    url: '/api/user/page',
    method: 'get',
    params
  });
}

/**
 * 修改用户名称/昵称
 */
export function updateUser(id, data) {
  return request({
    url: `/api/user/${id}`,
    method: 'put',
    data
  });
}

/**
 * 修改用户状态
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/api/user/${id}/status`,
    method: 'put',
    params: { status }
  });
}

/**
 * 为用户分配角色
 */
export function assignUserRoles(id, roleIds) {
  return request({
    url: `/api/user/${id}/roles`,
    method: 'put',
    data: { roleIds }
  });
}

/**
 * 物理删除用户
 */
export function deleteUser(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  });
}
