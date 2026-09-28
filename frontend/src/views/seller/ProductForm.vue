<script setup>
import { reactive, ref, computed } from 'vue'
import Icon from '../../components/Icon.vue'

const props = defineProps({
  product: { type: Object, default: null },
  categories: { type: Array, default: () => [] }
})
const emit = defineEmits(['save', 'close'])

const p = props.product
const form = reactive({
  title: p?.title || '',
  brand: p?.brand || '',
  category: p?.category || props.categories[0]?.slug || '',
  subCategory: p?.subCategory || '',
  mrp: p?.mrp || '',
  price: p?.price || '',
  stock: p?.stock ?? 10,
  thumbnail: p?.thumbnail || '',
  images: (p?.images || []).join('\n'),
  highlights: (p?.highlights || []).join('\n'),
  description: p?.description || '',
  warranty: p?.warranty || '1 year manufacturer warranty',
  returnPolicy: p?.returnPolicy || '7 days return policy'
})
const error = ref('')
const busy = ref(false)

const category = computed(() => props.categories.find((c) => c.slug === form.category))
const discount = computed(() => {
  const mrp = Number(form.mrp)
  const price = Number(form.price)
  return mrp > 0 && price > 0 && price <= mrp ? Math.round(((mrp - price) * 100) / mrp) : 0
})

const lines = (s) => s.split('\n').map((x) => x.trim()).filter(Boolean)

async function submit() {
  error.value = ''
  const mrp = Number(form.mrp)
  const price = Number(form.price)
  const stock = Number(form.stock)
  if (!form.title.trim() || !form.brand.trim() || !form.subCategory.trim()) return (error.value = 'Title, brand and sub-category are required')
  if (!form.description.trim()) return (error.value = 'Please add a description')
  if (!Number.isInteger(mrp) || mrp < 1 || !Number.isInteger(price) || price < 1) return (error.value = 'Prices must be whole numbers greater than 0')
  if (price > mrp) return (error.value = 'Selling price cannot be higher than MRP')
  if (!Number.isInteger(stock) || stock < 0) return (error.value = 'Stock must be a whole number ≥ 0')
  if (!form.thumbnail.trim()) return (error.value = 'Please provide a product image URL')

  busy.value = true
  const images = lines(form.images)
  await emit('save', {
    title: form.title.trim(),
    brand: form.brand.trim(),
    description: form.description.trim(),
    category: form.category,
    categoryName: category.value?.name || form.category,
    subCategory: form.subCategory.trim(),
    mrp,
    price,
    stock,
    thumbnail: form.thumbnail.trim(),
    images: images.length ? images : [form.thumbnail.trim()],
    highlights: lines(form.highlights),
    specifications: p?.specifications || { Brand: form.brand.trim() },
    warranty: form.warranty.trim(),
    returnPolicy: form.returnPolicy.trim()
  })
  busy.value = false
}
</script>

<template>
  <div class="overlay" @mousedown.self="emit('close')">
    <form class="card modal" novalidate @submit.prevent="submit">
      <header>
        <h2>{{ product ? 'Edit Product' : 'Add a New Product' }}</h2>
        <button type="button" class="x" aria-label="Close" @click="emit('close')"><Icon name="close" /></button>
      </header>

      <div class="body">
        <div class="preview">
          <div class="img">
            <img v-if="form.thumbnail" :src="form.thumbnail" alt="Preview" />
            <span v-else class="muted">Image preview</span>
          </div>
          <p class="muted small">Use a path like <code>/images/products/FK0121/1.webp</code> or any https image URL.</p>
        </div>

        <div class="grid">
          <label class="field full"><input v-model="form.title" placeholder=" " /><span>Product title</span></label>
          <label class="field"><input v-model="form.brand" placeholder=" " /><span>Brand</span></label>
          <label class="field">
            <select v-model="form.category">
              <option v-for="c in categories" :key="c.slug" :value="c.slug">{{ c.name }}</option>
            </select>
            <span>Category</span>
          </label>
          <label class="field">
            <input v-model="form.subCategory" list="subcats" placeholder=" " /><span>Sub-category</span>
            <datalist id="subcats"><option v-for="s in category?.subCategories || []" :key="s" :value="s" /></datalist>
          </label>
          <label class="field"><input v-model="form.stock" type="number" min="0" placeholder=" " /><span>Stock (units)</span></label>
          <label class="field"><input v-model="form.mrp" type="number" min="1" placeholder=" " /><span>MRP (₹)</span></label>
          <label class="field"><input v-model="form.price" type="number" min="1" placeholder=" " /><span>Selling price (₹) {{ discount ? `· ${discount}% off` : '' }}</span></label>
          <label class="field full"><input v-model="form.thumbnail" placeholder=" " /><span>Main image URL</span></label>
          <label class="field full"><textarea v-model="form.images" rows="2" placeholder=" " /><span>Gallery image URLs (one per line)</span></label>
          <label class="field full"><textarea v-model="form.description" rows="3" placeholder=" " /><span>Description</span></label>
          <label class="field full"><textarea v-model="form.highlights" rows="3" placeholder=" " /><span>Highlights (one per line)</span></label>
          <label class="field"><input v-model="form.warranty" placeholder=" " /><span>Warranty</span></label>
          <label class="field"><input v-model="form.returnPolicy" placeholder=" " /><span>Return policy</span></label>
        </div>
      </div>

      <footer>
        <p v-if="error" class="err">{{ error }}</p>
        <button type="button" class="btn btn-outline btn-sm" @click="emit('close')">Cancel</button>
        <button class="btn btn-orange btn-sm" :disabled="busy">{{ product ? 'Save changes' : 'List product' }}</button>
      </footer>
    </form>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.55);
  display: grid;
  place-items: center;
  padding: 16px;
}

.modal {
  width: 100%;
  max-width: 860px;
  max-height: calc(100vh - 32px);
  display: flex;
  flex-direction: column;
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
}

header h2 {
  font-size: 18px;
}

.x {
  border: none;
  background: none;
  color: var(--text-2);
}

.body {
  display: flex;
  gap: 24px;
  padding: 20px 24px;
  overflow-y: auto;
}

.preview {
  width: 180px;
  flex-shrink: 0;
}

.img {
  height: 180px;
  border: 1px dashed var(--border);
  border-radius: 4px;
  display: grid;
  place-items: center;
  margin-bottom: 8px;
}

.img img {
  max-height: 170px;
  object-fit: contain;
}

.small {
  font-size: 12px;
  line-height: 1.5;
}

code {
  font-size: 11px;
  word-break: break-all;
}

.grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.full {
  grid-column: 1 / -1;
}

footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  border-top: 1px solid var(--border-light);
}

.err {
  margin-right: auto;
  color: #e53935;
  font-size: 13px;
}

@media (max-width: 700px) {
  .body { flex-direction: column; }
  .preview { width: 100%; }
  .grid { grid-template-columns: 1fr; }
}
</style>
