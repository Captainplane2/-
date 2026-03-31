import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import request from '../utils/request';

export const useMatchStatusStore = defineStore('matchStatus', () => {
  // 当前约战状态
  const currentStatus = ref('WAITING');
  
  // 准备状态
  const hostReady = ref(false);
  const guestReady = ref(false);
  
  // 倒计时
  const countdown = ref(null);
  const countdownSeconds = ref(0);
  
  // 结束确认计数
  const finishConfirmCount = ref(0);
  
  // 当前约战ID
  const currentMatchId = ref(null);
  
  // 计算属性
  const isBothReady = computed(() => hostReady.value && guestReady.value);
  
  const isMatchExpired = computed(() => {
    // 这里需要根据实际的开赛时间计算
    // 暂时返回false，具体逻辑在组件中实现
    return false;
  });
  
  // 状态变更方法
  const updateStatus = (newStatus) => {
    currentStatus.value = newStatus;
  };
  
  // 准备/取消准备
  const toggleReady = async (matchId, userId, teamType) => {
    try {
      const response = await request.post(`/match-status/ready/${matchId}`, null, {
        params: {
          userId,
          teamType
        }
      });
      
      if (response.code === 200) {
        const room = response.data;
        
        // 更新本地状态
        hostReady.value = room.hostTeamReady;
        guestReady.value = room.guestTeamReady;
        currentStatus.value = room.matchStatus;
        
        // 如果双方都准备，开始倒计时
        if (room.hostTeamReady && room.guestTeamReady && room.countdownSeconds) {
          startCountdown(room.countdownSeconds);
        } else {
          // 任意一方取消准备，重置倒计时
          resetCountdown();
        }
        
        return room;
      } else {
        throw new Error(response.message || '操作失败');
      }
    } catch (error) {
      console.error('准备操作失败:', error);
      throw error;
    }
  };
  
  // 开始倒计时
  const startCountdown = (seconds) => {
    resetCountdown();
    countdownSeconds.value = seconds;
    
    countdown.value = setInterval(() => {
      countdownSeconds.value--;
      
      if (countdownSeconds.value <= 0) {
        clearInterval(countdown.value);
        countdown.value = null;
        
        // 倒计时结束，自动开始比赛
        startMatch(currentMatchId.value);
      }
    }, 1000);
  };
  
  // 开始比赛
  const startMatch = async (matchId) => {
    try {
      const response = await request.post(`/match-status/start/${matchId}`);
      
      if (response.code === 200) {
        const room = response.data;
        currentStatus.value = room.matchStatus;
        resetCountdown();
        return room;
      } else {
        throw new Error(response.message || '开始比赛失败');
      }
    } catch (error) {
      console.error('开始比赛失败:', error);
      throw error;
    }
  };
  
  // 确认比赛结束
  const confirmFinish = async (matchId, userId) => {
    try {
      const response = await request.post(`/match-status/finish-confirm/${matchId}`, null, {
        params: { userId }
      });
      
      if (response.code === 200) {
        const room = response.data;
        finishConfirmCount.value = room.finishConfirmCount;
        
        // 如果双方都确认结束，开始结束倒计时
        if (room.finishConfirmCount >= 2 && room.countdownSeconds) {
          startFinishCountdown(room.countdownSeconds);
        }
        
        return room;
      } else {
        throw new Error(response.message || '确认结束失败');
      }
    } catch (error) {
      console.error('确认结束失败:', error);
      throw error;
    }
  };
  
  // 开始结束倒计时
  const startFinishCountdown = (seconds) => {
    resetCountdown();
    countdownSeconds.value = seconds;
    
    countdown.value = setInterval(() => {
      countdownSeconds.value--;
      
      if (countdownSeconds.value <= 0) {
        clearInterval(countdown.value);
        countdown.value = null;
        
        // 结束倒计时结束，完成比赛
        finishMatch(currentMatchId.value);
      }
    }, 1000);
  };
  
  // 完成比赛
  const finishMatch = async (matchId) => {
    try {
      const response = await request.post(`/match-status/finish/${matchId}`);
      
      if (response.code === 200) {
        const room = response.data;
        currentStatus.value = room.matchStatus;
        finishConfirmCount.value = 0;
        resetCountdown();
        return room;
      } else {
        throw new Error(response.message || '完成比赛失败');
      }
    } catch (error) {
      console.error('完成比赛失败:', error);
      throw error;
    }
  };
  
  // 重置倒计时
  const resetCountdown = () => {
    if (countdown.value) {
      clearInterval(countdown.value);
      countdown.value = null;
      countdownSeconds.value = 0;
    }
  };
  
  // 初始化状态
  const initStatus = (room) => {
    currentMatchId.value = room.id;
    currentStatus.value = room.matchStatus || 'WAITING';
    hostReady.value = room.hostTeamReady || false;
    guestReady.value = room.guestTeamReady || false;
    finishConfirmCount.value = room.finishConfirmCount || 0;
    
    // 如果有活跃的倒计时，重新开始
    if (room.countdownStartTime && room.countdownSeconds) {
      const now = new Date().getTime();
      const startTime = new Date(room.countdownStartTime).getTime();
      const elapsedSeconds = Math.floor((now - startTime) / 1000);
      const remainingSeconds = Math.max(0, room.countdownSeconds - elapsedSeconds);
      
      if (remainingSeconds > 0) {
        startCountdown(remainingSeconds);
      }
    }
  };
  
  // 重置状态
  const reset = () => {
    currentStatus.value = 'WAITING';
    hostReady.value = false;
    guestReady.value = false;
    finishConfirmCount.value = 0;
    resetCountdown();
    currentMatchId.value = null;
  };
  
  return {
    // 状态
    currentStatus,
    hostReady,
    guestReady,
    countdownSeconds,
    finishConfirmCount,
    currentMatchId,
    
    // 计算属性
    isBothReady,
    isMatchExpired,
    
    // 方法
    updateStatus,
    toggleReady,
    startCountdown,
    startMatch,
    confirmFinish,
    finishMatch,
    resetCountdown,
    initStatus,
    reset
  };
});