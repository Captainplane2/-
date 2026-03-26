<template>
  <div class="match-page full-container">
    <div class="page-header">
      <h1 class="page-title">赛事信息</h1>
      <el-button 
        v-if="userStore.userInfo.role === 'ROLE_LEADER'" 
        type="primary" 
        @click="showPublishDialog = true"
      >发起约战</el-button>
    </div>

    <div class="content-box">
      <el-tabs v-model="queryForm.status" @tab-change="fetchMatches">
        <el-tab-pane label="全部约战" :name="null" />
        <el-tab-pane label="待匹配" :name="0" />
        <el-tab-pane label="已匹配" :name="1" />
        <el-tab-pane label="已完赛" :name="4" />
      </el-tabs>

      <div v-loading="loading" class="match-table-box">
        <el-table :data="matches" style="width: 100%" stripe>
          <el-table-column prop="matchTime" label="比赛时间" width="180" />
          <el-table-column prop="gameProject" label="项目" width="150">
            <template #default="scope">
              <el-tag effect="plain">{{ scope.row.gameProject }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="对阵双方">
            <template #default="scope">
              <div class="vs-cell">
                <span class="team-name">{{ scope.row.hostName || '待定' }}</span>
                <span class="vs-text">VS</span>
                <span class="team-name">{{ scope.row.guestName || '待匹配' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="mode" label="模式" width="100" />
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="right">
            <template #default="scope">
              <el-button 
                v-if="scope.row.status === 0" 
                type="primary" 
                size="small" 
                @click="handleApply(scope.row.id)"
              >申请对接</el-button>
              <el-button v-else link type="info">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="matches.length === 0" description="暂无相关赛事信息" />
      </div>
    </div>

    <!-- 发布对话框保持白色简约 -->
    <el-dialog v-model="showPublishDialog" title="发起约战挑战" width="500px">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="代表战队">
          <el-select v-model="publishForm.hostTeamId" placeholder="选择我的战队" class="w-full">
            <el-option v-for="t in myTeams" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="竞技项目">
          <el-select v-model="publishForm.gameProject" class="w-full">
            <el-option label="英雄联盟" value="LOL" />
            <el-option label="王者荣耀" value="王者荣耀" />
            <el-option label="CS2" value="CS2" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计时间">
          <el-date-picker
            v-model="publishForm.matchTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择开始时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="要求备注">
          <el-input v-model="publishForm.requirement" type="textarea" placeholder="段位要求等..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublish">立即发布</el-button>
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
const matches = ref([]);
const myTeams = ref([]);
const loading = ref(false);
const showPublishDialog = ref(false);

const queryForm = ref({
  status: null,
  gameProject: ''
});

const publishForm = ref({
  hostTeamId: null,
  gameProject: '',
  matchTime: '',
  mode: '线上',
  requirement: '',
  type: 0
});

const fetchMatches = async () => {
  loading.value = true;
  try {
    const res = await request.get('/match/list', { params: queryForm.value });
    matches.value = res.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const fetchMyTeams = async () => {
  if (!userStore.token) return;
  try {
    const res = await request.get(`/team/my/${userStore.userInfo.id}`);
    myTeams.value = res.data;
  } catch (err) {
    console.error(err);
  }
};

const handlePublish = async () => {
  try {
    await request.post('/match/publish', publishForm.value);
    ElMessage.success('发布成功');
    showPublishDialog.value = false;
    fetchMatches();
  } catch (err) {
    console.error(err);
  }
};

const handleApply = async (matchId) => {
  if (myTeams.value.length === 0) {
    ElMessage.warning('请先创建或加入战队');
    return;
  }
  try {
    await request.post(`/match/apply/${matchId}`, null, { params: { teamId: myTeams.value[0].id } });
    ElMessage.success('申请成功');
  } catch (err) {
    console.error(err);
  }
};

const getStatusText = (status) => {
  const map = { 0: '招募中', 1: '已匹配', 2: '待开赛', 3: '进行中', 4: '已完赛' };
  return map[status] || '准备中';
};

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'warning', 2: 'primary', 3: 'danger', 4: 'info' };
  return map[status] || '';
};

onMounted(() => {
  fetchMatches();
  fetchMyTeams();
});
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

.vs-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.vs-text {
  font-weight: bold;
  color: #ff4d4f;
  font-style: italic;
}

.team-name {
  font-weight: 500;
  width: 120px;
}

.w-full { width: 100%; }
</style>
