<template>
  <div class="team-management-page full-container">
    <div class="page-header">
      <h1 class="page-title">战队管理中心</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/team' }">战队列表</el-breadcrumb-item>
        <el-breadcrumb-item>战队管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 当还没有确定管理哪个战队时，或者在获取数据中 -->
    <div v-if="!teamId && !loading" class="no-team-wrapper">
      <el-empty description="未找到可管理的战队信息">
        <el-button type="primary" @click="$router.push('/team')">前往战队大厅</el-button>
      </el-empty>
    </div>

    <el-card v-else shadow="never" class="manage-card">
      <template #header>
        <div class="header">
          <span class="title">管理战队</span>
          <el-tag type="danger" effect="dark">队长权限</el-tag>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 成员管理 -->
        <el-tab-pane label="成员名单" name="members">
          <el-table :data="members" border stripe v-loading="loading">
            <el-table-column prop="nickname" label="选手昵称" />
            <el-table-column prop="username" label="注册邮箱" />
            <el-table-column label="队内角色" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.role === 1 ? 'danger' : 'info'">
                  {{ scope.row.role === 1 ? '队长' : '正式成员' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="right">
              <template #default="scope">
                <el-button 
                  v-if="scope.row.role !== 1" 
                  type="danger" 
                  link 
                  @click="handleRemoveMember(scope.row.id)"
                >移出战队</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 入队申请 -->
        <el-tab-pane name="applications">
          <template #label>
            <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge-item">
              入队申请
            </el-badge>
          </template>
          <el-table :data="applications" border stripe>
            <el-table-column prop="id" label="申请ID" width="80" />
            <el-table-column prop="userId" label="选手ID" width="100" />
            <el-table-column prop="createTime" label="申请时间" width="180" />
            <el-table-column label="操作" align="right">
              <template #default="scope">
                <el-button type="success" size="small" @click="handleReview(scope.row.id, 1)">通过</el-button>
                <el-button type="danger" size="small" @click="handleReview(scope.row.id, 2)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../store/user';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const teamId = ref(route.query.teamId || null);
const activeTab = ref('members');
const members = ref([]);
const applications = ref([]);
const loading = ref(false);
const pendingCount = ref(0);

// 如果路由没传 teamId，尝试从用户的我的战队里找到他是队长的战队
const initTeamId = async () => {
  if (teamId.value) return true;
  
  if (!userStore.userInfo.id) return false;
  
  try {
    const res = await request.get(`/team/my/${userStore.userInfo.id}`);
    const myTeams = res.data || [];
    // 找到用户是队长的战队
    const myLedTeam = myTeams.find(t => t.leaderId === userStore.userInfo.id);
    if (myLedTeam) {
      teamId.value = myLedTeam.id;
      return true;
    }
  } catch (err) {
    console.error('获取归属战队失败', err);
  }
  return false;
};

const fetchMembers = async () => {
  if (!teamId.value) return;
  loading.value = true;
  try {
    const res = await request.get(`/team/members/${teamId.value}`);
    members.value = res.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const fetchApplications = async () => {
  if (!teamId.value) return;
  try {
    const res = await request.get(`/team/applications/${teamId.value}`);
    applications.value = res.data.filter(a => a.status === 0);
    pendingCount.value = applications.value.length;
  } catch (err) {
    console.error(err);
  }
};

const handleTabChange = (name) => {
  if (name === 'members') fetchMembers();
  if (name === 'applications') fetchApplications();
};

const handleReview = async (appId, status) => {
  try {
    await request.post(`/team/applications/review/${appId}?status=${status}`);
    ElMessage.success('操作成功');
    fetchApplications();
    fetchMembers();
  } catch (err) {
    console.error(err);
  }
};

const handleRemoveMember = async (userId) => {
  try {
    await request.post(`/team/member/remove?teamId=${teamId.value}&userId=${userId}`);
    ElMessage.success('成员已移出');
    fetchMembers();
  } catch (err) {
    console.error(err);
  }
};

onMounted(async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  loading.value = true;
  const hasTeam = await initTeamId();
  if (hasTeam) {
    fetchMembers();
    fetchApplications();
  } else {
    loading.value = false;
  }
});
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; color: #333; margin-bottom: 10px; }

.no-team-wrapper {
  padding: 100px 0;
  background: var(--bg-white);
  border-radius: 8px;
}

.manage-card { border-radius: 12px; }
.header { display: flex; align-items: center; gap: 12px; }
.title { font-size: 18px; font-weight: bold; color: #333; }
.badge-item { margin-top: 4px; }
</style>
