<script setup>
import { ratingClass } from '../utils/format'
defineProps({ value: { type: Number, required: true } })
</script>

<template>
  <span class="rating" :class="ratingClass(value)">
    {{ value.toFixed(1) }}
    <svg width="10" height="10" viewBox="0 0 24 24"><path d="M12 17.3 18.2 21l-1.6-7L22 9.2l-7.2-.6L12 2 9.2 8.6 2 9.2 7.5 14l-1.7 7z" fill="#fff" /></svg>
  </span>
</template>
