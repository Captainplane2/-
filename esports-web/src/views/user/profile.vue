<template>
  <div class="profile-page full-container">
    <el-row :gutter="20">
      <!-- 左侧导航侧边栏 (对齐示例网站风格) -->
      <el-col :span="5">
        <el-card class="sidebar-card" shadow="never">
          <div class="user-profile-summary">
            <el-upload
              class="avatar-uploader"
              :action="`${env.apiBaseURL}/user/upload`"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :headers="uploadHeaders"
            >
              <el-avatar :size="80" :src="userStore.userInfo.avatar || defaultAvatar" />
              <div class="avatar-edit-mask">编辑</div>
            </el-upload>
            <div class="user-meta">
              <h3 class="username">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h3>
              <div class="tags-container">
                <p class="university-tag"><el-icon><School /></el-icon> {{ userStore.userInfo.university || '未绑定高校' }}</p>
                <el-tag 
                  v-for="(identity, gameProject) in userIdentities" 
                  :key="gameProject"
                  :type="identity === '战队队长' ? 'danger' : 'primary'"
                  effect="dark"
                >
                  {{ identity }}：{{ gameProject }}
                </el-tag>
              </div>
            </div>
          </div>

          <el-divider />

          <el-menu
            :default-active="activeTab"
            class="profile-menu"
            @select="handleSelect"
          >
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>个人资料</span>
            </el-menu-item>
            <el-menu-item index="team">
              <el-icon><Trophy /></el-icon>
              <span>我的战队</span>
            </el-menu-item>
            <el-menu-item index="post">
              <el-icon><ChatLineRound /></el-icon>
              <span>我的帖子</span>
            </el-menu-item>
            <el-menu-item index="collection">
              <el-icon><Star /></el-icon>
              <span>战队收藏</span>
            </el-menu-item>
            <el-menu-item index="message">
              <el-icon><Message /></el-icon>
              <span>系统留言</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧内容区 -->
      <el-col :span="19">
        <el-card class="content-card" shadow="never">
          <!-- 1. 个人资料 -->
          <div v-if="activeTab === 'info'" class="tab-content">
            <div class="section-title">个人信息编辑</div>
            <el-form :model="editForm" label-width="100px" class="profile-form">
              <el-form-item label="选手账号">
                <el-input v-model="editForm.username" disabled />
              </el-form-item>
              <el-form-item label="竞技昵称">
                <el-input v-model="editForm.nickname" placeholder="在平台展示的昵称" />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="editForm.realName" />
              </el-form-item>
              <el-form-item label="所属高校">
                <el-input v-model="editForm.university" disabled />
              </el-form-item>
              <el-form-item label="联系QQ">
                <el-input v-model="editForm.qq" />
              </el-form-item>
              <el-form-item label="选手性别">
                <el-radio-group v-model="editForm.gender">
                  <el-radio label="男">男</el-radio>
                  <el-radio label="女">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="竞技宣言">
                <el-input 
                  v-model="editForm.signature" 
                  type="textarea" 
                  :rows="3" 
                  maxlength="50" 
                  show-word-limit 
                  placeholder="一句话介绍自己..."
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 2. 我的战队 -->
          <div v-else-if="activeTab === 'team'" class="tab-content">
            <div class="section-title">我的战队归属</div>
            <el-table :data="myTeams" border stripe v-loading="loading" class="team-table">
              <el-table-column prop="name" label="战队名称" />
              <el-table-column prop="gameProject" label="项目" width="120" />
              <el-table-column label="我的身份" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.leaderId === userStore.userInfo.id ? 'danger' : 'info'">
                    {{ scope.row.leaderId === userStore.userInfo.id ? '战队队长' : '正式成员' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" align="center">
                <template #default="scope">
                  <el-button 
                    v-if="scope.row.leaderId === userStore.userInfo.id" 
                    type="primary" 
                    size="small"
                    @click="$router.push(`/team/manage?teamId=${scope.row.id}`)"
                  >战队管理</el-button>
                  <el-button v-else link type="primary" @click="$router.push('/team')">查看大厅</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="myTeams.length === 0" description="暂未加入任何战队" />
          </div>

          <!-- 3. 修改密码 -->
          <div v-else-if="activeTab === 'password'" class="tab-content">
            <div class="section-title">账户安全设置</div>
            <el-form :model="pwdForm" label-width="100px" class="profile-form">
              <el-form-item label="原密码" required>
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" required>
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码" required>
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdatePassword">更新密码</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 4. 系统留言 -->
          <div v-else-if="activeTab === 'message'" class="tab-content">
            <div class="header-with-btn">
              <div class="section-title">系统留言与反馈</div>
              <el-button type="primary" size="small" @click="showMessageDialog = true">我要留言</el-button>
            </div>
            <div class="message-list">
              <el-card v-for="msg in messages" :key="msg.id" shadow="never" class="msg-item-card">
                <div class="msg-content">{{ msg.content }}</div>
                <div class="msg-time">{{ formatDate(msg.createTime) }}</div>
                <div v-if="msg.reply" class="msg-reply">
                  <span class="reply-label">官方回复：</span>
                  {{ msg.reply }}
                </div>
              </el-card>
              <el-empty v-if="messages.length === 0" description="暂无留言记录" />
            </div>
          </div>

          <!-- 我的帖子 -->
          <div v-else-if="activeTab === 'post'" class="tab-content">
            <div class="header-with-btn">
              <div class="section-title">我的发帖记录</div>
              <el-button type="primary" size="small" @click="openPostDialog()">发布新帖</el-button>
            </div>
            
            <div class="post-list" v-loading="loading">
              <el-card v-for="post in myPosts" :key="post.id" shadow="hover" class="my-post-card">
                <div class="post-header">
                  <el-tag size="small" type="info">{{ post.category }}</el-tag>
                  <el-tag size="small" type="success" effect="dark" style="margin-left: 8px">{{ post.gameProject || '综合' }}</el-tag>
                  <span class="post-time" style="margin-left: auto; color: #999; font-size: 13px">{{ formatDate(post.createTime) }}</span>
                </div>
                <h3 class="post-title" @click="$router.push(`/community/${post.id}`)">{{ post.title }}</h3>
                <div class="post-actions">
                  <span class="stats"><el-icon><View /></el-icon> {{ post.views || post.viewCount || 0 }} &nbsp;&nbsp; <el-icon><ChatDotRound /></el-icon> {{ post.comments || post.commentCount || 0 }}</span>
                  <div>
                    <el-button type="primary" link @click="openPostDialog(post)">编辑</el-button>
                    <el-button type="danger" link @click="deletePost(post.id)">删除</el-button>
                  </div>
                </div>
              </el-card>
              <el-empty v-if="myPosts.length === 0" description="暂无发帖记录" />
            </div>
          </div>

          <!-- 其他暂未实现的占位 -->
          <div v-else class="tab-content">
            <el-empty description="该模块正在紧急同步中..." />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发帖/编辑弹窗 -->
    <el-dialog v-model="postDialogVisible" :title="postForm.id ? '编辑帖子' : '发布新帖'" width="800px" top="5vh">
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="帖子标题" required>
          <el-input v-model="postForm.title" placeholder="请输入吸引人的标题..." />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属板块" required>
              <el-select v-model="postForm.category" style="width: 100%">
                <el-option label="赛事讨论" value="赛事讨论" />
                <el-option label="技术交流" value="技术交流" />
                <el-option label="组队开黑" value="组队开黑" />
                <el-option label="吃瓜灌水" value="吃瓜灌水" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联游戏" required>
              <el-select v-model="postForm.gameProject" style="width: 100%">
                <el-option label="综合讨论" value="综合" />
                <el-option label="英雄联盟" value="LOL" />
                <el-option label="王者荣耀" value="王者荣耀" />
                <el-option label="CS2" value="CS2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="帖子正文" required>
          <div style="border: 1px solid #ccc; width: 100%">
            <Toolbar style="border-bottom: 1px solid #ccc" :editor="editorRef" :mode="'default'" />
            <Editor style="height: 300px; overflow-y: hidden;" v-model="postForm.content" :mode="'default'" @onCreated="handleCreated" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 留言弹窗 -->
    <el-dialog v-model="showMessageDialog" title="提交意见建议" width="480px">
      <el-input v-model="newMessage" type="textarea" :rows="5" placeholder="请详细描述您的问题或建议，我们将尽快回复..." />
      <template #footer>
        <el-button @click="showMessageDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitMessage">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css'
import { ref, onMounted, watch, shallowRef, onBeforeUnmount } from 'vue';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../store/user';
import request from '../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import env from '../../config/env';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const activeTab = ref('info');
const myTeams = ref([]);
const messages = ref([]);
const myPosts = ref([]);
const showMessageDialog = ref(false);
const newMessage = ref('');
const loading = ref(false);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 用户在所有游戏板块的身份
const userIdentities = ref({});

// 获取用户在所有游戏板块的身份
const fetchAllIdentities = async () => {
  userIdentities.value = await userStore.getAllIdentities();
};

// 富文本编辑器相关
const postDialogVisible = ref(false);
const editorRef = shallowRef();
const postForm = ref({
  id: null,
  title: '',
  category: '赛事讨论',
  gameProject: '综合',
  content: ''
});

const handleCreated = (editor) => { editorRef.value = editor; };
onBeforeUnmount(() => { if (editorRef.value) editorRef.value.destroy(); });

const uploadHeaders = { Authorization: `Bearer ${userStore.token}` };
const editForm = ref({ ...userStore.userInfo });
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' });

watch(() => route.query.tab, (newTab) => {
  if (newTab) {
    activeTab.value = newTab;
    loadTabData(newTab);
  }
}, { immediate: true });

const handleSelect = (index) => {
  activeTab.value = index;
  router.push({ path: '/user/profile', query: { tab: index } });
  loadTabData(index);
};

const loadTabData = (tab) => {
  if (tab === 'team') fetchMyTeams();
  if (tab === 'message') fetchMessages();
  if (tab === 'post') fetchMyPosts();
};

const fetchMyPosts = async () => {
  if (!userStore.userInfo.id) return;
  loading.value = true;
  try {
    const res = await request.get(`/post/my/${userStore.userInfo.id}`);
    myPosts.value = res.data || [];
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const openPostDialog = (post = null) => {
  if (post) {
    postForm.value = { ...post };
  } else {
    postForm.value = {
      id: null,
      title: '',
      category: '赛事讨论',
      gameProject: '综合',
      content: ''
    };
  }
  postDialogVisible.value = true;
};

const submitPost = async () => {
  if (!postForm.value.title || !postForm.value.content) {
    return ElMessage.warning('请填写完整标题和内容');
  }
  try {
    const data = {
      ...postForm.value,
      userId: userStore.userInfo.id,
      nickname: userStore.userInfo.nickname || userStore.userInfo.username,
      university: userStore.userInfo.university
    };
    await request.post('/post/save', data);
    ElMessage.success('保存成功');
    postDialogVisible.value = false;
    fetchMyPosts();
  } catch (err) {
    console.error(err);
  }
};

const deletePost = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条帖子吗？将连同评论一起删除。', '警告', { type: 'warning' });
    await request.post(`/post/delete/${id}`);
    ElMessage.success('删除成功');
    fetchMyPosts();
  } catch (e) {}
};

const fetchMyTeams = async () => {
  if (!userStore.userInfo.id) return;
  loading.value = true;
  try {
    const res = await request.get(`/team/my/${userStore.userInfo.id}`);
    myTeams.value = res.data || [];
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
    ElMessage.success('个人档案更新成功');
    userStore.setUserInfo({ ...userStore.userInfo, ...editForm.value });
  } catch (err) { console.error(err); }
};

const handleUpdatePassword = async () => {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    return ElMessage.warning('请输入完整密码信息');
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    return ElMessage.error('两次输入的新密码不一致');
  }
  try {
    await request.post('/user/password', {
      userId: userStore.userInfo.id,
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    });
    ElMessage.success('密码修改成功，请重新登录');
    userStore.logout();
    router.push('/login');
  } catch (err) { console.error(err); }
};

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    // 确保头像路径是完整的URL
    let avatarUrl = res.data;
    if (avatarUrl && !avatarUrl.startsWith('http')) {
      avatarUrl = env.getFullApiUrl(avatarUrl);
    }
    // 更新表单和用户存储中的头像
    editForm.value.avatar = avatarUrl;
    userStore.setUserInfo({ ...userStore.userInfo, avatar: avatarUrl });
    handleUpdateProfile();
  }
};

