<template>
  <header class="navbar-wrapper">
    <!-- Top Super Bar -->
    <div class="top-nav">
      <div class="nav-container">
        <!-- Logo -->
        <div class="brand" @click="$emit('change-tab', 'catalog')">
          <div class="logo-box">
            <span class="brand-emoji">🛒</span>
            <div class="brand-text">
              <span class="brand-title">NovaMart</span>
              <span class="brand-subtitle">Amazon × Instamart</span>
            </div>
          </div>
        </div>

        <!-- Delivery Location Indicator -->
        <div class="location-pill" @click="$emit('change-tab', 'profile')" title="Click to view/edit address">
          <span class="pin-icon">📍</span>
          <div class="loc-text">
            <span class="loc-sub">Delivering to <strong>{{ currentUser ? currentUser.name.split(' ')[0] : 'Guest' }}</strong></span>
            <span class="loc-addr">{{ shortAddress }}</span>
          </div>
        </div>

        <!-- Instamart 10-Min Delivery Pill -->
        <div class="delivery-speed-badge">
          <span class="bolt">⚡</span>
          <span>10 MINS DELIVERY</span>
        </div>

        <!-- Search Bar -->
        <div class="search-section">
          <div class="search-category-select">
            <select v-model="selectedCategory">
              <option value="All">All Categories</option>
              <option value="Fresh Produce">🥦 Fresh Produce</option>
              <option value="Dairy & Breakfast">🥛 Dairy & Eggs</option>
              <option value="Snacks & Drinks">🍪 Snacks & Drinks</option>
              <option value="Electronics">📱 Electronics & Mobiles</option>
              <option value="Fashion">👟 Fashion & Shoes</option>
              <option value="Home & Kitchen">🍳 Home & Kitchen</option>
            </select>
          </div>
          <input 
            type="text" 
            :value="searchQuery" 
            @input="$emit('update:searchQuery', $event.target.value)" 
            placeholder="Search for mangoes, milk, laptops, shoes, chips..." 
            class="search-input"
          />
          <button v-if="searchQuery" @click="$emit('update:searchQuery', '')" class="clear-search-btn" title="Clear">✕</button>
          <button class="search-submit-btn" title="Search">
            🔍
          </button>
        </div>

        <!-- User Actions -->
        <div class="nav-right-actions">
          <!-- User Profile & Account -->
          <div class="user-menu" :class="{ active: currentTab === 'profile' }" @click="$emit('change-tab', 'profile')">
            <span class="user-greeting">Hello, {{ currentUser ? currentUser.name.split(' ')[0] : 'Sign In' }}</span>
            <span class="menu-bold">Account & Info</span>
          </div>

          <!-- Returns & Orders -->
          <div class="orders-menu" :class="{ active: currentTab === 'orders' }" @click="$emit('change-tab', 'orders')">
            <span class="orders-sub">Returns</span>
            <span class="menu-bold">& Orders ({{ orderCount }})</span>
          </div>

          <!-- Cart Button -->
          <button class="cart-button" :class="{ active: currentTab === 'cart' }" @click="$emit('change-tab', 'cart')">
            <div class="cart-icon-wrapper">
              <span class="cart-icon">🛒</span>
              <span class="cart-badge">{{ cartCount }}</span>
            </div>
            <div class="cart-details">
              <span class="cart-label">Cart</span>
              <span class="cart-amount">₹{{ Number(cartTotal).toLocaleString('en-IN') }}</span>
            </div>
          </button>

          <!-- Logout Button -->
          <button class="logout-btn" @click="$emit('logout')" title="Log out or switch user">
            🚪 Exit
          </button>
        </div>
      </div>
    </div>

    <!-- Secondary Category Subnav Bar -->
    <nav class="sub-nav">
      <div class="sub-nav-container">
        <button 
          class="sub-item all-btn" 
          :class="{ active: currentTab === 'catalog' && (!activeCategory || activeCategory === 'All') }"
          @click="selectCat('All')"
        >
          ☰ All Products
        </button>
        <button 
          class="sub-item" 
          :class="{ active: activeCategory === 'Fresh Produce' && currentTab === 'catalog' }" 
          @click="selectCat('Fresh Produce')"
        >
          🥭 Fresh Fruits & Veggies
        </button>
        <button 
          class="sub-item" 
          :class="{ active: activeCategory === 'Dairy & Breakfast' && currentTab === 'catalog' }" 
          @click="selectCat('Dairy & Breakfast')"
        >
          🥛 Dairy & Breakfast
        </button>
        <button 
          class="sub-item" 
          :class="{ active: activeCategory === 'Snacks & Drinks' && currentTab === 'catalog' }" 
          @click="selectCat('Snacks & Drinks')"
        >
          🍫 Snacks & Cold Drinks
        </button>
        <button 
          class="sub-item" 
          :class="{ active: activeCategory === 'Electronics' && currentTab === 'catalog' }" 
          @click="selectCat('Electronics')"
        >
          💻 Electronics & Mobiles
        </button>
        <button 
          class="sub-item" 
          :class="{ active: activeCategory === 'Fashion' && currentTab === 'catalog' }" 
          @click="selectCat('Fashion')"
        >
          👟 Fashion & Lifestyle
        </button>
        <button 
          class="sub-item" 
          :class="{ active: activeCategory === 'Home & Kitchen' && currentTab === 'catalog' }" 
          @click="selectCat('Home & Kitchen')"
        >
          🍳 Home & Kitchen
        </button>

        <div class="persistent-status-chip">
          <span class="pulse-dot"></span>
          <span>Synced to Local DB</span>
        </div>
      </div>
    </nav>
  </header>
</template>

