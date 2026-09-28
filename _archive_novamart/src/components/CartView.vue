<template>
  <div class="cart-view-page">
    <div class="cart-header">
      <div>
        <h1 class="page-title">Shopping Cart</h1>
        <p class="page-subtitle">
          Items in this cart belong to <strong>{{ currentUser ? currentUser.name : 'your account' }}</strong> and are permanently preserved in your local database.
        </p>
      </div>

      <button v-if="cart.items && cart.items.length > 0" class="clear-all-btn" @click="$emit('clear-cart')">
        Empty Cart
      </button>
    </div>

    <!-- Empty State -->
    <div v-if="!cart.items || cart.items.length === 0" class="empty-cart-card">
      <div class="empty-cart-emoji">🛒</div>
      <h2>Your NovaMart Cart is Empty</h2>
      <p>Looks like you haven't added anything yet. Explore our Instamart fresh produce and Amazon supermart products!</p>
      <button class="shop-now-btn" @click="$emit('continue-shopping')">
        Explore Products Now
      </button>
    </div>

    <!-- Cart Layout -->
    <div v-else class="cart-content-layout">
      <!-- Left: Cart Items List -->
      <div class="cart-items-section">
        <div class="delivery-time-banner">
          <span class="banner-bolt">⚡</span>
          <div>
            <strong>10 - 15 MINS INSTANT DELIVERY GUARANTEE</strong>
            <p>Your order will be prepared immediately from the nearest dark store.</p>
          </div>
        </div>

        <div class="items-card-list">
          <div v-for="item in cart.items" :key="item.id" class="cart-item-row">
            <div class="item-icon-box">
              <span class="item-emoji">{{ item.emoji || '📦' }}</span>
            </div>

            <div class="item-main-details">
              <div class="item-badge-row">
                <span class="item-speed-badge">{{ item.deliveryBadge || '⚡ 10 MINS' }}</span>
              </div>
              <h3 class="item-name">{{ item.name }}</h3>
              <p v-if="item.unit" class="item-unit">{{ item.unit }}</p>
              <div class="item-price-info">
                <span class="unit-price">₹{{ Number(item.price).toLocaleString('en-IN') }} each</span>
              </div>
            </div>

            <!-- Quantity Stepper -->
            <div class="quantity-controller">
              <button 
                class="qty-btn" 
                @click="$emit('update-qty', item.id, item.quantity - 1)"
                title="Decrease"
              >
                −
              </button>
              <span class="qty-number">{{ item.quantity }}</span>
              <button 
                class="qty-btn" 
                @click="$emit('update-qty', item.id, item.quantity + 1)"
                title="Increase"
              >
                +
              </button>
            </div>

            <!-- Subtotal & Remove -->
            <div class="item-subtotal-column">
              <span class="item-total-price">₹{{ Number(item.subTotal).toLocaleString('en-IN') }}</span>
              <button class="delete-item-btn" @click="$emit('remove-item', item.id)" title="Remove item">
                🗑️ Remove
              </button>
            </div>
          </div>
        </div>

        <!-- Continue shopping link -->
        <button class="back-to-shop-btn" @click="$emit('continue-shopping')">
          ← Add More Items from Catalog
        </button>
      </div>

      <!-- Right: Bill Summary & Checkout -->
      <div class="cart-summary-section">
        <!-- Delivery Address Card -->
        <div class="summary-card address-card">
          <div class="card-header-line">
            <span class="pin">📍 Delivery Address</span>
          </div>
          <p class="address-text">{{ currentUser ? currentUser.address : 'Default Shipping Address' }}</p>
          <span class="delivery-time-note">Arriving in approx <strong>12 mins</strong></span>
        </div>

        <!-- Coupons Card -->
        <div class="summary-card coupon-card">
          <div class="coupon-header">
            <span>🏷️ Offers & Promo Codes</span>
          </div>

          <div v-if="cart.appliedCoupon" class="active-coupon-pill">
            <div>
              <span class="coupon-name">🎉 {{ cart.appliedCoupon }} APPLIED</span>
              <small class="coupon-saved">You saved ₹{{ Number(cart.discountAmount).toLocaleString('en-IN') }}!</small>
            </div>
            <button class="remove-coupon-btn" @click="$emit('remove-coupon')">Remove</button>
          </div>

          <div v-else class="coupon-input-group">
            <input 
              type="text" 
              v-model="couponCodeInput" 
              placeholder="Enter coupon code" 
              class="coupon-input"
            />
            <button class="apply-coupon-btn" @click="handleApplyCoupon">Apply</button>
          </div>

          <!-- Suggested Coupons -->
          <div class="suggested-coupons">
            <button class="sug-pill" @click="applyPresetCoupon('INSTANT10')">
              <strong>INSTANT10</strong>: 10% off entire order
            </button>
            <button class="sug-pill" @click="applyPresetCoupon('AMZ50')">
              <strong>AMZ50</strong>: ₹50 flat discount
            </button>
          </div>
        </div>

        <!-- Bill Details Card -->
        <div class="summary-card bill-card">
          <h3 class="bill-title">Bill Details</h3>

          <div class="bill-row">
            <span>Item Total ({{ cart.totalQuantity }} items)</span>
            <span>₹{{ Number(cart.subTotal).toLocaleString('en-IN') }}</span>
          </div>

          <div v-if="cart.discountAmount > 0" class="bill-row discount-row">
            <span>Coupon Discount</span>
            <span class="discount-val">- ₹{{ Number(cart.discountAmount).toLocaleString('en-IN') }}</span>
          </div>

          <div class="bill-row">
            <span>Delivery Partner Fee</span>
            <span v-if="cart.deliveryFee === 0" class="free-badge">FREE</span>
            <span v-else>₹{{ cart.deliveryFee }}</span>
          </div>

          <div class="bill-row">
            <span>Packaging & Handling</span>
            <span>₹{{ cart.packagingFee }}</span>
          </div>

          <div class="bill-divider"></div>

          <div class="bill-row total-row">
            <span>To Pay</span>
            <span class="grand-total">₹{{ Number(cart.finalTotal).toLocaleString('en-IN') }}</span>
          </div>

          <!-- Place Order / Checkout Button -->
          <button class="proceed-pay-btn" @click="$emit('open-checkout')">
            <span>Proceed to Checkout (₹{{ Number(cart.finalTotal).toLocaleString('en-IN') }})</span>
            <span class="arrow">→</span>
          </button>

          <div class="safety-badge">
            <span class="lock">🔒</span>
            <span>100% Safe Payments • Instant Cancellation available</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CartView',
  props: {
    cart: {
      type: Object,
      required: true
    },
    currentUser: {
      type: Object,
      default: null
    }
  },
  emits: ['update-qty', 'remove-item', 'clear-cart', 'continue-shopping', 'apply-coupon', 'remove-coupon', 'open-checkout'],
  data() {
    return {
      couponCodeInput: ''
    }
  },
  methods: {
    handleApplyCoupon() {
      if (!this.couponCodeInput.trim()) return
      this.$emit('apply-coupon', this.couponCodeInput)
      this.couponCodeInput = ''
    },
    applyPresetCoupon(code) {
      this.$emit('apply-coupon', code)
    }
  }
}
</script>

