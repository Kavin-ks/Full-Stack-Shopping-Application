<template>
  <div class="profile-page">
    <div class="profile-header">
      <div>
        <h1 class="page-title">Your Account & Credentials</h1>
        <p class="page-subtitle">Verified customer account details persistently stored in local database.</p>
      </div>

      <button class="logout-action-btn" @click="$emit('logout')">
        Sign Out / Switch Account
      </button>
    </div>

    <!-- Alert message if updated -->
    <div v-if="saveSuccess" class="save-toast">
      ✓ Profile and address updated successfully in persistent database!
    </div>

    <div class="profile-grid">
      <!-- Main Credentials Card -->
      <div class="profile-card">
        <div class="card-title-row">
          <div class="user-avatar-circle">
            {{ userInitial }}
          </div>
          <div>
            <h2 class="user-display-name">{{ currentUser ? currentUser.name : 'Customer' }}</h2>
            <span class="member-badge">⭐ {{ currentUser ? currentUser.memberTier : 'Prime Member' }}</span>
          </div>
        </div>

        <div class="info-list">
          <div class="info-item">
            <span class="info-label">Account User ID</span>
            <span class="info-val mono">{{ currentUser ? currentUser.id : '' }}</span>
          </div>

          <div class="info-item">
            <span class="info-label">Registered Email Address</span>
            <span class="info-val">{{ currentUser ? currentUser.email : '' }}</span>
          </div>

          <div class="info-item">
            <span class="info-label">Primary Mobile Number</span>
            <span class="info-val">{{ currentUser ? currentUser.phone : '' }}</span>
          </div>

          <div class="info-item">
            <span class="info-label">Account Created</span>
            <span class="info-val">{{ formatDate(currentUser ? currentUser.createdAt : null) }}</span>
          </div>

          <div class="info-item">
            <span class="info-label">Storage Consistency Status</span>
            <span class="info-val green-text">✓ Permanent Local Storage Active</span>
          </div>
        </div>
      </div>

      <!-- Delivery Address & Edit Form -->
      <div class="profile-card">
        <div class="card-header-flex">
          <h3>📍 Delivery Address Details</h3>
          <button class="edit-btn" @click="editingAddress = !editingAddress">
            {{ editingAddress ? 'Cancel' : 'Edit Address' }}
          </button>
        </div>

        <div v-if="!editingAddress" class="current-address-display">
          <p class="addr-line">{{ currentUser ? currentUser.address : 'No address set' }}</p>
          <span class="addr-badge">⚡ Set for 10-min Instamart deliveries</span>
        </div>

        <form v-else @submit.prevent="handleSaveAddress" class="address-edit-form">
          <div class="field-group">
            <label>Full Delivery Address</label>
            <textarea 
              v-model="addressInput" 
              rows="3" 
              placeholder="Flat/House, Street, Area, Landmark, City & Pincode"
              required
            ></textarea>
          </div>

          <div class="field-group">
            <label>Contact Phone Number</label>
            <input type="tel" v-model="phoneInput" required />
          </div>

          <button type="submit" class="save-addr-btn" :disabled="saving">
            {{ saving ? 'Saving...' : 'Save & Update Details' }}
          </button>
        </form>
      </div>

      <!-- Shopping Stats Overview -->
      <div class="profile-card stats-card">
        <h3>📊 Account Activity Snapshot</h3>
        <div class="stats-row">
          <div class="stat-box">
            <span class="stat-number">{{ orderCount }}</span>
            <span class="stat-name">Orders Placed</span>
          </div>
          <div class="stat-box">
            <span class="stat-number">{{ cartCount }}</span>
            <span class="stat-name">Items in Cart</span>
          </div>
          <div class="stat-box">
            <span class="stat-number">₹{{ totalSpent.toLocaleString('en-IN') }}</span>
            <span class="stat-name">Lifetime Purchases</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { api } from '../services/api'

export default {
  name: 'ProfileView',
  props: {
    currentUser: {
      type: Object,
      default: null
    },
    orderCount: {
      type: Number,
      default: 0
    },
    cartCount: {
      type: Number,
      default: 0
    },
    orders: {
      type: Array,
      default: () => []
    }
  },
  emits: ['logout', 'user-updated'],
  data() {
    return {
      editingAddress: false,
      addressInput: this.currentUser ? this.currentUser.address : '',
      phoneInput: this.currentUser ? this.currentUser.phone : '',
      saving: false,
      saveSuccess: false
    }
  },
  watch: {
    currentUser(newUser) {
      if (newUser) {
        this.addressInput = newUser.address
        this.phoneInput = newUser.phone
      }
    }
  },
  computed: {
    userInitial() {
      return this.currentUser && this.currentUser.name ? this.currentUser.name.charAt(0).toUpperCase() : 'U'
    },
    totalSpent() {
      return this.orders.reduce((sum, ord) => sum + (ord.totalAmount || 0), 0)
    }
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return 'Active Member'
      try {
        const d = new Date(dateStr)
        return d.toLocaleDateString('en-IN', {
          day: 'numeric',
          month: 'long',
          year: 'numeric'
        })
      } catch (e) {
        return dateStr
      }
    },
    async handleSaveAddress() {
      this.saving = true
      try {
        const updatedUser = await api.updateUserProfile(this.currentUser.id, {
          address: this.addressInput,
          phone: this.phoneInput
        })
        this.$emit('user-updated', updatedUser)
        this.editingAddress = false
        this.saveSuccess = true
        setTimeout(() => {
          this.saveSuccess = false
        }, 3000)
      } catch (err) {
        alert('Failed to update address: ' + err.message)
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 14px;
}

.page-title {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.logout-action-btn {
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  color: #0f172a;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.logout-action-btn:hover {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #dc2626;
}

.save-toast {
  background: #f0fdf4;
  border: 1px solid #86efac;
  color: #15803d;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.profile-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 14px;
}

.user-avatar-circle {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff5200 0%, #ea580c 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
  box-shadow: 0 4px 10px rgba(255, 82, 0, 0.25);
}

.user-display-name {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 4px 0;
}

.member-badge {
  background: #fffbeb;
  border: 1px solid #fef3c7;
  color: #b45309;
  font-size: 11.5px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 12px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.info-val {
  font-size: 13.5px;
  color: #0f172a;
  font-weight: 600;
}

.info-val.mono {
  font-family: monospace;
  color: #475569;
}

.info-val.green-text {
  color: #16a34a;
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-flex h3, .stats-card h3 {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.edit-btn {
  background: none;
  border: none;
  color: #2563eb;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
}

.current-address-display {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
}

.addr-line {
  font-size: 13.5px;
  color: #1e293b;
  line-height: 1.5;
  margin: 0 0 10px 0;
}

.addr-badge {
  font-size: 11.5px;
  color: #ea580c;
  font-weight: 600;
}

.address-edit-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-group label {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}

.field-group textarea, .field-group input {
  padding: 8px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
}

.save-addr-btn {
  background: #ff5200;
  color: #ffffff;
  border: none;
  padding: 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px 10px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-number {
  font-size: 20px;
  font-weight: 800;
  color: #ff5200;
}

.stat-name {
  font-size: 11px;
  color: #64748b;
}
</style>
