// Vue Router 設定
// ============================================================
// meta.requiresAuth = true 表示這個路由需登入，
// router.beforeEach 全域守衛會檢查並導去 /login（保留 redirect query）。
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // ---------- Auth ----------
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { guestOnly: true } },
  { path: '/register', name: 'register', component: () => import('@/views/Register.vue'), meta: { guestOnly: true } },
  { path: '/forgot-password', name: 'forgot-password', component: () => import('@/views/ForgotPassword.vue'), meta: { guestOnly: true } },
  { path: '/reset-password', name: 'reset-password', component: () => import('@/views/ResetPassword.vue'), meta: { guestOnly: true } },

  // ---------- 文章（公開的列表 / 單篇 + 需登入的建立 / 編輯） ----------
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'article-list', component: () => import('@/views/ArticleList.vue') },
      { path: 'articles/create', name: 'article-create', component: () => import('@/views/ArticleCreate.vue'), meta: { requiresAuth: true } },
      { path: 'articles/:id', name: 'article-show', component: () => import('@/views/ArticleShow.vue'), props: true },
      { path: 'articles/:id/edit', name: 'article-edit', component: () => import('@/views/ArticleEdit.vue'), props: true, meta: { requiresAuth: true } },
      { path: 'profile', name: 'profile', component: () => import('@/views/Profile.vue'), meta: { requiresAuth: true } }
    ]
  },

  // 404 兜底
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全域守衛
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isLoggedIn) {
    return { name: 'article-list' }
  }
})

export default router
