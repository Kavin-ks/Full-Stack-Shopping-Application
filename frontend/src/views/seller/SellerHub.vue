<script setup>
import { ref, computed, onMounted } from 'vue'
import { productApi, orderApi } from '../../api'
import { useUiStore } from '../../stores/ui'
import { useCatalogStore } from '../../stores/catalog'
import { rupees, number, formatDate, ORDER_STATUS, PAYMENT_LABEL } from '../../utils/format'
import Icon from '../../components/Icon.vue'
import ProductForm from './ProductForm.vue'

const ui = useUiStore()
const catalog = useCatalogStore()

const tab = ref('dashboard')
const products = ref([])
const stats = ref(null)
const orders = ref({ content: [], totalPages: 0, page: 0 })
const loading = ref(true)
const search = ref('')
const stockFilter = ref('all')
const drafts = ref({}) // productId -> edited stock
const formOpen = ref(false)
const editing = ref(null)
const nextStatus = ref({})

const LOW_STOCK = 5
const NEXT = {
  PLACED: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['OUT_FOR_DELIVERY', 'CANCELLED'],
  OUT_FOR_DELIVERY: ['DELIVERED']
}

onMounted(async () => {
  await Promise.all([loadProducts(), loadStats(), loadOrders(0)])
  loading.value = false
})

async function loadProducts() {
  const all = []
  let page = 0
  let totalPages = 1
  try {
    while (page < totalPages) {
      const res = await productApi.search({ sort: 'newest', page, size: 60 })
      all.push(...res.content)
      totalPages = res.totalPages
      page++
    }
    products.value = all
  } catch (e) {
    ui.error(e)
  }
}

async function loadStats() {
  try {
    stats.value = await orderApi.stats()
  } catch (e) {
    ui.error(e)
  }
}

async function loadOrders(page) {
  try {
    orders.value = await orderApi.all(page, 15)
  } catch (e) {
    ui.error(e)
  }
}

// ---------------------------------------------------------------- inventory
const lowStock = computed(() => products.value.filter((p) => p.stock > 0 && p.stock <= LOW_STOCK))
const outOfStock = computed(() => products.value.filter((p) => p.stock === 0))
const inventoryValue = computed(() => products.value.reduce((s, p) => s + p.price * p.stock, 0))

const filtered = computed(() => {
  const term = search.value.trim().toLowerCase()
  return products.value.filter((p) => {
    if (stockFilter.value === 'low' && !(p.stock > 0 && p.stock <= LOW_STOCK)) return false
    if (stockFilter.value === 'out' && p.stock !== 0) return false
    return !term || p.title.toLowerCase().includes(term) || p.brand.toLowerCase().includes(term) || p.id.toLowerCase().includes(term)
  })
})

function stockTag(p) {
  if (p.stock === 0) return { cls: 'tag-red', text: 'Out of stock' }
  if (p.stock <= LOW_STOCK) return { cls: 'tag-amber', text: 'Low stock' }
  return { cls: 'tag-green', text: 'In stock' }
}

async function saveStock(p) {
  const value = Number(drafts.value[p.id])
  if (!Number.isInteger(value) || value < 0) return ui.error('Stock must be a whole number ≥ 0')
  try {
    const updated = await productApi.updateStock(p.id, value)
    Object.assign(p, { stock: updated.stock })
    delete drafts.value[p.id]
    ui.notify(`Stock for ${p.title} updated to ${value}`)
  } catch (e) {
    ui.error(e)
  }
}

async function restock(p, amount = 25) {
  drafts.value[p.id] = p.stock + amount
  await saveStock(p)
}

function openNew() {
  editing.value = null
  formOpen.value = true
}

async function openEdit(p) {
  try {
    editing.value = await productApi.get(p.id)
    formOpen.value = true
  } catch (e) {
    ui.error(e)
  }
}

async function saveProduct(data) {
  try {
    const saved = editing.value ? await productApi.update(editing.value.id, data) : await productApi.create(data)
    const i = products.value.findIndex((p) => p.id === saved.id)
    if (i >= 0) products.value[i] = saved
    else products.value.unshift(saved)
    ui.notify(editing.value ? 'Product updated' : 'Product listed successfully')
    formOpen.value = false
    editing.value = null
  } catch (e) {
    ui.error(e)
  }
}

