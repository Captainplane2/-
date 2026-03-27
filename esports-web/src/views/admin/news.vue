<template>
  <div class="admin-news">
    <el-card shadow="never">
      <template #header>
        <div class="header-box">
          <span class="title">电竞新闻管理</span>
          <el-button type="primary" @click="handleCreate">发布新闻</el-button>
        </div>
      </template>

      <!-- 新闻列表 -->
      <el-table :data="newsList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="新闻标题" show-overflow-tooltip />
        <el-table-column prop="gameProject" label="所属项目" width="120">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.gameProject || '综合' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="160">
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

    <!-- 新闻编辑/发布弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑新闻' : '发布新闻'" 
      width="900px" 
      top="5vh"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="新闻标题" required>
          <el-input v-model="form.title" placeholder="请输入新闻标题" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="游戏项目">
              <el-select v-model="form.gameProject" placeholder="选择关联项目" class="w-full">
                <el-option label="综合电竞" value="综合" />
                <el-option label="英雄联盟" value="LOL" />
                <el-option label="王者荣耀" value="王者荣耀" />
                <el-option label="CS2" value="CS2" />
                <el-option label="无畏契约" value="无畏契约" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面图URL">
              <el-input v-model="form.coverImage" placeholder="图片链接(可选)" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="新闻摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="简短的新闻描述..." />
        </el-form-item>
        <el-form-item label="新闻正文" required>
          <div class="editor-container">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              :mode="mode"
            />
            <Editor
              style="height: 400px; overflow-y: hidden;"
              v-model="form.content"
              :defaultConfig="editorConfig"
              :mode="mode"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">保存发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css'
import { ref, onMounted, shallowRef, onBeforeUnmount } from 'vue';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../../utils/request';

const newsList = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);

const form = ref({
  id: null,
  title: '',
  summary: '',
  coverImage: '',
  gameProject: '综合',
  content: ''
});

const editorRef = shallowRef();
const mode = 'default';
const toolbarConfig = {};
const editorConfig = { placeholder: '请输入新闻正文...' };

const handleCreated = (editor) => {
  editorRef.value = editor;
};

onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor == null) return;
  editor.destroy();
});

const fetchNews = async () => {
  loading.value = true;
  try {
    const res = await request.get('/news/list');
    newsList.value = res.data || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const handleCreate = () => {
  isEdit.value = false;
  form.value = {
    id: null,
    title: '',
    summary: '',
    coverImage: '',
    gameProject: '综合',
    content: ''
  };
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  isEdit.value = true;
  form.value = { ...row };
  dialogVisible.value = true;
};

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条新闻吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/news/delete/${id}`);
      ElMessage.success('删除成功');
      fetchNews();
    } catch (err) {
      console.error(err);
    }
  }).catch(() => {});
};

const submitForm = async () => {
  if (!form.value.title) {
    return ElMessage.warning('请输入新闻标题');
  }
  if (!form.value.content || form.value.content === '<p><br></p>') {
    return ElMessage.warning('请输入新闻正文内容');
  }
  
  try {
    await request.post('/news/save', form.value);
    ElMessage.success(isEdit.value ? '更新成功' : '发布成功');
    dialogVisible.value = false;
    fetchNews();
  } catch (err) {
    console.error(err);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`;
};

onMounted(fetchNews);
</script>

<style scoped>
.header-box { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: bold; }
.w-full { width: 100%; }
.editor-container { border: 1px solid #ccc; width: 100%; }
</style>
