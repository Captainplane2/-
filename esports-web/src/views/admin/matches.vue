<template>
  <div class="admin-matches">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>约赛记录管理</span>
        </div>
      </template>

      <el-table :data="matches" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="gameProject" label="项目" width="120" />
        <el-table-column prop="matchTime" label="比赛时间" width="180" />
        <el-table-column prop="mode" label="模式" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editMatch(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteMatch(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const matches = ref([]);
const loading = ref(false);

const fetchMatches = async () => {
  loading.value = true;
  try {
    const res = await request.get('/match/admin/list');
    matches.value = res.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const editMatch = (match) => {
  // 打开编辑对话框
  ElMessage.warning('编辑功能正在开发中');
};

const deleteMatch = async (id) => {
  try {
    await request.post(`/match-room/cancel/${id}`);
    ElMessage.success('约战已删除');
    fetchMatches();
  } catch (err) {
    console.error(err);
  }
};

const getStatusText = (status) => {
  const map = { 0: '待匹配', 1: '已匹配', 2: '待开赛', 3: '进行中', 4: '已结束', 5: '已拒绝' };
  return map[status] || '未知';
};

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'danger', 4: 'success', 5: 'info' };
  return map[status] || 'info';
};

onMounted(fetchMatches);
</script>
