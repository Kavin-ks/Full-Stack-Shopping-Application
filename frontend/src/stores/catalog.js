import { defineStore } from 'pinia'
import { productApi } from '../api'

export const useCatalogStore = defineStore('catalog', {
  state: () => ({ categories: [], loaded: false }),
  getters: {
    byslug: (s) => (slug) => s.categories.find((c) => c.slug === slug)
  },
  actions: {
    async load() {
      if (this.loaded) return
      try {
        this.categories = await productApi.categories()
        this.loaded = true
      } catch {
        /* header still renders without categories */
      }
    }
  }
})
