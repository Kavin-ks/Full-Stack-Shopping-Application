import { defineStore } from 'pinia'

let toastTimer = null

export const useUiStore = defineStore('ui', {
  state: () => ({
    toast: null, // { message, type }
    loginOpen: false,
    loginAfter: null // callback to run after a successful login from the modal
  }),
  actions: {
    notify(message, type = 'info') {
      this.toast = { message, type, id: Date.now() }
      clearTimeout(toastTimer)
      toastTimer = setTimeout(() => (this.toast = null), 3200)
    },
    error(err) {
      this.notify(typeof err === 'string' ? err : err?.message || 'Something went wrong', 'error')
    },
    openLogin(after = null) {
      this.loginAfter = after
      this.loginOpen = true
    },
    closeLogin() {
      this.loginOpen = false
      this.loginAfter = null
    }
  }
})
