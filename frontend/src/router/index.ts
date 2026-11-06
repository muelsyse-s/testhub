import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '质量总览', icon: 'DataBoard' }
      },
      {
        path: 'projects',
        name: 'ProjectList',
        component: () => import('@/views/ProjectList.vue'),
        meta: { title: '项目管理', icon: 'Folder' }
      },
      {
        path: 'test-points',
        name: 'TestPointList',
        component: () => import('@/views/TestPointList.vue'),
        meta: { title: '测试点管理', icon: 'Aim' }
      },
      {
        path: 'test-cases',
        name: 'TestCaseList',
        component: () => import('@/views/TestCaseList.vue'),
        meta: { title: '测试用例', icon: 'Document' }
      },
      {
        path: 'test-plans',
        name: 'TestPlanList',
        component: () => import('@/views/TestPlanList.vue'),
        meta: { title: '测试计划', icon: 'Calendar' }
      },
      {
        path: 'test-plans/:id',
        name: 'PlanDetail',
        component: () => import('@/views/PlanDetail.vue'),
        meta: { title: '计划详情', hidden: true }
      },
      {
        path: 'executions',
        name: 'ExecutionList',
        component: () => import('@/views/ExecutionList.vue'),
        meta: { title: '执行记录', icon: 'CircleCheck' }
      },
      {
        path: 'defects',
        name: 'DefectList',
        component: () => import('@/views/DefectList.vue'),
        meta: { title: '缺陷管理', icon: 'Warning' }
      },
      {
        path: 'apis',
        name: 'ApiList',
        component: () => import('@/views/ApiList.vue'),
        meta: { title: '接口定义', icon: 'Connection' }
      },
      {
        path: 'api-scripts',
        name: 'ScriptList',
        component: () => import('@/views/ScriptList.vue'),
        meta: { title: '接口脚本', icon: 'Cpu' }
      },
      {
        path: 'environments',
        name: 'EnvList',
        component: () => import('@/views/EnvList.vue'),
        meta: { title: '环境管理', icon: 'SetUp' }
      },
      {
        path: 'reports',
        name: 'Report',
        component: () => import('@/views/Report.vue'),
        meta: { title: '统计报表', icon: 'TrendCharts' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/UserList.vue'),
        meta: { title: '用户权限', icon: 'User' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('testhub_token')
  if (to.meta.public) {
    next()
    return
  }
  if (!token) {
    next('/login')
    return
  }
  document.title = `${to.meta.title || ''} · TestHub`
  next()
})

export default router
