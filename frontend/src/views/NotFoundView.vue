<script setup>
import EmptyState from '../components/EmptyState.vue'
</script>

<template>
  <div class="container" style="padding-top: 24px; padding-bottom: 24px">
    <div class="card">
      <EmptyState image="box" title="Unfortunately the page you are looking for has been moved or deleted">
        <RouterLink to="/" class="btn btn-blue">Go to Homepage</RouterLink>
      </EmptyState>
    </div>
  </div>
</template>
