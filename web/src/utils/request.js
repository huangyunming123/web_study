import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';
import { useUserStore } from '../store/user';

// 创建全局 Axios 实例
const service = axios.create({
  baseURL: '/',
  timeout: 10000
});

// 请求拦截器: 自动附加 Bearer Token 请求头
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器: 统一业务状态码处理与数据解包
service.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 若响应是二进制流/Blob等直接返回
    if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
      return res;
    }

    // 约定统一返回结构: { code, msg, data }
    if (res.code === 200) {
      // 成功直接返回解包后的 data
      return res.data;
    }

    // 401: 未登录或凭据失效
    if (res.code === 401) {
      const userStore = useUserStore();
      userStore.clearAuth();
      ElMessage.error(res.msg || '登录已过期，请重新登录');
      router.push('/login');
      return Promise.reject(new Error(res.msg || 'Error 401'));
    }

    // 其他业务异常
    ElMessage.error(res.msg || '系统异常');
    return Promise.reject(new Error(res.msg || 'Error'));
  },
  (error) => {
    // 处理 HTTP 协议层面的状态码 (如网关返回的 401 或 502)
    if (error.response) {
      const { status, data } = error.response;
      const errorMsg = (data && data.msg) || error.message;

      if (status === 401) {
        const userStore = useUserStore();
        userStore.clearAuth();
        ElMessage.error(errorMsg || '登录已过期，请重新登录');
        router.push('/login');
      } else if (status === 502) {
        ElMessage.error('微服务下游不可达 (502 Bad Gateway)，请检查服务启动状态');
      } else {
        ElMessage.error(errorMsg || `网络请求错误 [${status}]`);
      }
    } else {
      ElMessage.error(error.message || '网络连接超时或连接失败');
    }
    return Promise.reject(error);
  }
);

export default service;
