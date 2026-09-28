<template>
  <div class="auth-wrapper">
    <div class="auth-card">
      <!-- Amazon / Instamart Brand Header -->
      <div class="brand-header">
        <div class="brand-badge-row">
          <span class="lightning-tag">⚡ 10 MINS INSTAMART</span>
          <span class="prime-tag">Prime SuperMart</span>
        </div>
        <div class="brand-logo-title">
          <span class="logo-emoji">🛒</span>
          <h1 class="brand-name">NovaMart</h1>
        </div>
        <p class="brand-tagline">Real-Time E-Commerce & Instant Delivery Store</p>
      </div>

      <!-- Auth Mode Switcher -->
      <div class="auth-tabs">
        <button 
          class="tab-btn" 
          :class="{ active: mode === 'login' }"
          @click="setMode('login')"
        >
          Sign In
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: mode === 'register' }"
          @click="setMode('register')"
        >
          Create New Account
        </button>
      </div>

      <!-- Error / Success Alert -->
      <div v-if="errorMessage" class="auth-alert error">
        <span class="alert-icon">⚠️</span>
        <span>{{ errorMessage }}</span>
      </div>

      <div v-if="successMessage" class="auth-alert success">
        <span class="alert-icon">✓</span>
        <span>{{ successMessage }}</span>
      </div>

      <!-- Quick Demo Login Presets -->
      <div v-if="mode === 'login'" class="demo-box">
        <div class="demo-title">
          <span>⚡ Quick Test Logins (Saved Carts & Orders Ready):</span>
        </div>
        <div class="demo-chips">
          <button type="button" class="demo-chip" @click="fillDemo('kavin')">
            <span class="chip-avatar">👨🏽‍💻</span>
            <div class="chip-info">
              <strong>Kavin Soorya</strong>
              <small>kavin@store.com (2 orders, 2 cart items)</small>
            </div>
          </button>
          <button type="button" class="demo-chip" @click="fillDemo('priya')">
            <span class="chip-avatar">👩🏻‍💼</span>
            <div class="chip-info">
              <strong>Priya Sharma</strong>
              <small>priya@amzmart.com</small>
            </div>
          </button>
        </div>
      </div>

      <!-- Sign In Form -->
      <form v-if="mode === 'login'" @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label for="login-email">Email or Mobile Phone Number</label>
          <input 
            id="login-email"
            type="email" 
            v-model="loginForm.email" 
            placeholder="e.g. kavin@store.com"
            required
            autocomplete="email"
          />
        </div>

        <div class="form-group">
          <div class="label-row">
            <label for="login-password">Password</label>
            <span class="hint-text">demo: password123</span>
          </div>
          <div class="password-wrapper">
            <input 
              id="login-password"
              :type="showPassword ? 'text' : 'password'" 
              v-model="loginForm.password" 
              placeholder="Enter your password"
              required
              autocomplete="current-password"
            />
            <button type="button" class="toggle-pwd" @click="showPassword = !showPassword">
              {{ showPassword ? 'Hide' : 'Show' }}
            </button>
          </div>
        </div>

        <div class="remember-row">
          <label class="checkbox-label">
            <input type="checkbox" v-model="rememberMe" />
            <span>Keep me signed in on this device</span>
          </label>
        </div>

        <button type="submit" class="submit-btn primary" :disabled="loading">
          <span v-if="loading">Signing In...</span>
          <span v-else>Sign In to NovaMart</span>
        </button>
      </form>

      <!-- Register / Create Account Form -->
      <form v-else @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label for="reg-name">Your Full Name</label>
          <input 
            id="reg-name"
            type="text" 
            v-model="registerForm.name" 
            placeholder="First and last name"
            required
          />
        </div>

        <div class="form-group">
          <label for="reg-email">Email Address</label>
          <input 
            id="reg-email"
            type="email" 
            v-model="registerForm.email" 
            placeholder="name@example.com"
            required
          />
        </div>

        <div class="form-group">
          <label for="reg-phone">Mobile Number (For Instamart Delivery Updates)</label>
          <input 
            id="reg-phone"
            type="tel" 
            v-model="registerForm.phone" 
            placeholder="+91 98765 43210"
            required
          />
        </div>

        <div class="form-group">
          <label for="reg-address">Delivery Address</label>
          <input 
            id="reg-address"
            type="text" 
            v-model="registerForm.address" 
            placeholder="Flat/House no, Street, Landmark, City & Pincode"
            required
          />
        </div>

        <div class="form-group">
          <label for="reg-password">Password (At least 6 characters)</label>
          <input 
            id="reg-password"
            type="password" 
            v-model="registerForm.password" 
            placeholder="Create a secure password"
            minlength="6"
            required
          />
        </div>

        <button type="submit" class="submit-btn register" :disabled="loading">
          <span v-if="loading">Creating Account...</span>
          <span v-else>Create your NovaMart Account</span>
        </button>
      </form>

      <!-- Persistent Storage Guarantee Note -->
      <div class="persistence-footer">
        <div class="guarantee-badge">
          <span class="shield-icon">🛡️</span>
          <div>
            <strong>Persistent Multi-User Storage Guarantee</strong>
            <p>Your cart items, completed orders, and profile are saved locally per user. When you close and reopen the app after days, your data is seamlessly restored upon logging in.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { api } from '../services/api'

