<script setup>
import { ref, watch } from 'vue'
import { productApi } from '../../api'
import { useWishlistStore } from '../../stores/wishlist'
import { number } from '../../utils/format'
import RatingBadge from '../../components/RatingBadge.vue'
import AssuredBadge from '../../components/AssuredBadge.vue'
import PriceLine from '../../components/PriceLine.vue'
import EmptyState from '../../components/EmptyState.vue'
import Icon from '../../components/Icon.vue'

const wishlist = useWishlistStore()
const products = ref([])
const loading = ref(true)

watch(
  () => wishlist.ids.join(','),
  async () => {
    try {
      products.value = wishlist.ids.length ? await productApi.batch(wishlist.ids) : []
    } catch {
      products.value = []
    } finally {
      loading.value = false
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="wishlist">
    <h2>My Wishlist ({{ products.length }})</h2>
    <div v-if="loading" class="center-box"><div class="spinner" /></div>
    <EmptyState v-else-if="!products.length" image="heart" title="Empty Wishlist" text="You have no items in your wishlist. Start adding!">
      <RouterLink to="/" class="btn btn-blue btn-sm">Explore products</RouterLink>
    </EmptyState>
    <template v-else>
      <article v-for="p in products" :key="p.id" class="item">
        <RouterLink :to="{ name: 'product', params: { id: p.id } }" class="thumb">
          <img :src="p.thumbnail" :alt="p.title" />
        </RouterLink>
        <div class="info">
          <RouterLink :to="{ name: 'product', params: { id: p.id } }" class="title">{{ p.title }}</RouterLink>
          <div class="row gap-8 rate">
            <RatingBadge v-if="p.rating" :value="p.rating" />
            <span class="muted">({{ number(p.ratingCount) }})</span>
            <AssuredBadge v-if="p.assured" />
          </div>
          <PriceLine :price="p.price" :mrp="p.mrp" :discount="p.discount" />
          <p v-if="p.stock === 0" class="oos">Currently unavailable</p>
        </div>
        <button class="del" :aria-label="`Remove ${p.title} from wishlist`" @click="wishlist.toggle(p.id)">
          <Icon name="trash" :size="18" />
        </button>
      </article>
    </template>
  </div>
</template>

<style scoped>
h2 {
  padding: 20px 32px;
  font-size: 18px;
  border-bottom: 1px solid var(--border-light);
}

.item {
  display: flex;
  gap: 24px;
  padding: 24px 32px;
  border-bottom: 1px solid var(--border-light);
}

.thumb {
  width: 90px;
  height: 90px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
}

.thumb img {
  max-height: 90px;
  object-fit: contain;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title {
  font-size: 16px;
}

.title:hover {
  color: var(--fk-blue);
}

.rate {
  font-size: 13px;
}

.oos {
  color: #ff6161;
  font-size: 13px;
  font-weight: 600;
}

.del {
  align-self: flex-start;
  border: none;
  background: none;
  color: #c2c2c2;
}

.del:hover {
  color: var(--text-2);
}

@media (max-width: 600px) {
  .item { padding: 16px; gap: 14px; }
  h2 { padding: 16px; }
}
</style>