async function remove(p) {
  if (!confirm(`Delete "${p.title}" from the catalogue? This cannot be undone.`)) return
  try {
    await productApi.remove(p.id)
    products.value = products.value.filter((x) => x.id !== p.id)
    ui.notify('Product deleted')
  } catch (e) {
    ui.error(e)
  }
}

// ---------------------------------------------------------------- orders
async function updateStatus(o) {
  const status = nextStatus.value[o.id]
  if (!status) return
  try {
    const updated = await orderApi.updateStatus(o.id, status)
    Object.assign(o, updated)
    delete nextStatus.value[o.id]
    ui.notify(`Order ${o.orderNumber} marked as ${ORDER_STATUS[status].label}`)
    loadStats()
    if (status === 'CANCELLED') loadProducts() // stock was released
  } catch (e) {
    ui.error(e)
  }
}

const statusBars = computed(() => {
  if (!stats.value) return []
  const max = Math.max(1, ...Object.values(stats.value.byStatus))
  return Object.entries(stats.value.byStatus).map(([k, v]) => ({ key: k, count: v, pct: (v / max) * 100, ...ORDER_STATUS[k] }))
})
</script>

<template>
  <div class="container seller">
    <div class="head">
      <div>
        <h1><Icon name="store" :size="26" /> Seller Hub</h1>
        <p class="muted">Manage your catalogue, stock and orders</p>
      </div>
      <button class="btn btn-blue btn-sm" @click="openNew"><Icon name="plus" :size="18" /> Add Product</button>
    </div>

    <nav class="tabs card">
      <button :class="{ on: tab === 'dashboard' }" @click="tab = 'dashboard'"><Icon name="chart" :size="18" /> Dashboard</button>
      <button :class="{ on: tab === 'inventory' }" @click="tab = 'inventory'"><Icon name="box" :size="18" /> Inventory ({{ products.length }})</button>
      <button :class="{ on: tab === 'orders' }" @click="tab = 'orders'"><Icon name="truck" :size="18" /> Orders ({{ stats?.totalOrders ?? 0 }})</button>
    </nav>

    <div v-if="loading" class="card center-box"><div class="spinner" /></div>

    <!-- Dashboard -->
    <template v-else-if="tab === 'dashboard'">
      <div class="stats">
        <div class="card stat"><p class="muted">Total Revenue</p><p class="val">{{ rupees(stats?.revenue) }}</p><p class="sub">excluding cancelled orders</p></div>
        <div class="card stat"><p class="muted">Total Orders</p><p class="val">{{ number(stats?.totalOrders) }}</p><p class="sub">{{ stats?.activeOrders }} active</p></div>
        <div class="card stat"><p class="muted">Products Listed</p><p class="val">{{ products.length }}</p><p class="sub">Inventory value {{ rupees(inventoryValue) }}</p></div>
        <div class="card stat warn" @click="tab = 'inventory'; stockFilter = 'low'"><p class="muted">Low Stock</p><p class="val">{{ lowStock.length }}</p><p class="sub">≤ {{ LOW_STOCK }} units left</p></div>
        <div class="card stat bad" @click="tab = 'inventory'; stockFilter = 'out'"><p class="muted">Out of Stock</p><p class="val">{{ outOfStock.length }}</p><p class="sub">not visible to buyers</p></div>
      </div>

      <div class="dash-grid">
        <section class="card panel">
          <h2>Orders by status</h2>
          <ul class="bars">
            <li v-for="b in statusBars" :key="b.key">
              <span>{{ b.label }}</span>
              <span class="bar"><span :style="{ width: b.pct + '%', background: b.color }" /></span>
              <b>{{ b.count }}</b>
            </li>
          </ul>
        </section>

        <section class="card panel">
          <h2>Needs restocking</h2>
          <p v-if="!lowStock.length && !outOfStock.length" class="muted">All products are well stocked.</p>
          <ul class="restock">
            <li v-for="p in [...outOfStock, ...lowStock].slice(0, 8)" :key="p.id">
              <img :src="p.thumbnail" :alt="p.title" />
              <div class="r-info">
                <p class="r-title">{{ p.title }}</p>
                <span class="tag" :class="stockTag(p).cls">{{ p.stock }} left</span>
              </div>
              <button class="btn btn-outline btn-sm" @click="restock(p)">+25</button>
            </li>
          </ul>
        </section>
      </div>
    </template>

    <!-- Inventory -->
    <section v-else-if="tab === 'inventory'" class="card">
      <div class="toolbar">
        <div class="search">
          <Icon name="search" :size="18" class="muted" />
          <input v-model="search" placeholder="Search by name, brand or ID" aria-label="Search products" />
        </div>
        <div class="seg">
          <button :class="{ on: stockFilter === 'all' }" @click="stockFilter = 'all'">All</button>
          <button :class="{ on: stockFilter === 'low' }" @click="stockFilter = 'low'">Low stock ({{ lowStock.length }})</button>
          <button :class="{ on: stockFilter === 'out' }" @click="stockFilter = 'out'">Out of stock ({{ outOfStock.length }})</button>
        </div>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr><th>Product</th><th>Category</th><th class="num">Price</th><th class="num">MRP</th><th>Stock</th><th>Status</th><th /></tr>
          </thead>
          <tbody>
            <tr v-for="p in filtered" :key="p.id">
              <td>
                <div class="prod">
                  <img :src="p.thumbnail" :alt="p.title" />
                  <div>
                    <RouterLink :to="{ name: 'product', params: { id: p.id } }" class="p-title">{{ p.title }}</RouterLink>
                    <p class="muted small">{{ p.id }} · {{ p.brand }}</p>
                  </div>
                </div>
              </td>
              <td>{{ p.subCategory }}</td>
              <td class="num">{{ rupees(p.price) }}</td>
              <td class="num muted">{{ rupees(p.mrp) }}</td>
              <td>
                <form class="stock-edit" @submit.prevent="saveStock(p)">
                  <input
                    type="number"
                    min="0"
                    :value="drafts[p.id] ?? p.stock"
                    :aria-label="`Stock for ${p.title}`"
                    @input="drafts[p.id] = $event.target.value"
                  />
                  <button v-if="drafts[p.id] !== undefined && Number(drafts[p.id]) !== p.stock" class="btn btn-blue btn-sm">Save</button>
                </form>
              </td>
              <td><span class="tag" :class="stockTag(p).cls">{{ stockTag(p).text }}</span></td>
              <td class="row-actions">
                <button aria-label="Edit product" title="Edit" @click="openEdit(p)"><Icon name="edit" :size="18" /></button>
                <button aria-label="Delete product" title="Delete" @click="remove(p)"><Icon name="trash" :size="18" /></button>
              </td>
            </tr>
            <tr v-if="!filtered.length"><td colspan="7" class="center-box muted">No products found</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Orders -->
    <section v-else class="card">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr><th>Order</th><th>Items</th><th class="num">Amount</th><th>Payment</th><th>Status</th><th>Update</th></tr>
          </thead>
          <tbody>
            <tr v-for="o in orders.content" :key="o.id">
              <td>
                <p class="mono">{{ o.orderNumber }}</p>
                <p class="muted small">{{ formatDate(o.createdAt, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }) }}</p>
                <p class="muted small">{{ o.address.name }}, {{ o.address.city }}</p>
              </td>
              <td>
                <p class="clip">{{ o.items[0].title }}</p>
                <p v-if="o.items.length > 1" class="muted small">+{{ o.items.length - 1 }} more item(s)</p>
              </td>
              <td class="num">{{ rupees(o.totalAmount) }}</td>
              <td>
                <p>{{ PAYMENT_LABEL[o.paymentMethod] }}</p>
                <span class="tag" :class="o.paymentStatus === 'PAID' ? 'tag-green' : o.paymentStatus === 'REFUNDED' ? 'tag-blue' : 'tag-amber'">{{ o.paymentStatus }}</span>
              </td>
              <td>
                <span class="status"><span class="dot" :style="{ background: ORDER_STATUS[o.status].color }" />{{ ORDER_STATUS[o.status].label }}</span>
              </td>
              <td>
                <form v-if="NEXT[o.status]" class="status-edit" @submit.prevent="updateStatus(o)">
                  <select v-model="nextStatus[o.id]" :aria-label="`Next status for ${o.orderNumber}`">
                    <option :value="undefined" disabled>Move to…</option>
                    <option v-for="s in NEXT[o.status]" :key="s" :value="s">{{ ORDER_STATUS[s].label }}</option>
                  </select>
                  <button class="btn btn-blue btn-sm" :disabled="!nextStatus[o.id]">Update</button>
                </form>
                <span v-else class="muted small">—</span>
              </td>
            </tr>
            <tr v-if="!orders.content.length"><td colspan="6" class="center-box muted">No orders yet</td></tr>
          </tbody>
        </table>
      </div>
      <div v-if="orders.totalPages > 1" class="pager">
        <button class="btn btn-outline btn-sm" :disabled="orders.page === 0" @click="loadOrders(orders.page - 1)">Previous</button>
        <span class="muted">Page {{ orders.page + 1 }} of {{ orders.totalPages }}</span>
        <button class="btn btn-outline btn-sm" :disabled="orders.page + 1 >= orders.totalPages" @click="loadOrders(orders.page + 1)">Next</button>
      </div>
    </section>

    <ProductForm
      v-if="formOpen"
      :product="editing"
      :categories="catalog.categories"
      @save="saveProduct"
      @close="formOpen = false; editing = null"
    />
  </div>
