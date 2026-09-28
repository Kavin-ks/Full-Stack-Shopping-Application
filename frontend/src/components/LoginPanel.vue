<script setup>
import { ref, computed, nextTick, onBeforeUnmount } from 'vue'
import { authApi } from '../api'
import { useSession } from '../session'
import { useUiStore } from '../stores/ui'

const emit = defineEmits(['success'])
const props = defineProps({ initialMode: { type: String, default: 'login' } })

const { afterLogin } = useSession()
const ui = useUiStore()

const mode = ref(props.initialMode) // 'login' | 'signup'
const step = ref('identify') // 'identify' | 'otp'
const identifier = ref('')
const name = ref('')
const otp = ref(['', '', '', '', '', ''])
const demoOtp = ref('')
const isNew = ref(false)
const error = ref('')
const busy = ref(false)
const resendIn = ref(0)
const otpInputs = ref([])
let timer = null

const title = computed(() => (mode.value === 'signup' || isNew.value ? "Looks like you're new here!" : 'Login'))
const subtitle = computed(() =>
  mode.value === 'signup' || isNew.value
    ? 'Sign up with your mobile number to get started'
    : 'Get access to your Orders, Wishlist and Recommendations'
)
const masked = computed(() => {
  const v = identifier.value.trim()
  if (v.includes('@')) return v
  return v.length >= 10 ? `+91${v.slice(-10)}` : v
})

