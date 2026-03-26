<template>
  <div class="admin-teams">
    <el-card shadow="never">
      <template #header>
        <div class="header-box">
          <span class="title">战队管理</span>
        </div>
      </template>

      <el-table 
        :data="teams" 
        border 
        stripe 
        v-loading="loading"
        @row-click="handleRowClick"
        row-style="cursor: pointer"
      >
        <el-table-column prop="id" label="战队ID" width="100" />
        <el-table-column prop="name" label="战队名称" />
        <el-table-column prop="university" label="所属高校" />
        <el-table-column prop="gameProject" label="竞技项目" width="120">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.gameProject }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="当前人数" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">
              {{ scope.row.status === 0 ? '正常' : '已解散' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 0"
              size="small" 
              type="danger"
              @click.stop="confirmDissolve(scope.row.id)"
            >强制解散</el-button>
            <el-button 
              v-else
              size="small" 
              type="danger" 
              link
              @click.stop="confirmRealDelete(scope.row.id)"
            >删除该记录</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 成员详情弹窗 -->
    <el-dialog v-model="memberVisible" :title="`战队成员名单 - ${currentTeamName}`" width="600px" center>
      <el-table :data="members" border stripe size="small">
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="username" label="邮箱账号" />
        <el-table-column label="队内角色">
          <template #default="scope">
            <el-tag :type="scope.row.role === 1 ? 'danger' : 'info'" size="small">
              {{ scope.row.role === 1 ? '队长' : '正式成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="加入时间" width="160" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';

const teams = ref([]);
const loading = ref(false);
const memberVisible = ref(false);
const members = ref([]);
const currentTeamName = ref('');

const fetchTeams = async () => {
  loading.value = true;
  try {
    const res = await request.get('/team/admin/list');
    teams.value = res.data || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleRowClick = async (row) => {
  currentTeamName.value = row.name;
  try {
    const res = await request.get(`/team/members/${row.id}`);
    members.value = res.data || [];
    memberVisible.value = true;
  } catch (err) {
    console.error(err);
  }
};

const confirmDissolve = (id) => {
  ElMessageBox.confirm(
    '确认解散该队伍？',
    '提示',
    {
      confirmButtonText: '确定解散',
      cancelButtonText: '取消',
      type: 'warning',
      center: true
    }
  ).then(() => {
    handleDissolve(id);
  }).catch(() => {});
};

const handleDissolve = async (id) => {
  try {
    await request.post(`/team/admin/delete/${id}`);
    ElMessage.success('战队已强制解散');
    fetchTeams();
  } catch (err) {
    console.error(err);
  }
};

const confirmRealDelete = (id) => {
  ElMessageBox.confirm(
    '确认永久删除该战队记录？此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
      center: true
    }
  ).then(async () => {
    try {
      await request.post(`/team/admin/real-delete/${id}`);
      ElMessage.success('战队记录已彻底删除');
      fetchTeams();
    } catch (err) {
      console.error(err);
    }
  }).catch(() => {});
};

onMounted(fetchTeams);
</script>

<style scoped>
.header-box { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: bold; }
</style>
