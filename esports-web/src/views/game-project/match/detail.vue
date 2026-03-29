<template>
  <div class="match-detail-page full-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="gameProject" :to="`/${gameProject}/match`">{{ gameProjectDisplayName }}</el-breadcrumb-item>
        <el-breadcrumb-item v-else :to="{ path: '/match' }">约战大厅</el-breadcrumb-item>
        <el-breadcrumb-item>约战详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-loading="loading">
      <el-card v-if="room" shadow="never" class="match-header-card">
        <div class="m-status-banner" :class="'status-' + room.status">
          {{ getStatusText(room.status, room.matchStatus, room.matchTime) }}
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
        </div>
      </el-card>

      <!-- 对战双方展示区 -->
      <div v-if="room" class="vs-section">
        <!-- 发起方 -->
        <el-card shadow="hover" class="team-card host-team" @click="goToTeamDetail(room.hostTeamId)">
          <div class="team-role">发起方</div>
          <el-avatar :size="80" :src="room.hostTeamLogo || 'https://via.placeholder.com/150/ff4d4f/ffffff?text=HOST'" />
          <h2 class="team-name">{{ room.hostTeamName }}</h2>
          <p class="team-uni"><el-icon><School /></el-icon> {{ room.hostUniversity }}</p>
          <div class="host-badge">队长: {{ room.hostLeaderNickname || room.hostId }}</div>
          
          <!-- 战队成员列表 -->
          <div class="team-members" v-if="hostTeamMembers && hostTeamMembers.length > 0">
            <h4 class="members-title">战队成员</h4>
            <div class="member-list">
              <div class="member-item" v-for="member in hostTeamMembers" :key="member.id">
                <el-avatar :size="32" :src="(member.avatar && member.avatar.startsWith('http')) ? member.avatar : member.avatar ? `http://localhost:8081${member.avatar}` : ''" />
                <span class="member-name">{{ member.nickname }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <div class="vs-icon">
          <img src="../../assets/vue.svg" alt="VS" style="width:60px; filter: grayscale(1) opacity(0.5); transform: rotate(45deg);" />
        </div>

        <!-- 应战方 -->
        <el-card v-if="room.status === 0" shadow="hover" class="team-card guest-team empty-guest">
          <div class="team-role">应战方</div>
          <div class="waiting-box">
            <el-icon :size="40" color="#ccc"><QuestionFilled /></el-icon>
            <p>等待勇者应战...</p>
            <el-button type="primary" size="large" @click="handleJoinClick" round>立即应战</el-button>
          </div>
        </el-card>
        
        <el-card v-else shadow="hover" class="team-card guest-team" @click="goToTeamDetail(room.guestTeamId)">
          <div class="team-role">应战方</div>
          <el-avatar :size="80" :src="room.guestTeamLogo || 'https://via.placeholder.com/150/1890ff/ffffff?text=GUEST'" />
          <h2 class="team-name">{{ room.guestTeamName }}</h2>
          <p class="team-uni"><el-icon><School /></el-icon> {{ room.guestUniversity }}</p>
          <div class="guest-badge">队长: {{ room.guestLeaderNickname || room.guestId }}</div>
          
          <!-- 战队成员列表 -->
          <div class="team-members" v-if="guestTeamMembers && guestTeamMembers.length > 0">
            <h4 class="members-title">战队成员</h4>
            <div class="member-list">
              <div class="member-item" v-for="member in guestTeamMembers" :key="member.id">
                <el-avatar :size="32" :src="(member.avatar && member.avatar.startsWith('http')) ? member.avatar : member.avatar ? `http://localhost:8081${member.avatar}` : ''" />
                <span class="member-name">{{ member.nickname }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>


      
      <!-- 状态管理面板 -->
      <MatchStatusPanel 
        :match-id="room.id" 
        :match-data="room" 
        v-if="room && room.status === 1" 
        class="status-panel"
      />
    </div>

    <!-- 应战选择对话框 -->
    <el-dialog v-model="joinDialogVisible" title="选择应战战队" width="400px">
      <el-form label-width="80px">
        <el-form-item label="我的战队" required>
          <el-select v-model="selectedJoinTeamId" placeholder="选择您要派出的战队" style="width:100%">
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
        <el-button type="primary" @click="confirmJoinRoom">确认出战</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useGameProjectStore } from '../../../store/gameProject';
import request from '../../../utils/request';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../../../store/user';
import MatchStatusPanel from '../../../components/MatchStatusPanel.vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const gameProjectStore = useGameProjectStore();

const gameProject = computed(() => route.params.gameProject?.toUpperCase() || null);
const gameProjectDisplayName = computed(() => {
  if (!gameProject.value) return '约战大厅';
  const project = gameProjectStore.getGameProjectByName(gameProject.value);
  return project ? project.displayName : gameProject.value;
});

const room = ref(null);
const loading = ref(false);
const hostTeamMembers = ref([]);
const guestTeamMembers = ref([]);
const countdown = ref('');
let countdownTimer = null;

const joinDialogVisible = ref(false);
const myTeams = ref([]);
const selectedJoinTeamId = ref(null);

const fetchTeamMembers = async (teamId, isHost = true) => {
  try {
    const res = await request.get(`/team/members/${teamId}`);
    if (res.data && res.data.length > 0) {
      if (isHost) {
        hostTeamMembers.value = res.data;
      } else {
        guestTeamMembers.value = res.data;
      }
    }
  } catch (err) {
    console.error('获取战队成员失败', err);
  }
};

const calculateCountdown = () => {
  if (!room.value || !room.value.matchTime) return;
  
  // 后端存储的是北京时间，直接使用即可
  const matchDate = new Date(room.value.matchTime);
  const matchTime = matchDate.getTime();
  const now = new Date().getTime();
  const diff = matchTime - now;
  
  if (diff <= 0) {
    countdown.value = '比赛已开始';
    return;
  }
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((diff % (1000 * 60)) / 1000);
  
  let result = '';
  if (days > 0) result += `${days}天 `;
  if (hours > 0 || days > 0) result += `${hours}小时 `;
  if (minutes > 0 || hours > 0 || days > 0) result += `${minutes}分 `;
  result += `${seconds}秒`;
  
  countdown.value = result;
};

const startCountdown = () => {
  // 清除之前的定时器
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
  
  // 立即计算一次
  calculateCountdown();
  
  // 每秒计算一次
  countdownTimer = setInterval(calculateCountdown, 1000);
};

const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;
  
  loading.value = true;
  try {
    const res = await request.get('/match-room/list');
    const r = res.data.find(item => item.id == id);
    if (r) {
      room.value = r;
      // 获取战队成员
      if (r.hostTeamId) {
        await fetchTeamMembers(r.hostTeamId, true);
      }
      // 获取应战方战队成员
      if (r.guestTeamId) {
        await fetchTeamMembers(r.guestTeamId, false);
      }
      // 启动倒计时
      if (r.status === 1) {
        startCountdown();
      }
    } else {
      ElMessage.error('约战房间不存在');
    }
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const getStatusText = (status, matchStatus, matchTime) => {
  // 首先检查比赛状态
  if (matchStatus === 'IN_PROGRESS') {
    return '比赛进行中';
  } else if (matchStatus === 'FINISHED') {
    return '比赛结束';
  }
  
  // 检查是否过期
  if (isMatchExpired(matchTime)) {
    return '已过期';
  }
  
  // 其他状态
  const map = { 0: '招募中', 1: '已应战', 2: '已结束', 3: '已取消' };
  return map[status] || '未知';
};

const isMatchExpired = (matchTime) => {
  if (!matchTime) return false;
  
  const matchDate = new Date(matchTime);
  const now = new Date();
  const timeDiff = now - matchDate;
  const thirtyMinutes = 30 * 60 * 1000;
  
  return timeDiff > thirtyMinutes;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  // 处理后端返回的时间字符串
  const date = new Date(dateStr);
  // 使用本地时间方法，确保显示用户期望的时间
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

const fetchMyTeams = async () => {
  if (!userStore.token) return;
  try {
    const res = await request.get(`/team/my/${userStore.userInfo.id}`);
    myTeams.value = (res.data || []).filter(t => t.leaderId === userStore.userInfo.id);
  } catch (err) {
    console.error(err);
  }
};

const handleJoinClick = () => {
  if (!userStore.token) return ElMessage.warning('请先登录');
  if (room.value.hostId === userStore.userInfo.id) return ElMessage.warning('不能应战自己的约战');
  if (myTeams.value.length === 0) return ElMessage.warning('您必须是某支战队的队长才能应战');
  
  joinDialogVisible.value = true;
};

const confirmJoinRoom = async () => {
  if (!selectedJoinTeamId.value) return ElMessage.warning('请选择应战战队');
  const team = myTeams.value.find(t => t.id === selectedJoinTeamId.value);
  try {
    await request.post(`/match-room/join/${room.value.id}`, null, {
      params: { 
        guestTeamId: team.id,
        guestTeamName: team.name
      }
    });
    ElMessage.success('应战成功！');
    joinDialogVisible.value = false;
    fetchDetail();
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

onMounted(() => {
  fetchDetail();
  fetchMyTeams();
});

onUnmounted(() => {
  // 清理定时器
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
});
</script>

<style scoped>
.match-detail-page { padding: 20px 0; }
.page-header { margin-bottom: 20px; }

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

.m-content { padding: 20px; }
.m-title { font-size: 28px; color: #333; margin: 0 0 20px 0; width: 80%; }
.m-tags { display: flex; gap: 15px; margin-bottom: 20px; }
.m-time { font-size: 16px; color: #666; margin-bottom: 20px; display: flex; align-items: center; gap: 8px; }
.m-location { font-size: 16px; color: #666; margin-bottom: 20px; display: flex; align-items: center; gap: 8px; }
.m-desc { background: #f8f9fa; padding: 15px; border-radius: 8px; }
.m-desc h3 { margin: 0 0 10px 0; font-size: 16px; color: #333; }
.m-desc p { margin: 0; color: #666; line-height: 1.6; }

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
.team-card:hover { transform: translateY(-5px); }

.team-role {
  position: absolute;
  top: 0;
  left: 0;
  background: #f0f2f5;
  padding: 4px 12px;
  border-radius: 12px 0 12px 0;
  font-size: 12px;
  color: #666;
}

.team-card.host-team .team-role { background: #fff1f0; color: #ff4d4f; }
.team-card.guest-team .team-role { background: #e6f7ff; color: #1890ff; }

.team-name { font-size: 20px; font-weight: bold; margin: 15px 0 5px 0; color: #333; }
.team-uni { font-size: 14px; color: #999; margin: 0 0 15px 0; }
.host-badge { font-size: 12px; color: #ff4d4f; background: #fff1f0; display: inline-block; padding: 2px 10px; border-radius: 10px; }
.guest-badge { font-size: 12px; color: #1890ff; background: #e6f7ff; display: inline-block; padding: 2px 10px; border-radius: 10px; }

.empty-guest { cursor: default; }
.empty-guest:hover { transform: none; }
.waiting-box { padding: 40px 0; display: flex; flex-direction: column; align-items: center; gap: 15px; }
.waiting-box p { color: #999; margin: 0; }

/* 战队成员样式 - 长方形框，一人一个框排成一列 */
.team-members {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e8e8e8;
}

.members-title {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px 0;
  text-align: center;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.member-name {
  font-size: 14px;
  color: #333;
  flex: 1;
  text-align: left;
}

/* 等待开赛区域样式 */
.waiting-start-section {
  display: flex;
  justify-content: center;
  margin: 40px 0;
}

.waiting-card {
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px 40px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.waiting-card:hover {
  box-shadow: 0 4px 15px rgba(0,0,0,0.15);
  transform: translateY(-2px);
}

.waiting-text {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.countdown {
  font-size: 16px;
  color: #ff6b35;
  font-weight: bold;
  background: #fff3cd;
  padding: 5px 15px;
  border-radius: 20px;
  border: 1px solid #ffeaa7;
  min-width: 120px;
  text-align: center;
}

/* 状态管理面板样式 */
.status-panel {
  margin: 40px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
}

/* 响应式 */
@media (max-width: 768px) {
  .waiting-card {
    flex-direction: column;
    gap: 10px;
    padding: 15px 30px;
  }
  
  .waiting-text {
    font-size: 16px;
  }
  
  .countdown {
    font-size: 14px;
  }
}
</style>