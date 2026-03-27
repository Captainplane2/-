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
            <el-button type="primary" link @click="$router.push('/wzry/news')">更多</el-button>
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

    <!-- 第二部分：功能介绍 -->
    <div class="section-container">
      <div class="section-header">
        <h2>王者荣耀板块</h2>
      </div>
      <div class="feature-grid">
        <el-card class="feature-card">
          <div class="feature-icon">
            <el-icon :size="48"><Trophy /></el-icon>
          </div>
          <h3>战队管理</h3>
          <p>创建和管理你的王者荣耀战队，招募队员，参加比赛</p>
        </el-card>
        <el-card class="feature-card">
          <div class="feature-icon">
            <el-icon :size="48"><FireFilled /></el-icon>
          </div>
          <h3>约战系统</h3>
          <p>发布和接受约战，与其他高校的战队一决高下</p>
        </el-card>
        <el-card class="feature-card">
          <div class="feature-icon">
            <el-icon :size="48"><News /></el-icon>
          </div>
          <h3>赛事新闻</h3>
          <p>了解最新的王者荣耀赛事动态和新闻</p>
        </el-card>
      </div>
      <div class="coming-soon">
        <el-empty description="王者荣耀板块正在开发中，敬请期待！" :image-size="120" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '../../utils/request';

const router = useRouter();
const loading = ref(false);

// 轮播图数据
const bannerList = ref([
  { image: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=1200&q=80', title: '2026年高校王者荣耀春季巅峰赛' },
  { image: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=1200&q=80', title: '王者荣耀全服集结：寻找最强校园战队' }
]);

// 混合新闻和公告
const mixedNews = ref([]);

const fetchData = async () => {
  loading.value = true;
  try {
    // 动态获取 Banner
    try {
      const bannerRes = await request.get('/banner/active', { params: { gameProject: 'WZRY' } });
      if (bannerRes.data && bannerRes.data.length > 0) {
        bannerList.value = bannerRes.data;
      }
    } catch (e) {
      console.warn('获取Banner失败，使用默认Banner', e);
    }

    const [newsRes, noticeRes] = await Promise.all([
      request.get('/news/list', { params: { gameProject: 'WZRY' } }),
      request.get('/notice/list', { params: { gameProject: 'WZRY' } })
    ]);

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

const goNewsDetail = (item) => {
  if (item.type === 'news') {
    router.push(`/wzry/news/${item.id}`);
  } else {
    router.push(`/wzry/notice/${item.id}`);
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
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
};

onMounted(() => {
  console.log('WZRY home page mounted');
  fetchData();
});
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
  text-align: center;
  margin-bottom: 40px;
}

.section-header h2 {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  margin-bottom: 40px;
}

.feature-card {
  text-align: center;
  padding: 30px;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.feature-icon {
  margin-bottom: 20px;
  color: var(--primary);
}

.feature-card h3 {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.feature-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0;
}

.coming-soon {
  text-align: center;
  padding: 60px 0;
  background: #f8f9fa;
  border-radius: 12px;
  margin-top: 40px;
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
  
  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>