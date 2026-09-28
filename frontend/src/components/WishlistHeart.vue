<script setup>
import { computed } from 'vue'
import { useWishlistStore } from '../stores/wishlist'

const props = defineProps({ productId: { type: String, required: true }, size: { type: Number, default: 16 } })
const wishlist = useWishlistStore()
const active = computed(() => wishlist.has(props.productId))
</script>

<template>
  <button
    class="heart"
    :class="{ active }"
    :aria-label="active ? 'Remove from wishlist' : 'Add to wishlist'"
    :aria-pressed="active"
    @click.prevent.stop="wishlist.toggle(productId)"
  >
    <svg :width="size" :height="size" viewBox="0 0 24 24">
      <path d="M12 20.3 10.6 19C5.4 14.4 2 11.3 2 7.5 2 4.4 4.4 2 7.5 2c1.7 0 3.4.8 4.5 2.1A6 6 0 0 1 16.5 2C19.6 2 22 4.4 22 7.5c0 3.8-3.4 6.9-8.6 11.5L12 20.3z" />
    </svg>
  </button>
</template>

<style scoped>
.heart {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #f0f0f0;
  background: #fff;
  display: grid;
  place-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.heart path {
  fill: #c2c2c2;
  transition: fill 0.15s;
}

.heart:hover path {
  fill: #ff4343;
  opacity: 0.6;
}

.heart.active path {
  fill: #ff4343;
  opacity: 1;
}
</style>