<script>
export default {
  name: 'Navbar',
  props: {
    cartCount: {
      type: Number,
      default: 0
    },
    cartTotal: {
      type: Number,
      default: 0
    },
    orderCount: {
      type: Number,
      default: 0
    },
    currentTab: {
      type: String,
      default: 'catalog'
    },
    searchQuery: {
      type: String,
      default: ''
    },
    currentUser: {
      type: Object,
      default: null
    },
    activeCategory: {
      type: String,
      default: 'All'
    }
  },
  emits: ['change-tab', 'update:searchQuery', 'select-category', 'logout'],
  data() {
    return {
      selectedCategory: 'All'
    }
  },
  computed: {
    shortAddress() {
      if (!this.currentUser || !this.currentUser.address) return 'Select Location'
      const parts = this.currentUser.address.split(',')
      return parts.length > 1 ? parts[parts.length - 2].trim() : parts[0].trim()
    }
  },
  watch: {
    selectedCategory(newCat) {
      this.selectCat(newCat)
    }
  },
  methods: {
    selectCat(cat) {
      this.$emit('select-category', cat)
      this.$emit('change-tab', 'catalog')
    }
  }
}
</script>

<style scoped>
.navbar-wrapper {
  position: sticky;
  top: 0;
  z-index: 200;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.top-nav {
  background-color: #131921;
  color: #ffffff;
  padding: 8px 16px;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid transparent;
  transition: border-color 0.2s;
  flex-shrink: 0;
}

.brand:hover {
  border-color: #ffffff;
}

.logo-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-emoji {
  font-size: 26px;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.3px;
  color: #ffffff;
  line-height: 1.1;
}

.brand-subtitle {
  font-size: 10px;
  font-weight: 700;
  color: #ff9900;
  letter-spacing: 0.5px;
}

.location-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid transparent;
  transition: border-color 0.2s;
  flex-shrink: 0;
}

.location-pill:hover {
  border-color: #ffffff;
}

.pin-icon {
  font-size: 18px;
}

.loc-text {
  display: flex;
  flex-direction: column;
}

.loc-sub {
  font-size: 11px;
  color: #cccccc;
}

.loc-addr {
  font-size: 12.5px;
  font-weight: 700;
  color: #ffffff;
  max-width: 130px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.delivery-speed-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #ff5200;
  color: #ffffff;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.4px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(255, 82, 0, 0.4);
}

.bolt {
  font-size: 14px;
}

.search-section {
  display: flex;
  flex-grow: 1;
  border-radius: 6px;
  overflow: hidden;
  background: #ffffff;
  height: 40px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.search-category-select {
  background: #e6e6e6;
  border-right: 1px solid #d1d5db;
  display: flex;
  align-items: center;
}

.search-category-select select {
  border: none;
  background: transparent;
  padding: 0 8px;
  font-size: 12px;
  color: #374151;
  font-weight: 500;
  outline: none;
  cursor: pointer;
  height: 100%;
}

.search-input {
  flex-grow: 1;
  border: none;
  padding: 0 14px;
  font-size: 14px;
  outline: none;
  color: #111827;
}

.clear-search-btn {
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 14px;
  cursor: pointer;
  padding: 0 8px;
}

.search-submit-btn {
  background-color: #febd69;
  border: none;
  width: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-submit-btn:hover {
  background-color: #f3a847;
}

.nav-right-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.user-menu, .orders-menu {
  display: flex;
  flex-direction: column;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid transparent;
  transition: border-color 0.2s;
}

.user-menu:hover, .orders-menu:hover {
  border-color: #ffffff;
}

.user-menu.active, .orders-menu.active {
  border-color: #febd69;
  background: rgba(254, 189, 105, 0.1);
}

.user-greeting, .orders-sub {
  font-size: 11px;
  color: #cccccc;
}

.menu-bold {
  font-size: 13px;
  font-weight: 700;
  color: #ffffff;
}

.cart-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: 1px solid transparent;
  color: #ffffff;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cart-button:hover {
  border-color: #ffffff;
}

.cart-button.active {
  border-color: #febd69;
  background: rgba(254, 189, 105, 0.15);
}

.cart-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.cart-icon {
  font-size: 24px;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #ff9900;
  color: #111827;
  font-size: 11px;
  font-weight: 800;
  padding: 1px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.cart-details {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.cart-label {
  font-size: 11px;
  color: #cccccc;
}

.cart-amount {
  font-size: 13px;
  font-weight: 700;
  color: #febd69;
}

.logout-btn {
  background: #232f3e;
  border: 1px solid #374151;
  color: #e5e7eb;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #374151;
  color: #ffffff;
}

/* Secondary Subnav */
.sub-nav {
  background-color: #232f3e;
  color: #ffffff;
  border-top: 1px solid #2d3748;
  padding: 0 16px;
  overflow-x: auto;
  white-space: nowrap;
}

.sub-nav-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 38px;
}

.sub-item {
  background: transparent;
  border: 1px solid transparent;
  color: #ffffff;
  font-size: 12.5px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.sub-item:hover {
  border-color: #ffffff;
}

.sub-item.active {
  background: #febd69;
  color: #111827;
  font-weight: 700;
}

.all-btn {
  font-weight: 700;
}

.persistent-status-chip {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  color: #a7f3d0;
  background: rgba(16, 185, 129, 0.15);
  border: 1px solid rgba(16, 185, 129, 0.3);
  padding: 3px 8px;
  border-radius: 12px;
}

.pulse-dot {
  width: 7px;
  height: 7px;
  background-color: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 6px #10b981;
}

@media (max-width: 1024px) {
  .delivery-speed-badge, .location-pill {
    display: none;
  }
}
</style>
