import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '../store/user';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '用户登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '用户注册', public: true }
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/workbench',
    children: [
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('../views/Workbench.vue'),
        meta: { title: 'CRM 工作台' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('../views/Order.vue'),
        meta: { title: '订单综合管理' }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('../views/Product.vue'),
        meta: { title: '商品中心与发布' }
      },
      {
        path: 'product/audit',
        name: 'ProductAudit',
        component: () => import('../views/ProductAudit.vue'),
        meta: { title: '商品发布审核' }
      },
      {
        path: 'logistics',
        name: 'Logistics',
        component: () => import('../views/Logistics.vue'),
        meta: { title: '物流发货与追踪' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/User.vue'),
        meta: { title: '系统用户管理' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('../views/Role.vue'),
        meta: { title: '角色权限分配' }
      },
      {
        path: 'mall',
        name: 'Mall',
        component: () => import('../views/Mall.vue'),
        meta: { title: '商城展示前台' }
      },
      {
        path: 'region',
        name: 'Region',
        component: () => import('../views/Region.vue'),
        meta: { title: '行政地区字典' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局路由导航守卫: 无 token 访问受保护页面重定向至 /login
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const isPublic = to.meta.public === true;

  if (!userStore.token && !isPublic) {
    next({ path: '/login', query: { redirect: to.fullPath } });
  } else {
    // 已经登录的情况下访问 /login 或 /register，直接跳到首页工作台
    if (userStore.token && (to.path === '/login' || to.path === '/register')) {
      next({ path: '/' });
    } else {
      next();
    }
  }
});

export default router;
