import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// All /api calls go to the Spring Cloud API gateway, which routes them to the microservices.
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5180,
    strictPort: true,
    proxy: {
      '/api': 'http://localhost:8090'
    }
  },
  preview: {
    port: 5180,
    proxy: {
      '/api': 'http://localhost:8090'
    }
  }
})
