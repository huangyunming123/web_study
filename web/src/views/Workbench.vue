<template>
  <div class="workbench-container">
    <!-- 欢迎卡片 -->
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <el-avatar :size="54" icon="UserFilled" class="user-avatar" />
        <div class="welcome-text">
          <h2 class="welcome-title">
            欢迎回来，{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}！
          </h2>
          <p class="welcome-subtitle">
            CRM 客户关系与运营中心已准备就绪，今日已处理大部分待发货运单。
          </p>
        </div>
      </div>
    </el-card>

    <!-- 统计指标卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-info">
              <span class="stat-label">总订单量</span>
              <span class="stat-value text-primary">{{ stats.totalOrders }}</span>
              <span class="stat-trend">已接入多状态跟踪</span>
            </div>
            <div class="stat-icon bg-primary-light">
              <el-icon :size="28" color="#409eff"><Tickets /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-info">
              <span class="stat-label">待发货订单</span>
              <span class="stat-value text-warning">{{ stats.waitShipOrders }}</span>
              <span class="stat-trend">急需仓储配货出库</span>
            </div>
            <div class="stat-icon bg-warning-light">
              <el-icon :size="28" color="#e6a23c"><Van /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-info">
              <span class="stat-label">在售商品总数</span>
              <span class="stat-value text-success">{{ stats.totalProducts }}</span>
              <span class="stat-trend">分布式锁保障库存</span>
            </div>
            <div class="stat-icon bg-success-light">
              <el-icon :size="28" color="#67c23a"><GoodsFilled /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-info">
              <span class="stat-label">系统用户规模</span>
              <span class="stat-value text-info">{{ stats.totalUsers }}</span>
              <span class="stat-trend">细粒度 RBAC 权限</span>
            </div>
            <div class="stat-icon bg-info-light">
              <el-icon :size="28" color="#909399"><Avatar /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷导航与最近订单 -->
    <el-row :gutter="16" class="main-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="recent-orders-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><List /></el-icon> 最近待处理订单
              </span>
              <el-button link type="primary" @click="router.push('/order')">
                查看全部订单 &gt;
              </el-button>
            </div>
          </template>

          <el-table :data="recentOrders" border stripe style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" min-width="170" show-overflow-tooltip />
            <el-table-column prop="customerName" label="客户" width="100" />
            <el-table-column prop="totalAmount" label="总金额" width="110" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥ {{ Number(row.totalAmount).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="170" align="center" />
            <el-table-column label="操作" width="110" align="center">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="router.push('/order')">
                  去处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="shortcut-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><Operation /></el-icon> CRM 业务快捷入口
              </span>
            </div>
          </template>

          <div class="shortcut-grid">
            <div class="shortcut-item" @click="router.push('/order')">
              <el-icon :size="24" color="#409eff"><Tickets /></el-icon>
              <span>订单查询</span>
            </div>
            <div class="shortcut-item" @click="router.push('/product')">
              <el-icon :size="24" color="#67c23a"><Plus /></el-icon>
              <span>商品发布</span>
            </div>
            <div class="shortcut-item" @click="router.push('/logistics')">
              <el-icon :size="24" color="#e6a23c"><Van /></el-icon>
              <span>物流发货</span>
            </div>
            <div class="shortcut-item" @click="router.push('/logistics')">
              <el-icon :size="24" color="#909399"><LocationFilled /></el-icon>
              <span>轨迹追踪</span>
            </div>
            <div class="shortcut-item" @click="router.push('/user')">
              <el-icon :size="24" color="#f56c6c"><User /></el-icon>
              <span>用户管理</span>
            </div>
            <div class="shortcut-item" @click="router.push('/role')">
              <el-icon :size="24" color="#b37feb"><Lock /></el-icon>
              <span>权限分配</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="tips-card" style="margin-top: 16px">
          <template #header>
            <span class="header-title">
              <el-icon><InfoFilled /></el-icon> 架构核心技术点
            </span>
          </template>
          <div class="tips-content">
            <p>• <b>高并发防超卖</b>：Redisson 分布式互斥锁 + 数据库乐观库存扣减。</p>
            <p>• <b>微服务双重鉴权</b>：JJWT 无状态令牌 + Redis 实时在线会话互踢。</p>
            <p>• <b>企业级 RBAC</b>：用户-角色-权限三级解耦，支持菜单与按钮级鉴权。</p>
            <p>• <b>全链路追踪</b>：物流流转节点按时序精确记录与可视化时间轴呈现。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store/user';
import { getOrderPage } from '../api/order';
import { getProductPage } from '../api/product';
import { getUserPage } from '../api/user';

const router = useRouter();
const userStore = useUserStore();

const stats = reactive({
  totalOrders: 0,
  waitShipOrders: 0,
  totalProducts: 0,
  totalUsers: 0
});

const recentOrders = ref([]);

const getStatusLabel = (status) => {
  const map = { 1: '待支付', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' };
  return map[status] || '未知';
};

const getStatusTag = (status) => {
  const map = { 1: 'warning', 2: 'danger', 3: 'primary', 4: 'success', 5: 'info' };
  return map[status] || 'info';
};

const initData = async () => {
  try {
    // 1. 获取订单列表
    const orderRes = await getOrderPage({ page: 1, size: 5 });
    recentOrders.value = orderRes.records || [];
    stats.totalOrders = orderRes.total || 0;

    // 统计待发货
    const waitShipRes = await getOrderPage({ page: 1, size: 1, status: 2 });
    stats.waitShipOrders = waitShipRes.total || 0;

    // 2. 获取商品总数
    const productRes = await getProductPage({ page: 1, size: 1 });
    stats.totalProducts = productRes.total || 0;

    // 3. 获取用户总数
    const userRes = await getUserPage({ page: 1, size: 1 });
    stats.totalUsers = userRes.total || 0;
  } catch (err) {
    // 异常已被 request 统一捕获
  }
};

onMounted(() => {
  initData();
});
</script>

<style scoped>
.workbench-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-card {
  border-radius: 8px;
  background: linear-gradient(135deg, #ffffff 0%, #f7f9fc 100%);
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 18px;
}

.user-avatar {
  background-color: #409eff;
}

.welcome-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.welcome-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: #909399;
}

.stat-card {
  border-radius: 8px;
}

.stat-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
}

.stat-trend {
  font-size: 12px;
  color: #a8abb2;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bg-primary-light { background-color: #ecf5ff; }
.bg-warning-light { background-color: #fdf6ec; }
.bg-success-light { background-color: #f0f9eb; }
.bg-info-light { background-color: #f4f4f5; }

.text-primary { color: #409eff; }
.text-warning { color: #e6a23c; }
.text-success { color: #67c23a; }
.text-info { color: #303133; }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.amount-text {
  font-weight: 600;
  color: #f56c6c;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 8px;
  background-color: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
  color: #606266;
}

.shortcut-item:hover {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
  color: #409eff;
  transform: translateY(-2px);
}

.tips-content p {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
  margin: 6px 0;
}
</style>
