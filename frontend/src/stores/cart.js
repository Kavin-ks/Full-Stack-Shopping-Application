import { defineStore } from 'pinia'
import { cartApi, productApi } from '../api'
import { useAuthStore } from './auth'
import { useUiStore } from './ui'
import { load, save, remove } from '../utils/storage'
import { summarize } from '../utils/format'

const GUEST_KEY = 'fk_guest_cart'
const MAX_QTY = 10
const emptySummary = () => summarize([])

/**
 * Logged-in users: the cart lives in cart-service.
 * Guests: kept in localStorage (like Flipkart) and merged into the server cart on login.
 */
export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    savedForLater: [],
    summary: emptySummary(),
    loading: false,
    loaded: false
  }),
  getters: {
    count: (s) => s.items.length,
    has: (s) => (id) => s.items.some((l) => l.productId === id),
    hasUnavailable: (s) => s.items.some((l) => !l.inStock)
  },
  actions: {
    apply(cart) {
      this.items = cart.items
      this.savedForLater = cart.savedForLater
      this.summary = cart.summary
      this.loaded = true
    },

    async load() {
      this.loading = true
      try {
        if (useAuthStore().isLoggedIn) this.apply(await cartApi.get())
        else this.apply(await buildGuestCart())
      } catch (e) {
        useUiStore().error(e)
      } finally {
        this.loading = false
      }
    },

    async add(product, quantity = 1, { silent = false } = {}) {
      const ui = useUiStore()
      try {
        if (useAuthStore().isLoggedIn) {
          this.apply(await cartApi.add(product.id, quantity))
        } else {
          if (product.stock <= 0) throw new Error(`${product.title} is out of stock`)
          const guest = load(GUEST_KEY, [])
          const line = guest.find((l) => l.productId === product.id)
          const max = Math.min(product.stock, MAX_QTY)
          if (line) {
            line.quantity = line.saved ? quantity : Math.min(line.quantity + quantity, max)
            line.saved = false
          } else guest.unshift({ productId: product.id, quantity: Math.min(quantity, max), saved: false })
          save(GUEST_KEY, guest)
          this.apply(await buildGuestCart())
        }
        if (!silent) ui.notify(`${product.title} added to your cart`)
        return true
      } catch (e) {
        ui.error(e)
        return false
      }
    },

    async updateQuantity(productId, quantity) {
      await this.mutate(
        () => cartApi.update(productId, quantity),
        (guest) => {
          const l = guest.find((x) => x.productId === productId)
          if (l) l.quantity = quantity
        }
      )
    },

    async remove(productId, title) {
      const ok = await this.mutate(
        () => cartApi.remove(productId),
        (guest) => guest.splice(guest.findIndex((x) => x.productId === productId), 1)
      )
      if (ok) useUiStore().notify(`Successfully removed ${title || 'item'} from your cart`)
    },

    async saveForLater(productId, title) {
      const ok = await this.mutate(
        () => cartApi.saveForLater(productId),
        (guest) => {
          const l = guest.find((x) => x.productId === productId)
          if (l) l.saved = true
        }
      )
      if (ok) useUiStore().notify(`You have saved ${title || 'item'} for later`)
    },

    async moveToCart(productId, title) {
      const ok = await this.mutate(
        () => cartApi.moveToCart(productId),
        (guest) => {
          const l = guest.find((x) => x.productId === productId)
          if (l) l.saved = false
        }
      )
      if (ok) useUiStore().notify(`${title || 'Item'} moved to your cart`)
    },

    async mutate(serverCall, guestChange) {
      try {
        if (useAuthStore().isLoggedIn) {
          this.apply(await serverCall())
        } else {
          const guest = load(GUEST_KEY, [])
          guestChange(guest)
          save(GUEST_KEY, guest)
          this.apply(await buildGuestCart())
        }
        return true
      } catch (e) {
        useUiStore().error(e)
        return false
      }
    },

    /** Called right after login: move the guest cart into the user's server cart. */
    async mergeGuestCart() {
      const guest = load(GUEST_KEY, [])
      for (const line of guest) {
        try {
          await cartApi.add(line.productId, line.quantity)
          if (line.saved) await cartApi.saveForLater(line.productId)
        } catch {
          /* skip items that went out of stock */
        }
      }
      remove(GUEST_KEY)
      await this.load()
    },

    reset() {
      this.items = []
      this.savedForLater = []
      this.summary = emptySummary()
      this.loaded = false
    }
  }
})

async function buildGuestCart() {
  const guest = load(GUEST_KEY, [])
  if (!guest.length) return { items: [], savedForLater: [], summary: emptySummary() }
  const products = await productApi.batch(guest.map((l) => l.productId))
  const byId = Object.fromEntries(products.map((p) => [p.id, p]))
  const items = []
  const savedForLater = []
  guest.forEach((l) => {
    const p = byId[l.productId]
    if (!p) return
    const max = Math.max(Math.min(p.stock, MAX_QTY), 1)
    const line = { productId: p.id, quantity: Math.min(l.quantity, max), maxQuantity: max, inStock: p.stock > 0, product: p }
    ;(l.saved ? savedForLater : items).push(line)
  })
  return { items, savedForLater, summary: summarize(items) }
}