<style scoped>
.cart-view-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 14px;
}

.page-title {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.clear-all-btn {
  background: #fee2e2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s;
}

.clear-all-btn:hover {
  background: #fca5a5;
}

.empty-cart-card {
  background: #ffffff;
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  text-align: center;
  padding: 60px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-cart-emoji {
  font-size: 64px;
  margin-bottom: 12px;
}

.empty-cart-card h2 {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 8px 0;
}

.empty-cart-card p {
  color: #64748b;
  font-size: 14px;
  max-width: 440px;
  margin: 0 0 24px 0;
}

.shop-now-btn {
  background: #ff5200;
  color: #ffffff;
  border: none;
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(255, 82, 0, 0.3);
}

.cart-content-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

.cart-items-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.delivery-time-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 10px;
  padding: 12px 16px;
}

.banner-bolt {
  font-size: 24px;
  color: #ea580c;
}

.delivery-time-banner strong {
  font-size: 12px;
  color: #9a3412;
  letter-spacing: 0.3px;
  display: block;
}

.delivery-time-banner p {
  font-size: 12px;
  color: #c2410c;
  margin: 2px 0 0 0;
}

.items-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-item-row {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.item-icon-box {
  width: 64px;
  height: 64px;
  background: #f8fafc;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-emoji {
  font-size: 36px;
}

.item-main-details {
  flex-grow: 1;
}

.item-speed-badge {
  background: #ff5200;
  color: #ffffff;
  font-size: 10px;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 4px;
}

.item-name {
  font-size: 14.5px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 2px 0;
}

.item-unit {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 4px 0;
}

.unit-price {
  font-size: 12px;
  color: #64748b;
}

.quantity-controller {
  display: flex;
  align-items: center;
  background: #f1f5f9;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
}

.qty-btn {
  background: transparent;
  border: none;
  width: 32px;
  height: 34px;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  cursor: pointer;
  transition: background 0.15s;
}

.qty-btn:hover {
  background: #e2e8f0;
}

.qty-number {
  width: 36px;
  text-align: center;
  font-size: 13.5px;
  font-weight: 800;
  color: #0f172a;
}

.item-subtotal-column {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  min-width: 90px;
  gap: 6px;
}

.item-total-price {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.delete-item-btn {
  background: transparent;
  border: none;
  font-size: 11.5px;
  color: #94a3b8;
  cursor: pointer;
  padding: 2px 4px;
}

.delete-item-btn:hover {
  color: #dc2626;
}

.back-to-shop-btn {
  align-self: flex-start;
  background: transparent;
  border: 1px solid #cbd5e1;
  color: #334155;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.back-to-shop-btn:hover {
  background: #f1f5f9;
}

.cart-summary-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header-line {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 6px;
}

.address-text {
  font-size: 12.5px;
  color: #475569;
  line-height: 1.4;
  margin: 0 0 6px 0;
}

.delivery-time-note {
  font-size: 11.5px;
  color: #ea580c;
}

.coupon-header {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
}

.active-coupon-pill {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f0fdf4;
  border: 1px solid #86efac;
  padding: 8px 12px;
  border-radius: 8px;
  margin-bottom: 8px;
}

.coupon-name {
  font-size: 12px;
  font-weight: 700;
  color: #15803d;
  display: block;
}

.coupon-saved {
  font-size: 11px;
  color: #16a34a;
}

.remove-coupon-btn {
  background: transparent;
  border: none;
  color: #dc2626;
  font-size: 11.5px;
  font-weight: 700;
  cursor: pointer;
}

.coupon-input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.coupon-input {
  flex-grow: 1;
  padding: 8px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 12.5px;
  text-transform: uppercase;
  outline: none;
}

.apply-coupon-btn {
  background: #0f172a;
  color: #ffffff;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
}

.suggested-coupons {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sug-pill {
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  padding: 6px 8px;
  font-size: 11px;
  text-align: left;
  color: #334155;
  cursor: pointer;
}

.sug-pill:hover {
  background: #eff6ff;
  border-color: #3b82f6;
}

.bill-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 12px 0;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 8px;
}

.bill-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #475569;
  margin-bottom: 8px;
}

.discount-row {
  color: #16a34a;
}

.free-badge {
  color: #16a34a;
  font-weight: 700;
}

.bill-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 12px 0;
}

.total-row {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 16px;
}

.grand-total {
  font-size: 20px;
  color: #ff5200;
}

.proceed-pay-btn {
  width: 100%;
  background: linear-gradient(180deg, #ff5200 0%, #ea580c 100%);
  color: #ffffff;
  border: none;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 12px rgba(255, 82, 0, 0.35);
  transition: transform 0.15s ease;
}

.proceed-pay-btn:hover {
  transform: translateY(-1px);
}

.safety-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 12px;
  font-size: 11px;
  color: #64748b;
}

@media (max-width: 900px) {
  .cart-content-layout {
    grid-template-columns: 1fr;
  }
}
</style>
