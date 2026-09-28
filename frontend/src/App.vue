<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import CategoryBar from './components/CategoryBar.vue'
import AppFooter from './components/AppFooter.vue'
import LoginModal from './components/LoginModal.vue'
import AppToast from './components/AppToast.vue'
import { setUnauthorizedHandler } from './api/http'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'
import { useWishlistStore } from './stores/wishlist'
import { useCatalogStore } from './stores/catalog'
import { useUiStore } from './stores/ui'

const route = useRoute()
const auth = useAuthStore()
const cart = useCartStore()
const wishlist = useWishlistStore()
const catalog = useCatalogStore()
const ui = useUiStore()

// An expired / invalid token anywhere logs the user out and asks them to sign in again.
setUnauthorizedHandler(() => {
  if (!auth.isLoggedIn) return
  auth.clear()
  cart.reset()
  wishlist.reset()
  cart.load()
  ui.notify('Your session has expired. Please login again', 'error')
  ui.openLogin()
})

onMounted(() => {
  catalog.load()
  cart.load()
  if (auth.isLoggedIn) {
    auth.refresh()
    wishlist.load()
  }
})
</script>

<template>
  <AppHeader />
  <CategoryBar v-if="!route.meta.noCategoryBar && !route.meta.categoryTiles" />
  <main>
    <RouterView v-slot="{ Component }">
      <component :is="Component" :key="route.name === 'product' ? route.fullPath : route.name" />
    </RouterView>
  </main>
  <AppFooter />
  <LoginModal />
  <AppToast />
</template>
