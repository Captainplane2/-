<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="always">
      <div class="card-title">
        <h2>新用户注册</h2>
        <p>加入 蘸豆爽！ 开启你的电竞之旅</p>
      </div>
      
      <el-form :model="regForm" :rules="rules" ref="regFormRef" label-width="0">
        <!-- 邮箱注册 -->
        <el-form-item prop="username">
          <el-input 
            v-model="regForm.username" 
            placeholder="请输入电子邮箱" 
            prefix-icon="Message" 
            size="large" 
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="regForm.password" 
            type="password" 
            placeholder="设置你的密码" 
            prefix-icon="Lock" 
            show-password 
            size="large" 
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="regForm.nickname" placeholder="选手昵称" prefix-icon="Edit" size="large" />
        </el-form-item>
        
        <!-- 完整的高校列表 -->
        <el-form-item prop="university">
          <el-select 
            v-model="regForm.university" 
            placeholder="选择所属高校 (985/211/双一流)" 
            filterable 
            size="large" 
            style="width: 100%"
          >
            <el-option v-for="uni in universityList" :key="uni" :label="uni" :value="uni" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="full-btn" size="large" @click="handleRegister">立即注册</el-button>
        </el-form-item>
      </el-form>
      
      <div class="card-footer">
        <span>已有账号？</span>
        <el-button link type="primary" @click="$router.push('/login')">返回登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import request from '../../utils/request';
import { ElMessage } from 'element-plus';

const router = useRouter();
const regFormRef = ref(null);
const loading = ref(false);

const regForm = ref({ 
  username: '', 
  password: '', 
  nickname: '', 
  university: '', 
  role: 'ROLE_USER' // 默认注册为普通选手，移除选择栏
});

// 常用邮箱后缀验证
const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@(?:[a-zA-Z0-9-]+\.)+(?:com|cn|net|org|edu|gov|me|info|io|edu\.cn)$/;
  
  if (!value) {
    callback(new Error('请输入电子邮箱'));
  } else if (!emailRegex.test(value)) {
    callback(new Error('请输入有效的邮箱地址'));
  } else {
    callback();
  }
};

const rules = {
  username: [{ validator: validateEmail, trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度至少6位', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  university: [{ required: true, message: '请选择高校', trigger: 'change' }]
};

// 完整的 985/211/双一流院校列表
const universityList = [
  "北京大学", "清华大学", "复旦大学", "上海交通大学", "浙江大学", "中国科学技术大学", "南京大学", "武汉大学", "中山大学", "西安交通大学", 
  "华中科技大学", "四川大学", "哈尔滨工业大学", "同济大学", "北京航空航天大学", "东南大学", "南开大学", "天津大学", "山东大学", "中南大学", 
  "厦门大学", "吉林大学", "华南理工大学", "兰州大学", "东北大学", "大连理工大学", "湖南大学", "重庆大学", "西北工业大学", "中国农业大学", 
  "电子科技大学", "华东师范大学", "北京师范大学", "中央民族大学", "中国海洋大学", "国防科技大学", "西北农林科技大学", "北京理工大学",
  "北京工业大学", "北京交通大学", "北京科技大学", "北京化工大学", "北京邮电大学", "北京林业大学", "北京中医药大学", "北京外国语大学", 
  "中国传媒大学", "对外经济贸易大学", "中央音乐学院", "中国地质大学（北京）", "中国政法大学", "中国石油大学（北京）", "中国矿业大学（北京）", 
  "中国科学院大学", "中国社会科学院大学", "天津工业大学", "天津医科大学", "天津中医药大学", "河北工业大学", "太原理工大学", "内蒙古大学", 
  "辽宁大学", "大连海事大学", "延边大学", "东北师范大学", "哈尔滨工程大学", "东北农业大学", "东北林业大学", "华东理工大学", "东华大学", 
  "上海海洋大学", "上海中医药大学", "上海师范大学", "上海大学", "上海财经大学", "上海外国语大学", "上海音乐学院", "上海体育学院", "上海科技大学", 
  "苏州大学", "南京师范大学", "中国药科大学", "南京航空航天大学", "南京理工大学", "河海大学", "南京邮电大学", "南京信息工程大学", "南京农业大学", 
  "南京中医药大学", "中国地质大学（武汉）", "中国矿业大学", "中国石油大学（华东）", "江南大学", "中国地质大学", "合肥工业大学", "安徽大学", 
  "福州大学", "南昌大学", "郑州大学", "河南大学", "武汉理工大学", "华中师范大学", "华中农业大学", "中南财经政法大学", "湖南师范大学", 
  "暨南大学", "华南师范大学", "海南大学", "广西大学", "贵州大学", "云南大学", "西藏大学", "西北大学", "西安电子科技大学", "长安大学", 
  "陕西师范大学", "宁夏大学", "新疆大学", "石河子大学", "青海大学", "四川农业大学", "成都理工大学", "西南石油大学", "成都中医药大学", 
  "西南财经大学", "西南交通大学"
];

const handleRegister = async () => {
  if (!regFormRef.value) return;
  
  await regFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await request.post('/user/register', regForm.value);
        ElMessage.success('注册成功！');
        router.push('/login');
      } catch (err) {
        console.error(err);
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.warning('请按要求填写注册信息');
      return false;
    }
  });
};
</script>

<style scoped>
.auth-page {
  height: 80vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.auth-card {
  width: 440px;
  padding: 30px 20px;
  border-radius: 12px;
}

.card-title {
  text-align: center;
  margin-bottom: 30px;
}

.card-title h2 {
  font-size: 24px;
  color: #333;
}

.card-title p {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}

.full-btn {
  width: 100%;
}

.card-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #666;
}
</style>
