<template>
  <div class="admin-game-projects">
    <el-card shadow="never">
      <template #header>游戏板块管理</template>
      
      <div class="action-bar">
        <el-button type="primary" icon="Plus" @click="showCreateDialog = true">新增游戏板块</el-button>
      </div>

      <el-table :data="gameProjects" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="路径名称" width="120" />
        <el-table-column prop="displayName" label="游戏名称" width="150" />
        <el-table-column label="图标" width="100">
          <template #default="scope">
            <el-image 
              :src="scope.row.icon || 'https://via.placeholder.com/50x50/eee/999?text=ICON'" 
              style="width: 40px; height: 40px; border-radius: 4px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="状态" width="180">
          <template #default="scope">
            <el-switch 
              v-model="scope.row.status" 
              :active-value="0" 
              :inactive-value="1"
              active-text="启用"
              inactive-text="禁用"
              @change="updateStatus(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editProject(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteProject(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 创建/编辑对话框 -->
      <el-dialog v-model="showCreateDialog" :title="editingProject.id ? '编辑游戏板块' : '新增游戏板块'" width="500px">
        <el-form :model="editingProject" label-width="120px">
          <el-form-item label="路径名称" required>
            <el-input v-model="editingProject.name" placeholder="必须大写，如：CS2, LOL, WZRY" />
          </el-form-item>
          <el-form-item label="游戏名称" required>
            <el-input v-model="editingProject.displayName" placeholder="如：CS2, 英雄联盟, 王者荣耀" />
          </el-form-item>
          <el-form-item label="游戏图标">
            <el-input v-model="editingProject.icon" placeholder="图标 URL" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="editingProject.sortOrder" :min="0" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch 
              v-model="editingProject.status" 
              :active-value="0" 
              :inactive-value="1"
              active-text="启用"
              inactive-text="禁用"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveProject">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';

const gameProjects = ref([]);
const showCreateDialog = ref(false);
const editingProject = ref({
  id: null,
  name: '',
  displayName: '',
  icon: '',
  sortOrder: 0,
  status: 0
});

const fetchGameProjects = async () => {
  try {
    const res = await request.get('/game-project/list');
    gameProjects.value = res.data || [];
  } catch (err) {
    console.error('获取游戏板块列表失败', err);
  }
};

const editProject = (project) => {
  editingProject.value = { ...project };
  showCreateDialog.value = true;
};

const saveProject = async () => {
  if (!editingProject.value.name || !editingProject.value.displayName) {
    ElMessage.warning('请填写游戏名称和显示名称');
    return;
  }
  
  try {
    if (editingProject.value.id) {
      await request.post('/game-project/update', editingProject.value);
      ElMessage.success('游戏板块更新成功');
    } else {
      await request.post('/game-project/create', editingProject.value);
      ElMessage.success('游戏板块创建成功');
    }
    showCreateDialog.value = false;
    fetchGameProjects();
    resetForm();
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '操作失败');
  }
};

const deleteProject = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个游戏板块吗？', '提醒');
    await request.post(`/game-project/delete/${id}`);
    ElMessage.success('游戏板块删除成功');
    fetchGameProjects();
  } catch (e) {}
};

const updateStatus = async (project) => {
  try {
    await request.post('/game-project/update', project);
    ElMessage.success('状态更新成功');
  } catch (err) {
    ElMessage.error('状态更新失败');
    // 恢复原状态
    fetchGameProjects();
  }
};

const resetForm = () => {
  editingProject.value = {
    id: null,
    name: '',
    displayName: '',
    icon: '',
    sortOrder: 0,
    status: 0
  };
};

onMounted(fetchGameProjects);
</script>

<style scoped>
.admin-game-projects { padding: 10px; }
.action-bar { margin-bottom: 20px; }
</style>