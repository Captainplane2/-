<template>
  <div class="players-page">
    <div class="page-header">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="gameProject">{{ gameProjectDisplayName }}</el-breadcrumb-item>
        <el-breadcrumb-item>选手信息</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card shadow="never" class="players-card">
      <template #header>
        <div class="filter-area">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="搜索选手昵称或战队" 
            clearable 
            style="width: 200px" 
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select 
            v-model="queryParams.university" 
            placeholder="高校筛选" 
            clearable 
            filterable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option 
              v-for="uni in universityList" 
              :key="uni" 
              :label="uni" 
              :value="uni" 
            />
          </el-select>

          <el-select 
            v-model="queryParams.teamId" 
            placeholder="战队筛选" 
            clearable 
            filterable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option 
              v-for="team in teamList" 
              :key="team.id" 
              :label="team.name" 
              :value="team.id" 
            />
          </el-select>

          <el-select 
            v-model="queryParams.role" 
            placeholder="身份筛选" 
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="战队队长" value="ROLE_LEADER" />
            <el-option label="战队成员" value="ROLE_TEAM_MEMBER" />
          </el-select>

          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </template>

      <el-table 
        :data="filteredPlayers" 
        border 
        stripe 
        v-loading="loading"
        :default-sort="{ prop: 'id', order: 'ascending' }"
      >
        <el-table-column type="index" label="序号" width="70" align="center" />
        
        <el-table-column label="选手信息" min-width="200">
          <template #default="scope">
            <div class="player-info">
              <el-avatar 
                :size="50" 
                :src="getAvatarUrl(scope.row.avatar)" 
                class="player-avatar"
              />
              <div class="player-details">
                <div class="player-name">{{ scope.row.nickname || scope.row.username }}</div>
                <div class="player-university">
                  <el-icon><School /></el-icon>
                  {{ scope.row.university || '未绑定高校' }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="所属战队" min-width="180">
          <template #default="scope">
            <div class="team-info">
              <div v-if="scope.row.teams && scope.row.teams.length > 0">
                <el-tag 
                  v-for="team in scope.row.teams" 
                  :key="team.id"
                  :type="getTeamTagType(team.role)"
                  effect="dark"
                  size="small"
                  class="team-tag"
                >
                  {{ team.name }}
                  <span class="role-badge">{{ getRoleText(team.role) }}</span>
                </el-tag>
              </div>
              <span v-else class="no-team">未加入战队</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="游戏项目" width="120" align="center">
          <template #default="scope">
            <div class="game-projects">
              <el-tag 
                v-for="project in scope.row.gameProjects" 
                :key="project"
                :type="getProjectColor(project)"
                effect="plain"
                size="small"
                class="project-tag"
              >
                {{ project }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="身份" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)" effect="dark" size="small">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="150" align="center" sortable>
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary"
              @click="viewPlayerDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-area">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useGameProjectStore } from '../../store/gameProject';
import { ElMessage } from 'element-plus';
import { Search, School } from '@element-plus/icons-vue';
import request from '../../utils/request';

const route = useRoute();
const router = useRouter();
const gameProjectStore = useGameProjectStore();

// 游戏板块信息
const gameProject = computed(() => route.params.gameProject?.toUpperCase() || null);
const gameProjectDisplayName = computed(() => {
  if (!gameProject.value) return '全部游戏';
  const project = gameProjectStore.getGameProjectByName(gameProject.value);
  return project ? project.displayName : gameProject.value;
});

const pageTitle = computed(() => {
  return gameProject.value ? `${gameProjectDisplayName.value} - 选手信息` : '选手信息';
});

// 默认头像
const defaultAvatar = 'https://via.placeholder.com/150/ccc/999?text=头像';

// 获取头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return defaultAvatar;
  // 如果已经是完整的URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  // 如果是相对路径，添加后端服务器地址
  if (avatar.startsWith('/')) {
    return 'http://localhost:8081' + avatar;
  }
  // 否则直接返回
  return avatar;
};

// 加载状态
const loading = ref(false);

// 选手列表
const players = ref([]);
const teamList = ref([]);

// 查询参数
const queryParams = ref({
  keyword: '',
  university: '',
  teamId: '',
  role: ''
});

// 分页
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
});

// 高校列表
const universityList = [
  "北京大学", "清华大学", "复旦大学", "上海交通大学", "浙江大学", "南京大学", "中国科学技术大学", 
  "华中科技大学", "四川大学", "哈尔滨工业大学", "同济大学", "北京航空航天大学", "东南大学", 
  "南开大学", "天津大学", "山东大学", "中南大学", "厦门大学", "吉林大学", "华南理工大学", 
  "兰州大学", "东北大学", "大连理工大学", "湖南大学", "重庆大学", "西北工业大学", "中国农业大学", 
  "电子科技大学", "华东师范大学", "北京师范大学", "中央民族大学", "中国海洋大学", "国防科技大学", 
  "西北农林科技大学", "北京理工大学", "北京工业大学", "北京交通大学", "北京科技大学", "北京化工大学", 
  "北京邮电大学", "北京林业大学", "北京中医药大学", "北京外国语大学", "中国传媒大学", "对外经济贸易大学", 
  "中央音乐学院", "中国地质大学（北京）", "中国政法大学", "中国石油大学（北京）", "中国矿业大学（北京）", 
  "中国科学院大学", "中国社会科学院大学", "天津工业大学", "天津医科大学", "天津中医药大学", 
  "河北工业大学", "太原理工大学", "内蒙古大学", "辽宁大学", "大连海事大学", "延边大学", "东北师范大学", 
  "哈尔滨工程大学", "东北农业大学", "东北林业大学", "华东理工大学", "东华大学", "上海海洋大学", 
  "上海中医药大学", "上海师范大学", "上海大学", "上海财经大学", "上海外国语大学", "上海音乐学院", 
  "上海体育学院", "上海科技大学", "苏州大学", "南京师范大学", "中国药科大学", "南京航空航天大学", 
  "南京理工大学", "河海大学", "南京邮电大学", "南京信息工程大学", "南京农业大学", "南京中医药大学", 
  "中国地质大学（武汉）", "中国矿业大学", "中国石油大学（华东）", "江南大学", "中国地质大学", 
  "合肥工业大学", "安徽大学", "福州大学", "南昌大学", "郑州大学", "河南大学", "武汉理工大学", 
  "华中师范大学", "华中农业大学", "中南财经政法大学", "湖南师范大学", "暨南大学", "华南师范大学", 
  "海南大学", "广西大学", "贵州大学", "云南大学", "西藏大学", "西北大学", "西安电子科技大学", 
  "长安大学", "陕西师范大学", "宁夏大学", "新疆大学", "石河子大学", "青海大学", "四川农业大学", 
  "成都理工大学", "西南石油大学", "成都中医药大学", "西南财经大学", "西南交通大学"
];

