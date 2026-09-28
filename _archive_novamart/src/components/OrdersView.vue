<template>
  <div class="orders-page">
    <div class="orders-header">
      <div>
        <h1 class="page-title">Your Orders & History</h1>
        <p class="page-subtitle">
          Showing verified order history saved permanently for <strong>{{ currentUser ? currentUser.name : 'your account' }}</strong>.
        </p>
      </div>

      <div class="orders-stats-chip">
        <span>Total Orders: <strong>{{ orders.length }}</strong></span>
      </div>
    </div>

    <!-- Empty Orders State -->
    <div v-if="orders.length === 0" class="empty-orders-card">
      <div class="empty-orders-icon">📦</div>
      <h2>No orders placed yet</h2>
      <p>When you place orders for groceries, tech, or fashion, they will be securely tracked and stored here forever.</p>
      <button class="start-shopping-btn" @click="$emit('continue-shopping')">
        Start Shopping Now
      </button>
    </div>

    <!-- Orders List -->
    <div v-else class="orders-list">
      <div v-for="order in orders" :key="order.orderId" class="order-card">
        <!-- Order Header Strip -->
        <div class="order-header-strip">
          <div class="header-col">
            <span class="strip-label">ORDER PLACED</span>
            <span class="strip-val">{{ formatDate(order.placedAt) }}</span>
          </div>

          <div class="header-col">
            <span class="strip-label">TOTAL AMOUNT</span>
            <span class="strip-val price-val">₹{{ Number(order.totalAmount).toLocaleString('en-IN') }}</span>
          </div>

          <div class="header-col">
            <span class="strip-label">SHIP TO</span>
            <span class="strip-val address-preview" :title="order.deliveryAddress">
              {{ currentUser ? currentUser.name : 'Recipient' }}
            </span>
          </div>

          <div class="header-col order-id-col">
            <span class="strip-label">ORDER # {{ order.orderId }}</span>
            <div class="header-actions">
              <button class="invoice-btn" @click="toggleInvoice(order.orderId)">
                {{ activeInvoiceId === order.orderId ? 'Hide Receipt' : 'View Invoice' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Order Body -->
        <div class="order-body">
          <div class="status-row">
            <div class="status-badge" :class="statusClass(order.status)">
              <span class="status-dot"></span>
              <span>{{ order.status }}</span>
            </div>
            <span class="delivery-type-tag">{{ order.deliveryType }}</span>
          </div>

          <!-- Items list in order -->
          <div class="order-items-grid">
            <div v-for="item in order.items" :key="item.productId" class="order-item-chip">
              <span class="item-emoji">{{ item.emoji || '📦' }}</span>
              <div class="chip-text">
                <span class="item-title">{{ item.name }}</span>
                <span class="item-meta">Qty: {{ item.quantity }} × ₹{{ Number(item.price).toLocaleString('en-IN') }}</span>
              </div>
            </div>
          </div>

          <!-- Invoice Details Toggle -->
          <div v-if="activeInvoiceId === order.orderId" class="invoice-details-box">
            <div class="invoice-grid">
              <div>
                <strong>Payment Method:</strong>
                <p>{{ order.paymentMethod }}</p>
              </div>
              <div>
                <strong>Delivered To:</strong>
                <p>{{ order.deliveryAddress }}</p>
              </div>
              <div>
                <strong>Pricing Breakdown:</strong>
                <p>Items Subtotal: ₹{{ Number(order.itemTotal || order.totalAmount).toLocaleString('en-IN') }}</p>
                <p v-if="order.discount">Discount: -₹{{ Number(order.discount).toLocaleString('en-IN') }}</p>
                <p>Delivery Fee: {{ order.deliveryFee ? `₹${order.deliveryFee}` : 'FREE' }}</p>
                <p><strong>Grand Total: ₹{{ Number(order.totalAmount).toLocaleString('en-IN') }}</strong></p>
              </div>
            </div>
          </div>

          <!-- Card Actions Footer -->
          <div class="order-card-footer">
            <button class="reorder-btn" @click="$emit('reorder', order)">
              🔄 Order These Items Again
            </button>
            <span class="sync-check">✓ Stored persistently in local database</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrdersView',
  props: {
    orders: {
      type: Array,
      default: () => []
    },
    currentUser: {
      type: Object,
      default: null
    }
  },
  emits: ['continue-shopping', 'reorder'],
  data() {
    return {
      activeInvoiceId: null
    }
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return 'Recently'
      try {
        const d = new Date(dateStr)
        return d.toLocaleDateString('en-IN', {
          day: 'numeric',
          month: 'short',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (e) {
        return dateStr
      }
    },
    statusClass(status) {
      if (!status) return 'in-transit'
      const s = status.toLowerCase()
      if (s.includes('delivered')) return 'delivered'
      if (s.includes('out') || s.includes('soon')) return 'out-delivery'
      return 'in-transit'
    },
    toggleInvoice(orderId) {
      this.activeInvoiceId = this.activeInvoiceId === orderId ? null : orderId
    }
  }
}
</script>

<style scoped>
.orders-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.orders-header {
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

.orders-stats-chip {
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  color: #334155;
}

.empty-orders-card {
  background: #ffffff;
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  padding: 60px 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-orders-icon {
  font-size: 54px;
  margin-bottom: 12px;
}

.empty-orders-card h2 {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px 0;
}

.empty-orders-card p {
  font-size: 13.5px;
  color: #64748b;
  max-width: 420px;
  margin: 0 0 20px 0;
}

.start-shopping-btn {
  background: #ff5200;
  color: #ffffff;
  border: none;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.order-header-strip {
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  padding: 12px 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  justify-content: space-between;
  align-items: center;
}

.header-col {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.strip-label {
  font-size: 10.5px;
  font-weight: 700;
  color: #64748b;
  letter-spacing: 0.5px;
}

.strip-val {
  font-size: 12.5px;
  font-weight: 600;
  color: #0f172a;
}

.strip-val.price-val {
  color: #ff5200;
  font-weight: 700;
}

.address-preview {
  max-width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.order-id-col {
  margin-left: auto;
  align-items: flex-end;
}

.invoice-btn {
  background: none;
  border: none;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
}

.order-body {
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 6px;
}

.status-badge.delivered {
  background: #dcfce7;
  color: #15803d;
}

.status-badge.out-delivery {
  background: #ffedd5;
  color: #ea580c;
}

.status-badge.in-transit {
  background: #eff6ff;
  color: #1d4ed8;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.delivery-type-tag {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.order-items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 10px;
}

.order-item-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px 12px;
}

.item-emoji {
  font-size: 26px;
}

.chip-text {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.item-title {
  font-size: 12.5px;
  font-weight: 600;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  font-size: 11px;
  color: #64748b;
}

.invoice-details-box {
  background: #f1f5f9;
  border-radius: 8px;
  padding: 14px;
}

.invoice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  font-size: 12px;
  color: #334155;
}

.invoice-grid strong {
  color: #0f172a;
  display: block;
  margin-bottom: 4px;
}

.invoice-grid p {
  margin: 2px 0;
  line-height: 1.4;
}

.order-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f1f5f9;
  padding-top: 14px;
  flex-wrap: wrap;
  gap: 10px;
}

.reorder-btn {
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  color: #0f172a;
  padding: 7px 14px;
  border-radius: 6px;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.reorder-btn:hover {
  background: #ff5200;
  border-color: #ff5200;
  color: #ffffff;
}

.sync-check {
  font-size: 11.5px;
  color: #059669;
  font-weight: 500;
}
</style>
