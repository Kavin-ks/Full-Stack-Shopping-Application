<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi, productApi, orderApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { useUiStore } from '../stores/ui'
import { useSession } from '../session'
import { rupees, summarize, deliveryDate } from '../utils/format'
import PriceDetails from '../components/PriceDetails.vue'
import PriceLine from '../components/PriceLine.vue'
import AddressForm from '../components/AddressForm.vue'
import Icon from '../components/Icon.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const ui = useUiStore()
const { logout } = useSession()

const step = ref(2)
const addresses = ref([])
const selectedId = ref(null)
const addressFormOpen = ref(false)
const editing = ref(null)
const savingAddress = ref(false)
const buyNowLines = ref([])
const loading = ref(true)
const placing = ref(false)

const payment = ref('UPI')
const upiId = ref('')
const card = ref({ number: '', expiry: '', cvv: '' })
const bank = ref('')
const payError = ref('')

const BANKS = ['State Bank of India', 'HDFC Bank', 'ICICI Bank', 'Axis Bank', 'Kotak Mahindra Bank', 'Punjab National Bank', 'Bank of Baroda', 'Canara Bank']

const isBuyNow = computed(() => !!route.query.buyNow)
const lines = computed(() => (isBuyNow.value ? buyNowLines.value : cart.items))
const summary = computed(() => (isBuyNow.value ? summarize(buyNowLines.value) : cart.summary))
const selected = computed(() => addresses.value.find((a) => a.id === selectedId.value))

onMounted(async () => {
  try {
    const [list] = await Promise.all([
      userApi.addresses(),
      isBuyNow.value ? loadBuyNow() : cart.load()
    ])
    addresses.value = list
    selectedId.value = (list.find((a) => a.defaultAddress) || list[0])?.id || null
    if (!list.length) addressFormOpen.value = true
    if (!isBuyNow.value && !cart.items.length) {
      ui.notify('Your cart is empty')
      router.replace('/cart')
    }
  } catch (e) {
    ui.error(e)
  } finally {
    loading.value = false
  }
})

async function loadBuyNow() {
  const [p] = await productApi.batch([route.query.buyNow])
  if (!p || p.stock <= 0) {
    ui.error(p ? `${p.title} is out of stock` : 'Product not found')
    router.replace('/')
    return
  }
  const max = Math.min(p.stock, 10)
  buyNowLines.value = [{ productId: p.id, quantity: Math.min(Number(route.query.qty) || 1, max), maxQuantity: max, inStock: true, product: p }]
}

// ---------------------------------------------------------------- address step
async function saveAddress(data) {
  savingAddress.value = true
  try {
    addresses.value = editing.value
      ? await userApi.updateAddress(editing.value.id, data)
      : await userApi.addAddress(data)
    if (!editing.value) selectedId.value = addresses.value[0].id
    addressFormOpen.value = false
    editing.value = null
    if (!isEditOnly.value) deliverHere()
  } catch (e) {
    ui.error(e)
  } finally {
    savingAddress.value = false
  }
}
const isEditOnly = ref(false)

function edit(a) {
  editing.value = a
  isEditOnly.value = true
  addressFormOpen.value = true
}

function addNew() {
  editing.value = null
  isEditOnly.value = false
  addressFormOpen.value = true
}

function deliverHere() {
  if (!selected.value) return ui.error('Please select a delivery address')
  step.value = 3
}

// ---------------------------------------------------------------- summary step
async function changeQty(line, delta) {
  const q = line.quantity + delta
  if (isBuyNow.value) line.quantity = q
  else await cart.updateQuantity(line.productId, q)
}

async function removeLine(line) {
  if (isBuyNow.value) {
    router.replace('/')
    return
  }
  await cart.remove(line.productId, line.product.title)
  if (!cart.items.length) router.replace('/cart')
}

// ---------------------------------------------------------------- payment step
function luhn(num) {
  let sum = 0
  let dbl = false
  for (let i = num.length - 1; i >= 0; i--) {
    let d = Number(num[i])
    if (dbl) {
      d *= 2
      if (d > 9) d -= 9
    }
    sum += d
    dbl = !dbl
  }
  return sum % 10 === 0
}

function formatCard(e) {
  const digits = e.target.value.replace(/\D/g, '').slice(0, 16)
  card.value.number = digits.replace(/(.{4})/g, '$1 ').trim()
}

function formatExpiry(e) {
  const d = e.target.value.replace(/\D/g, '').slice(0, 4)
  card.value.expiry = d.length > 2 ? `${d.slice(0, 2)}/${d.slice(2)}` : d
}

