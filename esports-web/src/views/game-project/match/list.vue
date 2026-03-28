<template>
  <div class="match-page full-container">
    <div class="page-header">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="gameProject">{{ gameProjectDisplayName }}</el-breadcrumb-item>
        <el-breadcrumb-item>约战大厅</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="match-content">
      <!-- 约战筛选与发起区 -->
      <div class="action-bar">
        <div class="filter-group">
          <el-radio-group v-model="filterProject" @change="fetchRooms">
            <el-radio-button label="全部">全部状态</el-radio-button>
            <el-radio-button :label="0">待匹配</el-radio-button>
            <el-radio-button :label="1">已匹配</el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" size="large" icon="Plus" @click="handleCreateClick">发起约战</el-button>
      </div>

      <!-- 约战房间列表 -->
      <div v-loading="loading" class="room-list">
        <el-row :gutter="20">
          <el-col :span="8" v-for="room in rooms" :key="room.id">
            <el-card class="room-card" shadow="hover" @click="goToMatchDetail(room.id)">
              <div class="room-header">
                <el-tag :type="room.type === 1 ? 'warning' : 'success'" size="small" effect="dark">
                  {{ room.type === 1 ? '线下' : '线上' }}
                </el-tag>
                <span class="game-tag">{{ room.gameProject }}</span>
              </div>
              <h3 class="room-title">{{ room.title }}</h3>
              
              <div class="room-info">
                <div class="info-item">
                  <span class="label">发起战队：</span>
                  <span class="value host-team">{{ room.hostTeamName }}</span>
                  <el-tag v-if="room.hostUniversity" size="small" effect="plain" class="university-tag">{{ room.hostUniversity }}</el-tag>
                </div>
                <div v-if="room.guestTeamName" class="info-item">
                  <span class="label">应战战队：</span>
                  <span class="value">{{ room.guestTeamName }}</span>
                  <el-tag v-if="room.guestUniversity" size="small" effect="plain" class="university-tag">{{ room.guestUniversity }}</el-tag>
                </div>
                <div class="info-item">
                  <span class="label">开赛时间：</span>
                  <span class="value">{{ formatDate(room.matchTime) }}</span>
                </div>
                <div v-if="room.type === 1" class="info-item">
                  <span class="label">地点：</span>
                  <span class="value">{{ room.location }}</span>
                </div>
              </div>

              <div class="room-footer">
                <el-button 
                  v-if="room.status === 0" 
                  type="primary" 
                  class="join-btn" 
                  @click.stop="handleJoinClick(room)"
                >立即应战</el-button>
                <el-button 
                  v-else-if="room.status === 1" 
                  type="warning" 
                  class="join-btn" 
                  disabled
                >等待开赛</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="rooms.length === 0" description="暂无约战房间，快去发起一个吧！" />
      </div>
    </div>

    <!-- 发起约战对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建约战房间" width="500px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="我的战队" required>
          <el-select v-model="selectedTeamId" placeholder="请选择您的战队" @change="onTeamChange">
            <el-option 
              v-for="team in myTeams" 
              :key="team.id" 
              :label="team.name" 
              :value="team.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="约战项目" required>
          <el-select v-model="createForm.gameProject" placeholder="请选择游戏" class="w-full" :disabled="!!gameProject">
            <el-option label="英雄联盟" value="LOL" />
            <el-option label="王者荣耀" value="王者荣耀" />
            <el-option label="CS2" value="CS2" />
            <el-option label="无畏契约" value="无畏契约" />
          </el-select>
        </el-form-item>
        <el-form-item label="约战标题" required>
          <el-input v-model="createForm.title" placeholder="例如：XX大学寻求白金水平约战" />
        </el-form-item>
        <el-form-item label="约战类型">
          <el-radio-group v-model="createForm.type">
            <el-radio :label="0">线上对决</el-radio>
            <el-radio :label="1">线下约战</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开赛时间" required>
          <el-date-picker
            v-model="createForm.matchTime"
            type="datetime"
            placeholder="选择开赛时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item v-if="createForm.type === 1" label="约战地点" required>
          <el-input v-model="createForm.location" placeholder="具体的网咖或电竞馆名称" />
        </el-form-item>
        <el-form-item label="备注信息">
          <el-input v-model="createForm.description" type="textarea" :rows="2" placeholder="可输入备注或直播间链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateRoom">确认发布</el-button>
      </template>
    </el-dialog>

    <!-- 应战选择对话框 -->
    <el-dialog v-model="joinDialogVisible" title="选择应战战队" width="400px">
      <el-form label-width="80px">
        <el-form-item label="我的战队" required>
          <el-select v-model="selectedJoinTeamId" placeholder="选择您要派出的战队" class="w-full">
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
        <el-button @click="joinDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmJoinRoom">确认应战</el-button>
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
  return gameProject.value ? `${gameProjectDisplayName.value} 约战大厅` : '约战大厅';
});

