<template>
  <div class="match-detail-page full-container">
    <div class="page-header">
      <h1 class="page-title">王者荣耀 约战详情</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/wzry' }">王者荣耀首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/wzry/match' }">约战大厅</el-breadcrumb-item>
        <el-breadcrumb-item>约战详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="content-box" v-loading="loading">
      <div v-if="matchRoom" class="match-detail">
        <div class="match-header">
          <h2 class="match-title">{{ matchRoom.title }}</h2>
          <el-tag :type="getStatusType(matchRoom.status)">{{ getStatusText(matchRoom.status) }}</el-tag>
        </div>

        <div class="match-meta">
          <el-tag size="small">{{ matchRoom.gameProject }}</el-tag>
          <el-tag size="small" :type="matchRoom.type === 1 ? 'warning' : 'success'">
            {{ matchRoom.type === 1 ? '线下约战' : '线上约战' }}
          </el-tag>
          <div class="match-time">
            <el-icon><Calendar /></el-icon> 开赛时间：{{ formatDate(matchRoom.matchTime) }}
          </div>
        </div>

        <div v-if="matchRoom.type === 1" class="match-location">
          <el-icon><Location /></el-icon> {{ matchRoom.location }}
        </div>

        <p class="match-desc">{{ matchRoom.description || '无详细说明' }}</p>

        <div class="teams-section">
          <h3 class="section-title">对战信息</h3>
          <div class="teams-container">
            <!-- 发起方战队 -->
            <div class="team-card host-team">
              <div class="team-header">
                <h4>发起方战队</h4>
                <span class="university">{{ matchRoom.hostUniversity }}</span>
              </div>
              <div class="team-info">
                <img :src="matchRoom.hostTeamLogo || 'https://via.placeholder.com/100x100/eee/999?text=TEAM'" class="team-logo">
                <div class="team-details">
                  <h5 class="team-name">{{ matchRoom.hostTeamName }}</h5>
                  <div class="leader-info">
                    <span class="leader-label">队长：</span>
                    <span class="leader-name">{{ matchRoom.hostLeaderNickname }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- VS 标志 -->
            <div class="vs-container">
              <div class="vs">VS</div>
            </div>

            <!-- 应战方战队 -->
            <div class="team-card guest-team">
              <div class="team-header">
                <h4>应战方战队</h4>
                <span class="university">{{ matchRoom.guestUniversity || '等待应战' }}</span>
              </div>
              <div class="team-info">
                <img :src="matchRoom.guestTeamLogo || 'https://via.placeholder.com/100x100/eee/999?text=TEAM'" class="team-logo">
                <div class="team-details">
                  <h5 class="team-name">{{ matchRoom.guestTeamName || '等待应战' }}</h5>
                  <div class="leader-info" v-if="matchRoom.guestLeaderNickname">
                    <span class="leader-label">队长：</span>
                    <span class="leader-name">{{ matchRoom.guestLeaderNickname }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 战队成员信息 -->
        <div class="members-section" v-if="hostTeamMembers.length > 0 || guestTeamMembers.length > 0">
          <h3 class="section-title">战队成员</h3>
          <div class="members-container">
            <!-- 发起方成员 -->
            <div class="members-card" v-if="hostTeamMembers.length > 0">
              <h4>{{ matchRoom.hostTeamName }} 成员</h4>
              <div class="members-list">
                <div class="member-item" v-for="member in hostTeamMembers" :key="member.id">
                  <img :src="member.avatar || 'https://via.placeholder.com/50x50/eee/999?text=USER'" class="member-avatar">
                  <div class="member-info">
                    <span class="member-name">{{ member.nickname }}</span>
                    <span class="member-role">{{ member.role === 1 ? '队长' : '队员' }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 应战方成员 -->
            <div class="members-card" v-if="guestTeamMembers.length > 0">
              <h4>{{ matchRoom.guestTeamName }} 成员</h4>
              <div class="members-list">
                <div class="member-item" v-for="member in guestTeamMembers" :key="member.id">
                  <img :src="member.avatar || 'https://via.placeholder.com/50x50/eee/999?text=USER'" class="member-avatar">
                  <div class="member-info">
                    <span class="member-name">{{ member.nickname }}</span>
                    <span class="member-role">{{ member.role === 1 ? '队长' : '队员' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="actions-section">
          <el-button type="primary" @click="goBack">返回约战大厅</el-button>
          <el-button 
            v-if="matchRoom.status === 0 && userStore.token && matchRoom.hostId !== userStore.userInfo.id" 
            type="success" 
            @click="joinMatch"
          >立即应战</el-button>
          <el-button 
            v-else-if="matchRoom.status === 1" 
            type="warning" 
            disabled
          >等待开赛</el-button>
          <el-button 
            v-if="matchRoom.status === 0 && userStore.token && matchRoom.hostId === userStore.userInfo.id" 
            type="danger" 
            @click="cancelMatch"
          >取消约战</el-button>
          <el-button 
            v-if="matchRoom.status === 1 && userStore.token && matchRoom.hostId === userStore.userInfo.id" 
            type="info" 
            @click="finishMatch"
          >结束约战</el-button>
        </div>
      </div>
      <el-empty v-else description="约战信息不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../../store/user';
import request from '../../../utils/request';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const matchRoom = ref(null);
const loading = ref(true);
const hostTeamMembers = ref([]);
const guestTeamMembers = ref([]);

const fetchMatchDetail = async () => {
  const roomId = route.params.id;
  if (!roomId) return;
  
  loading.value = true;
  try {
    // 这里应该调用获取约战详情的接口，暂时使用list接口模拟
    const res = await request.get('/match-room/list', { params: { gameProject: 'WZRY' } });
    const rooms = res.data || [];
    matchRoom.value = rooms.find(room => room.id == roomId);
    
    if (matchRoom.value) {
      // 获取发起方战队成员
      if (matchRoom.value.hostTeamId) {
        await fetchTeamMembers(matchRoom.value.hostTeamId, hostTeamMembers);
      }
      // 获取应战方战队成员
      if (matchRoom.value.guestTeamId) {
        await fetchTeamMembers(matchRoom.value.guestTeamId, guestTeamMembers);
      }
    }
  } catch (err) {
    console.error('获取约战详情失败', err);
  } finally {
    loading.value = false;
  }
};

const fetchTeamMembers = async (teamId, membersRef) => {
  try {
    const res = await request.get(`/team/members/${teamId}`);
    membersRef.value = res.data || [];
  } catch (err) {
    console.error('获取战队成员失败', err);
  }
};

const joinMatch = async () => {
  try {
    // 简单处理，使用第一个战队应战
    const res = await request.get(`/team/my/${userStore.userInfo.id}`);
    const myTeams = (res.data || []).filter(t => t.gameProject === 'WZRY');
    
    if (myTeams.length === 0) {
      ElMessage.warning('您还没有王者荣耀战队');
      return;
    }
    
    await request.post(`/match-room/join/${matchRoom.value.id}`, null, {
      params: {
        guestTeamId: myTeams[0].id,
        guestTeamName: myTeams[0].name
      }
    });
    ElMessage.success('应战成功');
    fetchMatchDetail();
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '应战失败');
  }
};

const cancelMatch = async () => {
  try {
    await request.post(`/match-room/cancel/${matchRoom.value.id}`, null, {
      params: { userId: userStore.userInfo.id }
    });
    ElMessage.success('约战已取消');
    fetchMatchDetail();
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '取消失败');
  }
};

const finishMatch = async () => {
  try {
    await request.post(`/match-room/finish/${matchRoom.value.id}`, null, {
      params: { userId: userStore.userInfo.id }
    });
    ElMessage.success('约战已结束');
    fetchMatchDetail();
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '结束失败');
  }
};

const goBack = () => {
  router.push('/wzry/match');
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

onMounted(fetchMatchDetail);
</script>

<style scoped>
.match-detail-page { padding: 20px 0; }

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

.match-detail {
  max-width: 800px;
  margin: 0 auto;
}

.match-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.match-title {
  font-size: 20px;
  font-weight: bold;
  margin: 0;
  flex: 1;
  margin-right: 20px;
}

.match-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.match-time {
  font-size: 14px;
  color: #666;
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 5px;
}

.match-location {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.match-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 30px 0;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 20px 0;
  color: #333;
}

.teams-section {
  margin-bottom: 30px;
}

.teams-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.team-card {
  flex: 1;
  background: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
}

.host-team {
  border-left: 4px solid #409eff;
}

.guest-team {
  border-left: 4px solid #67c23a;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.team-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.university {
  font-size: 12px;
  color: #999;
}

.team-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.team-logo {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.team-details h5 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.leader-info {
  font-size: 14px;
  color: #666;
}

.leader-label {
  color: #999;
  margin-right: 5px;
}

.leader-name {
  font-weight: 500;
  color: #333;
}

.vs-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
}

.vs {
  font-size: 24px;
  font-weight: bold;
  color: #999;
}

.members-section {
  margin-bottom: 30px;
}

.members-container {
  display: flex;
  gap: 20px;
}

.members-card {
  flex: 1;
  background: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
}

.members-card h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: white;
  border-radius: 4px;
}

.member-avatar {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 50%;
}

.member-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.member-role {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #e6f7ff;
  color: #1890ff;
}

.actions-section {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}
</style>