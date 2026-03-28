import { defineStore } from 'pinia';
import request from '../utils/request';

export const useGameProjectStore = defineStore('gameProject', {
  state: () => ({
    currentGameProject: null,
    gameProjectList: []
  }),

  getters: {
    getCurrentGameProject: (state) => state.currentGameProject,
    getGameProjectList: (state) => state.gameProjectList,
    getCurrentGameProjectName: (state) => {
      if (!state.currentGameProject) return '全部游戏';
      const project = state.gameProjectList.find(p => p.name === state.currentGameProject);
      return project ? project.displayName : state.currentGameProject;
    }
  },

  actions: {
    setCurrentGameProject(gameProject) {
      this.currentGameProject = gameProject;
    },

    async fetchGameProjects() {
      try {
        const res = await request.get('/game-project/list-enabled');
        this.gameProjectList = res.data || [];
      } catch (err) {
        console.error('获取游戏板块列表失败', err);
      }
    },

    getGameProjectByName(name) {
      return this.gameProjectList.find(p => p.name === name);
    },

    getGameProjectPath(path) {
      if (!this.currentGameProject || this.currentGameProject === 'all') {
        return path;
      }
      // 移除 path 开头的斜杠，避免生成双斜杠
      const cleanPath = path.startsWith('/') ? path.substring(1) : path;
      return `/${this.currentGameProject}/${cleanPath}`;
    }
  }
});