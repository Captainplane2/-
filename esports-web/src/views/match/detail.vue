<template>
  <div class="match-detail-page full-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/match' }">约战大厅</el-breadcrumb-item>
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
        <el-card shadow="hover" class="team-card host-team" @click="$router.push(`/team/${room.hostTeamId}`)">
          <div class="team-role">发起方</div>
          <el-avatar :size="80" :src="room.hostTeamLogo || 'https://via.placeholder.com/150/ff4d4f/ffffff?text=HOST'" />
          <h2 class="team-name">{{ room.hostTeamName }}</h2>
          <p class="team-uni"><el-icon><School /></el-icon> {{ room.hostUniversity }}</p>
          <div class="host-badge">队长: {{ room.hostLeaderNickname || room.hostId }}</div>
          
          <!-- 发起方准备按钮 -->
          <div v-if="room.status === 1 && isMatchStarted" class="ready-section">
            <el-button
              v-if="isHostLeader"
              :type="hostReady ? 'success' : 'info'"
              size="large"
              class="ready-btn"
              :disabled="!canPrepare"
              @click.stop="toggleHostReady"
            >
              <el-icon v-if="hostReady"><Check /></el-icon>
              {{ hostReady ? '已准备' : '未准备' }}
            </el-button>
            <el-tag 
              v-else 
              :type="hostReady ? 'success' : 'info'" 
              size="large" 
              class="ready-status"
              :class="{ 'status-changed': hostReadyChanged }"
            >
              {{ hostReady ? '已准备' : '未准备' }}
            </el-tag>
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
        
        <el-card v-else shadow="hover" class="team-card guest-team" @click="$router.push(`/team/${room.guestTeamId}`)">
          <div class="team-role">应战方</div>
          <el-avatar :size="80" :src="room.guestTeamLogo || 'https://via.placeholder.com/150/1890ff/ffffff?text=GUEST'" />
          <h2 class="team-name">{{ room.guestTeamName }}</h2>
          <p class="team-uni"><el-icon><School /></el-icon> {{ room.guestUniversity }}</p>
          <div class="guest-badge">队长: {{ room.guestLeaderNickname || room.guestId }}</div>
          
          <!-- 应战方准备按钮 -->
          <div v-if="room.status === 1 && isMatchStarted" class="ready-section">
            <el-button
              v-if="isGuestLeader"
              :type="guestReady ? 'success' : 'info'"
              size="large"
              class="ready-btn"
              :disabled="!canPrepare"
              @click.stop="toggleGuestReady"
            >
              <el-icon v-if="guestReady"><Check /></el-icon>
              {{ guestReady ? '已准备' : '未准备' }}
            </el-button>
            <el-tag 
              v-else 
              :type="guestReady ? 'success' : 'info'" 
              size="large" 
              class="ready-status"
              :class="{ 'status-changed': guestReadyChanged }"
            >
              {{ guestReady ? '已准备' : '未准备' }}
            </el-tag>
          </div>
        </el-card>
      </div>

      <!-- 倒计时/开始比赛区域 -->
      <div v-if="room && room.status === 1" class="match-action-section">
        <!-- 等待开赛倒计时 -->
        <div v-if="!isMatchStarted" class="countdown-box">
          <div class="countdown-label">等待开赛</div>
          <div class="countdown-time" :class="{ 'urgent': countdownSeconds < 60 }">
            {{ formatCountdown(countdownSeconds) }}
          </div>
        </div>
        
        <!-- 开始比赛按钮 -->
        <div v-else class="start-match-box">
          <el-button
            type="danger"
            size="large"
            class="start-match-btn"
            :disabled="!canStartMatch || isMatchInProgress || isMatchFinished"
            @click="startMatch"
          >
            <div class="btn-content">
              <div class="btn-main">
                {{ isMatchFinished ? '比赛结束' : (isMatchInProgress ? '比赛进行中' : `开始比赛 ${readyCount}/2`) }}
              </div>
              <div v-if="showStartCountdown && !isMatchInProgress && !isMatchFinished" class="btn-sub">
                准备开赛 倒计时{{ startCountdown }}s
              </div>
            </div>
          </el-button>
          
          <!-- 比赛结束确认按钮 -->
          <div v-if="isMatchInProgress && !isMatchFinished" class="finish-confirm-box">
            <el-button
              type="primary"
              size="large"
              class="start-match-btn"
              :disabled="!isHostLeader && !isGuestLeader"
              @click="toggleFinishConfirm"
            >
              <div class="btn-content">
                <div v-if="!showFinishConfirm" class="btn-main">
                  比赛结束？{{ finishConfirmCount }}/2
                </div>
                <div v-else class="btn-main">
                  比赛结束，房间即将关闭 {{ finishCountdown }}s
                </div>
              </div>
            </el-button>
          </div>
        </div>
      </div>
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
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../../store/user';

