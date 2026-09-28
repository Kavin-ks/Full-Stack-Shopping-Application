<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '../api'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { useUiStore } from '../stores/ui'
import { useCatalogStore } from '../stores/catalog'
import { addRecentlyViewed } from '../utils/storage'
import { rupees, number, deliveryDate, formatDate, FREE_DELIVERY_THRESHOLD } from '../utils/format'
import Icon from '../components/Icon.vue'
import RatingBadge from '../components/RatingBadge.vue'
import AssuredBadge from '../components/AssuredBadge.vue'
import WishlistHeart from '../components/WishlistHeart.vue'
import PriceLine from '../components/PriceLine.vue'
import ProductShelf from '../components/ProductShelf.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const auth = useAuthStore()
const ui = useUiStore()
const catalog = useCatalogStore()

const product = ref(null)
const similar = ref([])
const notFound = ref(false)
const activeImg = ref(0)
const adding = ref(false)
const pincode = ref('')
const pinResult = ref(null)
const showAllSpecs = ref(false)
const reviewOpen = ref(false)
const review = ref({ rating: 0, title: '', comment: '' })
const reviewBusy = ref(false)

const OFFERS = [
  { type: 'Bank Offer', text: '5% Unlimited Cashback on Flipkart Axis Bank Credit Card' },
  { type: 'Bank Offer', text: '10% off up to ₹1,250 on HDFC Bank Credit Card EMI Transactions, min order value ₹5,000' },
  { type: 'Special Price', text: 'Get extra discount (price inclusive of cashback/coupon)' },
  { type: 'Partner Offer', text: 'Sign up for Flipkart Pay Later and get Flipkart Gift Card worth up to ₹500' }
]

onMounted(async () => {
  try {
    product.value = await productApi.get(route.params.id)
    addRecentlyViewed(product.value.id)
    document.title = `${product.value.title} | Flipkart`
    productApi.similar(route.params.id).then((s) => (similar.value = s)).catch(() => {})
  } catch (e) {
    if (e.status === 404) notFound.value = true
    else ui.error(e)
  }
})

const category = computed(() => product.value && catalog.byslug(product.value.category))
const inCart = computed(() => product.value && cart.has(product.value.id))
const outOfStock = computed(() => product.value?.stock === 0)
const images = computed(() => (product.value?.images?.length ? product.value.images : [product.value?.thumbnail]))

const distribution = computed(() => {
  const reviews = product.value?.reviews || []
  const counts = [5, 4, 3, 2, 1].map((star) => ({ star, count: reviews.filter((r) => r.rating === star).length }))
  const max = Math.max(1, ...counts.map((c) => c.count))
  return counts.map((c) => ({ ...c, pct: (c.count / max) * 100 }))
})

const specs = computed(() => Object.entries(product.value?.specifications || {}))

async function addToCart() {
  if (inCart.value) return router.push('/cart')
  adding.value = true
  const ok = await cart.add(product.value, 1)
  adding.value = false
  if (ok) router.push('/cart')
}

function buyNow() {
  const go = () => router.push({ name: 'checkout', query: { buyNow: product.value.id, qty: 1 } })
  if (!auth.isLoggedIn) ui.openLogin(go)
  else go()
}

function checkPincode() {
  if (!/^[1-9]\d{5}$/.test(pincode.value)) {
    pinResult.value = { ok: false, text: 'Please enter a valid 6-digit pincode' }
    return
  }
  const days = 2 + (Number(pincode.value) % 4)
  const free = product.value.price >= FREE_DELIVERY_THRESHOLD
  pinResult.value = { ok: true, text: `Delivery by ${deliveryDate(days)}`, free }
}

function openReview() {
  if (!auth.isLoggedIn) return ui.openLogin(() => (reviewOpen.value = true))
  reviewOpen.value = true
}

async function submitReview() {
  if (!review.value.rating) return ui.error('Please select a rating')
  if (!review.value.comment.trim()) return ui.error('Please write a short review')
  reviewBusy.value = true
  try {
    product.value = await productApi.review(product.value.id, review.value)
    review.value = { rating: 0, title: '', comment: '' }
    reviewOpen.value = false
    ui.notify('Thank you! Your review has been published')
  } catch (e) {
    ui.error(e)
  } finally {
    reviewBusy.value = false
  }
}
</script>

