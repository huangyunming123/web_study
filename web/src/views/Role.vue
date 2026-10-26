<template>
  <div class="role-page">
    <el-card shadow="never" class="table-card">
      <div class="card-header">
        <div class="header-title">
          <el-icon><Lock /></el-icon>
          系统角色与权限分配矩阵
        </div>
        <el-tag type="info">RBAC 基于角色的权限访问控制模型</el-tag>
      </div>

      <!-- 角色表格 -->
      <el-table
        v-loading="loading"
        :data="roleList"
        border
        stripe
        style="width: 100%; margin-top: 16px"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="roleName" label="角色名称" width="180">
          <template #default="{ row }">
            <span class="role-name-text">{{ row.roleName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roleCode" label="角色唯一编码" width="200">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.roleCode)">{{ row.roleCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="角色职能描述" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="权限配置" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" icon="Setting" @click="openPermDrawer(row)">
              配置权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 权限树配置抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="配置角色操作权限"
      size="480px"
      destroy-on-close
    >
      <div v-loading="drawerLoading" class="drawer-inner">
        <div class="role-summary-banner">
          当前配置角色：<b>{{ currentRole?.roleName }}</b>
          <el-tag size="small" :type="getRoleTagType(currentRole?.roleCode)" style="margin-left: 8px">
            {{ currentRole?.roleCode }}
          </el-tag>
        </div>

        <p class="perm-tips">请勾选该角色具备访问权限的系统菜单与按钮操作权限：</p>

        <el-tree
          ref="treeRef"
          :data="permissionTree"
          :props="treeProps"
          show-checkbox
          node-key="id"
          default-expand-all
          highlight-current
          class="permission-tree"
        >
          <template #default="{ node, data }">
            <span class="tree-node-label">
              <span>{{ node.label }}</span>
              <el-tag size="small" :type="data.type === 1 ? 'primary' : 'info'" effect="plain">
                {{ data.type === 1 ? '菜单' : '操作' }}
              </el-tag>
            </span>
          </template>
        </el-tree>
      </div>

      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitPermissions">
          保存权限配置
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import {
  getRoleList,
  getPermissionTree,
  getRolePermissions,
  assignRolePermissions
} from '../api/role';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const roleList = ref([]);

const drawerVisible = ref(false);
const drawerLoading = ref(false);
const submitLoading = ref(false);
const currentRole = ref(null);
const permissionTree = ref([]);
const treeRef = ref(null);

const treeProps = {
  children: 'children',
  label: 'name'
};

const getRoleTagType = (roleCode) => {
  const map = {
    ROLE_ADMIN: 'danger',
    ROLE_OPERATOR: 'primary',
    ROLE_LOGISTICS: 'warning',
    ROLE_CUSTOMER_SERVICE: 'success'
  };
  return map[roleCode] || 'info';
};

const fetchRoles = async () => {
  loading.value = true;
  try {
    const res = await getRoleList();
    roleList.value = res || [];
  } catch (err) {
  } finally {
    loading.value = false;
  }
};

const openPermDrawer = async (row) => {
  currentRole.value = row;
  drawerVisible.value = true;
  drawerLoading.value = true;

  try {
    // 1. 获取权限树
    if (permissionTree.value.length === 0) {
      const treeRes = await getPermissionTree();
      permissionTree.value = treeRes || [];
    }

    // 2. 获取当前角色绑定的权限ID
    const permIds = await getRolePermissions(row.id);

    // 3. 勾选已有权限 (针对叶子节点勾选或过滤父节点)
    if (treeRef.value && permIds) {
      // 过滤出叶子节点或者全部设置
      treeRef.value.setCheckedKeys(permIds);
    }
  } catch (err) {
  } finally {
    drawerLoading.value = false;
  }
};

const submitPermissions = async () => {
  if (!currentRole.value || !treeRef.value) return;
  submitLoading.value = true;

  // 获取选中与半选中的节点ID
  const checkedKeys = treeRef.value.getCheckedKeys();
  const halfCheckedKeys = treeRef.value.getHalfCheckedKeys();
  const allSelectedKeys = [...checkedKeys, ...halfCheckedKeys];

  try {
    await assignRolePermissions(currentRole.value.id, allSelectedKeys);
    ElMessage.success(`角色 [${currentRole.value.roleName}] 权限矩阵更新成功！`);
    drawerVisible.value = false;
  } catch (err) {
  } finally {
    submitLoading.value = false;
  }
};

onMounted(() => {
  fetchRoles();
});
</script>

<style scoped>
.role-page {
  display: flex;
  flex-direction: column;
}

.table-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.role-name-text {
  font-weight: 600;
  color: #303133;
}

.drawer-inner {
  display: flex;
  flex-direction: column;
}

.role-summary-banner {
  padding: 12px 14px;
  background-color: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
  color: #303133;
  display: flex;
  align-items: center;
}

.perm-tips {
  margin: 14px 0 10px;
  font-size: 13px;
  color: #909399;
}

.permission-tree {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px;
  max-height: 520px;
  overflow-y: auto;
}

.tree-node-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 14px;
}
</style>