const route = useRoute();
const userStore = useUserStore();
const room = ref(null);
const loading = ref(false);

const joinDialogVisible = ref(false);
const myTeams = ref([]);
const selectedJoinTeamId = ref(null);

// 倒计时相关
const countdownSeconds = ref(0);
const isMatchStarted = ref(false);
let countdownTimer = null;

// 准备状态
const hostReady = ref(false);
const guestReady = ref(false);

// 状态变化动画
const hostReadyChanged = ref(false);
const guestReadyChanged = ref(false);

// 开始比赛倒计时
const showStartCountdown = ref(false);
const startCountdown = ref(15);
let startCountdownTimer = null;

// 比赛是否正在进行中
const isMatchInProgress = ref(false);

// 比赛结束确认相关
const hostFinishConfirm = ref(false);
const guestFinishConfirm = ref(false);
const showFinishConfirm = ref(false);
const finishConfirmCount = ref(0);
const finishCountdown = ref(15);
let finishCountdownTimer = null;
const isMatchFinished = ref(false);

// 轮询定时器
let pollingTimer = null;

// 计算属性：当前用户是否是发起方队长
const isHostLeader = computed(() => {
  return userStore.token && room.value && 
    String(userStore.userInfo.id) === String(room.value.hostId);
});

// 计算属性：当前用户是否是应战方队长
const isGuestLeader = computed(() => {
  return userStore.token && room.value && 
    String(userStore.userInfo.id) === String(room.value.guestId);
});

// 计算属性：已准备的队伍数量
const readyCount = computed(() => {
  let count = 0;
  if (hostReady.value) count++;
  if (guestReady.value) count++;
  return count;
});

// 计算属性：是否可以开始比赛
const canStartMatch = computed(() => {
  return readyCount.value === 2;
});

// 计算属性：是否在允许准备的时间范围内
const canPrepare = computed(() => {
  if (!room.value || !room.value.matchTime) return false;
  
  // 添加调试日志
  console.log('canPrepare检查 - room.value:', room.value);
  console.log('canPrepare检查 - status:', room.value.status, 'matchStatus:', room.value.matchStatus);
  
  // 检查状态：必须是已应战状态
  // status 是 Integer 类型，1 表示已应战
  // matchStatus 可能是枚举字符串 "READY" 或数字 1
  const isReadyStatus = room.value.status === 1 || 
                        room.value.matchStatus === 'READY' || 
                        room.value.matchStatus === 1;
  
  console.log('canPrepare检查 - isReadyStatus:', isReadyStatus);
  
  if (!isReadyStatus) return false;
  
  const matchTime = new Date(room.value.matchTime);
  const now = new Date();
  const thirtyMinutes = 30 * 60 * 1000;
  
  // 开始前30分钟内或开始后30分钟内
  const timeDiff = Math.abs(matchTime - now);
  const result = timeDiff <= thirtyMinutes;
  
  console.log('canPrepare检查 - timeDiff:', timeDiff, 'result:', result);
  
  return result;
});

// 计算属性：是否超过开赛时间30分钟
const isOverdue = computed(() => {
  if (!room.value || !room.value.matchTime) return false;
  
  const matchTime = new Date(room.value.matchTime);
  const now = new Date();
  const thirtyMinutes = 30 * 60 * 1000;
  
  // 超过开赛时间30分钟
  return now - matchTime > thirtyMinutes;
});

