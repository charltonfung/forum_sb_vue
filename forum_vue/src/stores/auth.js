// Auth Pinia store
// ============================================================
// 狀態：token + user。token 存 localStorage，重新整理頁面也能還原。
// 動作：login / register / fetchMe / logout
import { defineStore } from 'pinia'
import { loginApi, registerApi } from '@/api/auth'
import { getMe } from '@/api/user'

const TOKEN_KEY = 'forum_token'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: null
  }),

  getters: {
    isLoggedIn: (s) => !!s.token,
    currentUserId: (s) => s.user?.id ?? null
  },

  actions: {
    async login(payload) {
      const data = await loginApi(payload)
      this.setToken(data.token)
      this.user = data.user
      return data
    },

    async register(payload) {
      const data = await registerApi(payload)
      this.setToken(data.token)
      this.user = data.user
      return data
    },

    async fetchMe() {
      this.user = await getMe()
      return this.user
    },

    setToken(token) {
      this.token = token
      localStorage.setItem(TOKEN_KEY, token)
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
    }
  }
})
