import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 初始化恢复 token 与 用户信息
  const token = ref(localStorage.getItem('token') || '');
  const userInfo = ref(
    localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')) : null
  );

  /**
   * 登录成功后设置凭据
   */
  const setLoginInfo = (newToken, info) => {
    token.value = newToken;
    userInfo.value = info;
    localStorage.setItem('token', newToken);
    localStorage.setItem('userInfo', JSON.stringify(info));
  };

  /**
   * 退出登录并清除本地持久化缓存
   */
  const clearAuth = () => {
    token.value = '';
    userInfo.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
  };

  return {
    token,
    userInfo,
    setLoginInfo,
    clearAuth
  };
});
