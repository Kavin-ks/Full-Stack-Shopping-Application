<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderApi } from '../../api'
import { useUiStore } from '../../stores/ui'
import { rupees, formatDate, ORDER_STATUS } from '../../utils/format'
import EmptyState from '../../components/EmptyState.vue'
import Icon from '../../components/Icon.vue'

const ui = useUiStore()
const orders = ref([])
const loading = ref(true)
const search = ref('')
const applied = ref('')
const statuses = ref([])
const times = ref([])

const STATUS_FILTERS = [
  { key: 'ONTHEWAY', label: 'On the way', match: (s) => ['PLACED', 'SHIPPED', 'OUT_FOR_DELIVERY'].includes(s) },
  { key: 'DELIVERED', label: 'Delivered', match: (s) => s === 'DELIVERED' },
  { key: 'CANCELLED', label: 'Cancelled', match: (s) => s === 'CANCELLED' }
]
const TIME_FILTERS = [
  { key: '30', label: 'Last 30 days', match: (d) => Date.now() - new Date(d) <= 30 * 864e5 },
  { key: 'YEAR', label: String(new Date().getFullYear()), match: (d) => new Date(d).getFullYear() === new Date().getFullYear() },
  { key: 'OLDER', label: 'Older', match: (d) => new Date(d).getFullYear() < new Date().getFullYear() }
]

onMounted(async () => {
  try {
    orders.value = await orderApi.list()
  } catch (e) {
    ui.error(e)
  } finally {
    loading.value = false
  }
})

// One row per item, like Flipkart's orders page.
const rows = computed(() => {
  const term = applied.value.trim().toLowerCase()
  return orders.value
    .filter((o) => !statuses.value.length || STATUS_FILTERS.some((f) => statuses.value.includes(f.key) && f.match(o.status)))
    .filter((o) => !times.value.length || TIME_FILTERS.some((f) => times.value.includes(f.key) && f.match(o.createdAt)))
    .flatMap((o) => o.items.map((item) => ({ order: o, item })))
    .filter(({ order, item }) => !term || item.title.toLowerCase().includes(term) || order.orderNumber.toLowerCase().includes(term))
})

const short = { weekday: 'short', month: 'short', day: 'numeric' }

function statusText(o) {
  if (o.status === 'DELIVERED') return `Delivered on ${formatDate(o.expectedDelivery, short)}`
  if (o.status === 'CANCELLED') return `Cancelled on ${formatDate(o.statusHistory.at(-1)?.at, short)}`
  return `Delivery expected by ${formatDate(o.expectedDelivery, short)}`
}

function statusNote(o) {
  if (o.status === 'DELIVERED') return 'Your item has been delivered'
  if (o.status === 'CANCELLED') return o.cancelReason
  return ORDER_STATUS[o.status].label
}
</script>

<template>
  <div class="container orders-page">
    <nav class="breadcrumbs" aria-label="Breadcrumb">
      <RouterLink to="/">Home</RouterLink>
      <Icon name="chevron-right" :size="14" />
      <RouterLink to="/account">My Account</RouterLink>
      <Icon name="chevron-right" :size="14" />
      <span>My Orders</span>
    </nav>

    <div class="layout">
      <aside class="card filters">
        <h2>Filters</h2>
        <section>
          <h3>Order Status</h3>
          <label v-for="f in STATUS_FILTERS" :key="f.key" class="check">
            <input v-model="statuses" type="checkbox" :value="f.key" /> {{ f.label }}
          </label>
        </section>
        <section>
          <h3>Order Time</h3>
          <label v-for="f in TIME_FILTERS" :key="f.key" class="check">
            <input v-model="times" type="checkbox" :value="f.key" /> {{ f.label }}
          </label>
        </section>
      </aside>

      <section class="list">
        <form class="search card" @submit.prevent="applied = search">
          <input v-model="search" placeholder="Search your orders here" aria-label="Search your orders" />
          <button class="btn btn-blue btn-sm"><Icon name="search" :size="18" /> Search Orders</button>
        </form>

        <div v-if="loading" class="card center-box"><div class="spinner" /></div>

        <div v-else-if="!orders.length" class="card">
          <EmptyState image="box" title="You have no orders" text="Looks like you haven't placed an order yet.">
            <RouterLink to="/" class="btn btn-blue btn-sm">Start Shopping</RouterLink>
          </EmptyState>
        </div>

        <div v-else-if="!rows.length" class="card center-box muted">No orders match your filters.</div>

        <template v-else>
          <RouterLink
            v-for="{ order, item } in rows"
            :key="order.id + item.productId"
            :to="{ name: 'order', params: { id: order.id } }"
            class="card row-card"
          >
            <img :src="item.thumbnail" :alt="item.title" />
            <div class="title-col">
              <p class="title">{{ item.title }}</p>
              <p class="muted small">Qty: {{ item.quantity }} · {{ order.orderNumber }}</p>
            </div>
            <p class="price-col">{{ rupees(item.price * item.quantity) }}</p>
            <div class="status-col">
              <p class="status">
                <span class="dot" :style="{ background: ORDER_STATUS[order.status].color }" />
                {{ statusText(order) }}
              </p>
              <p class="muted small">{{ statusNote(order) }}</p>
            </div>
          </RouterLink>
        </template>
      </section>
    </div>
  </div>
</template>

<style scoped>
.orders-page {
  padding-top: 12px;
  padding-bottom: 16px;
  max-width: 1200px;
}

.breadcrumbs {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-2);
  font-size: 12px;
  margin-bottom: 12px;
}

.layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.filters {
  width: 260px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-h) + 12px);
}

.filters h2 {
  padding: 16px 20px;
  font-size: 18px;
  border-bottom: 1px solid var(--border-light);
}

.filters section {
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-light);
}

.filters h3 {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.list {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search {
  display: flex;
  overflow: hidden;
}

.search input {
  flex: 1;
  min-width: 0;
  padding: 0 16px;
  border: none;
  outline: none;
  font-size: 14px;
}

.search .btn {
  border-radius: 0;
  height: 42px;
}

.row-card {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px 24px;
  transition: box-shadow 0.15s;
}

.row-card:hover {
  box-shadow: var(--shadow-md);
}

.row-card:hover .title {
  color: var(--fk-blue);
}

.row-card img {
  width: 76px;
  height: 76px;
  object-fit: contain;
  flex-shrink: 0;
}

.title-col {
  flex: 1;
  min-width: 0;
}

.small {
  font-size: 12px;
  margin-top: 6px;
}

.price-col {
  width: 110px;
  font-weight: 500;
}

.status-col {
  width: 260px;
}

.status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  flex-shrink: 0;
}

@media (max-width: 860px) {
  .layout { flex-direction: column; }
  .filters { width: 100%; position: static; display: flex; flex-wrap: wrap; }
  .filters h2 { width: 100%; }
  .filters section { flex: 1; border-bottom: none; }
  .row-card { flex-wrap: wrap; }
  .price-col { width: auto; }
  .status-col { width: 100%; }
}
</style>
