<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../../api'
import { useUiStore } from '../../stores/ui'
import { useAuthStore } from '../../stores/auth'
import AddressForm from '../../components/AddressForm.vue'
import Icon from '../../components/Icon.vue'

const ui = useUiStore()
const auth = useAuthStore()
const addresses = ref([])
const loading = ref(true)
const formOpen = ref(false)
const editing = ref(null)
const busy = ref(false)
const menuFor = ref(null)

onMounted(async () => {
  try {
    addresses.value = await userApi.addresses()
  } catch (e) {
    ui.error(e)
  } finally {
    loading.value = false
  }
})

function openNew() {
  editing.value = null
  formOpen.value = true
}

function openEdit(a) {
  menuFor.value = null
  editing.value = a
  formOpen.value = true
}

function closeForm() {
  formOpen.value = false
  editing.value = null
}

async function save(data) {
  busy.value = true
  try {
    const wasEdit = !!editing.value
    addresses.value = wasEdit ? await userApi.updateAddress(editing.value.id, data) : await userApi.addAddress(data)
    ui.notify(wasEdit ? 'Address updated' : 'Address added')
    closeForm()
  } catch (e) {
    ui.error(e)
  } finally {
    busy.value = false
  }
}

async function remove(a) {
  menuFor.value = null
  if (!confirm('Are you sure you want to delete this address?')) return
  try {
    addresses.value = await userApi.deleteAddress(a.id)
    ui.notify('Address deleted')
  } catch (e) {
    ui.error(e)
  }
}
</script>

<template>
  <div class="addresses">
    <h2>Manage Addresses</h2>

    <div class="box">
      <div v-if="formOpen" class="form-wrap">
        <p class="form-title">{{ editing ? 'Edit Address' : 'Add a New Address' }}</p>
        <AddressForm
          :key="editing?.id || 'new'"
          :initial="editing || { name: auth.user?.name, mobile: auth.user?.mobile }"
          :busy="busy"
          @submit="save"
          @cancel="closeForm"
        />
      </div>
      <button v-else class="add" @click="openNew"><Icon name="plus" :size="18" /> Add a new address</button>
    </div>

    <div v-if="loading" class="center-box"><div class="spinner" /></div>

    <div v-else-if="addresses.length" class="list">
      <article v-for="a in addresses" :key="a.id" class="addr">
        <div class="top">
          <span class="type">{{ a.type }}</span>
          <span v-if="a.defaultAddress" class="tag tag-blue">Default</span>
          <div class="more">
            <button aria-label="Address options" @click="menuFor = menuFor === a.id ? null : a.id"><Icon name="more-vertical" :size="18" /></button>
            <div v-if="menuFor === a.id" class="menu card">
              <button @click="openEdit(a)">Edit</button>
              <button @click="remove(a)">Delete</button>
            </div>
          </div>
        </div>
        <p class="who"><b>{{ a.name }}</b> <b>{{ a.mobile }}</b></p>
        <p>{{ a.address }}, {{ a.locality }}, {{ a.city }}, {{ a.state }} - <b>{{ a.pincode }}</b></p>
        <p v-if="a.landmark" class="muted small">Landmark: {{ a.landmark }}</p>
      </article>
    </div>
    <p v-else-if="!formOpen" class="center-box muted">No addresses saved yet.</p>
  </div>
</template>

<style scoped>
.addresses {
  padding: 24px 32px 32px;
}

h2 {
  font-size: 18px;
  margin-bottom: 20px;
}

.box {
  border: 1px solid var(--border-light);
  margin-bottom: 16px;
}

.add {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 16px 20px;
  border: none;
  background: #fff;
  color: var(--fk-blue);
  font-weight: 600;
  text-transform: uppercase;
  font-size: 14px;
}

.form-wrap {
  padding: 20px;
  background: #f5faff;
}

.form-title {
  color: var(--fk-blue);
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 14px;
}

.list {
  border: 1px solid var(--border-light);
}

.addr {
  padding: 20px;
  border-bottom: 1px solid var(--border-light);
}

.addr:last-child {
  border-bottom: none;
}

.top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.type {
  padding: 3px 6px;
  border-radius: 2px;
  background: #f0f0f0;
  color: var(--text-2);
  font-size: 11px;
  font-weight: 600;
}

.more {
  position: relative;
  margin-left: auto;
}

.more > button {
  border: none;
  background: none;
  color: var(--text-2);
}

.menu {
  position: absolute;
  right: 0;
  top: 24px;
  z-index: 5;
  width: 120px;
  box-shadow: var(--shadow-md);
}

.menu button {
  display: block;
  width: 100%;
  padding: 10px 14px;
  border: none;
  background: #fff;
  text-align: left;
}

.menu button:hover {
  color: var(--fk-blue);
  background: #f5faff;
}

.who {
  display: flex;
  gap: 16px;
  margin-bottom: 6px;
}

.small {
  font-size: 12px;
  margin-top: 4px;
}

@media (max-width: 600px) {
  .addresses { padding: 20px; }
}
</style>
