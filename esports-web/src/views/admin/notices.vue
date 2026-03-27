<template>
  <div class="admin-notices">
    <el-card shadow="never">
      <template #header>
        <div class="header-box">
          <span class="title">系统公告管理</span>
          <el-button type="primary" @click="handleCreate">发布公告</el-button>
        </div>
      </template>

      <!-- 公告列表 -->
      <el-table :data="notices" border stripe v-loading="loading">
        <el-table-column prop="id" label="公告ID" width="80" />
        <el-table-column prop="title" label="公告标题" />
        <el-table-column prop="content" label="公告内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发布/编辑公告弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布新公告'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="公告标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入公告正文" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../../utils/request';

const notices = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);

const form = ref({
  id: null,
  title: '',
  content: ''
});

const fetchNotices = async () => {
  loading.value = true;
  try {
    const res = await request.get('/notice/list');
    notices.value = res.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleCreate = () => {
  isEdit.value = false;
  form.value = { id: null, title: '', content: '' };
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  isEdit.value = true;
  form.value = { ...row };
  dialogVisible.value = true;
};

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/notice/delete/${id}`);
      ElMessage.success('公告已删除');
      fetchNotices();
    } catch (err) {
      console.error(err);
    }
  }).catch(() => {});
};

const submitForm = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写完整公告信息');
    return;
  }
  
  try {
    await request.post('/notice/save', form.value);
    ElMessage.success(isEdit.value ? '公告更新成功' : '公告发布成功');
    dialogVisible.value = false;
    fetchNotices();
  } catch (err) {
    console.error(err);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`;
};

onMounted(fetchNotices);
</script>

<style scoped>
.header-box { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: bold; }
</style>
