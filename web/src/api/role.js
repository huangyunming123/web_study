import request from '../utils/request';

/**
 * 获取系统全部角色列表
 */
export function getRoleList() {
  return request({
    url: '/api/role/list',
    method: 'get'
  });
}

/**
 * 获取系统全量权限树 (菜单及按钮功能)
 */
export function getPermissionTree() {
  return request({
    url: '/api/role/permission/tree',
    method: 'get'
  });
}

/**
 * 获取指定角色已配置的权限ID列表
 */
export function getRolePermissions(roleId) {
  return request({
    url: `/api/role/${roleId}/permissions`,
    method: 'get'
  });
}

/**
 * 为指定角色配置权限列表
 */
export function assignRolePermissions(roleId, permissionIds) {
  return request({
    url: `/api/role/${roleId}/permissions`,
    method: 'put',
    data: { permissionIds }
  });
}
