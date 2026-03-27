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

    <div v-else>
      <el-card shadow="never" class="manage-card">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
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
          <el-form :model="teamForm" label-width="100px" style="max-width: 600px; padding-top: 20px;">
            <el-form-item label="战队名称">
              <el-input v-model="teamForm.name" placeholder="请输入战队名称" />
            </el-form-item>
            <el-form-item label="战队Logo">
              <div class="logo-edit-box">
                <el-upload
                  class="avatar-uploader"
                  action="http://localhost:8080/api/user/upload"
                  :show-file-list="false"
                  :on-success="handleLogoSuccess"
                  :headers="uploadHeaders"
                >
                  <div class="logo-preview">
                    <img :src="teamForm.logo || 'https://via.placeholder.com/200x150/eee/999?text=LOGO'" class="logo-img" />
                    <div class="logo-edit-mask">更换Logo</div>
                  </div>
                </el-upload>
                <div class="upload-tip">点击图片上传新Logo</div>
              </div>
            </el-form-item>
            <el-form-item label="竞技项目">
              <el-select v-model="teamForm.gameProject" disabled style="width: 100%">
                <el-option :label="teamForm.gameProject" :value="teamForm.gameProject" />
              </el-select>
            </el-form-item>
            <el-form-item label="战队简介/口号">
              <el-input v-model="teamForm.description" type="textarea" :rows="4" placeholder="写下你们的口号或简介..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateTeamInfo">更新战队资料</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 成员管理 -->
        <el-tab-pane label="成员名单" name="members">
          <el-table :data="members" border stripe v-loading="loading">
            <el-table-column prop="nickname" label="选手昵称" @click="goToUserInfo(scope.row.id)">
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
            <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge-item">
              入队申请
            </el-badge>
          </template>
          <el-table :data="applications" border stripe>
            <el-table-column prop="nickname" label="选手昵称" width="150">
              <template #default="scope">
                <span class="clickable" @click="goToUserInfo(scope.row.userId)">{{ scope.row.nickname }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="university" label="选手所属学校" width="180" />
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

const uploadHeaders = { Authorization: `Bearer ${userStore.token}` };

// 战队资料表单
const teamForm = ref({
  id: null,
  name: '',
  logo: '',
  description: '',
  gameProject: ''
});

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

const fetchTeamInfo = async () => {
  if (!teamId.value) return;
  try {
    const res = await request.get('/team/list');
    const t = res.data.find(item => item.id == teamId.value);
    if (t) {
      // 确保logo路径是完整的URL
      if (t.logo && !t.logo.startsWith('http')) {
        t.logo = `http://localhost:8080${t.logo}`;
      }
      teamForm.value = { ...t };
    }
  } catch (err) {
    console.error(err);
  }
};

const handleLogoSuccess = (res) => {
  if (res.code === 200) {
    // 确保图片路径是完整的URL
    if (res.data && !res.data.startsWith('http')) {
      teamForm.value.logo = `http://localhost:8080${res.data}`;
    } else {
      teamForm.value.logo = res.data;
    }
    ElMessage.success('Logo 上传成功，请点击更新保存');
  } else {
    ElMessage.error(res.msg || '上传失败');
  }
};

const handleUpdateTeamInfo = async () => {
  try {
    await request.post('/team/update', teamForm.value);
    ElMessage.success('战队资料更新成功');
    fetchTeamInfo();
  } catch (err) {
    console.error(err);
  }
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
  if (name === 'info') fetchTeamInfo();
  if (name === 'members') fetchMembers();
  if (name === 'applications') fetchApplications();
};

const handleReview = async (appId, status) => {
  try {
    await request.post(`/team/applications/review/${appId}?status=${status}`);
    ElMessage.success('操作成功');
    // 刷新申请列表
    fetchApplications();
    // 刷新成员列表
    fetchMembers();
    // 触发全局事件，通知Header刷新申请数红点
    window.dispatchEvent(new CustomEvent('refresh-pending-count'));
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
    router.push('/team');
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
    fetchTeamInfo();
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
.avatar-uploader {
  position: relative;
  cursor: pointer;
}
.logo-preview {
  position: relative;
  width: 200px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}
.logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.logo-edit-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-uploader:hover .logo-edit-mask {
  opacity: 1;
}
.upload-tip {
  font-size: 12px;
  color: #999;
}
</style>
