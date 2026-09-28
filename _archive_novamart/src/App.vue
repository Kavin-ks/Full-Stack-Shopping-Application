<template>
  <div class="app-root">
    <!-- Initial Page: Full Authentication Portal (When not logged in) -->
    <AuthView 
      v-if="!currentUser" 
      @auth-success="handleAuthSuccess" 
    />

    <!-- Main Shopping Store Application (When authenticated) -->
    <div v-else class="authenticated-store">
      <!-- Amazon x Instamart Header -->
      <Navbar 
        :cart-count="cart.totalQuantity" 
        :cart-total="cart.finalTotal"
        :order-count="orders.length"
        :current-tab="activeTab"
        :current-user="currentUser"
        :active-category="activeCategory"
        v-model:search-query="searchQuery"
        @change-tab="tab => activeTab = tab"
        @select-category="cat => { activeCategory = cat; activeTab = 'catalog'; }"
        @logout="handleLogout"
      />

      <!-- Main Content Area -->
      <main class="main-content">
        <!-- Toast Notification Alert -->
        <transition name="toast-slide">
          <div v-if="toastMessage" class="toast-alert" :class="toastType">
            <span class="toast-icon">{{ toastIcon }}</span>
            <span>{{ toastMessage }}</span>
          </div>
        </transition>

        <!-- Dynamic Views -->
        <transition name="view-fade" mode="out-in">
          <!-- Catalog View -->
          <div v-if="activeTab === 'catalog'" key="catalog">
            <ProductCatalog 
              :products="products"
              :search-query="searchQuery"
              :cart-items="cart.items"
              :selected-category-prop="activeCategory"
              @add-to-cart="handleAddToCart"
              @update-cart-qty="handleUpdateQty"
              @remove-cart-item="handleRemoveItem"
              @clear-search="searchQuery = ''"
              @category-change="cat => activeCategory = cat"
            />
          </div>

          <!-- Cart & Summary View -->
          <div v-else-if="activeTab === 'cart'" key="cart">
            <CartView 
              :cart="cart"
              :current-user="currentUser"
              @update-qty="handleUpdateQty"
              @remove-item="handleRemoveItem"
              @clear-cart="handleClearCart"
              @apply-coupon="handleApplyCoupon"
              @remove-coupon="handleRemoveCoupon"
              @open-checkout="isCheckoutOpen = true"
              @continue-shopping="activeTab = 'catalog'"
            />
          </div>

          <!-- Orders History View -->
          <div v-else-if="activeTab === 'orders'" key="orders">
            <OrdersView 
              :orders="orders"
              :current-user="currentUser"
              @continue-shopping="activeTab = 'catalog'"
              @reorder="handleReorder"
            />
          </div>

          <!-- User Profile & Credentials View -->
          <div v-else-if="activeTab === 'profile'" key="profile">
            <ProfileView 
              :current-user="currentUser"
              :order-count="orders.length"
              :cart-count="cart.totalQuantity"
              :orders="orders"
              @user-updated="handleUserUpdated"
              @logout="handleLogout"
            />
          </div>
        </transition>
      </main>

      <!-- Checkout Modal -->
      <CheckoutModal 
        v-if="isCheckoutOpen"
        :cart="cart"
        :current-user="currentUser"
        @close="isCheckoutOpen = false"
        @order-placed="handleOrderPlaced"
        @view-orders="switchToOrders"
      />

      <!-- Sticky Bottom Navigation for Mobile -->
      <nav class="mobile-bottom-bar">
        <button class="mob-nav-btn" :class="{ active: activeTab === 'catalog' }" @click="activeTab = 'catalog'">
          <span>🏪</span>
          <small>Shop</small>
        </button>
        <button class="mob-nav-btn" :class="{ active: activeTab === 'cart' }" @click="activeTab = 'cart'">
          <div class="mob-icon-wrap">
            <span>🛒</span>
            <span v-if="cart.totalQuantity > 0" class="mob-badge">{{ cart.totalQuantity }}</span>
          </div>
          <small>Cart</small>
        </button>
        <button class="mob-nav-btn" :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'">
          <span>📦</span>
          <small>Orders</small>
        </button>
        <button class="mob-nav-btn" :class="{ active: activeTab === 'profile' }" @click="activeTab = 'profile'">
          <span>👤</span>
          <small>Profile</small>
        </button>
      </nav>
    </div>
  </div>
</template>

<script>
import AuthView from './components/AuthView.vue'
import Navbar from './components/Navbar.vue'
import ProductCatalog from './components/ProductCatalog.vue'
import CartView from './components/CartView.vue'
import OrdersView from './components/OrdersView.vue'
import ProfileView from './components/ProfileView.vue'
import CheckoutModal from './components/CheckoutModal.vue'
import { api } from './services/api'

