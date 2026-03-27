<template>
  <div class="admin-settings">
    <el-card shadow="never">
      <template #header>系统可视化配置</template>
      
      <el-form label-width="120px">
        <el-divider content-position="left">基础设置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="网站标题">
              <el-input v-model="settings.siteName" placeholder="如：高校电竞集结站" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备案号">
              <el-input v-model="settings.icp" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">业务开关</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="开放新用户注册">
              <el-switch v-model="settings.allowRegister" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="约赛自动审核">
              <el-switch v-model="settings.autoMatch" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">Banner 管理 (最多6个/模块)</el-divider>
        <div class="banner-manage-list">
          <el-form-item label="选择模块">
            <el-select v-model="selectedModule" placeholder="选择模块" @change="fetchBanners">
              <el-option label="首页" value="home" />
              <el-option label="CS2" value="cs2" />
              <el-option label="英雄联盟" value="lol" />
              <el-option label="王者荣耀" value="wzry" />
            </el-select>
          </el-form-item>
          <el-table :data="banners" border stripe style="width: 100%">
            <el-table-column label="预览" width="180">
              <template #default="scope">
                <el-image 
                  :src="scope.row.image" 
                  style="width: 150px; height: 80px; border-radius: 4px"
                  fit="cover"
                >
                  <template #error>
                    <div class="image-slot" style="background:#f5f7fa; display:flex; align-items:center; justify-content:center; height:100%; color:#999">
                      无图
                    </div>
                  </template>
                </el-image>
              </template>
            </el-table-column>
            <el-table-column label="标题 & 链接 & 排序">
              <template #default="scope">
                <el-input v-model="scope.row.title" placeholder="Banner 标题" size="small" style="margin-bottom: 5px" />
                <el-input v-model="scope.row.image" placeholder="图片 URL" size="small" style="margin-bottom: 5px">
                  <template #prepend>图片</template>
                </el-input>
                <el-input v-model="scope.row.url" placeholder="跳转链接 (如 /news/1 或 http://...)" size="small">
                  <template #prepend>链接</template>
                </el-input>
              </template>
            </el-table-column>
            <el-table-column label="排序" width="100">
              <template #default="scope">
                <el-input-number v-model="scope.row.sortOrder" :min="0" size="small" style="width: 80px" />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-switch 
                  v-model="scope.row.status" 
                  :active-value="0" 
                  :inactive-value="1"
                  active-text="显"
                  inactive-text="隐"
                  inline-prompt
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button type="danger" icon="Delete" circle size="small" @click="removeBanner(scope.$index, scope.row.id)" />
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 15px">
            <el-button type="success" icon="Plus" @click="addBanner" :disabled="banners.length >= 6">添加 Banner</el-button>
            <el-button type="primary" icon="Check" @click="saveBanners">保存所有 Banner</el-button>
          </div>
        </div>

        <el-divider />
        <el-form-item>
          <el-button type="primary" size="large" @click="handleSaveConfig">保存基础配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';

const settings = ref({
  siteName: '高校电竞集结站',
  icp: '粤ICP备18154309号',
  allowRegister: true,
  autoMatch: false
});

const selectedModule = ref('home');
const banners = ref([]);

const fetchConfig = async () => {
  try {
    const res = await request.get('/config/list');
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

const fetchBanners = async () => {
  try {
    const res = await request.get('/banner/list', { params: { module: selectedModule.value } });
    banners.value = res.data || [];
  } catch (err) {
    console.error(err);
  }
};

const addBanner = () => {
  if (banners.value.length < 6) {
    banners.value.push({
      title: '',
      image: '',
      url: '',
      sortOrder: banners.value.length,
      status: 0,
      module: selectedModule.value
    });
  }
};

const removeBanner = async (index, id) => {
  if (!id) {
    banners.value.splice(index, 1);
    return;
  }
  
  try {
    await ElMessageBox.confirm('确定要删除这个 Banner 吗？', '提醒');
    await request.post(`/banner/delete/${id}`);
    ElMessage.success('删除成功');
    fetchBanners();
  } catch (e) {}
};

const saveBanners = async () => {
  try {
    for (const b of banners.value) {
      if (!b.image) {
        ElMessage.warning('请确保所有 Banner 都填写了图片 URL');
        return;
      }
      // 确保每个banner都有module字段
      b.module = selectedModule.value;
      await request.post('/banner/save', b);
    }
    ElMessage.success('所有 Banner 已保存');
    fetchBanners();
  } catch (err) {
    console.error(err);
  }
};

const handleSaveConfig = async () => {
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

onMounted(() => {
  fetchConfig();
  fetchBanners();
});
</script>

<style scoped>
.admin-settings { padding: 10px; }
.banner-manage-list { margin-top: 10px; }
:deep(.el-divider__text) { font-weight: bold; color: var(--primary); }
</style>
