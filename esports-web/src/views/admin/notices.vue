<template>
  <div class="admin-notices">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>公告管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">发布公告</el-button>
        </div>
      </template>

      <el-table :data="notices" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="views" label="浏览量" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除吗？" @confirm="handleDelete(scope.row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const notices = ref([]);
const dialogVisible = ref(false);
const form = ref({ id: null, title: '', content: '' });

const fetchNotices = async () => {
  const res = await request.get('/notice/list');
  notices.value = res.data;
};

const handleAdd = () => {
  form.value = { id: null, title: '', content: '' };
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  form.value = { ...row };
  dialogVisible.value = true;
};

const handleDelete = async (id) => {
  await request.post(`/notice/admin/delete/${id}`);
  ElMessage.success('删除成功');
  fetchNotices();
};

const handleSubmit = async () => {
  await request.post('/notice/admin/save', form.value);
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  fetchNotices();
};

onMounted(fetchNotices);
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
</style>
