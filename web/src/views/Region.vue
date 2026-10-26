<template>
  <div class="region-page">
    <el-card shadow="never" class="tree-card">
      <div class="tree-header">
        <div class="header-info">
          <span class="header-title">行政地区字典树</span>
          <el-tag type="info" size="small" effect="plain">后端 Redis 缓存 10 分钟，增删改自动清除缓存</el-tag>
        </div>
        <div>
          <el-button type="primary" icon="Plus" @click="openDialog('add-root')">新增顶级地区</el-button>
          <el-button icon="Refresh" @click="fetchTree">刷新</el-button>
        </div>
      </div>

      <div class="tree-container" v-loading="loading">
        <el-empty v-if="!treeData.length && !loading" description="暂无地区数据" />
        <el-tree
          v-else
          :data="treeData"
          node-key="id"
          default-expand-all
          :expand-on-click-node="false"
          :props="{ label: 'name', children: 'children' }"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span class="node-label">
                <el-tag size="small" :type="getLevelTagType(data.level)" class="level-tag">
                  L{{ data.level }}
                </el-tag>
                <span class="node-name">{{ data.name }}</span>
                <span class="node-id">(ID: {{ data.id }})</span>
              </span>
              <span class="node-actions">
                <el-button
                  link
                  type="primary"
                  size="small"
                  icon="Plus"
                  @click.stop="openDialog('add-child', data)"
                >
                  新增子级
                </el-button>
                <el-button
                  link
                  type="warning"
                  size="small"
                  icon="Edit"
                  @click.stop="openDialog('edit', data)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  size="small"
                  icon="Delete"
                  @click.stop="handleDelete(data)"
                >
                  删除
                </el-button>
              </span>
            </span>
          </template>
        </el-tree>
      </div>
    </el-card>

    <!-- 地区新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="460px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="padding-right: 20px"
      >
        <el-form-item label="父级节点">
          <el-input :value="parentDisplayName" disabled />
        </el-form-item>
        <el-form-item label="地区名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入地区名称 (如: 广东省 / 广州市)" />
        </el-form-item>
        <el-form-item label="地区层级" prop="level">
          <el-input-number v-model="form.level" :min="1" :max="5" style="width: 100%" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import {
  getRegionTree,
  addRegion,
  updateRegion,
  deleteRegion
} from '../api/region';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const treeData = ref([]);

const dialogVisible = ref(false);
const dialogMode = ref('add-root'); // 'add-root' | 'add-child' | 'edit'
const submitLoading = ref(false);
const formRef = ref(null);
const selectedParent = ref(null);

const form = reactive({
  id: null,
  parentId: 0,
  name: '',
  level: 1
});

const rules = {
  name: [{ required: true, message: '地区名称不能为空', trigger: 'blur' }],
  level: [{ required: true, message: '请指定层级', trigger: 'blur' }]
};

const dialogTitle = computed(() => {
  if (dialogMode.value === 'add-root') return '新增顶级行政区';
  if (dialogMode.value === 'add-child') return '新增下级地区';
  return '编辑地区信息';
});

const parentDisplayName = computed(() => {
  if (dialogMode.value === 'add-root') return '无 (顶级地区 parent_id = 0)';
  if (dialogMode.value === 'add-child') return `${selectedParent.value?.name} (ID: ${selectedParent.value?.id})`;
  return form.parentId === 0 ? '顶级地区' : `父ID: ${form.parentId}`;
});

const getLevelTagType = (level) => {
  if (level === 1) return 'primary';
  if (level === 2) return 'success';
  if (level === 3) return 'warning';
  return 'info';
};

const fetchTree = async () => {
  loading.value = true;
  try {
    const res = await getRegionTree();
    treeData.value = res || [];
  } catch (err) {
  } finally {
    loading.value = false;
  }
};

const openDialog = (mode, nodeData = null) => {
  dialogMode.value = mode;
  selectedParent.value = nodeData;

  if (mode === 'add-root') {
    form.id = null;
    form.parentId = 0;
    form.name = '';
    form.level = 1;
  } else if (mode === 'add-child') {
    form.id = null;
    form.parentId = nodeData.id;
    form.name = '';
    form.level = (nodeData.level || 1) + 1;
  } else if (mode === 'edit') {
    form.id = nodeData.id;
    form.parentId = nodeData.parentId;
    form.name = nodeData.name;
    form.level = nodeData.level;
  }

  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      if (dialogMode.value === 'edit') {
        await updateRegion(form.id, {
          name: form.name,
          parentId: form.parentId,
          level: form.level
        });
        ElMessage.success('地区修改成功，Redis 缓存已同步失效');
      } else {
        await addRegion({
          name: form.name,
          parentId: form.parentId,
          level: form.level
        });
        ElMessage.success('地区新增成功，Redis 缓存已同步失效');
      }
      dialogVisible.value = false;
      fetchTree();
    } catch (err) {
    } finally {
      submitLoading.value = false;
    }
  });
};

const handleDelete = (data) => {
  ElMessageBox.confirm(`确定要删除地区 "${data.name}" 吗？注意：若存在子节点将直接拒绝删除！`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRegion(data.id);
      ElMessage.success('地区已物理删除，Redis 缓存已同步清除');
      fetchTree();
    } catch (err) {}
  }).catch(() => {});
};

onMounted(() => {
  fetchTree();
});
</script>

<style scoped>
.region-page {
  display: flex;
  flex-direction: column;
}

.tree-card {
  border-radius: 8px;
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.tree-container {
  padding-top: 16px;
  min-height: 300px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 12px;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.level-tag {
  font-weight: bold;
}

.node-name {
  font-weight: 500;
  color: #303133;
}

.node-id {
  color: #909399;
  font-size: 12px;
}

.node-actions {
  display: none;
  gap: 6px;
}

/* 节点 Hover 时显示快捷操作按钮 */
.custom-tree-node:hover .node-actions {
  display: inline-flex;
}
</style>
