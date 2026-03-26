<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { ElConfigProvider } from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
import { onMounted } from 'vue';

onMounted(() => {
  // 移除 dark class，改用白色主题
  document.documentElement.classList.remove('dark');
});
</script>

<style>
/* 保证页面铺满 */
html, body, #app {
  height: 100%;
  width: 100%;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>
