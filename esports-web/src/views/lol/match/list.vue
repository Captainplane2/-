<template>
  <div class="lol-match-list-page">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/lol' }">LOL首页</el-breadcrumb-item>
        <el-breadcrumb-item>约战大厅</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>英雄联盟约战大厅</h1>
    </div>

    <div class="match-filter">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索约战标题"
        class="search-input"
        @keyup.enter="fetchRooms"
      >
        <template #append>
          <el-button @click="fetchRooms"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="createMatch">创建约战</el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="8" v-for="room in rooms" :key="room.id">
        <el-card class="match-room-card" shadow="hover" @click="goToMatchDetail(room.id, room.gameProject)">
          <div class="room-header">
            <el-tag :type="room.type === 1 ? 'warning' : 'success'" size="small" effect="dark">
              {{ room.type === 1 ? '线下' : '线上' }}
            </el-tag>
            <el-tag v-if="room.status === 0" size="small" effect="plain" type="success">招募中</el-tag>
            <el-tag v-else-if="room.status === 1" size="small" effect="plain" type="warning">已应战</el-tag>
            <el-tag v-else size="small" effect="plain" type="info">已结束</el-tag>
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
              @click.stop="goToMatchDetail(room.id, room.gameProject)"
            >立即应战</el-button>
            <el-button 
              v-else-if="room.matchStatus === 'IN_PROGRESS'" 
              type="danger" 
              class="join-btn" 
              disabled
            >比赛进行中</el-button>
            <el-button 
              v-else-if="room.matchStatus === 'FINISHED'" 
              type="info" 
              class="join-btn" 
              disabled
            >比赛结束</el-button>
            <el-button 
              v-else-if="room.status === 1" 
              :type="isMatchExpired(room.matchTime) ? 'info' : 'warning'" 
              class="join-btn"
              disabled
            >{{ isMatchExpired(room.matchTime) ? '已过期' : '等待开赛' }}</el-button>
            <el-button 
              v-else 
              type="info" 
              class="join-btn"
              disabled
            >已结束</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '../../../utils/request';

const router = useRouter();
const loading = ref(false);
const rooms = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(12);
const searchKeyword = ref('');

const fetchRooms = async () => {
  loading.value = true;
  try {
    const res = await request.get('/match-room/list', {
      params: {
        gameProject: 'LOL',
        title: searchKeyword.value,
        page: currentPage.value,
        pageSize: pageSize.value
      }
    });
    rooms.value = res.data || [];
    total.value = res.total || 0;
  } catch (err) {
    console.error('获取约战列表失败', err);
  } finally {
    loading.value = false;
  }
};

const createMatch = () => {
  router.push('/lol/match/manage');
};

const goToMatchDetail = (roomId, gameProject) => {
  // 根据游戏项目跳转到不同的详情页
  if (gameProject === 'CS2') {
    router.push(`/cs2/match/${roomId}`);
  } else if (gameProject === 'LOL') {
    router.push(`/lol/match/${roomId}`);
  } else if (gameProject === 'WZRY') {
    router.push(`/wzry/match/${roomId}`);
  } else {
    // 其他游戏使用通用详情页
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

// 判断约战是否过期（当前时间超过开赛时间30分钟）
const isMatchExpired = (matchTime) => {
  if (!matchTime) return false;
  
  // 解析开赛时间
  const matchDate = new Date(matchTime);
  
  // 获取当前时间
  const now = new Date();
  
  // 计算时间差（毫秒）
  const timeDiff = now - matchDate;
  
  // 30分钟 = 30 * 60 * 1000 毫秒
  const thirtyMinutes = 30 * 60 * 1000;
  
  // 如果当前时间超过开赛时间30分钟，则认为已过期
  return timeDiff > thirtyMinutes;
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchRooms();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  fetchRooms();
};

onMounted(() => {
  fetchRooms();
});
</script>

<style scoped>
.lol-match-list-page {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 10px 0 0 0;
}

.match-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.search-input {
  width: 300px;
}

.match-room-card {
  transition: transform 0.3s;
  cursor: pointer;
}

.match-room-card:hover {
  transform: translateY(-5px);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.room-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 20px 0;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.room-info {
  margin-bottom: 20px;
}

.info-item {
  font-size: 14px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.label {
  color: #999;
  min-width: 90px;
}

.value {
  color: #333;
  font-weight: 500;
}

.host-team {
  color: #ff4d4f;
  font-weight: bold;
}

.university-tag {
  margin-left: 5px;
}

.room-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.join-btn {
  width: 100%;
}

.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}
</style>