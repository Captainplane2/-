<template>
  <div class="admin-teams">
    <div class="page-header">
      <h1 class="page-title">战队管理</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/admin' }">管理后台</el-breadcrumb-item>
        <el-breadcrumb-item>战队管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card shadow="never" class="search-card">
      <div class="search-form">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索战队名称"
          style="width: 200px; margin-right: 10px;"
          @keyup.enter="fetchTeams"
        >
          <template #append>
            <el-button @click="fetchTeams">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-select
          v-model="searchForm.gameProject"
          placeholder="游戏项目"
          style="width: 150px; margin-right: 10px;"
          @change="fetchTeams"
        >
          <el-option label="全部项目" value=""></el-option>
          <el-option label="英雄联盟" value="LOL"></el-option>
          <el-option label="王者荣耀" value="王者荣耀"></el-option>
          <el-option label="CS2" value="CS2"></el-option>
        </el-select>
        <el-select
          v-model="searchForm.status"
          placeholder="战队状态"
          style="width: 120px;"
          @change="fetchTeams"
        >
          <el-option label="全部状态" value=""></el-option>
          <el-option label="正常" :value="0"></el-option>
          <el-option label="已解散" :value="1"></el-option>
        </el-select>
      </div>
    </el-card>

    <el-card shadow="never" class="content-card">
      <el-table :data="teams" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="战队信息" min-width="200">
          <template #default="scope">
            <div class="team-info">
              <el-avatar :size="40" :src="scope.row.logo || 'https://via.placeholder.com/40/eee/999?text=TEAM'" />
              <div class="team-details">
                <div class="team-name">{{ scope.row.name }}</div>
                <div class="team-game">{{ scope.row.gameProject }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="战队简介" min-width="200" show-overflow-tooltip />
        <el-table-column prop="memberCount" label="成员数" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? '正常' : '已解散' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="editTeam(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="viewMembers(scope.row)"
            >
              成员
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              size="small"
              @click="confirmDelete(scope.row.id)"
            >
              解散
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

    <!-- 编辑战队对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editForm.id ? '编辑战队' : '创建战队'"
      width="600px"
      @close="resetEditForm"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="100px"
      >
        <el-form-item label="战队名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入战队名称" />
        </el-form-item>
        
        <el-form-item label="游戏项目" prop="gameProject">
          <el-select v-model="editForm.gameProject" placeholder="请选择游戏项目" style="width: 100%;">
            <el-option label="英雄联盟" value="LOL"></el-option>
            <el-option label="王者荣耀" value="王者荣耀"></el-option>
            <el-option label="CS2" value="CS2"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="战队Logo" prop="logo">
          <el-upload
            class="avatar-uploader"
            :action="`${env.apiBaseURL}/user/upload`"
            :show-file-list="false"
            :before-upload="beforeLogoUpload"
            :on-success="handleLogoSuccess"
            :headers="uploadHeaders"
          >
            <img v-if="editForm.logo" :src="editForm.logo" class="avatar">
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议上传 200x200px 的方形图片</div>
        </el-form-item>
        
        <el-form-item label="战队简介" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入战队简介"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditForm" :loading="editLoading">
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 成员管理对话框 -->
    <el-dialog
      v-model="membersDialogVisible"
      title="战队成员管理"
      width="800px"
    >
      <div class="members-header">
        <h3>{{ currentTeam?.name }} - 成员列表</h3>
        <el-button type="primary" size="small" @click="showAddMemberDialog">
          添加成员
        </el-button>
      </div>
      
      <el-table :data="teamMembers" border stripe>
        <el-table-column label="成员信息" min-width="200">
          <template #default="scope">
            <div class="member-info">
              <el-avatar :size="40" :src="scope.row.avatar || 'https://via.placeholder.com/40/eee/999?text=USER'" />
              <div class="member-details">
                <div class="member-name">{{ scope.row.nickname }}</div>
                <div class="member-username">{{ scope.row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="university" label="学校" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.role === '队长' ? 'danger' : 'info'">
              {{ scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="加入时间" width="180" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button
              v-if="scope.row.role !== '队长'"
              type="danger"
              size="small"
              @click="confirmRemoveMember(scope.row)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="addMemberDialogVisible"
      title="添加战队成员"
      width="500px"
    >
      <el-form :model="addMemberForm" label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model="addMemberForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="addMemberForm.role" placeholder="请选择角色">
            <el-option label="队员" value="队员"></el-option>
            <el-option label="副队长" value="副队长"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddMember">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Plus } from '@element-plus/icons-vue';
import request from '../../utils/request';
import env from '../../config/env';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();

// 搜索表单
const searchForm = reactive({
  name: '',
  gameProject: '',
  status: ''
});

// 数据
const teams = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 编辑表单
const editDialogVisible = ref(false);
const editFormRef = ref();
const editForm = reactive({
  id: null,
  name: '',
  logo: '',
  gameProject: '',
  description: ''
});

const editFormRules = {
  name: [
    { required: true, message: '请输入战队名称', trigger: 'blur' },
    { min: 2, max: 20, message: '战队名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  gameProject: [
    { required: true, message: '请选择游戏项目', trigger: 'change' }
  ]
};

const editLoading = ref(false);

// 成员管理
const membersDialogVisible = ref(false);
const currentTeam = ref(null);
const teamMembers = ref([]);

// 添加成员
const addMemberDialogVisible = ref(false);
const addMemberForm = reactive({
  userId: '',
  role: '队员'
});

// 上传配置
const uploadHeaders = { Authorization: `Bearer ${userStore.token}` };

// 获取战队列表
const fetchTeams = async () => {
  loading.value = true;
  try {
    const response = await request.get('/team/admin/list');
    let data = response.data || [];
    
    // 过滤搜索条件
    if (searchForm.name) {
      data = data.filter(team => team.name.includes(searchForm.name));
    }
    if (searchForm.gameProject) {
      data = data.filter(team => team.gameProject === searchForm.gameProject);
    }
    if (searchForm.status !== '') {
      data = data.filter(team => team.status === searchForm.status);
    }
    
    teams.value = data;
    total.value = data.length;
  } catch (error) {
    console.error('获取战队列表失败:', error);
    ElMessage.error('获取战队列表失败');
  } finally {
    loading.value = false;
  }
};

// 编辑战队
const editTeam = (team) => {
  Object.assign(editForm, {
    id: team.id,
    name: team.name,
    logo: team.logo,
    gameProject: team.gameProject,
    description: team.description || ''
  });
  editDialogVisible.value = true;
};

// 提交编辑表单
const submitEditForm = async () => {
  if (!editFormRef.value) return;
  
  try {
    await editFormRef.value.validate();
    editLoading.value = true;
    
    if (editForm.id) {
      // 更新战队
      await request.post('/team/update', editForm);
      ElMessage.success('战队信息更新成功');
    } else {
      // 创建战队
      await request.post('/team/create', editForm);
      ElMessage.success('战队创建成功');
    }
    
    editDialogVisible.value = false;
    fetchTeams();
  } catch (error) {
    console.error('保存战队信息失败:', error);
    ElMessage.error('保存失败');
  } finally {
    editLoading.value = false;
  }
};

// 重置编辑表单
const resetEditForm = () => {
  Object.assign(editForm, {
    id: null,
    name: '',
    logo: '',
    gameProject: '',
    description: ''
  });
  if (editFormRef.value) {
    editFormRef.value.clearValidate();
  }
};

// Logo上传处理
const beforeLogoUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('Logo只能是 JPG/PNG 格式!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('Logo大小不能超过 2MB!');
    return false;
  }
  return true;
};

const handleLogoSuccess = (response) => {
  let logoUrl = response.data;
  if (logoUrl && !logoUrl.startsWith('http')) {
    logoUrl = env.getFullApiUrl(logoUrl);
  }
  editForm.logo = logoUrl;
  ElMessage.success('Logo上传成功');
};

// 查看成员
const viewMembers = async (team) => {
  currentTeam.value = team;
  try {
    const response = await request.get(`/team/members/${team.id}`);
    teamMembers.value = response.data || [];
    membersDialogVisible.value = true;
  } catch (error) {
    console.error('获取战队成员失败:', error);
    ElMessage.error('获取成员列表失败');
  }
};

// 添加成员
const showAddMemberDialog = () => {
  addMemberForm.userId = '';
  addMemberForm.role = '队员';
  addMemberDialogVisible.value = true;
};

const submitAddMember = async () => {
  if (!addMemberForm.userId) {
    ElMessage.error('请输入用户ID');
    return;
  }
  
  try {
    await request.post(`/team/join/${currentTeam.value.id}`, null, {
      params: { userId: addMemberForm.userId }
    });
    ElMessage.success('成员添加成功');
    addMemberDialogVisible.value = false;
    viewMembers(currentTeam.value); // 刷新成员列表
  } catch (error) {
    console.error('添加成员失败:', error);
    ElMessage.error('添加成员失败');
  }
};

// 移除成员
const confirmRemoveMember = (member) => {
  ElMessageBox.confirm(
    `确定要移除成员 "${member.nickname}" 吗？`,
    '警告',
    {
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.post('/team/member/remove', null, {
        params: {
          teamId: currentTeam.value.id,
          userId: member.id
        }
      });
      ElMessage.success('成员移除成功');
      viewMembers(currentTeam.value); // 刷新成员列表
    } catch (error) {
      console.error('移除成员失败:', error);
      ElMessage.error('移除成员失败');
    }
  }).catch(() => {
    // 取消操作
  });
};

// 解散战队
const confirmDelete = (teamId) => {
  ElMessageBox.confirm(
    '确定要解散这个战队吗？此操作不可撤销。',
    '警告',
    {
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.post(`/team/admin/delete/${teamId}`);
      ElMessage.success('战队解散成功');
      fetchTeams();
    } catch (error) {
      console.error('解散战队失败:', error);
      ElMessage.error('解散失败');
    }
  }).catch(() => {
    // 取消操作
  });
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  fetchTeams();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  fetchTeams();
};

// 初始化
onMounted(() => {
  fetchTeams();
});
</script>

<style scoped>
.admin-teams {
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

.team-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.team-details {
  display: flex;
  flex-direction: column;
}

.team-name {
  font-weight: 600;
  color: #333;
}

.team-game {
  font-size: 12px;
  color: #666;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-details {
  display: flex;
  flex-direction: column;
}

.member-name {
  font-weight: 600;
  color: #333;
}

.member-username {
  font-size: 12px;
  color: #666;
}

.members-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* Logo上传样式 */
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
}

.upload-tip {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}
</style>