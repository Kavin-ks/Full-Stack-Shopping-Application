# Flipkart Clone — Full Stack Microservices (Vue.js · Spring Boot · MongoDB)

Mini Project — *Shopping Cart Full Stack Web Application using Vue.js, Spring Boot and MongoDB using Microservice Architecture (alternate design)*.

## Architecture

```
                 Vue.js 3 SPA  (http://localhost:5180)
                          │  /api/**
                          ▼
           API Gateway — Spring Cloud Gateway (:8090)
           • routing   • JWT verification   • CORS
           • strips spoofed X-User-* headers
     ┌──────────────┬───────────┴──┬──────────────┬──────────────┐
     ▼              ▼              ▼              ▼
 User Service   Product Service  Cart Service   Order Service
   (:8084)         (:8081)         (:8082)        (:8083)
 OTP login, JWT  catalogue,      cart, save      checkout saga,
 profile,        search/filters, for later,      orders, cancel,
 addresses,      reviews, STOCK  price details   seller status
 wishlist            ▲   ▲            │  ▲            │
     │               │   └── batch ───┘  └── cart ────┤
     │               └──── reserve / release stock ───┘
     ▼               ▼              ▼              ▼
 flipkart_users  flipkart_products flipkart_carts flipkart_orders   (MongoDB)
```

Each service is an independent Spring Boot 4 application with its own port and its own database.
Services talk to each other over REST (`/internal/**` endpoints are **not** routed by the gateway, so browsers can't reach them).

### Checkout flow (order-service)
1. Read the user's cart from cart-service (live prices come from product-service — never from the browser).
2. **Reserve stock** in product-service: atomic `stock >= qty` updates, all-or-nothing, so two buyers can never oversell.
3. Save the order; if that fails, the reserved stock is released.
4. Clear the cart (saved-for-later items stay).
Cancelling an order releases its stock and marks prepaid payments as refunded.

## Features

| Area | What works |
|---|---|
| Login | Flipkart-style OTP login / sign-up with email or mobile, JWT sessions (7 days) |
| Home | Category tiles with sub-category menus, auto-sliding banners, deal shelves, top-rated / discount / new grids, recently viewed |
| Search | Live suggestions (keyboard navigable), "query in Category" rows |
| Listing | Filters (sub-category, price range, brand with search, rating, discount, F-Assured, out-of-stock), 6 sort orders, pagination, list view for phones/electronics & grid for others, mobile filter drawer — all synced to the URL |
| Product | Gallery, Add to Cart / Buy Now, offers, pincode delivery check, highlights, specifications, ratings & reviews (write your own), similar products |
| Cart | Guest cart (merged into your account at login), quantity limits by stock, save for later, price details, free delivery above ₹499 |
| Checkout | 4 steps: Login → Delivery Address → Order Summary → Payment (UPI, Card with Luhn check, Net Banking, COD) |
| Orders | Filters & search, tracking timeline, cancel with reason, refund status |
| Account | Profile, email/mobile update, manage addresses, wishlist |
| Seller Hub | Dashboard (revenue, orders by status, low stock), inventory with inline stock editing, add / edit / delete products, move orders through Shipped → Out for delivery → Delivered |

## Running it

Prerequisites: Java 21+, Maven, Node 18+, MongoDB running on `localhost:27017`.

```bash
./start.sh          # builds (first time), starts 5 services + frontend
./start.sh --rebuild
./stop.sh
```

Open **http://localhost:5180**.

* **Customer:** enter any 10-digit mobile number (starting 6-9) or an email. With no SMS gateway available locally, the OTP is shown on screen ("Demo mode") and printed in `backend/logs/user-service.log`. Turn this off with `app.otp.expose-in-response=false`.
* **Seller Hub:** log in with mobile **9999999999** (seeded admin account).

Ports: gateway 8090, product 8081, cart 8082, order 8083, user 8084, web 5180.
(8080 / 5173 were avoided because other local apps use them — change `server.port` in each `application.properties` if needed.)

## Product catalogue / stock file

`backend/product-service/src/main/resources/data/products.json` — 189 products across 7 categories, with INR prices, MRP, discount, stock, ratings, highlights, specs and reviews.
It is loaded into MongoDB automatically the first time product-service starts with an empty database. Product images live in `frontend/public/images/products/`.

Regenerate it with `python3 backend/tools/build_seed.py <dummyjson-products.json>` (source data: dummyjson.com).
To reset all data: drop the four `flipkart_*` databases and restart.

## API (through the gateway, `http://localhost:8090/api`)

| Method | Path | Auth |
|---|---|---|
| POST | `/auth/otp`, `/auth/verify` | – |
| GET/PUT | `/users/me` | user |
| GET/POST/PUT/DELETE | `/users/me/addresses[/{id}]` | user |
| GET/POST/DELETE | `/users/me/wishlist[/{productId}]` | user |
| GET | `/products?q&category&subCategory&brand&minPrice&maxPrice&minRating&minDiscount&assured&inStock&sort&page&size` | – |
| GET | `/products/{id}`, `/products/{id}/similar`, `/products/facets`, `/products/categories`, `/products/suggest?q=` | – |
| POST | `/products/batch` | – |
| POST | `/products/{id}/reviews` | user |
| POST/PUT/DELETE | `/products[/{id}]`, PATCH `/products/{id}/stock` | admin |
| GET | `/cart`, `/cart/count` | user |
| POST/PUT/DELETE | `/cart/items[/{productId}]`, POST `/cart/items/{id}/save-for-later`, `/move-to-cart` | user |
| POST/GET | `/orders`, GET `/orders/{id}`, POST `/orders/{id}/cancel` | user |
| GET | `/orders/admin/all`, `/orders/admin/stats`; PATCH `/orders/admin/{id}/status` | admin |

## Project layout

```
backend/
  api-gateway/  user-service/  product-service/  cart-service/  order-service/
  pom.xml                 # aggregator: `mvn package` builds all services
  tools/build_seed.py     # builds the product/stock seed file
frontend/                 # Vue 3 + Vue Router + Pinia + Vite
start.sh / stop.sh
_archive_novamart/        # the previous NovaMart version, kept for reference
```
