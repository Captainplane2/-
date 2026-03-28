<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="nav-header">
      <div class="header-inner">
        <!-- Logo 部分 -->
        <div class="header-left" @click="$router.push('/')">
          <div class="logo-icon">
            <el-icon :size="24"><Trophy /></el-icon>
          </div>
          <span class="site-name">{{ config.siteName || '蘸豆爽！' }}</span>
        </div>

        <!-- 中间菜单 -->
        <div class="header-center">
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            class="site-menu"
            :trigger="'click'"
            :collapse-transition="false"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/">首页</el-menu-item>
            <!-- 游戏选择菜单 -->
            <el-sub-menu :index="currentGameIndex" popper-append-to-body>
              <template #title>
                <div class="game-select-wrapper">
                  <span class="game-select-title" @click.stop="goToGameHome">{{ currentGameText }}</span>
                </div>
              </template>
              <el-menu-item index="/all-games">全部游戏</el-menu-item>
              <el-menu-item v-for="project in gameProjects" :key="project.name" :index="'/' + project.name">
                {{ project.displayName }}
              </el-menu-item>
              <el-menu-item v-if="gameProjects.length === 0" index="/no-games" disabled>
                暂无游戏板块
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item :index="teamPath">战队信息</el-menu-item>
            <el-menu-item :index="userPath">选手信息</el-menu-item>
            <el-menu-item :index="matchPath">约战大厅</el-menu-item>
            <el-menu-item index="/community">蘸豆爽吧</el-menu-item>
            <el-menu-item index="/news">电竞新闻</el-menu-item>
            <el-menu-item index="/message">系统留言</el-menu-item>
            <el-menu-item index="/notice">系统公告</el-menu-item>
          </el-menu>
        </div>

        <!-- 右侧用户 -->
        <div class="header-right">
          <div class="user-actions">
            <template v-if="!userStore.token">
              <el-button plain @click="$router.push('/login')">登录</el-button>
              <el-button plain @click="$router.push('/register')">注册</el-button>
            </template>
            <el-dropdown v-else @command="handleCommand" trigger="hover">
              <div class="user-profile-trigger">
                <!-- 个人中心图标右上角红点 -->
                <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge-item">
                  <el-avatar :size="32" :src="userStore.userInfo.avatar" />
                </el-badge>
                <span class="nickname">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  
                  <!-- 动态栏目：加入战队/创建战队 -->
                  <el-dropdown-item v-if="userStore.userInfo.role === 'ROLE_USER'" command="join-team">加入战队</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.userInfo.role === 'ROLE_LEADER'" command="create-team">创建战队</el-dropdown-item>
                  <!-- 战队管理处也显示一样的角标 -->
                  <el-dropdown-item v-if="userStore.userInfo.role === 'ROLE_LEADER'" command="manage-team">
                    战队管理
                    <el-badge v-if="pendingCount > 0" is-dot class="menu-badge" />
                  </el-dropdown-item>
                  
                  <!-- 约战管理 -->
                  <el-dropdown-item v-if="userStore.userInfo.role === 'ROLE_LEADER'" command="manage-match">
                    约战管理
                  </el-dropdown-item>
                  
                  <el-dropdown-item command="my-posts">我的帖子</el-dropdown-item>
                  <el-dropdown-item command="admin" v-if="userStore.userInfo.role === 'ROLE_ADMIN'">管理后台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </el-header>

    <el-main class="main-body">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <el-footer class="site-footer">
      <div class="footer-content">
        <p>© 2026 {{ config.siteName || '蘸豆爽！' }} | {{ config.icp || '粤ICP备18154309号' }}</p>
      </div>
    </el-footer>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="修改账户密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../store/user';
import { useGameProjectStore } from '../store/gameProject';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const gameProjectStore = useGameProjectStore();

// 网站配置
const config = ref({
  siteName: '蘸豆爽！',
  icp: '粤ICP备18154309号'
});

const activeMenu = computed(() => {
  const path = route.path;
  // 如果是游戏板块相关的路径，返回对应的基础路径
  if (path.includes('/team')) return teamPath.value;
  if (path.includes('/user/profile')) return userPath.value;
  if (path.includes('/match')) return matchPath.value;
  // 全部游戏路径
  if (path === '/all-games' || (path === '/' && !gameProjectStore.currentGameProject)) {
    return '/all-games';
  }
  return path;
});

// 当前游戏名称和索引
const currentGameText = ref('选择游戏');
const currentGameIndex = ref('/game');

// 动态路径计算
const teamPath = computed(() => {
  return gameProjectStore.getGameProjectPath('/team');
});

const matchPath = computed(() => {
  return gameProjectStore.getGameProjectPath('/match');
});

