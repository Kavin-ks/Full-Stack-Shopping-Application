<template>
  <div class="catalog-page">
    <!-- Promotional Hero Banners -->
    <div class="promo-strip">
      <div class="hero-banner instamart-banner">
        <div class="banner-badge">⚡ INSTAMART EXPRESS</div>
        <h2>Lightning Fast Groceries</h2>
        <p>Fresh Mangoes, Cold Brews & Dairy delivered to your doorstep in 10-15 minutes.</p>
        <span class="banner-highlight">Use code <strong>INSTANT10</strong> at checkout for 10% OFF</span>
      </div>

      <div class="hero-banner amazon-banner">
        <div class="banner-badge prime">PRIME SUPERMART</div>
        <h2>Flagship Tech & Fashion</h2>
        <p>MacBook M3, iPhone 16 Pro, Sony ANC headphones with Free Next-Day Prime delivery.</p>
        <span class="banner-highlight prime">Free Returns • 100% Genuine Guarantee</span>
      </div>
    </div>

    <!-- Filter & Sort Bar -->
    <div class="filter-controls">
      <div class="categories-scroll">
        <button 
          v-for="cat in categories" 
          :key="cat"
          class="cat-chip" 
          :class="{ active: selectedCategory === cat }"
          @click="selectCategory(cat)"
        >
          {{ cat }}
        </button>
      </div>

      <div class="sort-box">
        <label for="sort-select">Sort by:</label>
        <select id="sort-select" v-model="sortBy">
          <option value="featured">Featured / Best Match</option>
          <option value="price-low">Price: Low to High</option>
          <option value="price-high">Price: High to Low</option>
          <option value="rating">Customer Rating</option>
        </select>
      </div>
    </div>

    <!-- Active Search Notification -->
    <div v-if="searchQuery" class="search-indicator">
      <span>Search results for: <strong>"{{ searchQuery }}"</strong></span>
      <button class="clear-btn" @click="$emit('clear-search')">Clear search</button>
    </div>

    <!-- Empty State -->
    <div v-if="displayedProducts.length === 0" class="empty-state">
      <div class="empty-icon">🔍</div>
      <h3>No matching products found</h3>
      <p>Try searching with another keyword or pick a different category.</p>
      <button class="reset-filter-btn" @click="resetFilters">Show All Products</button>
    </div>

    <!-- Products Grid -->
    <div v-else class="products-grid">
      <div v-for="prod in displayedProducts" :key="prod.id" class="product-card">
        <!-- Delivery Speed Badge -->
        <div class="card-top-tags">
          <span 
            class="speed-tag" 
            :class="{ 'instamart-tag': prod.deliveryBadge.includes('10 MINS'), 'prime-tag': prod.deliveryBadge.includes('Prime') }"
          >
            {{ prod.deliveryBadge }}
          </span>
          <span v-if="discountPercent(prod) > 0" class="discount-pill">
            {{ discountPercent(prod) }}% OFF
          </span>
        </div>

        <!-- Product Visual Media -->
        <div class="card-visual">
          <span class="product-emoji">{{ prod.emoji }}</span>
          <span class="prod-category-label">{{ prod.category }}</span>
        </div>

        <!-- Card Content -->
        <div class="card-info">
          <h3 class="prod-name" :title="prod.name">{{ prod.name }}</h3>
          <p class="prod-unit">{{ prod.unit }}</p>
          
          <!-- Rating -->
          <div class="rating-row">
            <div class="rating-stars">
              <span class="star">★</span>
              <span class="rating-val">{{ prod.rating }}</span>
            </div>
            <span class="rating-count">({{ prod.ratingCount.toLocaleString('en-IN') }})</span>
          </div>

          <p class="prod-description">{{ prod.description }}</p>

          <!-- Pricing Row -->
          <div class="price-row">
            <div class="price-stack">
              <span class="current-price">₹{{ Number(prod.price).toLocaleString('en-IN') }}</span>
              <span v-if="prod.originalPrice > prod.price" class="original-price">
                ₹{{ Number(prod.originalPrice).toLocaleString('en-IN') }}
              </span>
            </div>
            <span class="stock-indicator" :class="{ low: prod.stock < 20 }">
              {{ prod.stock > 0 ? (prod.stock < 20 ? `Only ${prod.stock} left!` : 'In Stock') : 'Out of stock' }}
            </span>
          </div>

          <!-- Dynamic Instamart Cart Action -->
          <div class="card-action-bar">
            <!-- If item already in cart, show interactive quantity stepper -->
            <div v-if="getCartQty(prod.id) > 0" class="cart-stepper">
              <button 
                class="stepper-btn minus" 
                @click="decrementCart(prod.id)"
                title="Decrease quantity"
              >
                −
              </button>
              <span class="stepper-count">{{ getCartQty(prod.id) }}</span>
              <button 
                class="stepper-btn plus" 
                @click="incrementCart(prod)"
                :disabled="getCartQty(prod.id) >= prod.stock"
                title="Increase quantity"
              >
                +
              </button>
            </div>

            <!-- If not in cart, show Add button -->
            <button 
              v-else 
              class="add-to-cart-btn" 
              :disabled="prod.stock <= 0"
              @click="handleFirstAdd(prod)"
            >
              <span>+ ADD TO CART</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductCatalog',
  props: {
    products: {
      type: Array,
      default: () => []
    },
    searchQuery: {
      type: String,
      default: ''
    },
    cartItems: {
      type: Array,
      default: () => []
    },
    selectedCategoryProp: {
      type: String,
      default: 'All'
    }
  },
  emits: ['add-to-cart', 'update-cart-qty', 'remove-cart-item', 'clear-search', 'category-change'],
  data() {
    return {
      selectedCategory: this.selectedCategoryProp || 'All',
      sortBy: 'featured'
    }
  },
  watch: {
    selectedCategoryProp(newCat) {
      this.selectedCategory = newCat
    }
  },
  computed: {
    categories() {
      const cats = ['All']
      this.products.forEach(p => {
        if (p.category && !cats.includes(p.category)) {
          cats.push(p.category)
        }
      })
      return cats
    },
    displayedProducts() {
      let result = [...this.products]

      // Filter by category
      if (this.selectedCategory && this.selectedCategory !== 'All') {
        result = result.filter(p => p.category === this.selectedCategory)
      }

      // Filter by search query
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase().trim()
        result = result.filter(p => 
          p.name.toLowerCase().includes(query) ||
          p.category.toLowerCase().includes(query) ||
          p.description.toLowerCase().includes(query)
        )
      }

      // Sorting
      if (this.sortBy === 'price-low') {
        result.sort((a, b) => a.price - b.price)
      } else if (this.sortBy === 'price-high') {
        result.sort((a, b) => b.price - a.price)
      } else if (this.sortBy === 'rating') {
        result.sort((a, b) => b.rating - a.rating)
      }

      return result
    }
  },
  methods: {
    selectCategory(cat) {
      this.selectedCategory = cat
      this.$emit('category-change', cat)
    },
    resetFilters() {
      this.selectedCategory = 'All'
      this.$emit('clear-search')
    },
    discountPercent(prod) {
      if (!prod.originalPrice || prod.originalPrice <= prod.price) return 0
      return Math.round(((prod.originalPrice - prod.price) / prod.originalPrice) * 100)
    },
    getCartItem(productId) {
      return this.cartItems.find(item => item.productId === productId)
    },
    getCartQty(productId) {
      const item = this.getCartItem(productId)
      return item ? item.quantity : 0
    },
    handleFirstAdd(product) {
      this.$emit('add-to-cart', product, 1)
    },
    incrementCart(product) {
      const currentQty = this.getCartQty(product.id)
      const item = this.getCartItem(product.id)
      if (item) {
        this.$emit('update-cart-qty', item.id, currentQty + 1)
      } else {
        this.$emit('add-to-cart', product, 1)
      }
    },
    decrementCart(productId) {
      const item = this.getCartItem(productId)
      if (!item) return
      if (item.quantity <= 1) {
        this.$emit('remove-cart-item', item.id)
      } else {
        this.$emit('update-cart-qty', item.id, item.quantity - 1)
      }
    }
  }
}
</script>

