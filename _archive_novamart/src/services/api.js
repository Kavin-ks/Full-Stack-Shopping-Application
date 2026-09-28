// Persistent Data & API Service for Amazon / Instamart Store
// Stores user accounts, carts, orders, and addresses permanently in localStorage
// Synchronous & Asynchronous support with zero data loss across days/restarts.

const STORAGE_KEYS = {
  USERS: 'novamart_users',
  SESSION: 'novamart_active_session',
  USER_CART_PREFIX: 'novamart_cart_',
  USER_ORDERS_PREFIX: 'novamart_orders_',
  USER_PROFILE_PREFIX: 'novamart_profile_'
}

// Master Product Catalog (Amazon + Instamart categories)
export const CATALOG_PRODUCTS = [
  // --- Instamart Fresh & Groceries ---
  {
    id: 'inst-1',
    name: 'Fresh Alphonso Mangoes (Ratnagiri)',
    price: 499,
    originalPrice: 650,
    unit: '1 kg (approx 4-5 pcs)',
    category: 'Fresh Produce',
    emoji: '🥭',
    rating: 4.9,
    ratingCount: 3820,
    deliveryBadge: '⚡ 10 MINS',
    description: 'Naturally ripened, sweet and aromatic GI-tagged Ratnagiri Alphonso mangoes.',
    stock: 45
  },
  {
    id: 'inst-2',
    name: 'Organic Cavendish Bananas',
    price: 48,
    originalPrice: 60,
    unit: '6 pcs (approx 650g)',
    category: 'Fresh Produce',
    emoji: '🍌',
    rating: 4.8,
    ratingCount: 5120,
    deliveryBadge: '⚡ 10 MINS',
    description: 'Farm-fresh, pesticide-free ripe bananas, packed with potassium and natural sweetness.',
    stock: 60
  },
  {
    id: 'inst-3',
    name: 'Crisp Shimla Royal Apples',
    price: 180,
    originalPrice: 220,
    unit: '4 pcs (approx 500g)',
    category: 'Fresh Produce',
    emoji: '🍎',
    rating: 4.7,
    ratingCount: 1980,
    deliveryBadge: '⚡ 10 MINS',
    description: 'Hand-picked crisp juicy apples straight from orchards of Himachal Pradesh.',
    stock: 35
  },
  {
    id: 'inst-4',
    name: 'Hydroponic Tender Baby Spinach',
    price: 35,
    originalPrice: 50,
    unit: '200g bunch',
    category: 'Fresh Produce',
    emoji: '🥬',
    rating: 4.6,
    ratingCount: 890,
    deliveryBadge: '⚡ 10 MINS',
    description: 'Clean, soil-free hydroponically grown tender spinach leaves, washed and ready to cook.',
    stock: 25
  },

  // --- Dairy & Breakfast ---
  {
    id: 'dairy-1',
    name: 'Amul Taaza Homogenised Milk (Pack of 2)',
    price: 140,
    originalPrice: 148,
    unit: '1L x 2',
    category: 'Dairy & Breakfast',
    emoji: '🥛',
    rating: 4.9,
    ratingCount: 8400,
    deliveryBadge: '⚡ 10 MINS',
    description: 'Pure, fresh toned milk UHT treated with long shelf life. Rich source of calcium.',
    stock: 80
  },
  {
    id: 'dairy-2',
    name: 'Country Special Farm Fresh Brown Eggs',
    price: 110,
    originalPrice: 130,
    unit: '6 pcs tray',
    category: 'Dairy & Breakfast',
    emoji: '🥚',
    rating: 4.8,
    ratingCount: 4210,
    deliveryBadge: '⚡ 10 MINS',
    description: 'High protein, antibiotic-free nutritious brown eggs from free-range poultry.',
    stock: 50
  },
  {
    id: 'dairy-3',
    name: 'Epigamia Greek Yogurt (Blueberry)',
    price: 75,
    originalPrice: 85,
    unit: '120g cup',
    category: 'Dairy & Breakfast',
    emoji: '🫐',
    rating: 4.7,
    ratingCount: 1650,
    deliveryBadge: '⚡ 10 MINS',
    description: 'Real fruit chunks with thick Greek strained yogurt, zero preservatives and 6g protein.',
    stock: 30
  },

  // --- Snacks & Beverages ---
  {
    id: 'snack-1',
    name: 'Artisan Himalayan Salted Kettle Chips',
    price: 99,
    originalPrice: 120,
    unit: '150g bag',
    category: 'Snacks & Drinks',
    emoji: '🥔',
    rating: 4.6,
    ratingCount: 2310,
    deliveryBadge: '⚡ 10 MINS',
    description: 'Slow-cooked batch potato chips seasoned with pure pink Himalayan rock salt.',
    stock: 40
  },
  {
    id: 'snack-2',
    name: 'Ferrero Rocher Hazelnut Chocolates',
    price: 495,
    originalPrice: 550,
    unit: '16 pieces box',
    category: 'Snacks & Drinks',
    emoji: '🍫',
    rating: 4.9,
    ratingCount: 6540,
    deliveryBadge: '⚡ 10 MINS',
    description: 'Whole hazelnut dipped in smooth velvety filling, encased in a crispy wafer shell.',
    stock: 28
  },
  {
    id: 'snack-3',
    name: 'Blue Tokai Cold Brew Cans (Mocha)',
    price: 175,
    originalPrice: 195,
    unit: '250ml can',
    category: 'Snacks & Drinks',
    emoji: '☕',
    rating: 4.8,
    ratingCount: 1420,
    deliveryBadge: '⚡ 10 MINS',
    description: '18-hour slow steeped Arabica coffee infused with cocoa and low-fat dairy.',
    stock: 35
  },

  // --- Electronics & Mobiles (Amazon Super Saver) ---
  {
    id: 'tech-1',
    name: 'Apple MacBook Air 15" M3 Chip',
    price: 124990,
    originalPrice: 134900,
    unit: '16GB RAM, 512GB SSD - Midnight',
    category: 'Electronics',
    emoji: '💻',
    rating: 4.9,
    ratingCount: 3100,
    deliveryBadge: 'Prime Free One-Day',
    description: 'Blazing fast Apple M3 silicon with 18-hour battery life and Liquid Retina display.',
    stock: 12
  },
  {
    id: 'tech-2',
    name: 'iPhone 16 Pro Max 256GB',
    price: 139900,
    originalPrice: 144900,
    unit: 'Natural Titanium',
    category: 'Electronics',
    emoji: '📱',
    rating: 4.9,
    ratingCount: 7890,
    deliveryBadge: 'Prime Free One-Day',
    description: 'A18 Pro bionic chip, Grade 5 Titanium design, 48MP Fusion camera system.',
    stock: 15
  },
  {
    id: 'tech-3',
    name: 'Sony WH-1000XM5 Wireless Headphones',
    price: 26990,
    originalPrice: 34990,
    unit: 'Active Noise Cancelling - Black',
    category: 'Electronics',
    emoji: '🎧',
    rating: 4.8,
    ratingCount: 9450,
    deliveryBadge: 'Prime Free One-Day',
    description: 'Industry leading noise cancellation with two processors and 8 microphones.',
    stock: 22
  },
  {
    id: 'tech-4',
    name: 'Apple Watch Series 10 GPS 46mm',
    price: 46900,
    originalPrice: 49900,
    unit: 'Jet Black Aluminium Case',
    category: 'Electronics',
    emoji: '⌚',
    rating: 4.7,
    ratingCount: 1840,
    deliveryBadge: 'Prime Free One-Day',
    description: 'Thinnest Apple Watch ever with bigger wide-angle OLED display & sleep apnea tracking.',
    stock: 18
  },

  // --- Fashion & Lifestyle ---
  {
    id: 'fash-1',
    name: 'Nike Air Zoom Pegasus 41 Running Shoes',
    price: 8495,
    originalPrice: 10495,
    unit: 'Size UK 8, 9, 10 available',
    category: 'Fashion',
    emoji: '👟',
    rating: 4.7,
    ratingCount: 2900,
    deliveryBadge: 'Prime Free One-Day',
    description: 'Responsive cushioning with dual Air Zoom units and engineered breathable mesh.',
    stock: 20
  },
  {
    id: 'fash-2',
    name: 'Premium 100% Supima Cotton Crew T-Shirt',
    price: 1199,
    originalPrice: 1899,
    unit: 'Navy Blue - Regular Fit',
    category: 'Fashion',
    emoji: '👕',
    rating: 4.6,
    ratingCount: 1450,
    deliveryBadge: 'Prime Free One-Day',
    description: 'Long-staple Supima cotton for ultra-soft hand feel, pre-shrunk colorfast fabric.',
    stock: 35
  },
  {
    id: 'fash-3',
    name: 'Handcrafted Genuine Top-Grain Leather Wallet',
    price: 1499,
    originalPrice: 2499,
    unit: 'RFID Blocking - Vintage Tan',
    category: 'Fashion',
    emoji: '👛',
    rating: 4.8,
    ratingCount: 3800,
    deliveryBadge: 'Prime Free One-Day',
    description: 'Slim bifold wallet with 8 card slots, currency dividers and full RFID protection.',
    stock: 45
  },

  // --- Home & Kitchen ---
  {
    id: 'home-1',
    name: 'Philips Digital Air Fryer XL 4.2L',
    price: 7999,
    originalPrice: 11995,
    unit: 'Rapid Air Tech - 1400W',
    category: 'Home & Kitchen',
    emoji: '🍳',
    rating: 4.8,
    ratingCount: 11200,
    deliveryBadge: 'Prime Free One-Day',
    description: 'Fry, bake, grill, roast and reheat with up to 90% less oil. Touch digital preset menu.',
    stock: 16
  },
  {
    id: 'home-2',
    name: 'Ceramic Artisan Coffee Mug Set (Set of 4)',
    price: 899,
    originalPrice: 1499,
    unit: '350ml Matte Finish',
    category: 'Home & Kitchen',
    emoji: '☕',
    rating: 4.7,
    ratingCount: 920,
    deliveryBadge: 'Prime Free One-Day',
    description: 'Microwave and dishwasher safe high-fired stoneware mugs with ergonomic handles.',
    stock: 30
  }
]

