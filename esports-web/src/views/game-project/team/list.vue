<template>
  <div class="team-page full-container">
    <div class="page-header">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="gameProject">{{ gameProjectDisplayName }}</el-breadcrumb-item>
        <el-breadcrumb-item>战队列表</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="content-box">
      <div class="filter-bar">
        <el-form :inline="true" :model="queryForm">
          <el-form-item v-if="!gameProject" label="竞技项目">
            <el-select v-model="queryForm.gameProject" placeholder="全部项目" clearable @change="fetchTeams" style="width: 160px">
              <el-option label="英雄联盟" value="LOL" />
              <el-option label="王者荣耀" value="王者荣耀" />
              <el-option label="CS2" value="CS2" />
              <el-option label="无畏契约" value="无畏契约" />
            </el-select>
          </el-form-item>
          <el-form-item label="高校名称">
            <el-input v-model="queryForm.university" placeholder="输入搜索..." clearable @keyup.enter="fetchTeams" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchTeams">搜索</el-button>
            <el-button 
              v-if="userStore.token" 
              type="success" 
              @click="showCreateDialog = true"
            >创建战队</el-button>
            <el-button 
              v-if="userStore.token" 
              class="btn-my-team"
              @click="goToTeamManage"
            >我的战队</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-loading="loading" class="team-grid">
        <el-row :gutter="20">
          <el-col :span="6" v-for="team in teams" :key="team.id">
            <el-card class="team-card" :body-style="{ padding: '0px' }" shadow="hover" @click="goToTeamDetail(team.id)">
              <div class="team-img-box">
                <img :src="team.logo || 'https://via.placeholder.com/300x200/eee/999?text=TEAM'" class="team-img">
              </div>
              <div class="team-detail">
                <h3 class="name">{{ team.name }}</h3>
                <div class="meta">
                  <span>{{ team.university }}</span>
                  <el-tag size="small" effect="plain">{{ team.gameProject }}</el-tag>
                </div>
                <div class="actions">
                  <span class="m-count"><el-icon><User /></el-icon> {{ team.memberCount }}人</span>
                  <el-button type="primary" link @click.stop="joinTeam(team.id)">申请加入</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="teams.length === 0" description="暂无战队信息" />
      </div>
    </div>

    <!-- 创建对话框保持白色简约 -->
    <el-dialog v-model="showCreateDialog" title="创建新战队" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="战队名称" prop="name">
          <el-input v-model="createForm.name" maxlength="10" show-word-limit placeholder="起一个响亮的队名" />
        </el-form-item>
        <el-form-item label="竞技项目" prop="gameProject">
          <el-select v-model="createForm.gameProject" class="w-full" :disabled="!!gameProject">
            <el-option label="英雄联盟" value="LOL" />
            <el-option label="王者荣耀" value="王者荣耀" />
            <el-option label="CS2" value="CS2" />
            <el-option label="无畏契约" value="无畏契约" />
          </el-select>
        </el-form-item>
        <el-form-item label="战队简介">
          <el-input v-model="createForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTeam">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../../store/user';
import { useGameProjectStore } from '../../../store/gameProject';
import request from '../../../utils/request';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const gameProjectStore = useGameProjectStore();

const gameProject = computed(() => route.params.gameProject?.toUpperCase() || null);
const gameProjectDisplayName = computed(() => {
  if (!gameProject.value) return '';
  const project = gameProjectStore.getGameProjectByName(gameProject.value);
  return project ? project.displayName : gameProject.value;
});

const pageTitle = computed(() => {
  return gameProject.value ? `${gameProjectDisplayName.value} 战队信息` : '战队信息';
});

const teams = ref([]);
const loading = ref(false);
const showCreateDialog = ref(false);
const createFormRef = ref(null);

const queryForm = ref({
  gameProject: gameProject.value || '',
  university: ''
});

const createForm = ref({
  name: '',
  university: userStore.userInfo.university,
  gameProject: gameProject.value || '',
  description: '',
  leaderId: userStore.userInfo.id
});

const createRules = {
  name: [
    { required: true, message: '请输入战队名称', trigger: 'blur' }
  ],
  gameProject: [
    { required: true, message: '请选择竞技项目', trigger: 'change' }
  ]
};

const fetchTeams = async () => {
  loading.value = true;
  try {
    const params = { ...queryForm.value };
    if (!params.gameProject) {
      delete params.gameProject;
    }
    const res = await request.get('/team/list', { params });
    teams.value = res.data.map(team => {
      if (team.logo && !team.logo.startsWith('http')) {
        team.logo = `http://localhost:8081${team.logo}`;
      }
      return team;
    });
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleCreateTeam = async () => {
  if (!createFormRef.value) return;
  
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/team/create', createForm.value);
        ElMessage.success('战队创建成功');
        showCreateDialog.value = false;
        fetchTeams();
      } catch (err) {
        ElMessage.error(err.response?.data?.msg || '创建失败');
      }
    }
  });
};

const joinTeam = async (teamId) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    return;
  }
  try {
    await request.post(`/team/join/${teamId}`, null, { params: { userId: userStore.userInfo.id } });
    ElMessage.success('申请已发送');
  } catch (err) {
    console.error(err);
  }
};

const goToTeamDetail = (teamId) => {
  if (gameProject.value) {
    router.push(`/${gameProject.value}/team/${teamId}`);
  } else {
    router.push(`/team/${teamId}`);
  }
};

const goToTeamManage = () => {
  if (gameProject.value) {
    router.push(`/${gameProject.value}/team/manage`);
  } else {
    router.push('/team/manage');
  }
};

onMounted(() => {
  fetchTeams();
});
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #333;
}

.content-box {
  background-color: var(--bg-white);
  padding: 25px;
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.filter-bar {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.team-card {
  margin-bottom: 20px;
}

.team-img-box {
  height: 180px;
  overflow: hidden;
}

.team-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.team-detail {
  padding: 15px;
}

.name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #666;
  margin-bottom: 15px;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f2f5;
  padding-top: 12px;
}

.m-count {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.w-full { width: 100%; }

.btn-my-team {
  background-color: #ff4d4f !important;
  border-color: #ff4d4f !important;
  color: #ffffff !important;
  font-weight: 600;
}

.btn-my-team:hover {
  background-color: #ffdbdb !important;
  color: #ff1f1f !important;
}
</style>