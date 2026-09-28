import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'
import { useWishlistStore } from './stores/wishlist'
import { useUiStore } from './stores/ui'

/** Login / logout side-effects shared by the header, login page and account pages. */
export function useSession() {
  const router = useRouter()
  const auth = useAuthStore()
  const cart = useCartStore()
  const wishlist = useWishlistStore()
  const ui = useUiStore()

  async function afterLogin(token, user) {
    auth.setSession(token, user)
    await Promise.all([cart.mergeGuestCart(), wishlist.load()])
  }

  function logout() {
    auth.clear()
    cart.reset()
    wishlist.reset()
    cart.load()
    ui.notify('You have been logged out')
    router.push('/')
  }

  return { afterLogin, logout }
}