async function requestOtp() {
  error.value = ''
  if (!identifier.value.trim()) {
    error.value = 'Please enter valid Email ID/Mobile number'
    return
  }
  busy.value = true
  try {
    const res = await authApi.requestOtp(identifier.value)
    identifier.value = res.identifier
    isNew.value = res.newUser
    demoOtp.value = res.otp || ''
    otp.value = ['', '', '', '', '', '']
    step.value = 'otp'
    startResendTimer()
    await nextTick()
    otpInputs.value[0]?.focus()
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}

async function verify() {
  error.value = ''
  const code = otp.value.join('')
  if (code.length !== 6) {
    error.value = 'Please enter the 6-digit OTP'
    return
  }
  if (isNew.value && !name.value.trim()) {
    error.value = 'Please enter your full name'
    return
  }
  busy.value = true
  try {
    const res = await authApi.verify(identifier.value, code, name.value.trim() || undefined)
    await afterLogin(res.token, res.user)
    ui.notify(isNew.value ? `Welcome to Flipkart, ${res.user.name.split(' ')[0]}!` : 'Login successful')
    emit('success', res.user)
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}

function onOtpInput(i, e) {
  const digits = e.target.value.replace(/\D/g, '')
  if (digits.length > 1) {
    // pasted the whole code
    digits.slice(0, 6).split('').forEach((d, k) => (otp.value[k] = d))
    otpInputs.value[Math.min(digits.length, 6) - 1]?.focus()
    return
  }
  otp.value[i] = digits
  if (digits && i < 5) otpInputs.value[i + 1]?.focus()
}

function onOtpKey(i, e) {
  if (e.key === 'Backspace' && !otp.value[i] && i > 0) otpInputs.value[i - 1]?.focus()
}

function useDemoOtp() {
  otp.value = demoOtp.value.split('')
  otpInputs.value[5]?.focus()
}

function startResendTimer() {
  resendIn.value = 30
  clearInterval(timer)
  timer = setInterval(() => {
    resendIn.value -= 1
    if (resendIn.value <= 0) clearInterval(timer)
  }, 1000)
}

function change() {
  step.value = 'identify'
  error.value = ''
  demoOtp.value = ''
}

onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div class="login">
    <aside class="side">
      <h2>{{ title }}</h2>
      <p>{{ subtitle }}</p>
      <svg class="art" viewBox="0 0 200 150" aria-hidden="true">
        <rect x="40" y="30" width="120" height="90" rx="6" fill="#fff" opacity=".95" />
        <rect x="52" y="44" width="54" height="8" rx="2" fill="#2874f0" opacity=".35" />
        <rect x="52" y="58" width="96" height="6" rx="2" fill="#e0e0e0" />
        <rect x="52" y="70" width="80" height="6" rx="2" fill="#e0e0e0" />
        <rect x="52" y="92" width="60" height="16" rx="2" fill="#fb641b" />
        <circle cx="150" cy="32" r="18" fill="#ffe500" />
        <path d="M142 32l6 6 10-12" stroke="#2874f0" stroke-width="4" fill="none" stroke-linecap="round" stroke-linejoin="round" />
        <rect x="18" y="96" width="30" height="40" rx="4" fill="#fff" opacity=".6" />
        <rect x="160" y="82" width="26" height="54" rx="4" fill="#fff" opacity=".4" />
      </svg>
    </aside>

    <section class="form">
      <form v-if="step === 'identify'" @submit.prevent="requestOtp">
        <label class="underline-field" :class="{ invalid: error }">
          <input v-model="identifier" type="text" autocomplete="username" placeholder=" " autofocus />
          <span>Enter Email/Mobile number</span>
        </label>
        <p v-if="error" class="err">{{ error }}</p>
        <p class="terms">
          By continuing, you agree to Flipkart's <b>Terms of Use</b> and <b>Privacy Policy</b>.
        </p>
        <button class="btn btn-orange btn-block" :disabled="busy">
          {{ busy ? 'Please wait…' : mode === 'signup' ? 'Continue' : 'Request OTP' }}
        </button>
        <button type="button" class="switch" @click="mode = mode === 'signup' ? 'login' : 'signup'">
          {{ mode === 'signup' ? 'Existing User? Log in' : 'New to Flipkart? Create an account' }}
        </button>
      </form>

      <form v-else @submit.prevent="verify">
        <p class="sent">
          Please enter the OTP sent to <br />
          <b>{{ masked }}</b>
          <button type="button" class="link-btn" @click="change">Change</button>
        </p>

        <div v-if="demoOtp" class="demo">
          <span>Demo mode — your OTP is <b>{{ demoOtp }}</b></span>
          <button type="button" class="link-btn" @click="useDemoOtp">Autofill</button>
        </div>

        <div class="otp" @paste.prevent="onOtpInput(0, { target: { value: $event.clipboardData.getData('text') } })">
          <input
            v-for="(d, i) in otp"
            :key="i"
            :ref="(el) => (otpInputs[i] = el)"
            :value="d"
            inputmode="numeric"
            maxlength="6"
            :aria-label="`OTP digit ${i + 1}`"
            @input="onOtpInput(i, $event)"
            @keydown="onOtpKey(i, $event)"
          />
        </div>

        <label v-if="isNew" class="underline-field">
          <input v-model="name" type="text" autocomplete="name" placeholder=" " />
          <span>Enter your full name</span>
        </label>

        <p v-if="error" class="err">{{ error }}</p>

        <button class="btn btn-orange btn-block" :disabled="busy">
          {{ busy ? 'Verifying…' : isNew ? 'Sign Up' : 'Verify' }}
        </button>

        <p class="resend">
          Not received your code?
          <button type="button" class="link-btn" :disabled="resendIn > 0" @click="requestOtp">
            {{ resendIn > 0 ? `Resend code in 00:${String(resendIn).padStart(2, '0')}` : 'Resend code' }}
          </button>
        </p>
      </form>
    </section>
  </div>
</template>

<style scoped>
.login {
  display: flex;
  width: 100%;
  min-height: 528px;
  background: #fff;
}

.side {
  width: 40%;
  flex-shrink: 0;
  background: var(--fk-blue);
  color: #fff;
  padding: 40px 33px;
  display: flex;
  flex-direction: column;
}

.side h2 {
  font-size: 28px;
  font-weight: 600;
}

.side p {
  margin-top: 16px;
  font-size: 18px;
  line-height: 1.5;
  color: #dbdbdb;
}

.art {
  margin-top: auto;
  width: 100%;
}

.form {
  flex: 1;
  padding: 56px 35px 16px;
  display: flex;
  flex-direction: column;
}

.form form {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.underline-field {
  position: relative;
  display: block;
  margin-bottom: 24px;
}

.underline-field input {
  width: 100%;
  border: none;
  border-bottom: 1px solid var(--border);
  padding: 22px 0 8px;
  font-size: 16px;
  outline: none;
  background: transparent;
}

.underline-field input:focus {
  border-bottom: 2px solid var(--fk-blue);
}

.underline-field span {
  position: absolute;
  left: 0;
  top: 22px;
  color: var(--text-2);
  font-size: 16px;
  pointer-events: none;
  transition: 0.15s;
}

.underline-field input:focus + span,
.underline-field input:not(:placeholder-shown) + span {
  top: 0;
  font-size: 12px;
}

.underline-field.invalid input {
  border-bottom-color: var(--fk-red);
}

.err {
  margin: -14px 0 16px;
  color: #ff6161;
  font-size: 12px;
}

.terms {
  font-size: 12px;
  color: var(--text-2);
  margin: 8px 0 18px;
}

.terms b {
  color: var(--fk-blue);
  font-weight: 500;
}

.switch {
  margin-top: auto;
  padding: 16px 0;
  border: none;
  background: none;
  color: var(--fk-blue);
  font-weight: 600;
  font-size: 14px;
}

.sent {
  text-align: center;
  color: var(--text-2);
  line-height: 1.7;
  margin-bottom: 16px;
}

.sent b {
  color: var(--text);
  margin-right: 6px;
}

.demo {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 20px;
  border-radius: 4px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  font-size: 13px;
}

.otp {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 26px;
}

.otp input {
  width: 42px;
  height: 46px;
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  border: none;
  border-bottom: 2px solid var(--border);
  outline: none;
}

.otp input:focus {
  border-bottom-color: var(--fk-blue);
}

.resend {
  margin-top: 20px;
  text-align: center;
  color: var(--text-2);
  font-size: 14px;
}

.resend .link-btn:disabled {
  color: var(--text-2);
  cursor: default;
  text-decoration: none;
}

@media (max-width: 640px) {
  .login { flex-direction: column; min-height: 0; }
  .side { width: 100%; padding: 24px; }
  .side p { font-size: 15px; }
  .art { display: none; }
  .form { padding: 28px 24px; }
}
</style>
