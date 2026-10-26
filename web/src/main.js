import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { createPinia } from 'pinia';

// 全量引入 Element Plus 及其完整样式
// 【选择说明】: 在教学演示与快速工程验证中，全量引入无需配置复杂的按需解析器 (unplugin-vue-components)，
// 能最大程度杜绝样式缺失、打包命名冲突等环境排错成本，开箱即用。
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import zhCn from 'element-plus/es/locale/lang/zh-cn';

// 全局注册 Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

const app = createApp(App);
const pinia = createPinia();

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.use(pinia);
app.use(router);
app.use(ElementPlus, { locale: zhCn });

app.mount('#app');