const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;
  
  try {
    const res = await request.get('/match-room/list');
    const r = res.data.find(item => item.id == id);
    if (r) {
      room.value = r;
      // 检查比赛是否已开始
      checkMatchStart();
      // 获取准备状态
      await fetchReadyStatus();
    } else {
      ElMessage.error('约战房间不存在');
    }
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

// 获取准备状态
const fetchReadyStatus = async () => {
  const id = route.params.id;
  if (!id) return;
  
  try {
    const res = await request.get(`/match-status/ready/${id}`);
    if (res.code === 200) {
      // 检查状态变化，触发动画
      if (hostReady.value !== res.data.hostReady) {
        hostReady.value = res.data.hostReady;
        hostReadyChanged.value = true;
        setTimeout(() => {
          hostReadyChanged.value = false;
        }, 1000);
      }
      if (guestReady.value !== res.data.guestReady) {
        guestReady.value = res.data.guestReady;
        guestReadyChanged.value = true;
        setTimeout(() => {
          guestReadyChanged.value = false;
        }, 1000);
      }
      
      // 同步比赛状态（从后端获取）
      if (res.data.matchStatus === 'IN_PROGRESS') {
        // 比赛正在进行中
        isMatchInProgress.value = true;
        isMatchFinished.value = false;
        showStartCountdown.value = false;
        if (startCountdownTimer) {
          clearInterval(startCountdownTimer);
          startCountdownTimer = null;
        }
        
        // 同步比赛结束确认状态
        if (res.data.hostFinishConfirm !== undefined) {
          hostFinishConfirm.value = res.data.hostFinishConfirm;
        }
        if (res.data.guestFinishConfirm !== undefined) {
          guestFinishConfirm.value = res.data.guestFinishConfirm;
        }
        
        // 计算确认数量
        finishConfirmCount.value = (hostFinishConfirm.value ? 1 : 0) + (guestFinishConfirm.value ? 1 : 0);
        
        // 如果双方都确认结束，启动倒计时
        if (finishConfirmCount.value === 2 && !showFinishConfirm.value) {
          showFinishConfirm.value = true;
          startFinishCountdown();
        } else if (finishConfirmCount.value < 2 && showFinishConfirm.value) {
          showFinishConfirm.value = false;
          if (finishCountdownTimer) {
            clearInterval(finishCountdownTimer);
            finishCountdownTimer = null;
          }
        }
      } else if (res.data.matchStatus === 'FINISHED') {
        // 比赛已结束
        isMatchInProgress.value = false;
        isMatchFinished.value = true;
        showStartCountdown.value = false;
        showFinishConfirm.value = false;
        if (startCountdownTimer) {
          clearInterval(startCountdownTimer);
          startCountdownTimer = null;
        }
        if (finishCountdownTimer) {
          clearInterval(finishCountdownTimer);
          finishCountdownTimer = null;
        }
      } else {
        // 同步倒计时信息（从后端获取）
        if (res.data.countdownStartTime && res.data.countdownSeconds) {
          // 如果后端有倒计时信息，同步到前端
          const serverStartTime = new Date(res.data.countdownStartTime);
          const now = new Date();
          const elapsedSeconds = Math.floor((now - serverStartTime) / 1000);
          const remainingSeconds = Math.max(0, res.data.countdownSeconds - elapsedSeconds);
          
          // 如果双方都已准备且倒计时正在进行，显示倒计时
          if (hostReady.value && guestReady.value && remainingSeconds > 0) {
            if (!showStartCountdown.value) {
              showStartCountdown.value = true;
              startCountdown.value = remainingSeconds;
              startStartCountdown();
            }
          }
        } else {
          // 如果后端没有倒计时信息，重置前端倒计时
          if (showStartCountdown.value) {
            showStartCountdown.value = false;
            startCountdown.value = 15;
            if (startCountdownTimer) {
              clearInterval(startCountdownTimer);
              startCountdownTimer = null;
            }
          }
        }
      }
    }
  } catch (err) {
    console.error('获取准备状态失败:', err);
    // 不再显示错误提示，避免持续弹出
  }
};

// 启动轮询
const startPolling = () => {
  if (pollingTimer) clearInterval(pollingTimer);
  
  // 每1秒轮询一次
  pollingTimer = setInterval(() => {
    if (isMatchStarted.value) {
      fetchReadyStatus();
    }
  }, 1000);
};

// 检查比赛是否已开始（开赛时间已过）
const checkMatchStart = () => {
  if (!room.value || !room.value.matchTime) return;
  
  const matchTime = new Date(room.value.matchTime);
  const now = new Date();
  
  if (now >= matchTime) {
    isMatchStarted.value = true;
  } else {
    // 计算剩余秒数
    const diff = Math.floor((matchTime - now) / 1000);
    countdownSeconds.value = Math.max(0, diff);
    startWaitingCountdown();
  }
};

// 启动等待开赛倒计时
const startWaitingCountdown = () => {
  if (countdownTimer) clearInterval(countdownTimer);
  
  countdownTimer = setInterval(() => {
    if (countdownSeconds.value > 0) {
      countdownSeconds.value--;
    } else {
      isMatchStarted.value = true;
      clearInterval(countdownTimer);
    }
  }, 1000);
};

// 格式化倒计时显示
const formatCountdown = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  
  if (hours > 0) {
    return `${hours}时${String(minutes).padStart(2, '0')}分${String(secs).padStart(2, '0')}秒`;
  } else if (minutes > 0) {
    return `${minutes}分${String(secs).padStart(2, '0')}秒`;
  } else {
    return `${secs}秒`;
  }
};

