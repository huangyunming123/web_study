import request from '../utils/request';

/**
 * 账号登录
 */
export function loginApi(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  });
}

/**
 * 账号注册
 */
export function registerApi(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  });
}
