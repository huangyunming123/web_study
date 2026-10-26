<template>
  <div class="mall-page">
    <!-- 商城顶部横幅与搜索栏 -->
    <div class="mall-banner">
      <div class="banner-inner">
        <div class="banner-top-actions">
          <el-button
            type="warning"
            icon="List"
            round
            size="large"
            class="my-orders-btn"
            @click="openMyOrdersDrawer"
          >
            我的订单中心
            <el-badge v-if="waitReceiveCount > 0" :value="waitReceiveCount" class="badge-item" />
          </el-button>
        </div>
        <h1 class="banner-title">
          <el-icon :size="32"><ShoppingCart /></el-icon>
          甄选数码商城
        </h1>
        <p class="banner-sub">
          在线挑选已通过质检验核的在售正品，支持极速模拟下单与物流全链路追踪
        </p>

        <!-- 搜索与分类快速筛选 -->
        <div class="search-bar">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索心仪的商品名称，如：iPhone、耳机、显示器..."
            size="large"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" :loading="loading" @click="handleSearch">
                搜索
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="category-pills">
          <span
            v-for="cat in categoryList"
            :key="cat"
            class="category-pill"
            :class="{ active: queryParams.category === cat }"
            @click="selectCategory(cat)"
          >
            {{ cat || '全部分类' }}
          </span>
        </div>
      </div>
    </div>

    <!-- 结果统计栏 -->
    <div class="result-bar">
      <span>
        共找到
        <b class="total-num">{{ total }}</b>
        件已过审在售商品
        <template v-if="queryParams.category">
          ，类目「<span class="keyword-tag">{{ queryParams.category }}</span>」
        </template>
        <template v-if="queryParams.keyword">
          ，关键词「<span class="keyword-tag">{{ queryParams.keyword }}</span>」
        </template>
      </span>
      <el-button v-if="queryParams.keyword || queryParams.category" link type="primary" icon="RefreshLeft" @click="handleReset">
        清除筛选
      </el-button>
    </div>

    <!-- 商品卡片网格 -->
    <div v-loading="loading" class="product-grid">
      <el-empty v-if="!loading && tableData.length === 0" description="暂无符合条件的在售商品" />
      <el-row :gutter="18">
        <el-col
          v-for="item in tableData"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card shadow="hover" class="product-card" :body-style="{ padding: '0' }">
            <div class="product-thumb">
              <span class="thumb-letter">{{ (item.name || '?').charAt(0) }}</span>
              <el-tag class="cat-badge" effect="dark" size="small">{{ item.category || '数码电子' }}</el-tag>
              <el-tag
                v-if="item.stock <= 0"
                class="stock-badge"
                type="info"
                size="small"
                effect="dark"
              >
                已售罄
              </el-tag>
            </div>
            <div class="product-info">
              <div class="product-name" :title="item.name">{{ item.name }}</div>
              <div class="product-desc" :title="item.description">
                {{ item.description || '官方正品保证，全国联保' }}
              </div>
              <div class="price-stock-row">
                <span class="product-price">¥ {{ Number(item.price).toFixed(2) }}</span>
                <el-tag :type="item.stock > 10 ? 'success' : 'warning'" size="small" effect="light">
                  库存 {{ item.stock }}
                </el-tag>
              </div>
              <div class="card-action">
                <el-button
                  type="primary"
                  icon="ShoppingCart"
                  class="buy-btn"
                  :disabled="item.stock <= 0"
                  @click="openOrderModal(item)"
                >
                  {{ item.stock > 0 ? '立即下单' : '暂时缺货' }}
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[12, 24, 48]"
        :total="total"
        layout="prev, pager, next, jumper"
        background
        @current-change="fetchData"
        @size-change="handleSearch"
      />
    </div>

    <!-- 模拟下单弹窗 -->
    <el-dialog
      v-model="orderDialogVisible"
      title="填写收货信息并下单"
      width="520px"
      destroy-on-close
    >
      <div class="order-notice">
        <el-alert
          title="系统提示: 演示商城免除真实支付环节，提交后将通过 Redisson 分布式锁原子扣减库存，订单状态直接流转至【待发货】！"
          type="success"
          :closable="false"
          show-icon
        />
      </div>

      <el-form
        ref="orderFormRef"
        :model="orderForm"
        :rules="orderRules"
        label-width="100px"
        style="margin-top: 16px; padding-right: 20px"
      >
        <el-form-item label="选购商品:">
          <b>{{ currentProduct?.name }}</b>
        </el-form-item>
        <el-form-item label="单价:">
          <span class="price-text">¥ {{ Number(currentProduct?.price || 0).toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="购买数量:" prop="count">
          <el-input-number
            v-model="orderForm.count"
            :min="1"
            :max="currentProduct?.stock || 1"
            :step="1"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="合计应付:">
          <span class="price-text total-amount-text">
            ¥ {{ ((currentProduct?.price || 0) * orderForm.count).toFixed(2) }}
          </span>
        </el-form-item>

        <el-divider content-position="left">收货人信息</el-divider>

        <el-form-item label="收件姓名" prop="receiverName">
          <el-input v-model="orderForm.receiverName" placeholder="例如：李先生" />
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="orderForm.receiverPhone" placeholder="收件人手机号码" />
        </el-form-item>
        <el-form-item label="收货地址" prop="receiverAddress">
          <el-input
            v-model="orderForm.receiverAddress"
            type="textarea"
            :rows="2"
            placeholder="省份、城市、区县及详细门牌号"
          />
        </el-form-item>
        <el-form-item label="买家留言">
          <el-input v-model="orderForm.remark" placeholder="送前电话联系、放快递柜等备注说明" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="orderSubmitting" @click="submitOrder">
          免支付确认下单
        </el-button>
      </template>
    </el-dialog>

    <!-- “我的订单” 抽屉 -->
    <el-drawer
      v-model="myOrdersDrawerVisible"
      title="我的商城订单"
      size="620px"
      destroy-on-close
    >
      <div v-loading="ordersLoading" class="my-orders-drawer">
        <el-tabs v-model="orderTab" type="card" @tab-change="fetchMyOrders">
          <el-tab-pane label="全部订单" name="all" />
          <el-tab-pane label="待发货" name="waitShip" />
          <el-tab-pane label="已发货 (待收货)" name="shipped" />
          <el-tab-pane label="已完成" name="finished" />
        </el-tabs>

        <div v-if="myOrdersList.length === 0" class="empty-orders">
          <el-empty description="暂无相关订单记录" />
        </div>

        <div v-else class="order-cards-list">
          <el-card
            v-for="item in myOrdersList"
            :key="item.order.id"
            shadow="hover"
            class="my-order-card"
          >
            <div class="order-card-header">
              <span class="order-card-no">单号: {{ item.order.orderNo }}</span>
              <el-tag :type="getStatusTag(item.order.status)">
                {{ getStatusLabel(item.order.status) }}
              </el-tag>
            </div>

            <div class="order-items-preview">
              <div v-for="sub in item.items" :key="sub.id" class="order-item-row">
                <span class="item-name">{{ sub.productName }}</span>
                <span class="item-calc">
                  ¥{{ Number(sub.productPrice).toFixed(2) }} × {{ sub.quantity }}
                </span>
                <span class="item-subtotal">¥{{ Number(sub.subtotal).toFixed(2) }}</span>
              </div>
            </div>

            <div class="order-card-meta">
              <span>收货: {{ item.order.receiverName }} ({{ item.order.receiverPhone }})</span>
              <span class="order-total-price">
                实付: <b>¥ {{ Number(item.order.totalAmount).toFixed(2) }}</b>
              </span>
            </div>

            <!-- 操作按钮栏 (关键状态流转) -->
            <div class="order-card-actions">
              <!-- 已发货时允许查看物流 & 确认收货 -->
              <template v-if="item.order.status === 3">
                <el-button
                  size="small"
                  icon="Location"
                  @click="openTrackDialog(item.order.orderNo)"
                >
                  查看物流轨迹
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  icon="CircleCheck"
                  @click="handleConfirmReceipt(item.order)"
                >
                  确认收货
                </el-button>
              </template>

              <!-- 待发货状态提示 -->
              <template v-else-if="item.order.status === 2">
                <span class="status-tip-text">仓库正在拣选打包中，请耐心等待发货...</span>
              </template>

              <!-- 已完成状态提示 -->
              <template v-else-if="item.order.status === 4">
                <span class="status-finished-text">订单交易完成</span>
                <el-button
                  size="small"
                  link
                  type="primary"
                  icon="Location"
                  @click="openTrackDialog(item.order.orderNo)"
                >
                  流转轨迹
                </el-button>
              </template>
            </div>
          </el-card>
        </div>
      </div>
    </el-drawer>

    <!-- 物流轨迹弹窗 -->
    <el-dialog
      v-model="trackDialogVisible"
      title="订单物流全链路轨迹"
      width="560px"
      destroy-on-close
    >
      <div v-loading="trackLoading">
        <template v-if="trackData && trackData.logistics">
          <div class="track-head">
            <b>{{ trackData.logistics.companyName }}</b>
            <span>运单号: {{ trackData.logistics.trackingNo }}</span>
          </div>
          <el-timeline style="margin-top: 18px; padding-left: 10px">
            <el-timeline-item
              v-for="(node, idx) in trackData.tracks"
              :key="node.id || idx"
              :timestamp="node.trackTime"
              :type="idx === trackData.tracks.length - 1 ? 'primary' : 'success'"
              :hollow="idx !== trackData.tracks.length - 1"
              size="large"
            >
              <p class="track-node-text">{{ node.content }}</p>
            </el-timeline-item>
          </el-timeline>
        </template>
        <el-empty v-else description="暂无该订单的物流轨迹" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getProductPage } from '../api/product';
import { createMallOrder, getMyOrderPage, confirmOrderReceipt } from '../api/order';
import { getLogisticsTrackByOrderNo } from '../api/logistics';
import { useUserStore } from '../store/user';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';

const userStore = useUserStore();

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);

