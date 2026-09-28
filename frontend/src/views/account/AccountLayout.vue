<script setup>
import { useAuthStore } from '../../stores/auth'
import { useWishlistStore } from '../../stores/wishlist'
import { useSession } from '../../session'
import Icon from '../../components/Icon.vue'

const auth = useAuthStore()
const wishlist = useWishlistStore()
const { logout } = useSession()
</script>

<template>
  <div class="container account">
    <aside class="side">
      <div class="card hello">
        <span class="avatar">{{ auth.user?.name?.[0]?.toUpperCase() || 'U' }}</span>
        <div>
          <p class="small muted">Hello,</p>
          <p class="name">{{ auth.user?.name }}</p>
        </div>
      </div>

      <nav class="card menu">
        <RouterLink to="/account/orders" class="group-link">
          <Icon name="box" :size="20" class="blue" />
          <span>My Orders</span>
          <Icon name="chevron-right" :size="20" class="end" />
        </RouterLink>

        <div class="group">
          <p class="group-title"><Icon name="user" :size="20" class="blue" /> Account Settings</p>
          <RouterLink to="/account" exact-active-class="on">Profile Information</RouterLink>
          <RouterLink to="/account/addresses" active-class="on">Manage Addresses</RouterLink>
        </div>

        <div class="group">
          <p class="group-title"><Icon name="heart" :size="20" class="blue" /> My Stuff</p>
          <RouterLink to="/account/wishlist" active-class="on">My Wishlist ({{ wishlist.count }})</RouterLink>
          <RouterLink v-if="auth.isAdmin" to="/seller">Seller Hub</RouterLink>
        </div>

        <button class="group-link logout" @click="logout">
          <Icon name="logout" :size="20" class="blue" />
          <span>Logout</span>
        </button>
      </nav>
    </aside>

    <section class="main card">
      <RouterView />
    </section>
  </div>
</template>

<style scoped>
.account {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding-top: 16px;
  padding-bottom: 16px;
  max-width: 1200px;
}

.side {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hello {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #fff4c4;
  color: #d97a00;
  display: grid;
  place-items: center;
  font-size: 20px;
  font-weight: 700;
}

.small {
  font-size: 12px;
}

.name {
  font-size: 16px;
  font-weight: 600;
}

.menu {
  overflow: hidden;
}

.group-link {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  padding: 18px 20px;
  border: none;
  border-bottom: 1px solid var(--border-light);
  background: #fff;
  color: var(--text-2);
  font-size: 16px;
  font-weight: 600;
  text-transform: uppercase;
  text-align: left;
}

.group-link:hover {
  color: var(--fk-blue);
}

.end {
  margin-left: auto;
}

.blue {
  color: var(--fk-blue);
}

.group {
  border-bottom: 1px solid var(--border-light);
  padding-bottom: 8px;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px 10px;
  color: var(--text-2);
  font-size: 16px;
  font-weight: 600;
  text-transform: uppercase;
}

.group a {
  display: block;
  padding: 11px 20px 11px 56px;
  font-size: 14px;
}

.group a:hover {
  color: var(--fk-blue);
  background: #f5faff;
}

.group a.on {
  color: var(--fk-blue);
  background: #f5faff;
  font-weight: 600;
}

.logout {
  border-bottom: none;
}

.main {
  flex: 1;
  min-width: 0;
  min-height: 480px;
}

@media (max-width: 860px) {
  .account { flex-direction: column; }
  .side { width: 100%; }
  .main { width: 100%; }
}
</style>
