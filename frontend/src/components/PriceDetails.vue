<script setup>
import { rupees } from '../utils/format'
import Icon from './Icon.vue'

defineProps({ summary: { type: Object, required: true } })
</script>

<template>
  <aside class="price-details card">
    <h3>Price Details</h3>
    <div class="body">
      <div class="line">
        <span>Price ({{ summary.itemCount }} item{{ summary.itemCount === 1 ? '' : 's' }})</span>
        <span>{{ rupees(summary.totalMrp) }}</span>
      </div>
      <div class="line">
        <span>Discount</span>
        <span class="green">− {{ rupees(summary.totalDiscount) }}</span>
      </div>
      <div class="line">
        <span>Delivery Charges</span>
        <span v-if="summary.deliveryCharge === 0" class="green">
          <span class="mrp small">₹40</span> Free
        </span>
        <span v-else>{{ rupees(summary.deliveryCharge) }}</span>
      </div>
      <div class="line total">
        <span>Total Amount</span>
        <span>{{ rupees(summary.totalAmount) }}</span>
      </div>
      <p v-if="summary.savings > 0" class="save">You will save {{ rupees(summary.savings) }} on this order</p>
    </div>
    <p class="secure">
      <Icon name="shield" :size="26" />
      Safe and Secure Payments. Easy returns. 100% Authentic products.
    </p>
  </aside>
</template>

<style scoped>
h3 {
  padding: 13px 24px;
  border-bottom: 1px solid var(--border-light);
  color: var(--text-2);
  font-size: 16px;
  font-weight: 600;
  text-transform: uppercase;
}

.body {
  padding: 0 24px;
}

.line {
  display: flex;
  justify-content: space-between;
  margin: 20px 0;
  font-size: 16px;
}

.total {
  padding: 20px 0;
  border-top: 1px dashed var(--border);
  border-bottom: 1px dashed var(--border);
  font-weight: 600;
  font-size: 18px;
}

.small {
  font-size: 14px;
  margin-right: 4px;
}

.save {
  padding: 12px 0 20px;
  color: var(--fk-green);
  font-weight: 600;
  font-size: 16px;
}

.secure {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  background: var(--bg);
  color: var(--text-2);
  font-size: 13px;
  font-weight: 600;
}

.secure .icon {
  color: #878787;
}
</style>