const categoryList = ['', '数码电子', '电脑办公', '影音娱乐', '智能家居', '生活百货'];

const queryParams = reactive({
  page: 1,
  size: 12,
  keyword: '',
  category: '',
  status: 1 // 核心：商城只展示审核通过在售的商品
});

const selectCategory = (cat) => {
  queryParams.category = cat;
  queryParams.page = 1;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getProductPage({
      page: queryParams.page,
      size: queryParams.size,
      keyword: queryParams.keyword || undefined,
      category: queryParams.category || undefined,
      status: 1 // 锁定在售状态
    });
    tableData.value = res.records || [];
    total.value = res.total || 0;
  } catch (err) {
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  queryParams.page = 1;
  fetchData();
};

const handleReset = () => {
  queryParams.keyword = '';
  queryParams.category = '';
  queryParams.page = 1;
  fetchData();
};

// 下单弹窗
const orderDialogVisible = ref(false);
const orderSubmitting = ref(false);
const orderFormRef = ref(null);
const currentProduct = ref(null);

const orderForm = reactive({
  productId: null,
  count: 1,
  receiverName: '体验客户',
  receiverPhone: '13800138000',
  receiverAddress: '广东省广州市天河区天河路208号天河城A座',
  remark: '请尽快发货'
});

const orderRules = {
  count: [{ required: true, message: '请选择购买数量', trigger: 'blur' }],
  receiverName: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  receiverAddress: [{ required: true, message: '请输入详细收货地址', trigger: 'blur' }]
};

