<template>
  <div class="wzry-team-management-page">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/wzry' }">WZRY首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/wzry/team' }">战队列表</el-breadcrumb-item>
        <el-breadcrumb-item>战队管理</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>战队管理中心</h1>
    </div>

    <el-card v-loading="loading" shadow="never" class="team-info-card">
      <div class="team-header">
        <el-avatar :size="80" :src="team.logo || 'https://via.placeholder.com/150/eee/999?text=TEAM'" />
        <div class="team-info">
          <h2 class="team-name">{{ team.name }}</h2>
          <p class="team-uni"><el-icon><School /></el-icon> {{ team.university }}</p>
          <div class="team-actions">
            <el-button type="primary" @click="editTeamInfo">编辑战队信息</el-button>
            <el-button type="primary" @click="goToTeamDetail">战队详情页</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="team-members-card" style="margin-top: 30px;">
      <template #header>
        <div class="card-header">
          <h3>队员管理</h3>
          <el-button type="primary" @click="inviteMember">邀请成员</el-button>
        </div>
      </template>
      <el-table :data="members" border stripe>
        <el-table-column prop="nickname" label="队员昵称" width="150">
          <template #default="scope">
            <span class="clickable" @click="goToUserInfo(scope.row.userId)">{{ scope.row.nickname }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="university" label="所属大学" width="180" />
        <el-table-column label="队内角色" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.role === 'ROLE_LEADER'" type="danger">队长</el-tag>
            <el-tag v-else type="info">正式成员</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.role !== 'ROLE_LEADER'" type="danger" size="small" @click="removeMember(scope.row.userId)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="team-applications-card" style="margin-top: 30px;">
      <template #header>
        <div class="card-header">
          <h3>入队申请</h3>
        </div>
      </template>
      <el-table :data="applications" border stripe>
        <el-table-column prop="nickname" label="选手昵称" width="150">
          <template #default="scope">
            <span class="clickable" @click="goToUserInfo(scope.row.userId)">{{ scope.row.nickname }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="university" label="选手所属学校" width="180" />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button type="success" size="small" @click="handleReview(scope.row.id, 1)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReview(scope.row.id, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑战队信息弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑战队信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="战队名称">
          <el-input v-model="editForm.name" placeholder="请输入战队名称" />
        </el-form-item>
        <el-form-item label="所属高校">
          <el-input v-model="editForm.university" placeholder="请输入所属高校" />
        </el-form-item>
        <el-form-item label="战队简介">
          <el-input v-model="editForm.description" type="textarea" placeholder="请输入战队简介" :rows="3" />
        </el-form-item>
        <el-form-item label="战队Logo">
          <el-upload
            class="avatar-uploader"
            :action="`${env.apiBaseURL}/user/upload`"
            :show-file-list="false"
            :on-success="handleLogoUpload"
            :before-upload="beforeLogoUpload"
          >
            <img v-if="editForm.logo" :src="editForm.logo" class="avatar">
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateTeamInfo">确定</el-button>
      </template>
    </el-dialog>

    <!-- 邀请成员弹窗 -->
    <el-dialog v-model="inviteDialogVisible" title="邀请成员" width="400px">
      <el-form :model="inviteForm" label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model="inviteForm.userId" placeholder="请输入要邀请的用户ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendInvitation">发送邀请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '../../../utils/request';
import { useUserStore } from '../../../store/user';
import env from '../../../config/env';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const team = ref({});
const members = ref([]);
const applications = ref([]);

const editDialogVisible = ref(false);
const editForm = ref({});

const inviteDialogVisible = ref(false);
const inviteForm = ref({ userId: '' });

const fetchTeamInfo = async () => {
  loading.value = true;
  try {
    // 获取用户管理的战队
    const teamRes = await request.get(`/team/leader/${userStore.userInfo.id}`, {
      params: { gameProject: 'WZRY' }
    });
    if (teamRes.data) {
      team.value = teamRes.data;
      // 获取战队成员
      const membersRes = await request.get(`/team/members/${team.value.id}`);
      members.value = membersRes.data || [];
      // 获取入队申请
      const applicationsRes = await request.get(`/team/applications/${team.value.id}`);
      applications.value = applicationsRes.data || [];
    } else {
      ElMessage.warning('您还不是任何战队的队长');
      router.push('/wzry/team');
    }
  } catch (err) {
    console.error('获取战队信息失败', err);
  } finally {
    loading.value = false;
  }
};

const editTeamInfo = () => {
  editForm.value = { ...team.value };
  editDialogVisible.value = true;
};

const updateTeamInfo = async () => {
  try {
    await request.put('/team', {
      id: team.value.id,
      name: editForm.value.name,
      university: editForm.value.university,
      description: editForm.value.description,
      logo: editForm.value.logo
    });
    ElMessage.success('战队信息更新成功');
    editDialogVisible.value = false;
    fetchTeamInfo();
  } catch (err) {
    console.error('更新战队信息失败', err);
  }
};

const handleLogoUpload = (response, file) => {
  if (response.success) {
    // 确保logo路径是完整的URL
    let logoUrl = response.data;
    if (logoUrl && !logoUrl.startsWith('http')) {
      logoUrl = env.getFullApiUrl(logoUrl);
    }
    editForm.value.logo = logoUrl;
    ElMessage.success('Logo上传成功');
  } else {
    ElMessage.error('Logo上传失败');
  }
};

const beforeLogoUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isJpgOrPng) {
    ElMessage.error('只能上传JPG/PNG格式的图片');
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB');
  }
  return isJpgOrPng && isLt2M;
};

const inviteMember = () => {
  inviteForm.value.userId = '';
  inviteDialogVisible.value = true;
};

const sendInvitation = async () => {
  try {
    await request.post('/team/invite', {
      teamId: team.value.id,
      userId: inviteForm.value.userId
    });
    ElMessage.success('邀请已发送');
    inviteDialogVisible.value = false;
  } catch (err) {
    console.error('发送邀请失败', err);
  }
};

const removeMember = async (userId) => {
  try {
    await request.delete(`/team/member/${team.value.id}/${userId}`);
    ElMessage.success('成员已移除');
    fetchTeamInfo();
  } catch (err) {
    console.error('移除成员失败', err);
  }
};

const handleReview = async (applicationId, status) => {
  try {
    await request.put('/team/application/review', {
      id: applicationId,
      status: status
    });
    ElMessage.success(status === 1 ? '申请已通过' : '申请已拒绝');
    fetchTeamInfo();
  } catch (err) {
    console.error('审核申请失败', err);
  }
};

const goToUserInfo = (userId) => {
  router.push(`/user/info/${userId}`);
};

const goToTeamDetail = () => {
  router.push(`/wzry/team/${team.value.id}`);
};

onMounted(() => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  fetchTeamInfo();
});
</script>

<style scoped>
.wzry-team-management-page {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 10px 0 0 0;
}

.team-info-card {
  margin-bottom: 30px;
}

.team-header {
  display: flex;
  gap: 30px;
  align-items: center;
}

.team-info {
  flex: 1;
}

.team-name {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.team-uni {
  font-size: 16px;
  color: #999;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.team-actions {
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #999;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.3s;
}

.avatar-uploader-icon:hover {
  border-color: var(--primary);
}

.clickable {
  cursor: pointer;
  color: var(--primary);
}

.clickable:hover {
  text-decoration: underline;
}
</style>