// Default pre-seeded users for immediate login demonstration
const SEED_USERS = [
  {
    id: 'usr_kavin',
    name: 'Kavin Soorya',
    email: 'kavin@store.com',
    password: 'password123',
    phone: '+91 98765 43210',
    address: 'Plot 42, Green Valley Enclave, Anna Nagar, Chennai - 600040',
    memberTier: 'Prime & Instamart VIP',
    createdAt: '2026-09-01T10:00:00Z'
  },
  {
    id: 'usr_priya',
    name: 'Priya Sharma',
    email: 'priya@amzmart.com',
    password: 'password123',
    phone: '+91 98401 56789',
    address: 'Apt 504, Prestige Heights, Whitefield, Bengaluru - 560066',
    memberTier: 'Prime Member',
    createdAt: '2026-09-10T14:30:00Z'
  }
]

// Pre-seeded initial cart items for Kavin (so they are immediately available on first login)
const SEED_CARTS = {
  usr_kavin: {
    items: [
      {
        id: 'cart_seed_1',
        productId: 'inst-1',
        name: 'Fresh Alphonso Mangoes (Ratnagiri)',
        price: 499,
        originalPrice: 650,
        unit: '1 kg (approx 4-5 pcs)',
        quantity: 2,
        subTotal: 998,
        emoji: '🥭',
        deliveryBadge: '⚡ 10 MINS'
      },
      {
        id: 'cart_seed_2',
        productId: 'tech-3',
        name: 'Sony WH-1000XM5 Wireless Headphones',
        price: 26990,
        originalPrice: 34990,
        unit: 'Active Noise Cancelling - Black',
        quantity: 1,
        subTotal: 26990,
        emoji: '🎧',
        deliveryBadge: 'Prime Free One-Day'
      }
    ],
    appliedCoupon: 'INSTANT10',
    discountAmount: 2798.8
  }
}

