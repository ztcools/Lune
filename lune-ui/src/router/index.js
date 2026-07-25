import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('../views/landing/Landing.vue')
  },
  {
    path: '/',
    component: () => import('../layout/PublicLayout.vue'),
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/home/Home.vue') },
      { path: 'article/:id', name: 'ArticleDetail', component: () => import('../views/article/ArticleDetail.vue') },
      { path: 'family', name: 'Family', component: () => import('../views/family/Family.vue') },
      { path: 'treehole', name: 'TreeHole', component: () => import('../views/treehole/TreeHole.vue') },
      { path: 'essay', name: 'Essay', component: () => import('../views/essay/Essay.vue') },
      { path: 'record', name: 'Record', component: () => import('../views/record/Record.vue') }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../admin/Login.vue')
  },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('../admin/Dashboard.vue') },
      { path: 'articles', name: 'AdminArticles', component: () => import('../admin/ArticleManage.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('../admin/CategoryManage.vue') },
      { path: 'records', name: 'AdminRecords', component: () => import('../admin/RecordManage.vue') },
      { path: 'essays', name: 'AdminEssays', component: () => import('../admin/EssayManage.vue') },
      { path: 'comments', name: 'AdminComments', component: () => import('../admin/CommentManage.vue') },
      { path: 'treeholes', name: 'AdminTreeHoles', component: () => import('../admin/TreeHoleManage.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../admin/UserManage.vue') },
      { path: 'resources', name: 'AdminResources', component: () => import('../admin/ResourceManage.vue') },
      { path: 'family', name: 'AdminFamily', component: () => import('../admin/FamilyManage.vue') },
      { path: 'diaries', name: 'AdminDiaries', component: () => import('../admin/DiaryManage.vue') },
      { path: 'settings', name: 'AdminSettings', component: () => import('../admin/Settings.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    const user = JSON.parse(localStorage.getItem('user') || 'null')
    if (!token) {
      return next('/admin/login')
    }
    if (to.meta.requiresAdmin && user?.role !== 'ADMIN') {
      return next('/')
    }
  }
  next()
})

export default router
