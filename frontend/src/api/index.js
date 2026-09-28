import { http, query } from './http'

// Each group maps to one microservice behind the API gateway.

export const authApi = {
  requestOtp: (identifier) => http.post('/auth/otp', { identifier }),
  verify: (identifier, otp, name) => http.post('/auth/verify', { identifier, otp, name })
}

export const userApi = {
  me: () => http.get('/users/me'),
  updateProfile: (data) => http.put('/users/me', data),
  addresses: () => http.get('/users/me/addresses'),
  addAddress: (data) => http.post('/users/me/addresses', data),
  updateAddress: (id, data) => http.put(`/users/me/addresses/${id}`, data),
  deleteAddress: (id) => http.del(`/users/me/addresses/${id}`),
  wishlist: () => http.get('/users/me/wishlist'),
  addToWishlist: (productId) => http.post(`/users/me/wishlist/${productId}`),
  removeFromWishlist: (productId) => http.del(`/users/me/wishlist/${productId}`)
}

export const productApi = {
  search: (params) => http.get(`/products${query(params)}`),
  facets: (params) => http.get(`/products/facets${query(params)}`),
  categories: () => http.get('/products/categories'),
  suggest: (q) => http.get(`/products/suggest${query({ q })}`),
  get: (id) => http.get(`/products/${id}`),
  similar: (id) => http.get(`/products/${id}/similar`),
  batch: (ids) => http.post('/products/batch', { ids }),
  review: (id, data) => http.post(`/products/${id}/reviews`, data),
  // seller hub
  create: (data) => http.post('/products', data),
  update: (id, data) => http.put(`/products/${id}`, data),
  remove: (id) => http.del(`/products/${id}`),
  updateStock: (id, stock) => http.patch(`/products/${id}/stock`, { stock })
}

export const cartApi = {
  get: () => http.get('/cart'),
  count: () => http.get('/cart/count'),
  add: (productId, quantity = 1) => http.post('/cart/items', { productId, quantity }),
  update: (productId, quantity) => http.put(`/cart/items/${productId}`, { quantity }),
  remove: (productId) => http.del(`/cart/items/${productId}`),
  saveForLater: (productId) => http.post(`/cart/items/${productId}/save-for-later`),
  moveToCart: (productId) => http.post(`/cart/items/${productId}/move-to-cart`)
}

export const orderApi = {
  place: (data) => http.post('/orders', data),
  list: () => http.get('/orders'),
  get: (id) => http.get(`/orders/${id}`),
  cancel: (id, reason) => http.post(`/orders/${id}/cancel`, { reason }),
  // seller hub
  all: (page = 0, size = 20) => http.get(`/orders/admin/all${query({ page, size })}`),
  stats: () => http.get('/orders/admin/stats'),
  updateStatus: (id, status) => http.patch(`/orders/admin/${id}/status`, { status })
}
