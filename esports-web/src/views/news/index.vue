<template>
  <div class="news-page full-container">
    <div class="page-header">
      <h1 class="page-title">电竞新闻</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>新闻资讯</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="news-content">
      <!-- 游戏分类 Tab -->
      <el-tabs v-model="activeCategory" @tab-change="fetchNews" class="category-tabs">
        <el-tab-pane label="综合资讯" name="综合" />
        <el-tab-pane label="英雄联盟" name="LOL" />
        <el-tab-pane label="王者荣耀" name="王者荣耀" />
        <el-tab-pane label="CS2" name="CS2" />
        <el-tab-pane label="无畏契约" name="无畏契约" />
      </el-tabs>

      <!-- 新闻列表 -->
      <div v-loading="loading" class="news-list">
        <el-row :gutter="20">
          <el-col :span="24" v-for="item in newsList" :key="item.id">
            <!-- 修改点击事件，直接跳转详情页 -->
            <el-card class="news-card" shadow="hover" @click="$router.push(`/news/${item.id}`)">
              <div class="news-item">
                <div class="news-cover" v-if="item.coverImage">
                  <img :src="item.coverImage" />
                </div>
                <div class="news-info">
                  <h2 class="title">{{ item.title }}</h2>
                  <p class="summary">{{ item.summary || stripHtml(item.content).substring(0, 150) + '...' }}</p>
                  <div class="meta">
                    <!-- 修改标签颜色为绿色 -->
                    <span class="tag"><el-tag size="small" type="success" effect="dark">{{ item.gameProject }}</el-tag></span>
                    <span class="time">{{ formatDate(item.createTime) }}</span>
                    <span class="views"><el-icon><View /></el-icon> {{ item.viewCount || 0 }} 阅读</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="newsList.length === 0" description="暂无相关新闻资讯" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../utils/request';

const activeCategory = ref('综合');
const newsList = ref([]);
const loading = ref(false);

const fetchNews = async () => {
  loading.value = true;
  try {
    const res = await request.get('/news/list', {
      params: { gameProject: activeCategory.value === '综合' ? '' : activeCategory.value }
    });
    newsList.value = res.data || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const stripHtml = (html) => {
  if (!html) return '';
  let doc = new DOMParser().parseFromString(html, 'text/html');
  return doc.body.textContent || "";
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
};

onMounted(fetchNews);
</script>

<style scoped>
.news-page { padding: 20px 0; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 28px; font-weight: bold; color: #333; margin-bottom: 10px; }

.news-content {
  background: var(--bg-white);
  padding: 20px 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  min-height: 600px;
}

.category-tabs { margin-bottom: 20px; }

.news-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
}
.news-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  border-color: var(--primary);
}

.news-item {
  display: flex;
  gap: 20px;
}

.news-cover {
  width: 240px;
  height: 140px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
}
.news-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.news-card:hover .news-cover img {
  transform: scale(1.05);
}

.news-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-info .title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
  transition: color 0.3s;
}
.news-card:hover .news-info .title {
  color: var(--primary);
}

.news-info .summary {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  flex: 1;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-info .meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 13px;
  color: #999;
  margin-top: 15px;
}
.meta .views { display: flex; align-items: center; gap: 4px; }

/* 详情弹窗样式 */
.news-detail-container {
  padding: 0 10px;
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}
.detail-meta .views { display: flex; align-items: center; gap: 4px; }
.detail-cover {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  text-align: center;
}
.detail-cover img {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
}
.detail-html {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}
/* 解决wangEditor输出的图片溢出问题 */
:deep(.detail-html img) {
  max-width: 100%;
  height: auto;
}
</style>