// 切换发起方准备状态
const toggleHostReady = async () => {
  if (!canPrepare.value) {
    if (isOverdue.value) {
      ElMessage.warning('超时未开赛，比赛已取消');
    } else {
      ElMessage.warning('当前状态不允许准备操作');
    }
    return;
  }
  
  try {
    const id = route.params.id;
    const response = await request.post(`/match-status/ready/${id}`, null, {
      params: {
        userId: Number(userStore.userInfo.id),
        teamType: 'host'
      }
    });
    
    if (response.code === 200) {
      hostReady.value = !hostReady.value;
      checkBothReady();
      ElMessage.success(hostReady.value ? '发起方已准备' : '发起方取消准备');
    }
  } catch (error) {
    // 错误信息已在响应拦截器中显示，这里不再重复显示
    console.error('准备操作失败:', error);
  }
};

// 切换应战方准备状态
const toggleGuestReady = async () => {
  if (!canPrepare.value) {
    if (isOverdue.value) {
      ElMessage.warning('超时未开赛，比赛已取消');
    } else {
      ElMessage.warning('当前状态不允许准备操作');
    }
    return;
  }
  
  try {
    const id = route.params.id;
    const response = await request.post(`/match-status/ready/${id}`, null, {
      params: {
        userId: Number(userStore.userInfo.id),
        teamType: 'guest'
      }
    });
    
    if (response.code === 200) {
      guestReady.value = !guestReady.value;
      checkBothReady();
      ElMessage.success(guestReady.value ? '应战方已准备' : '应战方取消准备');
    }
  } catch (error) {
    // 错误信息已在响应拦截器中显示，这里不再重复显示
    console.error('准备操作失败:', error);
  }
};

// 检查双方是否都准备
const checkBothReady = () => {
  if (hostReady.value && guestReady.value) {
    // 双方都已准备，启动开始比赛倒计时
    if (!showStartCountdown.value) {
      showStartCountdown.value = true;
      startCountdown.value = 15;
      startStartCountdown();
    }
  } else {
    // 任何一方取消准备，重置倒计时
    if (showStartCountdown.value) {
      showStartCountdown.value = false;
      startCountdown.value = 15;
      if (startCountdownTimer) {
        clearInterval(startCountdownTimer);
        startCountdownTimer = null;
      }
    }
  }
};

// 启动开始比赛倒计时
const startStartCountdown = () => {
  if (startCountdownTimer) clearInterval(startCountdownTimer);
  
  startCountdownTimer = setInterval(() => {
    if (startCountdown.value > 0) {
      startCountdown.value--;
    } else {
      // 倒计时结束，自动开始比赛
      clearInterval(startCountdownTimer);
      startMatch();
    }
  }, 1000);
};

// 开始比赛
const startMatch = async () => {
  if (!canStartMatch.value) {
    ElMessage.warning('双方战队都需要准备后才能开始比赛');
    return;
  }
  
  try {
    // 调用后端API开始比赛
    const id = route.params.id;
    const response = await request.post(`/match-status/start/${id}`);
    
    if (response.code === 200) {
      isMatchInProgress.value = true;
      showStartCountdown.value = false;
      if (startCountdownTimer) {
        clearInterval(startCountdownTimer);
        startCountdownTimer = null;
      }
      ElMessage.success('比赛开始！');
    }
  } catch (error) {
    console.error('开始比赛失败:', error);
    ElMessage.error(error.message || '开始比赛失败');
  }
};

