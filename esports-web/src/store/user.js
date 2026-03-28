import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    userIdentities: {}, // 存储用户在各游戏板块的身份
    currentGameIdentity: null // 当前游戏板块的身份
  }),
  actions: {
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
    },
    setUserInfo(info) {
      this.userInfo = info;
      localStorage.setItem('userInfo', JSON.stringify(info));
    },
    logout() {
      this.token = '';
      this.userInfo = {};
      this.userIdentities = {};
      this.currentGameIdentity = null;
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
    },
    // 获取用户在指定游戏板块的身份
    async getIdentityInGameProject(gameProject) {
      if (!this.token) return null;
      
      try {
        const response = await fetch(`http://localhost:8080/api/user/identity/game/${gameProject}`, {
          headers: {
            'Authorization': `Bearer ${this.token}`
          }
        });
        
        if (response.ok) {
          const data = await response.json();
          if (data.code === 200) {
            this.currentGameIdentity = data.data.identity;
            return data.data.identity;
          }
        }
      } catch (error) {
        console.error('获取用户身份失败:', error);
      }
      return null;
    },
    // 获取用户在所有游戏板块的身份
    async getAllIdentities() {
      if (!this.token) return {};
      
      try {
        const response = await fetch('http://localhost:8080/api/user/identity/all', {
          headers: {
            'Authorization': `Bearer ${this.token}`
          }
        });
        
        if (response.ok) {
          const data = await response.json();
          if (data.code === 200) {
            this.userIdentities = data.data;
            return data.data;
          }
        }
      } catch (error) {
        console.error('获取用户所有身份失败:', error);
      }
      return {};
    },
    // 刷新用户身份信息
    async refreshIdentities() {
      await this.getAllIdentities();
    }
  }
});