function validatePayment() {
  if (payment.value === 'UPI' && !/^[\w.-]{2,}@[a-zA-Z]{2,}$/.test(upiId.value.trim())) {
    return 'Please enter a valid UPI ID (e.g. name@okaxis)'
  }
  if (payment.value === 'CARD') {
    const num = card.value.number.replace(/\s/g, '')
    if (num.length < 15 || !luhn(num)) return 'Please enter a valid card number'
    const [mm, yy] = card.value.expiry.split('/').map(Number)
    const now = new Date()
    const exp = new Date(2000 + (yy || 0), mm || 0, 0)
    if (!mm || mm > 12 || exp < now) return 'Please enter a valid expiry date'
    if (!/^\d{3,4}$/.test(card.value.cvv)) return 'Please enter a valid CVV'
  }
  if (payment.value === 'NETBANKING' && !bank.value) return 'Please select your bank'
  return ''
}

async function placeOrder() {
  payError.value = validatePayment()
  if (payError.value) return
  placing.value = true
  try {
    const a = selected.value
    const order = await orderApi.place({
      paymentMethod: payment.value,
      address: {
        name: a.name, mobile: a.mobile, pincode: a.pincode, locality: a.locality, address: a.address,
        city: a.city, state: a.state, landmark: a.landmark, alternatePhone: a.alternatePhone, type: a.type
      },
      items: isBuyNow.value ? buyNowLines.value.map((l) => ({ productId: l.productId, quantity: l.quantity })) : undefined
    })
    await cart.load()
    router.replace({ name: 'order-success', params: { id: order.id } })
  } catch (e) {
    ui.error(e)
    placing.value = false
  }
}
</script>

