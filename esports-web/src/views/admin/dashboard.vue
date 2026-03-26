<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statItems" :key="item.label">
        <el-card shadow="never" class="data-card">
          <div class="card-body">
            <el-icon class="icon" :style="{ color: item.color }">
              <component :is="item.icon" />
            </el-icon>
            <div class="info">
              <div class="value">{{ item.value }}</div>
              <div class="label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card shadow="never" header="游戏项目分布">
          <div ref="gameChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="用户增长趋势 (模拟)">
          <div ref="trendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import request from '../../utils/request';

const statItems = ref([
  { label: '用户总量', value: 0, icon: 'User', color: '#409eff' },
  { label: '活跃战队', value: 0, icon: 'Flag', color: '#67c23a' },
  { label: '约赛场次', value: 0, icon: 'Soccer', color: '#e6a23c' },
  { label: '社区热帖', value: 0, icon: 'ChatLineSquare', color: '#f56c6c' }
]);

const gameChartRef = ref(null);
const trendChartRef = ref(null);

const fetchStats = async () => {
  try {
    const res = await request.get('/stats/dashboard');
    const data = res.data;
    statItems.value[0].value = data.userCount;
    statItems.value[1].value = data.teamCount;
    statItems.value[2].value = data.matchCount;
    statItems.value[3].value = data.postCount;

    initCharts(data.gameDist);
  } catch (err) {
    console.error(err);
  }
};

const initCharts = (gameDist) => {
  // 游戏分布饼图
  const gameChart = echarts.init(gameChartRef.value);
  const pieData = Object.keys(gameDist).map(key => ({
    name: key,
    value: gameDist[key]
  }));
  
  gameChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, position: 'outside' },
      data: pieData.length > 0 ? pieData : [{ name: '暂无数据', value: 0 }]
    }]
  });

  // 模拟趋势折线图
  const trendChart = echarts.init(trendChartRef.value);
  trendChart.setOption({
    xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
    yAxis: { type: 'value' },
    series: [{
      data: [150, 230, 224, 218, 135, 147, 260],
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.1 }
    }]
  });
};

onMounted(() => {
  fetchStats();
});
</script>

<style scoped>
.mt-20 { margin-top: 20px; }

.data-card .card-body {
  display: flex;
  align-items: center;
  padding: 10px;
}

.data-card .icon {
  font-size: 48px;
  margin-right: 20px;
}

.data-card .value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.data-card .label {
  color: #909399;
  font-size: 14px;
}
</style>
