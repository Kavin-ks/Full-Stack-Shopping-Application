<script setup>
import { useCatalogStore } from '../stores/catalog'

const catalog = useCatalogStore()
const year = new Date().getFullYear()
</script>

<template>
  <footer class="footer">
    <div class="container cols">
      <div class="col">
        <h4>Shop</h4>
        <RouterLink v-for="c in catalog.categories" :key="c.slug" :to="{ name: 'products', query: { category: c.slug } }">
          {{ c.name }}
        </RouterLink>
      </div>
      <div class="col">
        <h4>Your Account</h4>
        <RouterLink to="/account">My Profile</RouterLink>
        <RouterLink to="/account/orders">Orders</RouterLink>
        <RouterLink to="/account/wishlist">Wishlist</RouterLink>
        <RouterLink to="/account/addresses">Saved Addresses</RouterLink>
        <RouterLink to="/cart">Cart</RouterLink>
      </div>
      <div class="col">
        <h4>Consumer Policy</h4>
        <span>7 days easy returns</span>
        <span>Cancel anytime before delivery</span>
        <span>Free delivery above ₹499</span>
        <span>Secure payments</span>
      </div>
      <div class="col address">
        <h4>Mail Us</h4>
        <p>
          Flipkart Clone — Mini Project,<br />
          Full Stack Web App (Vue.js, Spring Boot,<br />
          MongoDB, Microservices)
        </p>
      </div>
    </div>
    <div class="bottom">
      <div class="container bottom-inner">
        <span>© 2007-{{ year }} Flipkart Clone · Built for learning purposes</span>
        <span class="pay">
          <b>VISA</b><b>Mastercard</b><b>RuPay</b><b>UPI</b><b>Net Banking</b><b>COD</b>
        </span>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: #172337;
  color: #fff;
  margin-top: 16px;
  font-size: 12px;
}

.cols {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding-top: 40px;
  padding-bottom: 32px;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.col h4 {
  color: #878787;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.col a,
.col span,
.col p {
  color: #fff;
  font-weight: 500;
  line-height: 1.6;
}

.col a:hover {
  text-decoration: underline;
}

.bottom {
  border-top: 1px solid #454d5e;
}

.bottom-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  padding-top: 20px;
  padding-bottom: 20px;
}

.pay {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pay b {
  padding: 3px 8px;
  border-radius: 3px;
  background: #fff;
  color: #172337;
  font-size: 11px;
  font-weight: 700;
}

@media (max-width: 760px) {
  .cols { grid-template-columns: 1fr 1fr; }
}
</style>