</template>

<style scoped>
.seller {
  padding-top: 20px;
  padding-bottom: 24px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.head h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
}

.head h1 .icon {
  color: var(--fk-blue);
}

.head p {
  margin-top: 4px;
}

.tabs {
  display: flex;
  margin-bottom: 16px;
  overflow-x: auto;
}

.tabs button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 22px;
  border: none;
  border-bottom: 3px solid transparent;
  background: none;
  font-weight: 600;
  font-size: 14px;
  color: var(--text-2);
  white-space: nowrap;
}

.tabs button.on {
  color: var(--fk-blue);
  border-bottom-color: var(--fk-blue);
}

.stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.stat {
  padding: 18px 20px;
}

.stat.warn,
.stat.bad {
  cursor: pointer;
}

.stat .val {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 700;
}

.stat.warn .val { color: #d97a00; }
.stat.bad .val { color: #e53935; }

.stat .sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-2);
}

.dash-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 12px;
}

.panel {
  padding: 20px;
}

.panel h2 {
  font-size: 16px;
  margin-bottom: 16px;
}

.bars li {
  display: grid;
  grid-template-columns: 140px 1fr 40px;
  align-items: center;
  gap: 12px;
  margin: 12px 0;
  font-size: 14px;
}

.bar {
  height: 10px;
  border-radius: 5px;
  background: #f0f0f0;
  overflow: hidden;
}

