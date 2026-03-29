// 环境配置文件
const env = {
  // API 基础地址
  // 开发环境使用 localhost:8081，生产环境使用相对路径 /api
  apiBaseURL: import.meta.env.MODE === 'development' 
    ? 'http://localhost:8081/api' 
    : '/api',
  
  // 获取完整的 API URL（用于图片等资源）
  getFullApiUrl(path) {
    if (path && path.startsWith('http')) {
      return path;
    }
    return this.apiBaseURL.replace('/api', '') + (path || '');
  }
};

export default env;
