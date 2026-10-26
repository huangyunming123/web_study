<template>
  <div class="register-wrapper">
    <el-card class="register-card" shadow="hover">
      <div class="card-header">
        <h2 class="title">商城教学系统 - 注册</h2>
        <p class="subtitle">创建您的演示账号</p>
      </div>

      <el-form
        ref="regFormRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入登录用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="用户昵称" prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入用户昵称"
            prefix-icon="Avatar"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码 (不少于6位)"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <div class="actions">
          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </div>

        <div class="footer-links">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">返回登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { registerApi } from '../api/auth';
import { ElMessage } from 'element-plus';

const router = useRouter();
const regFormRef = ref(null);
const loading = ref(false);

const form = reactive({
  username: '',
  nickname: '',
  password: ''
});

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
};

const handleRegister = async () => {
  if (!regFormRef.value) return;
  await regFormRef.value.validate(async (valid) => {
    if (!valid) return;
    loading.value = true;
    try {
      await registerApi(form);
      ElMessage.success('注册成功，请使用新账号登录');
      router.push('/login');
    } catch (err) {
      // 错误由统一拦截器处理
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped>
.register-wrapper {
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #3a7bd5 100%);
}

.register-card {
  width: 420px;
  border-radius: 12px;
  padding: 10px 20px;
}

.card-header {
  text-align: center;
  margin-bottom: 25px;
}

.title {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  color: #909399;
}

.actions {
  margin-top: 15px;
}

.submit-btn {
  width: 100%;
}

.footer-links {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.login-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.login-link:hover {
  text-decoration: underline;
}
</style>
