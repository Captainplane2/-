<template>
  <div class="match-detail-page full-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/cs2' }">CS2首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/cs2/match' }">约战大厅</el-breadcrumb-item>
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
        <el-card shadow="hover" class="team-card host-team" @click="$router.push(`/cs2/team/${room.hostTeamId}`)">
          <div class="team-role">发起方</div>
          <el-avatar :size="80" :src="(room.hostTeamLogo && room.hostTeamLogo.startsWith('http')) ? room.hostTeamLogo : room.hostTeamLogo ? `http://localhost:8081${room.hostTeamLogo}` : 'https://via.placeholder.com/150/ff4d4f/ffffff?text=HOST'" />
          <h2 class="team-name">{{ room.hostTeamName }}</h2>
          <p class="team-uni"><el-icon><School /></el-icon> {{ hostLeaderUniversity }}</p>
          <div class="host-badge">队长: {{ hostLeaderNickname }}</div>
          
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
          <img src="../../../assets/vue.svg" alt="VS" style="width:60px; filter: grayscale(1) opacity(0.5); transform: rotate(45deg);" />
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
        
        <el-card v-else shadow="hover" class="team-card guest-team" @click="$router.push(`/cs2/team/${room.guestTeamId}`)">
          <div class="team-role">应战方</div>
          <el-avatar :size="80" :src="(room.guestTeamLogo && room.guestTeamLogo.startsWith('http')) ? room.guestTeamLogo : room.guestTeamLogo ? `http://localhost:8081${room.guestTeamLogo}` : 'https://via.placeholder.com/150/1890ff/ffffff?text=GUEST'" />
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
    </div>

    <!-- 应战选择对话框 -->
    <el-dialog v-model="joinDialogVisible" title="选择应战战队" width="400px">
      <el-form label-width="80px">
        <el-form-item label="我的战队" required>
          <el-select v-model="selectedJoinTeamId" placeholder="选择您要派出的CS2战队" style="width:100%">
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
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '../../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '../../../store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const room = ref(null);
const loading = ref(false);
const hostTeamMembers = ref([]);
const guestTeamMembers = ref([]);

const joinDialogVisible = ref(false);
const myTeams = ref([]);
const selectedJoinTeamId = ref(null);

// 计算属性：获取发起方队长信息
const hostLeader = computed(() => {
  return hostTeamMembers.value.find(m => m.role === 1) || null;
});

// 计算属性：获取发起方队长高校
const hostLeaderUniversity = computed(() => {
  return hostLeader.value?.university || room.value?.hostUniversity || '';
});

// 计算属性：获取发起方队长昵称
const hostLeaderNickname = computed(() => {
  return hostLeader.value?.nickname || room.value?.hostLeaderNickname || '未知';
});

const fetchTeamMembers = async (teamId, isHost = true) => {
  try {
    // 后端没有detail接口，使用members接口
    console.log('开始获取战队成员，teamId:', teamId);
    const res = await request.get(`/team/members/${teamId}`);
    console.log('获取战队成员成功，数据:', res.data);
    if (res.data && res.data.length > 0) {
      if (isHost) {
        hostTeamMembers.value = res.data;
        console.log('设置hostTeamMembers:', hostTeamMembers.value);
      } else {
        guestTeamMembers.value = res.data;
        console.log('设置guestTeamMembers:', guestTeamMembers.value);
      }
    } else {
      console.log('战队成员数据为空');
    }
  } catch (err) {
    console.error('获取战队成员失败', err);
  }
};

const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;
  
  loading.value = true;
  try {
    // 后端没有单个查询接口，我们从 list 接口中过滤
    const res = await request.get('/match-room/list', { params: { gameProject: 'CS2' } });
    const r = res.data.find(item => item.id == id);
    if (r) {
      room.value = r;
      // 获取战队成员
      if (r.hostTeamId) {
        await fetchTeamMembers(r.hostTeamId);
      }
      // 获取应战方战队成员
      if (r.guestTeamId) {
        await fetchTeamMembers(r.guestTeamId, false);
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

const getStatusText = (status) => {
  const map = { 0: '招募中', 1: '已应战', 2: '已结束', 3: '已取消' };
  return map[status] || '未知';
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

const fetchMyTeams = async () => {
  if (!userStore.token) return;
  try {
    const res = await request.get(`/team/my/${userStore.userInfo.id}`);
    // 仅列出CS2战队
    myTeams.value = (res.data || []).filter(t => t.gameProject === 'CS2');
  } catch (err) {
    console.error(err);
  }
};

const handleJoinClick = () => {
  if (!userStore.token) return ElMessage.warning('请先登录');
  if (room.value.hostId === userStore.userInfo.id) return ElMessage.warning('不能应战自己的约战');
  if (myTeams.value.length === 0) return ElMessage.warning('您必须有CS2战队才能应战');
  
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
    
    await request.post(`/match-room/cancel/${room.value.id}`, null, {
      params: { userId: userStore.userInfo.id }
    });
    ElMessage.success('约战已取消');
    // 跳转到约战大厅
    router.push('/cs2/match');
  } catch (err) {
    if (err !== 'cancel') {
      console.error(err);
      ElMessage.error('取消约战失败，请重试');
    }
  }
};

onMounted(() => {
  fetchDetail();
  fetchMyTeams();
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

.host-badge {
  font-size: 12px;
  color: #ff4d4f;
  background: #fff1f0;
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
}

.guest-badge {
  font-size: 12px;
  color: #1890ff;
  background: #e6f7ff;
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
}

/* 战队成员样式 */
.team-members {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.members-title {
  font-size: 14px;
  color: #333;
  margin: 0 0 10px 0;
  font-weight: bold;
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
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
  text-align: left;
}

.member-name {
  font-size: 14px;
  color: #333;
}

.empty-guest { cursor: default; }

.empty-guest:hover { transform: none; }

.waiting-box { padding: 40px 0; display: flex; flex-direction: column; align-items: center; gap: 15px; }

.waiting-box p { color: #999; margin: 0; }
</style>