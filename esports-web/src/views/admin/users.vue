<template>
  <div class="admin-users">
    <el-card shadow="never">
      <template #header>
        <div class="header-box">
          <span class="title">用户权限管理</span>
          <div class="filter-area">
            <el-input 
              v-model="queryParams.keyword" 
              placeholder="搜索邮箱或昵称" 
              clearable 
              style="width: 180px" 
              @keyup.enter="handleSearch"
            />
            <el-select 
              v-model="queryParams.university" 
              placeholder="高校筛选" 
              clearable 
              filterable
              style="width: 160px"
              @change="handleSearch"
            >
              <el-option v-for="uni in universityList" :key="uni" :label="uni" :value="uni" />
            </el-select>
            <el-input 
              v-model="queryParams.teamId" 
              placeholder="战队ID筛选" 
              clearable 
              style="width: 120px" 
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredUsers" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="注册邮箱" width="180" />
        <el-table-column prop="nickname" label="选手昵称" width="120" />
        <el-table-column prop="university" label="所属高校" />
        
        <!-- 竞技矩阵展示 -->
        <el-table-column label="竞技矩阵 (项目 / 战队)" min-width="240">
          <template #default="scope">
            <div class="project-tags">
              <el-tag 
                v-for="p in scope.row.projects" 
                :key="p.teamId" 
                :type="getProjectColor(p.projectName)"
                effect="dark"
                size="small"
                class="matrix-tag"
              >
                {{ p.projectName }}: {{ p.teamName }} (ID:{{ p.teamId }})
              </el-tag>
              <span v-if="!scope.row.projects || scope.row.projects.length === 0" class="no-data">无</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="身份权限" width="140">
          <template #default="scope">
            <el-select 
              v-model="scope.row.role" 
              size="small" 
              @change="(val) => handleRoleChange(scope.row.id, val)"
              :disabled="scope.row.id === userStore.userInfo.id"
            >
              <el-option label="普通用户" value="ROLE_USER" />
              <el-option label="战队队长" value="ROLE_LEADER" />
              <el-option label="管理员" value="ROLE_ADMIN" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(scope.row)"
              :disabled="scope.row.id === userStore.userInfo.id"
            >
              {{ scope.row.status === 1 ? '冻结' : '审核' }}
            </el-button>
            <el-popconfirm 
              title="确定要删除该用户吗？" 
              @confirm="handleDelete(scope.row.id)"
              v-if="scope.row.id !== userStore.userInfo.id"
            >
              <template #reference>
                <el-button size="small" type="danger" circle icon="Delete"></el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const users = ref([]);
const loading = ref(false);

const queryParams = ref({ keyword: '', university: '', teamId: '' });

const universityList = ["北京大学", "清华大学", "复旦大学", "上海交通大学", "浙江大学", "中山大学", "华南理工大学"]; 

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await request.get('/user/admin/list');
    users.value = res.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const matchKeyword = !queryParams.value.keyword || 
      user.username.toLowerCase().includes(queryParams.value.keyword.toLowerCase()) ||
      user.nickname.toLowerCase().includes(queryParams.value.keyword.toLowerCase());
    
    const matchUni = !queryParams.value.university || user.university === queryParams.value.university;
    
    let matchTeam = true;
    if (queryParams.value.teamId) {
      matchTeam = user.projects && user.projects.some(p => String(p.teamId) === String(queryParams.value.teamId));
    }

    return matchKeyword && matchUni && matchTeam;
  });
});

const getProjectColor = (project) => {
  const map = { 'LOL': '', '王者荣耀': 'success', 'CS2': 'warning', '无畏契约': 'danger' };
  return map[project] || 'info';
};

const handleSearch = () => { fetchUsers(); };

const toggleStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1;
  try {
    await request.post(`/user/admin/status/${user.id}?status=${newStatus}`);
    ElMessage.success('状态更新成功');
    fetchUsers();
  } catch (err) { console.error(err); }
};

const handleRoleChange = async (userId, role) => {
  try {
    await request.post(`/user/admin/role/${userId}?role=${role}`);
    ElMessage.success('权限设置成功');
    fetchUsers();
  } catch (err) { console.error(err); }
};

const handleDelete = async (id) => {
  try {
    await request.post(`/user/admin/delete/${id}`);
    ElMessage.success('用户已彻底移除');
    fetchUsers();
  } catch (err) { console.error(err); }
};

onMounted(fetchUsers);
</script>

<style scoped>
.header-box { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: bold; }
.filter-area { display: flex; gap: 12px; }
.project-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.matrix-tag { font-weight: 600; border-radius: 4px; }
.no-data { color: #999; font-size: 13px; }
</style>
