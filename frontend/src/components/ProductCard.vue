<script setup>
import { number } from '../utils/format'
import RatingBadge from './RatingBadge.vue'
import AssuredBadge from './AssuredBadge.vue'
import WishlistHeart from './WishlistHeart.vue'
import PriceLine from './PriceLine.vue'

// Grid tile used for fashion, home, beauty, grocery listings.
defineProps({ product: { type: Object, required: true } })
</script>

<template>
  <RouterLink :to="{ name: 'product', params: { id: product.id } }" class="pcard">
    <div class="img">
      <img :src="product.thumbnail" :alt="product.title" loading="lazy" />
      <WishlistHeart class="wish" :product-id="product.id" />
      <span v-if="product.stock === 0" class="sold">Sold Out</span>
    </div>
    <div class="info">
      <p class="brand">{{ product.brand }}</p>
      <p class="title" :title="product.title">{{ product.title }}</p>
      <div v-if="product.rating" class="row gap-8 rate">
        <RatingBadge :value="product.rating" />
        <span class="muted">({{ number(product.ratingCount) }})</span>
        <AssuredBadge v-if="product.assured" />
      </div>
      <PriceLine :price="product.price" :mrp="product.mrp" :discount="product.discount" size="sm" />
      <p v-if="product.stock > 0 && product.stock <= 5" class="few">Only {{ product.stock }} left</p>
      <p v-else-if="product.price >= 499" class="free">Free delivery</p>
    </div>
  </RouterLink>
</template>

<style scoped>
.pcard {
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #fff;
  transition: box-shadow 0.15s;
  height: 100%;
}

.pcard:hover {
  box-shadow: 0 3px 16px 0 rgba(0, 0, 0, 0.11);
  z-index: 1;
}

.pcard:hover .title {
  color: var(--fk-blue);
}

.img {
  position: relative;
  height: 220px;
  display: grid;
  place-items: center;
  margin-bottom: 12px;
}

.img img {
  max-height: 220px;
  max-width: 100%;
  object-fit: contain;
}

.wish {
  position: absolute;
  top: 0;
  right: 0;
}

.sold {
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  padding: 3px 10px;
  border-radius: 2px;
  background: rgba(33, 33, 33, 0.8);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.brand {
  color: var(--text-2);
  font-size: 13px;
  font-weight: 500;
}

.title {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rate {
  font-size: 13px;
}

.free {
  font-size: 12px;
  color: var(--text);
}

.few {
  font-size: 12px;
  color: #ff6161;
  font-weight: 500;
}
</style>
