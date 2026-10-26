<template>
  <div class="login-wrapper">
    <el-card class="login-card" shadow="hover">
      <div class="card-header">
        <h2 class="title">商城教学系统 - 登录</h2>
        <p class="subtitle">Spring Boot 3.3 + Vue 3 极简微服务教学工程</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名 (默认: admin)"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码 (默认: 123456)"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="actions">
          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </div>

        <div class="footer-links">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">去注册新用户</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { loginApi } from '../api/auth';
import { useUserStore } from '../store/user';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

const loginFormRef = ref(null);
const loading = ref(false);

const form = reactive({
  username: '',
  password: ''
});

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return;
    loading.value = true;
    try {
      const data = await loginApi(form);
      userStore.setLoginInfo(data.token, data.userInfo);
      ElMessage.success('登录成功');
      router.push('/');
    } catch (err) {
      // 错误由 request.js 统一拦截提示
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped>
.login-wrapper {
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #3a7bd5 100%);
}

.login-card {
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

.register-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