const openOrderModal = (product) => {
  currentProduct.value = product;
  orderForm.productId = product.id;
  orderForm.count = 1;
  orderForm.receiverName = userStore.userInfo?.nickname || userStore.userInfo?.username || '测试客户';
  orderDialogVisible.value = true;
};

const submitOrder = async () => {
  if (!orderFormRef.value) return;
  await orderFormRef.value.validate(async (valid) => {
    if (!valid) return;
    orderSubmitting.value = true;
    try {
      const orderNo = await createMallOrder({
        productId: orderForm.productId,
        count: orderForm.count,
        receiverName: orderForm.receiverName,
        receiverPhone: orderForm.receiverPhone,
        receiverAddress: orderForm.receiverAddress,
        remark: orderForm.remark
      });

      ElNotification({
        title: '下单成功 (免支付直接流转为待发货)',
        message: `订单编号：${orderNo}，库存已安全扣减，已自动流转至仓储【待发货】状态！`,
        type: 'success',
        duration: 6000
      });

      orderDialogVisible.value = false;
      fetchData(); // 刷新商品库存
      openMyOrdersDrawer(); // 自动弹出我的订单抽屉查看
    } catch (err) {
    } finally {
      orderSubmitting.value = false;
    }
  });
};

// 我的订单抽屉
const myOrdersDrawerVisible = ref(false);
const ordersLoading = ref(false);
const myOrdersList = ref([]);
const orderTab = ref('all');
const waitReceiveCount = ref(0);

const getStatusLabel = (status) => {
  const map = { 1: '待支付', 2: '待发货', 3: '已发货 (待确认)', 4: '已完成', 5: '已取消' };
  return map[status] || '未知';
};

const getStatusTag = (status) => {
  const map = { 1: 'warning', 2: 'danger', 3: 'primary', 4: 'success', 5: 'info' };
  return map[status] || 'info';
};

const openMyOrdersDrawer = () => {
  myOrdersDrawerVisible.value = true;
  fetchMyOrders();
};