// Pre-seeded initial past orders for Kavin
const SEED_ORDERS = {
  usr_kavin: [
    {
      orderId: 'ORD-2026-9812A',
      placedAt: '2026-09-26T18:45:00Z',
      deliveryType: '⚡ Instamart Instant (12 Mins)',
      status: 'Delivered',
      deliveryAddress: 'Plot 42, Green Valley Enclave, Anna Nagar, Chennai - 600040',
      paymentMethod: 'UPI (Google Pay)',
      items: [
        {
          productId: 'inst-2',
          name: 'Organic Cavendish Bananas',
          quantity: 2,
          price: 48,
          emoji: '🍌'
        },
        {
          productId: 'dairy-1',
          name: 'Amul Taaza Homogenised Milk (Pack of 2)',
          quantity: 1,
          price: 140,
          emoji: '🥛'
        },
        {
          productId: 'snack-1',
          name: 'Artisan Himalayan Salted Kettle Chips',
          quantity: 2,
          price: 99,
          emoji: '🥔'
        }
      ],
      itemTotal: 434,
      deliveryFee: 0,
      packagingFee: 15,
      discount: 40,
      totalAmount: 409
    },
    {
      orderId: 'ORD-2026-7734X',
      placedAt: '2026-09-18T11:15:00Z',
      deliveryType: '📦 Amazon Prime Free One-Day',
      status: 'Delivered',
      deliveryAddress: 'Plot 42, Green Valley Enclave, Anna Nagar, Chennai - 600040',
      paymentMethod: 'HDFC Credit Card ending in 4092',
      items: [
        {
          productId: 'tech-4',
          name: 'Apple Watch Series 10 GPS 46mm',
          quantity: 1,
          price: 46900,
          emoji: '⌚'
        }
      ],
      itemTotal: 46900,
      deliveryFee: 0,
      packagingFee: 0,
      discount: 2000,
      totalAmount: 44900
    }
  ]
}