<template>
  <div class="container pdp-page">
    <EmptyState v-if="notFound" class="card" image="box" title="This product is no longer available">
      <RouterLink to="/" class="btn btn-blue btn-sm">Continue Shopping</RouterLink>
    </EmptyState>

    <div v-else-if="!product" class="card pdp skeleton-wrap">
      <div class="gallery"><div class="skeleton" style="height: 420px" /></div>
      <div class="details">
        <div class="skeleton" style="height: 24px; width: 70%" />
        <div class="skeleton" style="height: 18px; width: 30%; margin-top: 14px" />
        <div class="skeleton" style="height: 34px; width: 40%; margin-top: 18px" />
        <div class="skeleton" style="height: 120px; margin-top: 24px" />
      </div>
    </div>

    <template v-else>
      <div class="card pdp">
        <!-- Left: gallery + CTA -->
        <div class="gallery">
          <div class="gallery-inner">
            <ul class="thumbs">
              <li
                v-for="(img, i) in images"
                :key="img"
                :class="{ on: i === activeImg }"
                @mouseenter="activeImg = i"
                @click="activeImg = i"
              >
                <img :src="img" :alt="`${product.title} image ${i + 1}`" />
              </li>
            </ul>
            <div class="main-img">
              <img :src="images[activeImg]" :alt="product.title" />
              <WishlistHeart class="wish" :product-id="product.id" :size="18" />
            </div>
          </div>
          <div class="cta">
            <button class="btn btn-amber" :disabled="outOfStock || adding" @click="addToCart">
              <Icon name="cart" :size="18" /> {{ inCart ? 'Go to Cart' : 'Add to Cart' }}
            </button>
            <button class="btn btn-orange" :disabled="outOfStock" @click="buyNow">
              <Icon name="bolt" :size="18" /> Buy Now
            </button>
          </div>
        </div>

        <!-- Right: details -->
        <div class="details">
          <nav class="breadcrumbs" aria-label="Breadcrumb">
            <RouterLink to="/">Home</RouterLink>
            <Icon name="chevron-right" :size="14" />
            <RouterLink :to="{ name: 'products', query: { category: product.category } }">{{ category?.name || product.categoryName }}</RouterLink>
            <Icon name="chevron-right" :size="14" />
            <RouterLink :to="{ name: 'products', query: { category: product.category, subCategory: product.subCategory } }">{{ product.subCategory }}</RouterLink>
            <Icon name="chevron-right" :size="14" />
            <span>{{ product.brand }}</span>
          </nav>

          <h1 class="title"><span class="brand">{{ product.brand }}</span> {{ product.title }}</h1>

          <div class="row gap-8 rating-row">
            <RatingBadge v-if="product.rating" :value="product.rating" />
            <span class="muted">{{ number(product.ratingCount) }} Ratings &amp; {{ number(product.reviewCount) }} Reviews</span>
            <AssuredBadge v-if="product.assured" />
          </div>

          <p v-if="product.discount >= 10" class="special green">Special price</p>
          <PriceLine :price="product.price" :mrp="product.mrp" :discount="product.discount" size="lg" />

          <p v-if="outOfStock" class="stock oos">Sold Out — This item is currently out of stock</p>
          <p v-else-if="product.stock <= 5" class="stock few">Hurry, Only {{ product.stock }} left!</p>
          <p v-else class="stock ok">In stock</p>

          <section class="offers">
            <h3>Available offers</h3>
            <ul>
              <li v-for="o in OFFERS" :key="o.text">
                <Icon name="tag" :size="18" class="green" />
                <span><b>{{ o.type }}</b> {{ o.text }} <span class="tnc">T&amp;C</span></span>
              </li>
            </ul>
          </section>

          <div class="info-grid">
            <span class="label">Delivery</span>
            <div>
              <form class="pin" @submit.prevent="checkPincode">
                <Icon name="location" :size="18" class="blue" />
                <input v-model="pincode" maxlength="6" inputmode="numeric" placeholder="Enter Delivery Pincode" aria-label="Delivery pincode" />
                <button class="link-btn">Check</button>
              </form>
              <p v-if="pinResult" :class="pinResult.ok ? 'pin-ok' : 'pin-err'">
                <b>{{ pinResult.text }}</b>
                <template v-if="pinResult.ok"> | <span :class="{ green: pinResult.free }">{{ pinResult.free ? 'Free' : '₹40' }}</span></template>
              </p>
              <p v-else class="muted small">Usually delivered in 3-5 days</p>
            </div>

            <span class="label">Highlights</span>
            <ul class="bullets">
              <li v-for="h in product.highlights" :key="h">{{ h }}</li>
            </ul>

            <span class="label">Services</span>
            <ul class="services">
              <li><Icon name="replace" :size="18" class="blue" /> {{ product.returnPolicy }}</li>
              <li><Icon name="cash" :size="18" class="blue" /> Cash on Delivery available</li>
              <li><Icon name="shield" :size="18" class="blue" /> {{ product.warranty }}</li>
            </ul>

            <span class="label">Seller</span>
            <div>
              <p class="seller"><span class="blue">RetailHub</span> <RatingBadge :value="4.6" /></p>
              <p class="muted small">{{ product.returnPolicy }} · GST invoice available</p>
            </div>
          </div>

          <section class="block">
            <h2>Product Description</h2>
            <p class="desc">{{ product.description }}</p>
          </section>

          <section class="block specs">
            <h2>Specifications</h2>
            <h3>General</h3>
            <table>
              <tr v-for="[k, v] in showAllSpecs ? specs : specs.slice(0, 4)" :key="k">
                <td>{{ k }}</td>
                <td>{{ v }}</td>
              </tr>
              <tr><td>Category</td><td>{{ product.subCategory }}</td></tr>
            </table>
            <button v-if="specs.length > 4" class="link-btn read-more" @click="showAllSpecs = !showAllSpecs">
              {{ showAllSpecs ? 'Read less' : 'Read more' }}
            </button>
          </section>

          <section class="block reviews">
            <div class="rev-head">
              <h2>Ratings &amp; Reviews</h2>
              <button class="btn btn-outline btn-sm" @click="openReview">Rate Product</button>
            </div>

            <div class="rev-summary">
              <div class="big">
                <p class="avg">{{ product.rating.toFixed(1) }} <Icon name="star" :size="22" /></p>
                <p class="muted">{{ number(product.ratingCount) }} Ratings &amp;<br />{{ number(product.reviewCount) }} Reviews</p>
              </div>
              <ul class="bars">
                <li v-for="d in distribution" :key="d.star">
                  <span>{{ d.star }} ★</span>
                  <span class="bar"><span :style="{ width: d.pct + '%' }" :class="d.star >= 3 ? 'g' : d.star === 2 ? 'o' : 'r'" /></span>
                  <span class="muted">{{ d.count }}</span>
                </li>
              </ul>
            </div>

            <form v-if="reviewOpen" class="review-form" @submit.prevent="submitReview">
              <p class="rf-title">Rate this product</p>
              <div class="stars" role="radiogroup" aria-label="Rating">
                <button
                  v-for="s in 5"
                  :key="s"
                  type="button"
                  :class="{ on: s <= review.rating }"
                  :aria-label="`${s} star`"
                  @click="review.rating = s"
                ><Icon name="star" :size="30" /></button>
              </div>
              <label class="field"><input v-model="review.title" maxlength="80" placeholder=" " /><span>Title (optional)</span></label>
              <label class="field"><textarea v-model="review.comment" maxlength="1000" placeholder=" " /><span>Description</span></label>
              <div class="row gap-12">
                <button class="btn btn-orange btn-sm" :disabled="reviewBusy">Submit</button>
                <button type="button" class="btn btn-outline btn-sm" @click="reviewOpen = false">Cancel</button>
              </div>
            </form>

            <ul class="rev-list">
              <li v-for="(r, i) in product.reviews" :key="i">
                <div class="row gap-8">
                  <RatingBadge :value="r.rating" />
                  <b>{{ r.title || (r.rating >= 4 ? 'Wonderful' : r.rating === 3 ? 'Nice' : 'Could be better') }}</b>
                </div>
                <p class="comment">{{ r.comment }}</p>
                <p class="by muted">
                  {{ r.userName }} <Icon name="check-circle" :size="14" /> Certified Buyer, {{ formatDate(r.date) }}
                </p>
              </li>
              <li v-if="!product.reviews.length" class="muted">No reviews yet. Be the first to review this product.</li>
            </ul>
          </section>
        </div>
      </div>

      <ProductShelf v-if="similar.length" title="Similar Products" :products="similar" mode="price" />
    </template>
  </div>
