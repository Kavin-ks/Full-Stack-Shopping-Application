<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '../../api'
import { useUiStore } from '../../stores/ui'
import { rupees, formatDate, ORDER_STATUS, PAYMENT_LABEL } from '../../utils/format'
import EmptyState from '../../components/EmptyState.vue'
import Icon from '../../components/Icon.vue'

const route = useRoute()
const ui = useUiStore()
const order = ref(null)
const missing = ref(false)
const cancelOpen = ref(false)
const reason = ref('')
const comment = ref('')
const cancelling = ref(false)

const REASONS = [
  'Order created by mistake',
  'Item would not arrive on time',
  'Shipping cost too high',
  'Item price too high',
  'Found cheaper somewhere else',
  'Need to change shipping address',
  'Need to change payment method',
  'Other'
]
const TRACK = ['PLACED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED']

onMounted(async () => {
  try {
    order.value = await orderApi.get(route.params.id)
  } catch {
    missing.value = true
  }
})

const canCancel = computed(() => order.value && ['PLACED', 'SHIPPED'].includes(order.value.status))

// Timeline: completed steps get their timestamp from the status history.
const steps = computed(() => {
  if (!order.value) return []
  const o = order.value
  const at = (s) => o.statusHistory.find((h) => h.status === s)?.at
  if (o.status === 'CANCELLED') {
    return [
      { key: 'PLACED', label: 'Order Confirmed', at: at('PLACED'), done: true },
      ...(at('SHIPPED') ? [{ key: 'SHIPPED', label: 'Shipped', at: at('SHIPPED'), done: true }] : []),
      { key: 'CANCELLED', label: 'Cancelled', at: at('CANCELLED'), done: true, bad: true }
    ]
  }
  const current = TRACK.indexOf(o.status)
  return TRACK.map((s, i) => ({
    key: s,
    label: ORDER_STATUS[s].label,
    at: at(s) || (s === 'DELIVERED' ? o.expectedDelivery : null),
    done: i <= current,
    expected: s === 'DELIVERED' && i > current
  }))
})

async function cancel() {
  if (!reason.value) return ui.error('Please select a reason for cancellation')
  cancelling.value = true
  try {
    order.value = await orderApi.cancel(order.value.id, comment.value.trim() ? `${reason.value}: ${comment.value.trim()}` : reason.value)
    cancelOpen.value = false
    ui.notify(order.value.paymentStatus === 'REFUNDED' ? 'Order cancelled. Refund has been initiated.' : 'Your order has been cancelled')
  } catch (e) {
    ui.error(e)
  } finally {
    cancelling.value = false
  }
}
</script>

