<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Icon from './Icon.vue'
import { productApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { useUiStore } from '../stores/ui'
import { useSession } from '../session'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const ui = useUiStore()
const { logout } = useSession()

const q = ref('')
const suggestions = ref([])
const showSuggest = ref(false)
const active = ref(-1)
let timer = null

// Keep the box in sync with the current search
watch(() => route.query.q, (v) => (q.value = v || ''), { immediate: true })

watch(q, (value) => {
  clearTimeout(timer)
  active.value = -1
  if (!value.trim()) {
    suggestions.value = []
    return
  }
  timer = setTimeout(async () => {
    try {
      suggestions.value = await productApi.suggest(value.trim())
    } catch {
      suggestions.value = []
    }
  }, 180)
})

function submit() {
  const s = active.value >= 0 ? suggestions.value[active.value] : null
  if (s) return pick(s)
  const term = q.value.trim()
  if (!term) return
  showSuggest.value = false
  router.push({ name: 'products', query: { q: term } })
}

function pick(s) {
  showSuggest.value = false
  if (s.type === 'product') router.push({ name: 'product', params: { id: s.id } })
  else router.push({ name: 'products', query: { category: s.category, subCategory: s.title } })
}

function move(delta) {
  if (!suggestions.value.length) return
  const n = suggestions.value.length
  active.value = (active.value + delta + n) % n
}

function hideSoon() {
  setTimeout(() => (showSuggest.value = false), 150)
}

function highlight(text) {
  const term = q.value.trim()
  if (!term) return text
  const i = text.toLowerCase().indexOf(term.toLowerCase())
  if (i < 0) return text
  return { before: text.slice(0, i), match: text.slice(i, i + term.length), after: text.slice(i + term.length) }
}

const cartCount = computed(() => cart.count)
</script>

<template>
  <header class="header">
    <div class="container bar">
      <RouterLink to="/" class="logo" aria-label="Flipkart home">
        <span class="wordmark">Flipkart</span>
        <span class="plus">Explore <b>Plus</b><svg width="10" height="10" viewBox="0 0 10 10"><path d="M5 0l1.2 3.8H10L6.9 6.1 8.1 10 5 7.6 1.9 10l1.2-3.9L0 3.8h3.8z" fill="#ffe500"/></svg></span>
      </RouterLink>

      <form class="search" @submit.prevent="submit" role="search">
        <input
          v-model="q"
          type="text"
          placeholder="Search for products, brands and more"
          autocomplete="off"
          aria-label="Search for products, brands and more"
          @focus="showSuggest = true"
          @blur="hideSoon"
          @keydown.down.prevent="move(1)"
          @keydown.up.prevent="move(-1)"
          @keydown.esc="showSuggest = false"
        />
        <button type="submit" aria-label="Search"><Icon name="search" :size="20" /></button>

        <ul v-if="showSuggest && suggestions.length" class="suggest card">
          <li
            v-for="(s, i) in suggestions"
            :key="s.type + (s.id || s.title)"
            :class="{ active: i === active }"
            @mousedown.prevent="pick(s)"
            @mouseenter="active = i"
          >
            <img v-if="s.thumbnail" :src="s.thumbnail" alt="" />
            <Icon v-else name="search" :size="16" class="muted" />
            <span v-if="s.type === 'category'" class="s-text">
              {{ q }} <span class="in">in</span> <span class="cat">{{ s.title }}</span>
            </span>
            <span v-else class="s-text">
              <template v-if="typeof highlight(s.title) === 'object'">
                {{ highlight(s.title).before }}<b>{{ highlight(s.title).match }}</b>{{ highlight(s.title).after }}
              </template>
              <template v-else>{{ s.title }}</template>
            </span>
          </li>
        </ul>
      </form>

      <nav class="actions">
        <div class="menu-wrap">
          <button v-if="!auth.isLoggedIn" class="login-btn" @click="ui.openLogin()">Login</button>
          <RouterLink v-else to="/account" class="user-btn">
            <span>{{ auth.firstName }}</span>
            <Icon name="chevron-down" :size="18" class="chev" />
          </RouterLink>

          <div class="dropdown card">
            <div v-if="!auth.isLoggedIn" class="signup">
              <span>New customer?</span>
              <button class="link-btn" @click="ui.openLogin()">Sign Up</button>
            </div>
            <RouterLink to="/account"><Icon name="user" :size="18" />My Profile</RouterLink>
            <RouterLink to="/account/orders"><Icon name="box" :size="18" />Orders</RouterLink>
            <RouterLink to="/account/wishlist"><Icon name="heart" :size="18" />Wishlist</RouterLink>
            <RouterLink v-if="auth.isAdmin" to="/seller"><Icon name="store" :size="18" />Seller Hub</RouterLink>
            <button v-if="auth.isLoggedIn" @click="logout"><Icon name="logout" :size="18" />Logout</button>
          </div>
        </div>

        <RouterLink v-if="auth.isAdmin" to="/seller" class="nav-link seller">
          <Icon name="store" :size="18" /> Seller Hub
        </RouterLink>

        <RouterLink to="/cart" class="nav-link cart">
          <span class="cart-icon">
            <Icon name="cart" :size="22" />
            <span v-if="cartCount" class="badge">{{ cartCount }}</span>
          </span>
          <span>Cart</span>
        </RouterLink>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: var(--fk-blue);
  height: var(--header-h);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}

