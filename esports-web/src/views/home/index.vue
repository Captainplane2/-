<template>
  <div class="home-container full-container">
    <!-- 第一部分：Banner + 最新公告 -->
    <div class="top-row">
      <!-- 左侧轮播 -->
      <div class="banner-box">
        <el-carousel height="400px" trigger="click">
          <el-carousel-item v-for="item in bannerList" :key="item.id">
            <div class="banner-img" :style="{ backgroundImage: `url(${item.image})` }">
              <div class="banner-title">{{ item.title }}</div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 右侧公告 -->
      <el-card class="notice-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="title">最新公告</span>
          </div>
        </template>
        <div class="notice-list">
          <div v-for="item in noticeList" :key="item.id" class="notice-item" @click="$router.push('/notice')">
            <span class="notice-text">{{ item.title }}</span>
            <span class="notice-date">{{ formatDate(item.createTime) }}</span>
          </div>
          <el-empty v-if="noticeList.length === 0" :image-size="60" description="暂无公告" />
        </div>
      </el-card>
    </div>

    <!-- 第二部分：最新战队 + 收藏排行 -->
    <div class="bottom-row">
      <!-- 左侧最新战队网格 -->
      <div class="team-section">
        <div class="section-header">
          <h2 class="section-title">最新战队</h2>
        </div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="team in teamList" :key="team.id">
            <el-card class="team-item-card" :body-style="{ padding: '0px' }" shadow="hover">
              <div class="team-cover">
                <img :src="team.logo || 'https://via.placeholder.com/300x200/eee/999?text=TEAM'" class="cover-img">
              </div>
              <div class="team-info">
                <h3 class="team-name">{{ team.name }}</h3>
                <p class="team-desc">{{ team.description || '该战队很懒，暂无简介...' }}</p>
                <div class="team-footer">
                  <span class="stars"><el-icon><Star /></el-icon> 0</span>
                  <el-button type="primary" link @click="$router.push('/team')">详情</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="teamList.length === 0" description="暂无战队信息" />
      </div>

      <!-- 右侧收藏排行 -->
      <el-card class="rank-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="title">收藏排行</span>
          </div>
        </template>
        <div class="rank-list">
          <div v-for="team in rankList" :key="team.id" class="rank-item">
            <img :src="team.logo || 'https://via.placeholder.com/60x40/eee/999?text=LOGO'" class="rank-img">
            <div class="rank-info">
              <span class="rank-name">{{ team.name }}</span>
              <span class="rank-stars"><el-icon><Star /></el-icon> 0</span>
            </div>
          </div>
          <el-empty v-if="rankList.length === 0" :image-size="60" description="暂无排名" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../utils/request';

const bannerList = [
  { id: 1, image: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=1200&q=80', title: '2025年KPL王者荣耀职业联赛春季赛' },
  { id: 2, image: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=1200&q=80', title: '该我上场：电竞新纪元' }
];

const noticeList = ref([]);
const teamList = ref([]);
const rankList = ref([]);

const fetchData = async () => {
  try {
    const [noticeRes, teamRes] = await Promise.all([
      request.get('/notice/list'),
      request.get('/team/list')
    ]);
    noticeList.value = noticeRes.data.slice(0, 5);
    teamList.value = teamRes.data.slice(0, 8);
    rankList.value = teamRes.data.slice(0, 5);
  } catch (err) {
    console.error('获取首页数据失败', err);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return dateStr.split('T')[0];
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.home-container {
  padding: 20px 0;
}

.top-row {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  margin-bottom: 30px;
}

.banner-box {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
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
  background: rgba(0,0,0,0.5);
  color: white;
  padding: 15px 20px;
  font-size: 20px;
  font-weight: bold;
}

.notice-card {
  height: 400px;
}

.card-header {
  font-weight: bold;
  color: #333;
}

.notice-list {
  display: flex;
  flex-direction: column;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  justify-content: space-between;
  cursor: pointer;
  transition: color 0.3s;
}

.notice-item:hover {
  color: var(--primary);
}

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
}

.bottom-row {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
}

.team-section .section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  padding-left: 10px;
  border-left: 4px solid var(--primary);
}

.team-item-card {
  margin-bottom: 20px;
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

.rank-card {
  height: fit-content;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f2f5;
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-img {
  width: 80px;
  height: 50px;
  border-radius: 4px;
  object-fit: cover;
}

.rank-info {
  display: flex;
  flex-direction: column;
}

.rank-name {
  font-size: 14px;
  font-weight: 500;
}

.rank-stars {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