<template>
  <div class="container detail-page">
    <nav class="breadcrumbs" aria-label="Breadcrumb">
      <RouterLink to="/">Home</RouterLink>
      <Icon name="chevron-right" :size="14" />
      <RouterLink to="/account">My Account</RouterLink>
      <Icon name="chevron-right" :size="14" />
      <RouterLink to="/account/orders">My Orders</RouterLink>
      <Icon name="chevron-right" :size="14" />
      <span>{{ order?.orderNumber }}</span>
    </nav>

    <div v-if="missing" class="card"><EmptyState image="box" title="Order not found" /></div>
    <div v-else-if="!order" class="card center-box"><div class="spinner" /></div>

    <template v-else>
      <div class="card top">
        <div class="addr">
          <h3>Delivery Address</h3>
          <p class="name">{{ order.address.name }}</p>
          <p>{{ order.address.address }}, {{ order.address.locality }}</p>
          <p>{{ order.address.city }}, {{ order.address.state }} - {{ order.address.pincode }}</p>
          <p class="phone"><b>Phone number</b> {{ order.address.mobile }}</p>
        </div>
        <div class="meta">
          <h3>Order Details</h3>
          <p><span class="muted">Order ID</span> {{ order.orderNumber }}</p>
          <p><span class="muted">Placed on</span> {{ formatDate(order.createdAt, { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) }}</p>
          <p><span class="muted">Payment</span> {{ PAYMENT_LABEL[order.paymentMethod] }}
            <span class="tag" :class="order.paymentStatus === 'PAID' ? 'tag-green' : order.paymentStatus === 'REFUNDED' ? 'tag-blue' : 'tag-amber'">{{ order.paymentStatus }}</span>
          </p>
        </div>
        <div class="actions">
          <h3>More actions</h3>
          <button v-if="canCancel" class="btn btn-outline btn-sm" @click="cancelOpen = true">Cancel Order</button>
          <p v-else-if="order.status === 'CANCELLED'" class="muted">This order was cancelled. {{ order.paymentStatus === 'REFUNDED' ? 'Refund initiated to the original payment method.' : '' }}</p>
          <p v-else class="muted">This order can no longer be cancelled.</p>
        </div>
      </div>

      <div class="card track">
        <ol class="timeline">
          <li v-for="s in steps" :key="s.key" :class="{ done: s.done, bad: s.bad }">
            <span class="node"><Icon v-if="s.done" name="check" :size="12" /></span>
            <p class="t-label">{{ s.label }}</p>
            <p v-if="s.at" class="t-date">{{ s.expected ? 'Expected by ' : '' }}{{ formatDate(s.at, { weekday: 'short', day: 'numeric', month: 'short' }) }}</p>
          </li>
        </ol>
        <p v-if="order.cancelReason" class="muted reason">Reason: {{ order.cancelReason }}</p>
      </div>

      <div class="card items">
        <article v-for="i in order.items" :key="i.productId" class="item">
          <RouterLink :to="{ name: 'product', params: { id: i.productId } }"><img :src="i.thumbnail" :alt="i.title" /></RouterLink>
          <div class="i-info">
            <RouterLink :to="{ name: 'product', params: { id: i.productId } }" class="i-title">{{ i.title }}</RouterLink>
            <p class="muted small">{{ i.brand }} · Qty: {{ i.quantity }}</p>
            <p class="i-price">{{ rupees(i.price * i.quantity) }} <span v-if="i.mrp > i.price" class="mrp">{{ rupees(i.mrp * i.quantity) }}</span></p>
          </div>
          <RouterLink v-if="order.status === 'DELIVERED'" :to="{ name: 'product', params: { id: i.productId } }" class="link-btn rate">
            <Icon name="star" :size="16" /> Rate &amp; Review Product
          </RouterLink>
        </article>
      </div>

      <div class="card prices">
        <h3>Price Details</h3>
        <div class="p-line"><span>List price</span><span class="mrp">{{ rupees(order.totalMrp) }}</span></div>
        <div class="p-line"><span>Selling price</span><span>{{ rupees(order.totalMrp - order.totalDiscount) }}</span></div>
        <div class="p-line"><span>Delivery Charges</span><span :class="{ green: !order.deliveryCharge }">{{ order.deliveryCharge ? rupees(order.deliveryCharge) : 'Free' }}</span></div>
        <div class="p-line total"><span>Total Amount</span><span>{{ rupees(order.totalAmount) }}</span></div>
        <p class="green saved">You saved {{ rupees(order.totalDiscount) }} on this order</p>
      </div>
    </template>

    <Transition name="fade">
      <div v-if="cancelOpen" class="overlay" @mousedown.self="cancelOpen = false">
        <form class="card modal" @submit.prevent="cancel">
          <h3>Cancel Order</h3>
          <p class="muted">Please tell us why you are cancelling this order.</p>
          <label class="field">
            <select v-model="reason">
              <option value="" disabled>Select reason</option>
              <option v-for="r in REASONS" :key="r">{{ r }}</option>
            </select>
            <span>Reason for cancellation</span>
          </label>
          <label class="field">
            <textarea v-model="comment" maxlength="200" placeholder=" " />
            <span>Comments (optional)</span>
          </label>
          <div class="row gap-12 m-actions">
            <button type="button" class="btn btn-outline btn-sm" @click="cancelOpen = false">Don't cancel</button>
            <button class="btn btn-orange btn-sm" :disabled="cancelling">{{ cancelling ? 'Cancelling…' : 'Confirm cancellation' }}</button>
          </div>
        </form>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.detail-page {
  padding-top: 12px;
  padding-bottom: 16px;
  max-width: 1100px;
}

.breadcrumbs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  color: var(--text-2);
  font-size: 12px;
  margin-bottom: 12px;
}

