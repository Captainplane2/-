<template>
  <div class="community-page full-container">
    <div class="page-header">
      <h1 class="page-title">竞技交流</h1>
      <el-button type="primary" size="large" icon="EditPen" @click="handleCreateClick">发布新帖</el-button>
    </div>

    <!-- 搜索和排序 -->
    <div class="search-sort-bar">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索帖子标题或内容..."
          class="search-input"
          @keyup.enter="fetchPosts"
        >
          <template #append>
            <el-button @click="fetchPosts" icon="Search" />
          </template>
        </el-input>
      </div>
      <div class="sort-box">
        <el-select v-model="sortBy" @change="fetchPosts" class="sort-select">
          <el-option label="最新发布" value="createTime" />
          <el-option label="热度排序" value="views" />
          <el-option label="评论最多" value="comments" />
        </el-select>
        <el-select v-model="sortOrder" @change="fetchPosts" class="order-select">
          <el-option label="降序" value="desc" />
          <el-option label="升序" value="asc" />
        </el-select>
      </div>
    </div>

    <div class="community-content">
      <!-- 左侧分类与项目过滤 -->
      <div class="sidebar">
        <el-card shadow="never" class="filter-card">
          <div class="filter-title">讨论板块</div>
          <el-menu :default-active="filterCategory" class="filter-menu" @select="handleCategorySelect">
            <el-menu-item index="全部">全部板块</el-menu-item>
            <el-menu-item index="赛事讨论">赛事讨论</el-menu-item>
            <el-menu-item index="技术交流">技术交流</el-menu-item>
            <el-menu-item index="组队开黑">组队开黑</el-menu-item>
            <el-menu-item index="吃瓜灌水">吃瓜灌水</el-menu-item>
          </el-menu>

          <div class="filter-title" style="margin-top: 20px;">游戏专区</div>
          <el-radio-group v-model="filterProject" @change="fetchPosts" class="project-radio" size="small">
            <el-radio-button label="全部">全部</el-radio-button>
            <el-radio-button label="LOL">LOL</el-radio-button>
            <el-radio-button label="王者荣耀">王者</el-radio-button>
            <el-radio-button label="CS2">CS2</el-radio-button>
          </el-radio-group>
        </el-card>
      </div>

      <!-- 右侧帖子列表 -->
      <div class="main-list" v-loading="loading">
        <el-card v-for="post in posts" :key="post.id" class="post-card" shadow="hover" @click="goDetail(post.id)">
          <div class="post-header">
            <div class="post-meta">
              <el-tag size="small" type="info" class="meta-tag">{{ post.category }}</el-tag>
              <el-tag size="small" effect="plain" class="meta-tag">{{ post.gameProject || '综合' }}</el-tag>
            </div>
            <span class="post-time">{{ formatDate(post.createTime) }}</span>
          </div>
          <h2 class="post-title">{{ post.title }}</h2>
          <p class="post-summary">{{ stripHtml(post.content).substring(0, 100) }}...</p>
          
          <div class="post-footer">
            <div class="author-info">
              <el-avatar :size="24" :src="post.avatar ? (post.avatar.startsWith('http') ? post.avatar : env.getFullApiUrl(post.avatar)) : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <span class="nickname">{{ post.nickname }}</span>
              <span class="university">· {{ post.university }}</span>
            </div>
            <div class="stats-info">
              <span class="stat-item"><el-icon><View /></el-icon> {{ post.views || 0 }}</span>
              <span class="stat-item"><el-icon><ChatDotRound /></el-icon> {{ post.comments || 0 }}</span>
            </div>
          </div>
        </el-card>

        <el-empty v-if="posts.length === 0" description="该板块暂无帖子，快来抢占沙发吧！" />
      </div>
    </div>

    <!-- 发帖弹窗 -->
    <el-dialog v-model="createDialogVisible" title="发布新帖" width="800px" top="5vh">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="帖子标题" required>
          <el-input v-model="createForm.title" placeholder="请输入吸引人的标题..." />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属板块" required>
              <el-select v-model="createForm.category" class="w-full">
                <el-option label="赛事讨论" value="赛事讨论" />
                <el-option label="技术交流" value="技术交流" />
                <el-option label="组队开黑" value="组队开黑" />
                <el-option label="吃瓜灌水" value="吃瓜灌水" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联游戏" required>
              <el-select v-model="createForm.gameProject" class="w-full">
                <el-option label="综合讨论" value="综合" />
                <el-option label="英雄联盟" value="LOL" />
                <el-option label="王者荣耀" value="王者荣耀" />
                <el-option label="CS2" value="CS2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="帖子正文" required>
          <div class="editor-container">
            <Toolbar style="border-bottom: 1px solid #ccc" :editor="editorRef" :mode="mode" />
            <Editor style="height: 300px; overflow-y: hidden;" v-model="createForm.content" :mode="mode" @onCreated="handleCreated" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css'