</template>

<style scoped>
.pdp-page {
  padding-top: 8px;
  padding-bottom: 8px;
}

.pdp {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 16px 0;
}

.skeleton-wrap {
  padding: 24px;
  gap: 32px;
}

.gallery {
  width: 42%;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-h) + 16px);
  padding: 0 16px;
}

.gallery-inner {
  display: flex;
  gap: 8px;
  border: 1px solid var(--border-light);
  padding: 8px;
}

.thumbs {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 64px;
}

.thumbs li {
  width: 64px;
  height: 64px;
  padding: 4px;
  border: 1px solid var(--border-light);
  display: grid;
  place-items: center;
  cursor: pointer;
}

.thumbs li.on {
  border: 2px solid var(--fk-blue);
}

.thumbs img {
  max-height: 54px;
  object-fit: contain;
}

.main-img {
  position: relative;
  flex: 1;
  height: 420px;
  display: grid;
  place-items: center;
}

.main-img img {
  max-height: 400px;
  max-width: 100%;
  object-fit: contain;
}

.wish {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 38px;
  height: 38px;
}

.cta {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.cta .btn {
  flex: 1;
  height: 56px;
  font-size: 16px;
}

.details {
  flex: 1;
  min-width: 0;
  padding: 0 24px 0 16px;
}

.breadcrumbs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  color: var(--text-2);
  font-size: 12px;
}

