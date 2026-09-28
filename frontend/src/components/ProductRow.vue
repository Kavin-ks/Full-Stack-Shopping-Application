<script setup>
import { number } from '../utils/format'
import RatingBadge from './RatingBadge.vue'
import AssuredBadge from './AssuredBadge.vue'
import WishlistHeart from './WishlistHeart.vue'
import PriceLine from './PriceLine.vue'

// List row used for mobiles / electronics, like Flipkart's search results.
defineProps({ product: { type: Object, required: true } })
</script>

<template>
  <RouterLink :to="{ name: 'product', params: { id: product.id } }" class="prow">
    <div class="img">
      <img :src="product.thumbnail" :alt="product.title" loading="lazy" />
      <WishlistHeart class="wish" :product-id="product.id" />
    </div>
    <div class="mid">
      <p class="title">{{ product.title }}</p>
      <div v-if="product.rating" class="row gap-8 rate">
        <RatingBadge :value="product.rating" />
        <span class="muted">{{ number(product.ratingCount) }} Ratings &amp; {{ number(product.reviewCount) }} Reviews</span>
      </div>
      <ul class="highlights">
        <li v-for="h in product.highlights?.slice(0, 5)" :key="h">{{ h }}</li>
      </ul>
    </div>
    <div class="right">
      <div class="row gap-8">
        <PriceLine :price="product.price" :mrp="product.mrp" :discount="product.discount" size="lg" class="big" />
        <AssuredBadge v-if="product.assured" />
      </div>
      <p v-if="product.stock === 0" class="oos">Currently unavailable</p>
      <p v-else-if="product.stock <= 5" class="few">Only {{ product.stock }} left</p>
      <p v-else class="free">Free delivery</p>
      <p class="offer">Bank Offer</p>
    </div>
  </RouterLink>
</template>

<style scoped>
.prow {
  display: flex;
  gap: 24px;
  padding: 24px;
  background: #fff;
  border-bottom: 1px solid var(--border-light);
}

.prow:hover .title {
  color: var(--fk-blue);
}

.img {
  position: relative;
  width: 200px;
  height: 200px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
}

.img img {
  max-width: 100%;
  max-height: 200px;
  object-fit: contain;
}

.wish {
  position: absolute;
  top: 0;
  right: 0;
}

.mid {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 8px;
}

.rate {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}

.highlights li {
  position: relative;
  padding-left: 16px;
  margin-bottom: 6px;
  color: #212121;
  font-size: 14px;
}

.highlights li::before {
  content: '';
  position: absolute;
  left: 2px;
  top: 8px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #c2c2c2;
}

.right {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.big :deep(.price) {
  font-size: 25px;
}

.big {
  flex-direction: column;
  gap: 2px;
  align-items: flex-start;
}

.free {
  font-size: 12px;
}

.few,
.oos {
  font-size: 13px;
  color: #ff6161;
  font-weight: 500;
}

.offer {
  font-size: 12px;
  color: var(--fk-green);
  font-weight: 600;
}

@media (max-width: 760px) {
  .prow { flex-wrap: wrap; padding: 16px; gap: 12px; }
  .img { width: 110px; height: 110px; }
  .img img { max-height: 110px; }
  .mid { flex: 1 1 calc(100% - 130px); }
  .right { width: 100%; }
  .highlights { display: none; }
}
</style>