<template>
  <div class="container checkout">
    <div v-if="loading" class="card center-box"><div class="spinner" /></div>

    <div v-else class="layout">
      <div class="steps">
        <!-- 1. LOGIN -->
        <section class="card step done">
          <header>
            <span class="num">1</span>
            <div class="h-text">
              <h2>Login <Icon name="check" :size="18" class="tick" /></h2>
              <p><b>{{ auth.user.name }}</b> {{ auth.contact }}</p>
            </div>
            <button class="btn btn-outline btn-sm change" @click="logout">Change</button>
          </header>
        </section>

        <!-- 2. DELIVERY ADDRESS -->
        <section class="card step" :class="{ active: step === 2, done: step > 2 }">
          <header v-if="step !== 2">
            <span class="num">2</span>
            <div class="h-text">
              <h2>Delivery Address <Icon name="check" :size="18" class="tick" /></h2>
              <p v-if="selected"><b>{{ selected.name }}</b> {{ selected.address }}, {{ selected.locality }}, {{ selected.city }}, {{ selected.state }} - <b>{{ selected.pincode }}</b></p>
            </div>
            <button class="btn btn-outline btn-sm change" @click="step = 2">Change</button>
          </header>
          <template v-else>
            <header class="blue-head"><span class="num">2</span><h2>Delivery Address</h2></header>
            <div class="addr-list">
              <label v-for="a in addresses" :key="a.id" class="addr" :class="{ on: a.id === selectedId }">
                <input v-model="selectedId" type="radio" :value="a.id" name="address" />
                <div class="addr-body">
                  <p class="row gap-8">
                    <b>{{ a.name }}</b> <span class="type">{{ a.type }}</span> <b>{{ a.mobile }}</b>
                    <button type="button" class="link-btn edit" @click.prevent="edit(a)">Edit</button>
                  </p>
                  <p class="addr-text">{{ a.address }}, {{ a.locality }}, {{ a.city }}, {{ a.state }} - <b>{{ a.pincode }}</b></p>
                  <button v-if="a.id === selectedId && !addressFormOpen" class="btn btn-orange deliver-btn" @click.prevent="deliverHere">
                    Deliver Here
                  </button>
                </div>
              </label>
            </div>
            <div v-if="addressFormOpen" class="form-wrap">
              <p class="form-title">{{ editing ? 'Edit Address' : 'Add a New Address' }}</p>
              <AddressForm
                :key="editing?.id || 'new'"
                :initial="editing || { name: auth.user.name, mobile: auth.user.mobile }"
                :submit-label="editing ? 'Save' : 'Save and Deliver Here'"
                :busy="savingAddress"
                @submit="saveAddress"
                @cancel="addressFormOpen = false; editing = null"
              />
            </div>
            <button v-else class="add-new" @click="addNew"><Icon name="plus" :size="18" /> Add a new address</button>
          </template>
        </section>

        <!-- 3. ORDER SUMMARY -->
        <section class="card step" :class="{ active: step === 3, done: step > 3 }">
          <header v-if="step !== 3">
            <span class="num">3</span>
            <div class="h-text">
              <h2>Order Summary <Icon v-if="step > 3" name="check" :size="18" class="tick" /></h2>
              <p v-if="step > 3"><b>{{ summary.itemCount }} Item{{ summary.itemCount === 1 ? '' : 's' }}</b></p>
            </div>
            <button v-if="step > 3" class="btn btn-outline btn-sm change" @click="step = 3">Change</button>
          </header>
          <template v-else>
            <header class="blue-head"><span class="num">3</span><h2>Order Summary</h2></header>
            <article v-for="line in lines" :key="line.productId" class="line">
              <div class="l-img">
                <img :src="line.product.thumbnail" :alt="line.product.title" />
                <div class="qty">
                  <button :disabled="line.quantity <= 1" aria-label="Decrease quantity" @click="changeQty(line, -1)"><Icon name="minus" :size="14" /></button>
                  <span>{{ line.quantity }}</span>
                  <button :disabled="line.quantity >= line.maxQuantity" aria-label="Increase quantity" @click="changeQty(line, 1)"><Icon name="plus" :size="14" /></button>
                </div>
              </div>
              <div class="l-info">
                <p class="l-title">{{ line.product.title }}</p>
                <p class="muted small">Seller: RetailHub</p>
                <PriceLine :price="line.product.price" :mrp="line.product.mrp" :discount="line.product.discount" />
                <button class="text-btn remove" @click="removeLine(line)">Remove</button>
              </div>
              <p class="l-eta">Delivery by {{ deliveryDate(4) }}</p>
            </article>
            <div class="continue-bar">
              <p>Order confirmation will be sent to <b>{{ auth.user.email || auth.user.mobile }}</b></p>
              <button class="btn btn-orange" @click="step = 4">Continue</button>
            </div>
          </template>
        </section>

        <!-- 4. PAYMENT -->
        <section class="card step" :class="{ active: step === 4 }">
          <header v-if="step !== 4">
            <span class="num">4</span>
            <div class="h-text"><h2>Payment Options</h2></div>
          </header>
          <template v-else>
            <header class="blue-head"><span class="num">4</span><h2>Payment Options</h2></header>
            <div class="pay">
              <label class="pay-opt" :class="{ on: payment === 'UPI' }">
                <input v-model="payment" type="radio" value="UPI" />
                <div class="pay-body">
                  <p class="pay-title"><Icon name="upi" :size="18" /> UPI</p>
                  <p class="muted small">Pay by any UPI app</p>
                  <div v-if="payment === 'UPI'" class="pay-fields">
                    <label class="field"><input v-model="upiId" placeholder=" " /><span>Enter your UPI ID</span></label>
                  </div>
                </div>
              </label>

              <label class="pay-opt" :class="{ on: payment === 'CARD' }">
                <input v-model="payment" type="radio" value="CARD" />
                <div class="pay-body">
                  <p class="pay-title"><Icon name="card" :size="18" /> Credit / Debit / ATM Card</p>
                  <p class="muted small">Add and secure cards as per RBI guidelines</p>
                  <div v-if="payment === 'CARD'" class="pay-fields card-fields">
                    <label class="field full"><input :value="card.number" inputmode="numeric" placeholder=" " autocomplete="cc-number" @input="formatCard" /><span>Card Number</span></label>
                    <label class="field"><input :value="card.expiry" inputmode="numeric" placeholder=" " autocomplete="cc-exp" @input="formatExpiry" /><span>Valid thru (MM/YY)</span></label>
                    <label class="field"><input v-model="card.cvv" type="password" maxlength="4" inputmode="numeric" placeholder=" " autocomplete="cc-csc" /><span>CVV</span></label>
                    <p class="muted small full">Card details are validated in your browser and are never stored.</p>
                  </div>
                </div>
              </label>

              <label class="pay-opt" :class="{ on: payment === 'NETBANKING' }">
                <input v-model="payment" type="radio" value="NETBANKING" />
                <div class="pay-body">
                  <p class="pay-title"><Icon name="bank" :size="18" /> Net Banking</p>
                  <div v-if="payment === 'NETBANKING'" class="pay-fields">
                    <label class="field">
                      <select v-model="bank">
                        <option value="" disabled>Select your bank</option>
                        <option v-for="b in BANKS" :key="b">{{ b }}</option>
                      </select>
                      <span>Bank</span>
                    </label>
                  </div>
                </div>
              </label>

              <label class="pay-opt" :class="{ on: payment === 'COD' }">
                <input v-model="payment" type="radio" value="COD" />
                <div class="pay-body">
                  <p class="pay-title"><Icon name="cash" :size="18" /> Cash on Delivery</p>
                  <p class="muted small">Pay in cash or UPI when your order is delivered</p>
                </div>
              </label>

              <p v-if="payError" class="pay-error">{{ payError }}</p>

              <div class="pay-bar">
                <button class="btn btn-orange pay-btn" :disabled="placing" @click="placeOrder">
                  {{ placing ? 'Processing…' : payment === 'COD' ? 'Confirm Order' : `Pay ${rupees(summary.totalAmount)}` }}
                </button>
              </div>
            </div>
          </template>
        </section>
      </div>

      <div class="side">
        <PriceDetails :summary="summary" />
      </div>
    </div>

    <div v-if="placing" class="processing">
      <div class="card proc-box">
        <div class="spinner" />
        <p>{{ payment === 'COD' ? 'Placing your order…' : 'Processing your payment securely…' }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.checkout {
  padding-top: 16px;
  padding-bottom: 16px;
  max-width: 1200px;
}

.layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.steps {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side {
  width: 380px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-h) + 16px);
}

