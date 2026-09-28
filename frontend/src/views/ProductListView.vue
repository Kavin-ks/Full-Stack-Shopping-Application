<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '../api'
import { useCatalogStore } from '../stores/catalog'
import { rupees, number } from '../utils/format'
import ProductCard from '../components/ProductCard.vue'
import ProductRow from '../components/ProductRow.vue'
import EmptyState from '../components/EmptyState.vue'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const catalog = useCatalogStore()

const PAGE_SIZE = 24
const SORTS = [
  { value: 'relevance', label: 'Relevance' },
  { value: 'popularity', label: 'Popularity' },
  { value: 'price_asc', label: 'Price -- Low to High' },
  { value: 'price_desc', label: 'Price -- High to Low' },
  { value: 'newest', label: 'Newest First' },
  { value: 'discount', label: 'Discount' }
]
const PRICE_STEPS = [0, 250, 500, 1000, 2000, 5000, 10000, 20000, 30000, 50000, 100000, 200000]
const RATINGS = [4, 3]
const DISCOUNTS = [50, 40, 30, 20, 10]

const page = ref(null)
const facets = ref({ brands: [], subCategories: [], minPrice: 0, maxPrice: 0 })
const loading = ref(true)
const error = ref('')
const brandSearch = ref('')
const showAllBrands = ref(false)
const drawer = ref(false)

const arr = (v) => (v == null ? [] : Array.isArray(v) ? v : [v])

// ---------------------------------------------------------------- URL state
const f = computed(() => ({
  q: route.query.q || '',
  category: route.query.category || '',
  subCategory: arr(route.query.subCategory),
  brand: arr(route.query.brand),
  minPrice: route.query.minPrice ? Number(route.query.minPrice) : null,
  maxPrice: route.query.maxPrice ? Number(route.query.maxPrice) : null,
  minRating: route.query.minRating ? Number(route.query.minRating) : null,
  minDiscount: route.query.minDiscount ? Number(route.query.minDiscount) : null,
  assured: route.query.assured === 'true',
  includeOos: route.query.includeOos === 'true',
  sort: route.query.sort || 'relevance',
  page: Number(route.query.page || 1)
}))

function update(patch, { keepPage = false } = {}) {
  const next = { ...route.query, ...patch }
  if (!keepPage) delete next.page
  Object.keys(next).forEach((k) => {
    const v = next[k]
    if (v === null || v === undefined || v === '' || v === false || (Array.isArray(v) && !v.length)) delete next[k]
  })
  router.push({ name: 'products', query: next })
}

function toggleIn(key, value) {
  const list = f.value[key]
  update({ [key]: list.includes(value) ? list.filter((x) => x !== value) : [...list, value] })
}

function clearAll() {
  const keep = {}
  if (f.value.q) keep.q = f.value.q
  if (f.value.category) keep.category = f.value.category
  router.push({ name: 'products', query: keep })
}