export default {
  name: 'App',
  components: {
    AuthView,
    Navbar,
    ProductCatalog,
    CartView,
    OrdersView,
    ProfileView,
    CheckoutModal
  },
  data() {
    return {
      currentUser: null,
      activeTab: 'catalog',
      activeCategory: 'All',
      searchQuery: '',
      products: [],
      cart: {
        items: [],
        totalQuantity: 0,
        subTotal: 0,
        discountAmount: 0,
        deliveryFee: 0,
        packagingFee: 0,
        finalTotal: 0,
        appliedCoupon: ''
      },
      orders: [],
      isCheckoutOpen: false,
      toastMessage: '',
      toastType: 'info',
      toastIcon: 'ℹ️'
    }
  },
  async created() {
    // Check if user session already exists in persistent localStorage
    const savedUser = api.getActiveSession()
    if (savedUser) {
      this.currentUser = savedUser
      await this.loadUserData(savedUser.id)
    }
  },
  methods: {
    showToast(msg, type = 'info', icon = '✓') {
      this.toastMessage = msg
      this.toastType = type
      this.toastIcon = icon
      setTimeout(() => {
        this.toastMessage = ''
      }, 3200)
    },
    async handleAuthSuccess(user) {
      this.currentUser = user
      await this.loadUserData(user.id)
      this.activeTab = 'catalog'
      this.showToast(`Welcome back, ${user.name}! Your cart & orders are loaded.`, 'success', '👋')
    },
    async loadUserData(userId) {
      try {
        this.products = await api.getProducts()
        this.cart = await api.getUserCart(userId)
        this.orders = await api.getUserOrders(userId)
      } catch (err) {
        console.error('Data load error:', err)
      }
    },
    handleLogout() {
      api.clearActiveSession()
      this.currentUser = null
      this.activeTab = 'catalog'
      this.cart = {
        items: [],
        totalQuantity: 0,
        subTotal: 0,
        discountAmount: 0,
        deliveryFee: 0,
        packagingFee: 0,
        finalTotal: 0,
        appliedCoupon: ''
      }
      this.orders = []
      this.showToast('You have been logged out. Re-login anytime to restore your data.', 'info', '🔒')
    },
    handleUserUpdated(updatedUser) {
      this.currentUser = updatedUser
      this.showToast('Delivery address updated successfully.', 'success', '📍')
    },
    async handleAddToCart(product, quantity = 1) {
      if (!this.currentUser) return
      this.cart = await api.addToCart(this.currentUser.id, product.id, quantity, product)
      this.showToast(`Added ${quantity}x "${product.name}" to cart!`, 'success', '🛒')
    },
    async handleUpdateQty(itemId, quantity) {
      if (!this.currentUser) return
      this.cart = await api.updateQuantity(this.currentUser.id, itemId, quantity)
    },
    async handleRemoveItem(itemId) {
      if (!this.currentUser) return
      this.cart = await api.removeFromCart(this.currentUser.id, itemId)
      this.showToast('Item removed from cart.', 'info', '🗑️')
    },
    async handleClearCart() {
      if (!this.currentUser) return
      this.cart = await api.clearCart(this.currentUser.id)
      this.showToast('Your cart has been emptied.', 'info', '🧹')
    },
    async handleApplyCoupon(code) {
      if (!this.currentUser) return
      this.cart = await api.applyCoupon(this.currentUser.id, code)
      if (this.cart.discountAmount > 0) {
        this.showToast(`Coupon "${code}" applied! You saved ₹${this.cart.discountAmount}.`, 'success', '🎉')
      } else {
        this.showToast(`Coupon "${code}" is invalid or minimum cart value not met. Try INSTANT10.`, 'error', '⚠️')
      }
    },
    async handleRemoveCoupon() {
      if (!this.currentUser) return
      this.cart = await api.removeCoupon(this.currentUser.id)
      this.showToast('Coupon removed.', 'info', '🏷️')
    },
    handleOrderPlaced(newOrder, updatedCart) {
      this.orders.unshift(newOrder)
      this.cart = updatedCart
      this.showToast(`Order #${newOrder.orderId} placed successfully!`, 'success', '🎉')
    },
    switchToOrders() {
      this.isCheckoutOpen = false
      this.activeTab = 'orders'
    },
    async handleReorder(order) {
      if (!this.currentUser || !order.items) return
      for (const item of order.items) {
        await api.addToCart(this.currentUser.id, item.productId, item.quantity, item)
      }
      this.cart = await api.getUserCart(this.currentUser.id)
      this.activeTab = 'cart'
      this.showToast('Previous order items re-added to your cart!', 'success', '🔄')
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Plus Jakarta Sans', 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  background-color: #f1f5f9;
  color: #0f172a;
  -webkit-font-smoothing: antialiased;
}

.app-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.authenticated-store {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex-grow: 1;
  max-width: 1360px;
  width: 100%;
  margin: 20px auto 40px auto;
  padding: 0 16px;
}

/* Toast Notifications */
.toast-alert {
  position: fixed;
  bottom: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  z-index: 2000;
  color: #ffffff;
}

.toast-alert.info {
  background-color: #0f172a;
  border-left: 4px solid #38bdf8;
}

.toast-alert.success {
  background-color: #15803d;
  border-left: 4px solid #86efac;
}

.toast-alert.error {
  background-color: #b91c1c;
  border-left: 4px solid #fca5a5;
}

.toast-icon {
  font-size: 16px;
}

.toast-slide-enter-active, .toast-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.toast-slide-enter-from, .toast-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* View transitions */
.view-fade-enter-active, .view-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.view-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.view-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* Mobile Bottom Navigation */
.mobile-bottom-bar {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #ffffff;
  border-top: 1px solid #e2e8f0;
  z-index: 150;
  justify-content: space-around;
  align-items: center;
}

.mob-nav-btn {
  background: transparent;
  border: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: #64748b;
  cursor: pointer;
  font-size: 18px;
}

.mob-nav-btn small {
  font-size: 10px;
  font-weight: 700;
}

.mob-nav-btn.active {
  color: #ff5200;
}

.mob-icon-wrap {
  position: relative;
}

.mob-badge {
  position: absolute;
  top: -4px;
  right: -8px;
  background: #ff5200;
  color: #ffffff;
  font-size: 9px;
  font-weight: 800;
  border-radius: 10px;
  padding: 1px 4px;
}

@media (max-width: 768px) {
  .mobile-bottom-bar {
    display: flex;
  }
  .main-content {
    margin-bottom: 70px;
  }
}
</style>
