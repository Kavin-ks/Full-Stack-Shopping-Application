import { defineStore } from 'pinia'
import { userApi } from '../api'
import { useAuthStore } from './auth'
import { useUiStore } from './ui'

export const useWishlistStore = defineStore('wishlist', {
  state: () => ({ ids: [] }),
  getters: {
    has: (s) => (id) => s.ids.includes(id),
    count: (s) => s.ids.length
  },
  actions: {
    async load() {
      if (!useAuthStore().isLoggedIn) {
        this.ids = []
        return
      }
      try {
        this.ids = await userApi.wishlist()
      } catch {
        this.ids = []
      }
    },
    async toggle(productId) {
      const auth = useAuthStore()
      const ui = useUiStore()
      if (!auth.isLoggedIn) {
        ui.openLogin(() => this.toggle(productId))
        return
      }
      try {
        if (this.has(productId)) {
          this.ids = await userApi.removeFromWishlist(productId)
          ui.notify('Removed from your Wishlist')
        } else {
          this.ids = await userApi.addToWishlist(productId)
          ui.notify('Added to your Wishlist')
        }
      } catch (e) {
        ui.error(e)
      }
    },
    reset() {
      this.ids = []
    }
  }
})
