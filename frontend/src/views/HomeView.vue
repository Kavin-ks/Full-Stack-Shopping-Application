<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { productApi } from '../api'
import { useCatalogStore } from '../stores/catalog'
import { recentlyViewed } from '../utils/storage'
import { rupees } from '../utils/format'
import ProductShelf from '../components/ProductShelf.vue'
import Icon from '../components/Icon.vue'

const catalog = useCatalogStore()

const SHELVES = [
  { key: 'electronics', title: 'Best of Electronics', params: { category: 'electronics', sort: 'discount' } },
  { key: 'mobiles', title: 'Top Deals on Smartphones', params: { category: 'mobiles', sort: 'popularity' }, mode: 'price' },
  { key: 'fashion', title: 'Fashion Top Deals', params: { category: 'fashion', sort: 'discount' } },
  { key: 'home', title: 'Home & Kitchen Essentials', params: { category: 'home', sort: 'popularity' } },
  { key: 'beauty', title: 'Beauty, Sports & More', params: { category: 'beauty', sort: 'discount' } },
  { key: 'grocery', title: 'Grocery Staples', params: { category: 'grocery', sort: 'price_asc' }, mode: 'price' }
]

const BANNERS = [
  { category: 'mobiles', eyebrow: 'Mobiles Bonanza', title: 'Latest smartphones', cta: 'Shop Now', bg: 'linear-gradient(120deg,#0b3aa4 0%,#2874f0 60%,#4d9bff 100%)' },
  { category: 'electronics', eyebrow: 'Laptops & Tablets', title: 'Work. Play. Create.', cta: 'Explore', bg: 'linear-gradient(120deg,#20124d 0%,#5b2bb5 60%,#8e63e8 100%)' },
  { category: 'fashion', eyebrow: 'Fashion Sale', title: 'Styles for every day', cta: 'Grab Now', bg: 'linear-gradient(120deg,#b3124b 0%,#e8336d 55%,#ff7a9c 100%)' },
  { category: 'home', eyebrow: 'Home Makeover', title: 'Furniture & décor', cta: 'Shop Now', bg: 'linear-gradient(120deg,#0d5c46 0%,#15936d 55%,#35c795 100%)' }
]

const shelves = ref(Object.fromEntries(SHELVES.map((s) => [s.key, []])))
const banners = ref(BANNERS.map((b) => ({ ...b, products: [], from: 0 })))
const grids = ref({ rated: [], discount: [], fresh: [] })
const recent = ref([])
const slide = ref(0)
let timer = null

onMounted(async () => {
  SHELVES.forEach(async (s) => {
    try {
      shelves.value[s.key] = (await productApi.search({ ...s.params, size: 12, inStock: true })).content
    } catch {
      /* shelf stays empty */
    }
  })
  banners.value.forEach(async (b) => {
    try {
      const page = await productApi.search({ category: b.category, sort: 'popularity', size: 3 })
      b.products = page.content
      b.from = Math.min(...page.content.map((p) => p.price))
    } catch {
      /* banner still renders text */
    }
  })
  try {
    const [rated, discount, fresh] = await Promise.all([
      productApi.search({ sort: 'relevance', size: 4, inStock: true }),
      productApi.search({ sort: 'discount', size: 4, inStock: true }),
      productApi.search({ sort: 'newest', size: 4, inStock: true })
    ])
    grids.value = { rated: rated.content, discount: discount.content, fresh: fresh.content }
  } catch {
    /* ignore */
  }
  const ids = recentlyViewed()
  if (ids.length) {
    try {
      recent.value = await productApi.batch(ids)
    } catch {
      /* ignore */
    }
  }
  start()
})

onBeforeUnmount(stop)

function start() {
  clearInterval(timer)
  timer = setInterval(() => go(1), 5000)
}

function stop() {
  clearInterval(timer)
}

function go(delta) {
  slide.value = (slide.value + delta + banners.value.length) % banners.value.length
}

function jump(i) {
  slide.value = i
  start()
}

