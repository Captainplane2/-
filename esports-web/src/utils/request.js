import axios from 'axios';
import { ElMessage } from 'element-plus';

const service = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端地址
  timeout: 5000
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      ElMessage.error(res.message || '系统异常');
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res;
  },
  error => {
    if (error.response && error.response.status === 401) {
      ElMessage.error('请先登录');
      localStorage.removeItem('token');
      // 可以跳转到登录页
    } else {
      ElMessage.error(error.message || '网络错误');
    }
    return Promise.reject(error);
  }
);

export default service;
