<template>
  <div class="team-page full-container">
    <div class="page-header">
      <h1 class="page-title">CS2 战队信息</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/cs2' }">CS2首页</el-breadcrumb-item>
        <el-breadcrumb-item>战队列表</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="content-box">
      <div class="filter-bar">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="竞技项目">
            <el-select v-model="queryForm.gameProject" placeholder="全部项目" clearable @change="fetchTeams" style="width: 160px">
              <el-option label="CS2" value="CS2" />
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
              @click="$router.push('/cs2/team/manage')"
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
    <el-dialog v-model="showCreateDialog" title="创建CS2战队" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="战队名称" prop="name">
          <el-input v-model="createForm.name" maxlength="10" show-word-limit placeholder="起一个响亮的队名" />
        </el-form-item>
        <el-form-item label="竞技项目" prop="gameProject">
          <el-select v-model="createForm.gameProject" class="w-full">
            <el-option label="CS2" value="CS2" />
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
import { ref, onMounted } from 'vue';
import { useUserStore } from '../../../store/user';
import { useRouter } from 'vue-router';
import request from '../../../utils/request';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const router = useRouter();
const teams = ref([]);
const loading = ref(false);
const showCreateDialog = ref(false);
const createFormRef = ref(null);

const queryForm = ref({
  gameProject: 'CS2',
  university: ''
});

const createForm = ref({
  name: '',
  university: userStore.userInfo.university,
  gameProject: 'CS2',
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
    const res = await request.get('/team/list', { params: queryForm.value });
    teams.value = res.data.map(team => {
      // 确保logo路径是完整的URL
      if (team.logo && !team.logo.startsWith('http')) {
        team.logo = `http://localhost:8080${team.logo}`;
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
  router.push(`/cs2/team/${teamId}`);
};

onMounted(fetchTeams);
</script>

<style scoped>
.team-page { padding: 20px 0; }

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.content-box {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.filter-bar {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.btn-my-team {
  margin-left: 10px;
}

.team-grid {
  margin-top: 20px;
}

.team-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.team-card:hover {
  transform: translateY(-5px);
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

.team-detail .name {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.team-detail .meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 12px;
  color: #666;
}

.team-detail .actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.m-count {
  font-size: 12px;
  color: #999;
}
</style>