import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'

const routes = [
  { path: '/', name: 'home', component: () => import('./views/HomeView.vue'), meta: { categoryTiles: true } },
  { path: '/products', name: 'products', component: () => import('./views/ProductListView.vue') },
  { path: '/product/:id', name: 'product', component: () => import('./views/ProductView.vue') },
  { path: '/cart', name: 'cart', component: () => import('./views/CartView.vue'), meta: { noCategoryBar: true } },
  { path: '/login', name: 'login', component: () => import('./views/LoginView.vue'), meta: { noCategoryBar: true } },
  {
    path: '/checkout',
    name: 'checkout',
    component: () => import('./views/CheckoutView.vue'),
    meta: { requiresAuth: true, noCategoryBar: true }
  },
  {
    path: '/order-success/:id',
    name: 'order-success',
    component: () => import('./views/OrderSuccessView.vue'),
    meta: { requiresAuth: true, noCategoryBar: true }
  },
  {
    path: '/account',
    component: () => import('./views/account/AccountLayout.vue'),
    meta: { requiresAuth: true, noCategoryBar: true },
    children: [
      { path: '', name: 'profile', component: () => import('./views/account/ProfileView.vue') },
      { path: 'addresses', name: 'addresses', component: () => import('./views/account/AddressesView.vue') },
      { path: 'wishlist', name: 'wishlist', component: () => import('./views/account/WishlistView.vue') }
    ]
  },
  {
    path: '/account/orders',
    name: 'orders',
    component: () => import('./views/account/OrdersView.vue'),
    meta: { requiresAuth: true, noCategoryBar: true }
  },
  {
    path: '/account/orders/:id',
    name: 'order',
    component: () => import('./views/account/OrderDetailView.vue'),
    meta: { requiresAuth: true, noCategoryBar: true }
  },
  {
    path: '/seller',
    name: 'seller',
    component: () => import('./views/seller/SellerHub.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, noCategoryBar: true }
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('./views/NotFoundView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, saved) {
    if (saved) return saved
    if (to.name === from.name && to.name === 'products') return false // keep position while filtering
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: 'home' }
  }
})

const TITLES = {
  cart: 'Shopping Cart | Flipkart',
  checkout: 'Secure Payment | Flipkart',
  login: 'Login | Flipkart',
  orders: 'My Orders | Flipkart',
  seller: 'Seller Hub | Flipkart'
}
const DEFAULT_TITLE = 'Online Shopping Site for Mobiles, Electronics, Furniture, Grocery, Lifestyle, Books & More. Best Offers!'

// The product page sets its own title once the product loads.
router.afterEach((to) => {
  if (to.name !== 'product') document.title = TITLES[to.name] || DEFAULT_TITLE
})

export default router
