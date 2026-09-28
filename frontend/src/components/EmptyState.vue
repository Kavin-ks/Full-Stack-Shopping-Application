<script setup>
defineProps({ title: String, text: String, image: { type: String, default: 'cart' } })
</script>

<template>
  <div class="empty center-box">
    <svg v-if="image === 'cart'" width="220" height="150" viewBox="0 0 220 150" aria-hidden="true">
      <ellipse cx="110" cy="138" rx="90" ry="8" fill="#e8eef9" />
      <path d="M40 30h18l14 70h86l14-52H66" fill="none" stroke="#2874f0" stroke-width="5" stroke-linejoin="round" stroke-linecap="round" />
      <circle cx="84" cy="118" r="9" fill="#2874f0" />
      <circle cx="150" cy="118" r="9" fill="#2874f0" />
      <rect x="86" y="54" width="22" height="30" rx="3" fill="#ffe500" />
      <rect x="114" y="62" width="26" height="22" rx="3" fill="#fb641b" opacity=".85" />
    </svg>
    <svg v-else-if="image === 'box'" width="180" height="150" viewBox="0 0 180 150" aria-hidden="true">
      <ellipse cx="90" cy="138" rx="70" ry="8" fill="#e8eef9" />
      <path d="M30 50 90 25l60 25v60l-60 25-60-25z" fill="#fff" stroke="#2874f0" stroke-width="4" stroke-linejoin="round" />
      <path d="M30 50l60 25 60-25M90 75v60" fill="none" stroke="#2874f0" stroke-width="4" />
      <path d="M60 37l60 25" stroke="#ffe500" stroke-width="8" />
    </svg>
    <svg v-else width="180" height="150" viewBox="0 0 180 150" aria-hidden="true">
      <ellipse cx="90" cy="138" rx="70" ry="8" fill="#e8eef9" />
      <path d="M90 122 80 113C50 86 32 70 32 50c0-16 12-28 28-28 9 0 18 4 30 14 12-10 21-14 30-14 16 0 28 12 28 28 0 20-18 36-48 63z" fill="#fff" stroke="#ff6161" stroke-width="5" />
    </svg>
    <h3>{{ title }}</h3>
    <p v-if="text" class="muted">{{ text }}</p>
    <slot />
  </div>
</template>

<style scoped>
.empty {
  padding: 48px 16px 56px;
}

h3 {
  font-size: 18px;
  font-weight: 500;
  margin-top: 12px;
}
</style>
