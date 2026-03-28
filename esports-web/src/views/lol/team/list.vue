<template>
  <div class="lol-team-list-page">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/lol' }">LOL首页</el-breadcrumb-item>
        <el-breadcrumb-item>战队列表</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>英雄联盟战队列表</h1>
    </div>

    <div class="team-filter">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索战队名称"
        class="search-input"
        @keyup.enter="fetchTeams"
      >
        <template #append>
          <el-button @click="fetchTeams"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="$router.push('/lol/team/manage')">创建战队</el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6" v-for="team in teams" :key="team.id">
        <el-card class="team-card" :body-style="{ padding: '0px' }" shadow="hover" @click="goToTeamDetail(team.id)">
          <div class="team-cover">
            <img :src="team.logo || 'https://via.placeholder.com/300x200/eee/999?text=TEAM'" class="cover-img">
          </div>
          <div class="team-info">
            <h3 class="team-name">{{ team.name }}</h3>
            <p class="team-uni"><el-icon><School /></el-icon> {{ team.university }}</p>
            <p class="team-desc">{{ team.description || '该战队很懒，暂无简介...' }}</p>
            <div class="team-footer">
              <span class="member-count"><el-icon><User /></el-icon> {{ team.memberCount }}人</span>
              <el-button type="primary" link @click.stop="goToTeamDetail(team.id)">详情</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '../../../utils/request';

const router = useRouter();
const loading = ref(false);
const teams = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(12);
const searchKeyword = ref('');

const fetchTeams = async () => {
  loading.value = true;
  try {
    const res = await request.get('/team/list', {
      params: {
        gameProject: 'LOL',
        name: searchKeyword.value,
        page: currentPage.value,
        pageSize: pageSize.value
      }
    });
    teams.value = res.data || [];
    total.value = res.total || 0;
  } catch (err) {
    console.error('获取战队列表失败', err);
  } finally {
    loading.value = false;
  }
};

const goToTeamDetail = (teamId) => {
  router.push(`/lol/team/${teamId}`);
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchTeams();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  fetchTeams();
};

onMounted(() => {
  fetchTeams();
});
</script>

<style scoped>
.lol-team-list-page {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 10px 0 0 0;
}

.team-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.search-input {
  width: 300px;
}

.team-card {
  transition: transform 0.3s;
  cursor: pointer;
}

.team-card:hover {
  transform: translateY(-5px);
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

.team-card:hover .cover-img {
  transform: scale(1.05);
}

.team-info {
  padding: 15px;
}

.team-name {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.team-uni {
  font-size: 14px;
  color: #999;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.team-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 15px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.team-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-count {
  font-size: 14px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}
</style>