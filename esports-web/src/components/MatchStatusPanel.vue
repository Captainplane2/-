<template>
  <div class="match-status-panel">
    <!-- 过期状态 -->
    <div v-if="isExpired" class="status-expired">
      <el-alert title="已过期" type="info" show-icon :closable="false">
        当前时间已超过开赛时间30分钟
      </el-alert>
    </div>
    
    <!-- 准备状态 -->
    <div v-else-if="currentStatus === 'READY'" class="status-ready">
      <div class="ready-section">
        <div class="team-ready" v-for="team in teams" :key="team.type">
          <div class="team-info">
            <el-avatar :size="40" :src="team.logo" />
            <span class="team-name">{{ team.name }}</span>
          </div>
          <el-button 
            :type="team.ready ? 'danger' : 'success'" 
            :disabled="!isTeamLeader(team.type)"
            @click="toggleReady(team.type)"
            class="ready-btn"
            :loading="loading[team.type]"
          >
            {{ team.ready ? '已准备' : '准备' }}
          </el-button>
        </div>
      </div>
      
      <!-- 倒计时开赛 -->
      <div v-if="isBothReady" class="countdown-section">
        <div class="countdown-box">
          <span class="countdown-text">马上开赛：</span>
          <span class="countdown-timer">{{ countdownSeconds }}s</span>
        </div>
      </div>
    </div>
    
    <!-- 正在比赛 -->
    <div v-else-if="currentStatus === 'IN_PROGRESS'" class="status-in-progress">
      <div class="match-in-progress">
        <el-alert title="正在比赛" type="error" show-icon :closable="false" />
        
        <!-- 结束确认 -->
        <div class="finish-confirm">
          <el-button 
            type="primary" 
            :disabled="!isCurrentUserTeamLeader"
            @click="confirmFinish"
            class="finish-btn"
            :loading="loading.finish"
          >
            结束比赛 {{ finishConfirmCount }}/2
          </el-button>
        </div>
        
        <!-- 比赛结束倒计时 -->
        <div v-if="finishConfirmCount === 2" class="finish-countdown">
          <div class="countdown-box">
            <span class="countdown-text">比赛结束：</span>
            <span class="countdown-timer">{{ countdownSeconds }}s</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 比赛结束 -->
    <div v-else-if="currentStatus === 'FINISHED'" class="status-finished">
      <el-button type="info" disabled class="finished-btn">
        比赛结束
      </el-button>
    </div>
    
    <!-- 招募中状态 -->
    <div v-else-if="currentStatus === 'WAITING'" class="status-waiting">
      <el-alert title="招募中" type="warning" show-icon :closable="false">
        等待其他战队应战
      </el-alert>
    </div>
    
    <!-- 已取消状态 -->
    <div v-else-if="currentStatus === 'CANCELLED'" class="status-cancelled">
      <el-alert title="已取消" type="info" show-icon :closable="false">
        该约战已被取消
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useMatchStatusStore } from '../store/matchStatus';
import { useUserStore } from '../store/user';

const props = defineProps({
  matchId: Number,
  matchData: Object
});

const matchStatusStore = useMatchStatusStore();
const userStore = useUserStore();

// 加载状态
const loading = ref({
  host: false,
  guest: false,
  finish: false
});

// 计算属性
const currentStatus = computed(() => matchStatusStore.currentStatus);
const hostReady = computed(() => matchStatusStore.hostReady);
const guestReady = computed(() => matchStatusStore.guestReady);
const isBothReady = computed(() => matchStatusStore.isBothReady);
const countdownSeconds = computed(() => matchStatusStore.countdownSeconds);
const finishConfirmCount = computed(() => matchStatusStore.finishConfirmCount);

// 检查是否过期
const isExpired = computed(() => {
  if (!props.matchData?.matchTime) return false;
  
  const matchTime = new Date(props.matchData.matchTime).getTime();
  const now = new Date().getTime();
  const thirtyMinutes = 30 * 60 * 1000; // 30分钟
  
  return now > matchTime + thirtyMinutes;
});

// 战队信息
const teams = computed(() => [
  {
    type: 'host',
    name: props.matchData?.hostTeamName || '发起方',
    logo: props.matchData?.hostTeamLogo,
    ready: hostReady.value,
    leaderId: props.matchData?.hostId
  },
  {
    type: 'guest',
    name: props.matchData?.guestTeamName || '应战方',
    logo: props.matchData?.guestTeamLogo,
    ready: guestReady.value,
    leaderId: props.matchData?.guestId
  }
]);

// 检查是否是特定战队的队长
const isTeamLeader = (teamType) => {
  const currentUser = userStore.userInfo;
  if (!currentUser) return false;
  
  const team = teams.value.find(t => t.type === teamType);
  return team && team.leaderId === currentUser.id;
};

// 检查当前用户是否是队长（任意一方）
const isCurrentUserTeamLeader = computed(() => {
  const currentUser = userStore.userInfo;
  if (!currentUser) return false;
  
  return teams.value.some(team => team.leaderId === currentUser.id);
});

// 准备/取消准备
const toggleReady = async (teamType) => {
  const currentUser = userStore.userInfo;
  if (!currentUser) {
    ElMessage.warning('请先登录');
    return;
  }
  
  if (!isTeamLeader(teamType)) {
    ElMessage.warning('只有队长可以操作准备状态');
    return;
  }
  
  loading.value[teamType] = true;
  
  try {
    await matchStatusStore.toggleReady(props.matchId, currentUser.id, teamType);
    ElMessage.success('准备状态已更新');
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  } finally {
    loading.value[teamType] = false;
  }
};

// 确认比赛结束
const confirmFinish = async () => {
  const currentUser = userStore.userInfo;
  if (!currentUser) {
    ElMessage.warning('请先登录');
    return;
  }
  
  if (!isTeamLeader.value) {
    ElMessage.warning('只有队长可以确认比赛结束');
    return;
  }
  
  loading.value.finish = true;
  
  try {
    await matchStatusStore.confirmFinish(props.matchId, currentUser.id);
    ElMessage.success('结束确认已记录');
  } catch (error) {
    ElMessage.error(error.message || '确认失败');
  } finally {
    loading.value.finish = false;
  }
};

// 初始化状态
onMounted(() => {
  if (props.matchData) {
    matchStatusStore.initStatus(props.matchData);
  }
});

// 清理倒计时
onUnmounted(() => {
  matchStatusStore.resetCountdown();
});

// 监听状态变化
watch(() => props.matchData, (newData) => {
  if (newData) {
    matchStatusStore.initStatus(newData);
  }
});
</script>

<style scoped>
.match-status-panel {
  margin: 20px 0;
}

.ready-section {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
}

.team-ready {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.team-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.team-name {
  font-weight: bold;
  font-size: 16px;
}

.ready-btn, .finish-btn, .finished-btn {
  min-width: 100px;
  height: 40px;
  font-size: 14px;
}

.countdown-section, .finish-countdown {
  text-align: center;
  margin: 20px 0;
}

.countdown-box {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  padding: 15px 30px;
  display: inline-block;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.countdown-text {
  font-weight: bold;
  color: #856404;
  font-size: 16px;
}

.countdown-timer {
  font-weight: bold;
  color: #ff6b35;
  font-size: 1.2em;
  margin-left: 10px;
}

.finish-confirm {
  text-align: center;
  margin: 20px 0;
}

.status-waiting, .status-cancelled, .status-expired {
  text-align: center;
  margin: 20px 0;
}

.status-in-progress {
  text-align: center;
}

.match-in-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}
</style>