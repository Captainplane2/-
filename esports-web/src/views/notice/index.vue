<template>
  <div class="notice-page">
    <div class="page-header">
      <h1 class="page-title">公告<span class="accent">中心</span></h1>
      <p class="page-desc">官方动态、赛事通知与平台维护信息</p>
    </div>

    <el-card shadow="never" class="notice-container-card glass-effect">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="icon-main"><Notification /></el-icon>
            <span>最新公告</span>
          </div>
          <el-input
            v-model="searchQuery"
            placeholder="搜索关键词..."
            prefix-icon="Search"
            style="width: 280px"
            class="dark-search"
            clearable
          />
        </div>
      </template>

      <div v-loading="loading" class="notice-list">
        <div v-for="item in filteredNotices" :key="item.id" class="notice-row" @click="viewDetail(item.id)">
          <div class="notice-info">
            <el-tag v-if="isNew(item.createTime)" type="danger" size="small" effect="dark" class="new-tag">NEW</el-tag>
            <span class="notice-title">{{ item.title }}</span>
          </div>
          <div class="notice-meta">
            <span class="views"><el-icon><View /></el-icon> {{ item.views }}</span>
            <span class="time">{{ formatDate(item.createTime) }}</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </div>
        <el-empty v-if="filteredNotices.length === 0" description="暂无相关公告内容" />
      </div>
    </el-card>

    <!-- 公告详情 (沉浸式弹窗) -->
    <el-dialog
      v-model="detailVisible"
      :title="currentNotice.title"
      width="760px"
      class="dark-dialog"
      destroy-on-close
    >
      <div class="notice-content-wrapper">
        <div class="notice-meta-info">
          <div class="info-item">
            <span class="label">发布者:</span>
            <span class="val">{{ currentNotice.author || '系统管理员' }}</span>
          </div>
          <div class="info-item">
            <span class="label">发布时间:</span>
            <span class="val">{{ formatDate(currentNotice.createTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">阅读量:</span>
            <span class="val">{{ currentNotice.views }}</span>
          </div>
        </div>
        <el-divider class="dark-divider" />
        <div class="notice-body" v-html="currentNotice.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import request from '../../utils/request';

const notices = ref([]);
const loading = ref(false);
const searchQuery = ref('');
const detailVisible = ref(false);
const currentNotice = ref({});

const fetchNotices = async () => {
  loading.value = true;
  try {
    const res = await request.get('/notice/list');
    notices.value = res.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const filteredNotices = computed(() => {
  if (!searchQuery.value) return notices.value;
  return notices.value.filter(n => n.title.includes(searchQuery.value));
});

const viewDetail = async (id) => {
  try {
    const res = await request.get(`/notice/${id}`);
    currentNotice.value = res.data;
    detailVisible.value = true;
    const target = notices.value.find(n => n.id === id);
    if (target) target.views++;
  } catch (err) {
    console.error(err);
  }
};

const formatDate = (d) => d?.split('T')[0] || '';

const isNew = (d) => {
  if (!d) return false;
  const date = new Date(d);
  const diff = Math.abs(new Date() - date);
  return (diff / (1000 * 60 * 60 * 24)) <= 3;
};

onMounted(fetchNotices);
</script>

<style scoped>
.notice-page { padding-bottom: 60px; }
.page-header { margin-bottom: 40px; }
.page-title { font-size: 36px; }
.page-title .accent { color: var(--primary); }
.page-desc { color: var(--text-muted); }

.notice-container-card {
  border-radius: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: var(--font-heading);
  font-size: 18px;
}

.icon-main { color: var(--primary); font-size: 22px; }

.dark-search :deep(.el-input__wrapper) {
  background: rgba(0, 0, 0, 0.2) !important;
  border: 1px solid var(--border-color);
}

.notice-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 16px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s;
}

.notice-row:last-child { border-bottom: none; }

.notice-row:hover {
  background: rgba(255, 255, 255, 0.02);
  padding-left: 24px;
}

.notice-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.new-tag {
  border-radius: 4px;
  font-family: var(--font-heading);
}

.notice-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-main);
}

.notice-row:hover .notice-title { color: var(--primary); }

.notice-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  color: var(--text-muted);
  font-size: 14px;
}

.views { display: flex; align-items: center; gap: 6px; }
.arrow { opacity: 0; transition: all 0.3s; color: var(--primary); }
.notice-row:hover .arrow { opacity: 1; transform: translateX(5px); }

/* Detail */
.notice-meta-info {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 24px;
}

.info-item { display: flex; gap: 8px; font-size: 13px; }
.info-item .label { color: var(--text-muted); }
.info-item .val { color: var(--text-main); font-weight: 600; }

.dark-divider { border-color: var(--border-color); }

.notice-body {
  font-size: 16px;
  line-height: 2;
  color: var(--text-main);
  white-space: pre-wrap;
  padding: 0 20px;
}
</style>