// ---------------------------------------------------------------- data
watch(
  () => route.query,
  async () => {
    if (route.name !== 'products') return
    loading.value = true
    error.value = ''
    const v = f.value
    try {
      const [result, fc] = await Promise.all([
        productApi.search({
          q: v.q,
          category: v.category,
          subCategory: v.subCategory,
          brand: v.brand,
          minPrice: v.minPrice,
          maxPrice: v.maxPrice,
          minRating: v.minRating,
          minDiscount: v.minDiscount,
          assured: v.assured,
          inStock: !v.includeOos,
          sort: v.sort,
          page: v.page - 1,
          size: PAGE_SIZE
        }),
        productApi.facets({ q: v.q, category: v.category })
      ])
      page.value = result
      facets.value = fc
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  },
  { immediate: true, deep: true }
)

// ---------------------------------------------------------------- view helpers
const category = computed(() => catalog.byslug(f.value.category))

const heading = computed(() => {
  if (f.value.q) return `Showing results for "${f.value.q}"`
  if (f.value.subCategory.length === 1) return f.value.subCategory[0]
  return category.value?.name || 'All Products'
})

const rangeText = computed(() => {
  if (!page.value || !page.value.totalElements) return ''
  const start = (f.value.page - 1) * PAGE_SIZE + 1
  const end = Math.min(start + page.value.content.length - 1, page.value.totalElements)
  return `Showing ${start} – ${end} of ${number(page.value.totalElements)} results`
})

// Phones & electronics read better as detailed rows, everything else as a grid (like Flipkart).
const listView = computed(() => {
  const items = page.value?.content || []
  if (['mobiles', 'electronics'].includes(f.value.category)) return true
  if (f.value.category || !items.length) return false
  const tech = items.filter((p) => ['mobiles', 'electronics'].includes(p.category)).length
  return tech / items.length > 0.6
})

const brands = computed(() => {
  const term = brandSearch.value.trim().toLowerCase()
  const list = facets.value.brands.filter((b) => !term || b.name.toLowerCase().includes(term))
  return showAllBrands.value || term ? list : list.slice(0, 6)
})

const priceOptions = computed(() => {
  const max = facets.value.maxPrice || 0
  return PRICE_STEPS.filter((p, i) => i === 0 || p < max * 1.5)
})

const chips = computed(() => {
  const c = []
  f.value.subCategory.forEach((s) => c.push({ label: s, remove: () => toggleIn('subCategory', s) }))
  f.value.brand.forEach((b) => c.push({ label: b, remove: () => toggleIn('brand', b) }))
  if (f.value.minPrice || f.value.maxPrice) {
    c.push({
      label: `${f.value.minPrice ? rupees(f.value.minPrice) : 'Min'} - ${f.value.maxPrice ? rupees(f.value.maxPrice) : 'Max'}`,
      remove: () => update({ minPrice: null, maxPrice: null })
    })
  }
  if (f.value.minRating) c.push({ label: `${f.value.minRating}★ & above`, remove: () => update({ minRating: null }) })
  if (f.value.minDiscount) c.push({ label: `${f.value.minDiscount}% or more`, remove: () => update({ minDiscount: null }) })
  if (f.value.assured) c.push({ label: 'Flipkart Assured', remove: () => update({ assured: null }) })
  if (f.value.includeOos) c.push({ label: 'Include Out of Stock', remove: () => update({ includeOos: null }) })
  return c
})

const pages = computed(() => {
  const total = page.value?.totalPages || 0
  const cur = f.value.page
  const start = Math.max(1, Math.min(cur - 4, total - 9))
  return Array.from({ length: Math.min(10, total) }, (_, i) => start + i)
})

function goPage(n) {
  update({ page: n === 1 ? null : n }, { keepPage: true })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <div class="container listing">
    <!-- Filters -->
    <aside class="filters card" :class="{ open: drawer }">
      <div class="f-head">
        <h2>Filters</h2>
        <button v-if="chips.length" class="link-btn clear" @click="clearAll">Clear all</button>
        <button class="close-drawer" aria-label="Close filters" @click="drawer = false"><Icon name="close" /></button>
      </div>

      <div v-if="chips.length" class="chips">
        <button v-for="c in chips" :key="c.label" class="chip" @click="c.remove">
          <Icon name="close" :size="12" /> {{ c.label }}
        </button>
      </div>

      <section class="f-sec">
        <h3>Categories</h3>
        <RouterLink v-if="f.category || f.q" :to="{ name: 'products', query: f.q ? { q: f.q } : {} }" class="crumb-back">
          <Icon name="chevron-left" :size="16" /> {{ f.category ? 'All Categories' : 'Clear search' }}
        </RouterLink>
        <template v-if="!f.category">
          <RouterLink
            v-for="c in catalog.categories"
            :key="c.slug"
            :to="{ name: 'products', query: { ...(f.q ? { q: f.q } : {}), category: c.slug } }"
            class="cat-link"
          >{{ c.name }}</RouterLink>
        </template>
        <template v-else>
          <p class="cat-current">{{ category?.name }}</p>
          <label v-for="s in facets.subCategories" :key="s.name" class="check">
            <input type="checkbox" :checked="f.subCategory.includes(s.name)" @change="toggleIn('subCategory', s.name)" />
            {{ s.name }} <span class="muted">({{ s.count }})</span>
          </label>
        </template>
      </section>

      <section class="f-sec">
        <h3>Price</h3>
        <div class="price-row">
          <select :value="f.minPrice || 0" aria-label="Minimum price" @change="update({ minPrice: Number($event.target.value) || null })">
            <option v-for="p in priceOptions" :key="p" :value="p" :disabled="f.maxPrice && p >= f.maxPrice">
              {{ p === 0 ? 'Min' : rupees(p) }}
            </option>
          </select>
          <span class="muted">to</span>
          <select :value="f.maxPrice || 0" aria-label="Maximum price" @change="update({ maxPrice: Number($event.target.value) || null })">
            <option :value="0">{{ rupees(PRICE_STEPS.at(-1)) }}+</option>
            <option v-for="p in priceOptions.slice(1)" :key="p" :value="p" :disabled="f.minPrice && p <= f.minPrice">
              {{ rupees(p) }}
            </option>
          </select>
        </div>
      </section>

      <section class="f-sec" v-if="facets.brands.length">
        <h3>Brand</h3>
        <div v-if="facets.brands.length > 6" class="brand-search">
          <Icon name="search" :size="16" class="muted" />
          <input v-model="brandSearch" placeholder="Search Brand" aria-label="Search brand" />
        </div>
        <label v-for="b in brands" :key="b.name" class="check">
          <input type="checkbox" :checked="f.brand.includes(b.name)" @change="toggleIn('brand', b.name)" />
          {{ b.name }}
        </label>
        <button v-if="facets.brands.length > 6 && !brandSearch" class="link-btn more" @click="showAllBrands = !showAllBrands">
          {{ showAllBrands ? 'Show less' : `${facets.brands.length - 6} more` }}
        </button>
      </section>

      <section class="f-sec">
        <label class="check assured-check">
          <input type="checkbox" :checked="f.assured" @change="update({ assured: $event.target.checked || null })" />
          <span class="assured">F Assured</span>
        </label>
      </section>

      <section class="f-sec">
        <h3>Customer Ratings</h3>
        <label v-for="r in RATINGS" :key="r" class="check">
          <input type="checkbox" :checked="f.minRating === r" @change="update({ minRating: $event.target.checked ? r : null })" />
          {{ r }}★ &amp; above
        </label>
      </section>

      <section class="f-sec">
        <h3>Discount</h3>
        <label v-for="d in DISCOUNTS" :key="d" class="check">
          <input type="checkbox" :checked="f.minDiscount === d" @change="update({ minDiscount: $event.target.checked ? d : null })" />
          {{ d }}% or more
        </label>
      </section>

      <section class="f-sec">
        <h3>Availability</h3>
        <label class="check">
          <input type="checkbox" :checked="f.includeOos" @change="update({ includeOos: $event.target.checked || null })" />
          Include Out of Stock
        </label>
      </section>
    </aside>
    <div v-if="drawer" class="scrim" @click="drawer = false" />

    <!-- Results -->
    <section class="results card">
      <div class="r-head">
        <nav class="breadcrumbs" aria-label="Breadcrumb">
          <RouterLink to="/">Home</RouterLink>
          <template v-if="category">
            <Icon name="chevron-right" :size="14" />
            <RouterLink :to="{ name: 'products', query: { category: category.slug } }">{{ category.name }}</RouterLink>
          </template>
          <template v-if="f.subCategory.length === 1">
            <Icon name="chevron-right" :size="14" />
            <span>{{ f.subCategory[0] }}</span>
          </template>
        </nav>
        <h1>
          {{ heading }}
          <span v-if="rangeText" class="range">({{ rangeText }})</span>
        </h1>
        <div class="sort">
          <span class="sort-label">Sort By</span>
          <button
            v-for="s in SORTS"
            :key="s.value"
            :class="{ on: f.sort === s.value }"
            @click="update({ sort: s.value === 'relevance' ? null : s.value })"
          >{{ s.label }}</button>
        </div>
        <button class="btn btn-outline btn-sm filter-toggle" @click="drawer = true">
          <Icon name="filter" :size="16" /> Filters <span v-if="chips.length">({{ chips.length }})</span>
        </button>
      </div>

      <div v-if="loading && !page" class="center-box"><div class="spinner" /></div>

      <div v-else-if="error" class="center-box">
        <p>{{ error }}</p>
        <button class="btn btn-blue btn-sm" @click="router.go(0)">Retry</button>
      </div>

      <EmptyState
        v-else-if="!page.content.length"
        image="box"
        title="Sorry, no results found!"
        text="Please check the spelling or try searching for something else"
      >
        <button v-if="chips.length" class="btn btn-blue btn-sm" @click="clearAll">Clear filters</button>
      </EmptyState>

      <template v-else>
        <div :class="[listView ? 'list' : 'grid', { dim: loading }]">
          <template v-for="p in page.content" :key="p.id">
            <ProductRow v-if="listView" :product="p" />
            <ProductCard v-else :product="p" />
          </template>
        </div>

        <nav v-if="page.totalPages > 1" class="pager" aria-label="Pagination">
          <span class="muted">Page {{ f.page }} of {{ page.totalPages }}</span>
          <div class="nums">
            <button v-if="f.page > 1" class="step" @click="goPage(f.page - 1)">Previous</button>
            <button v-for="n in pages" :key="n" :class="{ on: n === f.page }" @click="goPage(n)">{{ n }}</button>
            <button v-if="f.page < page.totalPages" class="step" @click="goPage(f.page + 1)">Next</button>
          </div>
        </nav>
      </template>
    </section>
  </div>
</template>

<style scoped>
.listing {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  padding-top: 8px;
  padding-bottom: 16px;
  max-width: 1400px;
}

/* ---- filters ---- */
.filters {
  width: 272px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-h) + 8px);
  max-height: calc(100vh - var(--header-h) - 16px);
  overflow-y: auto;
  scrollbar-width: thin;
}

