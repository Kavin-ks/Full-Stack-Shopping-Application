<script setup>
import { useUiStore } from '../stores/ui'
import Icon from './Icon.vue'

const ui = useUiStore()
</script>

<template>
  <Transition name="toast">
    <div v-if="ui.toast" :key="ui.toast.id" class="toast" :class="ui.toast.type" role="status" aria-live="polite">
      <Icon v-if="ui.toast.type === 'error'" name="info" :size="18" />
      <span>{{ ui.toast.message }}</span>
    </div>
  </Transition>
</template>

<style scoped>
.toast {
  position: fixed;
  left: 50%;
  bottom: 32px;
  transform: translateX(-50%);
  z-index: 200;
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: min(560px, calc(100vw - 32px));
  padding: 14px 24px;
  border-radius: 4px;
  background: #323232;
  color: #fff;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.toast.error .icon {
  color: #ff8a80;
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, 12px);
}
</style>
