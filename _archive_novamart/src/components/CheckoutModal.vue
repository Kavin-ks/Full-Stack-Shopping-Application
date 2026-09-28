<template>
  <div class="modal-backdrop" @click.self="$emit('close')">
    <div class="checkout-modal">
      <!-- Order Placed Success View -->
      <div v-if="orderSuccess" class="success-screen">
        <div class="celebration-badge">
          <span class="confetti-emoji">🎉</span>
          <span class="check-icon">✓</span>
        </div>
        <h2>Order Successfully Placed!</h2>
        <p class="order-id-txt">Order ID: <strong>{{ placedOrder.orderId }}</strong></p>
        <p class="delivery-eta-txt">
          ⚡ Estimated Delivery in <strong>12 - 15 minutes</strong> to {{ currentUser ? currentUser.name : 'you' }}.
        </p>

        <div class="order-receipt-box">
          <div class="receipt-row">
            <span>Delivering to:</span>
            <strong>{{ placedOrder.deliveryAddress }}</strong>
          </div>
          <div class="receipt-row">
            <span>Payment Method:</span>
            <strong>{{ placedOrder.paymentMethod }}</strong>
          </div>
          <div class="receipt-row">
            <span>Total Amount Paid:</span>
            <strong class="receipt-amount">₹{{ Number(placedOrder.totalAmount).toLocaleString('en-IN') }}</strong>
          </div>
          <div class="receipt-row">
            <span>Items ({{ placedOrder.items.length }}):</span>
            <span class="item-names-preview">
              {{ placedOrder.items.map(i => `${i.quantity}x ${i.name}`).join(', ') }}
            </span>
          </div>
        </div>

        <div class="success-actions">
          <button class="view-orders-btn" @click="$emit('view-orders')">
            📦 Track Order in "My Orders"
          </button>
          <button class="continue-btn" @click="$emit('close')">
            Continue Shopping
          </button>
        </div>
      </div>

      <!-- Checkout Form View -->
      <div v-else class="checkout-form-screen">
        <div class="modal-header">
          <div class="header-title-box">
            <span class="lock-icon">🔒</span>
            <h3>Secure Fast Checkout</h3>
          </div>
          <button class="close-x-btn" @click="$emit('close')">✕</button>
        </div>

        <div class="modal-body">
          <!-- Step 1: Delivery Option -->
          <div class="checkout-section">
            <label class="section-label">1. Choose Delivery Speed</label>
            <div class="speed-options">
              <label class="speed-card" :class="{ selected: deliveryType === 'instant' }">
                <input type="radio" value="instant" v-model="deliveryType" />
                <div class="speed-card-content">
                  <div class="speed-title-line">
                    <span class="bolt">⚡</span>
                    <strong>Instamart 10-15 Mins Instant</strong>
                    <span class="speed-tag">FASTEST</span>
                  </div>
                  <p>Direct priority delivery from nearby dark store.</p>
                </div>
              </label>

              <label class="speed-card" :class="{ selected: deliveryType === 'scheduled' }">
                <input type="radio" value="scheduled" v-model="deliveryType" />
                <div class="speed-card-content">
                  <div class="speed-title-line">
                    <span class="van">📦</span>
                    <strong>Prime Next Morning Delivery</strong>
                  </div>
                  <p>Guaranteed morning delivery slot (7 AM - 10 AM).</p>
                </div>
              </label>
            </div>
          </div>

          <!-- Step 2: Shipping Address -->
          <div class="checkout-section">
            <div class="section-header-flex">
              <label class="section-label">2. Delivery Address</label>
              <button class="edit-addr-link" @click="isEditingAddress = !isEditingAddress">
                {{ isEditingAddress ? 'Done' : 'Edit' }}
              </button>
            </div>

            <div v-if="!isEditingAddress" class="address-preview-box">
              <span class="recipient-name"><strong>{{ currentUser ? currentUser.name : 'Customer' }}</strong> ({{ currentUser ? currentUser.phone : '' }})</span>
              <p class="recipient-address">{{ customAddress || (currentUser ? currentUser.address : '') }}</p>
            </div>

            <div v-else class="address-edit-box">
              <input 
                type="text" 
                v-model="customAddress" 
                placeholder="Enter new delivery address with city and pin code"
                class="address-input"
              />
            </div>
          </div>

          <!-- Step 3: Payment Method -->
          <div class="checkout-section">
            <label class="section-label">3. Select Payment Method</label>
            <div class="payment-methods-list">
              <label class="payment-option" :class="{ active: paymentMethod === 'UPI (Google Pay / PhonePe)' }">
                <input type="radio" value="UPI (Google Pay / PhonePe)" v-model="paymentMethod" />
                <div class="option-info">
                  <span class="opt-title">⚡ Instant UPI (Google Pay, PhonePe, Paytm)</span>
                  <span class="opt-desc">Instant 0-fee payment via any UPI app</span>
                </div>
              </label>

              <label class="payment-option" :class="{ active: paymentMethod === 'Credit / Debit Card' }">
                <input type="radio" value="Credit / Debit Card" v-model="paymentMethod" />
                <div class="option-info">
                  <span class="opt-title">💳 Credit / Debit Card (Visa, Mastercard, RuPay)</span>
                  <span class="opt-desc">Save card securely for future purchases</span>
                </div>
              </label>

              <label class="payment-option" :class="{ active: paymentMethod === 'Cash on Delivery (COD)' }">
                <input type="radio" value="Cash on Delivery (COD)" v-model="paymentMethod" />
                <div class="option-info">
                  <span class="opt-title">💵 Cash / UPI on Delivery</span>
                  <span class="opt-desc">Pay at your doorstep upon receiving items</span>
                </div>
              </label>
            </div>
          </div>

          <!-- Order Summary Line -->
          <div class="checkout-bill-preview">
            <div class="bill-mini-row">
              <span>Items: {{ cart.totalQuantity }}</span>
              <span>Total: <strong>₹{{ Number(cart.finalTotal).toLocaleString('en-IN') }}</strong></span>
            </div>
          </div>
        </div>

        <!-- Place Order Button -->
        <div class="modal-footer">
          <button class="confirm-order-btn" :disabled="submitting" @click="handlePlaceOrder">
            <span v-if="submitting">Processing Order...</span>
            <span v-else>Confirm & Place Order (₹{{ Number(cart.finalTotal).toLocaleString('en-IN') }})</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { api } from '../services/api'

