import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/home/index.vue')
      },
      {
        path: 'all-games',
        name: 'AllGames',
        component: () => import('../views/home/index.vue')
      },
      {
        path: 'team',
        name: 'Team',
        component: () => import('../views/team/list.vue')
      },
      {
        path: 'team/:id',
        name: 'TeamDetail',
        component: () => import('../views/team/detail.vue')
      },
      {
        path: 'match',
        name: 'Match',
        component: () => import('../views/match/list.vue')
      },
      {
        path: 'match/:id',
        name: 'MatchDetail',
        component: () => import('../views/match/detail.vue')
      },
      {
        path: 'match/manage',
        name: 'MatchManage',
        component: () => import('../views/game-project/match/manage.vue')
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('../views/community/index.vue')
      },
      {
        path: 'community/:id',
        name: 'CommunityDetail',
        component: () => import('../views/community/detail.vue')
      },
      {
        path: 'community/my-posts',
        name: 'MyPosts',
        component: () => import('../views/community/my-posts.vue')
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('../views/notice/index.vue')
      },
      {
        path: 'notice/:id',
        name: 'NoticeDetail',
        component: () => import('../views/notice/detail.vue')
      },
      {
        path: 'news',
        name: 'News',
        component: () => import('../views/news/index.vue')
      },
      {
        path: 'news/:id',
        name: 'NewsDetail',
        component: () => import('../views/news/detail.vue')
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('../views/message/index.vue')
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/user/profile.vue')
      },
      {
        path: 'user/info/:id',
        name: 'UserInfo',
        component: () => import('../views/user/info.vue')
      },
      {
        path: 'team/manage',
        name: 'TeamManage',
        component: () => import('../views/team/management.vue')
      },
      {
        path: 'user/info',
        name: 'UserInfoSelf',
        component: () => import('../views/user/info.vue')
      },
      {
        path: 'players',
        name: 'Players',
        component: () => import('../views/players/index.vue')
      },
      {
        path: ':gameProject',
        name: 'GameProjectHome',
        component: () => import('../views/game-project/index.vue')
      },
      {
        path: ':gameProject/team',
        name: 'GameProjectTeam',
        component: () => import('../views/game-project/team/list.vue')
      },
      {
        path: ':gameProject/team/:id',
        name: 'GameProjectTeamDetail',
        component: () => import('../views/game-project/team/detail.vue')
      },
      {
        path: ':gameProject/team/manage',
        name: 'GameProjectTeamManage',
        component: () => import('../views/game-project/team/management.vue')
      },
      {
        path: ':gameProject/match',
        name: 'GameProjectMatch',
        component: () => import('../views/game-project/match/list.vue')
      },
      {
        path: ':gameProject/match/:id',
        name: 'GameProjectMatchDetail',
        component: () => import('../views/game-project/match/detail.vue')
      },
      {
        path: ':gameProject/match/manage',
        name: 'GameProjectMatchManage',
        component: () => import('../views/game-project/match/manage.vue')
      },
      {
        path: ':gameProject/user/profile',
        name: 'GameProjectUserProfile',
        component: () => import('../views/game-project/user/profile.vue')
      },
      {
        path: ':gameProject/players',
        name: 'GameProjectPlayers',
        component: () => import('../views/players/index.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layout/admin.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/dashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/users.vue')
      },
      {
        path: 'teams',
        name: 'AdminTeams',
        component: () => import('../views/admin/teams.vue')
      },
      {
        path: 'news',
        name: 'AdminNews',
        component: () => import('../views/admin/news.vue')
      },
      {
        path: 'matches',
        name: 'AdminMatches',
        component: () => import('../views/admin/matches.vue')
      },
      {
        path: 'community',
        name: 'AdminCommunity',
        component: () => import('../views/admin/community.vue')
      },
      {
        path: 'notices',
        name: 'AdminNotices',
        component: () => import('../views/admin/notices.vue')
      },
      {
        path: 'messages',
        name: 'AdminMessages',
        component: () => import('../views/admin/messages.vue')
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('../views/admin/settings.vue')
      },
      {
        path: 'game-projects',
        name: 'AdminGameProjects',
        component: () => import('../views/admin/game-projects.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/login/register.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;