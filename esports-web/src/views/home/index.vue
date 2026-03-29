<template>
  <div class="home-container full-container">
    <!-- 第一部分：Banner + 资讯/公告 -->
    <div class="top-row">
      <!-- 左侧：轮播图 -->
      <div class="banner-box">
        <el-carousel height="400px" trigger="click">
          <el-carousel-item v-for="(item, index) in bannerList" :key="index">
            <div 
              class="banner-img" 
              :style="{ backgroundImage: `url(${item.image})`, cursor: item.url ? 'pointer' : 'default' }"
              @click="handleBannerClick(item.url)"
            >
              <div class="banner-title">{{ item.title }}</div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 右侧：新闻与公告聚合版块 -->
      <el-card class="notice-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="title">新闻公告</span>
            <el-button type="primary" link @click="$router.push('/news')">更多</el-button>
          </div>
        </template>
        <div class="notice-list" v-loading="loading">
          <!-- 混合展示新闻和公告 -->
          <div 
            v-for="(item, index) in mixedNews" 
            :key="index" 
            class="notice-item" 
            @click="goNewsDetail(item)"
          >
            <!-- 5. 修改新闻标签颜色为绿色(success) -->
            <el-tag :type="item.type === 'notice' ? 'warning' : 'success'" size="small" class="n-tag" effect="dark">
              {{ item.type === 'notice' ? '公告' : '新闻' }}
            </el-tag>
            <span class="notice-text">{{ item.title }}</span>
            <span class="notice-date">{{ formatDate(item.createTime).substring(5) }}</span>
          </div>
          <el-empty v-if="mixedNews.length === 0" :image-size="60" description="暂无资讯" />
        </div>
      </el-card>
    </div>

    <!-- 第二部分：最新约战房间 -->
    <div class="section-wrapper">
      <div class="section-header">
        <h2 class="section-title">火热约战</h2>
        <el-button link @click="$router.push('/match')">前往大厅 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="6" v-for="room in matchRooms" :key="room.id">
          <!-- 点击约战卡片跳转到详情页 -->
          <el-card class="match-room-card" shadow="hover" @click="goToMatchDetail(room.id, room.gameProject)">
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
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && matchRooms.length === 0" description="暂无约战房间" :image-size="80" />
    </div>

    <!-- 第三部分：最新入驻战队 -->
    <div class="section-wrapper" style="margin-top: 40px;">
      <div class="section-header">
        <h2 class="section-title">新锐战队</h2>
        <el-button link @click="$router.push('/team')">全部战队 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="6" v-for="team in teamList" :key="team.id">
          <!-- 3. 点击战队卡片跳转到战队详情页 -->
          <el-card class="team-item-card" :body-style="{ padding: '0px' }" shadow="hover" @click="goToTeamDetail(team.id, team.gameProject)">
            <div class="team-cover">
              <img :src="team.logo || 'https://via.placeholder.com/300x200/eee/999?text=TEAM'" class="cover-img">
            </div>
            <div class="team-info">
              <h3 class="team-name">{{ team.name }}</h3>
              <p class="team-desc">{{ team.description || '该战队很懒，暂无简介...' }}</p>
              <div class="team-footer">
                <span class="stars"><el-icon><User /></el-icon> {{ team.memberCount }}人</span>
                <el-button type="primary" link @click.stop="goToTeamDetail(team.id, team.gameProject)">详情</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '../../utils/request';

const router = useRouter();
const loading = ref(false);

