<template>
  <div class="team-detail-page full-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/cs2' }">CS2首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/cs2/team' }">战队列表</el-breadcrumb-item>
        <el-breadcrumb-item>战队详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-loading="loading">
      <el-card v-if="team" shadow="never" class="team-header-card">
        <div class="header-content">
          <div class="t-logo">
            <img :src="team.logo || 'https://via.placeholder.com/150'" />
          </div>
          <div class="t-info">
            <h1 class="t-name">{{ team.name }}</h1>
            <div class="t-tags">
              <el-tag size="large">{{ team.gameProject }}</el-tag>
              <el-tag size="large" type="info"><el-icon><School /></el-icon> {{ team.university }}</el-tag>
              <el-tag size="large" type="warning">队长: {{ getLeaderNickname() }}</el-tag>
            </div>
            <p class="t-desc">{{ team.description || '这支战队很神秘，暂时没有简介...' }}</p>
          </div>
          <div class="t-actions">
            <div class="member-count">
              <span class="num">{{ team.memberCount || members.length }}</span>
              <span class="label">名成员</span>
            </div>
            <el-button type="primary" size="large" class="join-btn" @click="handleJoin">申请加入</el-button>
          </div>
        </div>
      </el-card>

      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 成员列表 -->
        <el-col :span="16">
          <el-card shadow="never" class="members-card">
            <template #header>
              <div class="card-title">战队成员</div>
            </template>
            <div class="member-grid">
              <div v-for="m in members" :key="m.id" class="member-item" @click="goToUserInfo(m.id)">
                <el-avatar :size="60" :src="m.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                <div class="m-info">
                  <span class="m-name">{{ m.nickname }}</span>
                  <el-tag size="small" :type="m.role === 1 ? 'danger' : 'info'">{{ m.role === 1 ? '队长' : '队员' }}</el-tag>
                </div>
              </div>
              <el-empty v-if="members.length === 0" description="暂无成员数据" />
            </div>
          </el-card>
        </el-col>
        <!-- 战队动态/战绩占位 -->
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>
              <div class="card-title">近期动态</div>
            </template>
            <el-empty description="该战队暂无公开动态" :image-size="80" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '../../../utils/request';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../../../store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const team = ref(null);
const members = ref([]);
const loading = ref(false);

const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;
  
  loading.value = true;
  try {
    // 根据目前后端的接口，先请求成员，从成员中找到队长或者直接展示。
    // 如果后端没有单独的 getTeamById 接口，我们可以从 list 里面过滤
    const listRes = await request.get('/team/list', { params: { gameProject: 'CS2' } });
    const t = listRes.data.find(item => item.id == id);
    if (t) {
      team.value = t;
    }
    
    // 获取成员
    const mRes = await request.get(`/team/members/${id}`);
    members.value = mRes.data || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleJoin = async () => {
  if (!userStore.token) return ElMessage.warning('请先登录');
  if (!team.value) return;
  
  try {
    await request.post(`/team/join/${team.value.id}`, null, {
      params: { userId: userStore.userInfo.id }
    });
    ElMessage.success('申请已发送，请等待队长审核');
  } catch (err) {
    console.error(err);
  }
};

const goToUserInfo = (userId) => {
  router.push(`/user/info/${userId}`);
};

const getLeaderNickname = () => {
  if (!team.value) return '';
  const leader = members.value.find(m => m.role === 1);
  return leader ? leader.nickname : team.value.leaderId;
};

onMounted(fetchDetail);
</script>

<style scoped>
.team-detail-page { padding: 20px 0; }

.page-header { margin-bottom: 20px; }

.team-header-card { border-radius: 12px; }

.header-content { display: flex; gap: 30px; align-items: center; }

.t-logo { width: 120px; height: 120px; border-radius: 12px; overflow: hidden; flex-shrink: 0; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }

.t-logo img { width: 100%; height: 100%; object-fit: cover; }

.t-info { flex: 1; }

.t-name { font-size: 28px; font-weight: bold; color: #333; margin: 0 0 15px 0; }

.t-tags { display: flex; gap: 10px; margin-bottom: 15px; }

.t-desc { font-size: 15px; color: #666; line-height: 1.6; margin: 0; }

.t-actions { display: flex; flex-direction: column; align-items: center; gap: 15px; min-width: 150px; border-left: 1px solid #f0f0f0; padding-left: 30px; }

.member-count { text-align: center; }

.member-count .num { font-size: 32px; font-weight: bold; color: var(--primary); display: block; }

.member-count .label { font-size: 13px; color: #999; }

.join-btn { width: 100%; border-radius: 20px; }

.card-title { font-weight: bold; font-size: 16px; }

.member-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 20px; }

.member-item { display: flex; flex-direction: column; align-items: center; text-align: center; }

.m-info { margin-top: 10px; display: flex; flex-direction: column; align-items: center; gap: 5px; }

.m-name { font-size: 14px; font-weight: 500; color: #333; width: 100%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>