const GRID_CARDS = computed(() => [
  { title: 'Top Rated Picks', items: grids.value.rated, to: { name: 'products', query: { sort: 'relevance' } }, label: (p) => `${p.rating.toFixed(1)}★ rated` },
  { title: 'Biggest Discounts', items: grids.value.discount, to: { name: 'products', query: { sort: 'discount' } }, label: (p) => `Min. ${Math.floor(p.discount / 10) * 10}% Off` },
  { title: 'New Arrivals', items: grids.value.fresh, to: { name: 'products', query: { sort: 'newest' } }, label: (p) => `From ${rupees(p.price)}` }
])
</script>

<template>
  <div class="home container">
    <!-- Category tiles -->
    <nav class="tiles card" aria-label="Shop by category">
      <div v-for="c in catalog.categories" :key="c.slug" class="tile-wrap">
        <RouterLink :to="{ name: 'products', query: { category: c.slug } }" class="tile">
          <span class="tile-img"><img :src="c.image" :alt="c.name" /></span>
          <span class="tile-name">
            {{ c.name }}
            <Icon v-if="c.subCategories.length > 1" name="chevron-down" :size="16" />
          </span>
        </RouterLink>
        <div v-if="c.subCategories.length > 1" class="tile-menu card">
          <RouterLink
            v-for="s in c.subCategories"
            :key="s"
            :to="{ name: 'products', query: { category: c.slug, subCategory: s } }"
          >{{ s }} <Icon name="chevron-right" :size="16" /></RouterLink>
        </div>
      </div>
      <template v-if="!catalog.categories.length">
        <div v-for="i in 7" :key="i" class="tile">
          <span class="tile-img skeleton" />
          <span class="skeleton" style="height: 14px; width: 70px" />
        </div>
      </template>
    </nav>

    <!-- Hero carousel -->
    <section class="hero" aria-roledescription="carousel" @mouseenter="stop" @mouseleave="start">
      <div class="slides" :style="{ transform: `translateX(-${slide * 100}%)` }">
        <RouterLink
          v-for="(b, i) in banners"
          :key="b.category"
          :to="{ name: 'products', query: { category: b.category } }"
          class="slide"
          :style="{ background: b.bg }"
          :aria-hidden="i !== slide"
          :tabindex="i === slide ? 0 : -1"
        >
          <div class="copy">
            <span class="eyebrow">{{ b.eyebrow }}</span>
            <h2>{{ b.title }}</h2>
            <p v-if="b.from">From {{ rupees(b.from) }}*</p>
            <span class="cta">{{ b.cta }} <Icon name="arrow-right" :size="16" /></span>
          </div>
          <div class="shots">
            <span v-for="p in b.products" :key="p.id" class="shot"><img :src="p.images?.[0] || p.thumbnail" :alt="p.title" /></span>
          </div>
        </RouterLink>
      </div>
      <button class="hero-nav prev" aria-label="Previous banner" @click="go(-1); start()"><Icon name="chevron-left" :size="26" /></button>
      <button class="hero-nav next" aria-label="Next banner" @click="go(1); start()"><Icon name="chevron-right" :size="26" /></button>
      <div class="dots">
        <button
          v-for="(b, i) in banners"
          :key="i"
          :class="{ on: i === slide }"
          :aria-label="`Show banner ${i + 1}`"
          @click="jump(i)"
        />
      </div>
    </section>

    <ProductShelf
      v-for="s in SHELVES.slice(0, 2)"
      :key="s.key"
      :title="s.title"
      :products="shelves[s.key]"
      :mode="s.mode"
      :view-all="{ name: 'products', query: s.params }"
    />

    <!-- 2x2 grid cards -->
    <div class="grids">
      <section v-for="g in GRID_CARDS" :key="g.title" class="grid-card card">
        <header>
          <h2>{{ g.title }}</h2>
          <RouterLink :to="g.to" class="all" :aria-label="`View all ${g.title}`"><Icon name="chevron-right" :size="20" /></RouterLink>
        </header>
        <div class="grid4">
          <RouterLink v-for="p in g.items" :key="p.id" :to="{ name: 'product', params: { id: p.id } }" class="g-item">
            <span class="g-img"><img :src="p.thumbnail" :alt="p.title" loading="lazy" /></span>
            <span class="g-name">{{ p.title }}</span>
            <span class="g-deal">{{ g.label(p) }}</span>
          </RouterLink>
        </div>
      </section>
    </div>

    <ProductShelf
      v-for="s in SHELVES.slice(2)"
      :key="s.key"
      :title="s.title"
      :products="shelves[s.key]"
      :mode="s.mode"
      :view-all="{ name: 'products', query: s.params }"
    />

    <ProductShelf v-if="recent.length" title="Recently Viewed" :products="recent" mode="price" />
  </div>