export default {
  name: 'AuthView',
  emits: ['auth-success'],
  data() {
    return {
      mode: 'login',
      loading: false,
      errorMessage: '',
      successMessage: '',
      showPassword: false,
      rememberMe: true,
      loginForm: {
        email: '',
        password: ''
      },
      registerForm: {
        name: '',
        email: '',
        phone: '',
        address: '',
        password: ''
      }
    }
  },
  methods: {
    setMode(newMode) {
      this.mode = newMode
      this.errorMessage = ''
      this.successMessage = ''
    },
    fillDemo(type) {
      if (type === 'kavin') {
        this.loginForm.email = 'kavin@store.com'
        this.loginForm.password = 'password123'
      } else {
        this.loginForm.email = 'priya@amzmart.com'
        this.loginForm.password = 'password123'
      }
      this.errorMessage = ''
      this.successMessage = `Filled demo credentials for ${this.loginForm.email}`
    },
    async handleLogin() {
      this.loading = true
      this.errorMessage = ''
      this.successMessage = ''
      try {
        const user = await api.login(this.loginForm.email, this.loginForm.password)
        this.successMessage = `Welcome back, ${user.name}! Restoring your cart and orders...`
        setTimeout(() => {
          this.$emit('auth-success', user)
        }, 350)
      } catch (err) {
        this.errorMessage = err.message || 'Login failed. Please check credentials.'
      } finally {
        this.loading = false
      }
    },
    async handleRegister() {
      this.loading = true
      this.errorMessage = ''
      this.successMessage = ''
      try {
        const user = await api.register(this.registerForm)
        this.successMessage = `Account created successfully for ${user.name}! Logging in...`
        setTimeout(() => {
          this.$emit('auth-success', user)
        }, 500)
      } catch (err) {
        this.errorMessage = err.message || 'Registration failed.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.auth-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 10% 20%, #1e293b 0%, #0f172a 90%);
  padding: 24px 16px;
}

.auth-card {
  background: #ffffff;
  width: 100%;
  max-width: 480px;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
  padding: 32px 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.brand-header {
  text-align: center;
}

.brand-badge-row {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 10px;
}

.lightning-tag {
  background: #ffedd5;
  color: #ea580c;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: 20px;
  letter-spacing: 0.5px;
}

.prime-tag {
  background: #002f34;
  color: #00a8e8;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: 20px;
}

.brand-logo-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.logo-emoji {
  font-size: 34px;
}

.brand-name {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.5px;
}

.brand-tagline {
  font-size: 13px;
  color: #64748b;
  margin: 6px 0 0 0;
}

.auth-tabs {
  display: flex;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 10px;
}

.tab-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn.active {
  background: #ffffff;
  color: #0f172a;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.auth-alert {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.4;
}

.auth-alert.error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
}

.auth-alert.success {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #15803d;
}

.demo-box {
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  padding: 12px;
}

.demo-title {
  font-size: 11.5px;
  font-weight: 700;
  color: #475569;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.demo-chips {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.demo-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s ease;
}

.demo-chip:hover {
  border-color: #f59e0b;
  background: #fffbeb;
  transform: translateY(-1px);
}

.chip-avatar {
  font-size: 20px;
}

.chip-info strong {
  display: block;
  font-size: 13px;
  color: #0f172a;
}

.chip-info small {
  display: block;
  font-size: 11px;
  color: #64748b;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-size: 12.5px;
  font-weight: 600;
  color: #334155;
}

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hint-text {
  font-size: 11px;
  color: #0284c7;
  font-weight: 500;
}

.form-group input {
  padding: 10px 14px;
  border: 1.5px solid #cbd5e1;
  border-radius: 8px;
  font-size: 13.5px;
  color: #0f172a;
  outline: none;
  transition: border-color 0.2s ease;
}

.form-group input:focus {
  border-color: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15);
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-wrapper input {
  width: 100%;
  padding-right: 56px;
}

.toggle-pwd {
  position: absolute;
  right: 10px;
  border: none;
  background: none;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
}

.remember-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: #475569;
  cursor: pointer;
}

.submit-btn {
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-btn.primary {
  background: linear-gradient(180deg, #f7ca00 0%, #f3a847 100%);
  color: #111827;
  border: 1px solid #a88734;
  box-shadow: 0 2px 5px rgba(213, 142, 22, 0.25);
}

.submit-btn.primary:hover:not(:disabled) {
  background: linear-gradient(180deg, #f5bc00 0%, #e8952b 100%);
}

.submit-btn.register {
  background: #ff5200;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(255, 82, 0, 0.25);
}

.submit-btn.register:hover:not(:disabled) {
  background: #e04800;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.persistence-footer {
  margin-top: 4px;
  border-top: 1px solid #f1f5f9;
  padding-top: 14px;
}

.guarantee-badge {
  display: flex;
  gap: 10px;
  background: #f8fafc;
  border-radius: 8px;
  padding: 10px;
}

.shield-icon {
  font-size: 20px;
}

.guarantee-badge strong {
  font-size: 12px;
  color: #1e293b;
  display: block;
}

.guarantee-badge p {
  font-size: 11px;
  color: #64748b;
  margin: 3px 0 0 0;
  line-height: 1.4;
}
</style>