.breadcrumbs a:hover {
  color: var(--fk-blue);
}

.title {
  margin-top: 12px;
  font-size: 18px;
  font-weight: 400;
  line-height: 1.4;
}

.title .brand {
  color: var(--text-2);
  font-weight: 500;
}

.rating-row {
  margin: 10px 0 14px;
  font-size: 14px;
  font-weight: 500;
}

.special {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stock {
  margin-top: 8px;
  font-size: 14px;
  font-weight: 600;
}

.stock.oos,
.stock.few {
  color: #ff6161;
}

.stock.ok {
  color: var(--fk-green);
}

.offers {
  margin-top: 20px;
}

.offers h3 {
  font-size: 16px;
  margin-bottom: 8px;
}

.offers li {
  display: flex;
  gap: 10px;
  padding: 5px 0;
  font-size: 14px;
}

.tnc {
  color: var(--fk-blue);
  font-weight: 500;
  margin-left: 4px;
}

.info-grid {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: 24px 16px;
  margin-top: 28px;
}

.label {
  color: var(--text-2);
  font-weight: 500;
}

.blue {
  color: var(--fk-blue);
}

.small {
  font-size: 12px;
  margin-top: 4px;
}

.pin {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 300px;
  border-bottom: 2px solid var(--fk-blue);
  padding-bottom: 4px;
}

.pin input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  font-weight: 500;
}

.pin-ok {
  margin-top: 8px;
  font-size: 14px;
}

.pin-err {
  margin-top: 8px;
  color: #ff6161;
  font-size: 13px;
}

.bullets li {
  position: relative;
  padding-left: 16px;
  margin-bottom: 8px;
}

.bullets li::before {
  content: '';
  position: absolute;
  left: 2px;
  top: 8px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #c2c2c2;
}

.services li {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.seller {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.block {
  margin-top: 28px;
  border: 1px solid var(--border-light);
  border-radius: 2px;
}

.block h2 {
  padding: 20px 24px;
  font-size: 22px;
  font-weight: 500;
  border-bottom: 1px solid var(--border-light);
}

.desc {
  padding: 20px 24px;
  line-height: 1.6;
}

.specs h3 {
  padding: 16px 24px 4px;
  font-size: 18px;
  font-weight: 500;
}

.specs table {
  width: 100%;
  border-collapse: collapse;
  margin: 8px 0 12px;
}

.specs td {
  padding: 10px 24px;
  vertical-align: top;
}

.specs td:first-child {
  width: 30%;
  color: var(--text-2);
}

.read-more {
  padding: 0 24px 20px;
}

.rev-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-light);
  padding-right: 24px;
}

.rev-head h2 {
  border: none;
}

.rev-summary {
  display: flex;
  gap: 40px;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-light);
}

.big {
  text-align: center;
}

.avg {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 30px;
  font-weight: 500;
}

.bars {
  flex: 1;
  max-width: 340px;
}

.bars li {
  display: grid;
  grid-template-columns: 36px 1fr 36px;
  align-items: center;
  gap: 10px;
  margin: 5px 0;
  font-size: 13px;
  font-weight: 500;
}

.bar {
  height: 5px;
  border-radius: 3px;
  background: #f0f0f0;
  overflow: hidden;
}

.bar span {
  display: block;
  height: 100%;
}

.bar .g { background: var(--fk-green); }
.bar .o { background: #ff9f00; }
.bar .r { background: #ff6161; }

.review-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-light);
  background: #fafafa;
}

.rf-title {
  font-weight: 600;
}

.stars {
  display: flex;
  gap: 4px;
}

.stars button {
  border: none;
  background: none;
  padding: 0;
  color: #e0e0e0;
}

.stars button.on {
  color: var(--fk-green);
}

.rev-list li {
  padding: 18px 24px;
  border-bottom: 1px solid var(--border-light);
}

.rev-list li:last-child {
  border-bottom: none;
}

.comment {
  margin: 10px 0;
}

.by {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

@media (max-width: 900px) {
  .pdp { flex-direction: column; }
  .gallery { width: 100%; position: static; }
  .details { padding: 16px; }
  .main-img { height: 320px; }
  .main-img img { max-height: 300px; }
  .info-grid { grid-template-columns: 1fr; gap: 8px; }
  .info-grid .label { margin-top: 12px; }
}
</style>