// Safe LocalStorage Helpers
function loadFromStorage(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch (e) {
    console.warn(`Storage load failed for ${key}:`, e)
    return fallback
  }
}

function saveToStorage(key, data) {
  try {
    localStorage.setItem(key, JSON.stringify(data))
  } catch (e) {
    console.error(`Storage save failed for ${key}:`, e)
  }
}

// Initialize default data if not present
function initializeStore() {
  const existingUsers = loadFromStorage(STORAGE_KEYS.USERS, null)
  if (!existingUsers || existingUsers.length === 0) {
    saveToStorage(STORAGE_KEYS.USERS, SEED_USERS)
  }

  // Pre-seed Kavin's cart if not initialized
  const kavinCartKey = STORAGE_KEYS.USER_CART_PREFIX + 'usr_kavin'
  if (!loadFromStorage(kavinCartKey, null)) {
    saveToStorage(kavinCartKey, SEED_CARTS.usr_kavin)
  }

  // Pre-seed Kavin's orders if not initialized
  const kavinOrdersKey = STORAGE_KEYS.USER_ORDERS_PREFIX + 'usr_kavin'
  if (!loadFromStorage(kavinOrdersKey, null)) {
    saveToStorage(kavinOrdersKey, SEED_ORDERS.usr_kavin)
  }
}

// Run initialization immediately
initializeStore()

// Recalculate cart totals helper
function computeCartTotals(cartData) {
  const items = cartData.items || []
  const totalQuantity = items.reduce((sum, item) => sum + (item.quantity || 0), 0)
  const subTotal = items.reduce((sum, item) => sum + (item.price * (item.quantity || 0)), 0)
  
  let discountAmount = 0
  if (cartData.appliedCoupon === 'INSTANT10') {
    discountAmount = Math.round(subTotal * 0.1)
  } else if (cartData.appliedCoupon === 'AMZ50') {
    discountAmount = subTotal > 500 ? 50 : 0
  } else if (cartData.appliedCoupon === 'SUPER100') {
    discountAmount = subTotal > 1000 ? 100 : 0
  }

  const deliveryFee = subTotal > 499 || subTotal === 0 ? 0 : 29
  const packagingFee = subTotal > 0 ? 15 : 0
  const finalTotal = Math.max(0, subTotal - discountAmount + deliveryFee + packagingFee)

  return {
    items,
    totalQuantity,
    subTotal,
    discountAmount,
    deliveryFee,
    packagingFee,
    finalTotal,
    appliedCoupon: cartData.appliedCoupon || ''
  }
}

