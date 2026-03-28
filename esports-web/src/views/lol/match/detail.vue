<template>
  <div class="lol-match-detail-page full-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/lol' }">LOL首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/lol/match' }">约战大厅</el-breadcrumb-item>
        <el-breadcrumb-item>约战详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-loading="loading">
      <el-card v-if="room" shadow="never" class="match-header-card">
        <div class="m-status-banner" :class="'status-' + room.status">
          {{ getStatusText(room.status) }}
        </div>
        
        <div class="m-content">
          <h1 class="m-title">{{ room.title }}</h1>
          <div class="m-tags">
            <el-tag size="large">{{ room.gameProject }}</el-tag>
            <el-tag size="large" :type="room.type === 1 ? 'warning' : 'success'">
              {{ room.type === 1 ? '线下约战' : '线上约战' }}
            </el-tag>
          </div>
          
          <div class="m-time">
            <el-icon><Calendar /></el-icon> 开赛时间：{{ formatDate(room.matchTime) }}
          </div>
          
          <div v-if="room.type === 1" class="m-location">
            <el-icon><Location /></el-icon> 约战地点：{{ room.location }}
          </div>
          
          <div class="m-desc">
            <h3>约战说明：</h3>
            <p>{{ room.description || '发起人没有留下额外的说明...' }}</p>
          </div>
          
          <!-- 取消约战按钮 -->
          <div v-if="userStore.userInfo.id === room.hostId && room.status === 0" class="m-actions">
            <el-button type="danger" @click="cancelMatch">取消约战</el-button>
          </div>
        </div>
      </el-card>

      <!-- 对战双方展示区 -->
      <div v-if="room" class="vs-section">
        <!-- 发起方 -->
        <el-card shadow="hover" class="team-card host-team" @click="$router.push(`/lol/team/${room.hostTeamId}`)">
          <div class="team-role">发起方</div>
          <el-avatar :size="80" :src="(room.hostTeamLogo && room.hostTeamLogo.startsWith('http')) ? room.hostTeamLogo : room.hostTeamLogo ? `http://localhost:8081${room.hostTeamLogo}` : 'https://via.placeholder.com/150/ff4d4f/ffffff?text=HOST'" />
          <h2 class="team-name">{{ room.hostTeamName }}</h2>
          <p class="team-uni"><el-icon><School /></el-icon> {{ hostLeaderUniversity }}</p>
          <div class="host-badge">队长: {{ room.hostLeaderNickname || room.hostId }}</div>
        </el-card>

        <div class="vs-icon">
          <img src="../../../assets/vue.svg" alt="VS" style="width:60px; filter: grayscale(1) opacity(0.5); transform: rotate(45deg);" />
        </div>

        <!-- 应战方 -->
        <el-card v-if="room.guestTeamId" shadow="hover" class="team-card guest-team" @click="$router.push(`/lol/team/${room.guestTeamId}`)">
          <div class="team-role">应战方</div>
          <el-avatar :size="80" :src="(room.guestTeamLogo && room.guestTeamLogo.startsWith('http')) ? room.guestTeamLogo : room.guestTeamLogo ? `http://localhost:8081${room.guestTeamLogo}` : 'https://via.placeholder.com/150/1890ff/ffffff?text=GUEST'" />
          <h2 class="team-name">{{ room.guestTeamName }}</h2>
          <p class="team-uni"><el-icon><School /></el-icon> {{ guestLeaderUniversity }}</p>
          <div class="guest-badge">队长: {{ room.guestLeaderNickname || room.guestId }}</div>
        </el-card>

        <!-- 未应战状态 -->
        <el-card v-else shadow="hover" class="team-card empty-guest" @click="handleJoinMatch">
          <div class="waiting-box">
            <el-icon :size="48"><UserFilled /></el-icon>
            <p>暂无应战队伍</p>
            <el-button type="primary" size="large">我来应战</el-button>
          </div>
        </el-card>
      </div>

      <!-- 应战对话框 -->
      <el-dialog v-model="joinDialogVisible" title="应战约战" width="500px">
        <el-form :model="joinForm" label-width="80px">
          <el-form-item label="选择战队">
            <el-select v-model="joinForm.teamId" placeholder="请选择您的战队">
              <el-option v-for="team in myTeams" :key="team.id" :label="team.name" :value="team.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="joinDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitJoin">确认应战</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../../../utils/request';
import { useUserStore } from '../../../store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const room = ref(null);
const myTeams = ref([]);
const joinDialogVisible = ref(false);
const joinForm = ref({ teamId: '' });

const roomId = computed(() => route.params.id);

const hostLeaderUniversity = computed(() => {
  return room.value?.hostUniversity || '未知高校';
});

const guestLeaderUniversity = computed(() => {
  return room.value?.guestUniversity || '未知高校';
});

