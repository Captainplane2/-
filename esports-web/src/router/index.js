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
      // CS2 板块路由
      {
        path: 'cs2',
        name: 'CS2Home',
        component: () => import('../views/cs2/index.vue')
      },
      {
        path: 'cs2/team',
        name: 'CS2Team',
        component: () => import('../views/cs2/team/list.vue')
      },
      {
        path: 'cs2/team/:id',
        name: 'CS2TeamDetail',
        component: () => import('../views/cs2/team/detail.vue')
      },
      {
        path: 'cs2/team/manage',
        name: 'CS2TeamManage',
        component: () => import('../views/cs2/team/management.vue')
      },
      {
        path: 'cs2/match',
        name: 'CS2Match',
        component: () => import('../views/cs2/match/list.vue')
      },
      {
        path: 'cs2/match/:id',
        name: 'CS2MatchDetail',
        component: () => import('../views/cs2/match/detail.vue')
      },
      {
        path: 'cs2/match/manage',
        name: 'CS2MatchManage',
        component: () => import('../views/cs2/match/manage.vue')
      },
      // LOL 板块路由
      {
        path: 'lol',
        name: 'LOLHome',
        component: () => import('../views/lol/index.vue')
      },
      // WZRY 板块路由
      {
        path: 'wzry',
        name: 'WZRYHome',
        component: () => import('../views/wzry/index.vue')
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
