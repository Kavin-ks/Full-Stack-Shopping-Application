<script setup>
import { rupees } from '../utils/format'
defineProps({
  price: Number,
  mrp: Number,
  discount: Number,
  size: { type: String, default: 'md' } // sm | md | lg
})
</script>

<template>
  <div class="price-line" :class="size">
    <span class="price">{{ rupees(price) }}</span>
    <span v-if="mrp > price" class="mrp">{{ rupees(mrp) }}</span>
    <span v-if="discount > 0" class="off">{{ discount }}% off</span>
  </div>
</template>

<style scoped>
.price-line {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 8px;
}

.sm .price { font-size: 16px; }
.sm .mrp, .sm .off { font-size: 13px; }
.md .price { font-size: 18px; }
.md .mrp, .md .off { font-size: 14px; }
.lg .price { font-size: 28px; font-weight: 600; }
.lg .mrp, .lg .off { font-size: 16px; }
.lg { gap: 12px; }
</style>
