<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="always">
      <div class="card-title">
        <h2>用户登录</h2>
        <p>欢迎回到 蘸豆爽！</p>
      </div>
      
      <el-form :model="loginForm" label-width="0">
        <el-form-item>
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入注册邮箱" 
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock" 
            show-password 
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="full-btn" size="large" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      
      <div class="card-footer">
        <span>还没有账号？</span>
        <el-button link type="primary" @click="$router.push('/register')">立即注册</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../store/user';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

const loginForm = ref({ username: '', password: '' });
const loading = ref(false);

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请填写完整');
    return;
  }
  loading.value = true;
  try {
    const res = await request.post('/user/login', loginForm.value);
    userStore.setToken(res.data.token);
    userStore.setUserInfo(res.data.user);
    ElMessage.success('欢迎回来！');
    router.push('/');
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.auth-page {
  height: 80vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: transparent;
}

.auth-card {
  width: 400px;
  padding: 30px 20px;
  border-radius: 12px;
}

.card-title {
  text-align: center;
  margin-bottom: 40px;
}

.card-title h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.card-title p {
  font-size: 14px;
  color: #999;
}

.full-btn {
  width: 100%;
  margin-top: 10px;
}

.card-footer {
  margin-top: 25px;
  text-align: center;
  font-size: 14px;
  color: #666;
}
</style>