// 切换比赛结束确认状态
const toggleFinishConfirm = async () => {
  if (!isHostLeader.value && !isGuestLeader.value) {
    ElMessage.warning('只有队长可以操作比赛结束确认');
    return;
  }
  
  try {
    const id = route.params.id;
    const teamType = isHostLeader.value ? 'host' : 'guest';
    
    const response = await request.post(`/match-status/finish-confirm/${id}`, null, {
      params: {
        userId: Number(userStore.userInfo.id),
        teamType: teamType
      }
    });
    
    if (response.code === 200) {
      // 刷新状态
      await fetchReadyStatus();
    }
  } catch (error) {
    console.error('比赛结束确认失败:', error);
    ElMessage.error(error.message || '比赛结束确认失败');
  }
};

// 启动比赛结束倒计时
const startFinishCountdown = () => {
  if (finishCountdownTimer) clearInterval(finishCountdownTimer);
  
  finishCountdown.value = 15;
  finishCountdownTimer = setInterval(() => {
    if (finishCountdown.value > 0) {
      finishCountdown.value--;
    } else {
      // 倒计时结束，比赛结束
      clearInterval(finishCountdownTimer);
      finishCountdownTimer = null;
      isMatchFinished.value = true;
      showFinishConfirm.value = false;
    }
  }, 1000);
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
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const period = hours < 12 ? '上午' : '下午';
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

onMounted(() => {
  fetchDetail();
  fetchMyTeams();
  startPolling();
});

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
  if (startCountdownTimer) clearInterval(startCountdownTimer);
  if (pollingTimer) clearInterval(pollingTimer);
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

/* 对战双方区域 */
.vs-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  padding: 20px;
  flex-wrap: wrap;
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

/* 准备按钮区域 */
.ready-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #e0e0e0;
}

.ready-btn {
  width: 120px;
  font-weight: bold;
}

.ready-btn .el-icon {
  margin-right: 5px;
}

.ready-status {
  font-weight: bold;
  padding: 10px 20px;
  width: 120px;
  text-align: center;
}

/* 状态变化动画 */
.status-changed {
  animation: statusChange 1s ease-in-out;
}

@keyframes statusChange {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 比赛操作区域 */
.match-action-section {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 30px;
}

/* 倒计时框 */
.countdown-box {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px 60px;
  text-align: center;
  color: white;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}

.countdown-label {
  font-size: 18px;
  margin-bottom: 10px;
  opacity: 0.9;
}

.countdown-time {
  font-size: 48px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.countdown-time.urgent {
  color: #ff6b6b;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 开始比赛按钮区域 */
.start-match-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  text-align: center;
}

.finish-confirm-box {
  width: 100%;
  display: flex;
  justify-content: center;
}

.start-match-btn {
  min-width: 280px;
  min-height: 80px;
  font-size: 20px;
  font-weight: bold;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(245, 108, 108, 0.3);
  transition: all 0.3s ease;
}

.start-match-btn:not(:disabled):hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(245, 108, 108, 0.4);
}

.start-match-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.btn-main {
  font-size: 22px;
}

.btn-sub {
  font-size: 14px;
  font-weight: normal;
  opacity: 0.9;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 0.9;
    transform: translateY(0);
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .vs-section {
    flex-direction: column;
    gap: 20px;
  }
  
  .team-card {
    width: 100%;
    max-width: 320px;
  }
  
  .vs-icon {
    transform: rotate(90deg);
  }
  
  .countdown-box {
    padding: 20px 40px;
  }
  
  .countdown-time {
    font-size: 36px;
  }
  
  .start-match-btn {
    min-width: 240px;
    min-height: 70px;
    font-size: 18px;
  }
  
  .btn-main {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .countdown-time {
    font-size: 28px;
  }
  
  .start-match-btn {
    min-width: 200px;
    min-height: 60px;
  }
  
  .btn-main {
    font-size: 18px;
  }
  
  .btn-sub {
    font-size: 12px;
  }
}
</style>
