<template>
  <div class="admin-settings">
    <el-card shadow="never">
      <template #header>系统可视化配置 (类WordPress)</template>
      
      <el-form label-width="150px" style="max-width: 800px">
        <el-divider content-position="left">基础设置</el-divider>
        <el-form-item label="网站标题">
          <el-input v-model="settings.siteName" placeholder="如：高校电竞集结站" />
        </el-form-item>
        <el-form-item label="备案号">
          <el-input v-model="settings.icp" />
        </el-form-item>
        
        <el-divider content-position="left">业务开关</el-divider>
        <el-form-item label="开放新用户注册">
          <el-switch v-model="settings.allowRegister" />
        </el-form-item>
        <el-form-item label="约赛自动审核">
          <el-switch v-model="settings.autoMatch" />
        </el-form-item>

        <el-divider content-position="left">视觉配置</el-divider>
        <el-form-item label="首页 Banner 1">
          <el-input v-model="settings.banner1" placeholder="图片 URL" />
        </el-form-item>
        <el-form-item label="首页 Banner 2">
          <el-input v-model="settings.banner2" placeholder="图片 URL" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" @click="handleSave">保存全局配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const settings = ref({
  siteName: '高校电竞集结站',
  icp: '粤ICP备18154309号',
  allowRegister: true,
  autoMatch: false,
  banner1: '',
  banner2: ''
});

const fetchConfig = async () => {
  try {
    const res = await request.get('/config/list');
    // 简单将 list 转为对象
    res.data.forEach(item => {
      if (item.configKey in settings.value) {
        if (typeof settings.value[item.configKey] === 'boolean') {
          settings.value[item.configKey] = item.configValue === 'true';
        } else {
          settings.value[item.configKey] = item.configValue;
        }
      }
    });
  } catch (err) {
    console.error(err);
  }
};

const handleSave = async () => {
  const configs = Object.keys(settings.value).map(key => ({
    configKey: key,
    configValue: String(settings.value[key])
  }));
  
  try {
    await request.post('/config/update', configs);
    ElMessage.success('系统配置已生效');
  } catch (err) {
    console.error(err);
  }
};

onMounted(fetchConfig);
</script>
