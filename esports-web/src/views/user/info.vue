<template>
  <div class="user-info-page full-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>用户信息</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-loading="loading">
      <el-card v-if="userInfo" shadow="never" class="user-info-card">
        <div class="user-header">
          <div class="u-avatar">
            <el-avatar :size="100" :src="userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          </div>
          <div class="u-info">
            <h1 class="u-name">{{ userInfo.nickname || userInfo.username }}</h1>
            <div class="u-meta">
              <el-tag size="large">{{ userInfo.university || '未绑定高校' }}</el-tag>
              <el-tag size="large" type="info">{{ userInfo.gender || '未知性别' }}</el-tag>
              <el-tag v-if="userInfo.role === 'ROLE_LEADER'" size="large" type="danger" effect="dark">战队队长</el-tag>
              <el-tag v-else-if="userInfo.role === 'ROLE_TEAM_MEMBER'" size="large" type="primary" effect="dark">战队成员</el-tag>
            </div>
            <p class="u-signature">{{ userInfo.signature || '该用户很神秘，暂无个人签名...' }}</p>
          </div>
        </div>

        <el-divider />

        <div class="user-details">
          <el-form label-width="120px" class="info-form">
            <el-form-item label="选手账号">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="竞技昵称">
              <el-input v-model="userInfo.nickname" disabled />
            </el-form-item>
            <el-form-item label="所属高校">
              <el-input v-model="userInfo.university" disabled />
            </el-form-item>
            <el-form-item label="选手性别">
              <el-input v-model="userInfo.gender" disabled />
            </el-form-item>
            <el-form-item label="竞技宣言">
              <el-input v-model="userInfo.signature" disabled type="textarea" />
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <el-empty v-else description="用户信息不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import request from '../../utils/request';
import { useUserStore } from '../../store/user';

const route = useRoute();
const userStore = useUserStore();
const userInfo = ref(null);
const loading = ref(false);

const fetchUserInfo = async () => {
  const userId = route.params.id;
  if (!userId) return;
  
  loading.value = true;
  try {
    const res = await request.get(`/user/${userId}`);
    userInfo.value = res.data;
    // 如果是当前登录用户，更新store中的用户信息
    if (userId == userStore.userInfo.id) {
      userStore.setUserInfo(res.data);
    }
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

onMounted(fetchUserInfo);
</script>

<style scoped>
.user-info-page { padding: 20px 0; }
.page-header { margin-bottom: 20px; }

.user-info-card { border-radius: 12px; }
.user-header { display: flex; gap: 30px; align-items: center; margin-bottom: 20px; }

.u-avatar { flex-shrink: 0; }

.u-info { flex: 1; }
.u-name { font-size: 24px; font-weight: bold; color: #333; margin: 0 0 15px 0; }
.u-meta { display: flex; gap: 10px; margin-bottom: 10px; }
.u-signature { font-size: 14px; color: #666; line-height: 1.5; margin: 0; }

.user-details { margin-top: 20px; }
.info-form { max-width: 600px; }
</style>