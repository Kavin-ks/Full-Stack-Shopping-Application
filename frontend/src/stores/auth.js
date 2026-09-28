import { defineStore } from 'pinia'
import { load, save, remove } from '../utils/storage'
import { userApi } from '../api'
import { getToken } from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    user: load('fk_user', null)
  }),
  getters: {
    isLoggedIn: (s) => !!s.token && !!s.user,
    isAdmin: (s) => s.user?.role === 'ADMIN',
    firstName: (s) => (s.user?.name || '').split(' ')[0] || 'Account',
    contact: (s) => s.user?.mobile || s.user?.email || ''
  },
  actions: {
    setSession(token, user) {
      this.token = token
      this.user = user
      try {
        localStorage.setItem('fk_token', token)
      } catch {
        /* ignore */
      }
      save('fk_user', user)
    },
    setUser(user) {
      this.user = user
      save('fk_user', user)
    },
    async refresh() {
      if (!this.token) return
      try {
        this.setUser(await userApi.me())
      } catch {
        /* 401 is handled globally */
      }
    },
    clear() {
      this.token = null
      this.user = null
      remove('fk_token')
      remove('fk_user')
    }
  }
})
