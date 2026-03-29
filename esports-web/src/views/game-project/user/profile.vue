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
                <el-tag v-if="currentIdentity === '战队队长'" type="danger" effect="dark">{{ currentIdentity }}</el-tag>
                <el-tag v-else-if="currentIdentity === '战队成员'" type="primary" effect="dark">{{ currentIdentity }}</el-tag>
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
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧内容区 (新增标题栏，与示例网站一致) -->
      <el-col :span="19">
        <div class="content-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: `/${gameProject.value}` }">{{ gameProjectDisplayName }}</el-breadcrumb-item>
            <el-breadcrumb-item>选手信息</el-breadcrumb-item>
          </el-breadcrumb>
          <h1 class="page-title">选手信息</h1>
        </div>

        <el-card shadow="never" class="content-card">
          <div v-if="loadingComponent" class="loading-container">
            <el-icon :size="40"><Loading /></el-icon>
            <p class="loading-text">加载中...</p>
          </div>
          <component v-else :is="activeComponent" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../../store/user';
import { useGameProjectStore } from '../../../store/gameProject';
import { ElMessage } from 'element-plus';
import { User, Trophy, ChatLineRound, Star, Lock, School } from '@element-plus/icons-vue';
import env from '../../../config/env';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const gameProjectStore = useGameProjectStore();

// 游戏板块信息
const gameProject = computed(() => route.params.gameProject?.toUpperCase() || null);
const gameProjectDisplayName = computed(() => {
  if (!gameProject.value) return '选手信息';
  const project = gameProjectStore.getGameProjectByName(gameProject.value);
  return project ? project.displayName : gameProject.value;
});

// 当前游戏板块的用户身份
const currentIdentity = ref(null);

// 获取用户在当前游戏板块的身份
const fetchCurrentIdentity = async () => {
  if (gameProject.value) {
    currentIdentity.value = await userStore.getIdentityInGameProject(gameProject.value);
  }
};

// 头像上传
const defaultAvatar = 'https://via.placeholder.com/150/ccc/999?text=头像';
const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${userStore.token}`
  };
});

const handleAvatarSuccess = (response, file) => {
  if (response.code === 200) {
    userStore.updateUserInfo({ avatar: response.data });
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error('头像上传失败');
  }
};

// 标签页管理
const activeTab = ref('info');
const activeComponent = ref(null);
const loadingComponent = ref(false);

const handleSelect = (key) => {
  activeTab.value = key;
  loadComponent(key);
};

const loadComponent = (tab) => {
  loadingComponent.value = true;
  switch (tab) {
    case 'info':
      import('./info.vue').then(module => {
        activeComponent.value = module.default;
      }).catch(error => {
        console.error('加载info组件失败:', error);
      }).finally(() => {
        loadingComponent.value = false;
      });
      break;
    case 'team':
      import('./team.vue').then(module => {
        activeComponent.value = module.default;
      }).catch(error => {
        console.error('加载team组件失败:', error);
      }).finally(() => {
        loadingComponent.value = false;
      });
      break;
    case 'post':
      import('./post.vue').then(module => {
        activeComponent.value = module.default;
      }).catch(error => {
        console.error('加载post组件失败:', error);
      }).finally(() => {
        loadingComponent.value = false;
      });
      break;
    case 'collection':
      import('./collection.vue').then(module => {
        activeComponent.value = module.default;
      }).catch(error => {
        console.error('加载collection组件失败:', error);
      }).finally(() => {
        loadingComponent.value = false;
      });
      break;
    case 'password':
      import('./password.vue').then(module => {
        activeComponent.value = module.default;
      }).catch(error => {
        console.error('加载password组件失败:', error);
      }).finally(() => {
        loadingComponent.value = false;
      });
      break;
  }
};

// 组件挂载时获取用户身份
onMounted(async () => {
  if (!userStore.token) {
    router.push('/login');
    return;
  }
  loadComponent(activeTab.value);
  await fetchCurrentIdentity();
});

// 监听游戏板块变化，重新获取身份
watch(gameProject, async (newGameProject) => {
  if (newGameProject) {
    await fetchCurrentIdentity();
  }
});
</script>

<style scoped>
.profile-page {
  padding: 20px 0;
}

.sidebar-card {
  border-radius: 8px;
  overflow: hidden;
}

.user-profile-summary {
  padding: 20px 0;
  text-align: center;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-edit-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.5);
  color: white;
  text-align: center;
  padding: 4px;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-uploader:hover .avatar-edit-mask {
  opacity: 1;
}

.user-meta {
  margin-top: 15px;
}

.username {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.tags-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.university-tag {
  font-size: 14px;
  color: #666;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.profile-menu {
  border: none;
}

.profile-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 0 10px;
  border-radius: 8px;
  margin-bottom: 8px;
}

.profile-menu .el-menu-item.is-active {
  background-color: #f0f9ff;
  color: #1890ff;
}

.content-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 10px 0 0 0;
}

.content-card {
  border-radius: 8px;
  min-height: 600px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
}

.loading-text {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .el-col {
    width: 100% !important;
  }
  
  .sidebar-card {
    margin-bottom: 20px;
  }
}
</style>