.bar span {
  display: block;
  height: 100%;
  border-radius: 5px;
}

.restock li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-light);
}

.restock img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.r-info {
  flex: 1;
  min-width: 0;
}

.r-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
}

.search {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 320px;
  max-width: 100%;
  height: 38px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 4px;
}

.search input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
}

.seg {
  display: flex;
  border: 1px solid var(--border);
  border-radius: 4px;
  overflow: hidden;
}

.seg button {
  padding: 8px 14px;
  border: none;
  border-right: 1px solid var(--border);
  background: #fff;
  font-size: 13px;
  font-weight: 500;
}

.seg button:last-child {
  border-right: none;
}

.seg button.on {
  background: #e8f0fe;
  color: var(--fk-blue);
}

.table-wrap {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th {
  padding: 12px 16px;
  background: #fafafa;
  color: var(--text-2);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  text-align: left;
  white-space: nowrap;
}

.table td {
  padding: 12px 16px;
  border-top: 1px solid var(--border-light);
  vertical-align: middle;
}

.num {
  text-align: right !important;
  white-space: nowrap;
}

.prod {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 260px;
}

.prod img {
  width: 44px;
  height: 44px;
  object-fit: contain;
}

.p-title:hover {
  color: var(--fk-blue);
}

.small {
  font-size: 12px;
  margin-top: 2px;
}

.stock-edit {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stock-edit input {
  width: 76px;
  height: 34px;
  padding: 0 8px;
  border: 1px solid var(--border);
  border-radius: 2px;
}

.stock-edit .btn {
  height: 34px;
  padding: 0 12px;
}

.row-actions {
  white-space: nowrap;
}

.row-actions button {
  border: none;
  background: none;
  color: var(--text-2);
  padding: 6px;
}

.row-actions button:hover {
  color: var(--fk-blue);
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 13px;
}

.clip {
  max-width: 240px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  white-space: nowrap;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.status-edit {
  display: flex;
  gap: 6px;
}

.status-edit select {
  height: 36px;
  padding: 0 8px;
  border: 1px solid var(--border);
  border-radius: 2px;
  background: #fff;
}

.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

@media (max-width: 1000px) {
  .stats { grid-template-columns: repeat(2, 1fr); }
  .dash-grid { grid-template-columns: 1fr; }
}
</style>
