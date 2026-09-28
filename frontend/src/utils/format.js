const inr = new Intl.NumberFormat('en-IN')

export const rupees = (n) => `₹${inr.format(Math.round(n || 0))}`
export const number = (n) => inr.format(n || 0)

export function ratingClass(r) {
  if (r >= 3.5) return ''
  if (r >= 2.5) return 'mid'
  return 'low'
}

export function formatDate(d, opts = { day: 'numeric', month: 'short', year: 'numeric' }) {
  return new Date(d).toLocaleDateString('en-IN', opts)
}

export function deliveryDate(days = 3) {
  const d = new Date()
  d.setDate(d.getDate() + days)
  return d.toLocaleDateString('en-IN', { weekday: 'short', month: 'short', day: 'numeric' })
}

export const FREE_DELIVERY_THRESHOLD = 500
export const DELIVERY_CHARGE = 40

/** Same rules as cart-service, used for the guest (not logged in) cart. */
export function summarize(lines) {
  let itemCount = 0
  let totalMrp = 0
  let price = 0
  lines.forEach((l) => {
    if (!l.inStock) return
    itemCount += l.quantity
    totalMrp += l.product.mrp * l.quantity
    price += l.product.price * l.quantity
  })
  const deliveryCharge = itemCount === 0 || price >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_CHARGE
  const totalDiscount = totalMrp - price
  return { itemCount, totalMrp, totalDiscount, deliveryCharge, totalAmount: price + deliveryCharge, savings: totalDiscount }
}

export const ORDER_STATUS = {
  PLACED: { label: 'Order Confirmed', color: '#2874f0' },
  SHIPPED: { label: 'Shipped', color: '#ff9f00' },
  OUT_FOR_DELIVERY: { label: 'Out For Delivery', color: '#ff9f00' },
  DELIVERED: { label: 'Delivered', color: '#26a541' },
  CANCELLED: { label: 'Cancelled', color: '#ff6161' }
}

export const PAYMENT_LABEL = {
  UPI: 'UPI',
  CARD: 'Credit / Debit Card',
  NETBANKING: 'Net Banking',
  COD: 'Cash on Delivery'
}

export const INDIAN_STATES = [
  'Andaman & Nicobar Islands', 'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chandigarh',
  'Chhattisgarh', 'Dadra & Nagar Haveli & Daman & Diu', 'Delhi', 'Goa', 'Gujarat', 'Haryana',
  'Himachal Pradesh', 'Jammu & Kashmir', 'Jharkhand', 'Karnataka', 'Kerala', 'Ladakh', 'Lakshadweep',
  'Madhya Pradesh', 'Maharashtra', 'Manipur', 'Meghalaya', 'Mizoram', 'Nagaland', 'Odisha', 'Puducherry',
  'Punjab', 'Rajasthan', 'Sikkim', 'Tamil Nadu', 'Telangana', 'Tripura', 'Uttar Pradesh', 'Uttarakhand',
  'West Bengal'
]
