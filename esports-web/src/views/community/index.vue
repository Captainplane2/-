<template>
  <div class="community-page full-container">
    <div class="page-header">
      <h1 class="page-title">竞技交流</h1>
      <el-button type="primary" icon="Edit" @click="showPostDialog = true">发布动态</el-button>
    </div>

    <el-row :gutter="24">
      <el-col :span="18">
        <div class="content-box">
          <el-tabs v-model="queryProject" @tab-change="fetchPosts">
            <el-tab-pane label="全部分类" name="" />
            <el-tab-pane label="英雄联盟" name="LOL" />
            <el-tab-pane label="王者荣耀" name="王者荣耀" />
            <el-tab-pane label="CS2" name="CS2" />
          </el-tabs>

          <div v-loading="loading" class="post-list">
            <div v-for="post in posts" :key="post.id" class="post-item" @click="viewPost(post.id)">
              <div class="post-main">
                <h3 class="post-title">{{ post.title }}</h3>
                <p class="post-content">{{ truncateContent(post.content) }}</p>
                <div class="post-footer">
                  <div class="meta">
                    <span class="user">选手 #{{ post.userId }}</span>
                    <span class="time">{{ formatTime(post.createTime) }}</span>
                    <el-tag size="small" effect="plain">{{ post.gameProject }}</el-tag>
                  </div>
                  <div class="stats">
                    <span><el-icon><View /></el-icon> {{ post.views }}</span>
                    <span><el-icon><Star /></el-icon> {{ post.likes }}</span>
                    <span><el-icon><ChatLineRound /></el-icon> {{ post.comments }}</span>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-if="posts.length === 0" description="暂无讨论内容" />
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <el-card class="side-card" header="社区热议">
          <div class="hot-list">
            <div v-for="(p, index) in posts.slice(0, 5)" :key="p.id" class="hot-item">
              <span class="rank">{{ index + 1 }}</span>
              <span class="hot-title">{{ p.title }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 弹窗部分 -->
    <el-dialog v-model="showPostDialog" title="发表新帖" width="600px">
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="postForm.title" />
        </el-form-item>
        <el-form-item label="竞技项目">
          <el-select v-model="postForm.gameProject" class="w-full">
            <el-option label="英雄联盟" value="LOL" />
            <el-option label="王者荣耀" value="王者荣耀" />
            <el-option label="CS2" value="CS2" />
          </el-select>
        </el-form-item>
        <el-form-item label="正文内容">
          <el-input v-model="postForm.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublishPost">立即发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '../../store/user';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const posts = ref([]);
const loading = ref(false);
const queryProject = ref('');
const showPostDialog = ref(false);

const postForm = ref({
  title: '',
  content: '',
  gameProject: '',
  userId: userStore.userInfo.id
});

const fetchPosts = async () => {
  loading.value = true;
  try {
    const res = await request.get('/community/post/list', { params: { gameProject: queryProject.value } });
    posts.value = res.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handlePublishPost = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    return;
  }
  try {
    await request.post('/community/post/publish', postForm.value);
    ElMessage.success('发布成功');
    showPostDialog.value = false;
    fetchPosts();
  } catch (err) {
    console.error(err);
  }
};

const formatTime = (t) => t?.split('T')[0] || '';
const truncateContent = (c) => c.length > 80 ? c.substring(0, 80) + '...' : c;

onMounted(fetchPosts);
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #333;
}

.content-box {
  background-color: var(--bg-white);
  padding: 20px;
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.post-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background 0.2s;
}

.post-item:hover {
  background-color: #fafafa;
}

.post-title {
  font-size: 17px;
  font-weight: bold;
  margin-bottom: 10px;
}

.post-content {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  line-height: 1.6;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #999;
}

.meta {
  display: flex;
  gap: 15px;
  align-items: center;
}

.stats {
  display: flex;
  gap: 20px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.rank {
  font-family: serif;
  font-style: italic;
  font-weight: bold;
  color: var(--primary);
}

.hot-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.w-full { width: 100%; }
</style>