export const api = {
  // --- AUTHENTICATION & USER MANAGEMENT ---
  getActiveSession: () => {
    return loadFromStorage(STORAGE_KEYS.SESSION, null)
  },

  setActiveSession: (user) => {
    saveToStorage(STORAGE_KEYS.SESSION, user)
    return user
  },

  clearActiveSession: () => {
    localStorage.removeItem(STORAGE_KEYS.SESSION)
  },

  login: async (email, password) => {
    // Artificial slight async delay for realistic auth feedback
    await new Promise(r => setTimeout(r, 250))
    const users = loadFromStorage(STORAGE_KEYS.USERS, SEED_USERS)
    const normalizedEmail = email.trim().toLowerCase()
    const user = users.find(u => u.email.toLowerCase() === normalizedEmail)

    if (!user) {
      throw new Error('No account found with this email address. Please check or register a new account.')
    }
    if (user.password !== password) {
      throw new Error('Incorrect password. Please verify your credentials and try again.')
    }

    // Save active session
    saveToStorage(STORAGE_KEYS.SESSION, user)
    return user
  },

  register: async ({ name, email, password, phone, address }) => {
    await new Promise(r => setTimeout(r, 300))
    const users = loadFromStorage(STORAGE_KEYS.USERS, SEED_USERS)
    const normalizedEmail = email.trim().toLowerCase()

    if (users.some(u => u.email.toLowerCase() === normalizedEmail)) {
      throw new Error('An account with this email already exists. Please sign in instead.')
    }

    const newUser = {
      id: 'usr_' + Date.now(),
      name: name.trim(),
      email: normalizedEmail,
      password: password,
      phone: phone || '+91 90000 00000',
      address: address || '100 Central Boulevard, City Center',
      memberTier: 'Prime Member',
      createdAt: new Date().toISOString()
    }

    users.push(newUser)
    saveToStorage(STORAGE_KEYS.USERS, users)

    // Initialize empty cart and orders for the new user
    saveToStorage(STORAGE_KEYS.USER_CART_PREFIX + newUser.id, { items: [], appliedCoupon: '' })
    saveToStorage(STORAGE_KEYS.USER_ORDERS_PREFIX + newUser.id, [])

    // Set as active session
    saveToStorage(STORAGE_KEYS.SESSION, newUser)
    return newUser
  },

  updateUserProfile: async (userId, updatedFields) => {
    const users = loadFromStorage(STORAGE_KEYS.USERS, [])
    const index = users.findIndex(u => u.id === userId)
    if (index !== -1) {
      users[index] = { ...users[index], ...updatedFields }
      saveToStorage(STORAGE_KEYS.USERS, users)
      saveToStorage(STORAGE_KEYS.SESSION, users[index])
      return users[index]
    }
    throw new Error('User not found.')
  },

  // --- CATALOG ---
  getProducts: async () => {
    return CATALOG_PRODUCTS
  },

  // --- CART PERSISTENCE PER USER ---
  getUserCart: async (userId) => {
    if (!userId) return computeCartTotals({ items: [] })
    const key = STORAGE_KEYS.USER_CART_PREFIX + userId
    const rawCart = loadFromStorage(key, { items: [], appliedCoupon: '' })
    return computeCartTotals(rawCart)
  },

  addToCart: async (userId, productId, quantity = 1, productDetails = {}) => {
    if (!userId) throw new Error('User must be logged in to modify cart')
    const key = STORAGE_KEYS.USER_CART_PREFIX + userId
    const cart = loadFromStorage(key, { items: [], appliedCoupon: '' })
    
    const existing = cart.items.find(i => i.productId === productId)
    if (existing) {
      existing.quantity += quantity
      existing.subTotal = existing.quantity * existing.price
    } else {
      const prod = CATALOG_PRODUCTS.find(p => p.id === productId) || productDetails
      cart.items.push({
        id: 'c_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4),
        productId: prod.id,
        name: prod.name,
        price: prod.price,
        originalPrice: prod.originalPrice || prod.price,
        unit: prod.unit || '',
        quantity: quantity,
        subTotal: prod.price * quantity,
        emoji: prod.emoji || '📦',
        deliveryBadge: prod.deliveryBadge || 'Standard'
      })
    }

    saveToStorage(key, cart)
    return computeCartTotals(cart)
  },

  updateQuantity: async (userId, itemId, quantity) => {
    if (!userId) return computeCartTotals({ items: [] })
    const key = STORAGE_KEYS.USER_CART_PREFIX + userId
    const cart = loadFromStorage(key, { items: [], appliedCoupon: '' })

    if (quantity <= 0) {
      cart.items = cart.items.filter(i => i.id !== itemId)
    } else {
      const item = cart.items.find(i => i.id === itemId)
      if (item) {
        item.quantity = quantity
        item.subTotal = item.quantity * item.price
      }
    }

    saveToStorage(key, cart)
    return computeCartTotals(cart)
  },

  removeFromCart: async (userId, itemId) => {
    if (!userId) return computeCartTotals({ items: [] })
    const key = STORAGE_KEYS.USER_CART_PREFIX + userId
    const cart = loadFromStorage(key, { items: [], appliedCoupon: '' })
    cart.items = cart.items.filter(i => i.id !== itemId)
    saveToStorage(key, cart)
    return computeCartTotals(cart)
  },

  applyCoupon: async (userId, couponCode) => {
    if (!userId) return
    const key = STORAGE_KEYS.USER_CART_PREFIX + userId
    const cart = loadFromStorage(key, { items: [], appliedCoupon: '' })
    cart.appliedCoupon = (couponCode || '').toUpperCase().trim()
    saveToStorage(key, cart)
    return computeCartTotals(cart)
  },

  removeCoupon: async (userId) => {
    if (!userId) return
    const key = STORAGE_KEYS.USER_CART_PREFIX + userId
    const cart = loadFromStorage(key, { items: [], appliedCoupon: '' })
    cart.appliedCoupon = ''
    saveToStorage(key, cart)
    return computeCartTotals(cart)
  },

  clearCart: async (userId) => {
    if (!userId) return computeCartTotals({ items: [] })
    const key = STORAGE_KEYS.USER_CART_PREFIX + userId
    const emptyCart = { items: [], appliedCoupon: '' }
    saveToStorage(key, emptyCart)
    return computeCartTotals(emptyCart)
  },

  // --- ORDERS PERSISTENCE PER USER ---
  getUserOrders: async (userId) => {
    if (!userId) return []
    const key = STORAGE_KEYS.USER_ORDERS_PREFIX + userId
    return loadFromStorage(key, [])
  },

  placeOrder: async (userId, orderData) => {
    if (!userId) throw new Error('User authentication required to place order')
    const ordersKey = STORAGE_KEYS.USER_ORDERS_PREFIX + userId
    const cartKey = STORAGE_KEYS.USER_CART_PREFIX + userId

    const existingOrders = loadFromStorage(ordersKey, [])
    
    const newOrder = {
      orderId: 'ORD-' + new Date().getFullYear() + '-' + Math.floor(100000 + Math.random() * 900000),
      placedAt: new Date().toISOString(),
      deliveryType: orderData.deliveryType || '⚡ Instamart 10-15 Mins Delivery',
      deliveryAddress: orderData.deliveryAddress || 'Primary Home Address',
      paymentMethod: orderData.paymentMethod || 'UPI (Instant)',
      items: orderData.items || [],
      itemTotal: orderData.subTotal || 0,
      discount: orderData.discountAmount || 0,
      deliveryFee: orderData.deliveryFee || 0,
      packagingFee: orderData.packagingFee || 0,
      totalAmount: orderData.finalTotal || 0,
      status: 'Order Placed (Arriving Soon)',
      eta: '12 - 15 mins'
    }

    // Insert at front of orders array
    existingOrders.unshift(newOrder)
    saveToStorage(ordersKey, existingOrders)

    // Clear cart after placing order
    saveToStorage(cartKey, { items: [], appliedCoupon: '' })

    return {
      order: newOrder,
      cart: computeCartTotals({ items: [] })
    }
  }
}
