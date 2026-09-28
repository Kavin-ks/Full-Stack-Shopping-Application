<script setup>
import { reactive, ref } from 'vue'
import { INDIAN_STATES } from '../utils/format'

const props = defineProps({
  initial: { type: Object, default: null },
  submitLabel: { type: String, default: 'Save' },
  busy: Boolean
})
const emit = defineEmits(['submit', 'cancel'])

const form = reactive({
  name: props.initial?.name || '',
  mobile: props.initial?.mobile || '',
  pincode: props.initial?.pincode || '',
  locality: props.initial?.locality || '',
  address: props.initial?.address || '',
  city: props.initial?.city || '',
  state: props.initial?.state || '',
  landmark: props.initial?.landmark || '',
  alternatePhone: props.initial?.alternatePhone || '',
  type: props.initial?.type || 'HOME',
  makeDefault: props.initial?.defaultAddress || false
})
const errors = ref({})

const MOBILE = /^[6-9]\d{9}$/
const PIN = /^[1-9]\d{5}$/

function validate() {
  const e = {}
  if (!form.name.trim()) e.name = 'Please enter your name'
  if (!MOBILE.test(form.mobile)) e.mobile = 'Please enter a valid 10-digit mobile number'
  if (!PIN.test(form.pincode)) e.pincode = 'Please enter a valid 6-digit pincode'
  if (!form.locality.trim()) e.locality = 'Please enter your locality'
  if (!form.address.trim()) e.address = 'Please enter your address'
  if (!form.city.trim()) e.city = 'Please enter your city/district/town'
  if (!form.state) e.state = 'Please select your state'
  if (form.alternatePhone && !MOBILE.test(form.alternatePhone)) e.alternatePhone = 'Please enter a valid mobile number'
  errors.value = e
  return !Object.keys(e).length
}

function submit() {
  if (validate()) emit('submit', { ...form })
}
</script>

<template>
  <form class="addr-form" novalidate @submit.prevent="submit">
    <div class="grid">
      <label class="field" :class="{ invalid: errors.name }">
        <input v-model="form.name" placeholder=" " autocomplete="name" />
        <span>Name</span>
        <p v-if="errors.name" class="field-error">{{ errors.name }}</p>
      </label>
      <label class="field" :class="{ invalid: errors.mobile }">
        <input v-model="form.mobile" maxlength="10" inputmode="numeric" placeholder=" " autocomplete="tel-national" />
        <span>10-digit mobile number</span>
        <p v-if="errors.mobile" class="field-error">{{ errors.mobile }}</p>
      </label>
      <label class="field" :class="{ invalid: errors.pincode }">
        <input v-model="form.pincode" maxlength="6" inputmode="numeric" placeholder=" " autocomplete="postal-code" />
        <span>Pincode</span>
        <p v-if="errors.pincode" class="field-error">{{ errors.pincode }}</p>
      </label>
      <label class="field" :class="{ invalid: errors.locality }">
        <input v-model="form.locality" placeholder=" " />
        <span>Locality</span>
        <p v-if="errors.locality" class="field-error">{{ errors.locality }}</p>
      </label>
      <label class="field full" :class="{ invalid: errors.address }">
        <textarea v-model="form.address" rows="3" placeholder=" " autocomplete="street-address" />
        <span>Address (Area and Street)</span>
        <p v-if="errors.address" class="field-error">{{ errors.address }}</p>
      </label>
      <label class="field" :class="{ invalid: errors.city }">
        <input v-model="form.city" placeholder=" " autocomplete="address-level2" />
        <span>City/District/Town</span>
        <p v-if="errors.city" class="field-error">{{ errors.city }}</p>
      </label>
      <label class="field" :class="{ invalid: errors.state }">
        <select v-model="form.state">
          <option value="" disabled>--Select State--</option>
          <option v-for="s in INDIAN_STATES" :key="s" :value="s">{{ s }}</option>
        </select>
        <span>State</span>
        <p v-if="errors.state" class="field-error">{{ errors.state }}</p>
      </label>
      <label class="field">
        <input v-model="form.landmark" placeholder=" " />
        <span>Landmark (Optional)</span>
      </label>
      <label class="field" :class="{ invalid: errors.alternatePhone }">
        <input v-model="form.alternatePhone" maxlength="10" inputmode="numeric" placeholder=" " />
        <span>Alternate Phone (Optional)</span>
        <p v-if="errors.alternatePhone" class="field-error">{{ errors.alternatePhone }}</p>
      </label>
    </div>

    <div class="type">
      <p class="muted">Address Type</p>
      <label class="check"><input v-model="form.type" type="radio" value="HOME" /> Home (All day delivery)</label>
      <label class="check"><input v-model="form.type" type="radio" value="WORK" /> Work (Delivery between 10 AM - 5 PM)</label>
    </div>

    <label class="check default">
      <input v-model="form.makeDefault" type="checkbox" /> Make this my default address
    </label>

    <div class="row gap-16 buttons">
      <button class="btn btn-orange" :disabled="busy" style="min-width: 220px">{{ busy ? 'Saving…' : submitLabel }}</button>
      <button type="button" class="text-btn cancel" @click="emit('cancel')">Cancel</button>
    </div>
  </form>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  max-width: 560px;
}

.full {
  grid-column: 1 / -1;
}

.type {
  margin-top: 18px;
}

.type .check {
  display: inline-flex;
  margin-right: 24px;
}

.default {
  margin-top: 8px;
}

.buttons {
  margin-top: 20px;
}

.cancel {
  color: var(--fk-blue);
}

@media (max-width: 600px) {
  .grid { grid-template-columns: 1fr; }
}
</style>
