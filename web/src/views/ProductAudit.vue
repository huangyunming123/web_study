<template>
  <div class="product-audit-page">
    <el-card shadow="never" class="table-card">
      <div class="header-banner">
        <div class="banner-title">
          <el-icon><DocumentChecked /></el-icon>
          商品发布审核中心
        </div>
        <p class="banner-sub">
          商户或运营发布的全新商品必须经由此处审核，审核通过后方可在商城前台对用户展示并允许下单。
        </p>
      </div>

      <!-- 审核状态过滤标签页 -->
      <el-tabs v-model="activeTab" type="card" style="margin-top: 16px" @tab-change="handleTabChange">
        <el-tab-pane label="待审核商品" name="pending" />
        <el-tab-pane label="审核通过 (在售)" name="approved" />
        <el-tab-pane label="审核驳回" name="rejected" />
        <el-tab-pane label="全部商品" name="all" />
      </el-tabs>

      <!-- 搜索过滤 -->
      <div class="filter-row">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索待审商品名称..."
          prefix-icon="Search"
          clearable
          style="width: 240px"
          @keyup.enter="fetchData"
          @clear="fetchData"
        />
        <el-select
          v-model="queryParams.category"
          placeholder="分类筛选"
          clearable
          style="width: 140px"
          @change="fetchData"
        >
          <el-option label="数码电子" value="数码电子" />
          <el-option label="电脑办公" value="电脑办公" />
          <el-option label="影音娱乐" value="影音娱乐" />
          <el-option label="智能家居" value="智能家居" />
          <el-option label="生活百货" value="生活百货" />
        </el-select>
        <el-button type="primary" icon="Search" @click="fetchData">查询</el-button>
        <el-button icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <!-- 商品列表表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 14px"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="商品名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="product-name-text">{{ row.name }}</span>
            <div v-if="row.description" class="desc-text">{{ row.description }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.category || '数码电子' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="销售价格" width="130" align="right">
          <template #default="{ row }">
            <span class="price-text">¥ {{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="提交库存" width="110" align="center" />
        <el-table-column prop="status" label="审核状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="驳回原因 / 备注" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.status === 2" class="reject-reason-text">
              {{ row.rejectReason || '未填写具体原因' }}
            </span>
            <span v-else class="sub-text">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" align="center" />
        <el-table-column label="审核操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button
                type="success"
                size="small"
                icon="Check"
                @click="handleApprove(row)"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                icon="Close"
                @click="openRejectDialog(row)"
              >
                驳回
              </el-button>
            </template>
            <template v-else-if="row.status === 2">
              <el-button
                type="primary"
                size="small"
                icon="RefreshRight"
                @click="handleApprove(row)"
              >
                重新通过
              </el-button>
            </template>
            <template v-else>
              <el-tag type="success" size="small">已审核在售</el-tag>
            </template>
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

    <!-- 驳回理由输入弹窗 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="商品审核驳回处理"
      width="460px"
      destroy-on-close
    >
      <div class="reject-dialog-content">
        <p>正在对商品【<b>{{ currentProduct?.name }}</b>】进行驳回操作，请详细填写驳回理由：</p>
        <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="4"
          placeholder="例如：商品资质材料不全、价格配置异常、图文描述含有侵权违规词汇等..."
        />
      </div>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitLoading" @click="submitReject">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getProductPage, auditProduct } from '../api/product';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const activeTab = ref('pending');

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  category: '',
  status: 0 // 默认只看待审核
});

const getStatusLabel = (status) => {
  const map = { 0: '待审核', 1: '在售 (已过审)', 2: '已驳回', 3: '已下架' };
  return map[status] || '未知';
};

const getStatusTag = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' };
  return map[status] || 'info';
};

const handleTabChange = (tabName) => {
  if (tabName === 'pending') {
    queryParams.status = 0;
  } else if (tabName === 'approved') {
    queryParams.status = 1;
  } else if (tabName === 'rejected') {
    queryParams.status = 2;
  } else {
    queryParams.status = undefined;
  }
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
      status: queryParams.status !== undefined ? queryParams.status : undefined
    });
    tableData.value = res.records || [];
    total.value = res.total || 0;
  } catch (err) {
  } finally {
    loading.value = false;
  }
};

const handleReset = () => {
  queryParams.keyword = '';
  queryParams.category = '';
  queryParams.page = 1;
  fetchData();
};

const handleApprove = (row) => {
  ElMessageBox.confirm(
    `确认审核通过商品 "${row.name}" 吗？通过后该商品将自动同步至商城前台展示并开放下单！`,
    '审核通过确认',
    {
      confirmButtonText: '审核通过',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(async () => {
    try {
      await auditProduct(row.id, 1, '');
      ElMessage.success(`商品 [${row.name}] 审核通过，已正式上架！`);
      fetchData();
    } catch (err) {}
  }).catch(() => {});
};

// 驳回弹窗
const rejectDialogVisible = ref(false);
const submitLoading = ref(false);
const currentProduct = ref(null);
const rejectReason = ref('');

const openRejectDialog = (row) => {
  currentProduct.value = row;
  rejectReason.value = '';
  rejectDialogVisible.value = true;
};

const submitReject = async () => {
  if (!rejectReason.value || !rejectReason.value.trim()) {
    ElMessage.warning('请填写具体的审核驳回原因');
    return;
  }
  submitLoading.value = true;
  try {
    await auditProduct(currentProduct.value.id, 2, rejectReason.value.trim());
    ElMessage.warning(`商品 [${currentProduct.value.name}] 审核已驳回`);
    rejectDialogVisible.value = false;
    fetchData();
  } catch (err) {
  } finally {
    submitLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.product-audit-page {
  display: flex;
  flex-direction: column;
}

.table-card {
  border-radius: 8px;
}

.header-banner {
  padding: 14px 18px;
  background-color: #fdf6ec;
  border-left: 4px solid #e6a23c;
  border-radius: 4px;
}

.banner-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #e6a23c;
}

.banner-sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: #606266;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
}

.product-name-text {
  font-weight: 600;
  color: #303133;
}

.desc-text {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.price-text {
  font-weight: 700;
  color: #f56c6c;
}

.reject-reason-text {
  color: #f56c6c;
  font-size: 12px;
}

.sub-text {
  color: #c0c4cc;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.reject-dialog-content p {
  margin: 0 0 12px;
  font-size: 14px;
  color: #606266;
}
</style>
