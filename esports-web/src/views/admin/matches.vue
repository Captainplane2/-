<template>
  <div class="admin-matches">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>约赛记录管理</span>
          <el-button type="primary" @click="showCreateDialog">添加约赛</el-button>
        </div>
      </template>

      <el-table :data="matches" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="gameProject" label="项目" width="120" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="matchTime" label="比赛时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.matchTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="模式" width="100">
          <template #default="scope">
            {{ scope.row.type === 0 ? '线上' : '线下' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editMatch(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除该约赛记录吗？"
              @confirm="deleteMatch(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑约赛对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="约战项目" required>
          <el-select v-model="form.gameProject" placeholder="请选择游戏" class="w-full">
            <el-option label="英雄联盟" value="LOL" />
            <el-option label="王者荣耀" value="王者荣耀" />
            <el-option label="CS2" value="CS2" />
            <el-option label="无畏契约" value="无畏契约" />
          </el-select>
        </el-form-item>
        <el-form-item label="约战标题" required>
          <el-input v-model="form.title" placeholder="例如：XX大学寻求白金水平约战" />
        </el-form-item>
        <el-form-item label="约战类型">
          <el-radio-group v-model="form.type">
            <el-radio :label="0">线上对决</el-radio>
            <el-radio :label="1">线下约战</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开赛时间" required>
          <el-date-picker
            v-model="form.matchTime"
            type="datetime"
            placeholder="选择开赛时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item v-if="form.type === 1" label="约战地点" required>
          <el-input v-model="form.location" placeholder="具体的网咖或电竞馆名称" />
        </el-form-item>
        <el-form-item label="备注信息">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可输入备注或直播间链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();

const matches = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('添加约赛');
const form = ref({
  id: null,
  gameProject: '',
  title: '',
  type: 0,
  matchTime: '',
  location: '',
  description: ''
});

const fetchMatches = async () => {
  loading.value = true;
  try {
    const res = await request.get('/match-room/admin/list');
    matches.value = res.data;
  } catch (err) {
    console.error(err);
    ElMessage.error('获取约赛记录失败');
  } finally {
    loading.value = false;
  }
};

const showCreateDialog = () => {
  form.value = {
    id: null,
    gameProject: '',
    title: '',
    type: 0,
    matchTime: '',
    location: '',
    description: ''
  };
  dialogTitle.value = '添加约赛';
  dialogVisible.value = true;
};

const editMatch = (match) => {
  form.value = {
    id: match.id,
    gameProject: match.gameProject,
    title: match.title,
    type: match.type,
    matchTime: parseDateTimeForPicker(match.matchTime),
    location: match.location,
    description: match.description
  };
  dialogTitle.value = '编辑约赛';
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!form.value.gameProject || !form.value.title || !form.value.matchTime) {
    return ElMessage.warning('请填写完整信息');
  }
  
  try {
    // 处理时间格式，确保时区正确
    const submitForm = { ...form.value };
    if (submitForm.matchTime) {
      submitForm.matchTime = formatDateTimeLocal(submitForm.matchTime);
    }
    
    if (form.value.id) {
      // 编辑约赛
      await request.put('/match-room/update', submitForm);
      ElMessage.success('约赛信息已更新');
    } else {
      // 添加约赛
      await request.post('/match-room/create', submitForm);
      ElMessage.success('约赛已添加');
    }
    dialogVisible.value = false;
    fetchMatches();
  } catch (err) {
    console.error(err);
    ElMessage.error('操作失败');
  }
};

const deleteMatch = async (id) => {
  try {
    // 使用管理端删除接口，真正从数据库删除记录
    await request.post(`/match-room/admin/delete/${id}`);
    ElMessage.success('约赛已删除');
    // 从本地列表中移除被删除的约战记录
    matches.value = matches.value.filter(match => match.id !== id);
  } catch (err) {
    console.error(err);
    ElMessage.error('删除失败');
  }
};

const getStatusText = (status) => {
  const map = { 0: '招募中', 1: '已应战', 2: '已结束', 3: '已取消' };
  return map[status] || '未知';
};

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' };
  return map[status] || 'info';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  // 添加上午/下午标识
  const period = hours < 12 ? '上午' : '下午';
  // 使用24小时制显示时间
  return `${month}月${day}日 ${period}${String(hours).padStart(2,'0')}:${String(minutes).padStart(2,'0')}`;
};

// 格式化时间为本地时间字符串（用于提交到后端）
const formatDateTimeLocal = (date) => {
  if (!date) return '';
  // el-date-picker 返回的是 UTC 时间的 Date 对象，需要转换为北京时间
  const d = new Date(date);
  // 将 UTC 时间转换为北京时间（东八区）
  const beijingOffset = 8 * 60 * 60 * 1000; // 8小时偏移量
  const beijingTime = new Date(d.getTime() + beijingOffset);
  
  const year = beijingTime.getUTCFullYear();
  const month = String(beijingTime.getUTCMonth() + 1).padStart(2, '0');
  const day = String(beijingTime.getUTCDate()).padStart(2, '0');
  const hours = String(beijingTime.getUTCHours()).padStart(2, '0');
  const minutes = String(beijingTime.getUTCMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}:00`;
};

// 解析后端返回的时间字符串为 el-date-picker 可以理解的格式
const parseDateTimeForPicker = (dateStr) => {
  if (!dateStr) return '';
  // 后端存储的是北京时间，需要转换为本地时间对象
  const date = new Date(dateStr);
  // 将北京时间转换为本地时间
  const localOffset = date.getTimezoneOffset() * 60 * 1000; // 本地时区偏移量
  const localTime = new Date(date.getTime() - localOffset);
  return localTime;
};

onMounted(fetchMatches);
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