</template>

<style scoped>
.home {
  padding-top: 12px;
  padding-bottom: 8px;
}

/* category tiles */
.tiles {
  display: flex;
  justify-content: space-around;
  gap: 8px;
  padding: 12px 16px;
  overflow-x: auto;
  scrollbar-width: none;
}

.tile-wrap {
  position: relative;
}

.tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  min-width: 96px;
}

.tile-img {
  width: 64px;
  height: 64px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #f5f7fb;
  overflow: hidden;
}

.tile-img img {
  width: 54px;
  height: 54px;
  object-fit: contain;
}

.tile-name {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

.tile:hover .tile-name {
  color: var(--fk-blue);
}

.tile-menu {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 30;
  min-width: 220px;
  padding: 8px 0;
  box-shadow: var(--shadow-md);
  display: none;
}

.tile-wrap:hover .tile-menu {
  display: block;
}

.tile-menu a {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  font-size: 14px;
}

.tile-menu a:hover {
  background: #f5faff;
  color: var(--fk-blue);
  font-weight: 500;
}

/* hero */
.hero {
  position: relative;
  margin-top: 12px;
  overflow: hidden;
  border-radius: 8px;
  height: 280px;
}

.slides {
  display: flex;
  height: 100%;
  transition: transform 0.5s ease;
}

.slide {
  flex: 0 0 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 80px;
  color: #fff;
}

.copy {
  max-width: 380px;
}

.eyebrow {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.copy h2 {
  margin-top: 14px;
  font-size: 36px;
  font-weight: 700;
  line-height: 1.15;
}

.copy p {
  margin-top: 10px;
  font-size: 20px;
  font-weight: 600;
  color: var(--fk-yellow);
}

.cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 22px;
  padding: 10px 20px;
  border-radius: 4px;
  background: #fff;
  color: var(--text);
  font-weight: 600;
}

.shots {
  display: flex;
  gap: 16px;
  align-items: flex-end;
}

.shot {
  width: 150px;
  height: 180px;
  display: grid;
  place-items: center;
  padding: 12px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.shot:nth-child(2) {
  height: 210px;
}

.shot img {
  max-height: 100%;
  object-fit: contain;
}

.hero-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 100px;
  border: none;
  background: rgba(255, 255, 255, 0.92);
  display: grid;
  place-items: center;
  color: var(--text);
}

.hero-nav.prev {
  left: 0;
  border-radius: 0 4px 4px 0;
}

.hero-nav.next {
  right: 0;
  border-radius: 4px 0 0 4px;
}

.dots {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
}

.dots button {
  width: 8px;
  height: 8px;
  padding: 0;
  border: none;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.5);
  transition: width 0.2s;
}

.dots button.on {
  width: 22px;
  background: #fff;
}

/* 2x2 grids */
.grids {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 12px;
}

.grid-card {
  padding: 16px 16px 12px;
}

.grid-card header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.grid-card h2 {
  font-size: 20px;
}

.all {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--fk-blue);
  color: #fff;
  display: grid;
  place-items: center;
}

.grid4 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.g-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  min-width: 0;
}

.g-item:hover {
  box-shadow: var(--shadow-md);
}

.g-img {
  height: 120px;
  display: grid;
  place-items: center;
}

.g-img img {
  max-height: 120px;
  object-fit: contain;
}

.g-name {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.g-deal {
  font-size: 14px;
  font-weight: 600;
}

@media (max-width: 960px) {
  .grids { grid-template-columns: 1fr; }
  .shots .shot:nth-child(3) { display: none; }
  .slide { padding: 0 56px; }
}

@media (max-width: 640px) {
  .hero { height: 220px; }
  .slide { padding: 0 24px; }
  .copy h2 { font-size: 24px; }
  .shots { display: none; }
  .hero-nav { display: none; }
}
</style>
