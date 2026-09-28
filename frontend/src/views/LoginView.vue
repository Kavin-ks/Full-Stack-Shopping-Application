<script setup>
import { useRoute, useRouter } from 'vue-router'
import { onMounted } from 'vue'
import LoginPanel from '../components/LoginPanel.vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

function done() {
  router.replace(route.query.redirect || '/')
}

onMounted(() => {
  if (auth.isLoggedIn) done()
})
</script>

<template>
  <div class="container login-page">
    <div class="card wrap">
      <LoginPanel :initial-mode="route.query.mode === 'signup' ? 'signup' : 'login'" @success="done" />
    </div>
  </div>
</template>

<style scoped>
.login-page {
  padding-top: 32px;
  padding-bottom: 32px;
  display: flex;
  justify-content: center;
}

.wrap {
  width: 100%;
  max-width: 750px;
  overflow: hidden;
}
</style>
