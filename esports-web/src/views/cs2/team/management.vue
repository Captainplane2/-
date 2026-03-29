<template>
  <div class="team-management-page full-container">
    <div class="page-header">
      <h1 class="page-title">CS2 战队管理中心</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/cs2' }">CS2首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/cs2/team' }">战队列表</el-breadcrumb-item>
        <el-breadcrumb-item>战队管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-if="!teamId" class="empty-state">
      <el-empty description="您还没有创建或加入任何CS2战队" />
      <el-button type="primary" style="margin-top: 20px" @click="$router.push('/cs2/team')">创建/加入战队</el-button>
    </div>

    <el-card v-else shadow="never" class="manage-card">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
          <div style="display: flex; align-items: center; gap: 12px;">
            <span class="title">战队管理中心</span>
            <el-tag type="danger" effect="dark">队长权限</el-tag>
            <el-button type="primary" @click="goToTeamDetail">战队详情页</el-button>
          </div>
          <el-button type="danger" @click="confirmDissolve">解散战队</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 战队资料编辑 -->
        <el-tab-pane label="战队资料" name="info">
          <el-form :model="teamForm" label-width="100px" class="info-form">
            <el-form-item label="战队名称">
              <el-input v-model="teamForm.name" placeholder="请输入战队名称" />
            </el-form-item>
            <el-form-item label="战队Logo">
              <div class="logo-edit-box">
                <div class="logo-preview" @click="triggerLogoUpload">
                  <img :src="teamForm.logo || 'https://via.placeholder.com/200x150/eee/999?text=LOGO'" class="logo-img" />
                  <div class="logo-edit-mask">更换Logo</div>
                </div>
                <el-upload
                  ref="logoUploadRef"
                  class="logo-uploader"
                  :action="`${env.apiBaseURL}/user/upload`"
                  :show-file-list="false"
                  :on-success="handleLogoSuccess"
                  :headers="uploadHeaders"
                >
                  <el-button type="primary" size="small">上传Logo</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="战队简介">
              <el-input v-model="teamForm.description" type="textarea" :rows="3" placeholder="请输入战队简介" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateTeam">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 成员管理 -->
        <el-tab-pane label="成员名单" name="members">
          <el-table :data="members" border stripe v-loading="loading">
            <el-table-column prop="nickname" label="选手昵称">
              <template #default="scope">
                <span class="clickable" @click="goToUserInfo(scope.row.id)">{{ scope.row.nickname }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="university" label="成员所属大学" />
            <el-table-column label="队内角色" width="120" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.role === 1 ? 'danger' : ''" effect="light" style="background-color: #e6f7ff; color: #1890ff; border-color: #91d5ff;">
                  {{ scope.row.role === 1 ? '队长' : '正式成员' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="joinTime" label="加入时间" width="180" />
            <el-table-column label="操作" width="150" align="center">
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
            <div class="tab-label">
              入队申请
              <el-badge v-if="pendingCount > 0" :value="pendingCount" />
            </div>
          </template>
          <el-table :data="applications" border stripe v-loading="loading">
            <el-table-column prop="nickname" label="选手昵称" width="150">
              <template #default="scope">
                <span class="clickable" @click="goToUserInfo(scope.row.userId)">{{ scope.row.nickname }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="university" label="选手所属学校" width="180" />
            <el-table-column prop="createTime" label="申请时间" width="180" />
            <el-table-column label="操作" width="150" align="right">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleApprove(scope.row.id)"
                >通过</el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleReject(scope.row.id)"
                >拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../../store/user';
import request from '../../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import env from '../../../config/env';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const teamId = ref(null);
const teamForm = ref({});
const members = ref([]);
const applications = ref([]);
const loading = ref(false);
const activeTab = ref('info');
const logoUploadRef = ref(null);

const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${userStore.token}`
  };
});

const pendingCount = computed(() => {
  return applications.value.filter(app => app.status === 0).length;
});

const fetchMyTeams = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  try {
    const res = await request.get(`/team/my/${userStore.userInfo.id}`);
    // 过滤出CS2战队
    const cs2Teams = (res.data || []).filter(t => t.gameProject === 'CS2');
    if (cs2Teams.length > 0) {
      teamId.value = cs2Teams[0].id;
      fetchTeamDetail();
      fetchMembers();
      fetchApplications();
    }
  } catch (err) {
    console.error(err);
  }
};

const fetchTeamDetail = async () => {
  if (!teamId.value) return;
  try {
    const res = await request.get(`/team/${teamId.value}`);
    teamForm.value = res.data;
    // 确保logo路径是完整的URL
    if (teamForm.value.logo && !teamForm.value.logo.startsWith('http')) {
      teamForm.value.logo = env.getFullApiUrl(teamForm.value.logo);
    }
  } catch (err) {
    console.error(err);
  }
};

const fetchMembers = async () => {
  if (!teamId.value) return;
  try {
    const res = await request.get(`/team/members/${teamId.value}`);
    members.value = res.data || [];
  } catch (err) {
    console.error(err);
  }
};

const fetchApplications = async () => {
  if (!teamId.value) return;
  try {
    const res = await request.get(`/team/applications/${teamId.value}`);
    applications.value = res.data || [];
  } catch (err) {
    console.error(err);
  }
};

const handleUpdateTeam = async () => {
  if (!teamId.value) return;
  try {
    await request.post('/team/update', teamForm.value);
    ElMessage.success('战队资料更新成功');
  } catch (err) {
    console.error(err);
  }
};

const triggerLogoUpload = () => {
  logoUploadRef.value.$refs.input.click();
};

const handleLogoSuccess = (res) => {
  if (res.code === 200) {
    if (res.data && !res.data.startsWith('http')) {
      teamForm.value.logo = env.getFullApiUrl(res.data);
    } else {
      teamForm.value.logo = res.data;
    }
  }
};

const handleApprove = async (appId) => {
  try {
    await request.post(`/team/applications/review/${appId}`, null, { params: { status: 1 } });
    ElMessage.success('已通过申请');
    fetchApplications();
    fetchMembers();
    // 触发全局事件，通知顶部导航栏更新角标
    window.dispatchEvent(new Event('refresh-pending-count'));
  } catch (err) {
    console.error(err);
  }
};

const handleReject = async (appId) => {
  try {
    await request.post(`/team/applications/review/${appId}`, null, { params: { status: 2 } });
    ElMessage.success('已拒绝申请');
    fetchApplications();
    // 触发全局事件，通知顶部导航栏更新角标
    window.dispatchEvent(new Event('refresh-pending-count'));
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

const goToUserInfo = (userId) => {
  router.push(`/user/info/${userId}`);
};

const goToTeamDetail = () => {
  if (teamId.value) {
    router.push(`/team/detail/${teamId.value}`);
  }
};

const confirmDissolve = () => {
  ElMessageBox.confirm(
    '确认解散该战队？解散后所有成员将变为普通选手。',
    '警告',
    {
      confirmButtonText: '确定解散',
      cancelButtonText: '取消',
      type: 'error',
      center: true
    }
  ).then(() => {
    handleDissolve();
  }).catch(() => {});
};

const handleDissolve = async () => {
  try {
    await request.post(`/team/dismiss/${teamId.value}`);
    ElMessage.success('战队已解散');
    // 刷新用户信息，更新角色状态
    userStore.setUserInfo({ ...userStore.userInfo, role: 'ROLE_USER' });
    // 跳转到战队列表页
    router.push('/cs2/team');
  } catch (err) {
    console.error(err);
  }
};

const handleTabChange = (tab) => {
  if (tab === 'applications') {
    fetchApplications();
  }
};

onMounted(async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  await fetchMyTeams();
});
</script>

<style scoped>
.team-management-page { padding: 20px 0; }

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.manage-card {
  border-radius: 8px;
}

.header { display: flex; align-items: center; justify-content: space-between; width: 100%; }

.title { font-size: 18px; font-weight: bold; color: #333; }

.header-actions { display: flex; align-items: center; gap: 12px; }

.badge-item { margin-top: 4px; }

.clickable {
  color: var(--primary);
  cursor: pointer;
  text-decoration: underline;
}

.logo-edit-box {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.logo-uploader {
  position: relative;
  cursor: pointer;
}

.logo-preview {
  width: 200px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  border: 1px solid #f0f0f0;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.logo-edit-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: white;
  text-align: center;
  padding: 10px;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s;
}

.logo-preview:hover .logo-edit-mask {
  opacity: 1;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 5px;
}

.info-form {
  max-width: 600px;
}
</style>