<template>
  <div class="admin-layout">
    <el-aside class="sidebar" :width="isCollapse ? '64px' : '240px'">
      <div class="logo">
        <span v-if="!isCollapse">蘸豆爽！管理后台</span>
        <span v-else>ZDS</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        background-color="#ffffff"
        text-color="#333333"
        active-text-color="#2b308b"
        :collapse="isCollapse"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>首页看板</span>
        </el-menu-item>
        <el-sub-menu index="user-manage">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users">用户列表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="business-manage">
          <template #title>
            <el-icon><Management /></el-icon>
            <span>业务管理</span>
          </template>
          <el-menu-item index="/admin/teams">战队管理</el-menu-item>
          <el-menu-item index="/admin/matches">赛事管理</el-menu-item>
          <el-menu-item index="/admin/community">社区内容</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="content-manage">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容发布</span>
          </template>
          <el-menu-item index="/admin/news">新闻发布</el-menu-item>
          <el-menu-item index="/admin/notices">公告管理</el-menu-item>
          <el-menu-item index="/admin/messages">留言处理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="admin-main">
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">前台首页</el-breadcrumb-item>
            <el-breadcrumb-item>管理中心</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="admin-info">
              <el-avatar :size="24" :src="userStore.userInfo.avatar" style="margin-right: 8px" />
              {{ userStore.userInfo.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/')">回到首页</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="content-wrapper">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const isCollapse = ref(false);

const activeMenu = computed(() => route.path);

const handleLogout = () => {
  userStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
}

.sidebar {
  background-color: #fff;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border-color);
}

.logo {
  height: 64px;
  line-height: 64px;
  text-align: center;
  color: var(--primary);
  font-size: 18px;
  font-weight: bold;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  overflow: hidden;
  white-space: nowrap;
}

.admin-menu {
  border-right: none;
  flex: 1;
}

.admin-main {
  flex: 1;
  background-color: var(--bg-body);
  display: flex;
  flex-direction: column;
}

.admin-header {
  background: #fff;
  height: 64px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-regular);
}

.admin-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.content-wrapper {
  padding: 20px;
  overflow-y: auto;
}
</style>
