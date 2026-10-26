<template>
  <div class="user-page">
    <el-card shadow="never" class="table-card">
      <!-- 搜索栏 -->
      <div class="filter-container">
        <div class="filter-left">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索用户名、昵称或手机号..."
            prefix-icon="Search"
            clearable
            style="width: 280px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 用户列表表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%; margin-top: 16px"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="登录用户名" width="160">
          <template #default="{ row }">
            <span class="username-text">{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="用户姓名/昵称" width="160" />
        <el-table-column prop="phone" label="联系电话" width="140" align="center">
          <template #default="{ row }">
            <span>{{ row.phone || '未绑定' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属角色" min-width="220">
          <template #default="{ row }">
            <div class="role-tags">
              <el-tag
                v-for="role in (row.roles || [])"
                :key="role.id"
                size="small"
                :type="getRoleTagType(role.roleCode)"
              >
                {{ role.roleName }}
              </el-tag>
              <span v-if="!row.roles || row.roles.length === 0" class="sub-text">未分配角色</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-text=""
              inactive-text=""
              @change="(val) => handleToggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="warning" icon="Key" @click="openRoleDialog(row)">
              分配角色
            </el-button>
            <el-button link type="primary" icon="Edit" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">
              删除
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

    <!-- 分配角色弹窗 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="配置用户所属角色"
      width="480px"
      destroy-on-close
    >
      <div class="dialog-user-info">
        当前配置用户：<b>{{ currentUser?.nickname || currentUser?.username }}</b> ({{ currentUser?.username }})
      </div>

      <el-form label-width="90px" style="margin-top: 16px">
        <el-form-item label="勾选角色:">
          <el-checkbox-group v-model="selectedRoleIds" class="role-checkbox-group">
            <el-checkbox
              v-for="role in allRoleList"
              :key="role.id"
              :label="role.id"
              border
              class="role-checkbox"
            >
              {{ role.roleName }} ({{ role.roleCode }})
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitting" @click="submitRoleAssign">
          保存角色分配
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑昵称弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户信息"
      width="420px"
      destroy-on-close
    >
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="用户名">
          <el-input :value="currentUser?.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.nickname" placeholder="请输入姓名或昵称" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="submitEdit">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getUserPage, updateUser, updateUserStatus, assignUserRoles, deleteUser } from '../api/user';
import { getRoleList } from '../api/role';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
});

const getRoleTagType = (roleCode) => {
  const map = {
    ROLE_ADMIN: 'danger',
    ROLE_OPERATOR: 'primary',
    ROLE_LOGISTICS: 'warning',
    ROLE_CUSTOMER_SERVICE: 'success'
  };
  return map[roleCode] || 'info';
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getUserPage({
      page: queryParams.page,
      size: queryParams.size,
      keyword: queryParams.keyword || undefined
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
  queryParams.page = 1;
  fetchData();
};

const handleToggleStatus = async (row, val) => {
  const targetStatus = val ? 1 : 0;
  try {
    await updateUserStatus(row.id, targetStatus);
    row.status = targetStatus;
    ElMessage.success(`用户已成功${targetStatus === 1 ? '启用' : '禁用'}`);
  } catch (err) {}
};

// 角色分配
const roleDialogVisible = ref(false);
const roleSubmitting = ref(false);
const currentUser = ref(null);
const allRoleList = ref([]);
const selectedRoleIds = ref([]);

const openRoleDialog = async (row) => {
  currentUser.value = row;
  selectedRoleIds.value = (row.roles || []).map(r => r.id);
  roleDialogVisible.value = true;

  if (allRoleList.value.length === 0) {
    try {
      const res = await getRoleList();
      allRoleList.value = res || [];
    } catch (e) {}
  }
};

const submitRoleAssign = async () => {
  if (!currentUser.value) return;
  roleSubmitting.value = true;
  try {
    await assignUserRoles(currentUser.value.id, selectedRoleIds.value);
    ElMessage.success('用户角色分配成功！');
    roleDialogVisible.value = false;
    fetchData();
  } catch (err) {
  } finally {
    roleSubmitting.value = false;
  }
};

// 编辑昵称
const editDialogVisible = ref(false);
const editSubmitting = ref(false);
const editForm = reactive({
  nickname: ''
});

const openEditDialog = (row) => {
  currentUser.value = row;
  editForm.nickname = row.nickname || '';
  editDialogVisible.value = true;
};

const submitEdit = async () => {
  if (!currentUser.value) return;
  editSubmitting.value = true;
  try {
    await updateUser(currentUser.value.id, { nickname: editForm.nickname });
    ElMessage.success('更新用户信息成功！');
    editDialogVisible.value = false;
    fetchData();
  } catch (err) {
  } finally {
    editSubmitting.value = false;
  }
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要物理删除用户 "${row.username}" 吗？`, '删除提醒', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      await deleteUser(row.id);
      ElMessage.success('删除成功');
      fetchData();
    } catch (err) {}
  }).catch(() => {});
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.user-page {
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
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username-text {
  font-weight: 600;
  color: #303133;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-user-info {
  padding: 10px 14px;
  background-color: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
  color: #606266;
}

.role-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.role-checkbox {
  margin-right: 0 !important;
}
</style>