.step header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 24px;
}

.num {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  border-radius: 2px;
  background: #f0f0f0;
  color: var(--fk-blue);
  font-size: 12px;
  font-weight: 600;
  display: grid;
  place-items: center;
  margin-top: 1px;
}

.h-text {
  flex: 1;
  min-width: 0;
}

.h-text h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-2);
  font-size: 16px;
  font-weight: 600;
  text-transform: uppercase;
}

.h-text p {
  margin-top: 6px;
  font-size: 14px;
}

.tick {
  color: var(--fk-blue);
}

.change {
  color: var(--fk-blue);
  flex-shrink: 0;
}

.blue-head {
  background: var(--fk-blue);
  color: #fff;
  align-items: center !important;
  border-radius: 4px 4px 0 0;
}

.blue-head .num {
  background: #fff;
}

.blue-head h2 {
  font-size: 16px;
  font-weight: 600;
  text-transform: uppercase;
}

.addr {
  display: flex;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
}

.addr.on {
  background: #f5faff;
}

.addr input {
  margin-top: 3px;
  accent-color: var(--fk-blue);
}

.addr-body {
  flex: 1;
}

.type {
  padding: 2px 6px;
  border-radius: 2px;
  background: #f0f0f0;
  color: var(--text-2);
  font-size: 11px;
  font-weight: 600;
}

.edit {
  margin-left: auto;
  text-transform: uppercase;
  font-size: 13px;
}

.addr-text {
  margin-top: 6px;
  font-size: 14px;
}

.deliver-btn {
  margin-top: 16px;
  width: 200px;
}

.form-wrap {
  padding: 16px 24px 24px;
  background: #f5faff;
}

.form-title {
  color: var(--fk-blue);
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 14px;
}

.add-new {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 18px 24px;
  border: none;
  background: #fff;
  color: var(--fk-blue);
  font-weight: 600;
  font-size: 14px;
  text-align: left;
}

.line {
  display: flex;
  gap: 24px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-light);
}

.l-img {
  width: 112px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.l-img img {
  height: 100px;
  object-fit: contain;
}

.qty {
  display: flex;
  align-items: center;
  gap: 6px;
}

.qty button {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 1px solid #c2c2c2;
  background: #fff;
  display: grid;
  place-items: center;
}

.qty button:disabled {
  opacity: 0.4;
}

.qty span {
  width: 40px;
  height: 26px;
  display: grid;
  place-items: center;
  border: 1px solid #c2c2c2;
  border-radius: 2px;
}

.l-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.l-title {
  font-size: 16px;
}

.small {
  font-size: 12px;
}

.remove {
  align-self: flex-start;
  margin-top: 8px;
}

.l-eta {
  width: 200px;
  font-size: 13px;
}

.continue-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 24px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.06);
}

.continue-bar .btn {
  width: 200px;
}

.pay-opt {
  display: flex;
  gap: 16px;
  padding: 18px 24px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
}

.pay-opt.on {
  background: #f5faff;
}

.pay-opt input {
  margin-top: 3px;
  accent-color: var(--fk-blue);
}

.pay-body {
  flex: 1;
}

.pay-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 15px;
}

.pay-fields {
  margin-top: 14px;
  max-width: 420px;
}

.card-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.card-fields .full {
  grid-column: 1 / -1;
}

.pay-error {
  padding: 12px 24px 0;
  color: #ff6161;
  font-size: 13px;
}

.pay-bar {
  display: flex;
  justify-content: flex-end;
  padding: 16px 24px;
}

.pay-btn {
  min-width: 240px;
  height: 52px;
  font-size: 16px;
}

.processing {
  position: fixed;
  inset: 0;
  z-index: 150;
  background: rgba(255, 255, 255, 0.75);
  display: grid;
  place-items: center;
}

.proc-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 32px 40px;
  box-shadow: var(--shadow-md);
  font-weight: 500;
}

@media (max-width: 960px) {
  .layout { flex-direction: column-reverse; }
  .side { width: 100%; position: static; }
  .l-eta { display: none; }
}
</style>
