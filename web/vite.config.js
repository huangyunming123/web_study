import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      // 鉴权相关请求反向代理到纯转发网关 (Port 8080)
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 业务相关请求反向代理到纯转发网关 (Port 8080)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