const handleSubmitMessage = async () => {
  if (!newMessage.value) return;
  try {
    await request.post('/message/submit', {
      userId: userStore.userInfo.id,
      content: newMessage.value
    });
    ElMessage.success('留言提交成功');
    newMessage.value = '';
    showMessageDialog.value = false;
    fetchMessages();
  } catch (err) { console.error(err); }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
};

const fetchUserInfo = async () => {
  if (!userStore.userInfo.id) return;
  try {
    const res = await request.get(`/user/${userStore.userInfo.id}`);
    userStore.setUserInfo(res.data);
    editForm.value = { ...res.data };
  } catch (err) {
    console.error(err);
  }
};

onMounted(async () => {
  if (!userStore.token) {
    router.push('/login');
    return;
  }
  // 先获取最新的用户信息
  await fetchUserInfo();
  await fetchAllIdentities();
  loadTabData(activeTab.value);
});
</script>

<style scoped>
.profile-page {
  padding: 20px 0;
}

.sidebar-card {
  border-radius: 8px;
  min-height: 600px;
}

.user-profile-summary {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
}

.avatar-edit-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(0,0,0,0.4);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-uploader:hover .avatar-edit-mask {
  opacity: 1;
}

.user-meta {
  text-align: center;
  margin-top: 15px;
}