<style scoped>
.catalog-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.promo-strip {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.hero-banner {
  border-radius: 12px;
  padding: 20px 24px;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
}

.instamart-banner {
  background: linear-gradient(135deg, #ea580c 0%, #c2410c 100%);
}

.amazon-banner {
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
}

.banner-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.5px;
  width: fit-content;
}

.banner-badge.prime {
  background: #ff9900;
  color: #111827;
}

.hero-banner h2 {
  font-size: 20px;
  font-weight: 800;
  margin: 2px 0 0 0;
  letter-spacing: -0.3px;
}

.hero-banner p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  line-height: 1.4;
}

.banner-highlight {
  font-size: 12px;
  font-weight: 600;
  background: rgba(0, 0, 0, 0.2);
  padding: 4px 10px;
  border-radius: 6px;
  margin-top: 6px;
  width: fit-content;
}

.banner-highlight.prime {
  color: #fde047;
}

.filter-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  background: #ffffff;
  padding: 12px 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.categories-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  white-space: nowrap;
}

.cat-chip {
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  color: #334155;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.cat-chip:hover {
  background: #e2e8f0;
}

.cat-chip.active {
  background: #ff5200;
  border-color: #ff5200;
  color: #ffffff;
}

.sort-box {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
}

.sort-box select {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  font-size: 13px;
  background: #ffffff;
  color: #0f172a;
  outline: none;
  cursor: pointer;
}