const fetchDetail = async () => {
  loading.value = true;
  try {
    const res = await request.get(`/match-room/${roomId.value}`);
    room.value = res.data;
  } catch (err) {
    console.error('获取约战详情失败', err);
  } finally {
    loading.value = false;
  }
};

const fetchMyTeams = async () => {
  if (!userStore.token) return;
  try {
    const res = await request.get(`/team/leader/${userStore.userInfo.id}`, {
      params: { gameProject: 'LOL' }
    });
    if (res.data) {
      myTeams.value = [res.data];
    }
  } catch (err) {
    console.error('获取我的战队失败', err);
  }
};

const handleJoinMatch = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  if (myTeams.value.length === 0) {
    ElMessage.warning('您还没有创建战队，请先创建战队');
    router.push('/lol/team/manage');
    return;
  }
  joinForm.value.teamId = myTeams.value[0].id;
  joinDialogVisible.value = true;
};

const submitJoin = async () => {
  try {
    await request.post(`/match-room/accept/${roomId.value}`, {
      guestTeamId: joinForm.value.teamId,
      guestId: userStore.userInfo.id
    });
    ElMessage.success('应战成功！');
    joinDialogVisible.value = false;
    fetchDetail(); // 刷新数据
  } catch (err) {
    console.error(err);
  }
};

const cancelMatch = async () => {
  try {
    // 弹出确认框
    await ElMessageBox.confirm(
      '确定要取消该约战吗？取消后将无法恢复。',
      '取消约战',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await request.post(`/match-room/cancel/${roomId.value}`, null, {
      params: { userId: userStore.userInfo.id }
    });
    ElMessage.success('约战已取消');
    // 跳转到约战大厅
    router.push('/lol/match');
  } catch (err) {
    if (err !== 'cancel') {
      console.error(err);
      ElMessage.error('取消约战失败，请重试');
    }
  }
};

const getStatusText = (status) => {
  const statusMap = {
    0: '招募中',
    1: '已应战',
    2: '已结束',
    3: '已取消'
  };
  return statusMap[status] || '未知状态';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  // 添加上午/下午标识
  const period = hours < 12 ? '上午' : '下午';
  // 使用24小时制显示时间
  return `${year}-${String(month).padStart(2,'0')}-${String(day).padStart(2,'0')} ${period}${String(hours).padStart(2,'0')}:${String(minutes).padStart(2,'0')}`;
};

onMounted(() => {
  fetchDetail();
  fetchMyTeams();
});
</script>

<style scoped>
.lol-match-detail-page {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 20px;
}

.match-header-card {
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  margin-bottom: 40px;
}

.m-status-banner {
  position: absolute;
  top: 20px;
  right: -30px;
  background: var(--primary);
  color: white;
  padding: 5px 40px;
  transform: rotate(45deg);
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.m-status-banner.status-0 { background: #67c23a; }

.m-status-banner.status-1 { background: #e6a23c; }

.m-status-banner.status-2 { background: #909399; }

.m-content {
  padding: 20px;
}

.m-title {
  font-size: 28px;
  color: #333;
  margin: 0 0 20px 0;
  width: 80%;
}

.m-tags {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.m-time {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.m-location {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.m-desc {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
}

.m-desc h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.m-desc p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.m-actions {
  margin-top: 20px;
}

/* 对战双方区域 */
.vs-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  padding: 20px;
}

.team-card {
  width: 320px;
  text-align: center;
  border-radius: 12px;
  position: relative;
  cursor: pointer;
  transition: transform 0.3s;
}

.team-card:hover {
  transform: translateY(-5px);
}

.team-role {
  position: absolute;
  top: 0;
  left: 0;
  background: #f0f2f5;
  padding: 4px 12px;
  border-radius: 12px 0 12px 0;
  font-size: 12px;
}

.team-card.host-team .team-role { background: #fff1f0; color: #ff4d4f; }
.team-card.guest-team .team-role { background: #e6f7ff; color: #1890ff; }

.team-name {
  font-size: 20px;
  font-weight: bold;
  margin: 15px 0 5px 0;
  color: #333;
}

.team-uni {
  font-size: 14px;
  color: #999;
  margin: 0 0 15px 0;
}

.host-badge { font-size: 12px; color: #ff4d4f; background: #fff1f0; display: inline-block; padding: 2px 10px; border-radius: 10px; }
.guest-badge { font-size: 12px; color: #1890ff; background: #e6f7ff; display: inline-block; padding: 2px 10px; border-radius: 10px; }

.vs-icon {
  font-size: 48px;
  color: #999;
  margin: 0 20px;
}

.empty-guest {
  cursor: default;
}

.empty-guest:hover {
  transform: none;
}

.waiting-box {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.waiting-box p {
  color: #999;
  margin: 0;
}
</style>