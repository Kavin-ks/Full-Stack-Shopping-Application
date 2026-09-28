<script setup>
import { watch } from 'vue'
import LoginPanel from './LoginPanel.vue'
import Icon from './Icon.vue'
import { useUiStore } from '../stores/ui'

const ui = useUiStore()

function done() {
  const after = ui.loginAfter
  ui.closeLogin()
  if (after) after()
}

watch(() => ui.loginOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
})
</script>

<template>
  <Transition name="fade">
    <div v-if="ui.loginOpen" class="overlay" @mousedown.self="ui.closeLogin()">
      <div class="dialog" role="dialog" aria-modal="true" aria-label="Login">
        <button class="close" aria-label="Close" @click="ui.closeLogin()"><Icon name="close" :size="22" /></button>
        <LoginPanel @success="done" />
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.72);
  display: grid;
  place-items: center;
  padding: 16px;
}

.dialog {
  position: relative;
  width: 100%;
  max-width: 750px;
  border-radius: 2px;
  overflow: visible;
}

.close {
  position: absolute;
  top: -4px;
  right: -44px;
  border: none;
  background: none;
  color: #fff;
}

@media (max-width: 860px) {
  .close { right: 8px; top: 8px; color: #fff; z-index: 2; }
}
</style>