.search-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fffbeb;
  border: 1px solid #fef3c7;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 13.5px;
  color: #92400e;
}

.clear-btn {
  background: transparent;
  border: 1px solid #f59e0b;
  color: #b45309;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 50px 20px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px dashed #cbd5e1;
}

.empty-icon {
  font-size: 44px;
  margin-bottom: 8px;
}

.empty-state h3 {
  font-size: 18px;
  color: #0f172a;
  margin: 0 0 6px 0;
}

.empty-state p {
  color: #64748b;
  font-size: 13.5px;
  margin: 0 0 16px 0;
}

.reset-filter-btn {
  background: #2563eb;
  color: #ffffff;
  border: none;
  padding: 8px 18px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}

.product-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
}

.card-top-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 2;
}

.speed-tag {
  font-size: 10.5px;
  font-weight: 800;
  padding: 3px 8px;
  border-radius: 4px;
  letter-spacing: 0.3px;
}

.instamart-tag {
  background: #ff5200;
  color: #ffffff;
}

.prime-tag {
  background: #002f34;
  color: #00a8e8;
}

.discount-pill {
  background: #16a34a;
  color: #ffffff;
  font-size: 10.5px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
}

.card-visual {
  background: #f8fafc;
  padding: 36px 16px 20px 16px;
  text-align: center;
  position: relative;
}

.product-emoji {
  font-size: 56px;
  display: inline-block;
  transition: transform 0.2s;
}

.product-card:hover .product-emoji {
  transform: scale(1.1);
}

.prod-category-label {
  position: absolute;
  bottom: 6px;
  left: 12px;
  font-size: 10px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
}

.card-info {
  padding: 14px;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  gap: 8px;
}

.prod-name {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
}

.prod-unit {
  font-size: 12px;
  color: #64748b;
  margin: 0;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 3px;
  background: #fffbeb;
  border: 1px solid #fef3c7;
  padding: 2px 6px;
  border-radius: 4px;
}

.star {
  color: #f59e0b;
  font-size: 12px;
}

.rating-val {
  font-size: 11.5px;
  font-weight: 700;
  color: #78350f;
}

.rating-count {
  font-size: 11.5px;
  color: #94a3b8;
}

.prod-description {
  font-size: 12px;
  color: #64748b;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex-grow: 1;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 4px;
}

.price-stack {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.current-price {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.original-price {
  font-size: 12px;
  color: #94a3b8;
  text-decoration: line-through;
}

.stock-indicator {
  font-size: 11px;
  font-weight: 600;
  color: #16a34a;
}

.stock-indicator.low {
  color: #dc2626;
}

.card-action-bar {
  margin-top: 6px;
}

.add-to-cart-btn {
  width: 100%;
  background: #ff5200;
  color: #ffffff;
  border: none;
  padding: 9px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.3px;
}

.add-to-cart-btn:hover:not(:disabled) {
  background: #e04800;
  box-shadow: 0 4px 10px rgba(255, 82, 0, 0.3);
}

.add-to-cart-btn:disabled {
  background: #cbd5e1;
  color: #64748b;
  cursor: not-allowed;
}

.cart-stepper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #1e293b;
  border-radius: 8px;
  overflow: hidden;
  height: 36px;
}

.stepper-btn {
  background: transparent;
  border: none;
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  width: 40px;
  height: 100%;
  cursor: pointer;
  transition: background 0.15s;
}

.stepper-btn:hover {
  background: rgba(255, 255, 255, 0.15);
}

.stepper-count {
  font-size: 14px;
  font-weight: 800;
  color: #ffffff;
}

@media (max-width: 768px) {
  .promo-strip {
    grid-template-columns: 1fr;
  }
}
</style>
