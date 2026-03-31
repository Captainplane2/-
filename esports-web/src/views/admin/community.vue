<template>
  <div class="admin-community">
    <div class="page-header">
      <h1 class="page-title">社区内容管理</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/admin' }">管理后台</el-breadcrumb-item>
        <el-breadcrumb-item>社区内容</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card shadow="never" class="search-card">
      <div class="search-form">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索帖子标题或内容"
          style="width: 300px; margin-right: 10px;"
        >
          <template #append>
            <el-button @click="fetchPosts">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-select
          v-model="searchForm.gameProject"
          placeholder="选择游戏项目"
          style="width: 180px; margin-right: 10px;"
          @change="fetchPosts"
        >
          <el-option label="全部项目" value=""></el-option>
          <el-option
            v-for="project in gameProjects"
            :key="project.name"
            :label="project.name"
            :value="project.name"
          />
        </el-select>
        <el-select
          v-model="searchForm.sortBy"
          placeholder="排序方式"
          style="width: 120px;"
          @change="fetchPosts"
        >
          <el-option label="最新发布" value="createTime"></el-option>
          <el-option label="最多浏览" value="views"></el-option>
          <el-option label="最多评论" value="comments"></el-option>
        </el-select>
      </div>
    </el-card>

    <el-card shadow="never" class="content-card">
      <el-table :data="posts" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="标题" min-width="300">
          <template #default="scope">
            <span class="post-title">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="gameProject" label="游戏项目" width="120" />
        <el-table-column prop="category" label="板块" width="120" />
        <el-table-column prop="nickname" label="作者" width="120" />
        <el-table-column prop="views" label="浏览量" width="80" />
        <el-table-column prop="comments" label="评论数" width="80" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              @click="confirmDelete(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import request from '../../utils/request';

// 搜索表单
const searchForm = ref({
  keyword: '',
  gameProject: '',
  sortBy: 'createTime',
  sortOrder: 'desc'
});

// 数据
const posts = ref([]);
const gameProjects = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 获取游戏项目列表
const fetchGameProjects = async () => {
  try {
    const response = await request.get('/game-project/list');
    gameProjects.value = response.data || [];
  } catch (error) {
    console.error('获取游戏项目列表失败:', error);
  }
};

// 获取帖子列表
const fetchPosts = async () => {
  loading.value = true;
  try {
    const response = await request.get('/post/list', {
      params: {
        keyword: searchForm.value.keyword,
        gameProject: searchForm.value.gameProject,
        sortBy: searchForm.value.sortBy,
        sortOrder: searchForm.value.sortOrder
      }
    });
    posts.value = response.data || [];
    total.value = posts.value.length;
  } catch (error) {
    console.error('获取帖子列表失败:', error);
    ElMessage.error('获取帖子列表失败');
  } finally {
    loading.value = false;
  }
};

// 确认删除
const confirmDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条帖子吗？将连同评论一起删除。', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/post/delete/${id}`);
      ElMessage.success('删除成功');
      fetchPosts();
    } catch (error) {
      console.error('删除帖子失败:', error);
      ElMessage.error('删除失败');
    }
  }).catch(() => {
    // 取消删除
  });
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  fetchPosts();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  fetchPosts();
};

// 初始化
onMounted(() => {
  fetchGameProjects();
  fetchPosts();
});
</script>

<style scoped>
.admin-community {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  align-items: center;
}

.content-card {
  margin-bottom: 20px;
}

.post-title {
  font-weight: 500;
  color: #333;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
