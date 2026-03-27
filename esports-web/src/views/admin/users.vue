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
        <el-table-column prop="university" label="所属高校">
          <template #default="scope">
            <el-select 
              v-model="scope.row.university" 
              placeholder="选择所属高校" 
              clearable 
              filterable
              size="small"
              @change="(val) => handleUniversityChange(scope.row.id, val)"
            >
              <el-option v-for="uni in universityList" :key="uni" :label="uni" :value="uni" />
            </el-select>
          </template>
        </el-table-column>
        
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
              <el-option label="战队队员" value="ROLE_TEAM_MEMBER" />
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

const universityList = [
  "北京大学", "清华大学", "复旦大学", "上海交通大学", "浙江大学", "南京大学", "中国科学技术大学", 
  "华中科技大学", "四川大学", "哈尔滨工业大学", "同济大学", "北京航空航天大学", "东南大学", "南开大学", "天津大学", "山东大学", "中南大学", 
  "厦门大学", "吉林大学", "华南理工大学", "兰州大学", "东北大学", "大连理工大学", "湖南大学", "重庆大学", "西北工业大学", "中国农业大学", 
  "电子科技大学", "华东师范大学", "北京师范大学", "中央民族大学", "中国海洋大学", "国防科技大学", "西北农林科技大学", "北京理工大学",
  "北京工业大学", "北京交通大学", "北京科技大学", "北京化工大学", "北京邮电大学", "北京林业大学", "北京中医药大学", "北京外国语大学", 
  "中国传媒大学", "对外经济贸易大学", "中央音乐学院", "中国地质大学（北京）", "中国政法大学", "中国石油大学（北京）", "中国矿业大学（北京）", 
  "中国科学院大学", "中国社会科学院大学", "天津工业大学", "天津医科大学", "天津中医药大学", "河北工业大学", "太原理工大学", "内蒙古大学", 
  "辽宁大学", "大连海事大学", "延边大学", "东北师范大学", "哈尔滨工程大学", "东北农业大学", "东北林业大学", "华东理工大学", "东华大学", 
  "上海海洋大学", "上海中医药大学", "上海师范大学", "上海大学", "上海财经大学", "上海外国语大学", "上海音乐学院", "上海体育学院", "上海科技大学", 
  "苏州大学", "南京师范大学", "中国药科大学", "南京航空航天大学", "南京理工大学", "河海大学", "南京邮电大学", "南京信息工程大学", "南京农业大学", 
  "南京中医药大学", "中国地质大学（武汉）", "中国矿业大学", "中国石油大学（华东）", "江南大学", "中国地质大学", "合肥工业大学", "安徽大学", 
  "福州大学", "南昌大学", "郑州大学", "河南大学", "武汉理工大学", "华中师范大学", "华中农业大学", "中南财经政法大学", "湖南师范大学", 
  "暨南大学", "华南师范大学", "海南大学", "广西大学", "贵州大学", "云南大学", "西藏大学", "西北大学", "西安电子科技大学", "长安大学", 
  "陕西师范大学", "宁夏大学", "新疆大学", "石河子大学", "青海大学", "四川农业大学", "成都理工大学", "西南石油大学", "成都中医药大学", 
  "西南财经大学", "西南交通大学"
]; 

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

const handleUniversityChange = async (userId, university) => {
  try {
    await request.post(`/user/admin/university/${userId}?university=${encodeURIComponent(university || '')}`);
    ElMessage.success('所属高校更新成功');
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
