<template>
  <div class="profile-page full-container">
    <div class="page-header">
      <h1 class="page-title">选手中心</h1>
    </div>

    <el-row :gutter="24">
      <!-- 左侧边栏 -->
      <el-col :span="6">
        <el-card class="user-info-card" shadow="never">
          <div class="user-avatar-box">
            <el-upload
              class="avatar-uploader"
              action="http://localhost:8080/api/user/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :headers="uploadHeaders"
            >
              <el-avatar :size="100" :src="userStore.userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="main-avatar" />
              <div class="avatar-mask">更换头像</div>
            </el-upload>
            <h2 class="nickname">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h2>
            
            <!-- 个性签名展示 -->
            <p class="signature">{{ userStore.userInfo.signature || '暂无个性签名' }}</p>

            <!-- 多项目战队标签系统 -->
            <div class="project-tags-container">
              <div 
                v-for="p in userProjects" 
                :key="p.projectName"
                class="tag-wrapper"
                @click="toggleTagVisibility(p, $event)"
              >
                <el-tag 
                  :type="p.hidden ? 'info' : getProjectTagType(p.projectName)" 
                  :class="{ 'is-hidden': p.hidden }"
                  effect="dark"
                  size="small"
                  class="project-status-tag"
                >
                  {{ p.projectName }}：{{ p.teamName }} {{ p.role === 1 ? '战队队长' : '战队成员' }}
                </el-tag>
              </div>
            </div>
          </div>
          <el-divider />
          <el-menu :default-active="activeTab" class="profile-menu" @select="handleSelect">
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>竞技档案</span>
            </el-menu-item>
            <el-menu-item index="team">
              <el-icon><Star /></el-icon>
              <span>我的战队</span>
            </el-menu-item>
            <el-menu-item index="match">
              <el-icon><Calendar /></el-icon>
              <span>竞技记录</span>
            </el-menu-item>
            <el-menu-item index="message">
              <el-icon><ChatDotRound /></el-icon>
              <span>系统回执</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧主内容 -->
      <el-col :span="18">
        <el-card class="content-card" shadow="never">
          <!-- 个人资料编辑 -->
          <div v-if="activeTab === 'info'">
            <div class="content-header">
              <h3 class="title">竞技档案编辑</h3>
            </div>
            <el-form :model="editForm" label-width="100px" style="max-width: 600px">
              <el-form-item label="选手账号">
                <el-input v-model="editForm.username" disabled />
              </el-form-item>
              <el-form-item label="竞技昵称">
                <el-input v-model="editForm.nickname" />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="editForm.realName" />
              </el-form-item>
              <el-form-item label="选手性别">
                <el-radio-group v-model="editForm.gender">
                  <el-radio label="男">男</el-radio>
                  <el-radio label="女">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="QQ号">
                <el-input v-model="editForm.qq" placeholder="方便战队联系" />
              </el-form-item>
              <el-form-item label="微信号">
                <el-input v-model="editForm.wechat" placeholder="方便战队联系" />
              </el-form-item>
              <el-form-item label="个性签名">
                <el-input 
                  v-model="editForm.signature" 
                  type="textarea" 
                  :rows="2" 
                  maxlength="20" 
                  show-word-limit
                  placeholder="展示你的竞技宣言"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" @click="handleUpdateProfile">更新选手档案</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 我的战队列表 -->
          <div v-else-if="activeTab === 'team'">
            <div class="content-header">
              <h3 class="title">战队归属管理</h3>
            </div>
            <el-table :data="myTeams" border stripe v-loading="loading">
              <el-table-column prop="name" label="战队名称" />
              <el-table-column prop="gameProject" label="项目" width="120" />
              <el-table-column prop="memberCount" label="人数" width="100" />
              <el-table-column label="角色" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.leaderId === userStore.userInfo.id ? 'danger' : 'info'">
                    {{ scope.row.leaderId === userStore.userInfo.id ? '队长' : '成员' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" align="right">
                <template #default="scope">
                  <!-- 抽离为独立路由页面后，直接跳转传参 -->
                  <el-button 
                    v-if="scope.row.leaderId === userStore.userInfo.id" 
                    type="primary" 
                    size="small" 
                    @click="$router.push(`/team/manage?teamId=${scope.row.id}`)"
                  >战队管理</el-button>
                  <el-button v-else link type="primary" @click="$router.push('/team')">进入战队大厅</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 系统回执 -->
          <div v-else-if="activeTab === 'message'">
            <div class="content-header">
              <h3 class="title">反馈与建议记录</h3>
              <el-button type="primary" size="small" @click="showMessageDialog = true">提交新建议</el-button>
            </div>
            <div class="message-feed">
              <el-card v-for="msg in messages" :key="msg.id" class="msg-card" shadow="never">
                <div class="m-body">{{ msg.content }}</div>
                <div class="m-time">{{ msg.createTime }}</div>
                <div v-if="msg.reply" class="m-reply">
                  <div class="r-head">官方回复：</div>
                  <div class="r-body">{{ msg.reply }}</div>
                </div>
              </el-card>
              <el-empty v-if="messages.length === 0" description="暂无历史建议记录" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 留言弹窗 -->
    <el-dialog v-model="showMessageDialog" title="提交意见建议" width="480px">
      <el-input v-model="newMessage" type="textarea" :rows="5" placeholder="请详细描述您的问题..." />
      <template #footer>
        <el-button @click="showMessageDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitMessage">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 隐藏标签提示文字小字 -->
    <div v-if="tipVisible" class="float-tip" :style="{ top: tipPos.y + 'px', left: tipPos.x + 'px' }">
      已隐藏
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../store/user';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const activeTab = ref('info');
const myTeams = ref([]);
const messages = ref([]);
const showMessageDialog = ref(false);
const newMessage = ref('');
const loading = ref(false);

// 标签交互逻辑
const tipVisible = ref(false);
const tipPos = ref({ x: 0, y: 0 });

const uploadHeaders = { Authorization: `Bearer ${userStore.token}` };
const editForm = ref({ ...userStore.userInfo });

// 选手参加的项目关联数据
const userProjects = ref([]);

watch(() => route.query.tab, (newTab) => {
  if (newTab) {
    activeTab.value = newTab;
    if (newTab === 'team') fetchMyTeams();
  }
}, { immediate: true });

const handleSelect = (index) => {
  activeTab.value = index;
  if (index === 'team') fetchMyTeams();
  if (index === 'message') fetchMessages();
};

const fetchMyTeams = async () => {
  if (!userStore.userInfo.id) return;
  loading.value = true;
  try {
    const res = await request.get(`/team/my/${userStore.userInfo.id}`);
    myTeams.value = res.data || [];
    // 动态更新头像下的标签数据
    userProjects.value = myTeams.value.map(t => ({
      projectName: t.gameProject,
      teamName: t.name,
      role: t.leaderId === userStore.userInfo.id ? 1 : 0,
      hidden: false
    }));
  } catch (err) { 
    console.error(err); 
  } finally {
    loading.value = false;
  }
};

const fetchMessages = async () => {
  if (!userStore.userInfo.id) return;
  try {
    const res = await request.get(`/message/my/${userStore.userInfo.id}`);
    messages.value = res.data || [];
  } catch (err) { console.error(err); }
};

const handleUpdateProfile = async () => {
  try {
    await request.post('/user/update', editForm.value);
    ElMessage.success('档案更新成功');
    userStore.setUserInfo({ ...userStore.userInfo, ...editForm.value });
  } catch (err) { console.error(err); }
};

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    editForm.value.avatar = res.data;
    handleUpdateProfile();
  }
};