.username {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #333;
  word-break: break-word;
  max-width: 100%;
}

.tags-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.university-tag {
  font-size: 13px;
  color: #666;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
  width: 100%;
  max-width: 200px;
}

.profile-menu {
  border-right: none;
}

.profile-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

.profile-menu :deep(.el-menu-item.is-active) {
  background-color: #f0f7ff;
  border-right: 3px solid var(--primary);
}

.content-card {
  border-radius: 8px;
  min-height: 600px;
  padding: 10px;
}

.tab-content {
  animation: fadeIn 0.3s;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #333;
  padding-left: 12px;
  border-left: 4px solid var(--primary);
}

.header-with-btn {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.profile-form {
  max-width: 600px;
}

.team-table {
  margin-top: 10px;
}

.msg-item-card {
  margin-bottom: 15px;
  border-radius: 8px;
  background-color: #fafafa;
}

.msg-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.msg-time {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}

.msg-reply {
  margin-top: 12px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
  font-size: 13px;
  color: #67c23a;
}

.reply-label {
  font-weight: bold;
}

.my-post-card {
  margin-bottom: 15px;
}
.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.post-title {
  font-size: 16px;
  color: #333;
  margin: 0 0 15px 0;
  cursor: pointer;
  transition: color 0.3s;
}
.post-title:hover {
  color: var(--primary);
}
.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f5f5f5;
  padding-top: 10px;
}
.post-actions .stats {
  color: #999;
  font-size: 13px;
  display: flex;
  align-items: center;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