// 获取选手列表
const fetchPlayers = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.value.current,
      size: pagination.value.size,
      gameProject: gameProject.value,
      ...queryParams.value
    };
    
    const res = await request.get('/players/list', { params });
    if (res.code === 200) {
      players.value = res.data.records || [];
      pagination.value.total = res.data.total || 0;
      
      // 调试：查看返回的头像字段
      if (players.value.length > 0) {
        console.log('第一个选手的头像字段:', players.value[0].avatar);
        console.log('第一个选手的完整数据:', players.value[0]);
      }
    }
  } catch (err) {
    console.error('获取选手列表失败:', err);
    ElMessage.error('获取选手列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取战队列表
const fetchTeams = async () => {
  try {
    const params = gameProject.value ? { gameProject: gameProject.value } : {};
    const res = await request.get('/team/list', { params });
    if (res.code === 200) {
      teamList.value = res.data || [];
    }
  } catch (err) {
    console.error('获取战队列表失败:', err);
  }
};

// 筛选后的选手列表
const filteredPlayers = computed(() => {
  return players.value.filter(player => {
    // 关键词筛选
    const matchKeyword = !queryParams.value.keyword || 
      (player.nickname && player.nickname.toLowerCase().includes(queryParams.value.keyword.toLowerCase())) ||
      (player.username && player.username.toLowerCase().includes(queryParams.value.keyword.toLowerCase())) ||
      (player.teams && player.teams.some(t => t.name.toLowerCase().includes(queryParams.value.keyword.toLowerCase())));
    
    // 高校筛选
    const matchUni = !queryParams.value.university || player.university === queryParams.value.university;
    
    // 战队筛选
    let matchTeam = true;
    if (queryParams.value.teamId) {
      matchTeam = player.teams && player.teams.some(t => String(t.id) === String(queryParams.value.teamId));
    }

    // 身份筛选
    let matchRole = true;
    if (queryParams.value.role) {
      matchRole = player.role === queryParams.value.role;
    }

    return matchKeyword && matchUni && matchTeam && matchRole;
  });
});

// 搜索
const handleSearch = () => {
  pagination.value.current = 1;
  fetchPlayers();
};

// 重置筛选
const resetFilter = () => {
  queryParams.value = {
    keyword: '',
    university: '',
    teamId: '',
    role: ''
  };
  pagination.value.current = 1;
  fetchPlayers();
};

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.value.size = size;
  fetchPlayers();
};

// 页码变化
const handleCurrentChange = (current) => {
  pagination.value.current = current;
  fetchPlayers();
};

// 查看选手详情
const viewPlayerDetail = (player) => {
  router.push(`/user/info/${player.id}`);
};

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    'ROLE_LEADER': '队长',
    'ROLE_TEAM_MEMBER': '成员',
    'ROLE_USER': '用户',
    'ROLE_ADMIN': '管理员'
  };
  return roleMap[role] || '用户';
};

// 获取角色标签类型
const getRoleTagType = (role) => {
  const typeMap = {
    'ROLE_LEADER': 'danger',
    'ROLE_TEAM_MEMBER': 'primary',
    'ROLE_USER': 'info',
    'ROLE_ADMIN': 'warning'
  };
  return typeMap[role] || 'info';
};

// 获取战队标签类型
const getTeamTagType = (role) => {
  return role === 'ROLE_LEADER' ? 'danger' : 'primary';
};

// 获取项目颜色
const getProjectColor = (project) => {
  const colorMap = { 
    'LOL': 'success', 
    '王者荣耀': 'warning', 
    'CS2': 'primary', 
    '无畏契约': 'danger',
    'DOTA2': 'info'
  };
  return colorMap[project] || 'info';
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 监听游戏板块变化
watch(gameProject, () => {
  pagination.value.current = 1;
  fetchPlayers();
  fetchTeams();
});

onMounted(() => {
  fetchPlayers();
  fetchTeams();
});
</script>

<style scoped>
.players-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.players-card {
  border-radius: 8px;
}

.filter-area {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.player-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.player-avatar {
  flex-shrink: 0;
}

.player-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.player-name {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.player-university {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.team-info {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.team-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.role-badge {
  font-size: 10px;
  opacity: 0.8;
}

.no-team {
  color: #999;
  font-size: 12px;
}

.game-projects {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
}

.project-tag {
  margin: 2px;
}

.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-area {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-area .el-input,
  .filter-area .el-select {
    width: 100% !important;
  }
}
</style>