const bannerList = ref([
  { image: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=1200&q=80', title: '蘸豆，爽！高校电竞约战平台现已开放！' },
  { image: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=1200&q=80', title: '立刻加入立刻爽！' }
]);

const mixedNews = ref([]);
const matchRooms = ref([]);
const teamList = ref([]);

const fetchData = async () => {
  loading.value = true;
  try {
    // 1. 动态获取 Banner
    try {
      const bannerRes = await request.get('/banner/active');
      if (bannerRes.data && bannerRes.data.length > 0) {
        bannerList.value = bannerRes.data;
      }
    } catch (e) {
      console.warn('获取Banner失败，使用默认Banner', e);
    }

    const [noticeRes, newsRes, matchRes, teamRes] = await Promise.all([
      request.get('/notice/list'),
      request.get('/news/list'),
      request.get('/match-room/list'),
      request.get('/team/list')
    ]);
    
    // 混合并排序新闻与公告
    let notices = (noticeRes.data || []).map(item => ({ ...item, type: 'notice' }));
    let news = (newsRes.data || []).map(item => ({ ...item, type: 'news' }));
    mixedNews.value = [...notices, ...news]
      .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      .slice(0, 7);

    // 提取前4个未结束的约战
    matchRooms.value = (matchRes.data || []).filter(room => room.status === 0 || room.status === 1).slice(0, 4);
    
    // 提取前4个战队
    teamList.value = (teamRes.data || []).slice(0, 4).map(team => {
      // 确保logo路径是完整的URL
      if (team.logo && !team.logo.startsWith('http')) {
        team.logo = `http://localhost:8081${team.logo}`;
      }
      return team;
    });

  } catch (err) {
    console.error('获取首页数据失败', err);
  } finally {
    loading.value = false;
  }
};

const goNewsDetail = (item) => {
  if (item.type === 'news') {
    // 5. 独立详情页跳转
    router.push(`/news/${item.id}`);
  } else {
    router.push(`/notice/${item.id}`);
  }
};

const handleBannerClick = (url) => {
  if (url) {
    if (url.startsWith('http')) {
      window.open(url, '_blank');
    } else {
      router.push(url);
    }
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  // 处理后端返回的时间字符串 (如: 2026-03-28T14:30:00 或 2026-03-28 14:30:00)
  const date = new Date(dateStr);
  // 使用本地时间方法，确保显示用户期望的时间
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

const goToTeamDetail = (teamId, gameProject) => {
  // 根据游戏项目跳转到不同的战队详情页
  if (gameProject === 'CS2') {
    router.push(`/cs2/team/${teamId}`);
  } else if (gameProject === 'LOL') {
    router.push(`/lol/team/${teamId}`);
  } else if (gameProject === 'WZRY') {
    router.push(`/wzry/team/${teamId}`);
  } else {
    // 其他游戏使用通用详情页
    router.push(`/team/${teamId}`);
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.home-container {
  padding: 20px 0;
}

/* 第一部分：横向排版 */
.top-row {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
  margin-bottom: 40px;
}

.banner-box {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.banner-img {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}

.banner-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.8));
  color: white;
  padding: 30px 20px 15px;
  font-size: 22px;
  font-weight: bold;
}

.notice-card {
  height: 400px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  color: #333;
}

.notice-list {
  display: flex;
  flex-direction: column;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px dashed #eee;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: color 0.3s;
}
.notice-item:last-child { border-bottom: none; }
.notice-item:hover { color: var(--primary); }

.n-tag { margin-right: 10px; flex-shrink: 0; }
.notice-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}
.notice-date {
  color: #999;
  font-size: 13px;
  margin-left: 10px;
  flex-shrink: 0;
}

/* 通用区块标题 */
.section-wrapper { margin-top: 20px; }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  padding-left: 12px;
  border-left: 4px solid var(--primary);
  margin: 0;
}

/* 约战房间卡片 */
.match-room-card {
  margin-bottom: 20px;
  border-radius: 12px;
  transition: transform 0.3s;
}
.match-room-card:hover { transform: translateY(-5px); }

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

/* 战队卡片 */
.team-item-card {
  margin-bottom: 20px;
  border-radius: 8px;
}
.team-cover {
  height: 160px;
  overflow: hidden;
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.team-item-card:hover .cover-img {
  transform: scale(1.05);
}
.team-info {
  padding: 15px;
}
.team-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.team-desc {
  font-size: 13px;
  color: #666;
  height: 40px;
  line-height: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 12px;
}
.team-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stars {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