const userPath = computed(() => {
  return gameProjectStore.getGameProjectPath('/players');
});

// 游戏板块列表
const gameProjects = ref([]);

// 获取游戏板块列表
const fetchGameProjects = async () => {
  try {
    const res = await request.get('/game-project/list-enabled');
    gameProjects.value = res.data || [];
    await gameProjectStore.fetchGameProjects();
  } catch (err) {
    console.error('获取游戏板块列表失败', err);
  }
};

// 监听路由变化，更新当前游戏名称
const updateCurrentGame = () => {
  const path = route.path;
  const pathParts = path.split('/').filter(Boolean);
  
  if (pathParts.length > 0) {
    const firstPart = pathParts[0];
    
    // 检查是否是全部游戏路径
    if (firstPart === 'all-games') {
      currentGameText.value = '全部游戏';
      currentGameIndex.value = '/all-games';
      gameProjectStore.setCurrentGameProject(null);
      return;
    }
    
    const gameProject = gameProjects.value.find(p => p.name.toUpperCase() === firstPart.toUpperCase());
    
    if (gameProject) {
      // 如果路径的第一部分是游戏板块，更新游戏选择
      currentGameText.value = gameProject.displayName;
      currentGameIndex.value = '/' + gameProject.name;
      gameProjectStore.setCurrentGameProject(gameProject.name);
    } else {
      // 如果路径的第一部分不是游戏板块，检查是否是首页路径
      // 在首页路径下，如果当前是"全部游戏"状态，保持该状态
      if (path === '/' && !gameProjectStore.currentGameProject) {
        currentGameText.value = '全部游戏';
        currentGameIndex.value = '/all-games';
      }
    }
  } else {
    // 根路径，检查是否是"全部游戏"状态
    if (!gameProjectStore.currentGameProject) {
      currentGameText.value = '全部游戏';
      currentGameIndex.value = '/all-games';
    }
  }
};

// 跳转到游戏首页
const goToGameHome = () => {
  if (currentGameIndex.value !== '/game' && currentGameIndex.value !== '/') {
    router.push(currentGameIndex.value);
  }
};

// 消息红点逻辑
const pendingCount = ref(0);
let timer = null;

const fetchPendingCount = async () => {
  // 先判断 token 是否存在
  if (!userStore.token) {
    pendingCount.value = 0;
    return;
  }
  try {
    const res = await request.get(`/team/pending-count/${userStore.userInfo.id}`);
    pendingCount.value = res.data || 0;
  } catch (err) {
    console.error('获取消息数失败', err);
  }
};

// 获取网站配置
const fetchConfig = async () => {
  try {
    const res = await request.get('/config/list');
    res.data.forEach(item => {
      if (item.configKey in config.value) {
        config.value[item.configKey] = item.configValue;
      }
    });
  } catch (err) {
    console.error('获取配置失败', err);
  }
};

onMounted(() => {
  updateCurrentGame();
  // 监听路由变化
  router.afterEach(() => {
    updateCurrentGame();
  });
  
  fetchConfig();
  fetchPendingCount();
  fetchGameProjects();
  // 缩短轮询时间至 10 秒，提高实时性
  timer = setInterval(fetchPendingCount, 10000);
  // 监听全局事件，以便在 management.vue 操作后即时刷新
  window.addEventListener('refresh-pending-count', fetchPendingCount);
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
  window.removeEventListener('refresh-pending-count', fetchPendingCount);
});

// 修改密码逻辑
const pwdDialogVisible = ref(false);
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const handleMenuSelect = (index) => {
  // 处理游戏选择菜单的子菜单项
  if (index === '/all-games') {
    // 全部游戏，跳转到 /all-games 路径
    gameProjectStore.setCurrentGameProject(null);
    currentGameText.value = '全部游戏';
    currentGameIndex.value = '/all-games';
    router.push('/all-games');
    return;
  }
  
  // 检查是否是游戏板块路径
  const gameProject = gameProjects.value.find(p => '/' + p.name === index);
  if (gameProject) {
    gameProjectStore.setCurrentGameProject(gameProject.name);
    currentGameText.value = gameProject.displayName;
    currentGameIndex.value = '/' + gameProject.name;
    router.push(index);
    return;
  }
  
  // 处理其他菜单项
  if (index === teamPath.value) {
    router.push(teamPath.value);
  } else if (index === userPath.value) {
    router.push(userPath.value);
  } else if (index === matchPath.value) {
    router.push(matchPath.value);
  } else {
    router.push(index);
  }
};

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    router.push('/user/profile');
  } else if (command === 'admin') {
    router.push('/admin');
  } else if (command === 'join-team' || command === 'create-team') {
    router.push(teamPath.value);
  } else if (command === 'password') {
    pwdDialogVisible.value = true;
  } else if (command === 'manage-team') {
    router.push(gameProjectStore.getGameProjectPath('/team/manage'));
  } else if (command === 'manage-match') {
    router.push(gameProjectStore.getGameProjectPath('/match/manage'));
  } else if (command === 'my-posts') {
    router.push('/community/my-posts');
  }
};

