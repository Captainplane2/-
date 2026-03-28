<template>
  <div class="match-management-page full-container">
    <div class="page-header">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="gameProject" :to="`/${gameProject}`">{{ gameProjectDisplayName }}</el-breadcrumb-item>
        <el-breadcrumb-item v-else :to="{ path: '/match' }">约战大厅</el-breadcrumb-item>
        <el-breadcrumb-item>约战管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card shadow="never" class="manage-card">
      <template #header>
        <div class="header">
          <span class="title">约战管理中心</span>
          <el-tag type="danger" effect="dark">队长权限</el-tag>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 我发布的约战 -->
        <el-tab-pane label="我发布的约战" name="my-matches">
          <el-table :data="myMatches" border stripe v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="约战标题" />
            <el-table-column prop="gameProject" label="项目"/>
            <el-table-column prop="matchTime" label="开赛时间" width="180" />
            <el-table-column label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="goToMatchDetail(scope.row.id)">查看详情</el-button>
                <el-button 
                  v-if="scope.row.status === 0" 
                  type="danger" 
                  size="small" 
                  @click="cancelMatch(scope.row.id)"
                >取消</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 我收到的应战 -->
        <el-tab-pane label="我收到的应战" name="received-responses">
          <el-table :data="receivedResponses" border stripe v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="约战标题" />
            <el-table-column prop="guestTeamName" label="应战战队" />
            <el-table-column prop="matchTime" label="开赛时间" width="180" />
            <el-table-column label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="goToMatchDetail(scope.row.id)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useGameProjectStore } from '../../../store/gameProject';
import { useUserStore } from '../../../store/user';
import request from '../../../utils/request';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const gameProjectStore = useGameProjectStore();
const userStore = useUserStore();

const gameProject = computed(() => route.params.gameProject?.toUpperCase() || null);
const gameProjectDisplayName = computed(() => {
  if (!gameProject.value) return '约战大厅';
  const project = gameProjectStore.getGameProjectByName(gameProject.value);
  return project ? project.displayName : gameProject.value;
});

const pageTitle = computed(() => {
  return gameProject.value ? `${gameProjectDisplayName.value} 约战管理中心` : '约战管理中心';
});

const activeTab = ref('my-matches');
const loading = ref(false);
const myMatches = ref([]);
const receivedResponses = ref([]);

const fetchMyMatches = async () => {
  loading.value = true;
  try {
    const params = { hostId: userStore.userInfo.id };
    if (gameProject.value) {
      params.gameProject = gameProject.value;
    }
    const res = await request.get('/match-room/list', { params });
    myMatches.value = res.data || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const fetchReceivedResponses = async () => {
  loading.value = true;
  try {
    const params = {};
    if (gameProject.value) {
      params.gameProject = gameProject.value;
    }
    const res = await request.get('/match-room/list', { params });
    receivedResponses.value = res.data.filter(room => 
      room.hostId === userStore.userInfo.id && room.status === 1
    ) || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const goToMatchDetail = (matchId) => {
  if (gameProject.value) {
    router.push(`/${gameProject.value}/match/${matchId}`);
  } else {
    router.push(`/match/${matchId}`);
  }
};

const cancelMatch = async (matchId) => {
  try {
    await request.post(`/match-room/cancel/${matchId}`, null, { 
      params: { userId: userStore.userInfo.id }
    });
    ElMessage.success('约战已取消');
    fetchMyMatches();
  } catch (err) {
    console.error(err);
  }
};

const getStatusText = (status) => {
  const map = { 0: '招募中', 1: '已应战', 2: '已结束', 3: '已取消' };
  return map[status] || '未知';
};

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'warning', 2: 'info', 3: 'danger' };
  return map[status] || 'info';
};

onMounted(() => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  fetchMyMatches();
  fetchReceivedResponses();
});
</script>

<style scoped>
.match-management-page { padding: 20px 0; }

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.manage-card {
  border-radius: 8px;
}

.header { display: flex; align-items: center; gap: 12px; }

.title { font-size: 18px; font-weight: bold; color: #333; }
</style>