const getProjectTagType = (project) => {
  const map = { 'LOL': '', '王者荣耀': 'success', 'CS2': 'warning', '无畏契约': 'danger' };
  return map[project] || 'info';
};

const toggleTagVisibility = (project, event) => {
  project.hidden = !project.hidden;
  if (project.hidden) {
    tipPos.value = { x: event.clientX + 10, y: event.clientY - 20 };
    tipVisible.value = true;
    setTimeout(() => { tipVisible.value = false; }, 3000);
  }
};

const handleSubmitMessage = async () => {
  if (!newMessage.value) return;
  try {
    await request.post('/message/submit', {
      userId: userStore.userInfo.id,
      content: newMessage.value
    });
    ElMessage.success('提交成功');
    newMessage.value = '';
    showMessageDialog.value = false;
    fetchMessages();
  } catch (err) { console.error(err); }
};

onMounted(() => {
  if (!userStore.token) {
    router.push('/login');
    return;
  }
  fetchMyTeams();
  if (route.query.tab) activeTab.value = route.query.tab;
  if (activeTab.value === 'message') fetchMessages();
});
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; color: #333; }

.user-info-card { text-align: center; border-radius: 12px; padding: 20px 0; }
.user-avatar-box { position: relative; margin-bottom: 20px; }

.avatar-uploader { display: inline-block; cursor: pointer; position: relative; }
.avatar-mask {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  border-radius: 50%; background: rgba(0,0,0,0.4);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 12px; opacity: 0; transition: opacity 0.3s;
}
.avatar-uploader:hover .avatar-mask { opacity: 1; }

.nickname { font-size: 20px; margin: 12px 0 8px; color: #333; }
.signature { font-size: 13px; color: #999; margin-bottom: 16px; padding: 0 20px; font-style: italic; }

.project-tags-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  margin-top: 10px;
}
.tag-wrapper { cursor: pointer; transition: all 0.3s; width: 90%; }
.project-status-tag { width: 100%; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.project-status-tag.is-hidden { opacity: 0.4; filter: grayscale(1); }

.float-tip {
  position: fixed;
  padding: 4px 8px;
  background: rgba(0,0,0,0.7);
  color: #fff;
  border-radius: 4px;
  font-size: 12px;
  z-index: 9999;
  pointer-events: none;
}

.profile-menu { border-right: none; text-align: left; }

.content-card { min-height: 600px; border-radius: 12px; }
.content-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 30px; 
  padding-bottom: 15px; 
  border-bottom: 1px solid #f0f2f5; 
}
.title { font-size: 18px; color: #333; margin: 0; }

.msg-feed { display: flex; flex-direction: column; gap: 16px; }
.msg-card { padding: 20px; background: #fcfcfc; }
.m-body { font-size: 15px; margin-bottom: 10px; }
.m-time { font-size: 12px; color: #999; }
.m-reply { margin-top: 15px; padding: 12px; background: #f0f9eb; border-left: 4px solid #67c23a; border-radius: 4px; }
.r-head { font-weight: bold; font-size: 13px; color: #67c23a; margin-bottom: 5px; }
.r-body { font-size: 14px; color: #606266; }

.w-full { width: 100%; }
.ml-12 { margin-left: 12px; }
</style>
