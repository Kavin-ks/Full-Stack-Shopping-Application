<script setup>
import { reactive, ref } from 'vue'
import { userApi } from '../../api'
import { useAuthStore } from '../../stores/auth'
import { useUiStore } from '../../stores/ui'

const auth = useAuthStore()
const ui = useUiStore()

const form = reactive({ firstName: '', lastName: '', gender: '', email: '', mobile: '' })
const editing = ref({ personal: false, email: false, mobile: false })
const busy = ref(false)

function reset() {
  const [first, ...rest] = (auth.user?.name || '').split(' ')
  Object.assign(form, {
    firstName: first || '',
    lastName: rest.join(' '),
    gender: auth.user?.gender || '',
    email: auth.user?.email || '',
    mobile: auth.user?.mobile || ''
  })
}
reset()

function toggle(section) {
  if (editing.value[section]) reset()
  editing.value[section] = !editing.value[section]
}

async function save(section) {
  if (!form.firstName.trim()) return ui.error('Please enter your first name')
  if (form.mobile && !/^[6-9]\d{9}$/.test(form.mobile)) return ui.error('Please enter a valid 10-digit mobile number')
  busy.value = true
  try {
    const user = await userApi.updateProfile({
      name: `${form.firstName.trim()} ${form.lastName.trim()}`.trim(),
      gender: form.gender || null,
      email: form.email.trim() || null,
      mobile: form.mobile.trim() || null
    })
    auth.setUser(user)
    editing.value[section] = false
    ui.notify('Your profile has been updated')
  } catch (e) {
    ui.error(e)
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <div class="profile">
    <section>
      <div class="sec-head">
        <h2>Personal Information</h2>
        <button class="link-btn" @click="toggle('personal')">{{ editing.personal ? 'Cancel' : 'Edit' }}</button>
      </div>
      <div class="row gap-12 fields">
        <label class="field"><input v-model="form.firstName" :disabled="!editing.personal" placeholder=" " /><span>First Name</span></label>
        <label class="field"><input v-model="form.lastName" :disabled="!editing.personal" placeholder=" " /><span>Last Name</span></label>
        <button v-if="editing.personal" class="btn btn-blue save" :disabled="busy" @click="save('personal')">Save</button>
      </div>
      <p class="label">Your Gender</p>
      <div class="row gap-16">
        <label class="check"><input v-model="form.gender" type="radio" value="MALE" :disabled="!editing.personal" /> Male</label>
        <label class="check"><input v-model="form.gender" type="radio" value="FEMALE" :disabled="!editing.personal" /> Female</label>
      </div>
    </section>

    <section>
      <div class="sec-head">
        <h2>Email Address</h2>
        <button class="link-btn" @click="toggle('email')">{{ editing.email ? 'Cancel' : 'Edit' }}</button>
      </div>
      <div class="row gap-12 fields">
        <label class="field"><input v-model="form.email" type="email" :disabled="!editing.email" placeholder=" " /><span>Email Address</span></label>
        <button v-if="editing.email" class="btn btn-blue save" :disabled="busy" @click="save('email')">Save</button>
      </div>
    </section>

    <section>
      <div class="sec-head">
        <h2>Mobile Number</h2>
        <button class="link-btn" @click="toggle('mobile')">{{ editing.mobile ? 'Cancel' : 'Edit' }}</button>
      </div>
      <div class="row gap-12 fields">
        <label class="field"><input v-model="form.mobile" maxlength="10" inputmode="numeric" :disabled="!editing.mobile" placeholder=" " /><span>Mobile Number</span></label>
        <button v-if="editing.mobile" class="btn btn-blue save" :disabled="busy" @click="save('mobile')">Save</button>
      </div>
    </section>

    <section class="faq">
      <h3>FAQs</h3>
      <p class="q">What happens when I update my email address (or mobile number)?</p>
      <p class="a">Your login email id (or mobile number) changes, likewise. You'll receive all your account related communication on your updated email address (or mobile number).</p>
      <p class="q">When will my account be updated with the new email address (or mobile number)?</p>
      <p class="a">It happens as soon as you save the changes.</p>
    </section>
  </div>
</template>

<style scoped>
.profile {
  padding: 24px 32px 32px;
}

section {
  margin-bottom: 36px;
}

.sec-head {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 20px;
}

h2 {
  font-size: 18px;
  font-weight: 600;
}

.fields {
  flex-wrap: wrap;
}

.field {
  width: 270px;
}

.field input:disabled {
  background: #fafafa;
  color: var(--text-2);
  cursor: not-allowed;
}

.save {
  height: 50px;
  width: 140px;
}

.label {
  margin: 20px 0 8px;
  font-size: 14px;
}

.faq h3 {
  font-size: 18px;
  margin-bottom: 18px;
}

.q {
  font-weight: 600;
  margin-top: 14px;
}

.a {
  margin-top: 8px;
  line-height: 1.6;
}

@media (max-width: 600px) {
  .profile { padding: 20px; }
  .field { width: 100%; }
}
</style>
