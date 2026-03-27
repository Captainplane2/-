<template>
  <div class="notice-detail-page full-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/notice' }">公告中心</el-breadcrumb-item>
        <el-breadcrumb-item>公告详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div v-loading="loading" class="detail-content-box">
      <el-card shadow="never" class="notice-card" v-if="notice">
        <h1 class="n-title">{{ notice.title }}</h1>
        <div class="n-meta">
          <span class="author"><el-icon><User /></el-icon> {{ notice.author || '管理员' }}</span>
          <span class="time">{{ formatDate(notice.createTime) }}</span>
          <span class="views"><el-icon><View /></el-icon> {{ notice.views || 0 }} 阅读</span>
        </div>
        
        <div class="n-body">
          {{ notice.content }}
        </div>
      </el-card>
      
      <el-empty v-else-if="!loading" description="抱歉，该公告已被删除或不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import request from '../../utils/request';

const route = useRoute();
const notice = ref(null);
const loading = ref(false);

const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;
  
  loading.value = true;
  try {
    const res = await request.get(`/notice/${id}`);
    notice.value = res.data;
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
.notice-detail-page { padding: 20px 0; }
.page-header { margin-bottom: 20px; }

.detail-content-box {
  max-width: 800px;
  margin: 0 auto;
}

.notice-card { border-radius: 12px; padding: 20px 40px; }

.n-title { font-size: 28px; font-weight: bold; color: #333; margin: 0 0 20px 0; text-align: center; }

.n-meta { display: flex; justify-content: center; align-items: center; gap: 20px; font-size: 14px; color: #999; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #eee; }
.n-meta .author { color: var(--primary); font-weight: 500; }

.n-body { font-size: 16px; line-height: 2; color: #444; white-space: pre-wrap; }
</style>
