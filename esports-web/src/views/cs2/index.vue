<template>
  <div class="home-page full-container">
    <!-- 第一部分：轮播Banner -->
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
            <span class="title">新闻与公告</span>
            <el-button type="primary" link @click="$router.push('/cs2/news')">更多</el-button>
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

    <!-- 第二部分：新锐战队展示 -->
    <div class="section-container">
      <div class="section-header">
        <h2>CS2 新锐战队</h2>
        <el-button type="primary" link @click="$router.push('/cs2/team')">查看全部</el-button>
      </div>
      <div v-loading="loading" class="team-grid">
        <el-row :gutter="20">
          <el-col :span="6" v-for="team in topTeams" :key="team.id">
            <el-card class="team-card" :body-style="{ padding: '0px' }" shadow="hover" @click="$router.push(`/cs2/team/${team.id}`)">
              <div class="team-img-box">
                <img :src="team.logo || 'https://via.placeholder.com/300x200/eee/999?text=TEAM'" class="team-img">
              </div>
              <div class="team-detail">
                <h3 class="name">{{ team.name }}</h3>
                <div class="meta">
                  <span>{{ team.university }}</span>
                  <el-tag size="small" effect="plain">{{ team.gameProject }}</el-tag>
                </div>
                <div class="actions">
                  <span class="m-count"><el-icon><User /></el-icon> {{ team.memberCount }}人</span>
                  <el-button type="primary" link @click.stop="joinTeam(team.id)">申请加入</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!loading && topTeams.length === 0" description="暂无战队信息" />
      </div>
    </div>

    <!-- 第三部分：热门约战 -->
    <div class="section-container" style="margin-top: 40px;">
      <div class="section-header">
        <h2>CS2 热门约战</h2>
        <el-button type="primary" link @click="$router.push('/cs2/match')">查看全部</el-button>
      </div>
      <div v-loading="loading" class="match-grid">
        <el-row :gutter="20">
          <el-col :span="6" v-for="match in hotMatches" :key="match.id">
            <el-card class="match-card" shadow="hover" @click="$router.push(`/cs2/match/${match.id}`)">
              <div class="match-info">
                <h3 class="match-title">{{ match.title }}</h3>
                <div class="match-meta">
                  <el-tag size="small">{{ match.gameProject }}</el-tag>
                  <el-tag size="small" :type="match.type === 1 ? 'warning' : 'success'">
                    {{ match.type === 1 ? '线下约战' : '线上约战' }}
                  </el-tag>
                </div>
                <p class="match-desc">{{ match.description || '无详细说明' }}</p>
                <div class="match-footer">
                  <span class="match-time"><el-icon><Calendar /></el-icon> {{ formatDate(match.matchTime) }}</span>
                  <el-button type="primary" link @click.stop="$router.push(`/cs2/match/${match.id}`)">查看详情</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!loading && hotMatches.length === 0" description="暂无约战信息" />
      </div>
    </div>

    <!-- 第四部分：最新动态 -->
    <div class="section-container" style="margin-top: 40px;">
      <div class="section-header">
        <h2>CS2 最新动态</h2>
        <el-button type="primary" link @click="$router.push('/cs2/news')">查看全部</el-button>
      </div>
      <div v-loading="loading" class="news-grid">
        <el-row :gutter="20">
          <el-col :span="6" v-for="news in latestNews" :key="news.id">
            <el-card class="news-card" shadow="hover" @click="$router.push(`/cs2/news/${news.id}`)">
              <div class="news-info">
                <h3 class="news-title">{{ news.title }}</h3>
                <p class="news-desc">{{ removeHtmlTags(news.content).substring(0, 100) }}...</p>
                <div class="news-footer">
                  <span class="news-time">{{ formatDate(news.createTime) }}</span>
                  <el-button type="primary" link @click.stop="$router.push(`/cs2/news/${news.id}`)">阅读更多</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!loading && latestNews.length === 0" description="暂无新闻信息" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../../store/user';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const topTeams = ref([]);
const hotMatches = ref([]);
const latestNews = ref([]);

// 轮播图数据
const bannerList = ref([
  { image: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=1200&q=80', title: '2026年高校CS2春季邀请赛' },
  { image: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=1200&q=80', title: 'CS2全服集结：寻找最强校园战队' }
]);

// 混合新闻和公告
const mixedNews = ref([]);

const fetchData = async () => {
  loading.value = true;
  try {
    // 动态获取 Banner
    try {
      const bannerRes = await request.get('/banner/active', { params: { gameProject: 'CS2' } });
      if (bannerRes.data && bannerRes.data.length > 0) {
        bannerList.value = bannerRes.data;
      }
    } catch (e) {
      console.warn('获取Banner失败，使用默认Banner', e);
    }

    const [teamRes, matchRes, newsRes, noticeRes] = await Promise.all([
      request.get('/team/list', { params: { gameProject: 'CS2' } }),
      request.get('/match-room/list', { params: { gameProject: 'CS2' } }),
      request.get('/news/list', { params: { gameProject: 'CS2' } }),
      request.get('/notice/list', { params: { gameProject: 'CS2' } })
    ]);

    // 提取前4个战队
    topTeams.value = (teamRes.data || []).slice(0, 4).map(team => {
      if (team.logo && !team.logo.startsWith('http')) {
        team.logo = `http://localhost:8081${team.logo}`;
      }
      return team;
    });

    // 获取CS2约战列表
    hotMatches.value = (matchRes.data || []).filter(m => m.status === 0).slice(0, 4);

    // 获取CS2新闻列表
    latestNews.value = (newsRes.data || []).slice(0, 4);

    // 混合并排序新闻与公告
    let notices = (noticeRes.data || []).map(item => ({ ...item, type: 'notice' }));
    let news = (newsRes.data || []).map(item => ({ ...item, type: 'news' }));
    mixedNews.value = [...notices, ...news]
      .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      .slice(0, 7);
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const joinTeam = async (teamId) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    return;
  }
  try {
    await request.post(`/team/join/${teamId}`, null, { params: { userId: userStore.userInfo.id } });
    ElMessage.success('申请已发送');
  } catch (err) {
    console.error(err);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
};

const removeHtmlTags = (html) => {
  if (!html) return '';
  return html.replace(/<[^>]*>/g, '');
};

const goNewsDetail = (item) => {
  if (item.type === 'news') {
    router.push(`/cs2/news/${item.id}`);
  } else {
    router.push(`/cs2/notice/${item.id}`);
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

onMounted(fetchData);
</script>

<style scoped>
.home-page {
  padding: 20px 0;
}

/* 第一部分：横向排版 */
.top-row {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
  margin-bottom: 40px;
  max-width: 1400px;
  margin: 0 auto 40px;
  padding: 0 20px;
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

.section-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.team-grid, .match-grid, .news-grid {
  margin-top: 20px;
}

.team-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.team-card:hover {
  transform: translateY(-5px);
}

.team-img-box {
  height: 180px;
  overflow: hidden;
}

.team-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.team-detail {
  padding: 15px;
}

.team-detail .name {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.team-detail .meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 12px;
  color: #666;
}

.team-detail .actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.match-card, .news-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.match-card:hover, .news-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.match-title, .news-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.match-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.match-desc, .news-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.match-footer, .news-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.match-time, .news-time {
  font-size: 12px;
  color: #999;
}

@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
    text-align: center;
  }
  
  .hero-text h1 {
    font-size: 36px;
  }
  
  .hero-buttons {
    justify-content: center;
  }
  
  .el-col {
    flex: 100%;
    max-width: 100%;
  }
}
</style>