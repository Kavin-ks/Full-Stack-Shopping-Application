<script setup>
import { useCatalogStore } from '../stores/catalog'

// Slim text navigation shown on inner pages (Flipkart shows image tiles only on the home page).
const catalog = useCatalogStore()
</script>

<template>
  <nav class="catbar" aria-label="Categories">
    <div class="container inner">
      <div v-for="c in catalog.categories" :key="c.slug" class="item">
        <RouterLink :to="{ name: 'products', query: { category: c.slug } }" class="top">
          {{ c.name }}
          <svg v-if="c.subCategories.length > 1" width="10" height="10" viewBox="0 0 24 24"><path d="M7.4 8.6 12 13.2l4.6-4.6L18 10l-6 6-6-6z" fill="currentColor" /></svg>
        </RouterLink>
        <div v-if="c.subCategories.length > 1" class="menu card">
          <RouterLink
            v-for="s in c.subCategories"
            :key="s"
            :to="{ name: 'products', query: { category: c.slug, subCategory: s } }"
          >{{ s }}</RouterLink>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.catbar {
  background: #fff;
  box-shadow: 0 1px 1px 0 rgba(0, 0, 0, 0.16);
  position: relative;
  z-index: 20;
}

.inner {
  display: flex;
  justify-content: space-between;
  height: 40px;
  overflow-x: auto;
  scrollbar-width: none;
}

.item {
  position: relative;
  display: flex;
  align-items: center;
}

.top {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  height: 100%;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.item:hover .top {
  color: var(--fk-blue);
}

.menu {
  position: absolute;
  top: 40px;
  left: 0;
  min-width: 200px;
  padding: 8px 0;
  box-shadow: var(--shadow-md);
  display: none;
}

.item:hover .menu {
  display: block;
}

.menu a {
  display: block;
  padding: 9px 20px;
  font-size: 14px;
}

.menu a:hover {
  color: var(--fk-blue);
  background: #f5faff;
}
</style>
