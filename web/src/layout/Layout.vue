<template>
  <el-container class="layout-container">
    <!-- 左侧菜单栏 -->
    <el-aside width="240px" class="layout-aside">
      <div class="logo-area">
        <el-icon :size="26" color="#409EFF"><Platform /></el-icon>
        <span class="logo-title">CRM 运营管理系统</span>
      </div>
      <el-menu
        :default-active="activePath"
        router
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/workbench">
          <el-icon><Odometer /></el-icon>
          <span>CRM 工作台</span>
        </el-menu-item>

        <el-menu-item index="/mall">
          <el-icon><ShoppingCart /></el-icon>
          <span>甄选商城 (买家端)</span>
        </el-menu-item>

        <el-menu-item index="/order">
          <el-icon><Tickets /></el-icon>
          <span>订单综合管理</span>
        </el-menu-item>

        <el-sub-menu index="product-manage">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理中心</span>
          </template>
          <el-menu-item index="/product">
            <el-icon><Menu /></el-icon>
            <span>商品发布与查询</span>
          </el-menu-item>
          <el-menu-item index="/product/audit">
            <el-icon><DocumentChecked /></el-icon>
            <span>商品发布审核</span>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/logistics">
          <el-icon><Van /></el-icon>
          <span>物流发货与追踪</span>
        </el-menu-item>

        <el-sub-menu index="system-manage">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>用户与权限中心</span>
          </template>
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>用户综合管理</span>
          </el-menu-item>
          <el-menu-item index="/role">
            <el-icon><Lock /></el-icon>
            <span>角色权限分配</span>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/region">
          <el-icon><Location /></el-icon>
          <span>行政地区字典</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主体内容 -->
    <el-container class="layout-main-container">
      <!-- 顶栏 Header -->
      <el-header class="layout-header">
        <div class="header-left">
          <span class="breadcrumb-title">{{ currentRouteTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown-link">
              <el-avatar :size="32" icon="UserFilled" class="user-avatar" />
              <span class="user-nickname">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容 Main -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../store/user';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const activePath = computed(() => route.path);
const currentRouteTitle = computed(() => route.meta.title || '');

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出当前登录状态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.clearAuth();
      ElMessage.success('已安全退出登录');
      router.push('/login');
    }).catch(() => {});
  }
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}

.layout-aside {
  background-color: #304156;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background-color: #263445;
  color: #fff;
}

.logo-title {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

.layout-main-container {
  background-color: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.layout-header {
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.breadcrumb-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.user-avatar {
  background-color: #409EFF;
}

.user-nickname {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.layout-main {
  padding: 20px;
  overflow-y: auto;
}
</style>