const handleUpdatePassword = async () => {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    ElMessage.warning('请输入密码');
    return;
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致');
    return;
  }

  try {
    await request.post('/user/password', {
      userId: userStore.userInfo.id,
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    });
    ElMessage.success('密码修改成功，请重新登录');
    pwdDialogVisible.value = false;
    userStore.logout();
    router.push('/login');
  } catch (err) {
    // 错误已由拦截器处理
  }
};
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.nav-header {
  background-color: var(--bg-white);
  height: 64px !important;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  justify-content: center;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-inner {
  width: 100%;
  max-width: 1600px;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 30px;
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background-color: var(--primary);
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  margin-right: 10px;
}

.site-name {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.header-center {
  flex: 1;
}

.site-menu {
  border-bottom: none;
}

.site-menu :deep(.el-menu-item),
.site-menu :deep(.el-sub-menu__title) {
  height: 64px;
  line-height: 64px;
  font-size: 15px;
  color: #333;
  box-sizing: border-box;
  position: relative;
}

/* 确保菜单项点击区域 */
.site-menu :deep(.el-menu-item) {
  padding: 0 20px;
}

/* 修复蓝条显示问题 - 首页激活时不延伸到选择游戏 */
.site-menu :deep(.el-menu) {
  position: relative;
  z-index: 1;
}

.site-menu :deep(.el-menu-item.is-active) {
  color: var(--primary);
  border-bottom: 2px solid var(--primary);
  position: relative;
  z-index: 2;
}

/* 选择游戏下拉菜单激活时不显示蓝条 */
.site-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--primary);
  border-bottom: none;
  position: relative;
  z-index: 2;
}

/* 当首页激活时，确保蓝条不延伸到选择游戏 */
.site-menu :deep(.el-menu-item:nth-child(1).is-active) ~ .el-sub-menu .el-sub-menu__title,
.site-menu :deep(.el-menu-item.is-active:first-child) {
  border-bottom: 2px solid var(--primary);
}

/* 选择游戏菜单项单独样式 */
.site-menu :deep(.el-sub-menu__title) {
  border-bottom: none !important;
}

/* 修复下拉箭头与文字重叠问题 */
.site-menu :deep(.el-sub-menu__title) {
  display: flex;
  align-items: center;
  padding-right: 30px !important;
}

.game-select-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  position: relative;
}

.game-select-title {
  cursor: pointer;
  flex: 1;
}

.game-select-title:hover {
  color: var(--primary);
}

/* 确保Element Plus的默认箭头能正常显示和点击 */
.site-menu :deep(.el-sub-menu__title) {
  display: flex;
  align-items: center;
  position: relative;
  cursor: default;
}

.site-menu :deep(.el-sub-menu__title .el-sub-menu__icon-arrow) {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  z-index: 10;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 禁止鼠标悬浮显示下拉菜单，只有点击箭头才显示 */
.site-menu :deep(.el-sub-menu:hover > .el-sub-menu__title) {
  background-color: transparent !important;
}

/* 确保箭头点击区域 */
.site-menu :deep(.el-sub-menu__icon-arrow) {
  cursor: pointer;
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

/* 确保蓝条不延伸 */
.site-menu :deep(.el-menu) {
  display: flex;
  align-items: center;
}

.site-menu :deep(.el-sub-menu) {
  position: relative;
}

.site-menu :deep(.el-sub-menu__title) {
  padding: 0 20px;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 消除悬停黑框并美化 */
.user-profile-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
  outline: none; /* 消除 focus 时的黑框/外边框 */
}

.user-profile-trigger:hover {
  background-color: #f5f7fa;
}

/* 深度选择器强制去除 Element Plus 下拉触发器的默认 focus 样式 */
:deep(.el-dropdown-link:focus-visible),
:deep(.el-tooltip__trigger:focus-visible) {
  outline: none !important;
}

.nickname {
  font-size: 14px;
  color: #333;
}

.badge-item :deep(.el-badge__content) {
  top: 5px;
  right: 5px;
}

.menu-badge {
  margin-left: 5px;
}

.main-body {
  flex: 1;
  padding: 20px;
  background-color: var(--bg-body);
}

.site-footer {
  background-color: var(--bg-white);
  padding: 20px 0;
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
  border-top: 1px solid var(--border-color);
}
</style>