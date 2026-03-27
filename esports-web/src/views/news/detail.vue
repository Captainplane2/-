<template>
  <div class="news-detail-page full-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/news' }">电竞新闻</el-breadcrumb-item>
        <el-breadcrumb-item>新闻详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-loading="loading" class="detail-content-box">
      <el-card shadow="never" class="news-card" v-if="news">
        <h1 class="n-title">{{ news.title }}</h1>
        <div class="n-meta">
          <el-tag size="small" type="success" effect="dark">{{ news.gameProject || '综合' }}</el-tag>
          <span class="time">{{ formatDate(news.createTime) }}</span>
          <span class="views"><el-icon><View /></el-icon> {{ news.viewCount || 0 }} 阅读</span>
        </div>
        
        <div v-if="news.coverImage" class="n-cover">
          <img :src="news.coverImage" />
        </div>
        
        <div class="n-html" v-html="news.content"></div>
      </el-card>
      
      <el-empty v-else-if="!loading" description="抱歉，该新闻已被删除或不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import request from '../../utils/request';

const route = useRoute();
const news = ref(null);
const loading = ref(false);

const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;
  
  loading.value = true;
  try {
    const res = await request.get(`/news/${id}`);
    news.value = res.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`;
};

onMounted(fetchDetail);
</script>

<style scoped>
.news-detail-page { padding: 20px 0; }
.page-header { margin-bottom: 20px; }

.detail-content-box {
  max-width: 1000px;
  margin: 0 auto;
}

.news-card { border-radius: 12px; padding: 20px 40px; }

.n-title { font-size: 32px; font-weight: bold; color: #333; margin: 0 0 20px 0; text-align: center; }

.n-meta { display: flex; justify-content: center; align-items: center; gap: 20px; font-size: 14px; color: #999; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #eee; }
.n-meta .views { display: flex; align-items: center; gap: 4px; }

.n-cover { margin-bottom: 30px; text-align: center; border-radius: 8px; overflow: hidden; }
.n-cover img { max-width: 100%; max-height: 500px; object-fit: contain; }

.n-html { font-size: 16px; line-height: 1.8; color: #333; }
:deep(.n-html img) { max-width: 100%; height: auto; display: block; margin: 10px auto; border-radius: 8px; }
</style>
