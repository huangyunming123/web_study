<template>
  <div class="logistics-page">
    <el-tabs v-model="activeTab" type="border-card" class="logistics-tabs">
      <!-- Tab 1: 待发货出库工作台 -->
      <el-tab-pane label="待发货订单出库" name="waitShip">
        <div class="tab-header">
          <div class="tab-title">
            <el-icon><Van /></el-icon>
            待发货订单处理
          </div>
          <el-button icon="Refresh" @click="fetchWaitShipOrders">刷新待发列表</el-button>
        </div>

        <el-table
          v-loading="waitLoading"
          :data="waitShipList"
          border
          stripe
          style="width: 100%; margin-top: 14px"
        >
          <el-table-column prop="orderNo" label="订单编号" min-width="190" />
          <el-table-column prop="customerName" label="客户姓名" width="120" />
          <el-table-column prop="customerPhone" label="客户电话" width="130" />
          <el-table-column prop="totalAmount" label="订单金额" width="120" align="right">
            <template #default="{ row }">
              <span class="price-text">¥ {{ Number(row.totalAmount).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="收货人与收货地址" min-width="240" show-overflow-tooltip>
            <template #default="{ row }">
              <div><b>{{ row.receiverName }}</b> ({{ row.receiverPhone }})</div>
              <div class="sub-text">{{ row.receiverAddress }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="170" align="center" />
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="warning" size="small" icon="Van" @click="openShipModal(row)">
                发货出库
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!waitLoading && waitShipList.length === 0" description="暂无待发货订单，所有订单已出库！" />
      </el-tab-pane>

      <!-- Tab 2: 物流轨迹流转查询 -->
      <el-tab-pane label="物流运单轨迹追踪" name="track">
        <div class="track-search-box">
          <el-input
            v-model="searchQuery"
            placeholder="输入订单编号 (如: ORD20261025...) 或 快递单号 (如: SF1688...)"
            clearable
            size="large"
            style="max-width: 520px"
            @keyup.enter="handleSearchTrack"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" :loading="trackLoading" @click="handleSearchTrack">
                查询流转轨迹
              </el-button>
            </template>
          </el-input>
          <div class="quick-samples">
            <span>演示快捷单号：</span>
            <el-tag
              class="sample-tag"
              @click="searchQuery = 'ORD202610250002B02'; handleSearchTrack()"
            >
              ORD202610250002B02 (顺丰)
            </el-tag>
            <el-tag
              class="sample-tag"
              type="success"
              @click="searchQuery = 'JD0099882026102302'; handleSearchTrack()"
            >
              JD0099882026102302 (京东)
            </el-tag>
          </div>
        </div>

        <div v-loading="trackLoading" class="track-result-area">
          <template v-if="trackResult && trackResult.logistics">
            <el-card shadow="never" class="track-header-card">
              <el-descriptions title="运单概览" :column="4" border size="small">
                <el-descriptions-item label="承运物流">
                  <span class="comp-highlight">{{ trackResult.logistics.companyName }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="快递单号">
                  <b>{{ trackResult.logistics.trackingNo }}</b>
                </el-descriptions-item>
                <el-descriptions-item label="关联订单">
                  {{ trackResult.logistics.orderNo }}
                </el-descriptions-item>
                <el-descriptions-item label="发货人员">
                  {{ trackResult.logistics.operatorName }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>

            <el-card shadow="never" class="timeline-card" style="margin-top: 16px">
              <div class="timeline-header">
                <el-icon><Location /></el-icon> 全链路流转时间轴
              </div>
              <el-timeline style="margin-top: 20px; padding-left: 10px">
                <el-timeline-item
                  v-for="(node, idx) in trackResult.tracks"
                  :key="node.id || idx"
                  :timestamp="node.trackTime"
                  :type="idx === trackResult.tracks.length - 1 ? 'primary' : 'success'"
                  :hollow="idx !== trackResult.tracks.length - 1"
                  size="large"
                >
                  <div class="timeline-content">
                    <p class="node-content">{{ node.content }}</p>
                    <div class="node-meta">
                      <span v-if="node.location">网点: {{ node.location }}</span>
                      <span v-if="node.operator">经办: {{ node.operator }}</span>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </el-card>
          </template>
          <el-empty
            v-else-if="!trackLoading && hasSearched"
            description="未查询到相关的物流轨迹信息，请核对订单号或单号"
          />
        </div>
      </el-tab-pane>

      <!-- Tab 3: 全部已发货记录 -->
      <el-tab-pane label="已发货订单全览" name="shippedList">
        <el-table
          v-loading="shippedLoading"
          :data="shippedList"
          border
          stripe
          style="width: 100%"
        >
          <el-table-column prop="orderNo" label="订单编号" min-width="190" />
          <el-table-column prop="customerName" label="客户姓名" width="120" />
          <el-table-column prop="totalAmount" label="金额" width="120" align="right">
            <template #default="{ row }">
              <span class="price-text">¥ {{ Number(row.totalAmount).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="订单状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 4 ? 'success' : 'primary'">
                {{ row.status === 4 ? '已完成' : '已发货' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="shipTime" label="发货时间" width="170" align="center" />
          <el-table-column label="操作" width="130" align="center">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                icon="Location"
                @click="viewRowTrack(row.orderNo)"
              >
                查看轨迹
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 发货弹窗 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="物流配货发货出库"
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
          <span>{{ currentOrder?.receiverName }} ({{ currentOrder?.receiverPhone }})</span>
        </el-form-item>
        <el-form-item label="收货地址">
          <span class="sub-text">{{ currentOrder?.receiverAddress }}</span>
        </el-form-item>
        <el-form-item label="承运公司" prop="companyName">
          <el-select
            v-model="shipForm.companyName"
            placeholder="请选择物流承运商"
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
        <el-form-item label="经办人员" prop="operatorName">
          <el-input v-model="shipForm.operatorName" placeholder="发货操作人" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipSubmitting" @click="submitShip">
          确认发货出库
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getOrderPage } from '../api/order';
import {
  shipOrder,
  getLogisticsTrackByOrderNo,
  getLogisticsTrackByTrackingNo,
  getLogisticsCompanies
} from '../api/logistics';
import { ElMessage } from 'element-plus';

const activeTab = ref('waitShip');

// Tab 1: 待发货
const waitLoading = ref(false);
const waitShipList = ref([]);

const fetchWaitShipOrders = async () => {
  waitLoading.value = true;
  try {
    const res = await getOrderPage({ page: 1, size: 50, status: 2 });
    waitShipList.value = res.records || [];
  } catch (err) {
  } finally {
    waitLoading.value = false;
  }
};

// Tab 2: 轨迹查询
const searchQuery = ref('ORD202610250002B02');
const trackLoading = ref(false);
const hasSearched = ref(false);
const trackResult = ref(null);

const handleSearchTrack = async () => {
  if (!searchQuery.value) {
    ElMessage.warning('请输入订单流水号或快递单号');
    return;
  }
  trackLoading.value = true;
  hasSearched.value = true;
  trackResult.value = null;

  const query = searchQuery.value.trim();
  try {
    let res = null;
    if (query.toUpperCase().startsWith('ORD')) {
      res = await getLogisticsTrackByOrderNo(query);
    } else {
      res = await getLogisticsTrackByTrackingNo(query);
    }
    trackResult.value = res;
  } catch (err) {
  } finally {
    trackLoading.value = false;
  }
};

// Tab 3: 已发货列表
const shippedLoading = ref(false);
const shippedList = ref([]);

const fetchShippedOrders = async () => {
  shippedLoading.value = true;
  try {
    const res = await getOrderPage({ page: 1, size: 50 });
    shippedList.value = (res.records || []).filter(item => item.status >= 3);
  } catch (err) {
  } finally {
    shippedLoading.value = false;
  }
};

const viewRowTrack = (orderNo) => {
  searchQuery.value = orderNo;
  activeTab.value = 'track';
  handleSearchTrack();
};

// 发货弹窗
const shipDialogVisible = ref(false);
const shipSubmitting = ref(false);
const shipFormRef = ref(null);
const currentOrder = ref(null);
const companyList = ref([]);

const shipForm = reactive({
  orderNo: '',
  companyName: '顺丰速运',
  companyCode: 'SF',
  trackingNo: '',
  operatorName: '仓储物流专员'
});

const shipRules = {
  companyName: [{ required: true, message: '请选择物流承运商', trigger: 'change' }],
  trackingNo: [{ required: true, message: '运单号不能为空', trigger: 'blur' }]
};

const openShipModal = async (row) => {
  currentOrder.value = row;
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
    shipSubmitting.value = true;
    try {
      await shipOrder({
        orderNo: shipForm.orderNo,
        companyName: shipForm.companyName,
        companyCode: shipForm.companyCode,
        trackingNo: shipForm.trackingNo,
        operatorName: shipForm.operatorName
      });
      ElMessage.success('发货成功！包裹已出库并交由快递承运');
      shipDialogVisible.value = false;
      fetchWaitShipOrders();
      fetchShippedOrders();
    } catch (err) {
    } finally {
      shipSubmitting.value = false;
    }
  });
};

onMounted(() => {
  fetchWaitShipOrders();
  fetchShippedOrders();
  handleSearchTrack();
});
</script>

<style scoped>
.logistics-page {
  display: flex;
  flex-direction: column;
}

.logistics-tabs {
  border-radius: 8px;
}

.tab-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tab-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

.price-text {
  font-weight: 700;
  color: #f56c6c;
}

.track-search-box {
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.quick-samples {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sample-tag {
  cursor: pointer;
}

.track-result-area {
  max-width: 820px;
  margin: 0 auto;
  width: 100%;
}

.comp-highlight {
  font-weight: 700;
  color: #409eff;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.timeline-content {
  padding: 2px 0;
}

.node-content {
  margin: 0;
  font-size: 13px;
  color: #303133;
  line-height: 1.5;
}

.node-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