.f-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--border-light);
}

.f-head h2 {
  font-size: 18px;
  font-weight: 600;
}

.clear {
  font-size: 12px;
  text-transform: uppercase;
}

.close-drawer {
  display: none;
  border: none;
  background: none;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: none;
  border-radius: 2px;
  background: #e0e0e0;
  font-size: 12px;
}

.chip:hover {
  text-decoration: line-through;
}

.f-sec {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-light);
}

.f-sec h3 {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.crumb-back {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-bottom: 8px;
  color: var(--text-2);
  font-size: 13px;
}

.cat-link {
  display: block;
  padding: 6px 0 6px 12px;
  font-size: 14px;
}

.cat-link:hover {
  color: var(--fk-blue);
}

.cat-current {
  padding: 4px 0 8px 12px;
  font-weight: 600;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.price-row select {
  flex: 1;
  min-width: 0;
  height: 32px;
  padding: 0 6px;
  border: 1px solid var(--border);
  border-radius: 2px;
  background: #fff;
}

.brand-search {
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 8px;
  padding: 4px 0;
}

.brand-search input {
  border: none;
  outline: none;
  flex: 1;
  min-width: 0;
}

.more {
  margin-top: 6px;
  font-size: 12px;
  text-transform: uppercase;
}

.assured-check .assured {
  font-size: 13px;
  height: 22px;
}

/* ---- results ---- */
.results {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.r-head {
  padding: 12px 16px 0;
  border-bottom: 1px solid var(--border-light);
}

.breadcrumbs {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-2);
  font-size: 12px;
}

.breadcrumbs a:hover {
  color: var(--fk-blue);
}

h1 {
  margin: 8px 0 4px;
  font-size: 16px;
  font-weight: 600;
}

.range {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-2);
}

.sort {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.sort-label {
  font-weight: 600;
  margin-right: 8px;
}

.sort button {
  padding: 8px 10px;
  border: none;
  border-bottom: 2px solid transparent;
  background: none;
  font-size: 14px;
}

.sort button.on {
  color: var(--fk-blue);
  border-bottom-color: var(--fk-blue);
  font-weight: 600;
}

.filter-toggle {
  display: none;
  margin: 8px 0 12px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  border-top: 1px solid var(--border-light);
}

.grid > * {
  border-right: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
}

.dim {
  opacity: 0.55;
  pointer-events: none;
  transition: opacity 0.15s;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 24px;
  flex-wrap: wrap;
}

.nums {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.nums button {
  min-width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: none;
  font-weight: 500;
}

.nums button.on {
  background: var(--fk-blue);
  color: #fff;
}

.nums .step {
  border-radius: 2px;
  padding: 0 12px;
  color: var(--fk-blue);
  font-weight: 600;
  text-transform: uppercase;
}

.scrim {
  display: none;
}

@media (max-width: 900px) {
  .filters {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 90;
    max-height: none;
    width: min(320px, 86vw);
    border-radius: 0;
    transform: translateX(-100%);
    transition: transform 0.25s;
  }
  .filters.open { transform: none; }
  .close-drawer { display: block; }
  .scrim { display: block; position: fixed; inset: 0; background: rgba(0, 0, 0, 0.45); z-index: 80; }
  .filter-toggle { display: inline-flex; }
  .sort-label { display: none; }
}
</style>