export default {
  name: 'CheckoutModal',
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
  emits: ['close', 'order-placed', 'view-orders'],
  data() {
    return {
      deliveryType: 'instant',
      paymentMethod: 'UPI (Google Pay / PhonePe)',
      customAddress: this.currentUser ? this.currentUser.address : '',
      isEditingAddress: false,
      submitting: false,
      orderSuccess: false,
      placedOrder: null
    }
  },
  methods: {
    async handlePlaceOrder() {
      this.submitting = true
      try {
        const finalAddress = this.customAddress || (this.currentUser ? this.currentUser.address : 'Default Address')
        const payload = {
          deliveryType: this.deliveryType === 'instant' ? '⚡ Instamart 10-15 Mins Instant' : '📦 Prime Scheduled Morning',
          deliveryAddress: finalAddress,
          paymentMethod: this.paymentMethod,
          items: this.cart.items,
          subTotal: this.cart.subTotal,
          discountAmount: this.cart.discountAmount,
          deliveryFee: this.cart.deliveryFee,
          packagingFee: this.cart.packagingFee,
          finalTotal: this.cart.finalTotal
        }

        const res = await api.placeOrder(this.currentUser.id, payload)
        this.placedOrder = res.order
        this.orderSuccess = true
        this.$emit('order-placed', res.order, res.cart)
      } catch (err) {
        alert('Order placement error: ' + err.message)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.checkout-modal {
  background: #ffffff;
  border-radius: 16px;
  width: 100%;
  max-width: 540px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.header-title-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title-box h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.close-x-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #94a3b8;
  cursor: pointer;
}

.modal-body {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.checkout-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-label {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
}

.section-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-addr-link {
  background: none;
  border: none;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.speed-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.speed-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.speed-card.selected {
  border-color: #ea580c;
  background: #fff7ed;
}

.speed-card-content p {
  font-size: 11.5px;
  color: #64748b;
  margin: 3px 0 0 0;
}

.speed-title-line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #0f172a;
}

.speed-tag {
  background: #ea580c;
  color: #ffffff;
  font-size: 9px;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: 4px;
}

.address-preview-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
}

.recipient-name {
  font-size: 12.5px;
  color: #0f172a;
  display: block;
}

.recipient-address {
  font-size: 12px;
  color: #475569;
  margin: 4px 0 0 0;
  line-height: 1.4;
}

.address-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
}

.payment-methods-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.payment-option.active {
  border-color: #2563eb;
  background: #eff6ff;
}

.option-info {
  display: flex;
  flex-direction: column;
}

.opt-title {
  font-size: 12.5px;
  font-weight: 700;
  color: #0f172a;
}

.opt-desc {
  font-size: 11px;
  color: #64748b;
}

.checkout-bill-preview {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px 14px;
  border: 1px dashed #cbd5e1;
}

.bill-mini-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #0f172a;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
}

.confirm-order-btn {
  width: 100%;
  background: #16a34a;
  color: #ffffff;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.3);
  transition: background 0.15s;
}

.confirm-order-btn:hover:not(:disabled) {
  background: #15803d;
}

.confirm-order-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Success View */
.success-screen {
  padding: 36px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.celebration-badge {
  position: relative;
  width: 72px;
  height: 72px;
  background: #dcfce7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.check-icon {
  font-size: 36px;
  color: #16a34a;
  font-weight: 900;
}

.confetti-emoji {
  position: absolute;
  top: -6px;
  right: -6px;
  font-size: 24px;
}

.success-screen h2 {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px 0;
}

.order-id-txt {
  font-size: 13.5px;
  color: #475569;
  margin: 0 0 4px 0;
}

.delivery-eta-txt {
  font-size: 13.5px;
  color: #ea580c;
  margin: 0 0 20px 0;
}

.order-receipt-box {
  width: 100%;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px 18px;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.receipt-row {
  display: flex;
  justify-content: space-between;
  font-size: 12.5px;
  color: #475569;
}

.receipt-row strong {
  color: #0f172a;
}

.receipt-amount {
  color: #ff5200;
  font-size: 14px;
}

.item-names-preview {
  font-size: 11.5px;
  color: #334155;
  max-width: 260px;
  text-align: right;
}

.success-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.view-orders-btn {
  background: #ff5200;
  color: #ffffff;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
}

.continue-btn {
  background: transparent;
  border: 1px solid #cbd5e1;
  color: #334155;
  padding: 10px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
</style>
