<template>
  <div class="match-page full-container">
    <div class="page-header">
      <h1 class="page-title">CS2 约战大厅</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/cs2' }">CS2首页</el-breadcrumb-item>
        <el-breadcrumb-item>约战大厅</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="content-box">
      <div class="filter-bar">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="竞技项目">
            <el-select v-model="queryForm.gameProject" placeholder="全部项目" clearable @change="fetchRooms" style="width: 160px">
              <el-option label="CS2" value="CS2" />
            </el-select>
          </el-form-item>
          <el-form-item label="约战类型">
            <el-select v-model="queryForm.type" placeholder="全部类型" clearable @change="fetchRooms" style="width: 120px">
              <el-option label="线上" value="0" />
              <el-option label="线下" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="约战状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable @change="fetchRooms" style="width: 120px">
              <el-option label="招募中" value="0" />
              <el-option label="已应战" value="1" />
              <el-option label="已结束" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchRooms">搜索</el-button>
            <el-button 
              v-if="userStore.token" 
              type="success" 
              @click="showCreateDialog = true"
            >发布约战</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-loading="loading" class="match-grid">
        <el-row :gutter="20">
          <el-col :span="8" v-for="room in rooms" :key="room.id">
            <el-card class="match-card" shadow="hover" @click="goToMatchDetail(room.id)">
              <div class="match-info">
                <div class="match-header">
                  <h3 class="match-title">{{ room.title }}</h3>
                  <el-tag :type="getStatusType(room.status)">{{ getStatusText(room.status) }}</el-tag>
                </div>
                <div class="match-meta">
                  <el-tag size="small">{{ room.gameProject }}</el-tag>
                  <el-tag size="small" :type="room.type === 1 ? 'warning' : 'success'">
                    {{ room.type === 1 ? '线下约战' : '线上约战' }}
                  </el-tag>
                  <span class="match-time"><el-icon><Calendar /></el-icon> 开赛时间：{{ formatDate(room.matchTime) }}</span>
                </div>
                <div v-if="room.type === 1" class="match-location">
                  <el-icon><Location /></el-icon> {{ room.location }}
                </div>
                <p class="match-desc">{{ room.description || '无详细说明' }}</p>
                <div class="match-teams">
                  <div class="team-info">
                    <span class="team-label">发起方：</span>
                    <span class="team-name">{{ room.hostTeamName }}</span>
                  </div>
                  <div class="team-info">
                    <span class="team-label">应战方：</span>
                    <span class="team-name">{{ room.guestTeamName || '等待应战' }}</span>
                  </div>
                </div>
                <div class="match-actions">
                  <el-button type="primary" size="small" @click.stop="goToMatchDetail(room.id)">查看详情</el-button>
                  <el-button 
                    v-if="room.status === 0 && userStore.token && room.hostId !== userStore.userInfo.id" 
                    type="success" 
                    size="small" 
                    @click.stop="joinMatch(room.id)"
                  >立即应战</el-button>
                  <el-button 
                    v-else-if="room.status === 1" 
                    type="warning" 
                    size="small" 
                    disabled
                  >等待开赛</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="rooms.length === 0" description="暂无约战信息" />
      </div>
    </div>

    <!-- 创建约战对话框 -->
    <el-dialog v-model="showCreateDialog" title="发布CS2约战" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="约战标题">
          <el-input v-model="createForm.title" placeholder="请输入约战标题" />
        </el-form-item>
        <el-form-item label="竞技项目">
          <el-select v-model="createForm.gameProject" class="w-full">
            <el-option label="CS2" value="CS2" />
          </el-select>
        </el-form-item>
        <el-form-item label="约战类型">
          <el-select v-model="createForm.type" class="w-full">
            <el-option label="线上约战" value="0" />
            <el-option label="线下约战" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="开赛时间">
          <el-date-picker
            v-model="createForm.matchTime"
            type="datetime"
            placeholder="选择开赛时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="约战地点" v-if="createForm.type === 1">
          <el-input v-model="createForm.location" placeholder="请输入约战地点" />
        </el-form-item>
        <el-form-item label="约战说明">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入约战说明" />
        </el-form-item>
        <el-form-item label="选择战队">
          <el-select v-model="createForm.hostTeamId" placeholder="选择您的CS2战队" class="w-full">
            <el-option 
              v-for="team in myTeams" 
              :key="team.id" 
              :label="team.name" 
              :value="team.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateMatch">确认发布</el-button>
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
const rooms = ref([]);
const loading = ref(false);
const showCreateDialog = ref(false);
const myTeams = ref([]);

