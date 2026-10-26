<template>
  <div class="order-page">
    <el-card shadow="never" class="table-card">
      <!-- 搜索过滤栏 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="filter-form">
          <el-form-item label="订单编号">
            <el-input
              v-model="queryParams.orderNo"
              placeholder="输入订单编号"
              clearable
              style="width: 180px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="客户姓名">
            <el-input
              v-model="queryParams.customerName"
              placeholder="客户姓名"
              clearable
              style="width: 140px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="客户手机">
            <el-input
              v-model="queryParams.customerPhone"
              placeholder="联系电话"
              clearable
              style="width: 140px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select
              v-model="queryParams.status"
              placeholder="全部状态"
              clearable
              style="width: 130px"
              @change="handleSearch"
            >
              <el-option label="待支付" :value="1" />
              <el-option label="待发货" :value="2" />
              <el-option label="已发货" :value="3" />
              <el-option label="已完成" :value="4" />
              <el-option label="已取消" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
            <el-button icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 订单表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 10px"
      >
        <el-table-column prop="orderNo" label="订单编号" min-width="190" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="order-no-text">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户信息" min-width="160">
          <template #default="{ row }">
            <div>{{ row.customerName }}</div>
            <div class="sub-text">{{ row.customerPhone }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="130" align="right">
          <template #default="{ row }">
            <span class="price-text">¥ {{ Number(row.totalAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div>{{ row.receiverName }} ({{ row.receiverPhone }})</div>
            <div class="sub-text">{{ row.receiverAddress }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Document" @click="handleOpenDetail(row)">
              详情
            </el-button>
            <!-- 待发货订单提供快捷发货按钮 -->
            <el-button
              v-if="row.status === 2"
              link
              type="warning"
              icon="Van"
              @click="handleOpenShip(row)"
            >
              发货
            </el-button>
            <!-- 已发货或已完成展示物流跟踪 -->
            <el-button
              v-if="row.status >= 3"
              link
              type="success"
              icon="Location"
              @click="handleOpenTrack(row)"
            >
              轨迹
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      title="订单综合详情与明细"
      size="620px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-drawer-content">
        <template v-if="orderDetail.order">
          <!-- 状态提示 -->
          <el-alert
            :title="`当前订单状态: ${getStatusLabel(orderDetail.order.status)}`"
            :type="getStatusTag(orderDetail.order.status)"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          />

          <!-- 基础信息 -->
          <el-descriptions title="基础信息" :column="2" border size="small">
            <el-descriptions-item label="订单编号">
              <b>{{ orderDetail.order.orderNo }}</b>
            </el-descriptions-item>
            <el-descriptions-item label="下单客户">
              {{ orderDetail.order.customerName }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ orderDetail.order.customerPhone }}
            </el-descriptions-item>
            <el-descriptions-item label="总金额">
              <span class="price-text">¥ {{ Number(orderDetail.order.totalAmount).toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">
              {{ orderDetail.order.createTime }}
            </el-descriptions-item>
            <el-descriptions-item label="支付时间">
              {{ orderDetail.order.payTime || '未支付' }}
            </el-descriptions-item>
            <el-descriptions-item label="发货时间">
              {{ orderDetail.order.shipTime || '未发货' }}
            </el-descriptions-item>
            <el-descriptions-item label="客户备注">
              {{ orderDetail.order.remark || '无' }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 收货信息 -->
          <el-descriptions title="收货地址" :column="1" border size="small" style="margin-top: 16px">
            <el-descriptions-item label="收件人">
              {{ orderDetail.order.receiverName }} ({{ orderDetail.order.receiverPhone }})
            </el-descriptions-item>
            <el-descriptions-item label="详细地址">
              {{ orderDetail.order.receiverAddress }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 商品明细清单 -->
          <div class="detail-section-title">购买商品明细清单</div>
          <el-table :data="orderDetail.items || []" border stripe size="small" style="width: 100%">
            <el-table-column prop="productName" label="商品名称" min-width="180" />
            <el-table-column prop="productPrice" label="单价" width="100" align="right">
              <template #default="{ row }">
                ¥ {{ Number(row.productPrice).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="subtotal" label="小计" width="100" align="right">
              <template #default="{ row }">
                <span class="price-text">¥ {{ Number(row.subtotal).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 物流信息 -->
          <div v-if="orderDetail.logistics" class="detail-section-title">承运物流信息</div>
          <el-descriptions v-if="orderDetail.logistics" :column="2" border size="small">
            <el-descriptions-item label="物流公司">
              {{ orderDetail.logistics.companyName }}
            </el-descriptions-item>
            <el-descriptions-item label="快递单号">
              <b>{{ orderDetail.logistics.trackingNo }}</b>
            </el-descriptions-item>
            <el-descriptions-item label="发货人员">
              {{ orderDetail.logistics.operatorName }}
            </el-descriptions-item>
            <el-descriptions-item label="发货时间">
              {{ orderDetail.logistics.shipTime }}
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="orderDetail.order.status === 2" style="margin-top: 20px; text-align: right">
            <el-button type="warning" icon="Van" @click="handleOpenShip(orderDetail.order)">
              立即为此订单发货
            </el-button>
          </div>
        </template>
      </div>
    </el-drawer>

    <!-- 订单快捷发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="订单物流发货出库"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="shipFormRef"
        :model="shipForm"
        :rules="shipRules"
        label-width="110px"
        style="padding-right: 20px"
      >
        <el-form-item label="订单编号">
          <el-input v-model="shipForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="收件客户">
          <span>{{ currentShipOrder?.receiverName }} ({{ currentShipOrder?.receiverPhone }})</span>
        </el-form-item>
        <el-form-item label="收货地址">
          <span class="sub-text">{{ currentShipOrder?.receiverAddress }}</span>
        </el-form-item>
        <el-form-item label="快递公司" prop="companyName">
          <el-select
            v-model="shipForm.companyName"
            placeholder="请选择承运物流公司"
            style="width: 100%"
            @change="handleCompanyChange"
          >
            <el-option
              v-for="item in companyList"
              :key="item.code"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入运单单号">
            <template #append>
              <el-button @click="generateTrackingNo">自动生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="发货人员" prop="operatorName">
          <el-input v-model="shipForm.operatorName" placeholder="经办人姓名" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="submitShip">
          确认发货出库
        </el-button>
      </template>
    </el-dialog>

    <!-- 物流轨迹时间轴弹窗 -->
    <el-dialog
      v-model="trackDialogVisible"
      title="物流流转全链路轨迹"
      width="580px"
      destroy-on-close
    >
      <div v-loading="trackLoading" class="track-dialog-content">
        <template v-if="trackData && trackData.logistics">
          <div class="logistics-header-info">
            <div class="comp-name">
              <el-icon><Van /></el-icon>
              {{ trackData.logistics.companyName }}
            </div>
            <div class="track-no">运单号: <b>{{ trackData.logistics.trackingNo }}</b></div>
          </div>

          <el-timeline style="margin-top: 20px; padding-left: 10px">
            <el-timeline-item
              v-for="(track, index) in trackData.tracks"
              :key="track.id || index"
              :timestamp="track.trackTime"
              :type="index === trackData.tracks.length - 1 ? 'primary' : 'success'"
              :hollow="index !== trackData.tracks.length - 1"
              size="large"
            >
              <div class="track-item-content">
                <p class="track-text">{{ track.content }}</p>
                <div class="track-meta">
                  <span v-if="track.location">📍 {{ track.location }}</span>
                  <span v-if="track.operator">👤 {{ track.operator }}</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </template>
        <el-empty v-else description="暂未查询到该订单的物流轨迹信息" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getOrderPage, getOrderDetail } from '../api/order';
import { shipOrder, getLogisticsTrackByOrderNo, getLogisticsCompanies } from '../api/logistics';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);

const queryParams = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  customerName: '',
  customerPhone: '',
  status: undefined
});

const getStatusLabel = (status) => {
  const map = { 1: '待支付', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' };
  return map[status] || '未知';
};

const getStatusTag = (status) => {
  const map = { 1: 'warning', 2: 'danger', 3: 'primary', 4: 'success', 5: 'info' };
  return map[status] || 'info';
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getOrderPage({
      page: queryParams.page,
      size: queryParams.size,
      orderNo: queryParams.orderNo || undefined,
      customerName: queryParams.customerName || undefined,
      customerPhone: queryParams.customerPhone || undefined,
      status: queryParams.status !== '' ? queryParams.status : undefined
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
  queryParams.orderNo = '';
  queryParams.customerName = '';
  queryParams.customerPhone = '';
  queryParams.status = undefined;
  queryParams.page = 1;
  fetchData();
};

// 详情抽屉
const detailVisible = ref(false);
const detailLoading = ref(false);
const orderDetail = ref({});

const handleOpenDetail = async (row) => {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getOrderDetail(row.orderNo);
    orderDetail.value = res || {};
  } catch (err) {
  } finally {
    detailLoading.value = false;
  }
};

// 快捷发货
const shipDialogVisible = ref(false);
const shipLoading = ref(false);
const shipFormRef = ref(null);
const currentShipOrder = ref(null);
const companyList = ref([]);

const shipForm = reactive({
  orderNo: '',
  companyName: '顺丰速运',
  companyCode: 'SF',
  trackingNo: '',
  operatorName: '仓储物流专员'
});

const shipRules = {
  companyName: [{ required: true, message: '请选择快递承运商', trigger: 'change' }],
  trackingNo: [{ required: true, message: '运单号不能为空', trigger: 'blur' }]
};

const handleOpenShip = async (row) => {
  currentShipOrder.value = row;
  shipForm.orderNo = row.orderNo;
  shipForm.companyName = '顺丰速运';
  shipForm.companyCode = 'SF';
  generateTrackingNo();
  shipDialogVisible.value = true;

  if (companyList.value.length === 0) {
    try {
      const companies = await getLogisticsCompanies();
      companyList.value = companies || [];
    } catch (e) {}
  }
};

const handleCompanyChange = (val) => {
  const match = companyList.value.find(c => c.name === val);
  shipForm.companyCode = match ? match.code : 'COMMON';
  generateTrackingNo();
};

const generateTrackingNo = () => {
  const code = shipForm.companyCode || 'EX';
  const timeStr = new Date().toISOString().replace(/[-:T.Z]/g, '').slice(0, 14);
  const rand = Math.floor(1000 + Math.random() * 9000);
  shipForm.trackingNo = `${code}${timeStr}${rand}`;
};

const submitShip = async () => {
  if (!shipFormRef.value) return;
  await shipFormRef.value.validate(async (valid) => {
    if (!valid) return;
    shipLoading.value = true;
    try {
      await shipOrder({
        orderNo: shipForm.orderNo,
        companyName: shipForm.companyName,
        companyCode: shipForm.companyCode,
        trackingNo: shipForm.trackingNo,
        operatorName: shipForm.operatorName
      });
      ElMessage.success('发货成功！订单状态已变更为已发货，物流运单已生成');
      shipDialogVisible.value = false;
      detailVisible.value = false;
      fetchData();
    } catch (err) {
    } finally {
      shipLoading.value = false;
    }
  });
};

// 轨迹弹窗
const trackDialogVisible = ref(false);
const trackLoading = ref(false);
const trackData = ref(null);

const handleOpenTrack = async (row) => {
  trackDialogVisible.value = true;
  trackLoading.value = true;
  trackData.value = null;
  try {
    const res = await getLogisticsTrackByOrderNo(row.orderNo);
    trackData.value = res;
  } catch (err) {
  } finally {
    trackLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.order-page {
  display: flex;
  flex-direction: column;
}

.table-card {
  border-radius: 8px;
}

.filter-form {
  margin-bottom: -10px;
}

.order-no-text {
  font-family: monospace;
  font-weight: 600;
  color: #409eff;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

.price-text {
  font-weight: 700;
  color: #f56c6c;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 18px 0 10px;
}

.logistics-header-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.comp-name {
  font-size: 16px;
  font-weight: 700;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 6px;
}

.track-no {
  font-size: 13px;
  color: #606266;
}

.track-item-content {
  padding: 2px 0;
}

.track-text {
  margin: 0;
  font-size: 13px;
  color: #303133;
  line-height: 1.5;
}

.track-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