.bar {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  line-height: 1;
  color: #fff;
  flex-shrink: 0;
  margin-left: 60px;
}

.wordmark {
  font-size: 21px;
  font-weight: 700;
  font-style: italic;
  letter-spacing: -0.3px;
}

.plus {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-top: 2px;
  font-size: 11px;
  font-style: italic;
  color: #fff;
}

.plus b {
  color: var(--fk-yellow);
  font-weight: 600;
}

.search {
  position: relative;
  flex: 1;
  max-width: 560px;
  display: flex;
  height: 36px;
  background: #fff;
  border-radius: 2px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.23);
}

.search input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 16px;
  font-size: 14px;
  background: transparent;
  min-width: 0;
}

.search button {
  width: 44px;
  border: none;
  background: transparent;
  color: var(--fk-blue);
  display: grid;
  place-items: center;
}

.suggest {
  position: absolute;
  top: 38px;
  left: 0;
  right: 0;
  padding: 6px 0;
  border-radius: 0 0 2px 2px;
  box-shadow: var(--shadow-md);
  z-index: 10;
}

.suggest li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
}

.suggest li.active {
  background: #f5faff;
}

.suggest img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.s-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.in {
  color: var(--text-2);
}

.cat {
  color: var(--fk-blue);
  font-weight: 500;
}

.actions {
  display: flex;
  align-items: center;
  gap: 28px;
  margin-left: auto;
  margin-right: 40px;
}

.menu-wrap {
  position: relative;
}

.login-btn {
  height: 32px;
  padding: 0 40px;
  background: #fff;
  color: var(--fk-blue);
  border: 1px solid #dbdbdb;
  border-radius: 2px;
  font-weight: 600;
  font-size: 15px;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  height: 32px;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  max-width: 140px;
}

.user-btn span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chev {
  transition: transform 0.2s;
}

.menu-wrap:hover .chev {
  transform: rotate(180deg);
}

.dropdown {
  position: absolute;
  top: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%);
  width: 240px;
  padding: 4px 0;
  box-shadow: var(--shadow-md);
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.15s, visibility 0.15s;
}

.dropdown::before {
  content: '';
  position: absolute;
  top: -16px;
  left: 0;
  right: 0;
  height: 16px;
}

.menu-wrap:hover .dropdown {
  opacity: 1;
  visibility: visible;
}

.dropdown a,
.dropdown button {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 12px 16px;
  border: none;
  background: none;
  border-bottom: 1px solid var(--border-light);
  font-size: 14px;
  color: var(--text);
  text-align: left;
}

.dropdown a:last-child,
.dropdown button:last-child {
  border-bottom: none;
}

.dropdown a:hover,
.dropdown button:hover {
  background: #f5faff;
}

.dropdown a .icon,
.dropdown button .icon {
  color: var(--fk-blue);
}

.signup {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  white-space: nowrap;
}

.cart-icon {
  position: relative;
  display: flex;
}

.badge {
  position: absolute;
  top: -8px;
  right: -9px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border: 1px solid #fff;
  border-radius: 9px;
  background: var(--fk-red);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

@media (max-width: 1100px) {
  .logo { margin-left: 0; }
  .actions { margin-right: 0; gap: 20px; }
  .seller { display: none; }
}

@media (max-width: 720px) {
  .header { height: auto; }
  .bar { flex-wrap: wrap; gap: 10px; padding-top: 10px; padding-bottom: 10px; }
  .search { order: 3; flex-basis: 100%; max-width: none; }
  .login-btn { padding: 0 20px; }
  .nav-link.cart span:last-child { display: none; }
}
</style>