const fetchMyOrders = async () => {
  ordersLoading.value = true;
  let statusParam = undefined;
  if (orderTab.value === 'waitShip') statusParam = 2;
  else if (orderTab.value === 'shipped') statusParam = 3;
  else if (orderTab.value === 'finished') statusParam = 4;

  try {
    const res = await getMyOrderPage({
      page: 1,
      size: 50,
      status: statusParam
    });
    myOrdersList.value = res.records || [];

    // 计算待收货红点
    waitReceiveCount.value = myOrdersList.value.filter(item => item.order.status === 3).length;
  } catch (err) {
  } finally {
    ordersLoading.value = false;
  }
};

// 确认收货 (流转为已完成)
const handleConfirmReceipt = (order) => {
  ElMessageBox.confirm(
    `确认已收到商品并完成订单 "${order.orderNo}" 吗？确认后订单状态将流转为【已完成】，物流将自动同步为【已签收】！`,
    '确认收货提醒',
    {
      confirmButtonText: '确认已收到',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(async () => {
    try {
      await confirmOrderReceipt(order.orderNo);
      ElMessage.success('确认收货成功！订单已完结，感谢您的购买！');
      fetchMyOrders();
    } catch (err) {}
  }).catch(() => {});
};

// 轨迹弹窗
const trackDialogVisible = ref(false);
const trackLoading = ref(false);
const trackData = ref(null);

const openTrackDialog = async (orderNo) => {
  trackDialogVisible.value = true;
  trackLoading.value = true;
  trackData.value = null;
  try {
    const res = await getLogisticsTrackByOrderNo(orderNo);
    trackData.value = res;
  } catch (e) {
  } finally {
    trackLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.mall-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mall-banner {
  background: linear-gradient(135deg, #1f2d3d 0%, #2b3b4e 50%, #304156 100%);
  border-radius: 12px;
  padding: 28px 24px 20px;
  color: #fff;
}

.banner-inner {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  position: relative;
}

.banner-top-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.my-orders-btn {
  font-weight: 600;
}

.banner-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 2px;
}

.banner-sub {
  margin: 8px 0 18px;
  font-size: 14px;
  opacity: 0.85;
}

.search-bar {
  display: flex;
  justify-content: center;
}

.search-input {
  width: 100%;
  max-width: 620px;
}

.category-pills {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.category-pill {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  background-color: rgba(255, 255, 255, 0.15);
  cursor: pointer;
  transition: all 0.2s ease;
}

.category-pill:hover,
.category-pill.active {
  background-color: #409eff;
  color: #fff;
  font-weight: 600;
}

.result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
  padding: 0 4px;
}

.total-num {
  color: #409eff;
  font-size: 16px;
}

.keyword-tag {
  color: #f56c6c;
  font-weight: 600;
}

.product-card {
  margin-bottom: 18px;
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.product-thumb {
  position: relative;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f4f8 0%, #e2eaf5 100%);
}

.thumb-letter {
  font-size: 50px;
  font-weight: 800;
  color: #cbd5e1;
}

.cat-badge {
  position: absolute;
  top: 8px;
  left: 8px;
}

.stock-badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.product-info {
  padding: 14px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-desc {
  font-size: 12px;
  color: #909399;
  margin: 6px 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.price-stock-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.buy-btn {
  width: 100%;
}

.price-text {
  font-weight: 700;
  color: #f56c6c;
}

.total-amount-text {
  font-size: 18px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 8px 0 20px;
}

.order-cards-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.my-order-card {
  border-radius: 8px;
}

.order-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
  margin-bottom: 8px;
}

.order-card-no {
  font-size: 13px;
  font-family: monospace;
  font-weight: 600;
  color: #606266;
}

.order-items-preview {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 0;
}

.order-item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.item-name {
  flex: 1;
  color: #303133;
}

.item-calc {
  width: 120px;
  color: #909399;
  text-align: right;
}

.item-subtotal {
  width: 90px;
  text-align: right;
  font-weight: 600;
  color: #f56c6c;
}

.order-card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
  font-size: 12px;
  color: #606266;
}

.order-total-price {
  font-size: 14px;
  color: #303133;
}

.order-total-price b {
  color: #f56c6c;
  font-size: 16px;
}

.order-card-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.status-tip-text {
  font-size: 12px;
  color: #e6a23c;
}

.status-finished-text {
  font-size: 12px;
  color: #67c23a;
  font-weight: 600;
}

.track-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f5f7fa;
  padding: 10px 14px;
  border-radius: 6px;
  color: #409eff;
}

.track-node-text {
  margin: 0;
  font-size: 13px;
  color: #303133;
}
</style>
