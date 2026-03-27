<template>
  <div class="message-page full-container">
    <div class="page-header">
      <h1 class="page-title">系统留言</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>意见反馈</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="message-content">
      <el-row :gutter="40">
        <!-- 左侧：提交留言表单 -->
        <el-col :span="10">
          <el-card shadow="never" class="form-card">
            <template #header>
              <div class="card-header">
                <span>发表留言</span>
              </div>
            </template>
            <el-form :model="form" label-position="top">
              <el-form-item label="留言内容" required>
                <el-input 
                  v-model="form.content" 
                  type="textarea" 
                  :rows="6" 
                  placeholder="写下您的建议、反馈或遇到的问题..."
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" class="submit-btn" @click="submitMessage">
                  提交留言
                </el-button>
              </el-form-item>
            </el-form>
            <div class="form-tip">
              <el-icon><InfoFilled /></el-icon>
              您的反馈对我们非常重要，管理员将在24小时内回复。
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：留言互动墙 (展示公开的优质建议) -->
        <el-col :span="14">
          <div class="message-wall">
            <div class="wall-header">最新互动</div>
            <div v-loading="loading" class="wall-list">
              <el-card v-for="msg in publicMessages" :key="msg.id" shadow="hover" class="wall-item">
                <div class="msg-uinfo">
                  <span class="u-name">选手 {{ msg.userId }}</span>
                  <span class="u-time">{{ formatDate(msg.createTime) }}</span>
                </div>
                <div class="msg-body">{{ msg.content }}</div>
                <div v-if="msg.reply" class="msg-reply">
                  <span class="reply-tag">官方回复</span>
                  <p>{{ msg.reply }}</p>
                </div>
              </el-card>
              <el-empty v-if="publicMessages.length === 0" description="暂无留言互动" />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '../../store/user';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const loading = ref(false);
const form = ref({ content: '' });
const publicMessages = ref([]);

const fetchPublicMessages = async () => {
  loading.value = true;
  try {
    // 假设后端有获取公开留言的接口，若无则获取我的留言作为演示
    const res = await request.get(`/message/my/${userStore.userInfo.id || 0}`);
    publicMessages.value = res.data || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const submitMessage = async () => {
  if (!userStore.token) return ElMessage.warning('请登录后再发表留言');
  if (!form.value.content.trim()) return ElMessage.warning('留言内容不能为空');

  try {
    await request.post('/message/submit', {
      userId: userStore.userInfo.id,
      content: form.value.content
    });
    ElMessage.success('留言已提交，感谢您的反馈');
    form.value.content = '';
    fetchPublicMessages();
  } catch (err) {
    console.error(err);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getMonth()+1}月${date.getDate()}日`;
};

onMounted(() => {
  fetchPublicMessages();
});
</script>

<style scoped>
.message-page { padding: 20px 0; }
.page-header { margin-bottom: 30px; }
.page-title { font-size: 28px; font-weight: bold; color: #333; }

.message-content {
  min-height: 600px;
}

.form-card { border-radius: 12px; }
.card-header { font-weight: bold; color: #333; }
.submit-btn { width: 100%; border-radius: 8px; font-weight: bold; }
.form-tip { margin-top: 20px; font-size: 13px; color: #999; display: flex; align-items: center; gap: 6px; }

.message-wall {
  background: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.wall-header { font-size: 18px; font-weight: bold; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 2px solid var(--primary); }

.wall-list { display: flex; flex-direction: column; gap: 15px; }
.wall-item { border-radius: 8px; }
.msg-uinfo { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 12px; color: #999; }
.u-name { font-weight: bold; color: #666; }
.msg-body { font-size: 14px; line-height: 1.6; color: #333; }

.msg-reply {
  margin-top: 15px;
  padding: 12px;
  background: #f0f9eb;
  border-left: 4px solid #67c23a;
  border-radius: 4px;
}
.reply-tag { font-size: 11px; color: #67c23a; font-weight: bold; text-transform: uppercase; margin-bottom: 5px; display: block; }
.msg-reply p { margin: 0; font-size: 13px; color: #606266; }
</style>