const loading = ref(false);
const filterProject = ref('全部');
const rooms = ref([]);
const myTeams = ref([]);

// 创建弹窗相关
const createDialogVisible = ref(false);
const selectedTeamId = ref(null);
const createForm = ref({
  title: '',
  gameProject: gameProject.value || '',
  type: 0,
  matchTime: '',
  location: '',
  description: '',
  hostTeamId: null,
  hostTeamName: '',
  hostId: userStore.userInfo.id,
  hostUniversity: userStore.userInfo.university
});

// 应战弹窗相关
const joinDialogVisible = ref(false);
const selectedJoinTeamId = ref(null);
const currentRoomId = ref(null);

const fetchRooms = async () => {
  loading.value = true;
  try {
    const params = { gameProject: gameProject.value };
    if (filterProject.value !== '全部') {
      params.status = filterProject.value;
    }
    const res = await request.get('/match-room/list', { params });
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
    myTeams.value = res.data || [];
  } catch (err) {
    console.error(err);
  }
};

const handleCreateClick = () => {
  if (!userStore.token) return ElMessage.warning('请先登录');
  if (myTeams.value.length === 0) return ElMessage.warning('您需要先加入或创建一个战队');
  createDialogVisible.value = true;
};

const onTeamChange = (val) => {
  const team = myTeams.value.find(t => t.id === val);
  if (team) {
    createForm.value.hostTeamId = team.id;
    createForm.value.hostTeamName = team.name;
    if (!gameProject.value) {
      createForm.value.gameProject = team.gameProject;
    }
  }
};

const submitCreateRoom = async () => {
  if (!createForm.value.hostTeamId || !createForm.value.title || !createForm.value.matchTime) {
    return ElMessage.warning('请填写完整信息');
  }
  try {
    await request.post('/match-room/create', createForm.value);
    ElMessage.success('约战房间已发布');
    createDialogVisible.value = false;
    fetchRooms();
  } catch (err) {
    console.error(err);
  }
};

const handleJoinClick = (room) => {
  if (!userStore.token) return ElMessage.warning('请先登录');
  if (room.hostId === userStore.userInfo.id) return ElMessage.warning('不能应战自己的约战');
  currentRoomId.value = room.id;
  joinDialogVisible.value = true;
};

const confirmJoinRoom = async () => {
  if (!selectedJoinTeamId.value) return ElMessage.warning('请选择应战战队');
  const team = myTeams.value.find(t => t.id === selectedJoinTeamId.value);
  try {
    await request.post(`/match-room/join/${currentRoomId.value}`, null, {
      params: { 
        guestTeamId: team.id,
        guestTeamName: team.name
      }
    });
    ElMessage.success('应战成功！请在约定时间准时对决');
    joinDialogVisible.value = false;
    fetchRooms();
  } catch (err) {
    console.error(err);
  }
};

const goToMatchDetail = (roomId) => {
  if (gameProject.value) {
    router.push(`/${gameProject.value}/match/${roomId}`);
  } else {
    router.push(`/match/${roomId}`);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  // 添加上午/下午标识
  const period = hours < 12 ? '上午' : '下午';
  // 使用24小时制显示时间
  return `${month}月${day}日 ${period}${String(hours).padStart(2,'0')}:${String(minutes).padStart(2,'0')}`;
};

onMounted(() => {
  fetchRooms();
  fetchMyTeams();
});
</script>

<style scoped>
.match-page { padding: 20px 0; }
.page-header { margin-bottom: 30px; }
.page-title { font-size: 28px; font-weight: bold; color: #333; margin-bottom: 10px; }

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.room-card {
  margin-bottom: 20px;
  border-radius: 12px;
  transition: transform 0.3s;
}
.room-card:hover { transform: translateY(-5px); }

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.game-tag { font-size: 12px; color: #999; font-weight: bold; }

.room-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  height: 50px;
  overflow: hidden;
}

.room-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.info-item {
  margin-bottom: 8px;
  font-size: 14px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.info-item .label { color: #999; flex-shrink: 0; }
.info-item .value { color: #333; font-weight: 500; }
.host-team { color: var(--primary); }
.university-tag { margin-left: 5px; }

.room-footer { text-align: center; }
.join-btn { width: 100%; border-radius: 20px; font-weight: bold; }

.w-full { width: 100%; }
</style>