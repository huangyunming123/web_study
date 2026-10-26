<template>
  <div class="product-page">
    <el-card shadow="never" class="table-card">
      <!-- 顶部搜索与操作栏 -->
      <div class="filter-container">
        <div class="filter-left">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索商品名称..."
            prefix-icon="Search"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-select
            v-model="queryParams.category"
            placeholder="商品分类"
            clearable
            style="width: 140px"
            @change="handleSearch"
          >
            <el-option label="数码电子" value="数码电子" />
            <el-option label="电脑办公" value="电脑办公" />
            <el-option label="影音娱乐" value="影音娱乐" />
            <el-option label="智能家居" value="智能家居" />
            <el-option label="生活百货" value="生活百货" />
          </el-select>
          <el-select
            v-model="queryParams.status"
            placeholder="状态"
            clearable
            style="width: 110px"
            @change="handleSearch"
          >
            <el-option label="在售" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </div>
        <div class="filter-right">
          <el-button type="success" icon="Plus" @click="openDialog('add')">发布新商品</el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 16px"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip>
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
        <el-table-column prop="price" label="销售价格 (元)" width="140" align="right">
          <template #default="{ row }">
            <span class="price-text">¥ {{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.stock > 10 ? 'success' : row.stock > 0 ? 'warning' : 'danger'">
              {{ row.stock > 0 ? row.stock : '已售罄' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="上架状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-text=""
              inactive-text=""
              @change="(val) => handleToggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" align="center" />
        <el-table-column label="操作" width="230" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="warning"
              icon="ShoppingCart"
              :disabled="row.stock <= 0 || row.status === 0"
              @click="openOrderDialog(row)"
            >
              模拟下单
            </el-button>
            <el-button link type="primary" icon="Edit" @click="openDialog('edit', row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增 / 编辑商品弹窗 (商品发布) -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '发布新商品' : '编辑商品信息'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="padding-right: 20px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品完整名称" />
        </el-form-item>
        <el-form-item label="所属分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择商品分类" style="width: 100%">
            <el-option label="数码电子" value="数码电子" />
            <el-option label="电脑办公" value="电脑办公" />
            <el-option label="影音娱乐" value="影音娱乐" />
            <el-option label="智能家居" value="智能家居" />
            <el-option label="生活百货" value="生活百货" />
          </el-select>
        </el-form-item>
        <el-form-item label="销售价格" prop="price">
          <el-input-number
            v-model="form.price"
            :precision="2"
            :step="10"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="初始库存" prop="stock">
          <el-input-number
            v-model="form.stock"
            :min="0"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="上架状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">立即上架在售</el-radio>
            <el-radio :label="0">暂存仓库下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="规格说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="填写商品的核心卖点、材质或技术规格说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          确认发布保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 模拟下单 (Redisson 分布式锁演示) 弹窗 -->
    <el-dialog
      v-model="orderDialogVisible"
      title="模拟高并发秒杀下单 (Redisson 分布式锁防超卖)"
      width="480px"
      destroy-on-close
    >
      <div class="order-tips">
        <el-alert
          title="底层原理: 请求通过 Redisson 获取当前商品的分布式互斥锁，加锁成功后原子扣减数据库库存并同步淘汰 Redis 缓存，杜绝超卖！"
          type="info"
          :closable="false"
          show-icon
        />
      </div>

      <el-form label-width="110px" style="margin-top: 15px; padding-right: 20px">
        <el-form-item label="商品名称:">
          <span class="detail-val-highlight">{{ orderForm.name }}</span>
        </el-form-item>
        <el-form-item label="商品单价:">
          <span class="price-text">¥ {{ Number(orderForm.price).toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="当前库存:">
          <el-tag :type="orderForm.stock > 0 ? 'success' : 'danger'">
            {{ orderForm.stock }} 件
          </el-tag>
        </el-form-item>
        <el-form-item label="购买数量:">
          <el-input-number
            v-model="orderForm.count"
            :min="1"
            :max="orderForm.stock > 0 ? orderForm.stock : 1"
            :step="1"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="预计应付:">
          <span class="price-text total-price">
            ¥ {{ (orderForm.price * orderForm.count).toFixed(2) }}
          </span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button
          type="warning"
          icon="Check"
          :loading="orderLoading"
          @click="submitOrder"
        >
          立即抢购下单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import {
  getProductPage,
  getProductDetail,
  addProduct,
  updateProduct,
  updateProductStatus,
  createOrder,
  deleteProduct
} from '../api/product';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  category: '',
  status: undefined
});

// 新增/编辑弹窗状态
const dialogVisible = ref(false);
const dialogType = ref('add');
const submitLoading = ref(false);
const formRef = ref(null);

const form = reactive({
  id: null,
  name: '',
  category: '数码电子',
  price: 0.00,
  stock: 100,
  status: 1,
  description: ''
});

const rules = {
  name: [{ required: true, message: '商品名称不能为空', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '销售价格不能为空', trigger: 'blur' }],
  stock: [{ required: true, message: '库存数量不能为空', trigger: 'blur' }]
};

// 模拟下单弹窗状态
const orderDialogVisible = ref(false);
const orderLoading = ref(false);
const orderForm = reactive({
  productId: null,
  name: '',
  price: 0.00,
  stock: 0,
  count: 1
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getProductPage({
      page: queryParams.page,
      size: queryParams.size,
      keyword: queryParams.keyword || undefined,
      category: queryParams.category || undefined,
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
  queryParams.keyword = '';
  queryParams.category = '';
  queryParams.status = undefined;
  queryParams.page = 1;
  fetchData();
};

const handleToggleStatus = async (row, val) => {
  const targetStatus = val ? 1 : 0;
  try {
    await updateProductStatus(row.id, targetStatus);
    row.status = targetStatus;
    ElMessage.success(`商品已成功${targetStatus === 1 ? '上架' : '下架'}，缓存已同步淘汰`);
  } catch (err) {
  }
};

const openDialog = async (type, row = null) => {
  dialogType.value = type;
  if (type === 'edit' && row) {
    try {
      const detail = await getProductDetail(row.id);
      form.id = detail.id;
      form.name = detail.name;
      form.category = detail.category || '数码电子';
      form.price = Number(detail.price);
      form.stock = detail.stock;
      form.status = detail.status ?? 1;
      form.description = detail.description || '';
    } catch (e) {
      form.id = row.id;
      form.name = row.name;
      form.category = row.category || '数码电子';
      form.price = Number(row.price);
      form.stock = row.stock;
      form.status = row.status ?? 1;
      form.description = row.description || '';
    }
  } else {
    form.id = null;
    form.name = '';
    form.category = '数码电子';
    form.price = 0.00;
    form.stock = 100;
    form.status = 1;
    form.description = '';
  }
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      if (dialogType.value === 'add') {
        await addProduct({
          name: form.name,
          category: form.category,
          price: form.price,
          stock: form.stock,
          status: form.status,
          description: form.description
        });
        ElMessage.success('发布新商品成功！');
      } else {
        await updateProduct(form.id, {
          name: form.name,
          category: form.category,
          price: form.price,
          stock: form.stock,
          status: form.status,
          description: form.description
        });
        ElMessage.success('更新商品成功，Redis 缓存已自动同步淘汰');
      }
      dialogVisible.value = false;
      fetchData();
    } catch (err) {
    } finally {
      submitLoading.value = false;
    }
  });
};

const openOrderDialog = (row) => {
  orderForm.productId = row.id;
  orderForm.name = row.name;
  orderForm.price = Number(row.price);
  orderForm.stock = row.stock;
  orderForm.count = 1;
  orderDialogVisible.value = true;
};

const submitOrder = async () => {
  orderLoading.value = true;
  try {
    const res = await createOrder({
      productId: orderForm.productId,
      count: orderForm.count
    });

    ElNotification({
      title: '下单成功 (分布式锁扣减完成)',
      message: `订单号: ${res.orderNo}，成功购买 ${res.count} 件，商品最新剩余库存: ${res.surplusStock} 件`,
      type: 'success',
      duration: 5000
    });

    orderDialogVisible.value = false;
    fetchData();
  } catch (err) {
  } finally {
    orderLoading.value = false;
  }
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要物理删除商品 "${row.name}" 吗？该操作将同步清理 Redis 缓存！`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      await deleteProduct(row.id);
      ElMessage.success('删除成功，Redis 缓存已同步清理');
      fetchData();
    } catch (err) {}
  }).catch(() => {});
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.product-page {
  display: flex;
  flex-direction: column;
}

.table-card {
  border-radius: 8px;
}

.filter-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
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
  font-weight: 600;
  color: #f56c6c;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.total-price {
  font-size: 18px;
}

.detail-val-highlight {
  font-weight: 600;
  color: #303133;
}

.order-tips {
  margin-bottom: 8px;
}
</style>