.card + .card {
  margin-top: 12px;
}

.top {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
}

.top > div {
  padding: 20px 24px;
}

.top > div + div {
  border-left: 1px solid var(--border-light);
}

.top h3 {
  font-size: 16px;
  margin-bottom: 12px;
}

.top p {
  margin-top: 4px;
}

.name {
  font-weight: 600;
}

.phone {
  margin-top: 10px !important;
}

.meta p {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.meta .muted {
  width: 70px;
}

.actions .btn {
  margin-top: 4px;
}

.track {
  padding: 28px 40px 22px;
}

.timeline {
  display: flex;
  margin: 0;
  padding: 0;
  list-style: none;
}

.timeline li {
  position: relative;
  flex: 1;
  text-align: center;
}

.timeline li::before {
  content: '';
  position: absolute;
  top: 9px;
  left: -50%;
  width: 100%;
  height: 4px;
  background: #e0e0e0;
}

.timeline li:first-child::before {
  display: none;
}

.timeline li.done::before {
  background: var(--fk-green-light);
}

.timeline li.bad::before {
  background: #ff6161;
}

.node {
  position: relative;
  z-index: 1;
  width: 22px;
  height: 22px;
  margin: 0 auto;
  border-radius: 50%;
  background: #e0e0e0;
  color: #fff;
  display: grid;
  place-items: center;
}

.done .node {
  background: var(--fk-green-light);
}

.bad .node {
  background: #ff6161;
}

.t-label {
  margin-top: 10px;
  font-weight: 600;
  font-size: 14px;
}

.t-date {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-2);
}

.reason {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
}

.item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-light);
}

.item:last-child {
  border-bottom: none;
}

.item img {
  width: 76px;
  height: 76px;
  object-fit: contain;
}

.i-info {
  flex: 1;
  min-width: 0;
}

.i-title:hover {
  color: var(--fk-blue);
}

.small {
  font-size: 12px;
  margin-top: 4px;
}

.i-price {
  margin-top: 8px;
  font-weight: 600;
  font-size: 16px;
}

.i-price .mrp {
  font-size: 13px;
  font-weight: 400;
  margin-left: 6px;
}

.rate {
  display: flex;
  align-items: center;
  gap: 4px;
}

.prices {
  padding: 20px 24px;
  max-width: 420px;
  margin-left: auto;
}

.prices h3 {
  font-size: 16px;
  color: var(--text-2);
  text-transform: uppercase;
  margin-bottom: 8px;
}

.p-line {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.p-line.total {
  border-top: 1px dashed var(--border);
  margin-top: 6px;
  padding-top: 14px;
  font-weight: 600;
  font-size: 16px;
}

.saved {
  margin-top: 8px;
  font-weight: 600;
}

.overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.55);
  display: grid;
  place-items: center;
  padding: 16px;
}

.modal {
  width: 100%;
  max-width: 440px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.modal h3 {
  font-size: 18px;
}

.m-actions {
  justify-content: flex-end;
  margin-top: 6px;
}

@media (max-width: 800px) {
  .top { grid-template-columns: 1fr; }
  .top > div + div { border-left: none; border-top: 1px solid var(--border-light); }
  .track { padding: 24px 12px; }
  .t-label { font-size: 12px; }
}
</style>
