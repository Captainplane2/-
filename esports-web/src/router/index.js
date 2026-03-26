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
        path: 'match',
        name: 'Match',
        component: () => import('../views/match/list.vue')
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('../views/community/index.vue')
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('../views/notice/index.vue')
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/user/profile.vue')
      },
      {
        path: 'team/manage',
        name: 'TeamManage',
        component: () => import('../views/team/management.vue')
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
