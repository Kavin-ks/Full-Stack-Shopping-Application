<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '../api'
import { rupees, formatDate, PAYMENT_LABEL } from '../utils/format'
import Icon from '../components/Icon.vue'

const route = useRoute()
const order = ref(null)

onMounted(async () => {
  try {
    order.value = await orderApi.get(route.params.id)
  } catch {
    /* handled by empty state */
  }
})
</script>

<template>
  <div class="container success-page">
    <div class="card box">
      <div v-if="!order" class="center-box"><div class="spinner" /></div>
      <template v-else>
        <div class="hero">
          <span class="tick"><Icon name="check" :size="40" /></span>
          <h1>Order placed successfully!</h1>
          <p>
            Your order <b>{{ order.orderNumber }}</b> will be delivered by
            <b>{{ formatDate(order.expectedDelivery, { weekday: 'long', day: 'numeric', month: 'long' }) }}</b>
          </p>
        </div>

        <div class="details">
          <div>
            <p class="label">Delivering to</p>
            <p><b>{{ order.address.name }}</b> · {{ order.address.mobile }}</p>
            <p class="muted">{{ order.address.address }}, {{ order.address.locality }}, {{ order.address.city }} - {{ order.address.pincode }}</p>
          </div>
          <div>
            <p class="label">Payment</p>
            <p>{{ PAYMENT_LABEL[order.paymentMethod] }}</p>
            <p class="muted">{{ order.paymentMethod === 'COD' ? `Pay ${rupees(order.totalAmount)} on delivery` : `${rupees(order.totalAmount)} paid` }}</p>
          </div>
        </div>

        <ul class="items">
          <li v-for="i in order.items" :key="i.productId">
            <img :src="i.thumbnail" :alt="i.title" />
            <div>
              <p>{{ i.title }}</p>
              <p class="muted">Qty: {{ i.quantity }} · {{ rupees(i.price * i.quantity) }}</p>
            </div>
          </li>
        </ul>

        <div class="buttons">
          <RouterLink :to="{ name: 'order', params: { id: order.id } }" class="btn btn-outline">Track Order</RouterLink>
          <RouterLink to="/" class="btn btn-orange">Continue Shopping</RouterLink>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.success-page {
  padding-top: 24px;
  padding-bottom: 24px;
  max-width: 820px;
}

.hero {
  text-align: center;
  padding: 40px 24px 28px;
  border-bottom: 1px solid var(--border-light);
}

.tick {
  width: 72px;
  height: 72px;
  margin: 0 auto 18px;
  border-radius: 50%;
  background: var(--fk-green-light);
  color: #fff;
  display: grid;
  place-items: center;
  animation: pop 0.4s ease;
}

@keyframes pop {
  0% { transform: scale(0.4); opacity: 0; }
  70% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}

h1 {
  font-size: 22px;
  margin-bottom: 10px;
}

.details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  padding: 20px 32px;
  border-bottom: 1px solid var(--border-light);
}

.details p {
  margin-top: 4px;
}

.label {
  color: var(--text-2);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.items {
  padding: 8px 32px;
}

.items li {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
}

.items img {
  width: 56px;
  height: 56px;
  object-fit: contain;
}

.buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 16px 24px 32px;
}

@media (max-width: 600px) {
  .details { grid-template-columns: 1fr; padding: 20px; }
  .items { padding: 8px 20px; }
}
</style>
