<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { useUiStore } from '../stores/ui'
import { userApi } from '../api'
import { deliveryDate, FREE_DELIVERY_THRESHOLD } from '../utils/format'
import PriceDetails from '../components/PriceDetails.vue'
import PriceLine from '../components/PriceLine.vue'
import EmptyState from '../components/EmptyState.vue'
import Icon from '../components/Icon.vue'

const router = useRouter()
const cart = useCartStore()
const auth = useAuthStore()
const ui = useUiStore()

const address = ref(null)
const busy = ref(null) // productId being changed

onMounted(async () => {
  cart.load()
  if (auth.isLoggedIn) {
    try {
      const list = await userApi.addresses()
      address.value = list.find((a) => a.defaultAddress) || list[0] || null
    } catch {
      /* ignore */
    }
  }
})

async function run(productId, fn) {
  busy.value = productId
  await fn()
  busy.value = null
}

const qty = (line, delta) => run(line.productId, () => cart.updateQuantity(line.productId, line.quantity + delta))

function placeOrder() {
  if (cart.hasUnavailable) {
    ui.error('Some items in your cart are out of stock. Please remove them or save for later.')
    return
  }
  if (!auth.isLoggedIn) ui.openLogin(() => router.push('/checkout'))
  else router.push('/checkout')
}

const freeDeliveryGap = computed(() => {
  const total = cart.summary.totalAmount - cart.summary.deliveryCharge
  return total > 0 && total < FREE_DELIVERY_THRESHOLD ? FREE_DELIVERY_THRESHOLD - total : 0
})
</script>

<template>
  <div class="container cart-page">
    <div v-if="cart.loading && !cart.loaded" class="card center-box"><div class="spinner" /></div>

    <div v-else-if="!cart.items.length && !cart.savedForLater.length" class="card">
      <EmptyState
        title="Your cart is empty!"
        :text="auth.isLoggedIn ? 'Add items to it now.' : 'Login to see the items you added previously'"
      >
        <button v-if="!auth.isLoggedIn" class="btn btn-orange" style="width: 220px" @click="ui.openLogin()">Login</button>
        <RouterLink v-else to="/" class="btn btn-blue" style="width: 220px">Shop now</RouterLink>
      </EmptyState>
    </div>

    <div v-else class="layout">
      <div class="left">
        <section class="card">
          <div class="tabs">
            <span class="tab on">Flipkart ({{ cart.items.length }})</span>
          </div>

          <div v-if="auth.isLoggedIn" class="deliver">
            <div v-if="address">
              Deliver to: <b>{{ address.name }}, {{ address.pincode }}</b>
              <span class="type">{{ address.type }}</span>
              <p class="muted addr">{{ address.address }}, {{ address.locality }}, {{ address.city }}</p>
            </div>
            <div v-else class="muted">Add a delivery address at checkout</div>
            <RouterLink to="/account/addresses" class="btn btn-outline btn-sm change">{{ address ? 'Change' : 'Add' }}</RouterLink>
          </div>

          <div v-if="freeDeliveryGap" class="nudge">
            <Icon name="truck" :size="18" /> Add items worth <b>₹{{ freeDeliveryGap }}</b> more for FREE delivery
          </div>

          <div v-if="!cart.items.length" class="center-box muted">No items in your cart. Move items from "Saved For Later".</div>

          <article v-for="line in cart.items" :key="line.productId" class="item" :class="{ dim: busy === line.productId }">
            <div class="thumb-col">
              <RouterLink :to="{ name: 'product', params: { id: line.productId } }" class="thumb">
                <img :src="line.product.thumbnail" :alt="line.product.title" />
              </RouterLink>
              <div class="qty" role="group" :aria-label="`Quantity of ${line.product.title}`">
                <button :disabled="line.quantity <= 1 || !line.inStock" aria-label="Decrease quantity" @click="qty(line, -1)">
                  <Icon name="minus" :size="14" />
                </button>
                <span>{{ line.quantity }}</span>
                <button :disabled="line.quantity >= line.maxQuantity || !line.inStock" aria-label="Increase quantity" @click="qty(line, 1)">
                  <Icon name="plus" :size="14" />
                </button>
              </div>
            </div>
            <div class="info">
              <RouterLink :to="{ name: 'product', params: { id: line.productId } }" class="name">{{ line.product.title }}</RouterLink>
              <p class="muted small">{{ line.product.subCategory }} · Seller: RetailHub</p>
              <PriceLine :price="line.product.price" :mrp="line.product.mrp" :discount="line.product.discount" class="pl" />
              <p v-if="!line.inStock" class="oos">Out of stock</p>
              <p v-else-if="line.product.stock <= 5" class="oos">Only {{ line.product.stock }} left</p>
              <div class="actions">
                <button class="text-btn" @click="run(line.productId, () => cart.saveForLater(line.productId, line.product.title))">Save for later</button>
                <button class="text-btn" @click="run(line.productId, () => cart.remove(line.productId, line.product.title))">Remove</button>
              </div>
            </div>
            <p class="eta">
              Delivery by {{ deliveryDate(3) }} |
              <span v-if="line.product.price >= FREE_DELIVERY_THRESHOLD" class="green">Free</span>
              <span v-else>₹40</span>
            </p>
          </article>

          <div v-if="cart.items.length" class="place-bar">
            <button class="btn btn-orange place" :disabled="cart.hasUnavailable" @click="placeOrder">Place Order</button>
          </div>
        </section>

        <section v-if="cart.savedForLater.length" class="card saved">
          <h2>Saved For Later ({{ cart.savedForLater.length }})</h2>
          <article v-for="line in cart.savedForLater" :key="line.productId" class="item" :class="{ dim: busy === line.productId }">
            <div class="thumb-col">
              <RouterLink :to="{ name: 'product', params: { id: line.productId } }" class="thumb">
                <img :src="line.product.thumbnail" :alt="line.product.title" />
              </RouterLink>
            </div>
            <div class="info">
              <RouterLink :to="{ name: 'product', params: { id: line.productId } }" class="name">{{ line.product.title }}</RouterLink>
              <p class="muted small">{{ line.product.subCategory }}</p>
              <PriceLine :price="line.product.price" :mrp="line.product.mrp" :discount="line.product.discount" class="pl" />
              <p v-if="!line.inStock" class="oos">Out of stock</p>
              <div class="actions">
                <button class="text-btn" :disabled="!line.inStock" @click="run(line.productId, () => cart.moveToCart(line.productId, line.product.title))">Move to cart</button>
                <button class="text-btn" @click="run(line.productId, () => cart.remove(line.productId, line.product.title))">Remove</button>
              </div>
            </div>
          </article>
        </section>
      </div>

      <div class="right">
        <PriceDetails v-if="cart.items.length" :summary="cart.summary" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  padding-top: 16px;
  padding-bottom: 16px;
  max-width: 1200px;
}

.layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.right {
  width: 380px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-h) + 16px);
}

.tabs {
  display: flex;
  border-bottom: 1px solid var(--border-light);
}

.tab {
  flex: 1;
  padding: 16px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}

.tab.on {
  color: var(--fk-blue);
  border-bottom: 2px solid var(--fk-blue);
}

.deliver {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
}

.type {
  margin-left: 8px;
  padding: 2px 6px;
  border-radius: 2px;
  background: #f0f0f0;
  color: var(--text-2);
  font-size: 11px;
  font-weight: 600;
}

.addr {
  margin-top: 4px;
  font-size: 12px;
}

.change {
  color: var(--fk-blue);
}

.nudge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #f5faff;
  border-bottom: 1px solid var(--border-light);
  font-size: 13px;
}

.nudge .icon {
  color: var(--fk-blue);
}

.item {
  display: flex;
  gap: 24px;
  padding: 24px;
  border-bottom: 1px solid var(--border-light);
  transition: opacity 0.15s;
}

.item.dim {
  opacity: 0.5;
  pointer-events: none;
}

.thumb-col {
  width: 112px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.thumb {
  width: 112px;
  height: 112px;
  display: grid;
  place-items: center;
}

.thumb img {
  max-height: 112px;
  object-fit: contain;
}

.qty {
  display: flex;
  align-items: center;
  gap: 6px;
}

.qty button {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #c2c2c2;
  background: #fff;
  display: grid;
  place-items: center;
}

.qty button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.qty span {
  width: 46px;
  height: 28px;
  display: grid;
  place-items: center;
  border: 1px solid #c2c2c2;
  border-radius: 2px;
  font-weight: 500;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 16px;
}

.name:hover {
  color: var(--fk-blue);
}

.small {
  font-size: 13px;
  margin-top: 6px;
}

.pl {
  margin-top: 12px;
}

.oos {
  margin-top: 6px;
  color: #ff6161;
  font-size: 13px;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 24px;
  margin-top: 18px;
}

.eta {
  width: 220px;
  flex-shrink: 0;
  font-size: 13px;
}

.place-bar {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  padding: 16px 22px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.place {
  width: 250px;
  height: 52px;
  font-size: 16px;
}

.saved h2 {
  padding: 16px 24px;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid var(--border-light);
}

@media (max-width: 960px) {
  .layout { flex-direction: column; }
  .right { width: 100%; position: static; }
  .eta { display: none; }
}

@media (max-width: 560px) {
  .item { gap: 14px; padding: 16px; }
  .thumb-col, .thumb { width: 84px; }
  .place { width: 100%; }
}
</style>
