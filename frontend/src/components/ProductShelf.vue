<script setup>
import { ref } from 'vue'
import Icon from './Icon.vue'
import { rupees } from '../utils/format'

// Home page horizontal shelf: "Best of Electronics", "Top Deals on Mobiles" …
defineProps({
  title: String,
  products: { type: Array, default: () => [] },
  viewAll: Object, // router location
  mode: { type: String, default: 'discount' } // discount | price
})

const track = ref(null)
function scroll(dir) {
  track.value?.scrollBy({ left: dir * track.value.clientWidth * 0.8, behavior: 'smooth' })
}
</script>

<template>
  <section class="shelf card">
    <header>
      <h2>{{ title }}</h2>
      <RouterLink v-if="viewAll" :to="viewAll" class="all" :aria-label="`View all ${title}`">
        <Icon name="chevron-right" :size="22" />
      </RouterLink>
    </header>
    <div class="track-wrap">
      <button class="nav prev" aria-label="Previous" @click="scroll(-1)"><Icon name="chevron-left" :size="24" /></button>
      <div ref="track" class="track">
        <RouterLink
          v-for="p in products"
          :key="p.id"
          :to="{ name: 'product', params: { id: p.id } }"
          class="tile"
        >
          <div class="img"><img :src="p.thumbnail" :alt="p.title" loading="lazy" /></div>
          <p class="name">{{ p.title }}</p>
          <p class="deal">
            {{ mode === 'price' ? `From ${rupees(p.price)}` : p.discount >= 10 ? `Min. ${Math.floor(p.discount / 10) * 10}% Off` : `Just ${rupees(p.price)}` }}
          </p>
        </RouterLink>
        <template v-if="!products.length">
          <div v-for="i in 6" :key="i" class="tile">
            <div class="img skeleton" />
            <p class="name skeleton" style="height: 14px" />
            <p class="deal skeleton" style="height: 14px; width: 60%" />
          </div>
        </template>
      </div>
      <button class="nav next" aria-label="Next" @click="scroll(1)"><Icon name="chevron-right" :size="24" /></button>
    </div>
  </section>
</template>

<style scoped>
.shelf {
  padding: 16px 0 12px;
  margin-top: 12px;
}

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px 12px;
}

h2 {
  font-size: 22px;
  font-weight: 600;
}

.all {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--fk-blue);
  color: #fff;
  display: grid;
  place-items: center;
}

.track-wrap {
  position: relative;
}

.track {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  padding: 4px 20px;
  scrollbar-width: none;
}

.track::-webkit-scrollbar {
  display: none;
}

.tile {
  flex: 0 0 188px;
  scroll-snap-align: start;
  padding: 12px;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  text-align: center;
  transition: box-shadow 0.15s, transform 0.15s;
}

.tile:hover {
  box-shadow: var(--shadow-md);
}

.tile:hover img {
  transform: scale(1.04);
}

.img {
  height: 150px;
  display: grid;
  place-items: center;
  margin-bottom: 12px;
  overflow: hidden;
}

.img img {
  max-height: 150px;
  object-fit: contain;
  transition: transform 0.2s;
}

.name {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.deal {
  margin-top: 6px;
  font-size: 15px;
  font-weight: 600;
}

.nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  width: 40px;
  height: 90px;
  border: none;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 1px 2px 10px -1px rgba(0, 0, 0, 0.3);
  display: grid;
  place-items: center;
  color: var(--text);
}

.prev {
  left: 0;
  border-radius: 0 4px 4px 0;
}

.next {
  right: 0;
  border-radius: 4px 0 0 4px;
}

@media (max-width: 720px) {
  .nav { display: none; }
  .tile { flex-basis: 150px; }
  h2 { font-size: 18px; }
}
</style>