import { ref, onMounted, shallowRef, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { useUserStore } from '../../store/user';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';
import env from '../../config/env';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const posts = ref([]);

// 筛选
const filterCategory = ref('全部');
const filterProject = ref('全部');
const searchKeyword = ref('');
const sortBy = ref('createTime');
const sortOrder = ref('desc');

// 发帖编辑器
const createDialogVisible = ref(false);
const editorRef = shallowRef();
const mode = 'default';
const createForm = ref({
  title: '',
  category: '赛事讨论',
  gameProject: '综合',
  content: '',
  userId: userStore.userInfo.id,
  nickname: userStore.userInfo.nickname || userStore.userInfo.username,
  university: userStore.userInfo.university || '未知高校'
});

const handleCreated = (editor) => { editorRef.value = editor; };
onBeforeUnmount(() => { if (editorRef.value) editorRef.value.destroy(); });

const fetchPosts = async () => {
  loading.value = true;
  try {
    const params = {};
    if (filterCategory.value !== '全部') params.category = filterCategory.value;
    if (filterProject.value !== '全部') params.gameProject = filterProject.value;
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    params.sortBy = sortBy.value;
    params.sortOrder = sortOrder.value;
    
    const res = await request.get('/post/list', { params });
    posts.value = res.data || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleCategorySelect = (index) => {
  filterCategory.value = index;
  fetchPosts();
};

const handleCreateClick = () => {
  if (!userStore.token) return ElMessage.warning('请先登录后发帖');
  // 重置表单，防止编辑状态遗留
  createForm.value = {
    id: null,
    title: '',
    category: '赛事讨论',
    gameProject: '综合',
    content: ''
  };
  createDialogVisible.value = true;
};

const submitPost = async () => {
  if (!createForm.value.title || !createForm.value.content || createForm.value.content === '<p><br></p>') {
    return ElMessage.warning('请填写完整标题和内容');
  }
  try {
    const postData = {
      ...createForm.value,
      userId: userStore.userInfo.id,
      nickname: userStore.userInfo.nickname || userStore.userInfo.username,
      university: userStore.userInfo.university
    };
    
    await request.post('/post/save', postData);
    ElMessage.success(createForm.value.id ? '更新成功' : '发帖成功');
    createDialogVisible.value = false;
    fetchPosts();
  } catch (err) {
    console.error(err);
  }
};

const goDetail = (id) => {
  router.push(`/community/${id}`);
};

const stripHtml = (html) => {
  let doc = new DOMParser().parseFromString(html, 'text/html');
  return doc.body.textContent || "";
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getMonth()+1}-${date.getDate()} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`;
};

onMounted(fetchPosts);
</script>

<style scoped>
.community-page { padding: 20px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 28px; font-weight: bold; color: #333; }

.search-sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.search-box {
  flex: 1;
  max-width: 500px;
}

.search-input {
  width: 100%;
}

.sort-box {
  display: flex;
  gap: 10px;
  align-items: center;
}

.sort-select,
.order-select {
  min-width: 120px;
}

.community-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
}

.filter-card { border-radius: 8px; }
.filter-title { font-size: 14px; font-weight: bold; color: #999; margin-bottom: 10px; padding-left: 10px; }
.filter-menu { border-right: none; }
.filter-menu :deep(.el-menu-item) { height: 40px; line-height: 40px; border-radius: 4px; }
.filter-menu :deep(.el-menu-item.is-active) { background-color: #f0f7ff; font-weight: bold; }
.project-radio { display: flex; flex-direction: column; gap: 10px; padding-left: 10px; }

.main-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-card { border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.post-card:hover { border-color: var(--primary); }

.post-header { display: flex; justify-content: space-between; margin-bottom: 10px; }
.meta-tag { margin-right: 8px; }
.post-time { font-size: 13px; color: #999; }

.post-title { font-size: 18px; font-weight: bold; margin-bottom: 10px; color: #333; }
.post-summary { font-size: 14px; color: #666; line-height: 1.5; margin-bottom: 15px; }

.post-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f5f5f5; padding-top: 12px; }
.author-info { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #666; }
.nickname { font-weight: 500; color: #333; }
.stats-info { display: flex; gap: 15px; font-size: 13px; color: #999; }
.stat-item { display: flex; align-items: center; gap: 4px; }

.w-full { width: 100%; }
.editor-container { border: 1px solid #ccc; width: 100%; }

/* 详情弹窗样式 */
.post-detail-content { padding: 0 10px; }
.detail-meta { display: flex; gap: 15px; font-size: 13px; color: #999; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
.detail-meta .author { color: var(--primary); font-weight: bold; }
.detail-html { line-height: 1.8; color: #333; font-size: 15px; min-height: 100px; margin-bottom: 30px; }

.comment-list { max-height: 300px; overflow-y: auto; margin-bottom: 20px; padding-right: 10px; }
.comment-item { padding: 12px 0; border-bottom: 1px dashed #eee; }
.c-header { display: flex; justify-content: space-between; margin-bottom: 5px; }
.c-author { font-size: 13px; font-weight: bold; color: #666; }
.c-time { font-size: 12px; color: #aaa; }
.c-body { font-size: 14px; color: #333; }

.comment-input-box { display: flex; flex-direction: column; align-items: flex-end; gap: 10px; background: #fafafa; padding: 15px; border-radius: 8px; }
.send-btn { width: 100px; }
</style>
