<template>
  <div class="community-detail-page full-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/community' }">竞技交流</el-breadcrumb-item>
        <el-breadcrumb-item>帖子详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：帖子详情与评论区 -->
      <el-col :span="18">
        <div v-loading="loading">
          <el-card shadow="never" class="post-card" v-if="post">
            <h1 class="p-title">{{ post.title }}</h1>
            <div class="p-meta">
              <div class="author-info">
                <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                <span class="nickname">{{ post.nickname }}</span>
                <span class="university">· {{ post.university }}</span>
              </div>
              <div class="time-views">
                <span class="tag"><el-tag size="small" type="info">{{ post.category }}</el-tag></span>
                <span class="tag"><el-tag size="small" type="success" effect="dark">{{ post.gameProject || '综合' }}</el-tag></span>
                <span class="time">{{ formatDate(post.createTime) }}</span>
                <span class="views"><el-icon><View /></el-icon> {{ post.views || post.viewCount || 0 }} 阅读</span>
              </div>
            </div>
            
            <div class="p-html" v-html="post.content"></div>
          </el-card>

          <!-- 评论区 -->
          <el-card shadow="never" class="comment-card" v-if="post" style="margin-top: 20px">
            <template #header>
              <div class="comment-header">
                <h3>全部评论 ({{ comments.length }})</h3>
              </div>
            </template>
            
            <div class="comment-list">
              <div v-for="c in comments" :key="c.id" class="comment-item">
                <el-avatar :size="40" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                <div class="c-content-box">
                  <div class="c-header">
                    <span class="c-author">{{ c.nickname }}</span>
                    <span class="c-time">{{ formatDate(c.createTime) }}</span>
                  </div>
                  <div class="c-body">{{ c.content }}</div>
                </div>
              </div>
              <el-empty v-if="comments.length === 0" description="暂无评论，快来发表你的看法吧" />
            </div>

            <!-- 发表评论框 -->
            <div class="comment-input-box" style="margin-top: 30px">
              <el-input 
                v-model="newComment" 
                type="textarea" 
                :rows="3" 
                placeholder="请友善发言，共建和谐电竞社区..." 
              />
              <div style="text-align: right; margin-top: 10px;">
                <el-button type="primary" size="large" @click="submitComment">发表评论</el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
      
      <!-- 右侧：侧边栏推荐 -->
      <el-col :span="6">
        <el-card shadow="never" class="sidebar-card">
          <template #header>关于作者</template>
          <div v-if="post" class="author-sidebar">
            <el-avatar :size="60" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <h3 style="margin: 10px 0 5px">{{ post.nickname }}</h3>
            <p style="margin: 0; color: #666; font-size: 13px">{{ post.university }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '../../store/user';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const route = useRoute();
const userStore = useUserStore();
const post = ref(null);
const comments = ref([]);
const loading = ref(false);
const newComment = ref('');

const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;
  
  loading.value = true;
  try {
    const res = await request.get(`/post/${id}`);
    post.value = res.data;
    if (post.value) {
      fetchComments(id);
    }
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const fetchComments = async (postId) => {
  try {
    const res = await request.get(`/post/comment/list/${postId}`);
    comments.value = res.data || [];
  } catch (err) {
    console.error(err);
  }
};

const submitComment = async () => {
  if (!userStore.token) return ElMessage.warning('请先登录');
  if (!newComment.value.trim()) return ElMessage.warning('评论内容不能为空');
  
  try {
    await request.post('/post/comment/add', {
      postId: post.value.id,
      content: newComment.value,
      userId: userStore.userInfo.id,
      nickname: userStore.userInfo.nickname || userStore.userInfo.username
    });
    ElMessage.success('评论成功');
    newComment.value = '';
    fetchComments(post.value.id);
  } catch (err) {
    console.error(err);
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
.community-detail-page { padding: 20px 0; }
.page-header { margin-bottom: 20px; }

.post-card { border-radius: 12px; padding: 20px; }
.p-title { font-size: 28px; font-weight: bold; color: #333; margin: 0 0 20px 0; }

.p-meta { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f0f2f5; padding-bottom: 15px; margin-bottom: 30px; }
.author-info { display: flex; align-items: center; gap: 10px; }
.author-info .nickname { font-weight: bold; color: #333; }
.author-info .university { color: #999; font-size: 13px; }

.time-views { display: flex; align-items: center; gap: 15px; font-size: 13px; color: #999; }
.tag { margin-right: 5px; }

.p-html { font-size: 16px; line-height: 1.8; color: #333; min-height: 200px; }
:deep(.p-html img) { max-width: 100%; height: auto; border-radius: 8px; }

.comment-card { border-radius: 12px; }
.comment-header h3 { margin: 0; font-size: 18px; color: #333; }

.comment-item { display: flex; gap: 15px; padding: 20px 0; border-bottom: 1px dashed #f0f0f0; }
.comment-item:last-child { border-bottom: none; }
.c-content-box { flex: 1; }
.c-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.c-author { font-weight: bold; color: #666; font-size: 14px; }
.c-time { color: #aaa; font-size: 12px; }
.c-body { font-size: 15px; color: #333; line-height: 1.6; }

.sidebar-card { border-radius: 12px; }
.author-sidebar { display: flex; flex-direction: column; align-items: center; padding: 10px 0; }
</style>