const queryForm = ref({
  gameProject: 'CS2',
  type: '',
  status: ''
});

const createForm = ref({
  title: '',
  gameProject: 'CS2',
  type: 0,
  matchTime: new Date(),
  location: '',
  description: '',
  hostTeamId: '',
  hostId: userStore.userInfo.id
});

const fetchRooms = async () => {
  loading.value = true;
  try {
    const res = await request.get('/match-room/list', { params: queryForm.value });
    rooms.value = res.data || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const fetchMyTeams = async () => {
  if (!userStore.token) return;
  try {
    const res = await request.get(`/team/my/${userStore.userInfo.id}`);
    // 过滤出CS2战队
    myTeams.value = (res.data || []).filter(t => t.gameProject === 'CS2');
  } catch (err) {
    console.error(err);
  }
};

const handleCreateMatch = async () => {
  if (!createForm.value.hostTeamId) {
    ElMessage.warning('请选择您的CS2战队');
    return;
  }
  try {
    // 找到选择的战队
    const selectedTeam = myTeams.value.find(team => team.id === createForm.value.hostTeamId);
    if (selectedTeam) {
      // 设置战队信息
      createForm.value.hostTeamName = selectedTeam.name;
      createForm.value.hostTeamLogo = selectedTeam.logo;
      createForm.value.hostUniversity = selectedTeam.university;
    }
    // 设置队长昵称
    createForm.value.hostLeaderNickname = userStore.userInfo.nickname;
    // 设置发起人ID
    createForm.value.hostId = userStore.userInfo.id;
    await request.post('/match-room/create', createForm.value);
    ElMessage.success('约战发布成功');
    showCreateDialog.value = false;
    fetchRooms();
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '发布失败');
  }
};

const joinMatch = async (roomId) => {
  if (myTeams.value.length === 0) {
    ElMessage.warning('您还没有CS2战队');
    return;
  }
  // 简单处理，使用第一个战队应战
  try {
    await request.post(`/match-room/join/${roomId}`, null, {
      params: {
        guestTeamId: myTeams.value[0].id,
        guestTeamName: myTeams.value[0].name
      }
    });
    ElMessage.success('应战成功');
    fetchRooms();
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '应战失败');
  }
};

const goToMatchDetail = (roomId) => {
  router.push(`/cs2/match/${roomId}`);
};

const getStatusText = (status) => {
  const map = { 0: '招募中', 1: '已应战', 2: '已结束', 3: '已取消' };
  return map[status] || '未知';
};

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'warning', 2: 'info', 3: 'danger' };
  return map[status] || 'info';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`;
};

onMounted(() => {
  fetchRooms();
  fetchMyTeams();
});
</script>

<style scoped>
.match-page { padding: 20px 0; }

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

.match-grid {
  margin-top: 20px;
}

.match-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.match-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.match-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.match-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  flex: 1;
  margin-right: 10px;
}

.match-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.match-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.match-location {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.match-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 15px 0;
}

.match-teams {
  margin-bottom: 15px;
}

.team-info {
  margin-bottom: 5px;
  font-size: 14px;
}

.team-label {
  color: #999;
  margin-right: 5px;
}

.team-name {
  font-weight: 500;
  color: #333;
}

.match-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>