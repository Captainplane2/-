<template>
  <div class="my-posts-page full-container">
    <div class="page-header">
      <h1 class="page-title">我的帖子</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/community' }">竞技交流</el-breadcrumb-item>
        <el-breadcrumb-item>我的帖子</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选和排序 -->
    <div class="filter-sort-bar">
      <div class="filter-group">
        <el-select v-model="statusFilter" placeholder="状态筛选" @change="fetchMyPosts" class="status-select">
          <el-option label="全部" value="" />
          <el-option label="已发布" value="0" />
          <el-option label="草稿" value="1" />
        </el-select>
        <el-select v-model="sortBy" placeholder="排序方式" @change="fetchMyPosts" class="sort-select">
          <el-option label="发布时间" value="createTime" />
          <el-option label="浏览量" value="views" />
          <el-option label="评论数" value="comments" />
        </el-select>
        <el-select v-model="sortOrder" placeholder="排序方向" @change="fetchMyPosts" class="order-select">
          <el-option label="降序" value="desc" />
          <el-option label="升序" value="asc" />
        </el-select>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div v-loading="loading" class="posts-list">
      <el-card v-for="post in posts" :key="post.id" class="post-card" shadow="hover">
        <div class="post-header">
          <h2 class="post-title">{{ post.title }}</h2>
          <div class="post-meta">
            <el-tag size="small" type="info" class="meta-tag">{{ post.category }}</el-tag>
            <el-tag size="small" effect="plain" class="meta-tag">{{ post.gameProject || '综合' }}</el-tag>
            <el-tag v-if="post.status === 1" size="small" type="warning" class="meta-tag">草稿</el-tag>
          </div>
        </div>
        <p class="post-summary">{{ stripHtml(post.content).substring(0, 100) }}...</p>
        <div class="post-footer">
          <div class="post-stats">
            <span class="stat-item"><el-icon><View /></el-icon> {{ post.views || 0 }} 浏览</span>
            <span class="stat-item"><el-icon><ChatDotRound /></el-icon> {{ post.comments || 0 }} 评论</span>
            <span class="stat-item"><el-icon><Calendar /></el-icon> {{ formatDate(post.createTime) }}</span>
          </div>
          <div class="post-actions">
            <el-button type="primary" size="small" @click="editPost(post)">编辑</el-button>
            <el-button type="danger" size="small" @click="deletePost(post.id)">删除</el-button>
            <el-button v-if="post.status === 1" type="success" size="small" @click="publishPost(post.id)">发布</el-button>
          </div>
        </div>
      </el-card>

      <el-empty v-if="posts.length === 0 && !loading" description="您还没有发布过帖子" />
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 编辑帖子弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑帖子" width="800px" top="5vh">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="帖子标题" required>
          <el-input v-model="editForm.title" placeholder="请输入吸引人的标题..." />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属板块" required>
              <el-select v-model="editForm.category" class="w-full">
                <el-option label="赛事讨论" value="赛事讨论" />
                <el-option label="技术交流" value="技术交流" />
                <el-option label="组队开黑" value="组队开黑" />
                <el-option label="吃瓜灌水" value="吃瓜灌水" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联游戏" required>
              <el-select v-model="editForm.gameProject" class="w-full">
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
            <Editor style="height: 300px; overflow-y: hidden;" v-model="editForm.content" :mode="mode" @onCreated="handleEditorCreated" />
          </div>
        </el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="0">已发布</el-radio>
            <el-radio :label="1">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存修改</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { View, ChatDotRound, Calendar } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const posts = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const statusFilter = ref('');
const sortBy = ref('createTime');
const sortOrder = ref('desc');

// 编辑帖子
const editDialogVisible = ref(false);
const editorRef = shallowRef();
const mode = 'default';
const editForm = ref({
  id: null,
  title: '',
  category: '赛事讨论',
  gameProject: '综合',
  content: '',
  status: 0
});

const handleEditorCreated = (editor) => { editorRef.value = editor; };
onBeforeUnmount(() => { if (editorRef.value) editorRef.value.destroy(); });

const fetchMyPosts = async () => {
  if (!userStore.userInfo.id) return;
  
  loading.value = true;
  try {
    const params = {
      userId: userStore.userInfo.id,
      status: statusFilter.value,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value,
      page: currentPage.value,
      pageSize: pageSize.value
    };
    
    const res = await request.get('/post/my', { params });
    posts.value = res.data.list || [];
    total.value = res.data.total || 0;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchMyPosts();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  fetchMyPosts();
};

const editPost = (post) => {
  editForm.value = {
    id: post.id,
    title: post.title,
    category: post.category,
    gameProject: post.gameProject,
    content: post.content,
    status: post.status
  };
  editDialogVisible.value = true;
};

const deletePost = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？此操作不可撤销。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await request.post(`/post/delete/${id}`);
    ElMessage.success('帖子删除成功');
    fetchMyPosts();
  } catch (err) {
    if (err !== 'cancel') {
      console.error(err);
    }
  }
};

const publishPost = async (id) => {
  try {
    await request.post(`/post/publish/${id}`);
    ElMessage.success('帖子发布成功');
    fetchMyPosts();
  } catch (err) {
    console.error(err);
  }
};

const submitEdit = async () => {
  if (!editForm.value.title || !editForm.value.content || editForm.value.content === '<p><br></p>') {
    return ElMessage.warning('请填写完整标题和内容');
  }
  
  try {
    const postData = {
      ...editForm.value,
      userId: userStore.userInfo.id,
      nickname: userStore.userInfo.nickname || userStore.userInfo.username,
      university: userStore.userInfo.university
    };
    
    await request.post('/post/save', postData);
    ElMessage.success('帖子更新成功');
    editDialogVisible.value = false;
    fetchMyPosts();
  } catch (err) {
    console.error(err);
  }
};

const stripHtml = (html) => {
  let doc = new DOMParser().parseFromString(html, 'text/html');
  return doc.body.textContent || "";
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`;
};

onMounted(() => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  fetchMyPosts();
});
</script>

<style scoped>
.my-posts-page { padding: 20px 0; }
.page-header { margin-bottom: 30px; }
.page-title { font-size: 28px; font-weight: bold; color: #333; margin-bottom: 10px; }

.filter-sort-bar {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  gap: 15px;
  align-items: center;
}

.status-select,
.sort-select,
.order-select {
  min-width: 120px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
}

.post-card {
  border-radius: 8px;
  transition: all 0.2s;
}

.post-card:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.post-header {
  margin-bottom: 15px;
}

.post-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.post-meta {
  display: flex;
  gap: 10px;
  align-items: center;
}

.meta-tag {
  margin-right: 0;
}

.post-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #999;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.editor-container {
  border: 1px solid #ccc;
  width: 100%;
}

.w-full { width: 100%; }
</style>